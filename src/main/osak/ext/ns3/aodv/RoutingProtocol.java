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

import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.ErrorCallback;
import osak.ext.ns3.callback.LocalDeliverCallback;
import osak.ext.ns3.callback.MulticastForwardCallback;
import osak.ext.ns3.callback.UnicastForwardCallback;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.core.Timer;
import osak.ext.ns3.internet.*;
import osak.ext.ns3.network.*;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO AodvRoutingProtocol
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class RoutingProtocol implements Ipv4RoutingProtocol {
    /// UDP Port for AODV control traffic
    public static final int AODV_PORT = 654;

    // Protocol parameters.
    private int m_rreqRetries; /// < Maximum number of retransmissions of RREQ with TTL = NetDiameter to
		       /// < discover a route
    private short m_ttlStart; /// < Initial TTL value for RREQ.
    private short m_ttlIncrement; /// < TTL increment for each attempt using the expanding ring search for
			  /// < RREQ dissemination.
    private short m_ttlThreshold; /// < Maximum TTL value for expanding ring search, TTL = NetDiameter is
			  /// < used beyond this value.
    private short m_timeoutBuffer; /// < Provide a buffer for the timeout.
    private short m_rreqRateLimit; /// < Maximum number of RREQ per second.
    private short m_rerrRateLimit; /// < Maximum number of REER per second.
    private Time m_activeRouteTimeout; /// < Period of time during which the route is considered to be valid.
    private int m_netDiameter; /// < Net diameter measures the maximum possible number of hops between
		       /// < two nodes in the network

    /**
     * NodeTraversalTime is a conservative estimate of the average one hop traversal
     * time for packets and should include queuing delays, interrupt processing
     * times and transfer times.
     */
    private Time m_nodeTraversalTime;
    private Time m_netTraversalTime; /// < Estimate of the average net traversal time.
    private Time m_pathDiscoveryTime; /// < Estimate of maximum time needed to find route in network.
    private Time m_myRouteTimeout; /// < Value of lifetime field in RREP generating by this node.
    /**
     * Every HelloInterval the node checks whether it has sent a broadcast within
     * the last HelloInterval. If it has not, it MAY broadcast a Hello message
     */
    private Time m_helloInterval;
    private int m_allowedHelloLoss; /// < Number of hello messages which may be loss for valid link
    /**
     * DeletePeriod is intended to provide an upper bound on the time for which an
     * upstream node A can have a neighbor B as an active next hop for destination
     * D, while B has invalidated the route to D.
     */
    private Time m_deletePeriod;
    private Time m_nextHopWait; /// < Period of our waiting for the neighbour's RREP_ACK
    private Time m_blackListTimeout; /// < Time for which the node is put into the blacklist
    private int m_maxQueueLen; /// < The maximum number of packets that we allow a routing protocol to
		       /// < buffer.
    private Time m_maxQueueTime; /// < The maximum period of time that a routing protocol is allowed to
			 /// < buffer a packet for.
    private boolean m_destinationOnly; /// < Indicates only the destination may respond to this RREQ.
    private boolean m_gratuitousReply; /// < Indicates whether a gratuitous RREP should be unicast to the node
			       /// < originated route discovery.
    private boolean m_enableHello; /// < Indicates whether a hello messages enable
    private boolean m_enableBroadcast; /// < Indicates whether a a broadcast data packets forwarding enable

    /// IP protocol
    private Ipv4 m_ipv4;
    /// Raw unicast socket per each IP interface, map socket -> iface address (IP +
    /// mask)
    private Map<Socket, Ipv4InterfaceAddress> m_socketAddresses = new HashMap<>();
    /// Raw subnet directed broadcast socket per each IP interface, map socket ->
    /// iface address (IP
    /// + mask)
    private Map<Socket, Ipv4InterfaceAddress> m_socketSubnetBroadcastAddresses = new HashMap<>();
    /// Loopback device used to defer RREQ until packet will be fully formed
    private NetDevice m_lo;

    /// Routing table
    private RoutingTable m_routingTable;
    /// A "drop-front" queue used by the routing layer to buffer packets to which it
    /// does not have a
    /// route.
    private RequestQueue m_queue;
    /// Broadcast ID
    private int m_requestId;
    /// Request sequence number
    private int m_seqNo;
    /// Handle duplicated RREQ
    private IdCache m_rreqIdCache;
    /// Handle duplicated broadcast/multicast packets
    private DuplicatePacketDetection m_dpd;
    /// Handle neighbors
    private Neighbors m_nb;
    /// Number of RREQs used for RREQ rate control
    private short m_rreqCount;
    /// Number of RERRs used for RERR rate control
    private short m_rerrCount;

    /// Start protocol operation
    private void Start() {
	if (m_enableHello) {
	    m_nb.ScheduleTimer();
	}
	m_rreqRateLimitTimer.SetFunction(() -> RreqRateLimitTimerExpire());
	m_rreqRateLimitTimer.Schedule(new Time(1, TimeUnit.SECONDS));

	m_rerrRateLimitTimer.SetFunction(() -> RreqRateLimitTimerExpire());
	m_rerrRateLimitTimer.Schedule(new Time(1, TimeUnit.SECONDS));
    }

    /**
     * Queue packet and send route request
     *
     * @param p      the packet to route
     * @param header the IP header
     * @param ucb    the UnicastForwardCallback function
     * @param ecb    the ErrorCallback function
     */
    void DeferredRouteOutput(Packet p, Ipv4Header header, UnicastForwardCallback ucb, ErrorCallback ecb) {
	QueueEntry newEntry = new QueueEntry(p, header, ucb, ecb, new Time(0));
	boolean result = m_queue.Enqueue(newEntry);
	if (result) {
	    MyLog.logInfo("DeferredRouteOutput",
		    "Add packet " + p.GetUid() + " to queue. Protocol " + header.GetProtocol());
	    RoutingTableEntry rt = new RoutingTableEntry();
	    boolean res = m_routingTable.LookupRoute(header.GetDestination(), rt);
	    if (!res || ((rt.GetFlag() != RouteFlags.IN_SEARCH) && res)) {
		MyLog.logInfo("DeferredRouteOutput", "Send new RREQ for outbound packet to " + header.GetDestination());
		SendRequest(header.GetDestination());
	    }
	}
    }

    /**
     * If route exists and is valid, forward packet.
     *
     * @param p      the packet to route
     * @param header the IP header
     * @param ucb    the UnicastForwardCallback function
     * @param ecb    the ErrorCallback function
     * @returns true if forwarded
     */
    boolean Forwarding(Packet p, Ipv4Header header, UnicastForwardCallback ucb, ErrorCallback ecb) {
	Ipv4Address dst = header.GetDestination();
	Ipv4Address origin = header.GetSource();
	m_routingTable.Purge();
	RoutingTableEntry toDst = new RoutingTableEntry();
	if (m_routingTable.LookupRoute(dst, toDst)) {
	    if (toDst.GetFlag() == RouteFlags.VALID) {
		Ipv4Route route = toDst.GetRoute();
		MyLog.logInfo("Forwarding",
			route.GetSource() + " forwarding to " + dst + " from " + origin + " packet " + p.GetUid());
		/*
		 * Each time a route is used to forward a data packet, its Active Route Lifetime
		 * field of the source, destination and the next hop on the path to the
		 * destination is updated to be no less than the current time plus
		 * ActiveRouteTimeout.
		 */
		UpdateRouteLifeTime(origin, m_activeRouteTimeout);
		UpdateRouteLifeTime(dst, m_activeRouteTimeout);
		UpdateRouteLifeTime(route.GetGateway(), m_activeRouteTimeout);
		/*
		 * Since the route between each originator and destination pair is expected to
		 * be symmetric, the Active Route Lifetime for the previous hop, along the
		 * reverse path back to the IP source, is also updated to be no less than the
		 * current time plus ActiveRouteTimeout
		 */
		RoutingTableEntry toOrigin = new RoutingTableEntry();
		m_routingTable.LookupRoute(origin, toOrigin);
		UpdateRouteLifeTime(toOrigin.GetNextHop(), m_activeRouteTimeout);
		// TODO: neibors
		m_nb.Update(route.GetGateway(), m_activeRouteTimeout);
		m_nb.Update(toOrigin.GetNextHop(), m_activeRouteTimeout);
		ucb.callback(route, p, header);
		return true;
	    }
	    else {
		if (toDst.GetValidSeqNo()) {
		    SendRerrWhenNoRouteToForward(dst, toDst.GetSeqNo(), origin);
		    MyLog.logInfo("Forwarding", "Drop packet " + p.GetUid() + " because no route to forward it.");
		    return false;
		}
	    }
	}
	MyLog.logInfo("Forwarding", "route not found to " + dst + ". Send RERR message.");
	MyLog.logInfo("Forwarding", "Drop packet " + p.GetUid() + " because no route to forward it.");
	SendRerrWhenNoRouteToForward(dst, 0, origin);
	return false;
    }

    /**
     * Repeated attempts by a source node at route discovery for a single
     * destination use the expanding ring search technique.
     * 
     * @param dst the destination IP address
     */
    void ScheduleRreqRetry(Ipv4Address dst) {
	// TODO: need to check
	if (!m_addressReqTimer.containsKey(dst)) {
	    Timer timer = new Timer();
	    m_addressReqTimer.put(dst, timer);
	}
	m_addressReqTimer.get(dst).SetFunction(() -> RouteRequestTimerExpire(dst));
	m_addressReqTimer.get(dst).Cancel();

	RoutingTableEntry rt = new RoutingTableEntry();
	m_routingTable.LookupRoute(dst, rt);
	long retry;
	if (rt.GetHop() < m_netDiameter) {
	    retry = 2 * m_nodeTraversalTime.getMillSeconds() * (rt.GetHop() + m_timeoutBuffer);
	}
	else {
	    short backoffFactor = (short) (rt.GetRreqCnt() - 1);
	    MyLog.logInfo("ScheduleRreqRetry", "Applying binary exponential backoff factor " + backoffFactor);
	    retry = m_netTraversalTime.getMillSeconds() * (1 << backoffFactor);
	}
	m_addressReqTimer.get(dst).Schedule(new Time(retry));
	MyLog.logInfo("Scheduled RREQ retry in " + retry / 1000 + "s");
    }

    /**
     * Set lifetime field in routing table entry to the maximum of existing lifetime
     * and lt, if the entry exists
     * 
     * @param addr destination address
     * @param lt   proposed time for lifetime field in routing table entry for
     *             destination with address addr.
     * @return true if route to destination address addr exist
     */
    boolean UpdateRouteLifeTime(Ipv4Address addr, Time lt) {
	RoutingTableEntry rt = new RoutingTableEntry();
	if (m_routingTable.LookupRoute(addr, rt)) {
	    if (rt.GetFlag() == RouteFlags.VALID) {
		MyLog.logOut("UpdateRouteLifeTime", "Updating VALID route", 2);
		rt.SetRreqCnt((byte) 0);
		rt.SetLifeTime(Time.MAX(lt, rt.GetLifeTime()));
		m_routingTable.Update(rt);
		return true;
	    }
	}
	return false;
    }

    /**
     * Update neighbor record.
     * 
     * @param receiver is supposed to be my interface
     * @param sender   is supposed to be IP address of my neighbor.
     */
    void UpdateRouteToNeighbor(Ipv4Address sender, Ipv4Address receiver) {
	RoutingTableEntry toNeighbor = new RoutingTableEntry();
	if (!m_routingTable.LookupRoute(sender, toNeighbor)) {
	    NetDevice dev = m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver));
	    RoutingTableEntry newEntry = new RoutingTableEntry(
		    /* dev= */dev,
		    /* dst= */sender,
		    /* vSeqNo= */false,
		    /* seqNo= */0,
		    /* iface= */m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0),
		    /* hops= */(short) 1,
		    /* nextHop= */sender,
		    /* lifetime= */m_activeRouteTimeout);
	    m_routingTable.AddRoute(newEntry);
	}
	else {
	    NetDevice dev = m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver));
	    if(toNeighbor.GetValidSeqNo() && (toNeighbor.GetHop()==1) && (toNeighbor.GetOutputDevice()==dev)) {
		toNeighbor.SetLifeTime(Time.MAX(m_activeRouteTimeout, toNeighbor.GetLifeTime()));
	    }
	    else {
		RoutingTableEntry newEntry = new RoutingTableEntry(
			    /* dev= */dev,
			    /* dst= */sender,
			    /* vSeqNo= */false,
			    /* seqNo= */0,
			/* iface= */m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0),
			    /* hops= */(short) 1,
			    /* nextHop= */sender,
			    /* lifetime= */Time.MAX(m_activeRouteTimeout, toNeighbor.GetLifeTime()));
		m_routingTable.Update(newEntry);
	    }
	}
    }

    /**
     * Test whether the provided address is assigned to an interface on this node
     * 
     * @param src the source IP address
     * @returns true if the IP address is the node's IP address
     */
    boolean IsMyOwnAddress(Ipv4Address src) {
	for (Map.Entry<Socket, Ipv4InterfaceAddress> i : m_socketAddresses.entrySet()) {
	    if (src == i.getValue().GetLocal()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Find unicast socket with local interface address iface
     *
     * @param iface the interface
     * @returns the socket associated with the interface
     */
    Socket FindSocketWithInterfaceAddress(Ipv4InterfaceAddress iface) {
	for (Map.Entry<Socket, Ipv4InterfaceAddress> i : m_socketAddresses.entrySet()) {
	    if (i.getValue() == iface) {
		return i.getKey();
	    }
	}
	// TODO:need to check
	return new Socket();
    }

    /**
     * Find subnet directed broadcast socket with local interface address iface
     *
     * @param iface the interface
     * @returns the socket associated with the interface
     */
    Socket FindSubnetBroadcastSocketWithInterfaceAddress(Ipv4InterfaceAddress iface) {
	for (Map.Entry<Socket, Ipv4InterfaceAddress> i : m_socketSubnetBroadcastAddresses.entrySet()) {
	    if (i.getValue() == iface) {
		return i.getKey();
	    }
	}
	// TODO:need to check
	return new Socket();
    }

    /**
     * Process hello message
     *
     * @param rrepHeader        RREP message header
     * @param receiverIfaceAddr receiver interface IP address
     */
    void ProcessHello(RrepHeader rrepHeader, Ipv4Address receiverIfaceAddr) {
	/*
	 * Whenever a node receives a Hello message from a neighbor, the node SHOULD
	 * make sure that it has an active route to the neighbor, and create one if
	 * necessary.
	 */
	RoutingTableEntry toNeighbor = new RoutingTableEntry();
	if(!m_routingTable.LookupRoute(rrepHeader.GetDst(), toNeighbor)) {
	    NetDevice dev = m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiverIfaceAddr));
	    RoutingTableEntry newEntry = new RoutingTableEntry(
		    /*dev=*/dev,
	            /*dst=*/rrepHeader.GetDst(),
	            /*vSeqNo=*/true,
	            /*seqNo=*/rrepHeader.GetDstSeqno(),
	            /*iface=*/m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiverIfaceAddr), 0),
	            (short) /*hops=*/1,
	            /*nextHop=*/rrepHeader.GetDst(),
	            /*lifetime=*/new Time(rrepHeader.GetLifeTime()));
	    m_routingTable.AddRoute(newEntry);
	}
	else {
	    toNeighbor.SetLifeTime(Time.MAX(Time.multiply(m_allowedHelloLoss, m_helloInterval), toNeighbor.GetLifeTime()));
	    toNeighbor.SetSeqNo(rrepHeader.GetDstSeqno());
	    toNeighbor.SetValidSeqNo(true);
	    toNeighbor.SetFlag(RouteFlags.VALID);
	    toNeighbor.SetOutputDevice(m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiverIfaceAddr)));
	    toNeighbor.SetInterface(m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiverIfaceAddr), 0));
	    toNeighbor.SetHop((short) 1);
	    toNeighbor.SetNextHop(rrepHeader.GetDst());
	    m_routingTable.Update(toNeighbor);
	}
	if (m_enableHello) {
	    m_nb.Update((Ipv4Address) rrepHeader.GetDst(), Time.multiply(m_allowedHelloLoss, m_helloInterval));
	}
    }

    /**
     * Create loopback route for given header
     *
     * @param header the IP header
     * @param oif    the output interface net device
     * @returns the route
     */
    Ipv4Route LoopbackRoute(Ipv4Header header, NetDevice oif) {
	assert (m_lo != null);
	Ipv4Route rt = new Ipv4Route();
	rt.SetDestination(header.GetDestination());
	//
	// Source address selection here is tricky. The loopback route is
	// returned when AODV does not have a route; this causes the packet
	// to be looped back and handled (cached) in RouteInput() method
	// while a route is found. However, connection-oriented protocols
	// like TCP need to create an endpoint four-tuple (src, src port,
	// dst, dst port) and create a pseudo-header for checksumming. So,
	// AODV needs to guess correctly what the eventual source address
	// will be.
	//
	// For single interface, single address nodes, this is not a problem.
	// When there are possibly multiple outgoing interfaces, the policy
	// implemented here is to pick the first available AODV interface.
	// If RouteOutput() caller specified an outgoing interface, that
	// further constrains the selection of source address
	//
	if (oif != null) {
	    for (Map.Entry<Socket, Ipv4InterfaceAddress> j : m_socketAddresses.entrySet()) {
		Ipv4Address addr = j.getValue().GetLocal();
		int iface = m_ipv4.GetInterfaceForAddress(addr);
		if (oif == m_ipv4.GetNetDevice(iface)) {
		    rt.SetSource(addr);
		    break;
		}
	    }
	} else {
	    for (Map.Entry<Socket, Ipv4InterfaceAddress> j : m_socketAddresses.entrySet()) {
		rt.SetSource(j.getValue().GetLocal());
		break;
	    }
	}
	// 判断rt->GetSource() != Ipv4Address()不是未初始化的地址
	// NS_ASSERT_MSG(rt->GetSource() != Ipv4Address(), "Valid AODV source address
	// not found");
	rt.SetGateway(Ipv4Address.GetLoopback());
	rt.SetOutputDevice(m_lo);
	return rt;
    }

    /**
     * \name Receive control packets @{
     */
    /**
     * Receive and process control packet
     * 
     * @param socket input socket
     */
    void RecvAodv(Socket socket) {
	Address sourceAddress = new Address();
	// TODO: rewrite socket use rawsocket
	// Packet packet = socket.getInputStream().read();
	Packet packet = new Packet(); // 临时
	// TODO: InetSocketAddress
	osak.ext.ns3.network.utils.InetSocketAddress inetSourceAddr = osak.ext.ns3.network.utils.InetSocketAddress.ConvertFrom(sourceAddress);
	Ipv4Address sender = inetSourceAddr.GetIpv4();
	Ipv4Address receiver = new Ipv4Address();

	if (m_socketAddresses.containsKey(socket)) {
	    // TODO: ipv4
	    receiver = m_socketAddresses.get(socket).GetLocal();
	} else if (m_socketSubnetBroadcastAddresses.containsKey(socket)) {
	    receiver = m_socketSubnetBroadcastAddresses.get(socket).GetLocal();
	}
	else {
	    MyLog.logOut("RoutingProtocol::RecvAodv", "Received a packet from an unknown socket", 4);
	    assert (false);
	}
	MyLog.logOut("RoutingProtocol::RecvAodv",
		"AODV node " + this + " received a AODV packet from " + sender + " to " + receiver, 2);
	// TODO:ipv4
	UpdateRouteToNeighbor(sender, receiver);
	TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREQ);
	packet.RemoveHeader(tHeader);
	if (!tHeader.IsValid()) {
	    // TODO:log_debug
	    return; // drop
	}
	switch (tHeader.Get()) {
	case AODVTYPE_RREQ: {
	    RecvRequest(packet, receiver, sender);
	    break;
	}
	case AODVTYPE_RREP: {
	    RecvReply(packet, receiver, sender);
	    break;
	}
	case AODVTYPE_RERR: {
	    RecvError(packet, sender);
	    break;
	}
	case AODVTYPE_RREP_ACK: {
	    RecvReplyAck(sender);
	    break;
	}
	}
    }

    /**
     * Receive RREQ
     * 
     * @param p        packet
     * @param receiver receiver address
     * @param src      sender address
     */
    void RecvRequest(Packet p, Ipv4Address receiver, Ipv4Address src) {
	RreqHeader rreqHeader = new RreqHeader();
	p.RemoveHeader(rreqHeader);

	// A node ignores all RREQs received from any node in its blacklist
	RoutingTableEntry toPrev = new RoutingTableEntry();
	if (m_routingTable.LookupRoute(src, toPrev)) {
	    if (toPrev.IsUnidirectional()) {
		MyLog.logOut(this.getClass().getName() + "RecvRequest", "Ignoring RREQ from node in blacklist", 2);
		return;
	    }
	}

	int id = rreqHeader.GetId();
	// TODO: Ipv4Address origin = new Ipv4Address((Inet4Address)
	// rreqHeader.GetOrigin());
	Ipv4Address origin = rreqHeader.GetOrigin();

	/*
	 * Node checks to determine whether it has received a RREQ with the same
	 * Originator IP Address and RREQ ID. If such a RREQ has been received, the node
	 * silently discards the newly received RREQ.
	 */
	if (m_rreqIdCache.IsDuplicate(origin, id)) {
	    MyLog.logOut(this.getClass().getName() + "RecvRequest", "Ignoring RREQ due to duplicate", 2);
	    return;
	}

	// Increment RREQ hop count
	byte hop = (byte) (rreqHeader.GetHopCount() + 1);
	rreqHeader.SetHopCount(hop);

	/*
	 *  When the reverse route is created or updated, the following actions on the route are also
	 * carried out:
	 *  1. the Originator Sequence Number from the RREQ is compared to the corresponding destination
	 * sequence number in the route table entry and copied if greater than the existing value there
	 *  2. the valid sequence number field is set to true;
	 *  3. the next hop in the routing table becomes the node from which the  RREQ was received
	 *  4. the hop count is copied from the Hop Count in the RREQ message;
	 *  5. the Lifetime is set to be the maximum of (ExistingLifetime, MinimalLifetime), where
	 *     MinimalLifetime = current time + 2*NetTraversalTime - 2*HopCount*NodeTraversalTime
	 */
	RoutingTableEntry toOrigin = new RoutingTableEntry();
	if (!m_routingTable.LookupRoute(origin, toOrigin))
	{
	    NetDevice dev = m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver));
	    RoutingTableEntry newEntry = new RoutingTableEntry(
		    /*dev=*/dev,
		    /*dst=*/origin,
		    /*vSeqNo=*/true,
		    /*seqNo=*/rreqHeader.GetOriginSeqno(),
		    /*iface=*/m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0),
		    /*hops=*/hop,
		    /*nextHop=*/src,
		    /*lifetime=*/Time.sub(Time.multiply(2 , m_netTraversalTime) , Time.multiply(2*hop, m_nodeTraversalTime)));
	    m_routingTable.AddRoute(newEntry);
	}
	else
	{
	    if (toOrigin.GetValidSeqNo())
	    {
		if ((int)(rreqHeader.GetOriginSeqno()) - (int)(toOrigin.GetSeqNo()) > 0)
		{
		    toOrigin.SetSeqNo(rreqHeader.GetOriginSeqno());
		}
	    }
	    else
	    {
		toOrigin.SetSeqNo(rreqHeader.GetOriginSeqno());
	    }
	    toOrigin.SetValidSeqNo(true);
	    toOrigin.SetNextHop(src);
	    toOrigin.SetOutputDevice(m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver)));
	    toOrigin.SetInterface(m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0));
	    toOrigin.SetHop(hop);
	    toOrigin.SetLifeTime(
		    Time.MAX(Time.sub(Time.multiply(2 , m_netTraversalTime) , Time.multiply(2*hop, m_nodeTraversalTime)),
			    toOrigin.GetLifeTime()));
	    m_routingTable.Update(toOrigin);
	    // m_nb.Update (src, Time (AllowedHelloLoss * HelloInterval));
	}

	RoutingTableEntry toNeighbor = new RoutingTableEntry();
	if (!m_routingTable.LookupRoute(src, toNeighbor))
	{
	    MyLog.logOut(this.getClass().getName() + "RecvRequest", "Neighbor:" + src + " not found in routing table. Creating an entry", 2);
	    NetDevice dev = m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver));
	    RoutingTableEntry newEntry = new RoutingTableEntry(dev,
		    src,
		    false,
		    rreqHeader.GetOriginSeqno(),
		    m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0),
		    (short) 1,
		    src,
		    m_activeRouteTimeout);
	    m_routingTable.AddRoute(newEntry);
	}
	else
	{
	    toNeighbor.SetLifeTime(m_activeRouteTimeout);
	    toNeighbor.SetValidSeqNo(false);
	    toNeighbor.SetSeqNo(rreqHeader.GetOriginSeqno());
	    toNeighbor.SetFlag(RouteFlags.VALID);
	    toNeighbor.SetOutputDevice(m_ipv4.GetNetDevice(m_ipv4.GetInterfaceForAddress(receiver)));
	    toNeighbor.SetInterface(m_ipv4.GetAddress(m_ipv4.GetInterfaceForAddress(receiver), 0));
	    toNeighbor.SetHop((short) 1);
	    toNeighbor.SetNextHop(src);
	    m_routingTable.Update(toNeighbor);
	}
	m_nb.Update(src, Time.multiply(m_allowedHelloLoss , m_helloInterval));

	MyLog.logInfo(this.getClass().getName() + "RecvRequest", 
		receiver + " receive RREQ with hop count "
			+ rreqHeader.GetHopCount() + " ID "
			+ rreqHeader.GetId() + " to destination " + rreqHeader.GetDst());

	//  A node generates a RREP if either:
	//  (i)  it is itself the destination,
	if (IsMyOwnAddress(rreqHeader.GetDst()))
	{
	    m_routingTable.LookupRoute(origin, toOrigin);
	    MyLog.logOut(this.getClass().getName() + "RecvRequest", "Send reply since I am the destination", 2);
	    SendReply(rreqHeader, toOrigin);
	    return;
	}

	/*
	 * (ii) or it has an active route to the destination, the destination sequence number in the
	 * node's existing route table entry for the destination is valid and greater than or equal to
	 * the Destination Sequence Number of the RREQ, and the "destination only" flag is NOT set.
	 */
	RoutingTableEntry toDst = new RoutingTableEntry();
	Ipv4Address dst = rreqHeader.GetDst();
	if (m_routingTable.LookupRoute(dst, toDst))
	{
	    /*
	     * Drop RREQ, This node RREP will make a loop.
	     */
	    if (toDst.GetNextHop() == src)
	    {
		MyLog.logOut(this.getClass().getName() + "RecvRequest",
			"Drop RREQ from " + src + ", dest next hop " + toDst.GetNextHop(), 2);
		return;
	    }
	    /*
	     * The Destination Sequence number for the requested destination is set to the maximum of
	     * the corresponding value received in the RREQ message, and the destination sequence value
	     * currently maintained by the node for the requested destination. However, the forwarding
	     * node MUST NOT modify its maintained value for the destination sequence number, even if
	     * the value received in the incoming RREQ is larger than the value currently maintained by
	     * the forwarding node.
	     */
	    if ((rreqHeader.GetUnknownSeqno() ||
		    (toDst.GetSeqNo() - rreqHeader.GetDstSeqno() >= 0)) &&
		    toDst.GetValidSeqNo())
	    {
		if (!rreqHeader.GetDestinationOnly() && toDst.GetFlag() == RouteFlags.VALID)
		{
		    m_routingTable.LookupRoute(origin, toOrigin);
		    SendReplyByIntermediateNode(toDst, toOrigin, rreqHeader.GetGratuitousRrep());
		    return;
		}
		rreqHeader.SetDstSeqno(toDst.GetSeqNo());
		rreqHeader.SetUnknownSeqno(false);
	    }
	}
	/* TODO: tag
	    SocketIpTtlTag tag;
	    p->RemovePacketTag(tag);
	    if (tag.GetTtl() < 2)
	    {
	        NS_LOG_DEBUG("TTL exceeded. Drop RREQ origin " << src << " destination " << dst);
	        return;
	    }
	 */
	for (Map.Entry<Socket, Ipv4InterfaceAddress> j:m_socketAddresses.entrySet())
	{
	    Socket socket = j.getKey();
	    Ipv4InterfaceAddress iface = j.getValue();
	    Packet packet = new Packet();
	    
	    /* TODO: tagttl
	        SocketIpTtlTag ttl;
	        ttl.SetTtl(tag.GetTtl() - 1);
	        packet->AddPacketTag(ttl);
	     */
	    
	    packet.AddHeader(rreqHeader);
	    TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREQ);
	    packet.AddHeader(tHeader);
	    // Send to all-hosts broadcast if on /32 addr, subnet-directed otherwise
	    Ipv4Address destination = new Ipv4Address();
	    if (iface.GetMask() == Ipv4Mask.GetOnes())
	    {
		destination = new Ipv4Address("255.255.255.255");
	    }
	    else
	    {
		destination = new Ipv4Address(iface.GetBroadcast());
	    }
	    m_lastBcastTime = Time.Now();

	    final Ipv4Address dest = destination;
	    Timer.Schedules(new Time(m_uniformRandomVariable.nextInt(10)),
		    ()->this.SendTo(socket,packet,dest));
	    /*
	        Simulator::Schedule(Time(MilliSeconds(m_uniformRandomVariable->GetInteger(0, 10))),
	                            &RoutingProtocol::SendTo,
	                            this,
	                            socket,
	                            packet,
	                            destination);*/
	}
    }

    /**
     * Receive RREP
     * 
     * @param p   packet
     * @param my  destination address
     * @param src sender address
     */
    void RecvReply(Packet p, Ipv4Address my, Ipv4Address src) {
	// TODO:
    }

    /**
     * Receive RREP_ACK
     * 
     * @param neighbor neighbor address
     */
    void RecvReplyAck(Ipv4Address neighbor) {
	RoutingTableEntry rt = new RoutingTableEntry();
	if (m_routingTable.LookupRoute(neighbor, rt)) {
	    rt.m_ackTimer.Cancel();
	    rt.SetFlag(RouteFlags.VALID);
	    m_routingTable.Update(rt);
	}
    }

    /**
     * Receive RERR
     * 
     * @param p   packet
     * @param src sender address
     */
    /// Receive from node with address src
    void RecvError(Packet p, Ipv4Address src) {
	// TODO:
    }

    /** @} */

    /**
     * \name Send @{
     */
    /**
     * Forward packet from route request queue
     * 
     * @param dst   destination address
     * @param route route to use
     */
    void SendPacketFromQueue(Ipv4Address dst, Ipv4Route route) {
	QueueEntry queueEntry = new QueueEntry();
	while (m_queue.Dequeue(dst, queueEntry)) {
	    // TODO: tag
	    DeferredRouteOutputTag tag = new DeferredRouteOutputTag();
	    Packet p = queueEntry.GetPacket();
	    if (p.RemovePacketTag(tag) && tag.GetInterface() != -1
		    && tag.GetInterface() != m_ipv4.GetInterfaceForDevice(route.GetOutputDevice())) {
		MyLog.logOut("Output device doesn't match. Dropped.", MyLog.DEBUG);
		return;
	    }
	    UnicastForwardCallback ucb = queueEntry.GetUnicastForwardCallback();
	    Ipv4Header header = queueEntry.GetIpv4Header();
	    header.SetSource(route.GetSource());
	    header.SetTtl((byte) (header.GetTtl() + 1)); // compensate extra TTL decrement by fake loopback routing
	    ucb.callback(route, p, header);
	}
    }

    /// Send hello
    void SendHello() {
	/* Broadcast a RREP with TTL = 1 with the RREP message fields set as follows:
	 *   Destination IP Address         The node's IP address.
	 *   Destination Sequence Number    The node's latest sequence number.
	 *   Hop Count                      0
	 *   Lifetime                       AllowedHelloLoss * HelloInterval
	 */
	for(Map.Entry<Socket, Ipv4InterfaceAddress> j : m_socketAddresses.entrySet()) {
	    Socket socket = j.getKey();
	    Ipv4InterfaceAddress iface = j.getValue();
	    RrepHeader helloHeader = new RrepHeader(
		    /* prefixSize= */(byte) 0,
                    /*hopCount=*/(byte)0,
                    /*dst=*/iface.GetLocal(),
                    /*dstSeqNo=*/m_seqNo,
                    /*origin=*/iface.GetLocal(),
                    /*lifetime=*/(int)Time.multiply(m_allowedHelloLoss , m_helloInterval).getMillSeconds());
	    Packet packet = new Packet();
	    /*
	     * TODO: tag 
	     * SocketIpTtlTag tag; 
	     * tag.SetTtl(1); 
	     * packet->AddPacketTag(tag);
	     */
	    packet.AddHeader(helloHeader);
	    TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREP);
	    packet.AddHeader(tHeader);
	    // Send to all-hosts broadcast if on /32 addr, subnet-directed otherwise
	    Ipv4Address destination = new Ipv4Address();
	    if (iface.GetMask() == Ipv4Mask.GetOnes()) {
		destination = new Ipv4Address("255.255.255.255");
	    } else {
		destination = new Ipv4Address(iface.GetBroadcast());
	    }
	    Time jitter = new Time(m_uniformRandomVariable.nextInt(10));
	    final Ipv4Address dest = destination;
	    Timer.Schedules(jitter, () -> this.SendTo(socket, packet, dest));
	}
    }

    /**
     * Send RREQ
     * 
     * @param dst destination address
     */
    void SendRequest(Ipv4Address dst) {
	// A node SHOULD NOT originate more than RREQ_RATELIMIT RREQ messages per
	// second.
	if (m_rreqCount == m_rreqRateLimit) {
	    Timer.Schedules(Time.add(m_rreqRateLimitTimer.GetDelayLeft(), new Time(100, TimeUnit.MICROSECONDS)),
		    () -> this.SendRequest(dst));
	    return;
	} else {
	    m_rreqCount++;
	}
	// Create RREQ header
	RreqHeader rreqHeader = new RreqHeader();
	rreqHeader.SetDst(dst);

	RoutingTableEntry rt = new RoutingTableEntry();
	// Using the Hop field in Routing Table to manage the expanding ring search
	short ttl = m_ttlStart;
	if (m_routingTable.LookupRoute(dst, rt)) {
	    if (rt.GetFlag() != RouteFlags.IN_SEARCH) {
		ttl = (short) Math.min(rt.GetHop() + m_ttlIncrement, m_netDiameter);
	    } else {
		ttl = (short) (rt.GetHop() + m_ttlIncrement);
		if (ttl > m_ttlThreshold) {
		    ttl = (short) m_netDiameter;
		}
	    }
	    if (ttl == m_netDiameter) {
		rt.IncrementRreqCnt();
	    }
	    if (rt.GetValidSeqNo()) {
		rreqHeader.SetDstSeqno(rt.GetSeqNo());
	    } else {
		rreqHeader.SetUnknownSeqno(true);
	    }
	    rt.SetHop(ttl);
	    rt.SetFlag(RouteFlags.IN_SEARCH);
	    rt.SetLifeTime(m_pathDiscoveryTime);
	    m_routingTable.Update(rt);
	} else {
	    rreqHeader.SetUnknownSeqno(true);
	    NetDevice dev = null;
	    RoutingTableEntry newEntry = new RoutingTableEntry(
		    /* dev= */dev, 
		    /* dst= */dst, 
		    /* vSeqNo= */false,
		    /* seqNo= */0, 
		    /* iface= */new Ipv4InterfaceAddress(), 
		    /* hops= */ttl,
		    /* nextHop= */new Ipv4Address(), 
		    /* lifetime= */m_pathDiscoveryTime);

	    // Check if TtlStart == NetDiameter
	    if (ttl == m_netDiameter) {
		newEntry.IncrementRreqCnt();
	    }
	    newEntry.SetFlag(RouteFlags.IN_SEARCH);
	    m_routingTable.AddRoute(newEntry);
	}

	if (m_gratuitousReply) {
	    rreqHeader.SetGratuitousRrep(true);
	}
	if (m_destinationOnly) {
	    rreqHeader.SetDestinationOnly(true);
	}

	m_seqNo++;
	rreqHeader.SetOriginSeqno(m_seqNo);
	m_requestId++;
	rreqHeader.SetId(m_requestId);
	
	// Send RREQ as subnet directed broadcast from each interface used by aodv
	for(Map.Entry<Socket, Ipv4InterfaceAddress> j : m_socketAddresses.entrySet()) {
	    Socket socket = j.getKey();
	    Ipv4InterfaceAddress iface = j.getValue();
	    
	    rreqHeader.SetOrigin(iface.GetLocal());
	    m_rreqIdCache.IsDuplicate(iface.GetLocal(), m_requestId);
	    
	    Packet packet = new Packet();
	    // TODO: Tag
	    /*
	     * <pre>
	     * SocketIpTtlTag tag;
	     * tag.SetTtl(ttl);
	     * packet.AddPacketTag(tag);
	     * </pre>
	     */
	    packet.AddHeader(rreqHeader);
	    TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREQ);
	    packet.AddHeader(tHeader);
	    // Send to all-hosts broadcast if on /32 addr, subnet-directed otherwise
	    Ipv4Address destination;
	    if (iface.GetMask() == Ipv4Mask.GetOnes())
	    {
		destination = new Ipv4Address("255.255.255.255");
	    }
	    else
	    {
		destination = new Ipv4Address(iface.GetBroadcast());
	    }
	    MyLog.logOut("RoutingProtocol::SendRequest", "Send RREQ with id " + rreqHeader.GetId() + " to socket", 2);
	    m_lastBcastTime = Time.Now();
	    final Ipv4Address dest = destination;
	    Timer.Schedules(new Time(m_uniformRandomVariable.nextInt(10)), 
		    ()->this.SendTo(socket, packet, dest));
	}
	ScheduleRreqRetry(new Ipv4Address(dst));
    }

    /**
     * Send RREP
     * 
     * @param rreqHeader route request header
     * @param toOrigin   routing table entry to originator
     */
    void SendReply(RreqHeader rreqHeader, RoutingTableEntry toOrigin) {
	/*
	 * Destination node MUST increment its own sequence number by one if the sequence number in the
	 * RREQ packet is equal to that incremented value. Otherwise, the destination does not change
	 * its sequence number before generating the  RREP message.
	 */
	if (!rreqHeader.GetUnknownSeqno() && (rreqHeader.GetDstSeqno() == m_seqNo + 1))
	{
	    m_seqNo++;
	}
	RrepHeader rrepHeader = new RrepHeader(/*prefixSize=*/(byte)0,
		/*hopCount=*/(byte)0,
		/*dst=*/rreqHeader.GetDst(),
		/*dstSeqNo=*/m_seqNo,
		/*origin=*/toOrigin.GetDestination(),
		/*lifetime=*/(int) m_myRouteTimeout.getMillSeconds());
	Packet packet = new Packet();
	/*
	SocketIpTtlTag tag;
	tag.SetTtl(toOrigin.GetHop());
	packet->AddPacketTag(tag);
	*/
	packet.AddHeader(rrepHeader);
	TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREP);
	packet.AddHeader(tHeader);
	Socket socket = FindSocketWithInterfaceAddress(toOrigin.GetInterface());
	assert (socket != null);
	// TODO: need to write
	// socket.SendTo(packet, 0, InetSocketAddress(toOrigin.GetNextHop(), AODV_PORT));
    }

    /**
     * Send RREP by intermediate node
     * 
     * @param toDst    routing table entry to destination
     * @param toOrigin routing table entry to originator
     * @param gratRep  indicates whether a gratuitous RREP should be unicast to
     *                 destination
     */
    void SendReplyByIntermediateNode(RoutingTableEntry toDst, RoutingTableEntry toOrigin, boolean gratRep) {
	RrepHeader rrepHeader = new RrepHeader(/*prefixSize=*/(byte)0,
		/*hopCount=*/(byte) toDst.GetHop(),
		/*dst=*/toDst.GetDestination(),
		/*dstSeqNo=*/toDst.GetSeqNo(),
		/*origin=*/toOrigin.GetDestination(),
		/*lifetime=*/(int) toDst.GetLifeTime().getMillSeconds());
	
	/* If the node we received a RREQ for is a neighbor we are
	 * probably facing a unidirectional link... Better request a RREP-ack
	 */
	if (toDst.GetHop() == 1)
	{
	    rrepHeader.SetAckRequired(true);
	    RoutingTableEntry toNextHop = new RoutingTableEntry();
	    m_routingTable.LookupRoute(toOrigin.GetNextHop(), toNextHop);
	    toNextHop.m_ackTimer.SetFunction(()->this.AckTimerExpire(toNextHop.GetDestination(),m_blackListTimeout));
	    toNextHop.m_ackTimer.SetDelay(m_nextHopWait);
	}
	toDst.InsertPrecursor(toOrigin.GetNextHop());
	toOrigin.InsertPrecursor(toDst.GetNextHop());
	m_routingTable.Update(toDst);
	m_routingTable.Update(toOrigin);

	Packet packet = new Packet();
	/* TODO: tag
	SocketIpTtlTag tag;
	tag.SetTtl(toOrigin.GetHop());
	packet->AddPacketTag(tag);
	*/
	packet.AddHeader(rrepHeader);
	TypeHeader tHeader = new TypeHeader(MessageType.AODVTYPE_RREP);
	packet.AddHeader(tHeader);
	Socket socket = FindSocketWithInterfaceAddress(toOrigin.GetInterface());
	assert(socket!=null);
	// TODO: socket
	// socket.SendTo(packet, 0, InetSocketAddress(toOrigin.GetNextHop(), AODV_PORT));

	// Generating gratuitous RREPs
	if (gratRep)
	{
	    RrepHeader gratRepHeader = new RrepHeader(/*prefixSize=*/(byte) 0,
		    /*hopCount=*/(byte) toOrigin.GetHop(),
		    /*dst=*/toOrigin.GetDestination(),
		    /*dstSeqNo=*/toOrigin.GetSeqNo(),
		    /*origin=*/toDst.GetDestination(),
		    /*lifetime=*/(int) toOrigin.GetLifeTime().getMillSeconds());
	    Packet packetToDst = new Packet();
	    /*
	    SocketIpTtlTag gratTag;
	    gratTag.SetTtl(toDst.GetHop());
	    packetToDst->AddPacketTag(gratTag);
	    */
	    packetToDst.AddHeader(gratRepHeader);
	    TypeHeader type = new TypeHeader(MessageType.AODVTYPE_RREP);
	    packetToDst.AddHeader(type);
	    Socket socket1 = FindSocketWithInterfaceAddress(toDst.GetInterface());
	    assert(socket1!=null);
	    MyLog.logInfo("RoutingProtocol::SendReplyByIntermediateNode","Send gratuitous RREP " + packet.GetUid());
	    // TODO: socket
	    // socket1.SendTo(packetToDst, 0, osak.ext.ns3.network.InetSocketAddress(toDst.GetNextHop(), AODV_PORT));
	}
    }

    /**
     * Send RREP_ACK
     * 
     * @param neighbor neighbor address
     */
    void SendReplyAck(Ipv4Address neighbor) {
	RrepAckHeader h = new RrepAckHeader();
	TypeHeader typeHeader = new TypeHeader(MessageType.AODVTYPE_RREP_ACK);
	Packet packet = new Packet();
	/* TODO: Tag
	SocketIpTtlTag tag;
	tag.SetTtl(1);
	packet->AddPacketTag(tag);
	*/
	packet.AddHeader(h);
	packet.AddHeader(typeHeader);
	RoutingTableEntry toNeighbor = new RoutingTableEntry();
	m_routingTable.LookupRoute(neighbor, toNeighbor);
	Socket socket = FindSocketWithInterfaceAddress(toNeighbor.GetInterface());
	assert(socket!=null);
	// TODO: socket
	// socket->SendTo(packet, 0, InetSocketAddress(neighbor, AODV_PORT));
    }

    /**
     * Initiate RERR
     * 
     * @param nextHop next hop address
     */
    void SendRerrWhenBreaksLinkToNextHop(Ipv4Address nextHop) {
	RerrHeader rerrHeader = new RerrHeader();
	List<Ipv4Address> precursors = new LinkedList<>();
	Map<Ipv4Address, Integer> unreachable = new HashMap<>();

	RoutingTableEntry toNextHop = new RoutingTableEntry();
	if (!m_routingTable.LookupRoute(nextHop, toNextHop)) {
	    return;
	}
	toNextHop.GetPrecursors(precursors);
	rerrHeader.AddUnDestination(nextHop, toNextHop.GetSeqNo());
	m_routingTable.GetListOfDestinationWithNextHop(nextHop, unreachable);
	
	for (Map.Entry<Ipv4Address, Integer> i : unreachable.entrySet()) {
	    if(!rerrHeader.AddUnDestination(i.getKey(), i.getValue())) {
		MyLog.logInfo("SendRerrWhenBreaksLinkToNextHop", "Send RERR message with maximum size.");
		TypeHeader typeHeader = new TypeHeader(MessageType.AODVTYPE_RERR);
		Packet packet = new Packet();
		/* TODO: tag
		 * SocketIpTtlTag tag;
            	   tag.SetTtl(1);
            	   packet->AddPacketTag(tag);
		 */
		packet.AddHeader(rerrHeader);
		packet.AddHeader(typeHeader);
		SendRerrMessage(packet, precursors);
		rerrHeader.Clear();
	    }
	    else {
		RoutingTableEntry toDst = new RoutingTableEntry();
		m_routingTable.LookupRoute(i.getKey(), toDst);
		toDst.GetPrecursors(precursors);
		// ++i; TODO: need to check
	    }
	}
	if(rerrHeader.GetDestCount() != 0) {
	    TypeHeader typeHeader = new TypeHeader(MessageType.AODVTYPE_RERR);
	    Packet packet = new Packet();
	    /*TODO: tag
	    SocketIpTtlTag tag;
	    tag.SetTtl(1);
	    packet->AddPacketTag(tag);
	     */
	    packet.AddHeader(rerrHeader);
	    packet.AddHeader(typeHeader);
	    SendRerrMessage(packet, precursors);
	}
	unreachable.put(nextHop, toNextHop.GetSeqNo());
	m_routingTable.InvalidateRoutesWithDst(unreachable);
    }

    /**
     * Forward RERR
     * 
     * @param packet     packet
     * @param precursors list of addresses of the visited nodes
     */
    void SendRerrMessage(Packet packet, List<Ipv4Address> precursors) {
	if (precursors.isEmpty())
	{
	    MyLog.logInfo("No precursors");
	    return;
	}
	// A node SHOULD NOT originate more than RERR_RATELIMIT RERR messages per second.
	if (m_rerrCount == m_rerrRateLimit)
	{
	    // Just make sure that the RerrRateLimit timer is running and will expire
	    assert(m_rerrRateLimitTimer.IsRunning());
	    // discard the packet and return
	    MyLog.logInfo("RerrRateLimit reached at "
		    + Time.Now() + " with timer delay left "
		    + m_rerrRateLimitTimer.GetDelayLeft() + "; suppressing RERR");
	    return;
	}
	// If there is only one precursor, RERR SHOULD be unicast toward that precursor
	if (precursors.size() == 1)
	{
	    RoutingTableEntry toPrecursor = new RoutingTableEntry();
	    if (m_routingTable.LookupValidRoute(precursors.get(0), toPrecursor))
	    {
		Socket socket = FindSocketWithInterfaceAddress(toPrecursor.GetInterface());
		assert(socket!=null);
		MyLog.logInfo("one precursor => unicast RERR to "
			+ toPrecursor.GetDestination() + " from "
			+ toPrecursor.GetInterface().GetLocal());
		Time jitter  = new Time(m_uniformRandomVariable.nextInt(10));
		Timer.Schedules(jitter, () -> this.SendTo(socket, packet, precursors.get(0)));
		m_rerrCount++;
	    }
	    return;
	}
	// TODO:remaining
	//  Should only transmit RERR on those interfaces which have precursor nodes for the broken
	//  route
	List<Ipv4InterfaceAddress> ifaces = new ArrayList<>();
	RoutingTableEntry toPrecursor = new RoutingTableEntry();
	for (Ipv4Address i : precursors)
	{
	    if (m_routingTable.LookupValidRoute(i, toPrecursor) &&
		    !ifaces.contains(toPrecursor.GetInterface()))
	    {
		ifaces.add(toPrecursor.GetInterface());
	    }
	}
	
	for(Ipv4InterfaceAddress i : ifaces) {
	    Socket socket = FindSocketWithInterfaceAddress(i);
	    assert(socket!=null);
	    MyLog.logInfo("Broadcast RERR message from interface "+i.GetLocal());
	    
	    // Send to all-hosts broadcast if on /32 addr, subnet-directed otherwise
	    Packet p = packet.Copy();
	    Ipv4Address destination=new Ipv4Address();
	    if (i.GetMask() == Ipv4Mask.GetOnes())
	    {
		destination = new Ipv4Address("255.255.255.255");
	    }
	    else
	    {
		destination = new Ipv4Address(i.GetBroadcast());
	    }
	    Time jitter = new Time(m_uniformRandomVariable.nextInt(10));
	    final Ipv4Address dest = destination;
	    Timer.Schedules(jitter, ()->this.SendTo(socket, p, dest));
	}
    }

    /**
     * Send RERR message when no route to forward input packet. Unicast if there is
     * reverse route to originating node, broadcast otherwise.
     * 
     * @param dst      destination node IP address
     * @param dstSeqNo destination node sequence number
     * @param origin   originating node IP address
     */
    private void SendRerrWhenNoRouteToForward(Ipv4Address dst, int dstSeqNo, Ipv4Address origin) {
	// A node SHOULD NOT originate more than RERR_RATELIMIT RERR messages per second.
	if (m_rerrCount == m_rerrRateLimit)
	{
	    // Just make sure that the RerrRateLimit timer is running and will expire
	    assert(m_rerrRateLimitTimer.IsRunning());
	    // discard the packet and return
	    MyLog.logInfo("RerrRateLimit reached at "
		    + Time.Now() + " with timer delay left "
		    + m_rerrRateLimitTimer.GetDelayLeft() + "; suppressing RERR");
	    return;
	}
	RerrHeader rerrHeader = new RerrHeader();
	rerrHeader.AddUnDestination(dst, dstSeqNo);
	RoutingTableEntry toOrigin = new RoutingTableEntry();
	Packet packet = new Packet();
	/* TODO:tag
	    SocketIpTtlTag tag;
	    tag.SetTtl(1);
	    packet->AddPacketTag(tag);
	 */
	packet.AddHeader(rerrHeader);
	packet.AddHeader(new TypeHeader(MessageType.AODVTYPE_RERR));
	if (m_routingTable.LookupValidRoute(origin, toOrigin))
	{
	    Socket socket = FindSocketWithInterfaceAddress(toOrigin.GetInterface());
	    assert(socket!=null);
	    MyLog.logInfo("Unicast RERR to the source of the data transmission");
	    // TODO:socket
	    // socket->SendTo(packet, 0, InetSocketAddress(toOrigin.GetNextHop(), AODV_PORT));
	}
	else
	{
	    for (Map.Entry<Socket, Ipv4InterfaceAddress> i : m_socketAddresses.entrySet())
	    {
		Socket socket = i.getKey();
		Ipv4InterfaceAddress iface = i.getValue();
		assert (socket != null);
		MyLog.logInfo("Broadcast RERR message from interface " + iface.GetLocal());
		// Send to all-hosts broadcast if on /32 addr, subnet-directed otherwise
		Ipv4Address destination = new Ipv4Address();
		if (iface.GetMask() == Ipv4Mask.GetOnes())
		{
		    destination = new Ipv4Address("255.255.255.255");
		}
		else
		{
		    destination = iface.GetBroadcast();
		}
		// TODO:socket
		// socket->SendTo(packet->Copy(), 0, InetSocketAddress(destination, AODV_PORT));
	    }
	}
    }

    /** @} */

    /**
     * Send packet to destination socket
     * 
     * @param socket      destination node socket
     * @param packet      packet to send
     * @param destination destination node IP address
     */
    private void SendTo(Socket socket, Packet packet, Ipv4Address destination) {

    }

    /// Hello timer
    private Timer m_htimer;

    /// Schedule next send of hello message
    private void HelloTimerExpire() {
	Time offset = new Time(0);
	if (m_lastBcastTime.getMillSeconds() > 0) {
	    offset = Time.sub(Time.Now(), m_lastBcastTime);
	    MyLog.logOut("Hello deferred due to last bcast at: " + m_lastBcastTime, MyLog.DEBUG);
	} else {
	    SendHello();
	}
	m_htimer.Cancel();
	Time diff = Time.sub(m_helloInterval, offset);
	m_htimer.Schedule(Time.MAX(new Time(0), diff));
	m_lastBcastTime = new Time(0);
    }

    /// RREQ rate limit timer
    private Timer m_rreqRateLimitTimer;

    /// Reset RREQ count and schedule RREQ rate limit timer with delay 1 sec.
    private void RreqRateLimitTimerExpire() {
	m_rreqCount = 0;
	m_rreqRateLimitTimer.Schedule(new Time(1, TimeUnit.SECONDS));
    }

    /// RERR rate limit timer
    private Timer m_rerrRateLimitTimer;

    /// Reset RERR count and schedule RERR rate limit timer with delay 1 sec.
    private void RerrRateLimitTimerExpire() {
	m_rerrCount = 0;
	m_rerrRateLimitTimer.Schedule(new Time(1, TimeUnit.SECONDS));
    }

    /// Map IP address + RREQ timer.
    private Map<Ipv4Address, Timer> m_addressReqTimer = new HashMap<>();

    /**
     * Handle route discovery process
     * 
     * @param dst the destination IP address
     */
    private void RouteRequestTimerExpire(Ipv4Address dst) {
	RoutingTableEntry toDst = new RoutingTableEntry();
	if (m_routingTable.LookupValidRoute(dst, toDst)) {
	    SendPacketFromQueue(dst, toDst.GetRoute());
	    MyLog.logInfo("route to " + dst + " found");
	    return;
	}
	/*
	 * If a route discovery has been attempted RreqRetries times at the maximum TTL
	 * without receiving any RREP, all data packets destined for the corresponding
	 * destination SHOULD be dropped from the buffer and a Destination Unreachable
	 * message SHOULD be delivered to the application.
	 */
	if (toDst.GetRreqCnt() == m_rreqRetries) {
	    MyLog.logInfo("route discovery to " + dst + " has been attempted RreqRetries (" + m_rreqRetries
		    + ") times with ttl " + m_netDiameter);
	    m_addressReqTimer.remove(dst);
	    m_routingTable.DeleteRoute(dst);
	    MyLog.logOut("Route not found. Drop all packets with dst " + dst, MyLog.DEBUG);
	    m_queue.DropPacketWithDst(dst);
	    return;
	}

	if (toDst.GetFlag() == RouteFlags.IN_SEARCH) {
	    MyLog.logInfo("Resend RREQ to " + dst + " previous ttl " + toDst.GetHop());
	    SendRequest(dst);
	} else {
	    MyLog.logInfo("Route down. Stop search. Drop packet with destination " + dst);
	    m_addressReqTimer.remove(dst);
	    m_routingTable.DeleteRoute(dst);
	    m_queue.DropPacketWithDst(dst);
	}
    }

    /**
     * Mark link to neighbor node as unidirectional for blacklistTimeout
     *
     * @param neighbor         the IP address of the neighbor node
     * @param blacklistTimeout the black list timeout time
     */
    void AckTimerExpire(Ipv4Address neighbor, Time blacklistTimeout) {
	m_routingTable.MarkLinkAsUnidirectional(neighbor, blacklistTimeout);
    }

    /// Provides uniform random variables.
    Random m_uniformRandomVariable = new Random();
    /// Keep track of the last bcast time
    Time m_lastBcastTime;
    
    /// constructor
    public RoutingProtocol() {
	m_rreqRetries = 2;
	m_ttlStart = 1;
	m_ttlIncrement = 2;
	m_ttlThreshold = 7;
	m_timeoutBuffer = 2;
	m_rreqRateLimit = 10;
	m_rerrRateLimit = 10;
	m_activeRouteTimeout = new Time(3, TimeUnit.SECONDS);
	m_netDiameter = 35;
	m_nodeTraversalTime = new Time(40);
	m_netTraversalTime = Time.multiply(2 * m_netDiameter, m_nodeTraversalTime);
	m_pathDiscoveryTime = Time.multiply(2, m_netTraversalTime);
	m_myRouteTimeout = Time.multiply(2, Time.MAX(m_pathDiscoveryTime, m_activeRouteTimeout));
	m_helloInterval = new Time(1, TimeUnit.SECONDS);
	m_allowedHelloLoss = 2;
	m_deletePeriod = Time.multiply(5, Time.MAX(m_activeRouteTimeout, m_helloInterval));
	m_nextHopWait = Time.add(m_nodeTraversalTime, new Time(10));
	m_blackListTimeout = Time.multiply(m_rreqRetries, m_netTraversalTime);
	m_maxQueueLen = 64;
	m_maxQueueTime = new Time(30, TimeUnit.SECONDS);
	m_destinationOnly = false;
	m_gratuitousReply = true;
	m_enableHello = false;
	m_routingTable = new RoutingTable(m_deletePeriod);
	m_queue = new RequestQueue(m_maxQueueLen, m_maxQueueTime);
	m_requestId = 0;
	m_seqNo = 0;
	// TODO:time
	m_rreqIdCache = new IdCache(m_pathDiscoveryTime.getMillSeconds());
	m_dpd = new DuplicatePacketDetection(m_pathDiscoveryTime.getMillSeconds());
	m_nb = new Neighbors(m_helloInterval);
	m_rreqCount = 0;
	m_rerrCount = 0;
	m_htimer = new Timer();
	m_rreqRateLimitTimer = new Timer();
	m_rerrRateLimitTimer = new Timer();
	m_lastBcastTime = new Time(0);
    }

    public void DoDispose() {

    }

    @Override
    public Ipv4Route RouteOutput(Packet p, Ipv4Header header, NetDevice oif, SocketErrno sockerr) {
	if (p==null)
	{
	    MyLog.logOut("Packet is == 0",MyLog.DEBUG);
	    return LoopbackRoute(header, oif); // later
	}
	if (m_socketAddresses.isEmpty())
	{
	    sockerr = SocketErrno.ERROR_NOROUTETOHOST;
	    MyLog.logInfo("No aodv interfaces");
	    Ipv4Route route = new Ipv4Route();
	    return route;
	}
	sockerr = SocketErrno.ERROR_NOTERROR;
	Ipv4Route route = new Ipv4Route();
	Ipv4Address dst = new Ipv4Address(header.GetDestination());
	RoutingTableEntry rt = new RoutingTableEntry();
	if (m_routingTable.LookupValidRoute(dst, rt))
	{
	    route = rt.GetRoute();
	    assert(route!=null);
	    MyLog.logOut("Exist route to " + route.GetDestination() + " from interface "
		    + route.GetSource(),MyLog.DEBUG);
	    if (oif!=null && route.GetOutputDevice() != oif)
	    {
		MyLog.logOut("Output device doesn't match. Dropped.",MyLog.DEBUG);
		sockerr = SocketErrno.ERROR_NOROUTETOHOST;
		return new Ipv4Route();
	    }
	    UpdateRouteLifeTime(dst, m_activeRouteTimeout);
	    UpdateRouteLifeTime(route.GetGateway(), m_activeRouteTimeout);
	    return route;
	}

	// Valid route not found, in this case we return loopback.
	// Actual route request will be deferred until packet will be fully formed,
	// routed to loopback, received from loopback and passed to RouteInput (see
	// below)
	int iif = (oif != null ? m_ipv4.GetInterfaceForDevice(oif) : -1);
	DeferredRouteOutputTag tag = new DeferredRouteOutputTag(iif);
	MyLog.logOut("Valid Route not found", MyLog.DEBUG);
	if (!p.PeekPacketTag(tag)) {
	    p.AddPacketTag(tag);
	}
	return LoopbackRoute(header, oif);
    }

    @Override
    public boolean RouteInput(Packet p, Ipv4Header header, final NetDevice idev, UnicastForwardCallback ucb, MulticastForwardCallback mcb,
	    LocalDeliverCallback lcb, ErrorCallback ecb) {
	if (m_socketAddresses.isEmpty())
	{
	    MyLog.logInfo("No aodv interfaces");
	    return false;
	}
	assert(m_ipv4!=null);
	assert(p!=null);
	
	// Check if input device supports IP
	assert(m_ipv4.GetInterfaceForDevice(idev) >= 0);
	int iif = m_ipv4.GetInterfaceForDevice(idev);

	Ipv4Address dst = new Ipv4Address(header.GetDestination());
	Ipv4Address origin = new Ipv4Address(header.GetSource());
	
	// Deferred route request
	if (idev == m_lo)
	{
	    // TODO: tag
	    /*
	    DeferredRouteOutputTag tag;
	    if (p->PeekPacketTag(tag))
	    {
		DeferredRouteOutput(p, header, ucb, ecb);
		return true;
	    }
	    */
	}
	// Duplicate of own packet
	if (IsMyOwnAddress(origin))
	{
	    return true;
	}

	// AODV is not a multicast routing protocol
	if (dst.IsMulticast())
	{
	    return false;
	}
	
	// Broadcast local delivery/forwarding
	for(Map.Entry<Socket, Ipv4InterfaceAddress> j : m_socketAddresses.entrySet()) {
	    Ipv4InterfaceAddress iface = j.getValue();
	    if (m_ipv4.GetInterfaceForAddress(iface.GetLocal()) == iif) {
		if (dst.equals(iface.GetBroadcast()) || dst.IsBroadcast()) {
		    if(m_dpd.IsDuplicate(p, header)) {
			MyLog.logOut("Duplicated packet " + p.GetUid() + " from " + origin
				+ ". Drop.",MyLog.DEBUG);
			return true;
		    }
		    UpdateRouteLifeTime(origin, m_activeRouteTimeout);
		    Packet packet = p.Copy();
		    if (lcb!= null)
		    {
			MyLog.logInfo("Broadcast local delivery to "+iface.GetLocal());
			lcb.callback(p, header, iif);
			// Fall through to additional processing
		    }
		    else
		    {
			MyLog.logOut("Unable to deliver packet locally due to null callback "
				+ p.GetUid() + " from " + origin, MyLog.ERROR);
			ecb.callback(p, header, SocketErrno.ERROR_NOROUTETOHOST);
		    }
		    if (!m_enableBroadcast)
		    {
			return true;
		    }
		    if (header.GetProtocol() == UdpL4Protocol.PROT_NUMBER)
		    {
			UdpHeader udpHeader = new UdpHeader();
			p.PeekHeader(udpHeader);
			if (udpHeader.GetDestinationPort() == AODV_PORT)
			{
			    // AODV packets sent in broadcast are already managed
			    return true;
			}
		    }
		    if (header.GetTtl() > 1)
		    {
			MyLog.logInfo("Forward broadcast. TTL " + header.GetTtl());
			RoutingTableEntry toBroadcast = new RoutingTableEntry();
			if (m_routingTable.LookupRoute(dst, toBroadcast))
			{
			    Ipv4Route route = toBroadcast.GetRoute();
			    ucb.callback(route, packet, header);
			}
			else
			{
			    MyLog.logOut("No route to forward broadcast. Drop packet " + p.GetUid(), MyLog.DEBUG);
			}
		    }
		    else
		    {
			MyLog.logOut("TTL exceeded. Drop packet " + p.GetUid(), MyLog.DEBUG);
		    }
		    return true;
			
		}
	    }
	}
	// Unicast local delivery
	if (m_ipv4.IsDestinationAddress(dst, iif)) {
	    UpdateRouteLifeTime(origin, m_activeRouteTimeout);
	    RoutingTableEntry toOrigin = new RoutingTableEntry();
	    if (m_routingTable.LookupValidRoute(origin, toOrigin)) {
		UpdateRouteLifeTime(toOrigin.GetNextHop(), m_activeRouteTimeout);
		m_nb.Update(toOrigin.GetNextHop(), m_activeRouteTimeout);
	    }
	    if (lcb != null) {
		MyLog.logInfo("Unicast local delivery to " + dst);
		lcb.callback(p, header, iif);
	    } else {
		MyLog.logOut("Unable to deliver packet locally due to null callback " + p.GetUid() + " from " + origin,
			MyLog.ERROR);
		ecb.callback(p, header, SocketErrno.ERROR_NOROUTETOHOST);
	    }
	    return true;
	}

	// Check if input device supports IP forwarding
	if (m_ipv4.IsForwarding(iif) == false) {
	    MyLog.logInfo("Forwarding disabled for this interface");
	    ecb.callback(p, header, SocketErrno.ERROR_NOROUTETOHOST);
	    return true;
	}

	// Forwarding
	return Forwarding(p, header, ucb, ecb);
    }

    @Override
    public void NotifyInterfaceUp(int iface) {
	// TODO:
    }

    @Override
    public void NotifyInterfaceDown(int iface) {
	// TODO:
    }

    @Override
    public void NotifyAddAddress(int iface, Ipv4InterfaceAddress address) {
	// TODO:
    }

    @Override
    public void NotifyRemoveAddress(int iface, Ipv4InterfaceAddress address) {
	// TODO:
    }

    @Override
    public void SetIpv4(Ipv4 ipv4) {
	assert(ipv4!=null);
	assert(m_ipv4==null);

	m_ipv4 = ipv4;

	// Create lo route. It is asserted that the only one interface up for now is loopback
	assert(m_ipv4.GetNInterfaces() == 1 &&
		m_ipv4.GetAddress(0, 0).GetLocal().equals(new Ipv4Address("127.0.0.1")));
	m_lo = m_ipv4.GetNetDevice(0);
	assert(m_lo!=null);
	// Remember lo route
	RoutingTableEntry rt = new RoutingTableEntry(
		/*dev=*/m_lo,
		/* dst= */Ipv4Address.GetLoopback(),
		/*vSeqNo=*/true,
		/*seqNo=*/0,
		/* iface= */new Ipv4InterfaceAddress(Ipv4Address.GetLoopback(), new Ipv4Mask("255.0.0.0")),
		/* hops= */(short) 1, /* nextHop= */Ipv4Address.GetLoopback(),
		/*lifetime=*/Time.Max());
	m_routingTable.AddRoute(rt);

	Timer.Schedules(new Time(0), ()->this.Start());
    }

    // Handle protocol parameters
    /**
     * Get maximum queue time
     * 
     * @returns the maximum queue time
     */
    public Time GetMaxQueueTime() {
	return m_maxQueueTime;
    }

    /**
     * Set the maximum queue time
     * 
     * @param t the maximum queue time
     */
    public void SetMaxQueueTime(Time t) {
	m_maxQueueTime = t;
	m_queue.SetQueueTimeout(t);
    }

    /**
     * Get the maximum queue length
     * 
     * @returns the maximum queue length
     */
    public int GetMaxQueueLen() {
	return m_maxQueueLen;
    }

    /**
     * Set the maximum queue length
     * 
     * @param len the maximum queue length
     */
    public void SetMaxQueueLen(int len) {
	m_maxQueueLen = len;
	m_queue.SetMaxQueueLen(len);
    }

    /**
     * Get destination only flag
     * 
     * @returns the destination only flag
     */
    public boolean GetDestinationOnlyFlag() {
	return m_destinationOnly;
    }

    /**
     * Set destination only flag
     * 
     * @param f the destination only flag
     */
    public void SetDestinationOnlyFlag(boolean f) {
	m_destinationOnly = f;
    }

    /**
     * Get gratuitous reply flag
     * 
     * @returns the gratuitous reply flag
     */
    public boolean GetGratuitousReplyFlag() {
	return m_gratuitousReply;
    }

    /**
     * Set gratuitous reply flag
     * 
     * @param f the gratuitous reply flag
     */
    public void SetGratuitousReplyFlag(boolean f) {
	m_gratuitousReply = f;
    }

    /**
     * Set hello enable
     * 
     * @param f the hello enable flag
     */
    public void SetHelloEnable(boolean f) {
	m_enableHello = f;
    }

    /**
     * Get hello enable flag
     * 
     * @returns the enable hello flag
     */
    public boolean GetHelloEnable() {
	return m_enableHello;
    }

    /**
     * Set broadcast enable flag
     * 
     * @param f enable broadcast flag
     */
    public void SetBroadcastEnable(boolean f) {
	m_enableBroadcast = f;
    }

    /**
     * Get broadcast enable flag
     * 
     * @returns the broadcast enable flag
     */
    public boolean GetBroadcastEnable() {
	return m_enableBroadcast;
    }

    /**
     * Assign a fixed random variable stream number to the random variables used by
     * this model. Return the number of streams (possibly zero) that have been
     * assigned.
     *
     * @param stream first stream index to use
     * @return the number of stream indices assigned by this model
     */
    public long AssignStreams(long stream) {
	// TODO: how to use
	return 0;
    }

    private void DoInitialize() {
	Time startTime;
	if (m_enableHello) {
	    startTime = new Time(m_uniformRandomVariable.nextInt(100));
	    MyLog.logInfo("DoInitialize", "Starting at time " + startTime + " ms");
	    m_htimer.Schedule(startTime, () -> HelloTimerExpire());
	}
    }
}
