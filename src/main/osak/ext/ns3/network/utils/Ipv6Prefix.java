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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * 
 * TODO Ipv6Prefix
 * 
 * @author lijianjun
 * @since   1.0
 */
public final class Ipv6Prefix{
    private byte[] m_prefix = new byte[16];
    private byte m_prefixLength;
    /**
     * 
     */
    public Ipv6Prefix() {
	Arrays.fill(m_prefix, (byte) 0);
	m_prefixLength = (byte) 64;	
    }
    
    /**
     * 
     * @param prefix
     */
    public Ipv6Prefix(String prefix) {
	try {
	    InetAddress addr = Inet6Address.getByName(prefix);
	    m_prefix = addr.getAddress();
	} catch (Exception e) {
	    assert false : "aborted. msg = Error, can not build an IPv6 prefix from an invalid string : " + prefix;
	}
	m_prefixLength = GetMinimumPrefixLength();
    }
    
    /**
     * 
     * @param prefix
     */
    public Ipv6Prefix(byte[] prefix) {
	System.arraycopy(prefix, 0, m_prefix, 0, 16);
	m_prefixLength = GetMinimumPrefixLength();
    }
    
    /**
     * 
     * @param prefix
     * @param prefixLength
     */
    public Ipv6Prefix(String prefix, byte prefixLength) {
	try {
	    InetAddress addr = Inet6Address.getByName(prefix);
	    m_prefix = addr.getAddress();
	} catch (Exception e) {
	    assert false : "aborted. msg = Error, can not build an IPv6 prefix from an invalid string : " + prefix;
	}
	byte autoLength = GetMinimumPrefixLength();
	Ipv6Address temp = new Ipv6Address(prefix);
	assert autoLength <= prefixLength : "Ipv6Prefix: address and prefix are not compatible: " + temp + "/" + prefixLength;
	m_prefixLength= prefixLength;
    }
    
    /**
     * 
     * @param prefix
     * @param prefixLength
     */
    public Ipv6Prefix(byte[] prefix, byte prefixLength) {
	System.arraycopy(prefix, 0, m_prefix, 0, 16);
	int autoLength = GetMinimumPrefixLength();
	Ipv6Address temp = new Ipv6Address(prefix);
	assert autoLength <= prefixLength : "Ipv6Prefix: address and prefix are not compatible: " + temp + "/" + prefixLength;
	m_prefixLength= prefixLength;
    }
    
    /**
     * 
     * @param prefix
     */
    public Ipv6Prefix(byte prefix) {
	int nb = 0;
	int mod = 0;
	int i = 0;
	int temp = prefix & 0xff;//signed->unsigned
	Arrays.fill(m_prefix, (byte) 0);
	m_prefixLength = prefix;
	
	assert prefix <= 128;
	nb = temp / 8; 
	mod = temp % 8;

	if(nb > 0) {
	    Arrays.fill(m_prefix, (byte) 0xff);
	}
	if(mod!=0) {
	    m_prefix[nb] = (byte) (0xff << (8 - mod));
	}
	if(nb < 16) {
	    nb++;
	    for(i = nb;i<16;i++) {
		m_prefix[i] = (byte) 0x00;
	    }
	}
    }
    
    /**
     * 
     * @param prefix
     */
    public Ipv6Prefix(Ipv6Prefix prefix) {
	System.arraycopy(prefix.m_prefix, 0, m_prefix, 0, 16);
	m_prefixLength = prefix.m_prefixLength;
    }
    
    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public boolean IsMatch(Ipv6Address a, Ipv6Address b) {
	byte[] addrA = new byte[16];
	byte[] addrB = new byte[16];
	int i = 0;
	
	a.GetBytes(addrA);
	b.GetBytes(addrB);
	
	for(i = 0;i<16;i++) {
	    if((addrA[i] & m_prefix[i]) != (addrB[i] & m_prefix[i])) {
		return false;
	    }
	}
	return true;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Prefix prefix = new Ipv6Prefix((byte)128);
    public Ipv6Prefix GetLoopback() {
	return prefix;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Prefix ones = new Ipv6Prefix((byte)128);
    public Ipv6Prefix GetOnes() {
	return ones;
    }
    
    /**
     * 
     * @return
     */
    private static Ipv6Prefix zeros = new Ipv6Prefix((byte)0);
    public Ipv6Prefix GetZero() {
	return zeros;
    }
    
    /**
     * 
     * @param buf
     */
    public void GetBytes(byte[] buf) {
	System.arraycopy(m_prefix, 0, buf, 0, 16);
    }
    
    /**
     * 
     * @return
     */
    public Ipv6Address ConvertToIpv6Address() {
	byte[] prefixBytes = new byte[16];
	System.arraycopy(m_prefix, 0, prefixBytes, 0, 16);
	
	Ipv6Address convertedPrefix = new Ipv6Address(prefixBytes);
	return convertedPrefix;
    }
    
    /**
     * 
     * @return
     */
    public byte GetPrefixLength() {
	return m_prefixLength;
    }
    
    public byte GetMinimumPrefixLength() {
	byte prefixLength = 0;
	boolean stop = false;
	
	for(int i=15;i>=0&&!stop;i--) {
	    byte mask = m_prefix[i];
	    for(int j=0;j<8&&!stop;j++) {
    	    	if((mask&1)==0) {
    	    	    mask = (byte) (mask >> 1);
    	    	    prefixLength++;
    	    	}
    	    	else {
    	    	    stop = true;
    	    	}
	    }
	}
	return (byte)(128 - prefixLength);
    }

    private String Gethost() {
   	String res = "/";	
   	byte temp = GetPrefixLength();
   	res += (temp&0xff);//signed->unsigned
   	return res;
    }
    
    @Override
    public String toString() {
	return "[" + Gethost() + "]";
    }
    
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(m_prefix);
	result = prime * result + Objects.hash(m_prefixLength);
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
	Ipv6Prefix other = (Ipv6Prefix) obj;
	return Arrays.equals(m_prefix, other.m_prefix) && m_prefixLength == other.m_prefixLength;
    }
    
    // for test
    public static void main(String[] args){
	System.out.println("here");
	Ipv6Prefix prefix = new Ipv6Prefix("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
	System.out.println(prefix);
    }
}