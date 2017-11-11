package edu.ucf.student.jdavies.cnt5008;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Main {
    private static final Options OPTIONS = new Options();
    static {
        OPTIONS.addOption("group",true,"Multicast Group [230.18.13.1");
        OPTIONS.addOption("port",true,"Multicast Port [default: 1813]");
        OPTIONS.addOption("mode",true,"Reliable Multicast Mode [default: ACK] (ACK or NACK)");
    }

    public static void main(String[] args) throws Exception {
        CommandLine cmdLine = new PosixParser().parse(OPTIONS, args);
        String optGroup = "230.18.13.1"; // 'R' is 18th letter of alphabet and 'M' is 13th.
        String optPort = "1813";
        String optMode = "ACK";
        if (cmdLine.hasOption("group")) {
            optGroup = cmdLine.getOptionValue("group");
        }
        if (cmdLine.hasOption("port")) {
            optPort = cmdLine.getOptionValue("port");
        }
        if (cmdLine.hasOption("port")) {
            optMode = cmdLine.getOptionValue("port");
        }
        InetAddress group = InetAddress.getByName(optGroup);
        int port = Integer.parseInt(optPort);
        ReliableMode mode = Enum.valueOf(ReliableMode.class,optMode);
        InetSocketAddress socketAddress = new InetSocketAddress(group,port);

        ReliableMulticastSocket socket = new ReliableMulticastSocket(socketAddress,mode);
    }
}
