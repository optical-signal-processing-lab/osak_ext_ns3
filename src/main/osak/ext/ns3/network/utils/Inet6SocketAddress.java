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

import java.util.Objects;

import osak.ext.ns3.network.Address;

/**
 * 
 * @author lijianjun
 * @since   1.0
 */
public class Inet6SocketAddress {
    /**
     * Get the underlying address type (automatically assigned).
     * 
     * @returns the address type.
     */
    private static byte GetType() {
	 return type;
    }

    private static byte type = Address.Register();
    private Ipv6Address m_ipv6;// !< the IPv6 address
    private short m_port;// !< the port
    
    /**
     * @param ipv6
     * @param port
     */
    public Inet6SocketAddress(Ipv6Address ipv6, short port) {
	m_ipv6 = ipv6;
	m_port = port;
    }
    
    /**
     * @param ipv6
     */
    public Inet6SocketAddress(Ipv6Address ipv6) {
	m_ipv6 = ipv6;
	m_port = 0;
    }
    
    /**
     * @param ipv6
     * @param port
     */
    public Inet6SocketAddress(String ipv6, short port) {
	m_ipv6 = new Ipv6Address(ipv6);
	m_port = port;
    }
    
    /**
     * @param ipv6
     */
    public Inet6SocketAddress(String ipv6) {
	m_ipv6 = new Ipv6Address(ipv6);
	m_port = 0;
    }
    
    /**
     * @param port
     */
    public Inet6SocketAddress(short port) {
	m_ipv6 = Ipv6Address.GetAny();
	m_port = 0;
    }
    
    /**
     * @returns the port number.
     */
    public short GetPort() {
	return m_port;
    }

    /**
     * @param port 
     * 	      The new port number.
     */
    public void SetPort(short port) {
	m_port = port;
    }
    
    /**
     * @returns the ipv6 address.
     */
    public Ipv6Address GetIpv6() {
	return m_ipv6;
    }
    
    /**
     * @param address 
     * 	      The new ipv6 address.
     */
    public void SetIpv6(Ipv6Address address) {
	m_ipv6 = address;
    }
    
    /**
     * @param address
     * @return
     */
    public static boolean IsMatchingType(Address address) {
	return address.CheckCompatible(GetType(), (byte) 18);// 16(address)+2(port)
    }

    /**
     * Returns an InetSocketAddress which corresponds to the input
     * Address.
     *
     * @param address the Address instance to convert from.
     * @returns An InetSocketAddress.
     */
    public static Inet6SocketAddress ConvertFrom(final Address address) {
	assert (address.CheckCompatible(GetType(), (byte) 18));
	byte[] buf = new byte[18];
	address.CopyTo(buf);
	Ipv6Address ipv6 = Ipv6Address.Deserialize(buf);
	short port = (short) (buf[16] | (buf[17] << 8));
	Inet6SocketAddress inet = new Inet6SocketAddress(ipv6, port);
	return inet;
    }

    /**
     * Convert to an Address type.
     * @return The Address corresponding to this object.
     */
    public Address ConvertTo() {
	byte[] buf = new byte[18];
	m_ipv6.Serialize(buf);
	buf[16] = (byte) (m_port & 0xff);
	buf[17] = (byte) ((m_port >>> 8) & 0xff);
	return new Address(GetType(), buf, (byte) 18);// Constructs a new Address object
    }
    
    private String Gethost() {
   	String res = "/";	
   	short port = GetPort();
   	Ipv6Address ipv6 = GetIpv6();
   	res += "port = " + port + " " + "Ipv6 : " + ipv6;
   	return res;
    }
    
    @Override
    public int hashCode() {
	return Objects.hash(m_ipv6, m_port);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Inet6SocketAddress other = (Inet6SocketAddress) obj;
	return Objects.equals(m_ipv6, other.m_ipv6) && m_port == other.m_port;
    }

    @Override
    public String toString() {
	return "[" + Gethost() + "]";
    }

    // for test
    public static void main(String[] args){
	System.out.println("here");
	Inet6SocketAddress ipv6Socket = new Inet6SocketAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
	System.out.println(ipv6Socket);
    }
}
