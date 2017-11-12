package edu.ucf.student.jdavies.cnt5008.sim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

public class SimSocket extends MulticastSocket {
    private LinkedBlockingQueue<DatagramPacket> incomingQueue = new LinkedBlockingQueue<>(1000);
    private Host host;
    private int port=-1;
    private InetAddress joinedGroup = null;

    public SimSocket(Host host) throws IOException {
        this.host = host;
        this.port = host.bind(this);
    }

    public SimSocket(Host host,int port) throws IOException {
        this.host = host;
        this.port = port;
        host.bind(this);
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
