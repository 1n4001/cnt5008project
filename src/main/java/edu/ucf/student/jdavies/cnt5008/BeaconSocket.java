package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ExtensionRegistry;
import edu.ucf.student.jdavies.cnt5008.proto.Beacon;
import edu.ucf.student.jdavies.cnt5008.proto.HostId;
import edu.ucf.student.jdavies.cnt5008.proto.Proto;

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

    private BeaconSocket() {
    }

    public BeaconSocket(InetAddress groupAddress, int port, HostId hostId) throws IOException {
        this();
        this.hostId = hostId;
        this.groupAddress = groupAddress;
        this.port = port;
    }

    public Set<HostId> getHosts() {
        return Collections.unmodifiableSet(knownHosts);
    }

    public void start() throws IOException {
        /**
         * Make sure we're not running
         */
        stop();

        /**
         * Create socket we will send from
         */
        sender = new DatagramSocket();

        /**
         * Setup multicast socket for beacons
         */
        socket = new MulticastSocket(port);
        if (groupAddress.isMulticastAddress())
            socket.joinGroup(groupAddress);
        System.err.println("Socket bound to: "+socket.getLocalSocketAddress());

        /**
         * Start listener thread
         */
        thread = new Thread(this::heartbeatHandler);
        thread.run();

        /**
         * Schedule heartbeat for every 5 seconds
         */
        heartbeater = scheduler.scheduleAtFixedRate(()->send(Beacon.Status.PRESENT),5,5, TimeUnit.SECONDS);

        /**
         * Announce our presence.
         */
        send(Beacon.Status.PRESENT);
    }

    private void send(Beacon.Status status) {
        /**
         * Don't send if we're not running / don't have sender socket
         */
        if (sender == null) return;

        /**
         * Build up beacon object
         */
        Beacon beacon = Beacon.newBuilder()
                .setHostId(hostId)
                .setStatus(status)
                .build();

        /**
         * Generate and send packet
         */
        byte[] bytes = beacon.toByteArray();
        DatagramPacket dp = new DatagramPacket(bytes,bytes.length,groupAddress,port);
        synchronized (sender) {
            try {
                System.err.println("Sending "+status+" from "+hostId.getIp()+":"+hostId.getPort()+" to "+groupAddress.getHostAddress()+":"+port);
                sender.send(dp);
            } catch (IOException e) {
                System.err.println("Error sending Beacon");
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
        System.err.println("heartbeatHandler running...");
        while(running) {
            try {
                System.err.println("heartbeatHandler waiting for packet...");
                packet.setData(packetBytes, 0, packetBytes.length);
                socket.receive(packet);
                Beacon beacon = Beacon.parseFrom(ByteBuffer.wrap(packet.getData(),packet.getOffset(),packet.getLength()));
                System.err.println("RECEIVED from " + hostId.getIp());
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
