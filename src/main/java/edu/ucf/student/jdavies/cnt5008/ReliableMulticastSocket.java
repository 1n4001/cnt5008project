package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.ucf.student.jdavies.cnt5008.proto.Message;

public class ReliableMulticastSocket {
    private MulticastSocket socket = null;
    private ReliableMode mode;
    private Map<Long,Integer> hostToSequence = new ConcurrentHashMap<>();

    public ReliableMulticastSocket(InetSocketAddress socketAddress, ReliableMode mode) throws IOException {
        socket = new MulticastSocket(socketAddress.getPort());
        socket.setReuseAddress(true);
        socket.joinGroup(socketAddress.getAddress());
        this.mode = mode;
    }

    public Message receiveMessage() {
        return null;
    }

}
