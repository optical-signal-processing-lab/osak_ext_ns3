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
import java.util.Random;
import java.util.concurrent.TimeUnit;

import osak.ext.communication.MyLog;
import osak.ext.ns3.core.Pair;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.core.Timer;
import osak.ext.ns3.network.*;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * ArpL3Protocol
 * <p>
 * An implementation of the ARP protocol.
 * 
 * @author zhangrui
 * @since 1.0
 */
public class ArpL3Protocol {

    private List<ArpCache> m_cacheList = new ArrayList<>();
    private Node m_node;
    private Random m_requestJitter = new Random();
    private TrafficControlLayer m_tc;

    private ArpCache FindCache(NetDevice device) {
	for (ArpCache i : m_cacheList) {
	    if (i.GetDevice().equals(device)) {
		return i;
	    }
	}
	assert (false);
	return null;
    }

    private void SendArpRequest(final ArpCache cache, Ipv4Address to) {
	ArpHeader arp = new ArpHeader();
	// need to pick a source address; use routing implementation to select
	Ipv4L3Protocol ipv4 = new Ipv4L3Protocol();
	NetDevice device = cache.GetDevice();
	assert (device != null);
	Packet packet = new Packet();
	Ipv4Address source = ipv4.SelectSourceAddress(device, to, Ipv4InterfaceAddress.InterfaceAddressScope_e.GLOBAL);

	arp.SetRequest(device.GetAddress(), source, device.GetBroadcast(), to);
	assert (m_tc != null);
	m_tc.Send(device, new ArpQueueDiscItem(packet, device.GetBroadcast(), PROT_NUMBER, arp));
    }
    
    /**
     * @brief Send an ARP reply to an host
     * @param cache the ARP cache to use
     * @param myIp  the source IP address
     * @param toIp  the destination IP
     * @param toMac the destination MAC address
     */
    private void SendArpReply(final ArpCache cache, Ipv4Address myIp, Ipv4Address toIp, Address toMac) {
	ArpHeader arp = new ArpHeader();
	MyLog.logInfo("ARP: sending reply from node " + m_node + "|| src: " + cache.GetDevice().GetAddress()
		+ " / " + myIp + " || dst: " + toMac + " / " + toIp);
	arp.SetReply(cache.GetDevice().GetAddress(), myIp, toMac, toIp);
	Packet packet = new Packet();
	assert (m_tc != null);
	m_tc.Send(cache.GetDevice(), new ArpQueueDiscItem(packet, toMac, PROT_NUMBER, arp));
    }

    public static final short PROT_NUMBER = 0x0806;

    public ArpL3Protocol() {
	m_tc = null;
    }

    /**
     * @brief Set the node the ARP L3 protocol is associated with
     * @param node the node
     */
    public void SetNode(Node node) {
	m_node = node;
    }

    /**
     * @brief Set the TrafficControlLayer.
     * @param tc TrafficControlLayer object
     */
    public void SetTrafficControl(TrafficControlLayer tc) {
	m_tc = tc;
    }

    /**
     * @param p
     * @param hdr
     * @param dest
     * @param m_device
     * @param m_cache
     * @param hardwareDestination
     * @return
     */
    public boolean Lookup(Packet packet, Ipv4Header ipHeader, Ipv4Address destination, NetDevice device, ArpCache cache,
	    Address hardwareDestination) {
	ArpCacheEntry entry = cache.Lookup(destination);
	if (entry != null) {
	    if (entry.IsExpired()) {
		if (entry.IsDead()) {
		    MyLog.logInfo("dead entry for " + destination + " expired -- send arp request");
		    entry.MarkWaitReply(new Pair<Packet, Ipv4Header>(packet, ipHeader));
		    Timer.Schedules(new Time(m_requestJitter.nextInt(1000), TimeUnit.MICROSECONDS),
			    () -> this.SendArpRequest(cache, destination));
		}
		else if (entry.IsAlive()) {
		    MyLog.logInfo("alive entry for " + destination + " expired -- send arp request");
		    entry.MarkWaitReply(new Pair<Packet, Ipv4Header>(packet, ipHeader));
		    Timer.Schedules(new Time(m_requestJitter.nextInt(1000), TimeUnit.MICROSECONDS),
			    () -> this.SendArpRequest(cache, destination));
		} else {
		    MyLog.logOut(
			    "Test for possibly unreachable code-- please file a bug report, with a test case, if this is ever hit",
			    MyLog.ERROR);
		}
	    }
	    else {
		if (entry.IsDead()) {
		    MyLog.logInfo("dead entry for " + destination + " valid -- drop");
		    // add the Ipv4 header for tracing purposes
		    // packet.AddHeader(ipHeader);
		    // m_dropTrace(packet);
		}
		else if (entry.IsAlive()) {
		    MyLog.logInfo("alive entry for " + destination + " valid -- send");
		    hardwareDestination = entry.GetMacAddress();
		    return true;
		} else if (entry.IsWaitReply()) {
		    MyLog.logInfo("wait reply for " + destination + " valid -- drop previous");
		    if (!entry.UpdateWaitReply(new Pair<Packet, Ipv4Header>(packet, ipHeader))) {
			// add the Ipv4 header for tracing purposes
			// packet.AddHeader(ipHeader);
			// m_dropTrace(packet);
		    }
		}
		else if (entry.IsPermanent() || entry.IsAutoGenerated()) {
		    MyLog.logInfo("permanent for " + destination + " valid -- send");
		    hardwareDestination = entry.GetMacAddress();
		    return true;
		} else {
		    MyLog.logInfo(
			    "Test for possibly unreachable code-- please file a bug report, with a test case, if this is ever hit");
		}
	    }
	}
	else {
	    // This is our first attempt to transmit data to this destination.
	    MyLog.logInfo("no entry for " + destination + " -- send arp request");
	    entry = cache.Add(destination);
	    entry.MarkWaitReply(new Pair<Packet, Ipv4Header>(packet, ipHeader));
	    Timer.Schedules(new Time(m_requestJitter.nextInt(1000), TimeUnit.MICROSECONDS),
		    () -> this.SendArpRequest(cache, destination));
	}
	return false;
    }

