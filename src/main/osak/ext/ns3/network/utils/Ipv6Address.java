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
import java.util.Objects;

import osak.ext.communication.MyLog;
import osak.ext.ns3.network.Address;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TODO Ipv6Address
 * 
 * @author lijianjun
 * @since   1.0
 */
public class Ipv6Address {
    byte[] m_address = new byte[16]; //IPv6 address
    boolean m_initialized; // 
    
    /**
     * 
     */
    public Ipv6Address() {
	Arrays.fill(m_address, (byte) 0);// fills 0
	m_initialized = false;
    }
    
    /**
     * 
     * @param addr
     */
    public Ipv6Address(final Ipv6Address addr) {
	System.arraycopy(addr.m_address, 0, m_address, 0, 16);// m_address->addr.m_addresss
	m_initialized = true;
    }
    
    /**
     * 
     * @param address
     */
    public Ipv6Address(byte[] address) {
	System.arraycopy(address, 0, m_address, 0, 16);// m_address->addr.m_addresss
	m_initialized = true;
    }
    
    /**
     * 
     * @param address
     */
    public Ipv6Address(String address) {
	try {
	    InetAddress addr = Inet6Address.getByName(address);
	    m_address = addr.getAddress();
	    m_initialized = true;
	} catch (Exception e) {
	    System.out.println(address);
	    MyLog.logOut("Ipv6Address::Ipv6Address",
		    "Error, can not build an IPv6 address from an invalid string: " + address, 16);
	    m_initialized = false;
	    Arrays.fill(m_address, (byte) 0);
	}
    }
    
    /**
     * 
     * @param address
     */
    public void Set(String address) {
	try {
	    InetAddress addr = Inet6Address.getByName(address);
	    m_address = addr.getAddress();//to 二进制
	    m_initialized = true;
	} catch (Exception e) {
	    MyLog.logOut("Ipv6Address::Ipv6Address",
		    "Error, can not build an IPv6 address from an invalid string: " + address, 16);
	    m_initialized = false;
	    Arrays.fill(m_address, (byte) 0);
	}
    }
    
    /**
     * 
     * @param address
     */
    public void Set(byte[] address) {
	System.arraycopy(address, 0, m_address, 0, 16);
	m_initialized = true;
    }
      
    /**
     * 
     * @param buf
     */
    public void Serialize(byte[] buf) {
	System.arraycopy(m_address, 0, buf, 0, 16);
    }
    
