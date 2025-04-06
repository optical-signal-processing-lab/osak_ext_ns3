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

import osak.ext.ns3.callback.Callback0;
import osak.ext.ns3.callback.Callback4;
import osak.ext.ns3.callback.Callback5;
import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4EndPoint
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4EndPoint {
    /**
     * @brief Constructor.
     * @param address the IPv4 address
     * @param port    the port
     */
    public Ipv4EndPoint(Ipv4Address address, short port) {
	m_localAddr = address;
	m_localPort = port;
	m_peerAddr = Ipv4Address.GetAny();
	m_peerPort = 0;
	m_rxEnabled = true;
    }

    /**
     * @brief Get the local address.
     * @return the local address
     */
    public Ipv4Address GetLocalAddress() {
	return m_localAddr;
    }

    /**
     * @brief Set the local address.
     * @param address the address to set
     */
    public void SetLocalAddress(Ipv4Address address) {
	m_localAddr = address;
    }

    /**
     * @brief Get the local port.
     * @return the local port
     */
    public short GetLocalPort() {
	return m_localPort;
    }

    /**
     * @brief Get the peer address.
     * @return the peer address
     */
    public Ipv4Address GetPeerAddress() {
	return m_peerAddr;
    }

    /**
     * @brief Get the peer port.
     * @return the peer port
     */
    public short GetPeerPort() {
	return m_peerPort;
    }

    /**
     * @brief Set the peer information (address and port).
     * @param address peer address
     * @param port    peer port
     */
    public void SetPeer(Ipv4Address address, short port) {
	m_peerAddr = address;
	m_peerPort = port;
    }

    /**
     * @brief Bind a socket to specific device.
     *
     *        This method corresponds to using setsockopt() SO_BINDTODEVICE of real
     *        network or BSD sockets. If set on a socket, this option will force
     *        packets to leave the bound device regardless of the device that IP
     *        routing would naturally choose. In the receive direction, only packets
     *        received from the bound interface will be delivered.
     *
     *        This option has no particular relationship to binding sockets to an
     *        address via Socket::Bind (). It is possible to bind sockets to a
     *        specific IP address on the bound interface by calling both
     *        Socket::Bind (address) and Socket::BindToNetDevice (device), but it is
     *        also possible to bind to mismatching device and address, even if the
     *        socket can not receive any packets as a result.
     *
     * @param netdevice Pointer to Netdevice of desired interface
     */
    public void BindToNetDevice(NetDevice netdevice) {
	m_boundnetdevice = netdevice;
    }

    /**
     * @brief Returns socket's bound netdevice, if any.
     *
     *        This method corresponds to using getsockopt() SO_BINDTODEVICE of real
     *        network or BSD sockets.
     *
     *
     * @returns Pointer to interface.
     */
    public NetDevice GetBoundNetDevice() {
	return m_boundnetdevice;

    }

    // Called from socket implementations to get notified about important events.
    /**
     * @brief Set the reception callback.
     * @param callback callback function
     */
    public void SetRxCallback(Callback4<Packet, Ipv4Header, Short, Ipv4Interface> callback) {
	m_rxCallback = callback;
    }

    /**
     * @brief Set the ICMP callback.
     * @param callback callback function
     */
    public void SetIcmpCallback(Callback5<Ipv4Address, Byte, Byte, Byte, Integer> callback) {
	m_icmpCallback = callback;
    }

    /**
     * @brief Set the default destroy callback.
     * @param callback callback function
     */
    public void SetDestroyCallback(Callback0 callback) {
	m_destroyCallback = callback;
    }

    /**
     * @brief Forward the packet to the upper level.
     *
     *        Called from an L4Protocol implementation to notify an endpoint of a
     *        packet reception.
     * @param p                 the packet
     * @param header            the packet header
     * @param sport             source port
     * @param incomingInterface incoming interface
     */
    public void ForwardUp(Packet p, final Ipv4Header header, short sport, Ipv4Interface incomingInterface) {
	if (m_rxCallback != null)
	{
	    m_rxCallback.callback(p, header, sport, incomingInterface);
	}
    }

    /**
     * @brief Forward the ICMP packet to the upper level.
     *
     *        Called from an L4Protocol implementation to notify an endpoint of an
     *        icmp message reception.
     *
     * @param icmpSource source IP address
     * @param icmpTtl    time-to-live
     * @param icmpType   ICMP type
     * @param icmpCode   ICMP code
     * @param icmpInfo   ICMP info
     */
    public void ForwardIcmp(Ipv4Address icmpSource, byte icmpTtl, byte icmpType, byte icmpCode, int icmpInfo) {
	if (m_icmpCallback != null) {
	    m_icmpCallback.callback(icmpSource, icmpTtl, icmpType, icmpCode, icmpInfo);
	}
    }

    /**
     * @brief Enable or Disable the endpoint Rx capability.
     * @param enabled true if Rx is enabled
     */
    public void SetRxEnabled(boolean enabled) {
	m_rxEnabled = enabled;
    }

    /**
     * @brief Checks if the endpoint can receive packets.
     * @returns true if the endpoint can receive packets.
     */
    public boolean IsRxEnabled() {
	return m_rxEnabled;

    }

    /**
     * @brief The local address.
     */
    private Ipv4Address m_localAddr = new Ipv4Address();

    /**
     * @brief The local port.
     */
    private short m_localPort;

    /**
     * @brief The peer address.
     */
    private Ipv4Address m_peerAddr = new Ipv4Address();

    /**
     * @brief The peer port.
     */
    private short m_peerPort;

    /**
     * @brief The NetDevice the EndPoint is bound to (if any).
     */
    private NetDevice m_boundnetdevice;

    /**
     * @brief The RX callback.
     */
    private Callback4<Packet, Ipv4Header, Short, Ipv4Interface> m_rxCallback = null;

    /**
     * @brief The ICMPv6 callback.
     */
    private Callback5<Ipv4Address, Byte, Byte, Byte, Integer> m_icmpCallback = null;

    /**
     * @brief The destroy callback.
     */
    private Callback0 m_destroyCallback = null;

    /**
     * @brief true if the endpoint can receive packets.
     */
    private boolean m_rxEnabled;

}
