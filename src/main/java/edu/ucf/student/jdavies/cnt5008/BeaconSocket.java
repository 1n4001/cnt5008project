package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ucf.student.jdavies.cnt5008.proto.Beacon;
import edu.ucf.student.jdavies.cnt5008.proto.HostId;
import edu.ucf.student.jdavies.cnt5008.sim.Host;
import edu.ucf.student.jdavies.cnt5008.sim.SimSocket;

public class BeaconSocket implements Runnable {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private MulticastSocket socket;
    private DatagramSocket sender;
    private HostId hostId;
    private InetAddress groupAddress;
    private int port;
    private Thread thread;
    private ScheduledFuture<?> heartbeater, reaperSchedule;
    private boolean running;
    private Host host;
    private Map<HostId,Long> lastSeen = new ConcurrentHashMap<>();
    private Set<HostId> knownHosts = new CopyOnWriteArraySet<>(); //use concurrent sets to avoid sync blocks
    private Set<Listener> listeners = new CopyOnWriteArraySet<>();

    private BeaconSocket() {
    }

    public BeaconSocket(Host simHost, InetAddress groupAddress, int port) throws IOException {
        this();
        this.host = simHost;
        this.groupAddress = groupAddress;
        this.port = port;
        if (simHost == null) {
            sender = new DatagramSocket();
        }
        else {
            sender = new SimSocket(simHost);
        }
        // only ip4v supported
        byte[] bytes = sender.getInetAddress().getAddress();
        int val = ((bytes[0]&0xFF) << 24) + ((bytes[1]&0xFF) << 16) + ((bytes[2]&0xFF) << 8) + (bytes[3]&0xFF);
        hostId = HostId.newBuilder().setIp(val).setPort(sender.getPort()).build();
        System.err.println("Created beacon with hostId: "+hostId);
    }

    public HostId getHostId() {
        return hostId;
    }

    public Set<HostId> getHosts() {
        return Collections.unmodifiableSet(knownHosts);
    }

    public void start() throws IOException {

        /**
         * Setup multicast socket for beacons
         */
        if (host == null) {
            socket = new MulticastSocket(port);
        } else {
            socket = new SimSocket(host,port);
        }
        if (groupAddress.isMulticastAddress())
            socket.joinGroup(groupAddress);

        /**
         * Start listener thread
         */
        thread = new Thread(this);
        thread.start();

        /**
         * Schedule heartbeat for every 5 seconds
         */
        heartbeater = scheduler.scheduleAtFixedRate(()->send(Beacon.Status.PRESENT),1,1, TimeUnit.SECONDS);

        reaperSchedule = scheduler.scheduleAtFixedRate(this::reaper,11,5,TimeUnit.SECONDS);

        /**
         * Announce our presence.
         */
        send(Beacon.Status.PRESENT);
    }

    private void send(Beacon.Status status) {
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
//                System.err.println("Sending "+status+" from "+hostId.getIp()+":"+hostId.getPort()+" to "+groupAddress.getHostAddress()+":"+port);
                sender.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
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
        reaperSchedule.cancel(true);
        reaperSchedule = null;
        knownHosts.clear();
    }

    /**
     * Method for handling unresponsive nodes or nodes which have not been seen in a sufficiently length of time
     *
     * In practice this may be much higher, say around 30 seconds.
     */
    private void reaper() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<HostId,Long>> it = lastSeen.entrySet().iterator(); it.hasNext();) {
            Map.Entry<HostId,Long> entry = it.next();
            if (now - entry.getValue() > 5000) {
                // reap!
                it.remove();
                knownHosts.remove(entry.getKey());
                listeners.stream().forEach(listener -> listener.hostParted(entry.getKey()));
            }
        }
    }

    /**
     * Loop handler for incoming heartbeat packets.
     */
    public void run() {
        byte[] packetBytes = new byte[4096];
        DatagramPacket packet = new DatagramPacket(packetBytes,packetBytes.length);
        running = true;
        while(running) {
            try {
                packet.setData(packetBytes, 0, packetBytes.length);
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
                    lastSeen.remove(beacon.getHostId());

                    /**
                     * Let our listeners know a new host is online
                     */
                    listeners.stream().forEach(listener -> listener.hostParted(beacon.getHostId()));
                    continue;
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
                lastSeen.put(beacon.getHostId(),System.currentTimeMillis());
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
