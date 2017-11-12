package edu.ucf.student.jdavies.cnt5008.sim;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Router / L3-switch simulation class, unconcerned with things like routing or L1/2 issues.
 * Routing
 */
public class Switch {
    private Map<InetAddress,Host> inetToHostMap = new ConcurrentHashMap<>();
    /**
     * in reality this would be InetAddress to many InetAddress map, but Collection of Host makes coding easier
     */
    private Map<InetAddress,Collection<Host>> groupMembershipMap = new ConcurrentHashMap<>();

    public void attach(Host host) {
        inetToHostMap.putIfAbsent(host.getInetAddress(),host);
        host.setUplink(this);
    }

    public void deattach(Host host) {
        inetToHostMap.remove(host.getInetAddress());
        groupMembershipMap.values().stream().forEach((collection) -> collection.remove(host));
        host.setUplink(null);
    }

    void send(Host host, DatagramPacket packet) {
        if (packet.getAddress() == null) return;
        InetAddress addr = packet.getAddress();
        if (addr.isMulticastAddress() && groupMembershipMap.containsKey(addr)) {
            Collection<Host> hosts = groupMembershipMap.get(addr);
            hosts.parallelStream().filter((h)->h!=host).forEach((h) -> h.receive(packet));
        }
        else if (inetToHostMap.containsKey(packet.getAddress())){
            inetToHostMap.get(packet.getAddress()).receive(packet);
        }
    }

    void joinGroup(Host host, InetAddress group) {
        groupMembershipMap.computeIfAbsent(group,Switch::newHostCollection).add(host);
    }

    void leaveGroup(Host host, InetAddress group) {
        groupMembershipMap.computeIfAbsent(group,Switch::newHostCollection).remove(host);
    }

    private static final Collection<Host> newHostCollection(InetAddress addr) {
        return new CopyOnWriteArraySet<>();
    }
}
