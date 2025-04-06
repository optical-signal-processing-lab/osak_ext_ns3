/*
 * Copyright 2024 OSPLAB (Optical Signal Processing Lab Of UESTC)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package osak.ext.ns3.internet;

import java.util.ArrayList;
import java.util.List;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.Callback2;
import osak.ext.ns3.network.*;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4Interface
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class Ipv4Interface {
    public Ipv4Interface() {
	m_ifup = false;
	m_forwarding = true;
	m_metric = 1;
	m_node = null;
	m_device = null;
	m_tc = null;
	m_cache = null;
    }

    /**
     * Set node associated with interface.
     * 
     * @param node node
     */
    public void SetNode(Node node) {
	m_node = node;
	DoSetup();
    }

    /**
     * Set the NetDevice.
     * 
     * @param device NetDevice
     */
    public void SetDevice(NetDevice device) {
	m_device = device;
	DoSetup();
    }

    /**
     * Set the TrafficControlLayer.
     * 
     * @param tc TrafficControlLayer object
     */
    public void SetTrafficControl(TrafficControlLayer tc) {
	m_tc = tc;
    }

    /**
     * Set ARP cache used by this interface
     * 
     * @param arpCache the ARP cache
     */
    public void SetArpCache(ArpCache arpCache) {
	m_cache = arpCache;
    }

    /**
     * @returns the underlying NetDevice. This method cannot return zero.
     */
    public NetDevice GetDevice() {
	return m_device;
    }

    /**
     * @return ARP cache used by this interface
     */
    public ArpCache GetArpCache() {
	return m_cache;
    }

    /**
     * @param metric configured routing metric (cost) of this interface
     *
     *               Note: This is synonymous to the Metric value that ifconfig
     *               prints out. It is used by ns-3 global routing, but other
     *               routing daemons choose to ignore it.
     */
    public void SetMetric(short metric) {
	m_metric = metric;
    }

    /**
     * @returns configured routing metric (cost) of this interface
     *
     *          Note: This is synonymous to the Metric value that ifconfig prints
     *          out. It is used by ns-3 global routing, but other routing daemons
     *          may choose to ignore it.
     */
    public short GetMetric() {
	return m_metric;
    }

    /**
     * These are IP interface states and may be distinct from NetDevice states, such
     * as found in real implementations (where the device may be down but IP
     * interface state is still up).
     */
    /**
     * @returns true if this interface is enabled, false otherwise.
     */
    public boolean IsUp() {
	return m_ifup;
    }

    /**
     * @returns true if this interface is disabled, false otherwise.
     */
    public boolean IsDown() {
	return !m_ifup;
    }

    /**
     * Enable this interface
     */
    public void SetUp() {
	m_ifup = true;
    }

    /**
     * Disable this interface
     */
    public void SetDown() {
	m_ifup = false;
    }

    /**
     * @returns true if this interface is enabled for IP forwarding of input
     *          datagrams
     */
    public boolean IsForwarding() {
	return m_forwarding;
    }

    /**
     * @param val Whether to enable or disable IP forwarding for input datagrams
     */
    public void SetForwarding(boolean val) {
	m_forwarding = val;
    }

    /**
     * @param p    packet to send
     * @param hdr  IPv4 header
     * @param dest next hop address of packet.
     *
     *             This method will eventually call the private SendTo method which
     *             must be implemented by subclasses.
     */
    @SuppressWarnings("unused")
    public void Send(Packet p, final Ipv4Header hdr, Ipv4Address dest) {
	if (!IsUp()) {
	    return;
	}
	// Check for a loopback device, if it's the case we don't pass through
	// traffic control layer
	if (m_device instanceof LoopbackNetDevice) {
	    /// TODO: additional checks needed here (such as whether multicast
	    /// goes to loopback)?
	    p.AddHeader(hdr);
	    m_device.Send(p, m_device.GetBroadcast(), Ipv4L3Protocol.PROT_NUMBER);
	    return;
	}
	assert (m_tc != null);
	// is this packet aimed at a local interface ?
	for (Ipv4InterfaceAddress i : m_ifaddrs) {
	    if (dest.equals(i.GetLocal())) {
		p.AddHeader(hdr);
		m_tc.Receive(m_device, p, Ipv4L3Protocol.PROT_NUMBER, m_device.GetBroadcast(), m_device.GetBroadcast(),
			PacketType.PACKET_HOST);
		return;
	    }
	}
	if(m_device.NeedsArp()) {
	    // TODO:
	    // ArpL3Protocol arp = m_node.GetObject<ArpL3Protocol>();
	    ArpL3Protocol arp = new ArpL3Protocol();
	    Address hardwareDestination = new Address();// TODO: need to check
	    boolean found = false;
	    // TODO: Error -> 要判断是否是广播地址
	    if (dest.IsBroadcast()) {
		// TODO:
		// hardwareDestination = m_device.GetBroadcast();
		hardwareDestination = m_device.GetBroadcast();
		found = true;
	    }
	    else if (dest.IsMulticast()) {
		hardwareDestination = m_device.GetMulticast(dest);
		found = true;
	    } else {
		for (Ipv4InterfaceAddress i : m_ifaddrs) {
		    if (/* dest.IsSubnetDirectedBroadcast((*i).GetMask() */false) {
			hardwareDestination = m_device.GetBroadcast();
			found = true;
			break;
		    }
		}
		if (!found) {
		    found = arp.Lookup(p, hdr, dest, m_device, m_cache, hardwareDestination);
		}
	    }

	    if (found) {
		m_tc.Send(m_device, new Ipv4QueueDiscItem(p, hardwareDestination, Ipv4L3Protocol.PROT_NUMBER, hdr));
	    }
	}
	else {
	    m_tc.Send(m_device, new Ipv4QueueDiscItem(p, m_device.GetBroadcast(), Ipv4L3Protocol.PROT_NUMBER, hdr));
	}
    }

    /**
     * @param address The Ipv4InterfaceAddress to add to the interface
     * @returns true if succeeded
     */
    public boolean AddAddress(Ipv4InterfaceAddress address) {
	m_ifaddrs.add(address);
	if (m_addAddressCallback != null) {
	    m_addAddressCallback.callback(this, address);
	}
	return true;
    }

    /**
     * @param index Index of Ipv4InterfaceAddress to return
     * @returns The Ipv4InterfaceAddress address whose index is i
     */
    public Ipv4InterfaceAddress GetAddress(int index) {
	return m_ifaddrs.get(index);
    }

    /**
     * @returns the number of Ipv4InterfaceAddress stored on this interface
     */
    public int GetNAddresses() {
	return m_ifaddrs.size();
    }

    /**
     * @param index Index of Ipv4InterfaceAddress to remove
     * @returns The Ipv4InterfaceAddress address whose index is index
     */
    public Ipv4InterfaceAddress RemoveAddress(int index) {
	if (index < m_ifaddrs.size()) {
	    Ipv4InterfaceAddress addr = m_ifaddrs.get(index);
	    m_ifaddrs.remove(index);
	    if (m_removeAddressCallback != null) {
		m_removeAddressCallback.callback(this, addr);
	    }
	    return addr;
	}
	return new Ipv4InterfaceAddress();
    }

    /**
     * Remove the given Ipv4 address from the interface.
     * 
     * @param address The Ipv4 address to remove
     * @returns The removed Ipv4 interface address
     * @returns The null interface address if the interface did not contain the
     *          address or if loopback address was passed as argument
     */
    public Ipv4InterfaceAddress RemoveAddress(Ipv4Address address) {
	if (address.equals(Ipv4Address.GetLoopback())) {
	    MyLog.logOut(this.getClass().getName() + "::RemoveAddress", "Cannot remove loopback address.", 3);
	    return new Ipv4InterfaceAddress();
	}
	for (Ipv4InterfaceAddress i : m_ifaddrs) {
	    if (i.GetLocal().equals(address)) {
		Ipv4InterfaceAddress ifAddr = i;
		m_ifaddrs.remove(i);
		if (m_removeAddressCallback != null) {
		    m_removeAddressCallback.callback(this, ifAddr);
		}
		return ifAddr;
	    }
	}
	return new Ipv4InterfaceAddress();
    }

    /**
     * This callback is set when an address is removed from an interface with
     * auto-generated Arp cache and it allow the neighbor cache helper to update
     * neighbor's Arp cache
     *
     * @param removeAddressCallback Callback when remove an address.
     */
    public void RemoveAddressCallback(Callback2<Ipv4Interface, Ipv4InterfaceAddress> removeAddressCallback) {
	m_removeAddressCallback = removeAddressCallback;
    }

    /**
     * This callback is set when an address is added from an interface with
     * auto-generated Arp cache and it allow the neighbor cache helper to update
     * neighbor's Arp cache
     *
     * @param addAddressCallback Callback when remove an address.
     */
    public void AddAddressCallback(Callback2<Ipv4Interface, Ipv4InterfaceAddress> addAddressCallback) {
	m_addAddressCallback = addAddressCallback;
    }
    
    protected void DoDispose() {
	m_node = null;
	m_device = null;
	m_tc = null;
	m_cache = null;
	// Object.DoDispose();
    }
    
    /**
     * \brief Initialize interface.
     */
    private void DoSetup() {
	// TODO:need to check
	if (m_node != null || m_device != null) {
	    return;
	}
	if (!m_device.NeedsArp()) {
	    return;
	}
	// ArpL3Protocol arp = m_node->GetObject<ArpL3Protocol>();
	ArpL3Protocol arp = new ArpL3Protocol();
	m_cache = arp.CreateCache(m_device, this);
    }

    /**
     * \brief Container for the Ipv4InterfaceAddresses.
     */
    //typedef std::list<Ipv4InterfaceAddress> Ipv4InterfaceAddressList;

    /**
     * \brief Container Iterator for the Ipv4InterfaceAddresses.
     */
    //typedef std::list<Ipv4InterfaceAddress>::const_iterator Ipv4InterfaceAddressListCI;

    /**
     * \brief Const Container Iterator for the Ipv4InterfaceAddresses.
     */
    //typedef std::list<Ipv4InterfaceAddress>::iterator Ipv4InterfaceAddressListI;

    private boolean m_ifup;                        //!< The state of this interface
    private boolean m_forwarding;                  //!< Forwarding state.
    private short m_metric;                  //!< Interface metric
    /**
     * \brief Container for the Ipv4InterfaceAddresses.
     */
    private List<Ipv4InterfaceAddress> m_ifaddrs = new ArrayList<>(); //!< Address list
    private Node m_node;                   //!< The associated node
    private NetDevice m_device;            //!< The associated NetDevice
    private TrafficControlLayer m_tc;      //!< The associated TrafficControlLayer
    private ArpCache m_cache;              //!< ARP cache
    private Callback2<Ipv4Interface, Ipv4InterfaceAddress>
        m_removeAddressCallback; //!< remove address callback
    private Callback2<Ipv4Interface, Ipv4InterfaceAddress>
        m_addAddressCallback; //!< add address callback

}
