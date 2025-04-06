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
package osak.ext.ns3.network;

import java.nio.ByteBuffer;

/**
 * ingroup: network <p>
 * defgroup: address Address<p>
 *
 * Network Address abstractions, including MAC, IPv4 and IPv6.
 * <p>
 * 
 * ingroup: address a polymophic address class
 * <p>
 *
 * This class is very similar in design and spirit to the BSD sockaddr
 * structure: they are both used to hold multiple types of addresses together
 * with the type of the address.
 * 
 * <pre>
 * A new address class defined by a user needs to:
 *   - allocate a type id with Address::Register
 *   - provide a method to convert his new address to an Address
 *     instance. This method is typically a member method named ConvertTo:
 *     Address MyAddress::ConvertTo () const;
 *   - provide a method to convert an Address instance back to
 *     an instance of his new address type. This method is typically
 *     a static member method of his address class named ConvertFrom:
 *     static MyAddress MyAddress::ConvertFrom (const Address &address);
 *   - the ConvertFrom method is expected to check that the type of the
 *     input Address instance is compatible with its own type.
 * </pre>
 * 
 * Typical code to create a new class type looks like:
 * 
 * <pre>
 * // this class represents addresses which are 2 bytes long.
 * class MyAddress
 * {
 * public:
 *   Address ConvertTo () const;
 *   static MyAddress ConvertFrom ();
 * private:
 *   static uint8_t GetType ();
 * };
 *
 * Address MyAddress::ConvertTo () const
 * {
 *   return Address (GetType (), m_buffer, 2);
 * }
 * MyAddress MyAddress::ConvertFrom (const Address &address)
 * {
 *   MyAddress ad;
 *   NS_ASSERT (address.CheckCompatible (GetType (), 2));
 *   address.CopyTo (ad.m_buffer, 2);
 *   return ad;
 * }
 * uint8_t MyAddress::GetType ()
 * {
 *   static uint8_t type = Address::Register ();
 *   return type;
 * }
 * </pre>
 * 
 *
 * To convert a specific Address T (e.g., Ipv6Address) to and from an Address
 * type, a class must implement three public functions:
 * 
 * <pre>
 * static T ConvertFrom(const Address& address);
 * Address ConvertTo() const;
 * operator Address() const;
 * </pre>
 *
 * @see attribute_Address
 * 
 * @author zhangrui
 * @since 1.0
 */
public class Address {
    public static final int MAX_SIZE = 20;

    /**
     * Create an invalid address
     */
    public Address() {

    }

    /**
     * Create an address from a type and a buffer.
     *
     * This constructor is typically invoked from the conversion functions of
     * various address types when they have to convert themselves to an Address
     * instance.
     *
     * @param type   the type of the Address to create
     * @param buffer a pointer to a buffer of bytes which hold a serialized
     *               representation of the address in network byte order.
     * @param len    the length of the buffer.
     */
    public Address(byte type, final byte[] buffer, byte len) {
	m_type = type;
	m_len = len;
	assert (m_len <= MAX_SIZE);
	System.arraycopy(buffer, 0, m_data, 0, m_len);
    }
    
    //TODO:modify
    public Address(byte type, final byte buffer, byte len) {
	m_type = type;
	m_len = len;
	assert (m_len <= MAX_SIZE);
	System.arraycopy(buffer, 0, m_data, 0, m_len);
    }

    /**
     * Create an address from another address.
     * 
     * @param address the address to copy
     */
    // copy
    public Address(final Address address) {
	m_type = address.m_type;
	m_len = address.m_len;
	System.arraycopy(address.m_data, 0, m_data, 0, address.m_len);
    }
    /**
     *  Basic assignment operator.
     * @param address the address to copy
     * @returns the address
     */
    // java中不存在
    // public Address copy(const Address& address);

    /**
     * @returns true if this address is invalid, false otherwise.
     *
     *          An address is invalid if and only if it was created through the
     *          default constructor and it was never re-initialized.
     */
    public boolean IsInvalid() {
	return m_len == 0 && m_type == 0;

    }

    /**
     * Get the length of the underlying address.
     * 
     * @returns the length of the underlying address.
     */
    public byte GetLength() {
	assert (m_len <= MAX_SIZE);
	return m_len;
    }

    /**
     *  Copy the address bytes into a buffer.
     * @param buffer buffer to copy the address bytes to.
     * @returns the number of bytes copied.
     */
    public int CopyTo(byte[] buffer) {
	assert (m_len <= MAX_SIZE);
	System.arraycopy(m_data, 0, buffer, 0, m_len);
	return m_len;
    }

    // TODO:modify
    public int CopyTo(byte buffer) {
	assert (m_len <= MAX_SIZE);
	System.arraycopy(m_data, 0, buffer, 0, m_len);
	return m_len;
    }
    
