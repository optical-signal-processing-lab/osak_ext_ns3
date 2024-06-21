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
package osak.ext.ns3.aodv;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import osak.ext.communication.Helper;
import osak.ext.ns3.network.Header;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * Route Error (RERR) Message Format
 * 
 * <pre>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Type      |N|          Reserved           |   DestCount   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |            Unreachable Destination IP Address (1)             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |         Unreachable Destination Sequence Number (1)           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
 * |  Additional Unreachable Destination IP Addresses (if needed)  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |Additional Unreachable Destination Sequence Numbers (if needed)|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class RerrHeader implements Header {
    private byte m_flag = 0;/// < No delete flag
    private byte m_reserved = 0;/// < Not used (must be 0)
    /// List of Unreachable destination: IP addresses and sequence numbers
    private Map<Ipv4Address, Integer> m_unreachableDstSeqNo = new HashMap<>();

    public RerrHeader() {
    }

    @Override
    public int GetSerializedSize() {
	return (3 + 8 * GetDestCount());
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_flag);
	buffer.put(m_reserved);
	buffer.put(GetDestCount());
	for (Ipv4Address key : m_unreachableDstSeqNo.keySet()) {
	    buffer.putInt(key.Get());
	    buffer.putInt(m_unreachableDstSeqNo.get(key));
	}
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	m_flag = buffer.get();
	m_reserved = buffer.get();
	byte dest = buffer.get();
	m_unreachableDstSeqNo.clear();
	Ipv4Address address;
	int seqNo;
	byte[] addr = new byte[4];
	for (byte k = 0; k < dest; ++k) {
	    buffer.get(addr);
	    address = new Ipv4Address(Helper.byte2int(addr));
	    seqNo = buffer.getInt();
	    m_unreachableDstSeqNo.put(address, seqNo);
	}
	return buffer.position() - start;
    }

    // No delete flag
    /**
     * Set the no delete flag
     * 
     * @param f the no delete flag
     * @bug in osak.ext.ns3 (1<<0)
     */
    public void SetNoDelete(boolean f) {
	if (f) {
	    m_flag |= (1 << 7);
	} else {
	    m_flag &= ~(1 << 7);
	}
    }

    /**
     * Get the no delete flag
     * 
     * @return the no delete flag
     * @bug in osak.ext.ns3
     */
    public boolean GetNoDelete() {
	return (m_flag & (1 << 7)) > 0;
    }

    /**
     * Add unreachable node address and its sequence number in RERR header
     * 
     * @param dst   unreachable IPv4 address
     * @param seqNo unreachable sequence number
     * @return false if we already added maximum possible number of unreachable
     *         destinations
     */
    public boolean AddUnDestination(Ipv4Address dst, int seqNo) throws RuntimeException {
	if (m_unreachableDstSeqNo.containsKey(dst)) {
	    return true;
	}
	if (GetDestCount() >= 255)
	    throw new RuntimeException("Can't support more than 255 destinations in single RERR");
	m_unreachableDstSeqNo.put(dst, seqNo);
	return true;
    }

    /**
     * Delete pair (address + sequence number) from REER header, if the number of
     * unreachable destinations > 0
     * 
     * @param unAddress unreachable address
     * @param un        unreachable sequence number
     * @return true on success
     */
    public boolean RemoveUnDestination(Ipv4Address unAddress, int un) {
	if (m_unreachableDstSeqNo.isEmpty()) {
	    return false;
	}
	return m_unreachableDstSeqNo.remove(unAddress, un);

    }
    /// Clear header
    public void Clear() {
	m_unreachableDstSeqNo.clear();
	m_flag = 0;
	m_reserved = 0;
    }

    /**
     * @return number of unreachable destinations in RERR message
     */
    public byte GetDestCount() {
	return (byte) m_unreachableDstSeqNo.size();
    }

    @Override
    public String toString() {
	String str1 = "Unreachable destination (ipv4 address, seq. number):";
	String str2 = "";
	for (Ipv4Address key : m_unreachableDstSeqNo.keySet()) {
	    String str3 = new Ipv4Address(key) + ", " + m_unreachableDstSeqNo.get(key);
	    str2 += str3;
	}
	String str4 = "No delete flag " + GetNoDelete();
	return str1 + str2 + str4;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof RerrHeader) {
	    RerrHeader o = (RerrHeader) obj;
	    if (m_flag != o.m_flag || m_reserved != o.m_reserved || GetDestCount() != o.GetDestCount()) {
		return false;
	    }
	    return m_unreachableDstSeqNo.equals(o.m_unreachableDstSeqNo);
	}
	return false;
    }
}
