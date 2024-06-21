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
package osak.ext.ns3.aodv;
import java.util.LinkedList;
import java.util.List;

import osak.ext.ns3.core.Time;
import osak.ext.ns3.core.Timer;
import osak.ext.ns3.internet.*;
import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO RoutingTableEntry
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class RoutingTableEntry {
    /// Valid Destination Sequence Number flag
    private boolean m_validSeqNo;
    /// Destination Sequence Number, if m_validSeqNo = true
    private int m_seqNo;
    /// Hop Count (number of hops needed to reach destination)
    private short m_hops;
    /**
     * Expiration or deletion time of the route
     * <p>
     * Lifetime field in the routing table plays dual role: for an active route it
     * is the expiration time, and for an invalid route it is the deletion time.
     */
    private Time m_lifeTime;
    /**
     * Ip route, include<p>
     *   - destination address<p>
     *   - source address<p>
     *   - next hop address (gateway)<p>
     *   - output device<p>
     */
    private Ipv4Route m_ipv4Route = null;
    /// Output interface address
    private Ipv4InterfaceAddress m_iface;
    /// Routing flags: valid, invalid or in search
    private RouteFlags m_flag = RouteFlags.VALID;

    /// List of precursors
    private List<Ipv4Address> m_precursorList = new LinkedList<>();
    /// When I can send another request
    private Time m_routeRequestTimout;
    /// Number of route requests
    private byte m_reqCount = 0;
    /// Indicate if this entry is in "blacklist"
    private boolean m_blackListState = false;
    /// Time for which the node is put into the blacklist
    private Time m_blackListTimeout = Time.Now();

    /// RREP_ACK timer
    public Timer m_ackTimer = new Timer();

    public void Copy(RoutingTableEntry o) {
	m_ackTimer = o.m_ackTimer;
	m_blackListState = o.m_blackListState;
	m_blackListTimeout = o.m_blackListTimeout;
	m_flag = o.m_flag;
	m_hops = o.m_hops;
	m_iface = o.m_iface;
	m_ipv4Route = o.m_ipv4Route;
	m_lifeTime = o.m_lifeTime;
	m_precursorList = o.m_precursorList;
	m_reqCount = o.m_reqCount;
	m_routeRequestTimout = o.m_routeRequestTimout;
	m_seqNo = o.m_seqNo;
	m_validSeqNo = o.m_validSeqNo;
    }

    /**
     * constructor
     *
     * @param dev      the device
     * @param dst      the destination IP address
     * @param vSeqNo   verify sequence number flag
     * @param seqNo    the sequence number
     * @param iface    the interface
     * @param hops     the number of hops
     * @param nextHop  the IP address of the next hop
     * @param lifetime the lifetime of the entry
     */
    public RoutingTableEntry(NetDevice dev, Ipv4Address dst, boolean vSeqNo, int seqNo, Ipv4InterfaceAddress iface,
	    short hops, Ipv4Address nextHop, Time lifetime) {
	m_validSeqNo = vSeqNo;
	m_seqNo = seqNo;
	m_hops = hops;
	m_lifeTime = Time.add(lifetime, Time.Now());
	m_iface = iface;

	m_ipv4Route = new Ipv4Route();
	m_ipv4Route.SetDestination(dst);
	m_ipv4Route.SetGateway(nextHop);
	m_ipv4Route.SetSource(m_iface.GetLocal());
	m_ipv4Route.SetOutputDevice(dev);
    }

    public RoutingTableEntry() {
	this.m_validSeqNo = false;
	this.m_seqNo = 0;
	this.m_iface = new Ipv4InterfaceAddress();
	this.m_lifeTime = Time.Now();

	m_ipv4Route = new Ipv4Route();
	m_ipv4Route.SetDestination(null);
	m_ipv4Route.SetGateway(null);
	m_ipv4Route.SetSource(m_iface.GetLocal());
	m_ipv4Route.SetOutputDevice(null);
    }

    /// \name Precursors management
    // \{
    /**
     * Insert precursor in precursor list if it doesn't yet exist in the list
     * 
     * @param id precursor address
     * @return true on success
     */
    public boolean InsertPrecursor(Ipv4Address id) {
	if (!LookupPrecursor(id)) {
	    m_precursorList.add(id);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Lookup precursor by address
     * 
     * @param id precursor address
     * @return true on success
     */
    public boolean LookupPrecursor(Ipv4Address id) {
	return m_precursorList.contains(id);
    }

    /**
     * \brief Delete precursor
     * 
     * @param id precursor address
     * @return true on success
     */
    public boolean DeletePrecursor(Ipv4Address id) {
	if (m_precursorList.contains(id)) {
	    m_precursorList.remove(id);
	    return true;
	}
	return false;
    }

    /// Delete all precursors
    public void DeleteAllPrecursors() {
	m_precursorList.clear();
    }

    /**
     * Check that precursor list is empty
     * @return true if precursor list is empty
     */
    public boolean IsPrecursorListEmpty() {
	return m_precursorList.isEmpty();
    }

    /**
     * Inserts precursors in output parameter prec if they do not yet exist in vector
     * @param prec vector of precursor addresses
     */
    public void GetPrecursors(List<Ipv4Address> prec) {
	if (IsPrecursorListEmpty()) {
	    return;
	}
	for (Ipv4Address i : m_precursorList) {
	    if (!prec.contains(i)) {
		prec.add(i);
	    }
	}
	// TODO: check for right
    }
    //\}

    /**
     * Mark entry as "down" (i.e. disable it)
     * 
     * @param badLinkLifetime duration to keep entry marked as invalid
     */
    // TODO:fix Time
    public void Invalidate(Time badLinkLifetime) {
	if (m_flag == RouteFlags.INVALID) {
	    return;
	}
	m_flag = RouteFlags.INVALID;
	m_reqCount = 0;
	m_lifeTime = Time.add(badLinkLifetime, Time.Now());
    }

    // Fields
    /**
     * Get destination address function
     * 
     * @returns the IPv4 destination address
     */
    public Ipv4Address GetDestination() {
	return m_ipv4Route.GetDestination();
    }

    /**
     * Get route function
     * 
     * @returns The IPv4 route
     */
    public Ipv4Route GetRoute() {
	return m_ipv4Route;
    }

    /**
     * Set route function
     * 
     * @param r the IPv4 route
     */
    public void SetRoute(Ipv4Route r) {
	m_ipv4Route = r;
    }

    /**
     * Set next hop address
     * @param nextHop the next hop IPv4 address
     */
    public void SetNextHop(Ipv4Address nextHop)
    {
	m_ipv4Route.SetGateway(nextHop);
    }

    /**
     * Get next hop address
     * 
     * @returns the next hop address
     */
    public Ipv4Address GetNextHop() {
	return m_ipv4Route.GetGateway();
    }

    /**
     * Set output device
     * @param dev The output device
     */
    public void SetOutputDevice(NetDevice dev)
    {
	m_ipv4Route.SetOutputDevice(dev);
    }

    /**
     * Get output device
     * 
     * @returns the output device
     */
    public NetDevice GetOutputDevice() {
	return m_ipv4Route.GetOutputDevice();
    }

    /**
     * Get the Ipv4InterfaceAddress
     * 
     * @returns the Ipv4InterfaceAddress
     */
    public Ipv4InterfaceAddress GetInterface() {
	return m_iface;
    }

    /**
     * Set the Ipv4InterfaceAddress
     * 
     * @param iface The Ipv4InterfaceAddress
     */
    public void SetInterface(Ipv4InterfaceAddress iface) {
	m_iface = iface;
    }

    /**
     * Set the valid sequence number
     * 
     * @param s the sequence number
     */
    public void SetValidSeqNo(boolean s) {
	m_validSeqNo = s;
    }

    /**
     * Get the valid sequence number
     * 
     * @returns the valid sequence number
     */
    public boolean GetValidSeqNo() {
	return m_validSeqNo;
    }

    /**
     * Set the sequence number
     * 
     * @param sn the sequence number
     */
    public void SetSeqNo(int sn) {
	m_seqNo = sn;
    }

    /**
     * Get the sequence number
     * 
     * @returns the sequence number
     */
    public int GetSeqNo() {
	return m_seqNo;
    }

    /**
     * Set the number of hops
     * 
     * @param hop the number of hops
     */
    public void SetHop(short hop) {
	m_hops = hop;
    }

    /**
     * Get the number of hops
     * 
     * @returns the number of hops
     */
    public short GetHop() {
	return m_hops;
    }

    /**
     * Set the lifetime
     * @param lt The lifetime
     */
    // TODO:fix Time
    public void SetLifeTime(Time lt)
    {
	m_lifeTime = Time.add(lt, Time.Now());
    }

    /**
     * Get the lifetime
     * @returns the lifetime
     */
    public Time GetLifeTime()
    {
	return Time.sub(m_lifeTime, Time.Now());
    }

    /**
     * Set the route flags
     * 
     * @param flag the route flags
     */
    public void SetFlag(RouteFlags flag) {
	m_flag = flag;
    }

    /**
     * Get the route flags
     * 
     * @returns the route flags
     */
    public RouteFlags GetFlag() {
	return m_flag;
    }

    /**
     * Set the RREQ count
     * 
     * @param n the RREQ count
     */
    public void SetRreqCnt(byte n) {
	m_reqCount = n;
    }

    /**
     * Get the RREQ count
     * 
     * @returns the RREQ count
     */
    public byte GetRreqCnt() {
	return m_reqCount;
    }

    /**
     * Increment the RREQ count
     */
    public void IncrementRreqCnt() {
	m_reqCount++;
    }

    /**
     * Set the unidirectional flag
     * 
     * @param u the uni directional flag
     */
    public void SetUnidirectional(boolean u) {
	m_blackListState = u;
    }

    /**
     * Get the unidirectional flag
     * 
     * @returns the unidirectional flag
     */
    public boolean IsUnidirectional() {
	return m_blackListState;
    }

    /**
     * Set the blacklist timeout
     * 
     * @param t the blacklist timeout value
     */
    public void SetBlacklistTimeout(Time t) {
	m_blackListTimeout = t;
    }

    /**
     * Get the blacklist timeout value
     * 
     * @returns the blacklist timeout value
     */
    public Time GetBlacklistTimeout() {
	return m_blackListTimeout;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Ipv4Address) {
	    Ipv4Address dst = (Ipv4Address) obj;
	    return m_ipv4Route.GetDestination().equals(dst);
	}
	return false;
    }
}
