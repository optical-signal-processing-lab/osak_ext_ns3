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

import java.net.Socket;

import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4
 * 
 * @author zhangrui
 * @since   1.0
 */
public interface Ipv4 {
    /**
     * Register a new routing protocol to be used by this Ipv4 stack
     *
     * This call will replace any routing protocol that has been previously
     * registered. If you want to add multiple routing protocols, you must add them
     * to a Ipv4ListRoutingProtocol directly.
     *
     * @param routingProtocol smart pointer to Ipv4RoutingProtocol object
     */
    public void SetRoutingProtocol(Ipv4RoutingProtocol routingProtocol);

    /**
     * Get the routing protocol to be used by this Ipv4 stack
     *
     * @returns smart pointer to Ipv4RoutingProtocol object, or null pointer if none
     */
    public Ipv4RoutingProtocol GetRoutingProtocol();

    /**
     * @param device device to add to the list of Ipv4 interfaces which can be used
     *               as output interfaces during packet forwarding.
     * @returns the index of the Ipv4 interface added.
     *
     *          Once a device has been added, it can never be removed: if you want
     *          to disable it, you can invoke Ipv4::SetDown which will make sure
     *          that it is never used during packet forwarding.
     */
    public int AddInterface(NetDevice device);

    /**
     * @returns the number of interfaces added by the user.
     */
    public int GetNInterfaces();

    /**
     * Return the interface number of the interface that has been assigned the
     * specified IP address.
     *
     * @param address The IP address being searched for
     * @returns The interface number of the Ipv4 interface with the given address or
     *          -1 if not found.
     *
     *          Each IP interface has one or more IP addresses associated with it.
     *          This method searches the list of interfaces for one that holds a
     *          particular address. This call takes an IP address as a parameter and
     *          returns the interface number of the first interface that has been
     *          assigned that address, or -1 if not found. There must be an exact
     *          match; this method will not match broadcast or multicast addresses.
     */
    public int GetInterfaceForAddress(Ipv4Address address);

    /**
     * @param packet      packet to send
     * @param source      source address of packet
     * @param destination address of packet
     * @param protocol    number of packet
     * @param route       route entry
     *
     *                    Higher-level layers call this method to send a packet down
     *                    the stack to the MAC and PHY layers.
     */
    public void Send(Packet packet, Ipv4Address source, Ipv4Address destination, byte protocol, Ipv4Route route);

    /**
     * @param packet   packet to send
     * @param ipHeader IP Header
     * @param route    route entry
     *
     *                 Higher-level layers call this method to send a packet with
     *                 IPv4 Header (Intend to be used with IpHeaderInclude
     *                 attribute.)
     */
    public void SendWithHeader(Packet packet, Ipv4Header ipHeader, Ipv4Route route);

    /**
     * @param protocol a template for the protocol to add to this L4 Demux.
     *
     *                 Invoke Copy on the input template to get a copy of the input
     *                 protocol which can be used on the Node on which this L4 Demux
     *                 is running. The new L4Protocol is registered internally as a
     *                 working L4 Protocol and returned from this method. The caller
     *                 does not get ownership of the returned pointer.
     */
    public void Insert(IpL4Protocol protocol);

    /**
     * Add a L4 protocol to a specific interface.
     *
     * This may be called multiple times for multiple interfaces for the same
     * protocol. To insert for all interfaces, use the separate Insert
     * (Ptr<IpL4Protocol> protocol) method.
     *
     * Setting a protocol on a specific interface will overwrite the previously
     * bound protocol.
     *
     * @param protocol       L4 protocol.
     * @param interfaceIndex interface index.
     */
    public void Insert(IpL4Protocol protocol, int ifaceIndex);

    /**
     * @param protocol protocol to remove from this demux.
     *
     *                 The input value to this method should be the value returned
     *                 from the Ipv4L4Protocol::Insert method.
     */
    public void Remove(IpL4Protocol protocol);

    /**
     * Remove a L4 protocol from a specific interface.
     * 
     * @param protocol       L4 protocol to remove.
     * @param interfaceIndex interface index.
     */
    public void Remove(IpL4Protocol protocol, int interfaceIndex);

