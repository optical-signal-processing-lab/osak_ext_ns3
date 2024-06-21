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

import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.Socket;

/**
 * TODO UdpSocket
 * 
 * @author zhangrui
 * @since   1.0
 */
public abstract class UdpSocket extends Socket {
    public UdpSocket() {

    }

    /**
     * <i>Corresponds to socket option MCAST_JOIN_GROUP</i>
     * <p>
     * Enable reception of multicast datagrams for this socket on the interface
     * number specified. If zero is specified as the interface, then a single local
     * interface is chosen by system. In the future, this function will generate
     * trigger IGMP joins as necessary when IGMP is implemented, but for now, this
     * just enables multicast datagram reception in the system if not already
     * enabled for this interface/groupAddress combination.
     * <p>
     * This function may be called repeatedly on a given socket but each join must
     * be for a different multicast address, or for the same multicast address but
     * on a different interface from previous joins. This enables host multihoming,
     * and the ability to join the same group on different interfaces.
     * 
     *
     * @param interface    interface number, or 0
     * @param groupAddress multicast group address
     * @returns on success, zero is returned. On error, -1 is returned, and errno is
     *          set appropriately
     *
     * @attention IGMP is not yet implemented in ns-3
     * 
     */
    public abstract int MulticastJoinGroup(int iface, final Address groupAddress);

    /**
     * <i> Corresponds to socket option MCAST_LEAVE_GROUP</i>
     *
     *
     * Disable reception of multicast datagrams for this socket on the interface
     * number specified. If zero is specified as the interfaceIndex, then a single
     * local interface is chosen by system. In the future, this function will
     * generate trigger IGMP leaves as necessary when IGMP is implemented, but for
     * now, this just disables multicast datagram reception in the system if this
     * socket is the last for this interface/groupAddress combination.
     *
     * @param interface    interface number, or 0
     * @param groupAddress multicast group address
     * @returns on success, zero is returned. On error, -1 is returned, and errno is
     *          set appropriately
     * @attention IGMP is not yet implemented in ns-3
     */
    public abstract int MulticastLeaveGroup(int iface, final Address groupAddress);

    // Indirect the attribute setting and getting through private virtual methods
    /**
     * @brief Set the receiving buffer size
     * @param size the buffer size
     */
    protected abstract void SetRcvBufSize(int size);

    /**
     * @brief Get the receiving buffer size
     * @returns the buffer size
     */
    protected abstract int GetRcvBufSize();

    /**
     * @brief Set the IP multicast TTL
     * @param ipTtl the IP multicast TTL
     */
    protected abstract void SetIpMulticastTtl(byte ipTtl);

    /**
     * @brief Get the IP multicast TTL
     * @returns the IP multicast TTL
     */
    protected abstract byte GetIpMulticastTtl();

    /**
     * @brief Set the IP multicast interface
     * @param ipIf the IP multicast interface
     */
    protected abstract void SetIpMulticastIf(int ipIf);

    /**
     * @brief Get the IP multicast interface
     * @returns the IP multicast interface
     */
    protected abstract int GetIpMulticastIf();

    /**
     * <i> Set the IP multicast loop capability</i>
     *
     * This means that the socket will receive the packets sent by itself on a
     * multicast address. Equivalent to setsockopt IP_MULTICAST_LOOP
     *
     * @param loop the IP multicast loop capability
     */
    protected abstract void SetIpMulticastLoop(boolean loop);

    /**
     * <i> Get the IP multicast loop capability</i>
     *
     * This means that the socket will receive the packets sent by itself on a
     * multicast address. Equivalent to setsockopt IP_MULTICAST_LOOP
     *
     * @returns the IP multicast loop capability
     */
    protected abstract boolean GetIpMulticastLoop();

    /**
     * @brief Set the MTU discover capability
     *
     * @param discover the MTU discover capability
     */
    protected abstract void SetMtuDiscover(boolean discover);

    /**
     * @brief Get the MTU discover capability
     *
     * @returns the MTU discover capability
     */
    protected abstract boolean GetMtuDiscover();
}
