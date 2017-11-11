package edu.ucf.student.jdavies.cnt5008;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.ucf.student.jdavies.cnt5008.proto.HostId;
import org.junit.Assert;
import org.junit.Test;

public class TestBeaconSocket {

    @Test
    public void testBeacon() throws IOException {
        HostId hostIdA = HostId.newBuilder().setIp(0x1234).setPort(0xFF01).build();
        HostId hostIdB = HostId.newBuilder().setIp(0x2345).setPort(0xFF02).build();
        InetAddress beaconAddress = InetAddress.getByName("238.18.13.1");
        int beaconPort = 7655;

        CountDownLatch discoverLatchA = new CountDownLatch(1);
        CountDownLatch goneLatchA = new CountDownLatch(1);
        CountDownLatch discoverLatchB = new CountDownLatch(1);
        CountDownLatch goneLatchB = new CountDownLatch(1);

        BeaconSocket beaconSocketA = new BeaconSocket(beaconAddress,beaconPort,hostIdA);
        BeaconSocket beaconSocketB = new BeaconSocket(beaconAddress,beaconPort,hostIdB);

        beaconSocketA.addListener(new BeaconSocket.Listener() {
            @Override
            public void hostJoined(HostId hostId) {
                if (hostId.equals(hostIdB))
                    discoverLatchB.countDown();
            }

            @Override
            public void hostParted(HostId hostId) {
                if (hostId.equals(hostIdB))
                    goneLatchB.countDown();
            }
        });
        beaconSocketB.addListener(new BeaconSocket.Listener() {
            @Override
            public void hostJoined(HostId hostId) {
                if (hostId.equals(hostIdA))
                    discoverLatchA.countDown();
            }

            @Override
            public void hostParted(HostId hostId) {
                if (hostId.equals(hostIdA))
                    goneLatchA.countDown();
            }
        });
        try {
            beaconSocketA.start();
            beaconSocketB.start();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        long start = System.currentTimeMillis();
        try {
            discoverLatchB.await(12500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Assert.fail("Did discover host from A in required time");
        }

        try {
            discoverLatchA.await(Math.max(100,12500 - (System.currentTimeMillis()-start)), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Assert.fail("Did discover host from B in required time");
        }

        Assert.assertEquals("Beacon A did not have the correct number of known hosts",1,beaconSocketA.getHosts().size());
        Assert.assertEquals("Beacon B did not have the correct number of known hosts",1,beaconSocketB.getHosts().size());

        beaconSocketA.stop();
        beaconSocketB.stop();
        try {
            goneLatchB.await(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Assert.fail("Did not hear GONE from BeaconSocket B in required time");
        }

        try {
            goneLatchA.await(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Assert.fail("Did not hear GONE from BeaconSocket A in required time");
        }

        Assert.assertEquals("Beacon A's hosts was not 0 after stop()",0,beaconSocketA.getHosts().size());
        Assert.assertEquals("Beacon B's hosts was not 0 after stop()",0,beaconSocketB.getHosts().size());

    }
}
