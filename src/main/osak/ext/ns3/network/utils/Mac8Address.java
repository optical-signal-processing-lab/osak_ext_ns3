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
 * TODO Mac8Address
 * 
 * @author lijianjun
 * @since   1.0
 */
public class Mac8Address{
    private static long m_allocationIndex; // !< Address allocation index
    private byte m_address;// !< address value

    public Mac8Address() {
	m_address = (byte)0xff;
    }

    public Mac8Address(byte addr) {
 	m_address = addr;
     }

    private static byte type = Address.Register();
    private static byte GetType() {
	return type;
    }
    
    /**
     * Convert an instance of this class to a polymorphic Address instance.
     * 
     * @returns a new Address instance
     *
     */
    public Address ConvertTo() {
	return new Address(GetType(), m_address, (byte) 1);
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
     * @returns a new Mac8Address from the polymorphic address
     *
     *          This function performs a type check and asserts if the type of the
     *          input address is not compatible with an Mac8Address.
     */
    public static Mac8Address ConvertFrom(final Address address) {
	Mac8Address retval = new Mac8Address();
	address.CopyTo(retval.m_address);
	return retval;
    }
    
    /**
     * @param address address to test
     * @returns true if the address matches, false otherwise.
     */
    public static boolean IsMatchingType(final Address address) {
	return address.CheckCompatible(GetType(), (byte) 1);
    }

    
    /**
     * Copy the input address to our internal buffer.
     * 
     * @param buffer address in network order
     *
     */
    public void CopyFrom(final byte buffer) {
	m_address = buffer;
    }

    /**
     * Copy the internal address to the input buffer.
     * @param buffer address in network order
     */
    public void CopyTo(byte buffer) {
	buffer = m_address;
    }

    /**
     * @returns the broadcast address
     */
    private static Mac8Address broadcast = new Mac8Address();
    public static Mac8Address GetBroadcast() {
	return broadcast;
    }
   
    /**
     * Allocate a new Mac8Address.
     * 
     * @returns newly allocated mac8Address
     */
    public static Mac8Address Allocate() {
	byte address = (byte)m_allocationIndex++;
	if(m_allocationIndex == 0xff) {
	    m_allocationIndex = 0;
	}
	Mac8Address result = new Mac8Address(address);
	return result;
    }

    /**
     * Reset the Mac8Address allocation index.
     *
     * This function resets (to zero) the global integer that is used for unique
     * address allocation. It is automatically called whenever \code
     * SimulatorDestroy (); \endcode is called. It may also be optionally called by
     * user code if there is a need to force a reset of this allocation index.
     */
    public static void ResetAllocationIndex() {
	m_allocationIndex = 0;
    }

    @Override
    public int hashCode() {
	return Objects.hash(m_address);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Mac8Address other = (Mac8Address) obj;
	return this.m_address == other.m_address;
    }
}
