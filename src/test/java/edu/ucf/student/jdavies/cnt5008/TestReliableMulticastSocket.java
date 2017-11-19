package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        try {
            Future<Void> future = socketA.send(new byte[512]);
            if (!future.isDone()) {
                future.get(5, TimeUnit.SECONDS);
            }
            System.err.printf("Reliable send [ACK] took: %dms%n",(System.currentTimeMillis()-time));
        } catch (Throwable ie) {
            System.err.println("Reliable packet failed");
            ie.printStackTrace();
            Assert.fail("Reliable send failed");
        }
    }

    @Test
    public void testReliableSocketAckMultiReceiver() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("230.18.13.1"),32000);
        InetAddress hostAddressA = InetAddress.getByName("127.0.0.1");
        Switch router = new Switch();
        Host hostA = new Host(hostAddressA);
        router.attach(hostA);
        ReliableMulticastSocket socketA = new ReliableMulticastSocket(hostA,socketAddress, ReliableMode.ACK);

        List<Host> receiverHosts = new ArrayList<>(10);
        List<ReliableMulticastSocket> receiverSockets = new ArrayList<>(10);
        for (int i=2;i<12;i++) {
            InetAddress hostAddressB = InetAddress.getByName("127.0.0."+i);
            Host hostB = new Host(hostAddressB);
            router.attach(hostB);
            receiverHosts.add(hostB);
            ReliableMulticastSocket socketB = new ReliableMulticastSocket(hostB,socketAddress, ReliableMode.ACK);
            receiverSockets.add(socketB);
        }


        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(10,socketA.getRegistry().getHosts().size());
        for (ReliableMulticastSocket socketB : receiverSockets) {
            if (socketB.getRegistry().getHosts().isEmpty()) {
                System.err.println("Waiting for B to receive beacon from A");
                while (socketB.getRegistry().getHosts().isEmpty()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {}
                }
            }
        }
        long time = System.currentTimeMillis();
        try {
            Future<Void> future = socketA.send(new byte[512]);
            if (!future.isDone()) {
                future.get(5, TimeUnit.SECONDS);
            }
            System.err.printf("Reliable send [ACK] to 10 hosts took: %dms%n",(System.currentTimeMillis()-time));
        } catch (Throwable ie) {
            System.err.println("Reliable packet failed");
            ie.printStackTrace();
            Assert.fail("Reliable send failed");
        }
    }

    @Test
    public void testReliableSocketAckThroughput() throws IOException {
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
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        LinkedBlockingQueue<Future<Void>> futures = new LinkedBlockingQueue<>(4096);
        for (int i=0;i<4096;i++) {
            try {
                Thread.sleep(1);
                futures.add(socketA.send(new byte[512]));
            } catch (InterruptedException e) {
                break;
            }
        }
        futures.forEach((future) -> {
            try {
                if (!future.isDone()) {
                    future.get(5,TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        time = System.currentTimeMillis()-time;
        System.err.printf("ACK: Reliable send took: %dms%n",time);
        System.err.printf("ACK: Throughput is %1.2f reliable packets per second%n",4096.0/(time/1000.0));
    }


    @Test
    public void testReliableSocketAckThroughputLossy() throws IOException {
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

        socketA.getSocket().setLossRate(0.01f);
        socketB.getSocket().setLossRate(0.01f);

        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        LinkedBlockingQueue<Future<Void>> futures = new LinkedBlockingQueue<>(4096);
        for (int i=0;i<4096;i++) {
            try {
                Thread.sleep(1);
                futures.add(socketA.send(new byte[512]));
            } catch (InterruptedException e) {
                break;
            }
        }
        futures.forEach((future) -> {
            try {
                if (!future.isDone()) {
                    future.get(5,TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        int retransmitsFromA = socketA.getNumberOfRetransmits();
        int retransmitsFromB = socketB.getNumberOfRetransmits();
        time = System.currentTimeMillis()-time;
        System.err.printf("ACK: Reliable send took: %dms%n",time);
        System.err.printf("ACK: Throughput is %1.2f reliable packets per second%n",4096.0/(time/1000.0));
        System.err.printf("Retransmits: A=%d B=%d%n",retransmitsFromA,retransmitsFromB);
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
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        Future<Void> future = socketA.send(new byte[512]);
        try {
            if (!future.isDone()) {
                future.get(5, TimeUnit.SECONDS);
            }
            System.err.printf("Reliable send [NACK] took: %dms%n",(System.currentTimeMillis()-time));
        } catch (TimeoutException | ExecutionException | InterruptedException ie) {
            System.err.println("Reliable packet failed");
            Assert.fail("Reliable send failed");
        }
    }


    @Test
    public void testReliableSocketNackThroughput() throws IOException {
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
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        LinkedBlockingQueue<Future<Void>> futures = new LinkedBlockingQueue<>(4096);
        for (int i=0;i<4096;i++) {
            try {
                Thread.sleep(1);
                futures.add(socketA.send(new byte[512]));
            } catch (InterruptedException e) {
                break;
            }
        }
        futures.forEach((future) -> {
            try {
                if (!future.isDone()) {
                    future.get(5,TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        time = System.currentTimeMillis()-time;
        System.err.printf("NACK: Reliable send took: %dms%n",time);
        System.err.printf("NACK: Throughput is %1.2f reliable packets per second%n",4096.0/(time/1000.0));
    }


    @Test
    public void testReliableSocketNackThroughputLossy() throws IOException {
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

        socketA.getSocket().setLossRate(0.01f);
        socketB.getSocket().setLossRate(0.01f);

        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for A to receive beacon from B");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        if (socketB.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for B to receive beacon from A");
            while (socketB.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        Assert.assertEquals(1,socketA.getRegistry().getHosts().size());
        Assert.assertEquals(1,socketB.getRegistry().getHosts().size());
        long time = System.currentTimeMillis();
        LinkedBlockingQueue<Future<Void>> futures = new LinkedBlockingQueue<>(4096);
        for (int i=0;i<4096;i++) {
            try {
                Thread.sleep(1);
                futures.add(socketA.send(new byte[512]));
            } catch (InterruptedException e) {
                break;
            }
        }
        futures.forEach((future) -> {
            try {
                if (!future.isDone()) {
                    future.get(5,TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        int retransmitsFromA = socketA.getNumberOfRetransmits();
        int retransmitsFromB = socketB.getNumberOfRetransmits();
        time = System.currentTimeMillis()-time;
        System.err.printf("NACK: Reliable send took: %dms%n",time);
        System.err.printf("NACK: Throughput is %1.2f reliable packets per second%n",4096.0/(time/1000.0));
        System.err.printf("Retransmits: A=%d B=%d%n",retransmitsFromA,retransmitsFromB);
    }

}