    /**
     * Determine whether address and interface corresponding to received packet can
     * be accepted for local delivery
     *
     * @param address The IP address being considered
     * @param iif     The incoming Ipv4 interface index
     * @returns true if the address is associated with the interface index
     *
     *          This method can be used to determine whether a received packet has
     *          an acceptable address for local delivery on the host. The address
     *          may be a unicast, multicast, or broadcast address. This method will
     *          return true if address is an exact match of a unicast address on one
     *          of the host's interfaces (see below), if address corresponds to a
     *          multicast group that the host has joined (and the incoming device is
     *          acceptable), or if address corresponds to a broadcast address.
     *
     *          If the Ipv4 attribute WeakEsModel is true, the unicast address may
     *          match any of the Ipv4 addresses on any interface. If the attribute
     *          is false, the address must match one assigned to the incoming
     *          device.
     */
    public boolean IsDestinationAddress(Ipv4Address address, int iif);

    /**
     * Return the interface number of first interface found that has an Ipv4 address
     * within the prefix specified by the input address and mask parameters
     *
     * @param address The IP address assigned to the interface of interest.
     * @param mask    The IP prefix to use in the mask
     * @returns The interface number of the Ipv4 interface with the given address or
     *          -1 if not found.
     *
     *          Each IP interface has one or more IP addresses associated with it.
     *          This method searches the list of interfaces for the first one found
     *          that holds an address that is included within the prefix formed by
     *          the input address and mask parameters. The value -1 is returned if
     *          no match is found.
     */
    public int GetInterfaceForPrefix(Ipv4Address address, Ipv4Mask mask);

    /**
     * @param interface The interface number of an Ipv4 interface.
     * @returns The NetDevice associated with the Ipv4 interface number.
     */
    public NetDevice GetNetDevice(int iface);

    /**
     * @param device The NetDevice for an Ipv4Interface
     * @returns The interface number of an Ipv4 interface or -1 if not found.
     */
    public int GetInterfaceForDevice(NetDevice device);

    /**
     * @param interface Interface number of an Ipv4 interface
     * @param address   Ipv4InterfaceAddress address to associate with the
     *                  underlying Ipv4 interface
     * @returns true if the operation succeeded
     */
    public boolean AddAddress(int iface, Ipv4InterfaceAddress address);

    /**
     * @param interface Interface number of an Ipv4 interface
     * @returns the number of Ipv4InterfaceAddress entries for the interface.
     */
    public int GetNAddresses(int iface);

    /**
     * Because addresses can be removed, the addressIndex is not guaranteed to be
     * static across calls to this method.
     *
     * @param interface    Interface number of an Ipv4 interface
     * @param addressIndex index of Ipv4InterfaceAddress
     * @returns the Ipv4InterfaceAddress associated to the interface and
     *          addressIndex
     */
    public Ipv4InterfaceAddress GetAddress(int iface, int addressIndex);

    /**
     * Remove the address at addressIndex on named interface. The addressIndex for
     * all higher indices will decrement by one after this method is called; so, for
     * example, to remove 5 addresses from an interface i, one could call
     * RemoveAddress (i, 0); 5 times.
     *
     * @param interface    Interface number of an Ipv4 interface
     * @param addressIndex index of Ipv4InterfaceAddress to remove
     * @returns true if the operation succeeded
     */
    public boolean RemoveAddress(int iface, int addressIndex);

    /**
     * Remove the given address on named Ipv4 interface
     *
     * @param interface Interface number of an Ipv4 interface
     * @param address   The address to remove
     * @returns true if the operation succeeded
     */
    public boolean RemoveAddress(int iface, Ipv4Address address);

