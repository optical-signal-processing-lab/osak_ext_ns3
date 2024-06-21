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
package osak.ext.ns3.network;

import java.net.Inet6Address;

import osak.ext.ns3.callback.Callback0;
import osak.ext.ns3.callback.CallbackR4;
import osak.ext.ns3.callback.CallbackR6;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO NetDevice
 * 
 * @author zhangrui
 * @since   1.0
 */

/**
 * netdevice
 * <p>
 * Network layer to device interface
 * 
 * <pre>
 * This interface defines the API which the IP and ARP
 * layers need to access to manage an instance of a network device
 * layer. It currently does not support MAC-level
 * multicast but this should not be too hard to add by adding
 * extra methods to register MAC multicast addresses to
 * filter out unwanted packets before handing them to the
 * higher layers.
 *
 * In Linux, this interface is analogous to the interface
 * just above dev_queue_xmit() (i.e., IP packet is fully
 * constructed with destination MAC address already selected).
 *
 * If you want to write a new MAC layer, you need to subclass
 * this base class and implement your own version of the
 * pure virtual methods in this class.
 *
 * This class was designed to hide as many MAC-level details as
 * possible from the perspective of layer 3 to allow a single layer 3
 * to work with any kind of MAC layer. Specifically, this class
 * encapsulates the specific format of MAC addresses used by a
 * device such that the layer 3 does not need any modification
 * to handle new address formats. This means obviously that the
 * NetDevice class must know about the address format of all potential
 * layer 3 protocols through its GetMulticast methods: the current
 * API has been optimized to make it easy to add new MAC protocols,
 * not to add new layer 3 protocols.
 *
 * Devices aiming to support flow control and dynamic queue limits must perform
 * the following operations:
 *   - in the NotifyNewAggregate method
 *     + cache the pointer to the netdevice queue interface aggregated to the
 *       device
 *     + set the select queue callback through the netdevice queue interface,
 *       if the device is multi-queue
 *   - anytime before initialization
 *     + set the number of device transmission queues (and optionally create them)
 *       through the netdevice queue interface, if the device is multi-queue
 *   - when the device queues have been created, invoke
 *     NetDeviceQueueInterface::ConnectQueueTraces, which
 *     + connects the Enqueue traced callback of the device queues to the
 *       PacketEnqueued static method of the NetDeviceQueue class
 *     + connects the Dequeue and DropAfterDequeue traced callback of the device
 *       queues to the PacketDequeued static method of the NetDeviceQueue
 *       class
 *     + connects the DropBeforeEnqueue traced callback of the device queues to
 *       the PacketDiscarded static method of the NetDeviceQueue class
 * </pre>
 */
public interface NetDevice {
    /**
     * @param index ifIndex of the device
     */
     void SetIfIndex(final int index);
    /**
     * @return index ifIndex of the device
     */
     int GetIfIndex();

    /**
     * @return the channel this NetDevice is connected to. The value
     *         returned can be zero if the NetDevice is not yet connected
     *         to any channel or if the underlying NetDevice has no
     *         concept of a channel. i.e., callers _must_ check for zero
     *         and be ready to handle it.
     */
     Channel GetChannel();

    /**
     * Set the address of this interface
     * @param address address to set
     */
    void SetAddress(Address address);

    /**
     * @return the current Address of this interface.
     */
    Address GetAddress();

    /**
     * @param mtu MTU value, in bytes, to set for the device
     * @return whether the MTU value was within legal bounds
     *
     * Override for default MTU defined on a per-type basis.
     */
     boolean SetMtu(final short mtu);
    /**
     * @return the link-level MTU in bytes for this interface.
     *
     * This value is typically used by the IP layer to perform
     * IP fragmentation when needed.
     */
     short GetMtu();
    /**
     * @return true if link is up; false otherwise
     */
     boolean IsLinkUp();
    /**
     * TracedCallback signature for link changed event.
     */
     interface LinkChangeTracedCallback{
	 void callback();
     }
    //typedef void (*LinkChangeTracedCallback)();
    /**
     * @param callback the callback to invoke
     *
     * Add a callback invoked whenever the link
     * status changes to UP. This callback is typically used
     * by the IP/ARP layer to flush the ARP cache and by IPv6 stack
     * to flush NDISC cache whenever the link goes up.
     */
     void AddLinkChangeCallback(Callback0 callback);
    /**
     * @return true if this interface supports a broadcast address,
     *         false otherwise.
     */
     boolean IsBroadcast();
    /**
     * @return the broadcast address supported by
     *         this netdevice.
     *
     * Calling this method is invalid if IsBroadcast returns
     * not true.
     */
    Address GetBroadcast();

    /**
     * @return value of m_isMulticast flag
     */
     boolean IsMulticast();