    /**
     * 
     * @param buf
     * @return
     */
    public static Ipv6Address Deserialize(byte[] buf) {
	Ipv6Address ipv6 = new Ipv6Address(buf);
	ipv6.m_initialized = true;
	return ipv6;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeIpv4MappedAddress(Ipv4Address addr) {
	byte [] buf = new byte[16];
	buf[0] = 0x00;
	buf[1] = 0x00;
	buf[2] = 0x00;
	buf[3] = 0x00;
	buf[4] = 0x00;
	buf[5] = 0x00;
	buf[6] = 0x00;
	buf[7] = 0x00;
	buf[8] = 0x00;
	buf[9] = 0x00;
	buf[10] = (byte)0xff;
	buf[11] = (byte)0xff;
	buf[12] = 0x00;
	buf[13] = 0x00;
	buf[14] = 0x00;
	buf[15] = 0x00;
	
	//将IPv4复制到buf的后四个字节
	byte[] temp_buf = new byte[4];
	addr.Serialize(temp_buf);
	buf[12] = temp_buf[0];
	buf[13] = temp_buf[1];
	buf[14] = temp_buf[2];
	buf[15] = temp_buf[3];
	Ipv6Address ipv6 = new Ipv6Address(buf);
	return ipv6;
    }
    
    /**
     * 
     * @return
     */
    public Ipv4Address GetIpv4MappedAddress() {
	byte[] buf = new byte[16];
	Ipv4Address v4Addr = new Ipv4Address();
	Serialize(buf);
	byte[] temp_buf = new byte[4];
	v4Addr = Ipv4Address.Deserialize(temp_buf);
	buf[12] = temp_buf[0];
	buf[13] = temp_buf[1];
	buf[14] = temp_buf[2];
	buf[15] = temp_buf[3];
	return v4Addr;
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Address addr, Ipv6Address prefix) {
	Ipv6Address ipv6Addr = GetAny();
	if(Mac64Address.IsMatchingType(addr)) {
	    ipv6Addr = MakeAutoconfiguredAddress(Mac64Address.ConvertFrom(addr), prefix);
	}
	else if(Mac48Address.IsMatchingType(addr)) {
	    ipv6Addr = MakeAutoconfiguredAddress(Mac48Address.ConvertFrom(addr), prefix);
	}
	else if(Mac16Address.IsMatchingType(addr)) {
	    ipv6Addr = MakeAutoconfiguredAddress(Mac16Address.ConvertFrom(addr), prefix);
	}
	else if(Mac8Address.IsMatchingType(addr)) {
	    ipv6Addr = MakeAutoconfiguredAddress(Mac8Address.ConvertFrom(addr), prefix);
	}
	if(ipv6Addr.IsAny()) {
	    assert false : "aborted. msg = Unknown address type";
	}
	return ipv6Addr;
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Address addr, Ipv6Prefix prefix) {
	Ipv6Address ipv6PrefixAddr = GetOnes().CombinePrefix(prefix);
	return MakeAutoconfiguredAddress(addr, ipv6PrefixAddr);
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Mac16Address addr, Ipv6Address prefix) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[2];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	prefix.GetBytes(buf2);
	Arrays.fill(buf2, 8, 16, (byte) 0);
	
	System.arraycopy(buf, 0, buf2, 14, 2);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Mac48Address addr, Ipv6Address prefix) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[16];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	prefix.GetBytes(buf2);
	
