package edu.ucf.student.jdavies.cnt5008;

import edu.ucf.student.jdavies.cnt5008.sim.Host;
import edu.ucf.student.jdavies.cnt5008.sim.Switch;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Options OPTIONS = new Options();
    static {
        OPTIONS.addOption("help",false,"Print help");
        OPTIONS.addOption("group",true,"Multicast Group [230.18.13.1");
        OPTIONS.addOption("port",true,"Multicast Port [default: 1813]");
        OPTIONS.addOption("mode",true,"Reliable Multicast Mode [default: ACK] (ACK or NACK)");
        OPTIONS.addOption("loss",true,"Packet loss probability [default: 0.00]");
        OPTIONS.addOption("receivers",true,"Number of receivers to run with [default: 10]");
        OPTIONS.addOption("usecase",true,"Use case to test [default: OneToMany] (OneToMany or ManyToMany)");
    }

    public enum UseCase {
        OneToMany,
        ManyToMany
    }

    /**
     * Main entry point for running reliable multicast simulation.
     *
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        CommandLine cmdLine = new PosixParser().parse(OPTIONS, args);
        String optGroup = "230.18.13.1"; // 'R' is 18th letter of alphabet and 'M' is 13th.
        String optPort = "1813";
        String optMode = "ACK";
        String optUsecase = "OneToMany";
        String optReceivers = "10";
        String optLoss = "0.00";
        String optCount = "1024";
        if (cmdLine.hasOption("help")) {
            System.err.println("Command line options:");
            for (Option option : cmdLine.getOptions()) {
                System.err.println("\t-"+option.getArgName()+"\t"+option.getDescription());
            }
            return;
        }
        if (cmdLine.hasOption("group")) {
            optGroup = cmdLine.getOptionValue("group");
        }
        if (cmdLine.hasOption("port")) {
            optPort = cmdLine.getOptionValue("port");
        }
        if (cmdLine.hasOption("mode")) {
            optMode = cmdLine.getOptionValue("mode");
        }
        if (cmdLine.hasOption("loss")) {
            optLoss = cmdLine.getOptionValue("loss");
        }
        if (cmdLine.hasOption("count")) {
            optCount = cmdLine.getOptionValue("count");
        }
        if (cmdLine.hasOption("receivers")) {
            optReceivers = cmdLine.getOptionValue("receivers");
        }
        if (cmdLine.hasOption("usecase")) {
            optUsecase = cmdLine.getOptionValue("usecase");
        }
        InetAddress group = InetAddress.getByName(optGroup);
        int port = Integer.parseInt(optPort);
        ReliableMode mode = Enum.valueOf(ReliableMode.class,optMode);
        int numReceivers = Integer.parseInt(optReceivers);
        numReceivers = Math.max(1,Math.min(numReceivers,253));
        int messageCount = Integer.parseInt(optCount);
        float loss = Float.parseFloat(optLoss);
        UseCase useCase = Enum.valueOf(UseCase.class,optUsecase);

        if (useCase == UseCase.OneToMany) {
            oneToMany(group,port,mode,loss,numReceivers,messageCount);
        }
        else if (useCase == UseCase.ManyToMany) {
            manyToMany(group,port,mode,loss,numReceivers,messageCount);
        }
    }

    /**
     * Simulate a one to many scenario.  This could be an example of an audio or video broadcast to many listeners.
     *
     * @param group multicast group to join
     * @param port port for primary traffic (port+1 used for beacon)
     * @param mode the mode to be used -- Nack or Ack
     * @param loss the probability of any single packet getting dropped
     * @param numReceivers the number of receivers (i.e. the 'many')
     * @param messageCount the number of messages to send out
     * @throws IOException
     */
    public static void oneToMany(InetAddress group, int port, ReliableMode mode, float loss, int numReceivers, int messageCount) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(group,port);
        InetAddress hostAddressA = InetAddress.getByName("127.0.0.1");
        Switch router = new Switch();
        Host hostA = new Host(hostAddressA);

        router.attach(hostA);
        ReliableMulticastSocket socketA = new ReliableMulticastSocket(hostA,socketAddress, mode);
        socketA.getSocket().setLossRate(loss);

        List<Host> receiverHosts = new ArrayList<>(10);
        List<ReliableMulticastSocket> receiverSockets = new ArrayList<>(10);
        for (int i=2;i<numReceivers+2;i++) {
            InetAddress hostAddressB = InetAddress.getByName("127.0.0."+i);
            Host hostB = new Host(hostAddressB);
            router.attach(hostB);
            receiverHosts.add(hostB);
            ReliableMulticastSocket socketB = new ReliableMulticastSocket(hostB,socketAddress, mode);
            socketB.getSocket().setLossRate(loss);
            receiverSockets.add(socketB);
        }


        if (socketA.getRegistry().getHosts().isEmpty()) {
            System.err.println("Waiting for source to receive beacon from receivers");
            while (socketA.getRegistry().getHosts().isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        }
        for (ReliableMulticastSocket socketB : receiverSockets) {
            if (socketB.getRegistry().getHosts().isEmpty()) {
                System.err.println("Waiting for receiver "+socketB.getSocket().getInetAddress()+"'s beacon...");
                while (socketB.getRegistry().getHosts().isEmpty()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {}
                }
            }
        }
        long time = System.currentTimeMillis();
        LinkedBlockingQueue<Future<Void>> futures = new LinkedBlockingQueue<>(4096);
        for (int i=0;i<messageCount;i++) {
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
        System.err.printf("%s: Reliable send took: %dms%n",mode.toString(),time);
        System.err.printf("%s: Throughput is %1.2f reliable packets per second%n",mode.toString(),messageCount/(time/1000.0));
        System.err.printf("%s: Retransmits from source: %d%n",mode.toString(),socketA.getNumberOfRetransmits());

    }

    /**
     * Simulate a many-to-many scenario.
     * @param group
     * @param port
     * @param mode
     * @param loss
     * @param numReceivers
     * @param messageCount
     */
    public static void manyToMany(InetAddress group, int port, ReliableMode mode, float loss, int numReceivers, int messageCount) {

    }
}
