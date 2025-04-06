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
 * The {@code Ipv6Prefix} class represents an Ipv6 prefix, which consists of an Ipv6 address and a prefix length.
 * It provides various constructors to create an Ipv6 prefix from different input types and includes methods for 
 * manipulation and comparison.
 * 
 * @author lijianjun
 * @since   1.0
 */
public final class Ipv6Prefix{
    private byte[] m_prefix = new byte[16];
    private byte m_prefixLength;
    /**
     * Default constructor.
     */
    public Ipv6Prefix() {
	Arrays.fill(m_prefix, (byte) 0);
	m_prefixLength = (byte) 64;	
    }
    
    /**
     * Constructs an {@code Ipv6Prefix} from a string representation of the Ipv6 address.
     * The prefix length is automatically determined as the minimum required.
     * @param prefix
     * 	      A string representing the Ipv6 address.
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
     * Constructs an {@code Ipv6Prefix} from a byte array representing the Ipv6 address.
     * The prefix length is automatically determined as the minimum required.
     * @param prefix
     * 	      A byte array representing the Ipv6 address.
     */
    public Ipv6Prefix(byte[] prefix) {
	System.arraycopy(prefix, 0, m_prefix, 0, 16);
	m_prefixLength = GetMinimumPrefixLength();
    }
    
    /**
     * Constructs an Ipv6Prefix from a string representation of the IPv6 address and a specific prefix length.
     * @param prefix
     * 	      A string representing the IPv6 address.
     * @param prefixLength
     * 	      The prefix length in bits.
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
     * Constructs an Ipv6Prefix from a byte array representing the IPv6 address and a specific prefix length.
     * @param prefix
     * 	      A byte array representing the IPv6 address.
     * @param prefixLength
     * 	      The prefix length in bits.
     */
    public Ipv6Prefix(byte[] prefix, byte prefixLength) {
	System.arraycopy(prefix, 0, m_prefix, 0, 16);
	int autoLength = GetMinimumPrefixLength();
	Ipv6Address temp = new Ipv6Address(prefix);
	assert autoLength <= prefixLength : "Ipv6Prefix: address and prefix are not compatible: " + temp + "/" + prefixLength;
	m_prefixLength= prefixLength;
    }
    
    /**
     * Constructs an Ipv6Prefix from a given prefix length. The address is automatically filled with the appropriate value based on the prefix length.
     * @param prefix
     *        The prefix length in bits.
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
     * Constructs a new Ipv6Prefix from an existing Ipv6Prefix.
     * @param prefix
     * 	      The Ipv6Prefix to copy.
     */
    public Ipv6Prefix(Ipv6Prefix prefix) {
	System.arraycopy(prefix.m_prefix, 0, m_prefix, 0, 16);
	m_prefixLength = prefix.m_prefixLength;
    }
    
    /**
     * Checks if two Ipv6 address match the current prefix.
     * @param a
     * 	      The first {@code Ipv6Address} to compare.
     * @param b
     * 	      The second {@code Ipv6Address} to compare.
     * @return true if the addresses match the prefix, false otherwise.
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
     * Gets the loopback Ipv6 prefix.
     * @return An {@code Ipv6Prefix} representing the loopback address.
     */
    private static Ipv6Prefix prefix = new Ipv6Prefix((byte)128);
    public Ipv6Prefix GetLoopback() {
	return prefix;
    }
    
    /**
     * Gets the all-ones Ipv6 prefix (all bits set to 1).
     * @return An {@code Ipv6Prefix} representing the all-ones address.
     */
    private static Ipv6Prefix ones = new Ipv6Prefix((byte)128);
    public Ipv6Prefix GetOnes() {
	return ones;
    }
    
    /**
     * Gets the all-zeros Ipv6 prefix (all bits set to 0).
     * @return An {@code Ipv6Prefix} representing the all-zeros address.
     */
    private static Ipv6Prefix zeros = new Ipv6Prefix((byte)0);
    public Ipv6Prefix GetZero() {
	return zeros;
    }
    
    /**
     * Copies the prefix bytes into a given buffer.
     * @param buf
     * 	      The buffer to copy the prefix into.
     */
    public void GetBytes(byte[] buf) {
	System.arraycopy(m_prefix, 0, buf, 0, 16);
    }
    
    /**
     * Converts the {@code Ipv6Prefix} to an {@code Ipv6Address}.
     * @return An {@code Ipv6Address} object representing the prefix.
     */
    public Ipv6Address ConvertToIpv6Address() {
	byte[] prefixBytes = new byte[16];
	System.arraycopy(m_prefix, 0, prefixBytes, 0, 16);
	
	Ipv6Address convertedPrefix = new Ipv6Address(prefixBytes);
	return convertedPrefix;
    }
    
    /**
     * Gets the prefix length.
     * @return The prefix length in bits.
     */
    public byte GetPrefixLength() {
	return m_prefixLength;
    }
    /**
     * Determines the minimum prefix length based on the non-zero bits of the address.
     * @return The minimum prefix length as a byte.
     */
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