	System.arraycopy(buf, 0, buf2, 8, 3);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	System.arraycopy(buf, 3, buf2, 13, 3);
	buf2[8] ^= (byte) 0x02;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Mac64Address addr, Ipv6Address prefix) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[8];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	prefix.GetBytes(buf2);
	
	System.arraycopy(buf, 0, buf2, 8, 8);
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @param prefix
     * @return
     */
    public Ipv6Address MakeAutoconfiguredAddress(Mac8Address addr, Ipv6Address prefix) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[2];
	byte[] buf2 = new byte[16];
	
	buf[0] = 0;
	addr.CopyTo(buf[1]);
	prefix.GetBytes(buf2);
	Arrays.fill(buf2, 8, 16, (byte) 0);
	
	System.arraycopy(buf, 0, buf2, 8, 8);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeAutoconfiguredALinkLocalAddress(Address addr) {
   	Ipv6Address ipv6Addr = GetAny();
   	if(Mac64Address.IsMatchingType(addr)) {
   	    ipv6Addr = MakeAutoconfiguredALinkLocalAddress(Mac64Address.ConvertFrom(addr));
   	}
   	else if(Mac48Address.IsMatchingType(addr)) {
   	    ipv6Addr = MakeAutoconfiguredALinkLocalAddress(Mac48Address.ConvertFrom(addr));
   	}
   	else if(Mac16Address.IsMatchingType(addr)) {
   	    ipv6Addr = MakeAutoconfiguredALinkLocalAddress(Mac16Address.ConvertFrom(addr));
   	}
   	else if(Mac8Address.IsMatchingType(addr)) {
   	    ipv6Addr = MakeAutoconfiguredALinkLocalAddress(Mac8Address.ConvertFrom(addr));
   	}
   	if(ipv6Addr.IsAny()) {
   	    assert false : "aborted. msg = Unknown address type";
   	}
   	return ipv6Addr;
       }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeAutoconfiguredALinkLocalAddress(Mac16Address addr) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[2];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	Arrays.fill(buf2, 0, 16, (byte) 0);
	buf2[0] = (byte) 0xfe;
	buf2[1] = (byte) 0x80;
	
	System.arraycopy(buf, 0, buf2, 14, 2);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeAutoconfiguredALinkLocalAddress(Mac48Address addr) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[16];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	Arrays.fill(buf2, 0, 16, (byte) 0);
	buf2[0] = (byte) 0xfe;
	buf2[1] = (byte) 0x80;
	System.arraycopy(buf, 0, buf2, 8, 3);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	System.arraycopy(buf, 3, buf2, 13, 3);
	buf2[8] ^= (byte) 0x02;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeAutoconfiguredALinkLocalAddress(Mac64Address addr) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[8];
	byte[] buf2 = new byte[16];
	
	addr.CopyTo(buf);
	Arrays.fill(buf2, 0, 16, (byte) 0);
	buf2[0] = (byte) 0xfe;
	buf2[1] = (byte) 0x80;
	System.arraycopy(buf, 0, buf2, 8, 8);
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeAutoconfiguredALinkLocalAddress(Mac8Address addr) {
	Ipv6Address ret = new Ipv6Address();
	byte[] buf = new byte[2];
	byte[] buf2 = new byte[16];
	
	buf[0] = 0;
	addr.CopyTo(buf[1]);
	Arrays.fill(buf2, 0, 16, (byte) 0);
	buf2[0] = (byte) 0xfe;
	buf2[1] = (byte) 0x80;
	System.arraycopy(buf, 0, buf2, 14, 2);
	buf2[11] = (byte) 0xff;
	buf2[12] = (byte) 0xfe;
	ret.Set(buf2);
	return ret;
    }
    
    /**
     * 
     * @param addr
     * @return
     */
    public Ipv6Address MakeSolicitedAddress(Ipv6Address addr) {
	byte[] buf = new byte[16];
	byte[] buf2 = new byte[16];
	Ipv6Address ret = new Ipv6Address();
	
	addr.Serialize(buf2);
	Arrays.fill(buf, 0, 16, (byte) 0);
	buf[0] = (byte) 0xff;
	buf[1] = (byte) 0x02;
	buf[11] = (byte) 0x01;
	buf[12] = (byte) 0xff;
	buf[13] = buf2[13];
	buf[14] = buf2[14];
	buf[15] = buf2[15];
	ret.Set(buf2);
	return ret;
    }
    
    private String Gethost() {
	String res = "";	
	try {
	    InetAddress ipv6Address = Inet6Address.getByAddress(m_address);
	    res += ipv6Address.getHostAddress();
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} 
	return res;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address localhost = new Ipv6Address("::1");
    public boolean IsLocalhost() {	
	return (this.equals(localhost));
    }
    
    /**
     * 
     * @return
     */
    public boolean IsMulticast() {
	if(m_address[0] == 0xff)
	{
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @return
     */
    public boolean IsLinkLocalMulticast() {
	if(m_address[0] == 0xff && m_address[1] == 0x02) {
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @return
     */
    private static byte[] v4MappedPrefix = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff};
    public boolean IsIpv4MappedAddress() {
	if(Arrays.equals(m_address, v4MappedPrefix)) {
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @param prefix
     * @return
     */
    public Ipv6Address CombinePrefix(Ipv6Prefix prefix) {
	Ipv6Address ipv6 = new Ipv6Address();
	byte[] addr = new byte [16];
	byte[] pref = new byte [16];
	int i =0;
	
	System.arraycopy(m_address, 0, addr, 0, 16);
	prefix.GetBytes(pref);
	for(i = 0; i<16; i++) {
	    addr[i] = (byte) (addr[i] & pref[i]);
	}
	ipv6.Set(addr);;
	return ipv6;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address documentation1 = new Ipv6Address("ff02::1:ff00:0");
    public boolean IsSolicitedMulticast() {
	Ipv6Prefix prefix = new Ipv6Prefix((byte)104);
	if(CombinePrefix(prefix) == documentation1) {
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address allNodesI = new Ipv6Address("ff01::2");
    private static Ipv6Address allNodesL = new Ipv6Address("ff02::2");
    private static Ipv6Address allNodesR = new Ipv6Address("ff03::2");
    public boolean IsAllNodesMulticast() {
	return (this.equals(allNodesI) || this.equals(allNodesL) || this.equals(allNodesR));
    }
    
    /**
     * 
     * @return 
     */
    private static Ipv6Address allroutersI = new Ipv6Address("ff01::2");
    private static Ipv6Address allroutersL = new Ipv6Address("ff02::2");
    private static Ipv6Address allroutersR = new Ipv6Address("ff03::2");
    private static Ipv6Address allroutersS = new Ipv6Address("ff05::2");
    public boolean IsAllRoutersMulticast() {
	return (this.equals(allroutersI) || this.equals(allroutersL) 
		|| this.equals(allroutersR) ||this.equals(allroutersS));
    }
    
    /**
     * @return
     */
    public boolean IsAny() {
	Ipv6Address any = new Ipv6Address("::");
	return this.equals(any);
    }
   
    /**
     * 
     * @return
     */
    private static Ipv6Address documentation2 = new Ipv6Address("2001:db8::0");
    public boolean IsDocumentation() {
	Ipv6Prefix prefix = new Ipv6Prefix((byte)32);
	if(CombinePrefix(prefix) == documentation2) {
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @param prefix
     * @return
     */
    public boolean HasPrefix(Ipv6Prefix prefix) {
	Ipv6Address masked = CombinePrefix(prefix);
	Ipv6Address reference = GetOnes().CombinePrefix(prefix);
	return  (masked == reference);
    }
    
    /**
     * 
     * @param address
     * @return
     */
    public static boolean IsMatchingType(Address address) {
	return address.CheckCompatible(GetType(), (byte) 16);
    }
    
    /**
     * 
     * @return
     */
    public Address ConvertTo() {
	byte[] buf = new byte[16];
	Serialize(buf);
	Address address = new Address(GetType(), buf, (byte) 16);
	return address;
    }
    
    /**
     * 
     * @param address
     * @return
     */
    public static Ipv6Address ConvertFrom(Address address) {
	assert address.CheckCompatible(GetType(), (byte) 16) : "NS_ASSERT failed, cond=false";
	byte[] buf = new byte[16];
	address.CopyTo(buf);
	return Deserialize(buf);
    }
   
    /**
     * 
     * @return
     */
    private static byte type = Address.Register();
    public static byte GetType() {
	return type;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address nmc = new Ipv6Address("ff02::1");
    public Ipv6Address GetAllNodesMulticast() {
	return nmc;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address rmc = new Ipv6Address("ff02::2");
    public Ipv6Address GetAllRoutersMulticast() {
	return rmc;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address hmc = new Ipv6Address("ff02::3");
    public Ipv6Address GetAllHostsMulticast() {
	return hmc;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address loopback = new Ipv6Address("::1");
    public Ipv6Address GetLoopback() {
	return loopback;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address zero = new Ipv6Address("::");
    public Ipv6Address GetZero() {
	return zero;
    }
    
    /**
     * @return
     */
    private static Ipv6Address any = new Ipv6Address("::");
    public static Ipv6Address GetAny() {
	return any;
    }

    /**
     * 
     * @return
     */
    private static Ipv6Address ones = new Ipv6Address("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
    public Ipv6Address GetOnes() {
	return ones;
    }
    
    /**
     * 
     * @param buf
     */
    public void GetBytes(byte[] buf) {
	System.arraycopy(m_address, 0, buf, 0, 16);// m_address->buf
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Address linkLocal = new Ipv6Address("fe80::0");
    public boolean IsLinkLocal() {
	Ipv6Prefix prefix = new Ipv6Prefix((byte)64);
	if(CombinePrefix(prefix) == linkLocal) {
	    return true;
	}
	return false;
    }
    
    /**
     * 
     * @return
     */
    public boolean IsInitialized() {
	return m_initialized;
    }    
    
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(m_address);
	result = prime * result + Objects.hash(m_initialized);
	return result;
    }

    @Override
    public String toString() {
	return "[" + Gethost() + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Ipv6Address other = (Ipv6Address) obj;
	return Arrays.equals(m_address, other.m_address) && m_initialized == other.m_initialized;
    }
    
    // for test
    public static void main(String[] args){
	System.out.println("here");
	Ipv6Address ipv6 = new Ipv6Address("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
	System.out.println(ipv6);
    }
}
