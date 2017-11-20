package edu.ucf.student.jdavies.cnt5008.sim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Wrapper class to simulate a multicast socket when not connected to a physical network.
 */
public class SimSocket extends MulticastSocket {
    private static final Random RANDOM = new Random();
    private LinkedBlockingQueue<DatagramPacket> incomingQueue = new LinkedBlockingQueue<>(1000);
    private Host host;
    private int port=-1;
    private InetAddress joinedGroup = null;
    private float lossRate = 0.0f;

    /**
     * Construct a new socket and bind to an anonymous port
     * @param host simulated host
     * @throws IOException
     */
    public SimSocket(Host host) throws IOException {
        this.host = host;
        this.port = host.bind(this);
    }

    /**
     * Construct a new socket
     * @param host simulated host
     * @param port the port to bind to
     * @throws IOException
     */
    public SimSocket(Host host,int port) throws IOException {
        this.host = host;
        this.port = port;
        host.bind(this);
    }

    /**
     * Get the simulated packet loss for the socket
     * @return packet loss [0,1]
     */
    public float getLossRate() {
        return lossRate;
    }

    /**
     * Set the simulated packet loss for the socket
     * @param lossRate packet loss rate [0,1]
     */
    public void setLossRate(float lossRate) {
        this.lossRate = lossRate;
    }

    /**
     * Get the port this socket is bound to
     * @return
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Join a multicast group
     * @param group group to join
     */
    @Override
    public void joinGroup(InetAddress group) {
        if (joinedGroup != null) {
            leaveGroup(joinedGroup);
        }
        host.joinGroup(group);
        joinedGroup = group;
    }

    /**
     * Leave a multicast group
     * @param group group to leave
     */
    @Override
    public void leaveGroup(InetAddress group) {
        if (joinedGroup == null) return;
        if (joinedGroup != group) return;
        host.leaveGroup(group);
        joinedGroup = null;
    }

    /**
     * Close this socket
     */
    @Override
    public void close() {
        host.unbind(this);
    }

    /**
     * Method to enqueue a packet for processing downstream via #receive
     * @param p the packet being sent to this socket
     * @see #receive(DatagramPacket)
     */
    void enqueue(DatagramPacket p) {
        try {
            if (RANDOM.nextFloat()<lossRate) {
                return; // packet drop
            }
            incomingQueue.put(p);
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * Receive a datagram packet
     * @param packet packet to fill in
     */
    @Override
    public void receive(DatagramPacket packet) {
        try {
            DatagramPacket incoming = incomingQueue.take();
            packet.setData(incoming.getData(),0,incoming.getLength());
        } catch (InterruptedException e) {
        }
    }

    /**
     * Get the InetAddress this socket is bound to
     * @return InetAddress
     */
    @Override
    public InetAddress getInetAddress() {
        return host.getInetAddress();
    }

    /**
     * Get the InetSocketAddress this socket is bound to
     * @return InetSocketAddress
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return new InetSocketAddress(host.getInetAddress(),port);
    }

    /**
     * Send a packet out using this socket
     * @param packet packet to send
     * @throws IOException
     */
    @Override
    public void send(DatagramPacket packet) throws IOException {
        try {
            host.send(packet);
        } catch (Exception e) {
            throw new IOException("Error sending packet",e);
        }
    }
}
