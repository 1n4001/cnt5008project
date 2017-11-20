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

    /**
     * Attach a host to this switch
     * @param host
     */
    public void attach(Host host) {
        inetToHostMap.putIfAbsent(host.getInetAddress(),host);
        host.setUplink(this);
    }

    /**
     * Detach a host from this switch
     * @param host
     */
    public void deattach(Host host) {
        inetToHostMap.remove(host.getInetAddress());
        groupMembershipMap.values().stream().forEach((collection) -> collection.remove(host));
        host.setUplink(null);
    }

    /**
     * Send packet
     * @param host sending host -- to avoid reflecting data back
     * @param packet packet to send
     */
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

    /**
     * Join a host to a multicast group
     * @param host joining host
     * @param group group being joined
     */
    void joinGroup(Host host, InetAddress group) {
        groupMembershipMap.computeIfAbsent(group,Switch::newHostCollection).add(host);
    }

    /**
     * Remove a host from a multicast group
     * @param host leaving host
     * @param group group being left
     */
    void leaveGroup(Host host, InetAddress group) {
        groupMembershipMap.computeIfAbsent(group,Switch::newHostCollection).remove(host);
    }

    /**
     * Helper method for computeIfAbsent
     * @param addr unused
     * @return return concurrent collection
     */
    private static final Collection<Host> newHostCollection(InetAddress addr) {
        return new CopyOnWriteArraySet<>();
    }
}