    public int CopyTo(byte[] buffer, int index) {
	assert (m_len <= MAX_SIZE);
	System.arraycopy(m_data, 0, buffer, index, m_len);
	return m_len;
    }

    /**
     * @param buffer buffer to copy the whole address data structure to
     * @param len    the size of the buffer
     * @returns the number of bytes copied.
     *
     *          Copies the type to buffer[0], the length of the address internal
     *          buffer to buffer[1] and copies the internal buffer starting at
     *          buffer[2]. len must be at least the size of the internal buffer plus
     *          a byte for the type and a byte for the length.
     */
    public int CopyAllTo(byte[] buffer, byte len) {
	assert (len - m_len > 1);
	buffer[0] = m_type;
	buffer[1] = m_len;
	System.arraycopy(m_data, 0, buffer, 2, m_len);
	return m_len + 2;
    }

    /**
     * @param buffer pointer to a buffer of bytes which contain a serialized
     *               representation of the address in network byte order.
     * @param len    length of buffer
     * @returns the number of bytes copied.
     *
     *          Copy the address bytes from buffer into to the internal buffer of
     *          this address instance.
     */
    public int CopyFrom(final byte[] buffer, byte len) {
	assert (m_len <= MAX_SIZE);
	System.arraycopy(buffer, 0, m_data, 0, len);
	m_len = len;
	return m_len;
    }

    /**
     * @param buffer pointer to a buffer of bytes which contain a copy of all the
     *               members of this Address class.
     * @param len    the length of the buffer
     * @returns the number of bytes copied.
     *
     *          The inverse of CopyAllTo().
     *
     * @see CopyAllTo
     */
    public int CopyAllFrom(final byte[] buffer, byte len) {
	assert (len >= 2);
	m_type = buffer[0];
	m_len = buffer[1];

	assert (len - m_len > 1);
	System.arraycopy(buffer, 2, m_data, 0, m_len);
	return m_len + 2;
    }

    /**
     * @param type a type id as returned by Address::Register
     * @param len  the length associated to this type id.
     *
     * @returns true if the type of the address stored internally is compatible with
     *          the requested type, false otherwise.
     */
    public boolean CheckCompatible(byte type, byte len) {
	assert (len <= MAX_SIZE);
	/// \internal
	/// Mac address type/length detection is discussed in \bugid{1568}
	return (m_len == len && m_type == type) || (m_len >= len && m_type == 0);
    }

    /**
     * @param type a type id as returned by Address::Register
     * @returns true if the type of the address stored internally is compatible with
     *          the requested type, false otherwise.
     *
     *          This method checks that the types are _exactly_ equal. This method
     *          is really used only by the PacketSocketAddress and there is little
     *          point in using it otherwise so, you have been warned: DO NOT USE
     *          THIS METHOD.
     */
    public boolean IsMatchingType(byte type) {
	return m_type == type;
    }

    /**
     * Allocate a new type id for a new type of address.
     * 
     * @returns a new type id.
     */
    public static byte Register() {
	type++;
	return type;
    }

    /**
     * Get the number of bytes needed to serialize the underlying Address Typically,
     * this is GetLength () + 2
     *
     * @returns the number of bytes required for an Address in serialized form
     */
    public int GetSerializedSize() {
	return 1 + 1 + m_len;
    }

    /**
     * Serialize this address in host byte order to a byte buffer
     *
     * @param buffer output buffer that gets written with this Address
     */
    // public void Serialize(TagBuffer buffer) {
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_type);
	buffer.put(m_len);
	buffer.put(m_data, 0, m_len);
    }

    /**
     * @param buffer buffer to read address from
     *
     *               The input address buffer is expected to be in host byte order
     *               format.
     */
    // public void Deserialize(TagBuffer buffer) {
    public void Deserialize(ByteBuffer buffer) {
	m_type = buffer.get();
	m_len = buffer.get();
	buffer.get(m_data, 0, m_len);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Address) {
	    Address o = (Address) obj;
	    return m_data == o.m_data && m_len == o.m_len;
	}
	return false;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Integer.hashCode(m_len);
	result = prime * result + m_data.hashCode();
	return result;
    }

    @Override
    public String toString() {
	if (m_len == 4) {
	    String res = "";
	    for (int i = 0; i < 3; i++) {
		String a = m_data[i] + ".";
		res += a;
	    }
	    res += m_data[3];
	    return res;
	} else {
	    String res = "";
	    for (int i = 0; i < m_len - 1; i++) {
		String a = String.format("%02x::", m_data[i]);
		res += a;
	    }
	    res += String.format("%02x", m_data[m_len - 1]);
	    return res;
	}
    }

    private byte m_type = 0;
    private byte m_len = 0;
    private byte[] m_data = new byte[MAX_SIZE];
    private static byte type = 1;
}
