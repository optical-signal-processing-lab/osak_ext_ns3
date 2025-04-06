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
import osak.ext.communication.MyLog;
import osak.ext.ns3.network.Header;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4Header
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4Header implements Header {
    boolean m_calcChecksum = false; // !< true if the checksum must be calculated

    short m_payloadSize = 0; // !< payload size
    short m_identification = 0; // !< identification
    byte m_tos = 0; // !< TOS, also used as DSCP + ECN value
    byte m_ttl = 0; // !< TTL
    byte m_protocol = 0; // !< Protocol
    byte m_flags = 0; // !< flags
    short m_fragmentOffset = 0; // !< Fragment offset
    Ipv4Address m_source; // !< source address
    Ipv4Address m_destination; // !< destination address
    short m_checksum = 0; // !< checksum
    boolean m_goodChecksum = true; // !< true if checksum is correct
    short m_headerSize = 5 * 4; // !< IP header size

    /**
     * Construct a null IPv4 header
     */
    public Ipv4Header() {
    }

    /**
     * Enable checksum calculation for this header.
     */
    public void EnableChecksum() {
	m_calcChecksum = true;
    }

    /**
     * @param size the size of the payload in bytes
     */
    public void SetPayloadSize(short size) {
	m_payloadSize = size;
    }

    /**
     * @param identification the Identification field of IPv4 packets.
     *
     *                       By default, set to zero.
     */
    public void SetIdentification(short identification) {
	m_identification = identification;
    }

    /**
     * @param tos the 8 bits of Ipv4 TOS.
     */
    public void SetTos(byte tos) {
	m_tos = tos;
    }

    /**
     * Set DSCP Field
     * 
     * @param dscp DSCP value
     */
    public void SetDscp(DscpType dscp) {
	m_tos &= 0x3;// Clear out the DSCP part, retain 2 bits of ECN
	m_tos |= (dscp.value() << 2);
    }
    
    /**
     * Set ECN Field
     * 
     * @param ecn ECN Type
     */
    public void SetEcn(EcnType ecn) {
	m_tos &= 0xFC;// Clear out the ECN part, retain 6 bits of DSCP
	m_tos |= ecn.value();
    }

    /// flags related to IP fragmentation
    private static final byte DONT_FRAGMENT = (1 << 0);
    private static final byte MORE_FRAGMENTS = (1 << 1);

    /**
     * This packet is not the last packet of a fragmented ipv4 packet.
     */
    public void SetMoreFragments() {
	m_flags |= MORE_FRAGMENTS;
    }

    /**
     * This packet is the last packet of a fragmented ipv4 packet.
     */
    public void SetLastFragment() {
	m_flags &= ~MORE_FRAGMENTS;
    }

    /**
     * Don't fragment this packet: if you need to anyway, drop it.
     */
    public void SetDontFragment() {
	m_flags |= DONT_FRAGMENT;
    }

    /**
     * If you need to fragment this packet, you can do it.
     */
    public void SetMayFragment() {
	m_flags &= ~DONT_FRAGMENT;
    }

    /**
     * The offset is measured in bytes for the packet start. Mind that IPv4
     * "fragment offset" field is 13 bits long and is measured in 8-bytes words.
     * Hence, the function does enforce that the offset is a multiple of 8.
     * 
     * @param offsetBytes the ipv4 fragment offset measured in bytes from the start.
     */
    public void SetFragmentOffset(short offsetBytes) {
	m_fragmentOffset = offsetBytes;
    }

    /**
     * @param ttl the ipv4 TTL
     */
    public void SetTtl(byte ttl) {
	m_ttl = ttl;
    }

    /**
     * @param num the ipv4 protocol field
     */
    public void SetProtocol(byte protocol) {
	m_protocol = protocol;
    }

    /**
     * @param source the source of this packet
     */
    public void SetSource(Ipv4Address source) {
	m_source = source;
    }

    /**
     * @param destination the destination of this packet.
     */
    public void SetDestination(Ipv4Address destination) {
	m_destination = destination;
    }

    /**
     * @returns the size of the payload in bytes
     */
    public short GetPayloadSize() {
	return m_payloadSize;
    }

    /**
     * @returns the identification field of this packet.
     */
    public short GetIdentification() {
	return m_identification;
    }

    /**
     * @returns the TOS field of this packet.
     */
    public byte GetTos() {
	return m_tos;
    }

    /**
     * @returns the DSCP field of this packet.
     */
    public DscpType GetDscp() {
	return DscpType.valueOf((byte) ((m_tos & 0xFC) >>> 2));
    }

    /**
     * @param dscp the dscp
     * @returns String of DSCPType
     */
    public String DscpTypeToString(DscpType dscp) {
	switch (dscp) {
	case DscpDefault:
	    return "Default";
	case DSCP_CS1:
	    return "CS1";
	case DSCP_AF11:
	    return "AF11";
	case DSCP_AF12:
	    return "AF12";
	case DSCP_AF13:
	    return "AF13";
	case DSCP_CS2:
	    return "CS2";
	case DSCP_AF21:
	    return "AF21";
	case DSCP_AF22:
	    return "AF22";
	case DSCP_AF23:
	    return "AF23";
	case DSCP_CS3:
	    return "CS3";
	case DSCP_AF31:
	    return "AF31";
	case DSCP_AF32:
	    return "AF32";
	case DSCP_AF33:
	    return "AF33";
	case DSCP_CS4:
	    return "CS4";
	case DSCP_AF41:
	    return "AF41";
	case DSCP_AF42:
	    return "AF42";
	case DSCP_AF43:
	    return "AF43";
	case DSCP_CS5:
	    return "CS5";
	case DSCP_EF:
	    return "EF";
	case DSCP_CS6:
	    return "CS6";
	case DSCP_CS7:
	    return "CS7";
	default:
	    return "Unrecognized DSCP";
	}
    }

    /**
     * @returns the ECN field of this packet.
     */
    public EcnType GetEcn() {
	return EcnType.valueOf((byte) (m_tos & 0x3));
    }

    /**
     * @param ecn the ECNType
     * @returns String of ECNType
     */
    public String EcnTypeToString(EcnType ecn) {
	switch (ecn) {
	case ECN_NotECT:
	    return "Not-ECT";
	case ECN_ECT1:
	    return "ECT (1)";
	case ECN_ECT0:
	    return "ECT (0)";
	case ECN_CE:
	    return "CE";
	default:
	    return "Unknown ECN";
	}
    }

    /**
     * @returns true if this is the last fragment of a packet, false otherwise.
     */
    public boolean IsLastFragment() {
	return !((m_flags & MORE_FRAGMENTS) > 0);
    }

    /**
     * @returns true if this is this packet can be fragmented.
     */
    public boolean IsDontFragment() {
	return (m_flags & DONT_FRAGMENT) > 0;
    }

    /**
     * @returns the offset of this fragment measured in bytes from the start.
     */
    public short GetFragmentOffset() throws Exception {
	if (m_fragmentOffset + m_payloadSize > 65535 - 5 * 4) {
	    throw new Exception("Fragment will exceed the maximum packet size once reassembled");
	}
	return m_fragmentOffset;
    }

    /**
     * @returns the TTL field of this packet
     */
    public byte GetTtl() {
	return m_ttl;
    }

    /**
     * @returns the protocol field of this packet
     */
    public byte GetProtocol() {
	return m_protocol;
    }

    /**
     * @returns the source address of this packet
     */
    public Ipv4Address GetSource() {
	return m_source;
    }

    /**
     * @returns the destination address of this packet
     */
    public Ipv4Address GetDestination() {
	return m_destination;
    }

    /**
     * @returns true if the ipv4 checksum is correct, false otherwise.
     *
     *          If Ipv4Header::EnableChecksums has not been called prior to
     *          deserializing this header, this method will always return true.
     */
    public boolean IsChecksumOk() {
	return m_goodChecksum;
    }

    @Override
    public int GetSerializedSize() {
	return m_headerSize;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	// int start = buffer.position();
	byte verIhl = (4 << 4) | 5;
	buffer.put(verIhl);
	buffer.put(m_tos);
	buffer.putShort((short) (m_payloadSize + m_headerSize));
	buffer.putShort(m_identification);
	int fragmentOffset = m_fragmentOffset / 8;
	byte flagsFrag = (byte) ((fragmentOffset >>> 8) & 0x1f);
	if ((m_flags & DONT_FRAGMENT) > 0) {
	    flagsFrag |= (1 << 6);
	}
	if ((m_flags & MORE_FRAGMENTS) > 0) {
	    flagsFrag |= (1 << 5);
	}
	buffer.put(flagsFrag);
	byte frag = (byte) (fragmentOffset & 0xff);
	buffer.put(frag);
	buffer.put(m_ttl);
	buffer.put(m_protocol);
	buffer.putShort((short) 0); // checksum
	buffer.putInt(m_source.Get());
	buffer.putInt(m_destination.Get());

	// 此处不需要
	// if (m_calcChecksum) {
	// int end = buffer.position();
	// uint16_t checksum = i.CalculateIpChecksum(20);
	// buffer.position(start+10);
	// buffer.putShort(checksum);
	// }

    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	byte verIhl = buffer.get();
	byte ihl = (byte) (verIhl & 0x0f);
	short headerSize = (short) (ihl * 4);

	if ((verIhl >>> 4) != 4) {
	    MyLog.logOut("Deserialize", "Trying to decode a non-IPv4 header, refusing to do it.", 3);
	    return 0;
	}

	m_tos = buffer.get();
	short size = buffer.getShort();
	m_payloadSize = (short) (size - headerSize);
	m_identification = buffer.getShort();
	byte flags = buffer.get();
	m_flags = 0;
	if ((flags & (1 << 6)) > 0) {
	    m_flags |= DONT_FRAGMENT;
	}
	if ((flags & (1 << 5)) > 0) {
	    m_flags |= MORE_FRAGMENTS;
	}
	m_fragmentOffset = (short) (flags & 0x1f);
	m_fragmentOffset <<= 8;
	m_fragmentOffset |= buffer.get();
	m_fragmentOffset <<= 3;
	m_ttl = buffer.get();
	m_protocol = buffer.get();
	m_checksum = buffer.getShort();

	byte[] dst = new byte[4];
	buffer.get(dst);
	m_source = new Ipv4Address(Helper.byte2int(dst));

	buffer.get(dst);
	m_destination = new Ipv4Address(Helper.byte2int(dst));

	m_headerSize = headerSize;
	return buffer.position() - start;
    }

}
