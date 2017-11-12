package edu.ucf.student.jdavies.cnt5008.sim;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Host class receiving datagram packets
 */
public class Host {
    private InetAddress inetAddress;
    private Map<Integer,Collection<SimSocket>> binds = new ConcurrentHashMap<>();
    private Switch uplink = null;
    private AtomicInteger portCounter = new AtomicInteger(Short.MAX_VALUE);

    public Host(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    void setUplink(Switch uplink) {
        this.uplink = uplink;
    }

    public int bind(SimSocket socket) {
        int port = socket.getPort();
        if (port < 0) {
            port = portCounter.getAndIncrement();
            System.err.println("Allocated port: "+port);
        }
        else {
            System.err.println("Using port: "+port);
        }
        binds.computeIfAbsent(port,Host::newSocketCollection).add(socket);
        return port;
    }
    public void unbind(SimSocket socket) {
        binds.computeIfAbsent(socket.getPort(),Host::newSocketCollection).remove(socket);
    }

    void joinGroup(InetAddress group) {
        if (group.isMulticastAddress() && uplink != null) {
            uplink.joinGroup(this,group);
        }
    }
    void leaveGroup(InetAddress group) {
        if (group.isMulticastAddress() && uplink != null) {
            uplink.leaveGroup(this,group);
        }
    }

    public void send(DatagramPacket packet) {
        try {
            // add some latency like we're on a 10mbit link
            Thread.sleep(1);
        } catch (InterruptedException ie) {}
        if (packet.getAddress() != null && packet.getAddress().equals(inetAddress)) {
            if (binds.containsKey(packet.getPort())) {
                binds.get(packet.getPort()).forEach((socket) -> socket.enqueue(packet));
            }
        }
        else if (uplink != null) {
            uplink.send(this,packet);
        }
    }

    public void receive(DatagramPacket packet) {
        try {
            // add some latency like we're on a 10mbit link
            Thread.sleep(1);
        } catch (InterruptedException ie) {}
        if (!binds.containsKey(packet.getPort())) return;
        binds.get(packet.getPort()).forEach((socket) -> socket.enqueue(packet));
    }

    private static Collection<SimSocket> newSocketCollection(int port) {
        return new CopyOnWriteArraySet<>();
    }
}
