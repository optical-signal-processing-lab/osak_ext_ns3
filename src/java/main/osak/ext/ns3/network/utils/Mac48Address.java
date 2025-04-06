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
package osak.ext.ns3.network.utils;

import java.util.Arrays;

import osak.ext.communication.MyLog;
import osak.ext.ns3.network.Address;

/**
 * TODO Mac48Address
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Mac48Address {
    private static long m_allocationIndex; // !< Address allocation index
    private byte[] m_address = new byte[6];// !< address value

    public Mac48Address() {
	Arrays.fill(m_address, (byte) 0);
    }


    /**
     * The format of the string is "xx:xx:xx:xx:xx:xx"
     * 
     * @param str a string representing the new Mac48Address
     *
     */
    private static int toByte(char c) {
	if (c >= '0' && c <= '9')
	    return (c - '0');
	if (c >= 'A' && c <= 'F')
	    return (c - 'A' + 0x0A);
	if (c >= 'a' && c <= 'f')
	    return (c - 'a' + 0x0a);
	throw new RuntimeException("Error MacAddress");
    }

    public Mac48Address(String str) {
	String[] the_split = str.split(":");
	assert (the_split.length == 6);
	int i = 0;
	try {
	    for(String s:the_split) {
		int temp = (toByte(s.charAt(0)) << 4) | (toByte(s.charAt(1)));
		m_address[i] = (byte) (temp & 0xff);
		i++;
	    }
	}catch(RuntimeException e) {
	    MyLog.logOut(e.getMessage(), MyLog.ERROR);
	    Arrays.fill(m_address, (byte) 0);
	}

    }

    /**
     * Copy the input address to our internal buffer.
     * 
     * @param buffer address in network order
     *
     */
    public void CopyFrom(final byte[] buffer) {
	System.arraycopy(buffer, 0, m_address, 0, 6);
    }

    /**
     * Copy the internal address to the input buffer.
     * @param buffer address in network order
     */
    public void CopyTo(byte[] buffer) {
	System.arraycopy(m_address, 0, buffer, 0, 6);
    }

    /**
     * Convert an instance of this class to a polymorphic Address instance.
     * <p>
     * dont exist in java
     * 
     * @returns a new Address instance
     */
    // operator Address() const;

    /**
     * @param address a polymorphic address
     * @returns a new Mac48Address from the polymorphic address
     *
     *          This function performs a type check and asserts if the type of the
     *          input address is not compatible with an Mac48Address.
     */
    public static Mac48Address ConvertFrom(final Address address) {
	assert (address.CheckCompatible(GetType(), (byte) 6));
	Mac48Address retval = new Mac48Address();
	address.CopyTo(retval.m_address);
	return retval;
    }

    /**
     * Convert an instance of this class to a polymorphic Address instance.
     * 
     * @returns a new Address instance
     *
     */
    public Address ConvertTo() {
	return new Address(GetType(), m_address, (byte) 6);
    }

    /**
     * @param address address to test
     * @returns true if the address matches, false otherwise.
     */
    public static boolean IsMatchingType(final Address address) {
	return address.CheckCompatible(GetType(), (byte) 6);
    }

    /**
     * Allocate a new Mac48Address.
     * 
     * @returns newly allocated mac48Address
     */
    public static Mac48Address Allocate() {
	/*
	 * TODO: need to check 
	 * if (m_allocationIndex == 0) {
	 * Simulator::ScheduleDestroy(Mac48Address::ResetAllocationIndex); }
	 */
	m_allocationIndex++;
	Mac48Address address = new Mac48Address();
	address.m_address[0] = (byte) ((m_allocationIndex >>> 40) & 0xff);
	address.m_address[1] = (byte) ((m_allocationIndex >>> 32) & 0xff);
	address.m_address[2] = (byte) ((m_allocationIndex >>> 24) & 0xff);
	address.m_address[3] = (byte) ((m_allocationIndex >>> 16) & 0xff);
	address.m_address[4] = (byte) ((m_allocationIndex >>> 8) & 0xff);
	address.m_address[5] = (byte) (m_allocationIndex & 0xff);
	return address;
    }

    /**
     * Reset the Mac48Address allocation index.
     *
     * This function resets (to zero) the global integer that is used for unique
     * address allocation. It is automatically called whenever \code
     * SimulatorDestroy (); \endcode is called. It may also be optionally called by
     * user code if there is a need to force a reset of this allocation index.
     */
    public static void ResetAllocationIndex() {
	m_allocationIndex = 0;
    }

    /**
     * @returns true if this is a broadcast address, false otherwise.
     */
    public boolean IsBroadcast() {
	return this.equals(Mac48Address.GetBroadcast());
    }

    /**
     * @returns true if the group bit is set, false otherwise.
     */
    public boolean IsGroup() {
	return (m_address[0] & 0x01) == 0x01;
    }

    /**
     * @returns the broadcast address
     */
    private static Mac48Address broadcast = new Mac48Address("ff:ff:ff:ff:ff:ff");
    public static Mac48Address GetBroadcast() {
	return broadcast;
    }

    /**
     * @param address base IPv4 address
     * @returns a multicast address
     */

    public static Mac48Address GetMulticast(Ipv4Address multicastGroup) {
	Mac48Address etherAddr = Mac48Address.GetMulticastPrefix();
	//
	// We now have the multicast address in an abstract 48-bit container. We
	// need to pull it out so we can play with it. When we're done, we have the
	// high order bits in etherBuffer[0], etc.
	//
	byte[] etherBuffer = new byte[6];
	etherAddr.CopyTo(etherBuffer);
	//
	// Now we need to pull the raw bits out of the Ipv4 destination address.
	//
	byte[] ipBuffer = new byte[4];
	multicastGroup.Serialize(ipBuffer);

	//
	// RFC 1112 says that an Ipv4 host group address is mapped to an EUI-48
	// multicast address by placing the low-order 23-bits of the IP address into
	// the low-order 23 bits of the Ethernet multicast address
	// 01-00-5E-00-00-00 (hex).
	//
	etherBuffer[3] |= ipBuffer[1] & 0x7f;
	etherBuffer[4] = ipBuffer[2];
	etherBuffer[5] = ipBuffer[3];

	//
	// Now, etherBuffer has the desired ethernet multicast address. We have to
	// suck these bits back into the Mac48Address,
	//
	Mac48Address result = new Mac48Address();
	result.CopyFrom(etherBuffer);
	return result;
    }

    /**
     * @brief Get multicast address from IPv6 address.
     * @param address base IPv6 address
     * @returns a multicast address
     */
    public static Mac48Address GetMulticast(Ipv6Address address) {
	MyLog.logOut("not implement yet", MyLog.ERROR);
	return null;
    }

    /**
     * @returns the multicast prefix (01:00:5e:00:00:00).
     */
    private static Mac48Address multicastPrefix = new Mac48Address("01:00:5e:00:00:00");
    public static Mac48Address GetMulticastPrefix() {
	return multicastPrefix;
    }

    /**
     * @brief Get the multicast prefix for IPv6 (33:33:00:00:00:00).
     * @returns a multicast address.
     */
    private static Mac48Address multicast = new Mac48Address("33:33:00:00:00:00");
    public static Mac48Address GetMulticast6Prefix() {
	return multicast;
    }

    private static byte type = Address.Register();
    private static byte GetType() {
	return type;
    }


    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(m_address);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Mac48Address other = (Mac48Address) obj;
	return Arrays.equals(m_address, other.m_address);
    }

    @Override
    public String toString() {
	String res = "";
	for (int i = 0; i < 5; i++) {
	    res += String.format("%02x:", m_address[i]);
	}
	res += String.format("%02x", m_address[5]);
	return res;
    }

}
