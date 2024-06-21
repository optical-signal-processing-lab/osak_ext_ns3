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
 * TODO Mac64Address
 * 
 * @author lijianjun
 * @since   1.0
 */
public class Mac64Address{
    private static long m_allocationIndex; // !< Address allocation index
    private byte[] m_address = new byte[8];// !< address value

    public Mac64Address() {
	Arrays.fill(m_address, (byte) 0);
    }


    /**
     * The format of the string is "xx:xx:xx:xx:xx:xx"
     * 
     * @param str a string representing the new Mac64Address
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

    public Mac64Address(String str) {
	String[] the_split = str.split(":");
	assert (the_split.length == 8);
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
	System.arraycopy(buffer, 0, m_address, 0, 8);
    }

    /**
     * Copy the internal address to the input buffer.
     * @param buffer address in network order
     */
    public void CopyTo(byte[] buffer) {
	System.arraycopy(m_address, 0, buffer, 0, 8);
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
     * @returns a new Mac64Address from the polymorphic address
     *
     *          This function performs a type check and asserts if the type of the
     *          input address is not compatible with an Mac64Address.
     */
    public static Mac64Address ConvertFrom(final Address address) {
	assert (address.CheckCompatible(GetType(), (byte) 8));
	Mac64Address retval = new Mac64Address();
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
	return new Address(GetType(), m_address, (byte) 8);
    }

    /**
     * @param address address to test
     * @returns true if the address matches, false otherwise.
     */
    public static boolean IsMatchingType(final Address address) {
	return address.CheckCompatible(GetType(), (byte) 8);
    }

    /**
     * Allocate a new Mac64Address.
     * 
     * @returns newly allocated mac64Address
     */
    public static Mac64Address Allocate() {
	m_allocationIndex++;
	Mac64Address address = new Mac64Address();
	address.m_address[0] = (byte) ((m_allocationIndex >>> 56) & 0xff);
	address.m_address[1] = (byte) ((m_allocationIndex >>> 48) & 0xff);
	address.m_address[2] = (byte) ((m_allocationIndex >>> 40) & 0xff);
	address.m_address[3] = (byte) ((m_allocationIndex >>> 32) & 0xff);
	address.m_address[4] = (byte) ((m_allocationIndex >>> 24) & 0xff);
	address.m_address[5] = (byte) ((m_allocationIndex >>> 16) & 0xff);
	address.m_address[6] = (byte) ((m_allocationIndex >>> 8) & 0xff);
	address.m_address[7] = (byte) (m_allocationIndex & 0xff);
	return address;
    }

    /**
     * Reset the Mac64Address allocation index.
     *
     * This function resets (to zero) the global integer that is used for unique
     * address allocation. It is automatically called whenever \code
     * SimulatorDestroy (); \endcode is called. It may also be optionally called by
     * user code if there is a need to force a reset of this allocation index.
     */
    public static void ResetAllocationIndex() {
	m_allocationIndex = 0;
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
	Mac64Address other = (Mac64Address) obj;
	return Arrays.equals(m_address, other.m_address);
    }   
}
