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

import osak.ext.ns3.network.Address;

/**
 * TODO InetSocketAddress
 * 
 * @author zhangrui
 * @since   1.0
 */
public class InetSocketAddress {
    /**
     * Get the underlying address type (automatically assigned).
     *
     * @returns the address type
     */
    private static byte GetType() {
	return type;
    }

    private static byte type = Address.Register();
    private Ipv4Address m_ipv4;// !< the IPv4 address
    private short m_port;// !< the port
    private byte m_tos;// !< the ToS

    /**
     * @param ipv4 the ipv4 address
     * @param port the port number
     */
    public InetSocketAddress(Ipv4Address ipv4, short port) {
	m_ipv4 = ipv4;
	m_port = port;
	m_tos = 0;

    }

    /**
     * @param ipv4 the ipv4 address
     *
     *             The port number is set to zero by default.
     */
    public InetSocketAddress(Ipv4Address ipv4) {
	m_ipv4 = ipv4;
	m_port = 0;
	m_tos = 0;
    }

    /**
     * @param port the port number
     *
     *             The ipv4 address is set to the "Any" address by default.
     */
    public InetSocketAddress(short port) {
	m_ipv4 = Ipv4Address.GetAny();
	m_port = 0;
	m_tos = 0;
    }

    /**
     * @param ipv4 string which represents an ipv4 address
     * @param port the port number
     */
    public InetSocketAddress(String ipv4, short port) {
	m_ipv4 = new Ipv4Address(ipv4);
	m_port = port;
	m_tos = 0;
    }

    /**
     * @param ipv4 string which represents an ipv4 address
     *
     *             The port number is set to zero.
     */
    public InetSocketAddress(String ipv4) {
	m_ipv4 = new Ipv4Address(ipv4);
	m_port = 0;
	m_tos = 0;

    }

    /**
     * @returns the port number
     */
    public short GetPort() {
	return m_port;
    }

    /**
     * @returns the ipv4 address
     */
    public Ipv4Address GetIpv4() {
	return m_ipv4;
    }

    /**
     * @returns the ToS
     */
    public byte GetTos() {
	return m_tos;
    }

    /**
     * @param port the new port number.
     */
    public void SetPort(short port) {
	m_port = port;
    }

    /**
     * @param address the new ipv4 address
     */
    public void SetIpv4(Ipv4Address address) {
	m_ipv4 = address;
    }

    public void SetIpv4(Inet4Address address) {
	m_ipv4 = new Ipv4Address(address);
    }

    /**
     * @param tos the new ToS.
     */
    public void SetTos(byte tos) {
	m_tos = tos;
    }

    /**
     * @param address address to test
     * @returns true if the address matches, false otherwise.
     */
    public static boolean IsMatchingType(final Address address) {
	return address.CheckCompatible(GetType(), (byte) 7);
    }

    /**
     * @returns an Address instance which represents this
     * InetSocketAddress instance.
     */
    // 类型转换重载，java中不存在
    // public operator Address();

    /**
     *  Returns an InetSocketAddress which corresponds to the input
     * Address.
     *
     * @param address the Address instance to convert from.
     * @returns an InetSocketAddress
     */
    public static InetSocketAddress ConvertFrom(final Address address) {
	assert (address.CheckCompatible(GetType(), (byte) 7));
	byte[] buf = new byte[7];
	address.CopyTo(buf);
	Ipv4Address ipv4 = Ipv4Address.Deserialize(buf);
	short port = (short) (buf[4] | (buf[5] << 8));
	byte tos = buf[6];
	InetSocketAddress inet = new InetSocketAddress(ipv4, port);
	inet.SetTos(tos);
	return inet;
    }

    /**
     *  Convert to an Address type
     * @return the Address corresponding to this object.
     */
    public Address ConvertTo() {
	byte[] buf = new byte[7];
	m_ipv4.Serialize(buf);
	buf[4] = (byte) (m_port & 0xff);
	buf[5] = (byte) ((m_port >>> 8) & 0xff);
	buf[6] = m_tos;
	return new Address(GetType(), buf, (byte) 7);
    }
}