    /**
     * @param m_device
     * @param ipv4Interface
     * @return
     */
    public ArpCache CreateCache(NetDevice device, Ipv4Interface ipv4Interface) {
	// Ipv4L3Protocol ipv4 = new Ipv4L3Protocol();
	ArpCache cache = new ArpCache();
	cache.SetDevice(device, ipv4Interface);
	assert (device.IsBroadcast());
	device.AddLinkChangeCallback(() -> cache.Flush());
	cache.SetArpRequestCallback((c, addr) -> this.SendArpRequest(c, addr));
	m_cacheList.add(cache);
	return cache;
    }

    /**
     * @brief Receive a packet
     * @param device the source NetDevice
     * @param p the packet
     * @param protocol the protocol
     * @param from the source address
     * @param to the destination address
     * @param packetType type of packet (i.e., unicast, multicast, etc.)
     */
    public void Receive(NetDevice device, final Packet p, short protocol, final Address from, final Address to,
	    PacketType packetType) {
	Packet packet = p.Copy();
	MyLog.logInfo("ARP: received packet of size " + packet.GetSize());
	ArpCache cache = FindCache(device);

	//
	// If we're connected to a real world network, then some of the fields sizes
	// in an ARP packet can vary in ways not seen in simulations. We need to be
	// able to detect ARP packets with headers we don't recongnize and not process
	// them instead of crashing. The ArpHeader will return 0 if it can't deal
	// with the received header.
	//
	ArpHeader arp = new ArpHeader();
	int size = packet.RemoveHeader(arp);
	if (size == 0) {
	    MyLog.logInfo("ARP: Cannot remove ARP header");
	    return;
	}
	MyLog.logInfo("ARP: received " + arp.GetType() + ", got " + arp.GetType() + " from "
		+ arp.GetSourceIpv4Address() + " for address " + arp.GetDestinationIpv4Address()
		+ "; we have addresses: ");
	for (int i = 0; i < cache.GetInterface().GetNAddresses(); i++) {
	    MyLog.logInfo("", cache.GetInterface().GetAddress(i).GetLocal() + ", ");
	}

	/**
	 * \internal 
	 * Note: we do not update the ARP cache when we receive an ARP request
	 * from an unknown node. See \bugid{107}
	 */
	boolean found = false;
	for (int i = 0; i < cache.GetInterface().GetNAddresses(); i++) {
	    if (arp.IsRequest()
		    && arp.GetDestinationIpv4Address().equals(cache.GetInterface().GetAddress(i).GetLocal())) {
		found = true;
		MyLog.logInfo("got request from " + arp.GetSourceIpv4Address() + " -- send reply");

		SendArpReply(cache, arp.GetDestinationIpv4Address(), arp.GetSourceIpv4Address(),
			arp.GetSourceHardwareAddress());
		break;
	    } else if (arp.IsReply()
		    && arp.GetDestinationIpv4Address().equals(cache.GetInterface().GetAddress(i).GetLocal())
		    && arp.GetDestinationHardwareAddress().equals(device.GetAddress())) {
		found = true;
		Ipv4Address fromL = arp.GetSourceIpv4Address();
		ArpCacheEntry entry = cache.Lookup(fromL);
		if (entry != null) {
		    if (entry.IsWaitReply()) {
			MyLog.logInfo("got reply from " + arp.GetSourceIpv4Address() + " for waiting entry -- flush");
			Address from_mac = arp.GetSourceHardwareAddress();
			entry.MarkAlive(from_mac);
			Pair<Packet, Ipv4Header> pending = entry.DequeuePending();
			while (pending.first() != null) {
			    cache.GetInterface().Send(pending.first(), pending.second(), arp.GetSourceIpv4Address());
			    pending = entry.DequeuePending();
			}
		    } else {
			// ignore this reply which might well be an attempt
			// at poisening my arp cache.
			MyLog.logInfo("got reply from "
                                + arp.GetSourceIpv4Address()
				+ " for non-waiting entry -- drop");
			// m_dropTrace(packet);
		    }
		}
		else {
		    MyLog.logInfo("got reply from unknown entry -- drop");
		    // m_dropTrace(packet);
		}
		break;
	    }
	}
	if (found == false) {
	    MyLog.logInfo("got request from " + arp.GetSourceIpv4Address() + " for unknown address "
		    + arp.GetDestinationIpv4Address() + " -- drop");
	}
    }

    /**
     * Assign a fixed random variable stream number to the random variables
     * used by this model.  Return the number of streams (possibly zero) that
     * have been assigned.
     *
     * @param stream first stream index to use
     * @return the number of stream indices assigned by this model
     */
    public long AssignStreams(long stream) {
	return 0;
    }

    protected void DoDispose() {
	m_cacheList.clear();
	m_node = null;
	m_tc = null;
    }
    /*
     * This function will notify other components connected to the node that a new stack member is
     * now connected This will be used to notify Layer 3 protocol of layer 4 protocol stack to
     * connect them together.
     */
    protected void NotifyNewAggregate() {
	// pass
    }


}