    /**
     *  Return the first primary source address with scope less than
     * or equal to the requested scope, to use in sending a packet to
     * destination dst out of the specified device.
     *
     * This method mirrors the behavior of Linux inet_select_addr() and is
     * provided because interfaces may have multiple IP addresses configured
     * on them with different scopes, and with a primary and secondary status.
     * Secondary addresses are never returned.
     * \see Ipv4InterfaceAddress
     *
     * If a non-zero device pointer is provided, the method first tries to
     * return a primary address that is configured on that device, and whose
     * subnet matches that of dst and whose scope is less than or equal to
     * the requested scope.  If a primary address does not match the
     * subnet of dst but otherwise matches the scope, it is returned.
     * If no such address on the device is found, the other devices are
     * searched in order of their interface index, but not considering dst
     * as a factor in the search.  Because a loopback interface is typically
     * the first one configured on a node, it will be the first alternate
     * device to be tried.  Addresses scoped at LINK scope are not returned
     * in this phase.
     *
     * If no device pointer is provided, the same logic as above applies, only
     * that there is no preferred device that is consulted first.  This means
     * that if the device pointer is null, input parameter dst will be ignored.
     *
     * If there are no possible addresses to return, a warning log message
     * is issued and the all-zeroes address is returned.
     *
     * @param device output NetDevice (optionally provided, only to constrain the search)
     * @param dst Destination address to match, if device is provided
     * @param scope Scope of returned address must be less than or equal to this
     * @returns the first primary Ipv4Address that meets the search criteria
     */
    public Ipv4Address SelectSourceAddress(
        NetDevice device,
	    Ipv4Address dst, Ipv4InterfaceAddress.InterfaceAddressScope_e scope);

    /**
     * @param interface The interface number of an Ipv4 interface
     * @param metric    routing metric (cost) associated to the underlying Ipv4
     *                  interface
     */
    public void SetMetric(int iface, short metric);

    /**
     * @param interface The interface number of an Ipv4 interface
     * @returns routing metric (cost) associated to the underlying Ipv4 interface
     */
    public short GetMetric(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     * @returns the Maximum Transmission Unit (in bytes) associated to the
     *          underlying Ipv4 interface
     */
    public short GetMtu(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     * @returns true if the underlying interface is in the "up" state, false
     *          otherwise.
     */
    public boolean IsUp(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     *
     *                  Set the interface into the "up" state. In this state, it is
     *                  considered valid during Ipv4 forwarding.
     */
    public void SetUp(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     *
     *                  Set the interface into the "down" state. In this state, it
     *                  is ignored during Ipv4 forwarding.
     */
    public void SetDown(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     * @returns true if IP forwarding enabled for input datagrams on this device
     */
    public boolean IsForwarding(int iface);

    /**
     * @param interface Interface number of Ipv4 interface
     * @param val       Value to set the forwarding flag
     *
     *                  If set to true, IP forwarding is enabled for input datagrams
     *                  on this device
     */
    public void SetForwarding(int iface, boolean val);

    /**
     * Choose the source address to use with destination address.
     * 
     * @param interface interface index
     * @param dest      IPv4 destination address
     * @return IPv4 source address to use
     */
    public Ipv4Address SourceAddressSelection(int iface, Ipv4Address dest);

    /**
     * @param protocolNumber number of protocol to lookup in this L4 Demux
     * @returns a matching L4 Protocol
     *
     *          This method is typically called by lower layers to forward packets
     *          up the stack to the right protocol.
     */
    public IpL4Protocol GetProtocol(int protocolNumber);

    /**
     * Get L4 protocol by protocol number for the specified interface.
     * 
     * @param protocolNumber protocol number
     * @param interfaceIndex interface index, -1 means "any" interface.
     * @return corresponding IpL4Protocol or 0 if not found
     */
    public IpL4Protocol GetProtocol(int protocolNumber, int interfaceIndex);

    /**
     * Creates a raw socket
     *
     * @returns a smart pointer to the instantiated raw socket
     */
    public Socket CreateRawSocket();

    /**
     * Deletes a particular raw socket
     *
     * @param socket Smart pointer to the raw socket to be deleted
     */
    public void DeleteRawSocket(Socket socket);

    public static int IF_ANY = 0xffffffff; // !< interface wildcard, meaning any interface

    // Indirect the Ipv4 attributes through private pure methods

    /**
     * Set or unset the IP forwarding state
     * 
     * @param forward the forwarding state
     */
    public void SetIpForward(boolean forward);

    /**
     * Get the IP forwarding state
     * 
     * @returns true if IP is in forwarding state
     */
    public boolean GetIpForward();

    /**
     * Set or unset the Weak Es Model
     *
     * RFC1122 term for whether host accepts datagram with a dest. address on
     * another interface
     * 
     * @param model true for Weak Es Model
     */
    public void SetWeakEsModel(boolean model);

    /**
     * Get the Weak Es Model status
     *
     * RFC1122 term for whether host accepts datagram with a dest. address on
     * another interface
     * 
     * @returns true for Weak Es Model activated
     */
    public boolean GetWeakEsModel();
}
