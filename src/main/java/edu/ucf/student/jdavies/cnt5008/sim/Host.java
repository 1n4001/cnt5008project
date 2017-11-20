package edu.ucf.student.jdavies.cnt5008.sim;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Host class receiving datagram packets.  This is primarily to represent Layers 1-3 of the OSI stack + OS implementation.
 */
public class Host {
    private InetAddress inetAddress;
    private Executor executor = Executors.newSingleThreadExecutor();
//    private Executor executor = Executors.newCachedThreadPool();
    private Map<Integer,Collection<SimSocket>> binds = new ConcurrentHashMap<>();
    private Switch uplink = null;
    private AtomicInteger portCounter = new AtomicInteger(Short.MAX_VALUE);

    /**
     * Construct a new host represented by the given inetAddress
     * @param inetAddress InetAddress of host network interface
     */
    public Host(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    /**
     * Get the inet address of the host
     * @return bound inet address
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Set the uplink of this host to a switch
     * @param uplink the uplink to use
     */
    void setUplink(Switch uplink) {
        this.uplink = uplink;
    }

    /**
     * Bind a socket to a port
     * @param socket socket to bind (uses SocketAddress from socket to get port)
     * @return port bound to
     */
    public int bind(SimSocket socket) {
        int port = socket.getPort();
        if (port < 0) {
            port = portCounter.getAndIncrement();
        }
        binds.computeIfAbsent(port,Host::newSocketCollection).add(socket);
        return port;
    }

    /**
     * Unbind a socket from a port
     * @param socket socket to unbind
     */
    public void unbind(SimSocket socket) {
        binds.computeIfAbsent(socket.getPort(),Host::newSocketCollection).remove(socket);
    }

    /**
     * Join a multicast group
     * @param group group to join
     */
    void joinGroup(InetAddress group) {
        if (group.isMulticastAddress() && uplink != null) {
            uplink.joinGroup(this,group);
        }
    }

    /**
     * Leave a multicast group
     * @param group group to leave
     */
    void leaveGroup(InetAddress group) {
        if (group.isMulticastAddress() && uplink != null) {
            uplink.leaveGroup(this,group);
        }
    }

    /**
     * Send a datagram packet from this host to another via the switch/uplink we're connected to.
     * @param packet the packet to send
     */
    public void send(DatagramPacket packet) {
        executor.execute(() -> {
            if (packet.getAddress() != null && packet.getAddress().equals(inetAddress)) {
                if (binds.containsKey(packet.getPort())) {
                    binds.get(packet.getPort()).forEach((socket) -> socket.enqueue(packet));
                }
            }
            else if (uplink != null) {
                uplink.send(this,packet);
            }
        });
    }

    /**
     * Handle a packet received from another node (a switch notifies us)
     * @param packet the packet being delivered
     */
    public void receive(DatagramPacket packet) {
        if (!binds.containsKey(packet.getPort())) return;
        binds.get(packet.getPort()).forEach((socket) -> socket.enqueue(packet));
    }

    /**
     * Helper method for computeIfAbsent
     * @param port unused.
     * @return new concurrent collection
     */
    private static Collection<SimSocket> newSocketCollection(int port) {
        return new CopyOnWriteArraySet<>();
    }
}
