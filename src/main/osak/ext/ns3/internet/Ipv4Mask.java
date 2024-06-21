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

import java.net.Inet4Address;
import java.net.UnknownHostException;

import osak.ext.communication.Helper;

/**
 * TODO Ipv4Mask
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class Ipv4Mask {
    private int m_mask;// !< IP mask
    /**
     * Will initialize to a garbage value (0x66666666)
     */
    public Ipv4Mask() {
	m_mask = 0x66666666;
    }
    
    /**
     * @param mask bitwise integer representation of the mask
     *
     *             For example, the integer input 0xffffff00 yields a 24-bit mask
     */
    public Ipv4Mask(int mask) {
	m_mask = mask;
    }
    
    /**
     * @param mask String constant either in "255.255.255.0" or "/24" format
     */
    public Ipv4Mask(String mask) {
	if (mask.charAt(0) == '/') {
	    int plen = Integer.parseInt(mask.substring(1));
	    assert(plen<=32);
	    if(plen>0) {
		m_mask = 0xffffffff << (32 - plen);
	    } else {
		m_mask = 0;
	    }
	}
	else {
	    try {
		Inet4Address temp = (Inet4Address) Inet4Address.getByName(mask);
		m_mask = Helper.byte2int(temp.getAddress());
	    } catch (UnknownHostException e) {
		m_mask = 0x66666666;
	    }

	}
    }
    /**
     * @param a first address to compare
     * @param b second address to compare
     * @return true if both addresses are equal in their masked bits,
     * corresponding to this mask
     */
    public boolean IsMatch(Inet4Address a, Inet4Address b) {
	int Aaddr = Helper.byte2int(a.getAddress());
	int Baddr = Helper.byte2int(b.getAddress());
	if ((Aaddr & m_mask) == (Baddr & m_mask)) {
	    return true;
	} else {
	    return false;
	}
    }
    
    /**
     * Get the host-order 32-bit IP mask
     * 
     * @return the host-order 32-bit IP mask
     */
    public int Get() {
	return m_mask;
    }

    /**
     * input mask is in host order.
     * 
     * @param mask The host order 32-bit mask
     */
    public void Set(int mask) {
	m_mask = mask;
    }

    /**
     * \brief Return the inverse mask in host order.
     * 
     * @return The inverse mask
     */
    public int GetInverse() {
	return ~m_mask;
    }

    /**
     * \brief Print this mask to the given output stream
     *
     * The print format is in the typical "255.255.255.0"
     * @param os The output stream to which this Ipv4Address is printed
     */
    // public void Print(std::ostream& os) const;

    /**
     * @return the prefix length of mask (the yy in x.x.x.x/yy notation)
     */
    public short GetPrefixLength() {
	short tmp = 0;
	int mask = m_mask;
	while (mask != 0) {
	    mask <<= 1;
	    tmp++;
	}
	return tmp;
    }

    /**
     * @return the 255.0.0.0 mask corresponding to a typical loopback address
     */
    private static final Ipv4Mask loopback = new Ipv4Mask("0.0.0.0");
    public static Ipv4Mask GetLoopback() {
	return loopback;
    }

    /**
     * @return the 0.0.0.0 mask
     */
    private static final Ipv4Mask zero = new Ipv4Mask("0.0.0.0");
    public static Ipv4Mask GetZero() {
	return zero;
    }

    /**
     * @return the 255.255.255.255 mask
     */
    private static final Ipv4Mask ones = new Ipv4Mask("0.0.0.0");
    public static Ipv4Mask GetOnes() {
	return ones;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Ipv4Mask) {
	    Ipv4Mask o = (Ipv4Mask) obj;
	    return (m_mask == o.m_mask);
	}
	return false;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Integer.hashCode(m_mask);
	return result;
    }
}
