package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.ucf.student.jdavies.cnt5008.proto.Header;
import edu.ucf.student.jdavies.cnt5008.sim.Host;
import edu.ucf.student.jdavies.cnt5008.sim.Switch;
import org.junit.Assert;
import org.junit.Test;

public class TestReliableMulticastSocket {
    @Test
    public void testReliableSocketAck() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("230.18.13.1"),32000);
        InetAddress hostAddressA = InetAddress.getByName("127.0.0.1");
        InetAddress hostAddressB = InetAddress.getByName("127.0.0.2");
        Switch router = new Switch();
        Host hostA = new Host(hostAddressA);
        Host hostB = new Host(hostAddressB);
        router.attach(hostA);
        router.attach(hostB);

        ReliableMulticastSocket socketA = new ReliableMulticastSocket(hostA,socketAddress, ReliableMode.ACK);
        ReliableMulticastSocket socketB = new ReliableMulticastSocket(hostB,socketAddress, ReliableMode.ACK);

        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Socket A not ready yet");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {}
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Socket B not ready yet");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {}
        }
        System.err.println("Host A's known hosts: "+socketA.getRegistry().getHosts());
        System.err.println("Host B's known hosts: "+socketB.getRegistry().getHosts());
        System.err.println("Sending reliable packet from A to group");
        long time = System.currentTimeMillis();
        Future<Void> future = socketA.send(new byte[512]);
        try {
            future.get(5, TimeUnit.SECONDS);
            System.err.printf("Reliable send took: %dms%n",(System.currentTimeMillis()-time));
        } catch (TimeoutException | ExecutionException | InterruptedException ie) {
            System.err.println("Reliable packet failed");
            Assert.fail("Reliable send failed");
        }
    }
    @Test
    public void testReliableSocketNack() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("230.18.13.1"),32000);
        InetAddress hostAddressA = InetAddress.getByName("127.0.0.1");
        InetAddress hostAddressB = InetAddress.getByName("127.0.0.2");
        Switch router = new Switch();
        Host hostA = new Host(hostAddressA);
        Host hostB = new Host(hostAddressB);
        router.attach(hostA);
        router.attach(hostB);

        ReliableMulticastSocket socketA = new ReliableMulticastSocket(hostA,socketAddress, ReliableMode.NACK);
        ReliableMulticastSocket socketB = new ReliableMulticastSocket(hostB,socketAddress, ReliableMode.NACK);

        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Socket A not ready yet");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {}
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Socket B not ready yet");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {}
        }
        System.err.println("Host A's known hosts: "+socketA.getRegistry().getHosts());
        System.err.println("Host B's known hosts: "+socketB.getRegistry().getHosts());
        System.err.println("Sending reliable packet from A to group");
        long time = System.currentTimeMillis();
        Future<Void> future = socketA.send(new byte[512]);
        try {
            future.get(5, TimeUnit.SECONDS);
            System.err.printf("Reliable send took: %dms%n",(System.currentTimeMillis()-time));
        } catch (TimeoutException | ExecutionException | InterruptedException ie) {
            System.err.println("Reliable packet failed");
            Assert.fail("Reliable send failed");
        }
    }
}
