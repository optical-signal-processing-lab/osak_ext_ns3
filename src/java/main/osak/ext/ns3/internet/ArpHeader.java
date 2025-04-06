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

import java.nio.ByteBuffer;

import osak.ext.communication.Helper;
import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.Header;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO ArpHeader
 * 
 * @author zhangrui
 * @since   1.0
 */
public class ArpHeader implements Header {
    public ArpType_e m_type; // !< type of the ICMP (ARP_TYPE_REQUEST)
    public Address m_macSource; // !< hardware source address
    public Address m_macDest; // !< hardware destination address
    public Ipv4Address m_ipv4Source; // !< IP source address
    public Ipv4Address m_ipv4Dest; // !< IP destination address

    /**
     * @brief Enumeration listing the possible ARP types
     */
    public enum ArpType_e {
	ARP_TYPE_REQUEST, ARP_TYPE_REPLY
    };

    /**
     * @brief Set the ARP request parameters
     * @param sourceHardwareAddress      the source hardware address
     * @param sourceProtocolAddress      the source IP address
     * @param destinationHardwareAddress the destination hardware address (usually
     *                                   the broadcast address)
     * @param destinationProtocolAddress the destination IP address
     */
    public void SetRequest(Address sourceHardwareAddress, Ipv4Address sourceProtocolAddress,
	    Address destinationHardwareAddress, Ipv4Address destinationProtocolAddress) {
	m_type = ArpType_e.ARP_TYPE_REQUEST;
	m_macSource = sourceHardwareAddress;
	m_macDest = destinationHardwareAddress;
	m_ipv4Source = sourceProtocolAddress;
	m_ipv4Dest = destinationProtocolAddress;
    }

    /**
     * @brief Set the ARP reply parameters
     * @param sourceHardwareAddress      the source hardware address
     * @param sourceProtocolAddress      the source IP address
     * @param destinationHardwareAddress the destination hardware address (usually
     *                                   the broadcast address)
     * @param destinationProtocolAddress the destination IP address
     */
    public void SetReply(Address sourceHardwareAddress, Ipv4Address sourceProtocolAddress,
	    Address destinationHardwareAddress,
	    Ipv4Address destinationProtocolAddress) {
	m_type = ArpType_e.ARP_TYPE_REPLY;
	m_macSource = sourceHardwareAddress;
	m_macDest = destinationHardwareAddress;
	m_ipv4Source = sourceProtocolAddress;
	m_ipv4Dest = destinationProtocolAddress;
    }

    /**
     * @brief Check if the ARP is a request
     * @returns true if it is a request
     */
    public boolean IsRequest() {
	return m_type == ArpType_e.ARP_TYPE_REQUEST;
    }

    /**
     * @brief Check if the ARP is a reply
     * @returns true if it is a reply
     */
    public boolean IsReply() {
	return m_type == ArpType_e.ARP_TYPE_REPLY;
    }

    public ArpType_e GetType() {
	return m_type;
    }

    /**
     * @brief Returns the source hardware address
     * @returns the source hardware address
     */
    public Address GetSourceHardwareAddress() {
	return m_macSource;
    }

    /**
     * @brief Returns the destination hardware address
     * @returns the destination hardware address
     */
    public Address GetDestinationHardwareAddress() {
	return m_macDest;
    }

    /**
     * @brief Returns the source IP address
     * @returns the source IP address
     */
    public Ipv4Address GetSourceIpv4Address() {
	return m_ipv4Source;
    }

    /**
     * @brief Returns the destination IP address
     * @returns the destination IP address
     */
    public Ipv4Address GetDestinationIpv4Address() {
	return m_ipv4Dest;
    }



    @Override
    public int GetSerializedSize() {
	assert ((m_macSource.GetLength() == 6) || (m_macSource.GetLength() == 8) || (m_macSource.GetLength() == 1));
	assert (m_macSource.GetLength() == m_macDest.GetLength());
	int length = 16;// Length minus two hardware addresses
	length += m_macSource.GetLength() * 2;
	return length;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	assert (m_macSource.GetLength() == m_macDest.GetLength());
	/* ethernet */
	buffer.putShort((short) 0x0001);
	/* ipv4 */
	buffer.putShort((short) 0x0800);
	buffer.put(m_macSource.GetLength());
	buffer.put((byte)4);

	switch(m_type) {
	case ARP_TYPE_REQUEST:
	    buffer.putShort((short) 1);
	    break;
	case ARP_TYPE_REPLY:
	    buffer.putShort((short) 2);
	    break;
	}

	byte[] macSource = new byte[m_macSource.GetLength()];
	m_macSource.CopyTo(macSource);
	buffer.put(macSource);
	buffer.putInt(m_ipv4Source.Get());
	
	byte[] macDest = new byte[m_macDest.GetLength()];
	m_macSource.CopyTo(macDest);
	buffer.put(macDest);
	buffer.putInt(m_ipv4Dest.Get());
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	buffer.getShort();// Skip HRD
	int protocolType = buffer.getShort();// Read PRO
	byte hardwareAddressLen = buffer.get();// Read HLN
	byte protocolAddressLen = buffer.get();// Read PLN

	//
	// It is implicit here that we have a protocol type of 0x800 (IP).
	// It is also implicit here that we are using Ipv4 (PLN == 4).
	// If this isn't the case, we need to return an error since we don't want to
	// be too fragile if we get connected to real networks.
	//
	if (protocolType != 0x800 || protocolAddressLen != 4) {
	    return 0;
	}

	// Read OP
	short type = buffer.getShort();
	if (type == 1)
	    m_type = ArpType_e.ARP_TYPE_REQUEST;
	else if (type == 2)
	    m_type = ArpType_e.ARP_TYPE_REPLY;

	byte[] macdata = new byte[hardwareAddressLen];
	byte[] ipdata = new byte[protocolAddressLen];

	// Read SHA (size HLN)
	buffer.get(macdata);
	m_macSource.CopyFrom(macdata, hardwareAddressLen);

	// Read SPA (size PLN == 4)
	buffer.get(ipdata);
	m_ipv4Source.Set(Helper.byte2int(ipdata));

	// Read THA (size HLN)
	buffer.get(macdata);
	m_macDest.CopyFrom(macdata, hardwareAddressLen);

	// Read TPA (size PLN == 4)
	buffer.get(ipdata);
	m_ipv4Dest.Set(Helper.byte2int(ipdata));

	return buffer.position() - start;
    }

}