    /**
     * @brief Make and return a MAC multicast address using the provided
     *        multicast group
     *
     * \RFC{1112} says that an Ipv4 host group address is mapped to an Ethernet
     * multicast address by placing the low-order 23-bits of the IP address into
     * the low-order 23 bits of the Ethernet multicast address
     * 01-00-5E-00-00-00 (hex).  Similar RFCs exist for Ipv6 and Eui64 mappings.
     * This method performs the multicast address creation function appropriate
     * to the underlying MAC address of the device.  This MAC address is
     * encapsulated in an abstract Address to avoid dependencies on the exact
     * MAC address format.
     *
     * In the case of net devices that do not support
     * multicast, clients are expected to test NetDevice::IsMulticast and avoid
     * attempting to map multicast packets.  Subclasses of NetDevice that do
     * support multicasting are expected to override this method and provide an
     * implementation appropriate to the particular device.
     *
     * @param multicastGroup The IP address for the multicast group destination
     * of the packet.
     * @return The MAC multicast Address used to send packets to the provided
     * multicast group.
     *
     * \warning Calling this method is invalid if IsMulticast returns not true.
     * \see IsMulticast()
     */
    Address GetMulticast(Ipv4Address multicastGroup);

    /**
     * @brief Get the MAC multicast address corresponding
     * to the IPv6 address provided.
     * @param addr IPv6 address
     * @return the MAC multicast address
     * \warning Calling this method is invalid if IsMulticast returns not true.
     */
    Address GetMulticast(Inet6Address addr);

    /**
     * @brief Return true if the net device is acting as a bridge.
     *
     * @return value of m_isBridge flag
     */
     boolean IsBridge();

    /**
     * @brief Return true if the net device is on a point-to-point link.
     *
     * @return value of m_isPointToPoint flag
     */
     boolean IsPointToPoint();
    /**
     * @param packet packet sent from above down to Network Device
     * @param dest mac address of the destination (already resolved)
     * @param protocolNumber identifies the type of payload contained in
     *        this packet. Used to call the right L3Protocol when the packet
     *        is received.
     *
     *  Called from higher layer to send packet into Network Device
     *  to the specified destination Address
     *
     * @return whether the Send operation succeeded
     */
    boolean Send(Packet packet, final Address dest, short protocolNumber);
    /**
     * @param packet packet sent from above down to Network Device
     * @param source source mac address (so called "MAC spoofing")
     * @param dest mac address of the destination (already resolved)
     * @param protocolNumber identifies the type of payload contained in
     *        this packet. Used to call the right L3Protocol when the packet
     *        is received.
     *
     *  Called from higher layer to send packet into Network Device
     *  with the specified source and destination Addresses.
     *
     * @return whether the Send operation succeeded
     */
    boolean SendFrom(Packet packet, final Address source, final Address dest, short protocolNumber);
    /**
     * @returns the node base class which contains this network
     *          interface.
     *
     * When a subclass needs to get access to the underlying node
     * base class to print the nodeid for example, it can invoke
     * this method.
     */
     Node GetNode();

    /**
     * @param node the node associated to this netdevice.
     *
     * This method is called from osak.ext.ns3::Node::AddDevice.
     */
     void SetNode(Node node);

    /**
     * @returns true if ARP is needed, false otherwise.
     *
     * Called by higher-layers to check if this NetDevice requires
     * ARP to be used.
     */
     boolean NeedsArp();

    /**
     * @param device a pointer to the net device which is calling this callback
     * @param packet the packet received
     * @param protocol the 16 bit protocol number associated with this packet.
     *        This protocol number is expected to be the same protocol number
     *        given to the Send method by the user on the sender side.
     * @param sender the address of the sender
     * @returns true if the callback could handle the packet successfully, false
     *          otherwise.
     */
    // ReceiveCallback -> CallbackR4<Boolean,NetDevice,Packet,Short, MacAddress>
    // typedef Callback<boolean, Ptr<NetDevice>, Ptr<const Packet>, uint16_t, const Address&>
    //    ReceiveCallback;

    /**
     * @param cb callback to invoke whenever a packet has been received and must
     *        be forwarded to the higher layers.
     *
     * Set the callback to be used to notify higher layers when a packet has been
     * received.
     */
    void SetReceiveCallback(CallbackR4<Boolean, NetDevice, Packet, Short, Address> cb);

    /**
     * @param device a pointer to the net device which is calling this callback
     * @param packet the packet received
     * @param protocol the 16 bit protocol number associated with this packet.
     *        This protocol number is expected to be the same protocol number
     *        given to the Send method by the user on the sender side.
     * @param sender the address of the sender
     * @param receiver the address of the receiver
     * @param packetType type of packet received (broadcast/multicast/unicast/otherhost)
     * @returns true if the callback could handle the packet successfully, false
     *          otherwise.
     */
    // PromiscReceiveCallback -> CallbackR6<Boolean,NetDevice,Packet,Short,
    // MacAddress, MacAddress, PacketType>
//    typedef Callback<boolean,
//                     Ptr<NetDevice>,
//                     Ptr<const Packet>,
//                     uint16_t,
//                     const Address&,
//                     const Address&,
//                     enum PacketType>
//        PromiscReceiveCallback;

    /**
     * @param cb callback to invoke whenever a packet has been received in promiscuous mode and must
     *        be forwarded to the higher layers.
     *
     * Enables netdevice promiscuous mode and sets the callback that
     * will handle promiscuous mode packets.  Note, promiscuous mode
     * packets means _all_ packets, including those packets that can be
     * sensed by the netdevice but which are intended to be received by
     * other hosts.
     */
    void SetPromiscReceiveCallback(
	    CallbackR6<Boolean, NetDevice, Packet, Short, Address, Address, PacketType> cb);

    /**
     * @return true if this interface supports a bridging mode, false otherwise.
     */
     boolean SupportsSendFrom();
}
