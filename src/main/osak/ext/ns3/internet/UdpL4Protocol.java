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

import java.util.List;

import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Node;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.Socket;
import osak.ext.ns3.network.utils.Ipv4Address;
import osak.ext.ns3.network.utils.Ipv6Address;

/**
 * Implementation of the UDP protocol
 * <p>
 * 
 * This is an implementation of the User Datagram Protocol described in
 * \RFC{768}. It implements a connectionless, unreliable datagram packet
 * service. Packets may be reordered or duplicated before they arrive. UDP
 * generates and checks checksums to catch transmission errors.
 * <p>
 * The following options are not presently part of this implementation:
 * UDP_CORK, MSG_DONTROUTE, path MTU discovery control (e.g. IP_MTU_DISCOVER).
 * MTU handling is also weak in ns-3 for the moment; it is best to send
 * datagrams that do not exceed 1500 byte MTU (e.g. 1472 byte UDP datagrams)
 * 
 * @author zhangrui
 * @since 1.0
 */
public class UdpL4Protocol extends IpL4Protocol {
    public static final byte PROT_NUMBER = 0x11;

    public UdpL4Protocol() {

    }

    // Delete copy constructor and assignment operator to avoid misuse
    // UdpL4Protocol(const UdpL4Protocol&) = delete;
    // UdpL4Protocol& operator=(const UdpL4Protocol&) = delete;

    /**
     * Set node associated with this stack
     * 
     * @param node the node
     */
    public void SetNode(Node node) {

    }

    /**
     * @return A smart Socket pointer to a UdpSocket, allocated by this instance of
     *         the UDP protocol
     */
    public Socket CreateSocket() {
	return null;

    }

    /**
     * @brief Allocate an IPv4 Endpoint
     * @return the Endpoint
     */
    public Ipv4EndPoint Allocate() {
	return null;

    }

    /**
     * @brief Allocate an IPv4 Endpoint
     * @param address address to use
     * @return the Endpoint
     */
    public Ipv4EndPoint Allocate(Ipv4Address address) {
	return null;

    }

    /**
     * @brief Allocate an IPv4 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param port           port to use
     * @return the Endpoint
     */
    public Ipv4EndPoint Allocate(NetDevice boundNetDevice, short port) {
	return null;

    }

    /**
     * @brief Allocate an IPv4 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param address        address to use
     * @param port           port to use
     * @return the Endpoint
     */
    public Ipv4EndPoint Allocate(NetDevice boundNetDevice, Ipv4Address address, short port) {
	return null;

    }

    /**
     * @brief Allocate an IPv4 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param localAddress   local address to use
     * @param localPort      local port to use
     * @param peerAddress    remote address to use
     * @param peerPort       remote port to use
     * @return the Endpoint
     */
    public Ipv4EndPoint Allocate(NetDevice boundNetDevice, Ipv4Address localAddress, short localPort,
	    Ipv4Address peerAddress, short peerPort) {
	return null;

    }

    /**
     * @brief Allocate an IPv6 Endpoint
     * @return the Endpoint
     */
    public Ipv6EndPoint Allocate6() {
	return null;

    }

    /**
     * @brief Allocate an IPv6 Endpoint
     * @param address address to use
     * @return the Endpoint
     */
    public Ipv6EndPoint Allocate6(Ipv6Address address) {
	return null;

    }

    /**
     * @brief Allocate an IPv6 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param port           port to use
     * @return the Endpoint
     */
    public Ipv6EndPoint Allocate6(NetDevice boundNetDevice, short port) {
	return null;

    }

    /**
     * @brief Allocate an IPv6 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param address        address to use
     * @param port           port to use
     * @return the Endpoint
     */
    public Ipv6EndPoint Allocate6(NetDevice boundNetDevice, Ipv6Address address, short port) {
	return null;

    }

