package edu.ucf.student.jdavies.cnt5008.sim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class SimSocket extends MulticastSocket {
    private static final Random RANDOM = new Random();
    private LinkedBlockingQueue<DatagramPacket> incomingQueue = new LinkedBlockingQueue<>(1000);
    private Host host;
    private int port=-1;
    private InetAddress joinedGroup = null;
    private float lossRate = 0.0f;

    public SimSocket(Host host) throws IOException {
        this.host = host;
        this.port = host.bind(this);
    }

    public SimSocket(Host host,int port) throws IOException {
        this.host = host;
        this.port = port;
        host.bind(this);
    }

    public float getLossRate() {
        return lossRate;
    }

    public void setLossRate(float lossRate) {
        this.lossRate = lossRate;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void joinGroup(InetAddress group) {
        if (joinedGroup != null) {
            leaveGroup(joinedGroup);
        }
        host.joinGroup(group);
        joinedGroup = group;
    }

    @Override
    public void leaveGroup(InetAddress group) {
        if (joinedGroup == null) return;
        if (joinedGroup != group) return;
        host.leaveGroup(group);
        joinedGroup = null;
    }

    @Override
    public void close() {
        host.unbind(this);
    }

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

    @Override
    public void receive(DatagramPacket packet) {
        try {
            DatagramPacket incoming = incomingQueue.take();
            packet.setData(incoming.getData(),0,incoming.getLength());
        } catch (InterruptedException e) {
        }
    }

    @Override
    public InetAddress getInetAddress() {
        return host.getInetAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return new InetSocketAddress(host.getInetAddress(),port);
    }

    public void send(DatagramPacket packet) throws IOException {
        try {
            host.send(packet);
        } catch (Exception e) {
            throw new IOException("Error sending packet",e);
        }
    }
}
