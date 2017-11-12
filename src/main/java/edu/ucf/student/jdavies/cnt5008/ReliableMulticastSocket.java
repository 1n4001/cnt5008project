package edu.ucf.student.jdavies.cnt5008;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ByteString;
import edu.ucf.student.jdavies.cnt5008.proto.Header;
import edu.ucf.student.jdavies.cnt5008.proto.HostId;
import edu.ucf.student.jdavies.cnt5008.proto.Message;
import edu.ucf.student.jdavies.cnt5008.sim.Host;
import edu.ucf.student.jdavies.cnt5008.sim.SimSocket;

public class ReliableMulticastSocket implements Runnable, Closeable {
    private static final int QUEUE_SIZE = 4;
    private MulticastSocket socket = null;
    private AtomicInteger sequence = new AtomicInteger(0);
    private ReliableMode mode;
    private BeaconSocket registry;
    private Host host;
    private HostId hostId;
    private volatile boolean running;
    private InetSocketAddress socketAddress;
    private Thread listenerThread;
    private volatile boolean closed=false;
    private Map<Integer,Collection<HostId>> pendingAcks = new ConcurrentHashMap<>();
    private Map<Integer,CompletableFuture<Void>> pendingFutures = new ConcurrentHashMap<>();
    private Map<Long,Integer> hostToSequence = new ConcurrentHashMap<>();

    public ReliableMulticastSocket(Host host, InetSocketAddress socketAddress, ReliableMode mode) throws IOException {
        this.host = host;
        registry = new BeaconSocket(host,socketAddress.getAddress(),socketAddress.getPort()+1);
        registry.start();
        hostId = registry.getHostId();
        this.socketAddress = socketAddress;
        socket = new SimSocket(host,socketAddress.getPort());
        socket.setReuseAddress(true);
        socket.joinGroup(socketAddress.getAddress());
        this.mode = mode;
        listenerThread = new Thread(this);
        listenerThread.start();
    }

    public BeaconSocket getRegistry() {
        return registry;
    }

    @Override
    public void close() {
        running = false;
        registry.stop();
        registry = null;
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.join();
            }
        }
        catch (InterruptedException ie) {}
        finally {
            listenerThread = null;
            closed = true;
        }
    }

    public Message receiveMessage() {
        return null;
    }

    public Future<Void> send(byte[] payload) throws IOException {
        if (closed) throw new IOException("Socket is closed");

        Message message = Message.newBuilder()
                .setSource(hostId)
                .setPayload(ByteString.copyFrom(payload))
                .setHeader(Header.newBuilder()
                    .setModeValue(mode==ReliableMode.ACK?Header.Mode.ACK_VALUE:Header.Mode.NACK_VALUE)
                .setSequence(sequence.getAndIncrement())
                .build())
                .setSource(hostId).build();
        byte[] bytes = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes,0,bytes.length,socketAddress);
        CompletableFuture<Void> future = new CompletableFuture<>();
        pendingFutures.put(message.getHeader().getSequence(),future);
        pendingAcks.put(message.getHeader().getSequence(),new CopyOnWriteArraySet<>(registry.getHosts()));
        System.err.println("Sending from host "+hostId.getIp()+":"+hostId.getPort()+" --> "+socketAddress);
        System.err.println("\tMessage: "+message);
        socket.send(packet);
        return future;
    }

    public void run() {
        running = true;
        byte[] buffer = new byte[4096];
        DatagramPacket p = new DatagramPacket(buffer,0,buffer.length);
        while (running) {
            try {
                p.setData(buffer,0,buffer.length);
                socket.receive(p);
                System.err.println("Host "+hostIdToInetAddress(hostId.getIp())+":"+hostId.getPort()+" received packet...");
                Message message = Message.parseFrom(ByteBuffer.wrap(p.getData(),0,p.getLength()));
                if (message.getHeader().getResponse()) {
                    Collection<HostId> hosts = pendingAcks.get(message.getHeader().getSequence());
                    hosts.remove(message.getSource());
                    if (hosts.isEmpty()) {
                        pendingAcks.remove(message.getHeader().getSequence());
                        pendingFutures.remove(message.getHeader().getSequence()).complete(null);
                    }
                }
                else {
                    if (message.getHeader().getMode() == Header.Mode.ACK) {
                        Message response = Message.newBuilder()
                                .setHeader(Header.newBuilder()
                                        .setModeValue(mode==ReliableMode.ACK?Header.Mode.ACK_VALUE:Header.Mode.NACK_VALUE)
                                        .setSequence(sequence.getAndIncrement())
                                        .setResponse(true)
                                        .build())
                                .setSource(hostId).build();
                        byte[] bytes = response.toByteArray();
                        DatagramPacket ackPacket = new DatagramPacket(bytes,0,bytes.length,new InetSocketAddress(hostIdToInetAddress(message.getSource().getIp()),socketAddress.getPort()));
                        socket.send(ackPacket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static InetAddress hostIdToInetAddress(int ip) {
        byte[] bytes=new byte[4];
        bytes[0] = (byte)((ip >> 24) & 0xFF);
        bytes[1] = (byte)((ip >> 16) & 0xFF);
        bytes[2] = (byte)((ip >> 8) & 0xFF);
        bytes[3] = (byte)((ip) & 0xFF);
        try {
            return InetAddress.getByAddress(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
