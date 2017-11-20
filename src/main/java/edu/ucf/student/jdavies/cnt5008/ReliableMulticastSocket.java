package edu.ucf.student.jdavies.cnt5008;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;
import edu.ucf.student.jdavies.cnt5008.proto.Header;
import edu.ucf.student.jdavies.cnt5008.proto.HostId;
import edu.ucf.student.jdavies.cnt5008.proto.Message;
import edu.ucf.student.jdavies.cnt5008.sim.Host;
import edu.ucf.student.jdavies.cnt5008.sim.SimSocket;

/**
 * An implementation of a reliable multicast assignment.  The implementation can run on simulated or on real sockets.
 * The multicast socket uses the next port up from the selected port to run a beacon/heartbeat service for node discovery
 * since java does not get notified of who (e.g. list of InetAddress) is currently joined.  This could be taken care
 * of using some clever JNI or JNA code, but it is beyond the scope of this project.
 */
public class ReliableMulticastSocket implements Closeable, BeaconSocket.Listener {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static final int QUEUE_SIZE = 256;
    private SimSocket socket = null;
    private AtomicInteger sequence = new AtomicInteger(0);
    private ReliableMode mode;
    private BeaconSocket registry;
    private Host host;
    private HostId hostId;
    private volatile boolean running;
    private InetSocketAddress socketAddress;
    private Thread listenerThread;
    private volatile boolean closed=false;
    private AtomicInteger retransmits = new AtomicInteger(0);
    private Message[] history = new Message[QUEUE_SIZE];
    private Map<Integer,Collection<HostId>> pendingAcks = new ConcurrentHashMap<>();
    private Map<Integer,CompletableFuture<Void>> pendingFutures = new ConcurrentHashMap<>();
    private Map<Integer,byte[]> pendingMessages = new ConcurrentHashMap<>();
    private Map<Integer,Long> pendingTimes = new ConcurrentHashMap<>();
    private Map<HostId,Integer> hostToSequence = new ConcurrentHashMap<>();
    private Map<HostId,Message[]> hostToMessages = new ConcurrentHashMap<>();
    private ScheduledFuture<?> retransmitFuture = null;
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Construct a new reliable multicast socket.
     *
     * @param host simulated host (passing null will use a real host and go out over the network)
     * @param socketAddress the socket address to bind to
     * @param mode the reliable multicast mode (ACK or NACK)
     * @throws IOException
     */
    public ReliableMulticastSocket(Host host, InetSocketAddress socketAddress, ReliableMode mode) throws IOException {
        this.host = host;
        registry = new BeaconSocket(host,socketAddress.getAddress(),socketAddress.getPort()+1);
        registry.start();
        registry.addListener(this);
        hostId = registry.getHostId();
        this.socketAddress = socketAddress;
        socket = new SimSocket(host,socketAddress.getPort());
        socket.setReuseAddress(true);
        socket.joinGroup(socketAddress.getAddress());
        this.mode = mode;
        listenerThread = new Thread(this::receive);
        listenerThread.start();
        retransmitFuture = executor.scheduleAtFixedRate(this::retransmit,250,250, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the registry to find out which hosts are joined.
     * @return BeaconSocket for discovery.
     */
    public BeaconSocket getRegistry() {
        return registry;
    }

    /**
     * Callback for when another host has joined the multicast group
     * @param hostId identity of the host joining
     */
    @Override
    public void hostJoined(HostId hostId) {

    }

    /**
     * Callback for when a host has left the multicast group.  Cleans up any pending acknowledgements and completes
     * any futures if parting host was holding up a packet.
     *
     * @param hostId identify of the parting host
     */
    @Override
    public void hostParted(HostId hostId) {
        for (Iterator<Map.Entry<Integer,Collection<HostId>>> it = pendingAcks.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer,Collection<HostId>> entry = it.next();
            if (entry.getValue().remove(hostId)) {
                if (entry.getValue().isEmpty()) {
                    it.remove();
                    pendingMessages.remove(entry.getKey());
                    pendingTimes.remove(entry.getKey());
                    pendingFutures.remove(entry.getKey()).complete(null);

                }
            }
        }
    }

    /**
     * Close any resources used by this socket.
     */
    @Override
    public void close() {
        running = false;
        retransmitFuture.cancel(true);
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

    /**
     * TODO: if this were published to an application layer it would be implemented
     * @return message (in-order)
     */
    public Message receiveMessage() {
        return null;
    }

    /**
     * Get the operating mode of this reliable multicast socket instance
     * @return current mode of operation.
     */
    public ReliableMode getMode() {
        return mode;
    }

    /**
     * Get the host identifier for this socket
     * @return host identifier
     */
    public HostId getHostId() {
        return hostId;
    }

    /**
     * Get the socket address this socket is bound to
     * @return bound socket address
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Check to see if the socket is closed
     * @return true if socket has been closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Send message bytes to group, reliably.
     * @param payload message bytes to send
     * @return future which will resolve once packet has been acknowledged (or immediately for NACK)
     * @throws IOException
     */
    public Future<Void> send(byte[] payload) throws IOException {
        if (closed) throw new IOException("Socket is closed");

        lock.lock();
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        try {
            int sequenceId = sequence.getAndIncrement();
//        System.err.println("Send "+sequenceId);
//        Thread.dumpStack();
            Header.Mode headerMode = mode == ReliableMode.NACK ? Header.Mode.NACK : Header.Mode.ACK;
            boolean waitFor = false;
            if (mode == ReliableMode.NACK && (sequenceId % QUEUE_SIZE) == QUEUE_SIZE - 1) {
//            System.err.println("Changing NACK to ACK (sequence: "+sequenceId+", QUEUE_SIZE: "+QUEUE_SIZE+")");
                headerMode = Header.Mode.ACK;
                waitFor = true;
            }
            Message message = Message.newBuilder()
                    .setSource(hostId)
                    .setPayload(ByteString.copyFrom(payload))
                    .setHeader(Header.newBuilder().setModeValue(headerMode.getNumber()).setSequence(sequenceId).build())
                    .setSource(hostId).build();
            byte[] bytes = message.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, socketAddress);

            Message previousMessage = history[message.getHeader().getSequence() % QUEUE_SIZE];
            if (previousMessage != null && previousMessage.getHeader().getMode().getNumber() == Header.Mode.ACK_VALUE) {
                Future<Void> pendingFuture = pendingFutures.get(previousMessage.getHeader().getSequence());
                if (pendingFuture != null && !pendingFuture.isDone()) {
                    try {
                        pendingFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            history[message.getHeader().getSequence() % QUEUE_SIZE] = message;
            if (message.getHeader().getMode() == Header.Mode.NACK) {
//            System.err.println("-->Complete future");
                //NACKs complete immediately, but keep the packet in the queue in case someone NACKs back asking for it
                future.complete(null);
            } else {
//            System.err.println("-->Add pending");
                pendingFutures.put(sequenceId, future);
                pendingAcks.put(sequenceId, new CopyOnWriteArraySet<>(registry.getHosts()));
                pendingTimes.put(sequenceId, System.currentTimeMillis());
                pendingMessages.put(sequenceId, bytes);
            }
//        System.err.println("Sending "+message.getHeader().getMode()+" from host "+hostId.getIp()+":"+hostId.getPort()+" --> "+socketAddress);
            socket.send(packet);
            if (waitFor) {
                try {
//                System.err.println("Wait for packet...");
                    future.get();
                } catch (ExecutionException | InterruptedException ie) {
                    throw new IOException(ie);
                }
            }
        } finally {
            lock.unlock();
        }
        return future;
    }

    /**
     * Get the underlying datagram socket used by this reliable multicast socket
     * @return backing socket
     */
    public SimSocket getSocket() {
        return socket;
    }

    /**
     * Get the number of times a packets have been retransmitted
     * @return number of retransmits
     */
    public int getNumberOfRetransmits() {
        return retransmits.get();
    }

    /**
     * Main loop for receiving packets and handling NACK/ACK responses.
     */
    private void receive() {
        running = true;
        byte[] buffer = new byte[4096];
        DatagramPacket p = new DatagramPacket(buffer,0,buffer.length);
        while (running) {
            try {
                p.setData(buffer,0,buffer.length);
                socket.receive(p);
                if (verbose)
                    System.err.println("Host "+hostIdToInetAddress(hostId.getIp())+":"+hostId.getPort()+" received packet...");
                Message message = Message.parseFrom(ByteBuffer.wrap(p.getData(),0,p.getLength()));
                int sequenceId = message.getHeader().getSequence();
                if (message.getHeader().getResponse()) {
                    /**
                     * This message is a response from another node -- either a NACK or an ACK for a sequence
                     */
                    if (message.getHeader().getMode() == Header.Mode.NACK){
                        if (verbose) {
                            System.err.println("  -nacked "+message.getHeader().getSequence());
                        }
                        boolean handled = false;
                        for (int i=0;i<QUEUE_SIZE;i++) {
                            if (history[i]==null) continue;
                            if (history[i].getHeader().getSequence() != sequenceId) continue;
                            SocketAddress retransmitAddress = computeSocketAddress(message.getSource(),socketAddress.getPort());
                            byte[] bytes = history[i].toByteArray();
                            DatagramPacket packet = new DatagramPacket(bytes,0,bytes.length,retransmitAddress);
                            if (verbose) {
                                System.err.println("  -resent "+message.getHeader().getSequence());
                            }

                            socket.send(packet);
                            retransmits.incrementAndGet();
                            handled = true;
                            break;
                        }
                        if (!handled) {
                            System.err.println("Unhandled NACK");
                        }
                    }
                    else {
                        Collection<HostId> hosts = pendingAcks.get(sequenceId);
                        if (hosts != null) {
                            hosts.remove(message.getSource());
                        }
                        if (hosts == null || hosts.isEmpty()) {
                            complete(sequenceId);
                        }
                    }
                }
                else {
                    /**
                     * Process reception of a packet from another node
                     */
                    if (mode == ReliableMode.ACK) {
                        ack(message);
                    }
                    else {
                        Integer lastSequenceId = hostToSequence.get(message.getSource());
                        Message[] hostQueue = hostToMessages.computeIfAbsent(message.getSource(), (host) -> new Message[QUEUE_SIZE]);
                        int offset = sequenceId % QUEUE_SIZE;
                        hostQueue[offset] = message;
                        if (verbose)
                            System.err.printf("sequence %d lastSequence %d%n",sequenceId,lastSequenceId);
                        if (lastSequenceId == null || sequenceId == lastSequenceId+1) {
                            //first packet seen for host or next logical sequence
                            hostToSequence.put(message.getSource(),sequenceId);

                            if (lastSequenceId != null) {
                                int oldLastSequenceId = lastSequenceId;
                                int lastOffset = lastSequenceId % QUEUE_SIZE;
                                for (int i = lastOffset; i < QUEUE_SIZE; i++) {
                                    if (hostQueue[i] == null) break;
                                    int testSequence = hostQueue[i].getHeader().getSequence();
                                    if (testSequence < lastSequenceId) break;
                                    if (testSequence > lastSequenceId + 1) break;
                                    hostToSequence.put(message.getSource(), testSequence);
                                    lastSequenceId = testSequence;
                                }
                                for (int i = 0; i < lastOffset; i++) {
                                    if (hostQueue[i] == null) break;
                                    int testSequence = hostQueue[i].getHeader().getSequence();
                                    if (testSequence < lastSequenceId) break;
                                    if (testSequence > lastSequenceId + 1) break;
                                    hostToSequence.put(message.getSource(), testSequence);
                                    lastSequenceId = testSequence;
                                }
                                if (verbose && lastSequenceId-oldLastSequenceId > 1)
                                    System.err.println("Sequence skipping "+oldLastSequenceId+" -> "+lastSequenceId);
                            }
                            if (message.getHeader().getMode() == Header.Mode.ACK) {
                                ack(message,lastSequenceId);
                            }
                            //TODO: process sequenceId -> lastSequenceId for this host
                        }
                        else if (sequenceId>lastSequenceId+1) {
                            //send NACK for lastSequenceId+1;
                            nack(message,lastSequenceId+1);
                        }
                        else if (sequenceId == lastSequenceId && message.getHeader().getMode() == Header.Mode.ACK) {
                            ack(message,lastSequenceId);
                        }
                        // If sequenceId <= lastSequenceId then do nothing, it was a duplicate
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Helper method for sending ACK messages
     * @param message the message to ACK
     * @throws IOException
     */
    private void ack(Message message ) throws IOException {
        if (verbose)
            System.err.println("Acking "+message.getHeader().getSequence());
        Message response = Message.newBuilder()
                .setHeader(Header.newBuilder()
                        .setModeValue(Header.Mode.ACK_VALUE)
                        .setSequence(message.getHeader().getSequence())
                        .setResponse(true)
                        .build())
                .setSource(hostId).build();
        byte[] bytes = response.toByteArray();
//                        System.err.println("Acking "+message.getHeader().getSequence());
        DatagramPacket ackPacket = new DatagramPacket(bytes,0,bytes.length,new InetSocketAddress(hostIdToInetAddress(message.getSource().getIp()),socketAddress.getPort()));
        socket.send(ackPacket);
    }

    /**
     * Ack a specific sequence given a message
     * @param message message to get host / socket info from
     * @param ackSequence the seqeunce id to ack
     * @throws IOException
     */
    private void ack(Message message, int ackSequence) throws IOException {
        InetSocketAddress ackAddr = new InetSocketAddress(hostIdToInetAddress(message.getSource().getIp()),socketAddress.getPort());
        if (verbose)
            System.err.println("Acking "+message.getHeader().getSequence() + " to "+ackAddr);
        Message response = Message.newBuilder()
                .setHeader(Header.newBuilder()
                        .setModeValue(Header.Mode.ACK_VALUE)
                        .setSequence(ackSequence)
                        .setResponse(true)
                        .build())
                .setSource(hostId).build();
        byte[] bytes = response.toByteArray();
//                        System.err.println("Acking "+message.getHeader().getSequence());
        DatagramPacket ackPacket = new DatagramPacket(bytes,0,bytes.length,ackAddr);
        socket.send(ackPacket);
    }

    /**
     * Nack a specific message sequence.
     * @param message the message to nack
     * @param nackSequence the sequence being nack'd
     * @throws IOException
     */
    private void nack(Message message, int nackSequence) throws IOException {
        if (verbose)
            System.err.println("Nacking "+nackSequence);
        Message response = Message.newBuilder()
                .setHeader(Header.newBuilder()
                        .setModeValue(Header.Mode.NACK_VALUE)
                        .setSequence(nackSequence)
                        .setResponse(true)
                        .build())
                .setSource(hostId).build();
        byte[] bytes = response.toByteArray();
//                        System.err.println("Nacking "+message.getHeader().getSequence());
        DatagramPacket ackPacket = new DatagramPacket(bytes,0,bytes.length,new InetSocketAddress(hostIdToInetAddress(message.getSource().getIp()),socketAddress.getPort()));
        socket.send(ackPacket);
    }

    /**
     * Mark a sequence as complete
     * @param sequenceId the sequence id to complete
     */
    private void complete(int sequenceId) {
        pendingAcks.remove(sequenceId);
        CompletableFuture<Void> f = pendingFutures.remove(sequenceId);
        if (f != null) f.complete(null);
        pendingTimes.remove(sequenceId);
        pendingMessages.remove(sequenceId);
    }

    private static boolean verbose = false;

    /**
     * Utility thread for retransmitting un-acknowledged packets (e.g. packet or ack may have been dropped)
     */
    private void retransmit() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<Integer,Long>> it = pendingTimes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer,Long> entry = it.next();
            long delay = now - entry.getValue();
            if (delay < 250) continue;
            if ((delay/1000) % 5 == 4) {
                verbose = true;
                String str = pendingAcks.get(entry.getKey()).stream()
                        .map((hostId) -> computeSocketAddress(hostId,socketAddress.getPort()))
                        .map(SocketAddress::toString)
                        .collect(Collectors.joining(",","[","]"));
                System.err.println("Still waiting on "+str+" to ack sequence "+entry.getKey());
            }
            if (now-entry.getValue() > 30000) {
                complete(entry.getKey()); // don't get stuck forever
                continue;
            }
            byte[] bytes = pendingMessages.get(entry.getKey());
            DatagramPacket packet = new DatagramPacket(bytes,0,bytes.length,socketAddress);
            try {
                retransmits.incrementAndGet();
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Utility method for computing a socket address from a host id and port
     * @param hostId host id
     * @param port port number
     * @return socket address with IP from host ID and port from argument
     */
    public static SocketAddress computeSocketAddress(HostId hostId, int port) {
        try {
            return new InetSocketAddress(hostIdToInetAddress(hostId.getIp()),port);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Compute the InetAddress for a given hostId's IP address
     * @param ip the IP encoded as a 32bit Integer
     * @return InetSocket address
     */
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
