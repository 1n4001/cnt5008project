package edu.ucf.student.jdavies.cnt5008;

import edu.ucf.student.jdavies.cnt5008.sim.Host;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Main {
    private static final Options OPTIONS = new Options();
    static {
        OPTIONS.addOption("help",false,"Print help");
        OPTIONS.addOption("group",true,"Multicast Group [230.18.13.1");
        OPTIONS.addOption("port",true,"Multicast Port [default: 1813]");
        OPTIONS.addOption("mode",true,"Reliable Multicast Mode [default: ACK] (ACK or NACK)");
        OPTIONS.addOption("loss",true,"Packet loss probability [default: 0.01]");
        OPTIONS.addOption("receivers",true,"Number of receivers to run with [default: 10]");
        OPTIONS.addOption("usecase",true,"Use case to test [default: OneToMany] (OneToMany or ManyToMany)");
    }

    public static void main(String[] args) throws Exception {
        CommandLine cmdLine = new PosixParser().parse(OPTIONS, args);
        String optGroup = "230.18.13.1"; // 'R' is 18th letter of alphabet and 'M' is 13th.
        String optPort = "1813";
        String optMode = "ACK";
        String optUsecase = "OneToMany";
        String optReceivers = "10";
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
        if (cmdLine.hasOption("receivers")) {
            optReceivers = cmdLine.getOptionValue("receivers");
        }
        if (cmdLine.hasOption("usecase")) {
            optUsecase = cmdLine.getOptionValue("usecase");
        }
        InetAddress group = InetAddress.getByName(optGroup);
        int port = Integer.parseInt(optPort);
        ReliableMode mode = Enum.valueOf(ReliableMode.class,optMode);
        InetSocketAddress socketAddress = new InetSocketAddress(group,port);

        Host host = new Host(InetAddress.getLocalHost());

        ReliableMulticastSocket socket = new ReliableMulticastSocket(host,socketAddress,mode);
    }
}
