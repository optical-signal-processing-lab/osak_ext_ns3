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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import osak.ext.communication.Helper;
import osak.ext.communication.MyLog;
import osak.ext.ns3.internet.Ipv4Mask;
import osak.ext.ns3.network.Address;

/**
 * TODO Ipv4Address
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4Address {
    private static byte GetType() {
	return type;
    }

    private static final int UNINITIALIZED = 0x66666666;
    private static byte type = Address.Register();
    int m_address; // !< IPv4 address
    boolean m_initialized; // !< IPv4 address has been explicitly initialized to a valid value.

    // convert Inet4Address to Ipv4Address
    public Ipv4Address(InetAddress address) {
	byte[] addrB = address.getAddress();
	m_address = Helper.byte2int(addrB);
	m_initialized = true;
    }

    public Ipv4Address() {
	m_address = UNINITIALIZED;
	m_initialized = false;
    }

    /**
     * input address is in host order.
     * 
     * @param address The host order 32-bit address
     */
    public Ipv4Address(int address) {
	m_address = address;
	m_initialized = true;
    }

    /**
     * @brief Constructs an Ipv4Address by parsing a the input C-string
     *
     * Input address is in format:
     * \c hhh.xxx.xxx.lll
     * where \c h is the high byte and \c l the
     * low byte
     * @param address C-string containing the address as described above
     */
    public Ipv4Address(String address) {
	try {
	    InetAddress addr = Inet4Address.getByName(address);
	    byte[] addrB = addr.getAddress();
	    m_address = Helper.byte2int(addrB);
	    m_initialized = true;
	} catch (Exception e) {
	    MyLog.logOut("Ipv4Address::Ipv4Address",
		    "Error, can not build an IPv4 address from an invalid string: " + address, 4);
	    m_initialized = false;
	    m_address = 0;
	}
    }

    /**
     * @param getLocal
     */
    public Ipv4Address(final Ipv4Address obj) {
	m_address = obj.m_address;
	m_initialized = obj.m_initialized;
    }

    public Inet4Address GetInet4() {
	byte[] addr = Helper.int2Byte(m_address);
	try {
	    return (Inet4Address) Inet4Address.getByAddress(addr);
	} catch (UnknownHostException e) {
	    MyLog.logOut("Ipv4Address::GetInet4", e.getMessage(), 3);
	}
	return (Inet4Address) Inet4Address.getLoopbackAddress();
    }

    /**
     * Get the host-order 32-bit IP address
     * @return the host-order 32-bit IP address
     */
    public int Get() {
	return m_address;
    }

    /**
     * input address is in host order.
     * 
     * @param address The host order 32-bit address
     */
    public void Set(int address) {
	m_address = address;
	m_initialized = true;
    }

    /**
     * @brief Sets an Ipv4Address by parsing a the input C-string
     *
     * Input address is in format:
     * \c hhh.xxx.xxx.lll
     * where \c h is the high byte and \c l the
     * low byte
     * @param address C-string containing the address as described above
     */
    public void Set(String address) {
	try {
	    InetAddress addr = Inet4Address.getByName(address);
	    byte[] addrB = addr.getAddress();
	    m_address = Helper.byte2int(addrB);
	    m_initialized = true;
	} catch (Exception e) {
	    MyLog.logOut("Ipv4Address::Set",
		    "Error, can not build an IPv4 address from an invalid string: " + address, 4);
	    m_initialized = false;
	    m_address = 0;
	}
    }

    /**
     * Serialize this address to a 4-byte buffer
     *
     * @param buf output buffer to which this address gets overwritten with this
     * Ipv4Address
     */
    public void Serialize(byte[] buf) {
	buf[0] = (byte) ((m_address >>> 24) & 0xff);
	buf[1] = (byte) ((m_address >>> 16) & 0xff);
	buf[2] = (byte) ((m_address >>> 8) & 0xff);
	buf[3] = (byte) ((m_address >>> 0) & 0xff);
    }

    public void Serialize(byte[] buf, int index) {
	buf[0 + index] = (byte) ((m_address >>> 24) & 0xff);
	buf[1 + index] = (byte) ((m_address >>> 16) & 0xff);
	buf[2 + index] = (byte) ((m_address >>> 8) & 0xff);
	buf[3 + index] = (byte) ((m_address >>> 0) & 0xff);
    }

    /**
     * @param buf buffer to read address from
     * @return an Ipv4Address
     *
     * The input address is expected to be in network byte order format.
     */
    public static Ipv4Address Deserialize(final byte[] buf) {
	Ipv4Address ipv4 = new Ipv4Address();
	ipv4.m_address = 0;
	ipv4.m_address |= buf[0];
	ipv4.m_address <<= 8;
	ipv4.m_address |= buf[1];
	ipv4.m_address <<= 8;
	ipv4.m_address |= buf[2];
	ipv4.m_address <<= 8;
	ipv4.m_address |= buf[3];
	ipv4.m_initialized = true;

	return ipv4;
    }

    /**
     * @brief Print this address to the given output stream
     *
     * The print format is in the typical "192.168.1.1"
     * @param os The output stream to which this Ipv4Address is printed
     */
    // No use
    // public void Print(std::ostream& os){}

    /**
     * @return true if address is initialized (i.e., set to something), false otherwise
     */
    public boolean IsInitialized() {
	return (m_initialized);
    }

    /**
     * @return true if address is 0.0.0.0; false otherwise
     */
    public boolean IsAny() {
	return (m_address == 0x00000000);
    }

    /**
     * @return true if address is 127.0.0.1; false otherwise
     */
    public boolean IsLocalhost() {
	return (m_address == 0x7f000001);
    }

    /**
     * @return true if address is 255.255.255.255; false otherwise
     */
    public boolean IsBroadcast() {
	return (m_address == 0xffffffff);
    }

    /**
     * @return true only if address is in the range 224.0.0.0 - 239.255.255.255
     */
    public boolean IsMulticast() {
	return (m_address >= 0xe0000000 && m_address <= 0xefffffff);
    }

    /**
     * @return true only if address is in local multicast address scope, 224.0.0.0/24
     */
    public boolean IsLocalMulticast() {
	return (m_address & 0xffffff00) == 0xe0000000;
    }

    /**
     * @brief Combine this address with a network mask
     *
     * This method returns an IPv4 address that is this address combined
     * (bitwise and) with a network mask, yielding an IPv4 network
     * address.
     *
     * @param mask a network mask
     * @returns the address combined with the mask
     */
    public Ipv4Address CombineMask(final Ipv4Mask mask) {
	return new Ipv4Address(Get() & mask.Get());
    }

    /**
     * @brief Generate subnet-directed broadcast address corresponding to mask
     *
     * The subnet-directed broadcast address has the host bits set to all
     * ones.  If this method is called with a mask of 255.255.255.255,
     * (i.e., the address is a /32 address), the program will assert, since
     * there is no subnet associated with a /32 address.
     *
     * @param mask a network mask
     * @returns a broadcast address for the subnet.
     */
    public Ipv4Address GetSubnetDirectedBroadcast(final Ipv4Mask mask) {
	if (mask == Ipv4Mask.GetOnes()){
	    MyLog.logOut("Ipv4Address::GetSubnetDirectedBroadcast",
		    "Trying to get subnet-directed broadcast address with an all-ones netmask", 4);
	    assert (false);
	}
	return new Ipv4Address(Get() | mask.GetInverse());
    }

    /**
     * @brief Generate subnet-directed broadcast address corresponding to mask
     *
     * The subnet-directed broadcast address has the host bits set to all
     * ones.  If this method is called with a mask of 255.255.255.255,
     * (i.e., the address is a /32 address), the program will assert, since
     * there is no subnet associated with a /32 address.
     *
     * @param mask a network mask
     * @return true if the address, when combined with the input mask, has all
     * of its host bits set to one
     */
    public boolean IsSubnetDirectedBroadcast(final Ipv4Mask mask) {
	if (mask == Ipv4Mask.GetOnes()) {
	    // If the mask is 255.255.255.255, there is no subnet directed
	    // broadcast for this address.
	    return false;
	}
	return ((Get() | mask.Get()) == Ipv4Address.GetBroadcast().Get());
    }

    /**
     * @param address an address to compare type with
     *
     * @return true if the type of the address stored internally
     * is compatible with the type of the input address, false otherwise.
     */
    public static boolean IsMatchingType(final Address address) {
	return address.CheckCompatible(GetType(), (byte) 4);
    }

    /**
     * Convert an instance of this class to a polymorphic Address instance.
     *
     * @return a new Address instance
     */
    // 不存在
    // public operator Address() const{}
    /**
     * @param address a polymorphic address
     * @return a new Ipv4Address from the polymorphic address
     *
     * This function performs a type check and asserts if the
     * type of the input address is not compatible with an
     * Ipv4Address.
     */
    public static Ipv4Address ConvertFrom(final Address address) {
	assert (address.CheckCompatible(GetType(), (byte) 4));
	byte[] buf = new byte[4];
	address.CopyTo(buf);
	return Deserialize(buf);
    }

    /**
     * @brief Convert to an Address type
     * @return the Address corresponding to this object.
     */
    public Address ConvertTo() {
	byte[] buf = new byte[4];
	Serialize(buf);
	return new Address(GetType(), buf, (byte) 4);
    }

    /**
     * @return the 0.0.0.0 address
     */
    private static Ipv4Address zero = new Ipv4Address("0.0.0.0");
    public static Ipv4Address GetZero() {
	return zero;
    }

    /**
     * @return the 0.0.0.0 address
     */
    private static Ipv4Address any = new Ipv4Address("0.0.0.0");
    public static Ipv4Address GetAny() {
	return any;
    }

    /**
     * @return the 255.255.255.255 address
     */
    private static Ipv4Address broadcast = new Ipv4Address("255.255.255.255");
    public static Ipv4Address GetBroadcast() {
	return broadcast;
    }

    /**
     * @return the 127.0.0.1 address
     */
    private static Ipv4Address loopback = new Ipv4Address("127.0.0.1");
    public static Ipv4Address GetLoopback() {
	return loopback;
    }

    private String Gethost() {
	String res = "";
	for (int i = 3; i > 0; i--) {
	    int temp = (m_address >>> (8 * i)) & 0xff;
	    String scope = temp + ".";
	    res += scope;
	}
	int temp = m_address & 0xff;
	res += temp;
	return res;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Ipv4Address) {
	    Ipv4Address o = (Ipv4Address) obj;
	    return (m_address == o.m_address && m_initialized == o.m_initialized);
	}
	return false;
    }

    @Override
    public String toString() {
	return "[" + Gethost() + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Integer.hashCode(m_address);
	return result;
    }
    // for test
    public static void main(String[] args){
	System.out.println("here");
	Ipv4Address ipv4 = new Ipv4Address("255.168.1.1");
	System.out.println(ipv4);
    }
}
