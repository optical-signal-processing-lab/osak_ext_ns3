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

import osak.ext.ns3.network.utils.Ipv4Address;
/**
 * TODO Ipv4InterfaceAddress
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class Ipv4InterfaceAddress {
    enum InterfaceAddressScope_e {
	HOST, LINK, GLOBAL
    };

    private Ipv4Address m_local;// !< Interface address
    // Note: m_peer may be added in future when necessary
    // Ipv4Address m_peer; // Peer destination address (in Linux: m_address)
    private Ipv4Mask m_mask; // !< Network mask
    private Ipv4Address m_broadcast; // !< Broadcast address

    private InterfaceAddressScope_e m_scope = InterfaceAddressScope_e.GLOBAL; // !< Address scope
    private boolean m_secondary = false; // !< For use in multihoming

    public Ipv4InterfaceAddress() {

    }

    /**
     * Configure local address, mask and broadcast address
     * 
     * @param local the local address
     * @param mask  the network mask
     */
    public Ipv4InterfaceAddress(Ipv4Address local, Ipv4Mask mask) {
	m_local = local;
	if (m_local == Ipv4Address.GetLoopback())
	{
	    m_scope = InterfaceAddressScope_e.HOST;
	}
	m_mask = mask;
	m_broadcast = new Ipv4Address(local.Get() | ~mask.Get());
    }

    /**
     * Copy constructor
     * 
     * @param o the object to copy
     */
    public Ipv4InterfaceAddress(Ipv4InterfaceAddress o) {
	m_local = o.m_local;
	m_mask = o.m_mask;
	m_broadcast = o.m_broadcast;
	m_scope = o.m_scope;
	m_secondary = o.m_secondary;
    }

    /**
     * Set local address
     * 
     * @param local the address
     *
     * @note Functionally identical to `Ipv4InterfaceAddress::SetAddress`. The
     *       method corresponds to the linux variable in_ifaddr.ifa_local
     *       `Ipv4InterfaceAddress::SetAddress` is to be preferred.
     */
    public void SetLocal(Ipv4Address local) {
	// TODO: maybe never use
	m_local = local;
    }

    /**
     * Set local address
     * 
     * @param address the address
     *
     * @note Functially identical to `Ipv4InterfaceAddress::SetLocal`. This function
     *       is consistent with `Ipv6InterfaceAddress::SetAddress`.
     */
    public void SetAddress(Ipv4Address address) {
	SetLocal(address);
    }

    /**
     * Get the local address
     * 
     * @returns the local address
     *
     * @note Functionally identical to `Ipv4InterfaceAddress::GetAddress`. The
     *       method corresponds to the linux variable in_ifaddr.ifa_local
     *       `Ipv4InterfaceAddress::GetAddress` is to be preferred.
     */
    public Ipv4Address GetLocal() {
	return m_local;
    }

    /**
     * Get the local address
     * 
     * @returns the local address
     *
     * @note Functially identical to `Ipv4InterfaceAddress::GetLocal`. This function
     *       is consistent with `Ipv6InterfaceAddress::GetAddress`.
     */
    public Ipv4Address GetAddress() {
	return GetLocal();
    }

    /**
     * Set the network mask
     * 
     * @param mask the network mask
     */
    public void SetMask(Ipv4Mask mask) {
	m_mask = mask;
    }

    /**
     *  Get the network mask
     * @returns the network mask
     */
    public Ipv4Mask GetMask() {
	return m_mask;
    }

    /**
     * Set the broadcast address
     * 
     * @param broadcast the broadcast address
     */
    public void SetBroadcast(Ipv4Address broadcast) {
	m_broadcast = broadcast;
    }

    /**
     *  Get the broadcast address
     * @returns the broadcast address
     */
    public Ipv4Address GetBroadcast() {
	return m_broadcast;
    }

    /**
     *  Set the scope.
     * @param scope the scope of address
     */
    public void SetScope(InterfaceAddressScope_e scope) {
	m_scope = scope;
    }

    /**
     *  Get address scope.
     * @return scope
     */
    public InterfaceAddressScope_e GetScope() {
	return m_scope;
    }

    /**
     *  Checks if the address is in the same subnet.
     * @param b the address to check
     * @return true if the address is in the same subnet.
     */
    public boolean IsInSameSubnet(final Ipv4Address b) {
	Ipv4Address aAddr = m_local;
	aAddr = aAddr.CombineMask(m_mask);
	Ipv4Address bAddr = b;
	bAddr = bAddr.CombineMask(m_mask);

	return (aAddr.equals(bAddr));
    }

    /**
     *  Check if the address is a secondary address
     *
     * Secondary address is used for multihoming
     * @returns true if the address is secondary
     */
    public boolean IsSecondary() {
	return m_secondary;
    }

    /**
     * Make the address secondary (used for multihoming)
     */
    public void SetSecondary() {
	m_secondary = true;
    }

    /**
     * Make the address primary
     */
    public void SetPrimary() {
	m_secondary = false;
    }
}