    /**
     * @brief Allocate an IPv6 Endpoint
     * @param boundNetDevice Bound NetDevice (if any)
     * @param localAddress   local address to use
     * @param localPort      local port to use
     * @param peerAddress    remote address to use
     * @param peerPort       remote port to use
     * @return the Endpoint
     */
    public Ipv6EndPoint Allocate6(NetDevice boundNetDevice, Ipv6Address localAddress, short localPort,
	    Ipv6Address peerAddress, short peerPort) {
	return null;

    }

    /**
     * @brief Remove an IPv4 Endpoint.
     * @param endPoint the end point to remove
     */
    public void DeAllocate(Ipv4EndPoint endPoint) {

    }

    /**
     * @brief Remove an IPv6 Endpoint.
     * @param endPoint the end point to remove
     */
    public void DeAllocate(Ipv6EndPoint endPoint) {

    }

    // called by UdpSocket.
    /**
     * @brief Send a packet via UDP (IPv4)
     * @param packet The packet to send
     * @param saddr  The source Ipv4Address
     * @param daddr  The destination Ipv4Address
     * @param sport  The source port number
     * @param dport  The destination port number
     */
    public void Send(Packet packet, Ipv4Address saddr, Ipv4Address daddr, short sport, short dport) {

    }

    /**
     * @brief Send a packet via UDP (IPv4)
     * @param packet The packet to send
     * @param saddr  The source Ipv4Address
     * @param daddr  The destination Ipv4Address
     * @param sport  The source port number
     * @param dport  The destination port number
     * @param route  The route
     */
    public void Send(Packet packet, Ipv4Address saddr, Ipv4Address daddr, short sport, short dport, Ipv4Route route) {

    }

    /**
     * @brief Send a packet via UDP (IPv6)
     * @param packet The packet to send
     * @param saddr  The source Ipv4Address
     * @param daddr  The destination Ipv4Address
     * @param sport  The source port number
     * @param dport  The destination port number
     */
    public void Send(Packet packet, Ipv6Address saddr, Ipv6Address daddr, short sport, short dport) {

    }

    /**
     * @brief Send a packet via UDP (IPv6)
     * @param packet The packet to send
     * @param saddr  The source Ipv4Address
     * @param daddr  The destination Ipv4Address
     * @param sport  The source port number
     * @param dport  The destination port number
     * @param route  The route
     */
    public void Send(Packet packet, Ipv6Address saddr, Ipv6Address daddr, short sport, short dport, Ipv6Route route) {

    }

    @Override
    public int GetProtocolNumber() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public RxStatus Receive(Packet p, Ipv4Header header, Ipv4Interface incomingInterface) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public RxStatus Receive(Packet p, Ipv6Header header, Ipv6Interface incomingInterface) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void ReceiveIcmp(Ipv4Address icmpSource, byte icmpTtl, byte icmpType, byte icmpCode, int icmpInfo,
	    Ipv4Address payloadSource, Ipv4Address payloadDestination, byte[] payload) {
	// TODO Auto-generated method stub

    }

    @Override
    public void ReceiveIcmp(Ipv6Address icmpSource, byte icmpTtl, byte icmpType, byte icmpCode, int icmpInfo,
	    Ipv6Address payloadSource, Ipv6Address payloadDestination, byte[] payload) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SetDownTarget(DownTargetCallback cb) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SetDownTarget6(DownTargetCallback6 cb) {
	// TODO Auto-generated method stub

    }

    @Override
    public DownTargetCallback GetDownTarget() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public DownTargetCallback6 GetDownTarget6() {
	// TODO Auto-generated method stub
	return null;
    }

    private Node m_node; // !< the node this stack is associated with
    private Ipv4EndPointDemux m_endPoints; // !< A list of IPv4 end points.
    private Ipv6EndPointDemux m_endPoints6; // !< A list of IPv6 end points.

    private List<UdpSocketImpl> m_sockets; // !< list of sockets
    private DownTargetCallback m_downTarget; // !< Callback to send packets over IPv4
    private DownTargetCallback6 m_downTarget6; // !< Callback to send packets over IPv6

}
