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

import osak.ext.ns3.callback.ErrorCallback;
import osak.ext.ns3.callback.LocalDeliverCallback;
import osak.ext.ns3.callback.MulticastForwardCallback;
import osak.ext.ns3.callback.UnicastForwardCallback;
import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.SocketErrno;

/**
 * TODO Ipv4RoutingProtocol
 * 
 * @author zhangrui
 * @since   1.0
 */
public interface Ipv4RoutingProtocol {
    /**
     * @brief Query routing cache for an existing route, for an outbound packet
     *
     * This lookup is used by transport protocols.  It does not cause any
     * packet to be forwarded, and is synchronous.  Can be used for
     * multicast or unicast.  The Linux equivalent is ip_route_output()
     *
     * The header input parameter may have an uninitialized value
     * for the source address, but the destination address should always be
     * properly set by the caller.
     *
     * @param p packet to be routed.  Note that this method may modify the packet.
     *          Callers may also pass in a null pointer.
     * @param header input parameter (used to form key to search for the route)
     * @param oif Output interface Netdevice.  May be zero, or may be bound via
     *            socket options to a particular output interface.
     * @param sockerr Output parameter; socket errno
     *
     * @returns a code that indicates what happened in the lookup
     */
    Ipv4Route RouteOutput(Packet p, Ipv4Header header, NetDevice oif, SocketErrno sockerr);

    /**
     * @brief Route an input packet (to be forwarded or locally delivered)
     *
     * This lookup is used in the forwarding process.  The packet is
     * handed over to the Ipv4RoutingProtocol, and will get forwarded onward
     * by one of the callbacks.  The Linux equivalent is ip_route_input().
     * There are four valid outcomes, and a matching callbacks to handle each.
     *
     * @param p received packet
     * @param header input parameter used to form a search key for a route
     * @param idev Pointer to ingress network device
     * @param ucb Callback for the case in which the packet is to be forwarded
     *            as unicast
     * @param mcb Callback for the case in which the packet is to be forwarded
     *            as multicast
     * @param lcb Callback for the case in which the packet is to be locally
     *            delivered
     * @param ecb Callback to call if there is an error in forwarding
     * @returns true if the Ipv4RoutingProtocol takes responsibility for
     *          forwarding or delivering the packet, false otherwise
     */
    boolean RouteInput(final Packet p, final Ipv4Header header, final NetDevice idev, UnicastForwardCallback ucb,
	    MulticastForwardCallback mcb, LocalDeliverCallback lcb, ErrorCallback ecb);

    /**
     * @param interface the index of the interface we are being notified about
     *
     * Protocols are expected to implement this method to be notified of the state change of
     * an interface in a node.
     */
    void NotifyInterfaceUp(int iface);
    /**
     * @param interface the index of the interface we are being notified about
     *
     * Protocols are expected to implement this method to be notified of the state change of
     * an interface in a node.
     */
    void NotifyInterfaceDown(int iface);

    /**
     * @param interface the index of the interface we are being notified about
     * @param address a new address being added to an interface
     *
     * Protocols are expected to implement this method to be notified whenever
     * a new address is added to an interface. Typically used to add a 'network route' on an
     * interface. Can be invoked on an up or down interface.
     */
    void NotifyAddAddress(int iface, Ipv4InterfaceAddress address);

    /**
     * @param interface the index of the interface we are being notified about
     * @param address a new address being added to an interface
     *
     * Protocols are expected to implement this method to be notified whenever
     * a new address is removed from an interface. Typically used to remove the 'network route' of
     * an interface. Can be invoked on an up or down interface.
     */
    void NotifyRemoveAddress(int iface, Ipv4InterfaceAddress address);

    /**
     * @param ipv4 the ipv4 object this routing protocol is being associated with
     *
     * Typically, invoked directly or indirectly from osak.ext.ns3::Ipv4::SetRoutingProtocol
     */
    void SetIpv4(Ipv4 ipv4);

    /**
     * @brief Print the Routing Table entries
     *
     * @param stream The ostream the Routing table is printed to
     * @param unit The time unit to be used in the report
     */
//    void PrintRoutingTable(OutputStreamWrapper stream, TimeUnit unit = Time::S);
}
