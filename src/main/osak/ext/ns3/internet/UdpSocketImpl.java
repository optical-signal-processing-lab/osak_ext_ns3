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

import java.util.Queue;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.Callback5;
import osak.ext.ns3.core.Pair;
import osak.ext.ns3.network.*;
import osak.ext.ns3.network.utils.Inet6SocketAddress;
import osak.ext.ns3.network.utils.InetSocketAddress;
import osak.ext.ns3.network.utils.Ipv4Address;
import osak.ext.ns3.network.utils.Ipv6Address;

/**
 * TODO UdpSocketImpl
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class UdpSocketImpl extends UdpSocket {
    /**
     * Create an unbound udp socket.
     */
    public UdpSocketImpl() {
	m_endPoint = null;
	m_endPoint6 = null;
	m_node = null;
	m_udp = null;
	m_errno = SocketErrno.ERROR_NOTERROR;
	m_shutdownSend = false;
	m_shutdownRecv = false;
	m_connected = false;
	m_rxAvailable = 0;
    }

    /**
     * @brief Set the associated node.
     * @param node the node
     */
    public void SetNode(Node node) {
	m_node = node;
    }

    /**
     * @brief Set the associated UDP L4 protocol.
     * @param udp the UDP L4 protocol
     */
    public void SetUdp(UdpL4Protocol udp) {
	m_udp = udp;
    }

    @Override
    public int MulticastJoinGroup(int iface, Address groupAddress) {
	/**
	 * <pre>
	     1) sanity check interface
	     2) sanity check that it has not been called yet on this interface/group
	     3) determine address family of groupAddress
	     4) locally store a list of (interface, groupAddress)
	     5) call ipv4->MulticastJoinGroup () or Ipv6->MulticastJoinGroup ()
	 * </pre>
	 */
	return 0;
    }

    @Override
    public int MulticastLeaveGroup(int iface, Address groupAddress) {
	/**
	 * <pre>
	     1) sanity check interface
	     2) determine address family of groupAddress
	     3) delete from local list of (interface, groupAddress); raise a LOG_WARN
	        if not already present (but return 0)
	     5) call ipv4->MulticastLeaveGroup () or Ipv6->MulticastLeaveGroup ()
	 * </pre>
	 */
	return 0;
    }


    @Override
    public SocketErrno GetErrno() {
	return m_errno;
    }

    @Override
    public SocketType GetSocketType() {
	return SocketType.NS3_SOCK_DGRAM;
    }

    @Override
    public Node GetNode() {
	return m_node;
    }

    @Override
    public int Bind(Address address) {
	if (InetSocketAddress.IsMatchingType(address)) {
	    assert m_endPoint == null : "Endpoint already allocated.";

	    InetSocketAddress transport = InetSocketAddress.ConvertFrom(address);
	    Ipv4Address ipv4 = transport.GetIpv4();
	    short port = transport.GetPort();
	    SetIpTos(transport.GetTos());
	    if (ipv4.equals(Ipv4Address.GetAny()) && port == 0) {
		m_endPoint = m_udp.Allocate();
	    } else if (ipv4.equals(Ipv4Address.GetAny()) && port != 0) {
		m_endPoint = m_udp.Allocate(GetBoundNetDevice(), port);
	    } else if (!ipv4.equals(Ipv4Address.GetAny()) && port == 0) {
		m_endPoint = m_udp.Allocate(ipv4);
	    } else if (!ipv4.equals(Ipv4Address.GetAny()) && port != 0) {
		m_endPoint = m_udp.Allocate(GetBoundNetDevice(), ipv4, port);
	    }
	    if (null == m_endPoint) {
		m_errno = port != 0 ? SocketErrno.ERROR_ADDRINUSE : SocketErrno.ERROR_ADDRNOTAVAIL;
		return -1;
	    }
	    if (m_boundnetdevice != null) {
		m_endPoint.BindToNetDevice(m_boundnetdevice);
	    }
	}
	/**
	 * TODO: Ipv6 not implement yet
	 * 
	 * <pre>
	else if (Inet6SocketAddress.IsMatchingType(address)) {
	    assert m_endPoint == null : "Endpoint already allocated.";
	
	    Inet6SocketAddress transport = Inet6SocketAddress.ConvertFrom(address);
	    Ipv4Address ipv4 = transport.GetIpv6();
	    short port = transport.GetPort();
	    SetIpTos(transport.GetTos());
	    if (ipv6.equals(Ipv6Address.GetAny()) && port == 0) {
		m_endPoint = m_udp.Allocate6();
	    } else if (ipv6.equals(Ipv6Address.GetAny()) && port != 0) {
		m_endPoint = m_udp.Allocate6(GetBoundNetDevice(), port);
	    } else if (!ipv6.equals(Ipv6Address.GetAny()) && port == 0) {
		m_endPoint = m_udp.Allocate6(ipv4);
	    } else if (!ipv6.equals(Ipv6Address.GetAny()) && port != 0) {
		m_endPoint = m_udp.Allocate6(GetBoundNetDevice(), ipv4, port);
	    }
	    if (null == m_endPoint) {
		m_errno = port != 0 ? SocketErrno.ERROR_ADDRINUSE : SocketErrno.ERROR_ADDRNOTAVAIL;
		return -1;
	    }
	    if (m_boundnetdevice != null) {
		m_endPoint6.BindToNetDevice(m_boundnetdevice);
	    }
	    
	    if (ipv6.IsMulticast())
	    {
		Ipv6L3.Protocol ipv6l3 = m_node->GetObject<Ipv6L3Protocol>();
		if (ipv6l3)
		{
		    if (!m_boundnetdevice)
		    {
			ipv6l3.AddMulticastAddress(ipv6);
		    }
		    else
		    {
			int index = ipv6l3.GetInterfaceForDevice(m_boundnetdevice);
			ipv6l3.AddMulticastAddress(m_endPoint6.GetLocalAddress(), index);
		    }
		}
	    }
	}
	 * </pre>
	 */
	else {
	    MyLog.logOut("Not IsMatchingType",MyLog.ERROR);
	    m_errno = SocketErrno.ERROR_INVAL;
	    return -1;
	}

	return FinishBind();
    }

    @Override
    public int Bind() {
	m_endPoint = m_udp.Allocate();
	if (m_boundnetdevice != null) {
	    m_endPoint.BindToNetDevice(m_boundnetdevice);
	}
	return FinishBind();
    }

    @Override
    public int Bind6() {
	m_endPoint6 = m_udp.Allocate6();
	if (m_boundnetdevice != null) {
	    m_endPoint6.BindToNetDevice(m_boundnetdevice);
	}
	return FinishBind();
    }

    @Override
    public int Close() {
	if (m_shutdownRecv == true && m_shutdownSend == true) {
	    m_errno = SocketErrno.ERROR_BADF;
	    return -1;
	}
	Ipv6LeaveGroup();
	m_shutdownRecv = true;
	m_shutdownSend = true;
	DeallocateEndPoint();
	return 0;
    }

    @Override
    public int ShutdownSend() {
	m_shutdownSend = true;
	return 0;
    }

    @Override
    public int ShutdownRecv() {
	m_shutdownRecv = true;
	if (m_endPoint != null) {
	    m_endPoint.SetRxEnabled(false);
	}
	if (m_endPoint6 != null) {
	    m_endPoint6.SetRxEnabled(false);
	}
	return 0;
    }

    @Override
    public int Connect(Address address) {
	if (InetSocketAddress.IsMatchingType(address) == true) {
	    InetSocketAddress transport = InetSocketAddress.ConvertFrom(address);
	    m_defaultAddress = new Address(transport.GetIpv4().ConvertTo());
	    m_defaultPort = transport.GetPort();
	    SetIpTos(transport.GetTos());
	    m_connected = true;
	    NotifyConnectionSucceeded();
	}
	/**
	 * TODO: not implement yet
	 * 
	 * <pre>
	    else if (Inet6SocketAddress.IsMatchingType(address) == true)
	    {
	        Inet6SocketAddress transport = Inet6SocketAddress.ConvertFrom(address);
	        m_defaultAddress = Address(transport.GetIpv6());
	        m_defaultPort = transport.GetPort();
	        m_connected = true;
	        NotifyConnectionSucceeded();
	    }
	 * </pre>
	 */
	else {
	    NotifyConnectionFailed();
	    return -1;
	}

	return 0;
    }

    @Override
    public int Listen() {
	m_errno = SocketErrno.ERROR_OPNOTSUPP;
	return -1;
    }

    @Override
    public int GetTxAvailable() {
	// No finite send buffer is modelled, but we must respect
	// the maximum size of an IP datagram (65535 bytes - headers).
	return 65507;
    }

    @Override
    public int Send(Packet p, int flags) {
	if (!m_connected) {
	    m_errno = SocketErrno.ERROR_NOTCONN;
	    return -1;
	}

	return DoSend(p);
    }

    @Override
    public int SendTo(Packet p, int flags, Address toAddress) {
	if (InetSocketAddress.IsMatchingType(toAddress)) {
	    InetSocketAddress transport = InetSocketAddress.ConvertFrom(toAddress);
	    Ipv4Address ipv4 = transport.GetIpv4();
	    short port = transport.GetPort();
	    byte tos = transport.GetTos();
	    return DoSendTo(p, ipv4, port, tos);
	} else if (Inet6SocketAddress.IsMatchingType(toAddress)) {
	    Inet6SocketAddress transport = Inet6SocketAddress.ConvertFrom(toAddress);
	    Ipv6Address ipv6 = transport.GetIpv6();
	    short port = transport.GetPort();
	    return DoSendTo(p, ipv6, port);
	}
	return -1;
    }

    @Override
    public int GetRxAvailable() {
	// We separately maintain this state to avoid walking the queue
	// every time this might be called
	return m_rxAvailable;
    }

    @Override
    public Packet Recv(int maxSize, int flags) {
	Address fromAddress = new Address();
	Packet packet = RecvFrom(maxSize, flags, fromAddress);
	return packet;
    }

    @Override
    public Packet RecvFrom(int maxSize, int flags, Address fromAddress) {
	if (m_deliveryQueue.isEmpty()) {
	    m_errno = SocketErrno.ERROR_AGAIN;
	    return null;
	}
	Packet p = m_deliveryQueue.peek().first();
	fromAddress = m_deliveryQueue.peek().second();

	if (p.GetSize() <= maxSize) {
	    m_deliveryQueue.poll();
	    m_rxAvailable -= p.GetSize();
	} else {
	    p = null;
	}
	return p;
    }

    @Override
    public int GetSockName(Address address) {
	if (m_endPoint != null) {

	    address = new InetSocketAddress(m_endPoint.GetLocalAddress(), m_endPoint.GetLocalPort()).ConvertTo();
	} else if (m_endPoint6 != null) {
	    address = new Inet6SocketAddress(m_endPoint6.GetLocalAddress(), m_endPoint6.GetLocalPort()).ConvertTo();
	} else {
	    // It is possible to call this method on a socket without a name
	    // in which case, behavior is unspecified
	    // Should this return an InetSocketAddress or an Inet6SocketAddress?
	    address = new InetSocketAddress(Ipv4Address.GetZero(), (short) 0).ConvertTo();
	}
	return 0;
    }

    @Override
    public int GetPeerName(Address address) {
	if (!m_connected) {
	    m_errno = SocketErrno.ERROR_NOTCONN;
	    return -1;
	}
	if (Ipv4Address.IsMatchingType(m_defaultAddress)) {
	    Ipv4Address addr = Ipv4Address.ConvertFrom(m_defaultAddress);
	    InetSocketAddress inet = new InetSocketAddress(addr, m_defaultPort);
	    inet.SetTos(GetIpTos());
	    address = inet.ConvertTo();
	} else if (Ipv6Address.IsMatchingType(m_defaultAddress)) {
	    Ipv6Address addr = Ipv6Address.ConvertFrom(m_defaultAddress);
	    address = new Inet6SocketAddress(addr, m_defaultPort).ConvertTo();
	} else {
	    assert false : "unexpected address type";
	}

	return 0;
    }

    @Override
    public boolean SetAllowBroadcast(boolean allowBroadcast) {
	m_allowBroadcast = allowBroadcast;
	return true;
    }

    @Override
    public boolean GetAllowBroadcast() {
	return m_allowBroadcast;
    }

    @Override
    protected void SetRcvBufSize(int size) {
	m_rcvBufSize = size;
    }

    @Override
    protected int GetRcvBufSize() {
	return m_rcvBufSize;
    }

    @Override
    protected void SetIpMulticastTtl(byte ipTtl) {
	m_ipMulticastTtl = ipTtl;
    }

    @Override
    protected byte GetIpMulticastTtl() {
	return m_ipMulticastTtl;
    }

    @Override
    protected void SetIpMulticastIf(int ipIf) {
	m_ipMulticastIf = ipIf;
    }

    @Override
    protected int GetIpMulticastIf() {
	return m_ipMulticastIf;
    }

    @Override
    protected void SetIpMulticastLoop(boolean loop) {
	m_ipMulticastLoop = loop;
    }

    @Override
    protected boolean GetIpMulticastLoop() {
	return m_ipMulticastLoop;
    }

    @Override
    protected void SetMtuDiscover(boolean discover) {
	m_mtuDiscover = discover;
    }

    @Override
    protected boolean GetMtuDiscover() {
	return m_mtuDiscover;
    }
    
    /**
     * Finish the binding process
     * @returns 0 on success, -1 on failure
     */
    private int FinishBind() {
	boolean done = false;
	if (m_endPoint != null) {
	    m_endPoint.SetRxCallback((a, b, c, d) -> this.ForwardUp(a, b, c, d));
	    m_endPoint.SetIcmpCallback((a, b, c, d, e) -> this.ForwardIcmp(a, b, c, d, e));
	    m_endPoint.SetDestroyCallback(() -> this.Destroy());
	    done = true;
	}
	if (m_endPoint6 != null) {
	    m_endPoint6.SetRxCallback((a, b, c, d) -> this.ForwardUp6(a, b, c, d));
	    m_endPoint6.SetIcmpCallback((a, b, c, d, e) -> this.ForwardIcmp6(a, b, c, d, e));
	    m_endPoint6.SetDestroyCallback(() -> this.Destroy6());
	    done = true;
	}
	if (done) {
	    m_shutdownRecv = false;
	    m_shutdownSend = false;
	    return 0;
	}
	return -1;
    }

    /**
     * @brief Called by the L3 protocol when it received a packet to pass on to TCP.
     *
     * @param packet the incoming packet
     * @param header the packet's IPv4 header
     * @param port the remote port
     * @param incomingInterface the incoming interface
     */
    private void ForwardUp(Packet packet,
	    Ipv4Header header, short port, Ipv4Interface incomingInterface) {
	if (m_shutdownRecv) {
	    return;
	}

	// Should check via getsockopt ()..
	if (IsRecvPktInfo()) {
	    Ipv4PacketInfoTag tag = new Ipv4PacketInfoTag();
	    packet.RemovePacketTag(tag);
	    tag.SetAddress(header.GetDestination());
	    tag.SetTtl(header.GetTtl());
	    tag.SetRecvIf(incomingInterface.GetDevice().GetIfIndex());
	    packet.AddPacketTag(tag);
	}

	// Check only version 4 options
	if (IsIpRecvTos()) {
	    SocketIpTosTag ipTosTag = new SocketIpTosTag();
	    ipTosTag.SetTos(header.GetTos());
	    packet.AddPacketTag(ipTosTag);
	}

	if (IsIpRecvTtl()) {
	    SocketIpTtlTag ipTtlTag = new SocketIpTtlTag();
	    ipTtlTag.SetTtl(header.GetTtl());
	    packet.AddPacketTag(ipTtlTag);
	}

	// in case the packet still has a priority tag attached, remove it
	SocketPriorityTag priorityTag = new SocketPriorityTag();
	packet.RemovePacketTag(priorityTag);

	if ((m_rxAvailable + packet.GetSize()) <= m_rcvBufSize) {
	    Address address = new InetSocketAddress(header.GetSource(), port).ConvertTo();
	    m_deliveryQueue.offer(new Pair<Packet, Address>(packet, address));
	    m_rxAvailable += packet.GetSize();
	    NotifyDataRecv();
	} else {
	    // In general, this case should not occur unless the
	    // receiving application reads data from this socket slowly
	    // in comparison to the arrival rate
	    //
	    // drop and trace packet
	    MyLog.logOut("No receive buffer space available.  Drop.", MyLog.WARNING);
	    // m_dropTrace(packet);
	}

    }

    /**
     * @brief Called by the L3 protocol when it received a packet to pass on to TCP.
     *
     * @param packet the incoming packet
     * @param header the packet's IPv6 header
     * @param port the remote port
     * @param incomingInterface the incoming interface
     */
    private void ForwardUp6(Packet packet,
                    Ipv6Header header,
                    short port,
                    Ipv6Interface incomingInterface) {
	assert false : "Not implement method ForwardUp6";
    }

    /**
     * @brief Kill this socket by zeroing its attributes (IPv4)
     *
     * This is a callback function configured to m_endpoint in
     * SetupCallback(), invoked when the endpoint is destroyed.
     */
    private void Destroy() {
	m_endPoint = null;
    }

    /**
     * @brief Kill this socket by zeroing its attributes (IPv6)
     *
     * This is a callback function configured to m_endpoint in
     * SetupCallback(), invoked when the endpoint is destroyed.
     */
    private void Destroy6() {
	m_endPoint6 = null;
    }

    /**
     * @brief Deallocate m_endPoint and m_endPoint6
     */
    private void DeallocateEndPoint() {
	if (m_endPoint != null) {
	    m_endPoint.SetDestroyCallback(null);
	    m_udp.DeAllocate(m_endPoint);
	    m_endPoint = null;
	}
	if (m_endPoint6 != null) {
	    m_endPoint6.SetDestroyCallback(null);
	    m_udp.DeAllocate(m_endPoint6);
	    m_endPoint6 = null;
	}
    }

    /**
     * @brief Send a packet
     * @param p packet
     * @returns 0 on success, -1 on failure
     */
    private int DoSend(Packet p) {
	if ((m_endPoint == null) && (Ipv4Address.IsMatchingType(m_defaultAddress) == true)) {
	    if (Bind() == -1) {
		assert (m_endPoint == null);
		return -1;
	    }
	    assert (m_endPoint != null);
	} else if ((m_endPoint6 == null) && (Ipv6Address.IsMatchingType(m_defaultAddress) == true)) {
	    if (Bind6() == -1) {
		assert (m_endPoint6 == null);
		return -1;
	    }
	    assert (m_endPoint6 != null);
	}

	if (m_shutdownSend) {
	    m_errno = SocketErrno.ERROR_SHUTDOWN;
	    return -1;
	}

	if (Ipv4Address.IsMatchingType(m_defaultAddress)) {
	    return DoSendTo(p, Ipv4Address.ConvertFrom(m_defaultAddress), m_defaultPort, GetIpTos());
	} else if (Ipv6Address.IsMatchingType(m_defaultAddress)) {
	    return DoSendTo(p, Ipv6Address.ConvertFrom(m_defaultAddress), m_defaultPort);
	}

	m_errno = SocketErrno.ERROR_AFNOSUPPORT;
	return (-1);
    }
    /**
     * @brief Send a packet to a specific destination and port (IPv4)
     * @param p packet
     * @param daddr destination address
     * @param dport destination port
     * @param tos ToS
     * @returns 0 on success, -1 on failure
     */
    private int DoSendTo(Packet p, Ipv4Address dest, short port, byte tos) {
	if (m_boundnetdevice != null) {
	    MyLog.logInfo("Bound interface number " + m_boundnetdevice.GetIfIndex());
	}
	if (m_endPoint == null) {
	    if (Bind() == -1) {
		assert (m_endPoint == null);
		return -1;
	    }
	    assert (m_endPoint != null);
	}
	if (m_shutdownSend) {
	    m_errno = SocketErrno.ERROR_SHUTDOWN;
	    return -1;
	}

	if (p.GetSize() > GetTxAvailable()) {
	    m_errno = SocketErrno.ERROR_MSGSIZE;
	    return -1;
	}

	byte priority = GetPriority();
	if (tos != (byte) 0) {
	    SocketIpTosTag ipTosTag = new SocketIpTosTag();
	    ipTosTag.SetTos(tos);
	    // This packet may already have a SocketIpTosTag (see BUG 2440)
	    p.ReplacePacketTag(ipTosTag);
	    priority = IpTos2Priority(tos);
	}

	if (priority != (byte) 0) {
	    SocketPriorityTag priorityTag = new SocketPriorityTag();
	    priorityTag.SetPriority(priority);
	    p.ReplacePacketTag(priorityTag);
	}

	// TODO:
	// Ipv4 ipv4 = m_node->GetObject<Ipv4>();
	Ipv4 ipv4 = null;

	// Locally override the IP TTL for this socket
	// We cannot directly modify the TTL at this stage, so we set a Packet tag
	// The destination can be either multicast, unicast/anycast, or
	// either all-hosts broadcast or limited (subnet-directed) broadcast.
	// For the latter two broadcast types, the TTL will later be set to one
	// irrespective of what is set in these socket options. So, this tagging
	// may end up setting the TTL of a limited broadcast packet to be
	// the same as a unicast, but it will be fixed further down the stack
	if (m_ipMulticastTtl != (byte) 0 && dest.IsMulticast()) {
	    SocketIpTtlTag tag = new SocketIpTtlTag();
	    tag.SetTtl(m_ipMulticastTtl);
	    p.AddPacketTag(tag);
	} else if (IsManualIpTtl() && GetIpTtl() != (byte) 0 && !dest.IsMulticast() && !dest.IsBroadcast()) {
	    SocketIpTtlTag tag = new SocketIpTtlTag();
	    tag.SetTtl(GetIpTtl());
	    p.AddPacketTag(tag);
	}
	{
	    SocketSetDontFragmentTag tag = new SocketSetDontFragmentTag();
	    boolean found = p.RemovePacketTag(tag);
	    if (!found) {
		if (m_mtuDiscover) {
		    tag.Enable();
		} else {
		    tag.Disable();
		}
		p.AddPacketTag(tag);
	    }
	}

	// Note that some systems will only send limited broadcast packets
	// out of the "default" interface; here we send it out all interfaces
	if (dest.IsBroadcast()) {
	    if (!m_allowBroadcast) {
		m_errno = SocketErrno.ERROR_OPNOTSUPP;
		return -1;
	    }
	    MyLog.logInfo("Limited broadcast start.");
	    for (int i = 0; i < ipv4.GetNInterfaces(); i++) {
		// Get the primary address
		Ipv4InterfaceAddress iaddr = ipv4.GetAddress(i, 0);
		Ipv4Address addri = iaddr.GetLocal();
		if (addri.equals(new Ipv4Address("127.0.0.1"))) {
		    continue;
		}
		// Check if interface-bound socket
		if (m_boundnetdevice != null) {
		    if (!ipv4.GetNetDevice(i).equals(m_boundnetdevice)) {
			continue;
		    }
		}
		MyLog.logInfo("Sending one copy from " + addri + " to " + dest);
		m_udp.Send(p.Copy(), addri, dest, m_endPoint.GetLocalPort(), port);
		NotifyDataSent(p.GetSize());
		NotifySend(GetTxAvailable());
	    }
	    MyLog.logInfo("Limited broadcast end.");
	    return p.GetSize();
	} else if (!m_endPoint.GetLocalAddress().equals(Ipv4Address.GetAny())) {
	    m_udp.Send(p.Copy(), m_endPoint.GetLocalAddress(), dest, m_endPoint.GetLocalPort(), port, null);
	    NotifyDataSent(p.GetSize());
	    NotifySend(GetTxAvailable());
	    return p.GetSize();
	} else if (ipv4.GetRoutingProtocol() != null) {
	    Ipv4Header header = new Ipv4Header();
	    header.SetDestination(dest);
	    header.SetProtocol(UdpL4Protocol.PROT_NUMBER);
	    SocketErrno errno_ = SocketErrno.ERROR_NOTERROR;
	    Ipv4Route route;
	    NetDevice oif = m_boundnetdevice; // specify non-zero if bound to a specific device
	    // TBD-- we could cache the route and just check its validity
	    route = ipv4.GetRoutingProtocol().RouteOutput(p, header, oif, errno_);
	    if (route != null) {
		MyLog.logInfo("Route exists");
		if (!m_allowBroadcast) {
		    // Here we try to route subnet-directed broadcasts
		    int outputIfIndex = ipv4.GetInterfaceForDevice(route.GetOutputDevice());
		    int ifNAddr = ipv4.GetNAddresses(outputIfIndex);
		    for (int addrI = 0; addrI < ifNAddr; ++addrI) {
			Ipv4InterfaceAddress ifAddr = ipv4.GetAddress(outputIfIndex, addrI);
			if (dest == ifAddr.GetBroadcast()) {
			    m_errno = SocketErrno.ERROR_OPNOTSUPP;
			    return -1;
			}
		    }
		}

		header.SetSource(route.GetSource());
		m_udp.Send(p.Copy(), header.GetSource(), header.GetDestination(), m_endPoint.GetLocalPort(), port,
			route);
		NotifyDataSent(p.GetSize());
		return p.GetSize();
	    } else {
		MyLog.logInfo("No route to destination");
		MyLog.logOut("" + errno_, MyLog.ERROR);
		m_errno = errno_;
		return -1;
	    }
	} else {
	    MyLog.logOut("ERROR_NOROUTETOHOST", MyLog.ERROR);
	    m_errno = SocketErrno.ERROR_NOROUTETOHOST;
	    return -1;
	}
    }
    /**
     * @brief Send a packet to a specific destination and port (IPv6)
     * @param p packet
     * @param daddr destination address
     * @param dport destination port
     * @returns 0 on success, -1 on failure
     */
    private int DoSendTo(Packet p, Ipv6Address daddr, short dport) {
	// TODO:
	assert false : "Not implement yet";
	return 0;
    }

    /**
     * @brief Called by the L3 protocol when it received an ICMP packet to pass on to TCP.
     *
     * @param icmpSource the ICMP source address
     * @param icmpTtl the ICMP Time to Live
     * @param icmpType the ICMP Type
     * @param icmpCode the ICMP Code
     * @param icmpInfo the ICMP Info
     */
    private void ForwardIcmp(Ipv4Address icmpSource,
	    byte icmpTtl, byte icmpType, byte icmpCode, int icmpInfo) {
	if (m_icmpCallback != null) {
	    m_icmpCallback.callback(icmpSource, icmpTtl, icmpType, icmpCode, icmpInfo);
	}
    }

    /**
     * @brief Called by the L3 protocol when it received an ICMPv6 packet to pass on to TCP.
     *
     * @param icmpSource the ICMP source address
     * @param icmpTtl the ICMP Time to Live
     * @param icmpType the ICMP Type
     * @param icmpCode the ICMP Code
     * @param icmpInfo the ICMP Info
     */
    private void ForwardIcmp6(Ipv6Address icmpSource,
	    byte icmpTtl, byte icmpType, byte icmpCode, int icmpInfo) {
	if (m_icmpCallback6 != null) {
	    m_icmpCallback6.callback(icmpSource, icmpTtl, icmpType, icmpCode, icmpInfo);
	}
    }

    // Connections to other layers of TCP/IP
    private Ipv4EndPoint m_endPoint; // !< the IPv4 endpoint
    private Ipv6EndPoint m_endPoint6; // !< the IPv6 endpoint
    private Node m_node;          //!< the associated node
    private UdpL4Protocol m_udp;  //!< the associated UDP L4 protocol
    private Callback5<Ipv4Address, Byte, Byte, Byte, Integer> m_icmpCallback = null; //!< ICMP callback
    private Callback5<Ipv6Address, Byte, Byte, Byte, Integer> m_icmpCallback6 = null; // !< ICMPv6 callback

    private Address m_defaultAddress; // !< Default address
    private short m_defaultPort; // !< Default port
    // private TracedCallback<Packet> m_dropTrace; //!< Trace for dropped packets

    private SocketErrno m_errno; // !< Socket error code
    private boolean m_shutdownSend; // !< Send no longer allowed
    private boolean m_shutdownRecv; // !< Receive no longer allowed
    private boolean m_connected; // !< Connection established
    private boolean m_allowBroadcast; // !< Allow send broadcast packets

    private Queue<Pair<Packet, Address>> m_deliveryQueue; // !< Queue for incoming packets
    private int m_rxAvailable; // !< Number of available bytes to be received

    // Socket attributes
    private int m_rcvBufSize; // !< Receive buffer size
    private byte m_ipMulticastTtl; // !< Multicast TTL
    private int m_ipMulticastIf; // !< Multicast Interface
    private boolean m_ipMulticastLoop; // !< Allow multicast loop
    private boolean m_mtuDiscover; // !< Allow MTU discovery

}
