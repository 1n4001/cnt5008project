package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ucf.student.jdavies.cnt5008.proto.Beacon;
import edu.ucf.student.jdavies.cnt5008.proto.HostId;

public class BeaconSocket {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private MulticastSocket socket;
    private DatagramSocket sender;
    private HostId hostId;
    private InetAddress groupAddress;
    private int port;
    private Thread thread;
    private ScheduledFuture<?> heartbeater;
    private boolean running;
    private Set<HostId> knownHosts = new CopyOnWriteArraySet<>(); //use concurrent sets to avoid sync blocks
    private Set<Listener> listeners = new CopyOnWriteArraySet<>();

    private BeaconSocket() {} //intentional

    public BeaconSocket(InetAddress groupAddress, int port, HostId hostId) throws IOException {
        this.hostId = hostId;
        this.groupAddress = groupAddress;
        this.port = port;
    }

    public Set<HostId> getHosts() {
        return Collections.unmodifiableSet(knownHosts);
    }

    public void start() throws IOException {
        stop();
        sender = new DatagramSocket();
        socket = new MulticastSocket(port);
        socket.joinGroup(groupAddress);
        sender = new DatagramSocket();
        thread = new Thread(this::heartbeatHandler);
        thread.run();
        System.err.println("Schedule PRESENT");
        heartbeater = scheduler.scheduleAtFixedRate(()->send(Beacon.Status.PRESENT),5,5, TimeUnit.SECONDS);
        System.err.println("Send PRESENT");
        send(Beacon.Status.PRESENT);
    }

    private void send(Beacon.Status status) {
        if (sender == null) return;
        Beacon beacon = Beacon.newBuilder()
                .setHostId(hostId)
                .setStatus(status)
                .build();
        byte[] bytes = beacon.toByteArray();
        DatagramPacket dp = new DatagramPacket(bytes,bytes.length,groupAddress,port);
        synchronized (sender) {
            try {
                System.err.println("Sending packet from "+hostId+" to "+groupAddress.getHostAddress()+":"+port);
                sender.send(dp);
            } catch (IOException e) {
                System.err.println("Error sending heartbeat");
            }
        }
    }

    public void stop() {
        running = false;
        if (heartbeater != null) {
            heartbeater.cancel(true);
            heartbeater = null;
        }
        if (thread != null) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                thread = null;
            }
        }
        send(Beacon.Status.GONE);
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (sender != null) {
            sender.close();
            sender = null;
        }
    }

    /**
     * Loop handler for incoming heartbeat packets.
     */
    private void heartbeatHandler() {
        byte[] packetBytes = new byte[4096];
        DatagramPacket packet = new DatagramPacket(packetBytes,packetBytes.length);
        while(running) {
            try {
                socket.receive(packet);
                Beacon beacon = Beacon.parseFrom(ByteBuffer.wrap(packet.getData(),packet.getOffset(),packet.getLength()));
                if (hostId.equals(beacon.getHostId())) {
                    /**
                     * skip our own beacon messages
                     */
                    continue;
                }
                if (beacon.getStatus() == Beacon.Status.GONE) {
                    /**
                     * Remove host id from our known list.
                     */
                    knownHosts.remove(beacon.getHostId());

                    /**
                     * Let our listeners know a new host is online
                     */
                    listeners.stream().forEach(listener -> listener.hostParted(beacon.getHostId()));
                }
                else if (knownHosts.add(beacon.getHostId())) {
                    /**
                     * Discovered new host, reply with a 'PRESENT' beacon of our own to aid fast discovery
                     */
                    send(Beacon.Status.PRESENT);

                    /**
                     * Notify our listeners a new host joined
                     */
                    listeners.stream().forEach(listener -> listener.hostJoined(beacon.getHostId()));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void hostJoined(HostId hostId);
        void hostParted(HostId hostId);
    }
}
