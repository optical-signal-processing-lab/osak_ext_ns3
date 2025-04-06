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
import java.net.InetAddress;
import java.nio.ByteBuffer;

import osak.ext.communication.Helper;
import osak.ext.ns3.network.Header;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * Route Reply (RREP) Message Format
 * 
 * <pre>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Type      |R|A|    Reserved     |Prefix Sz|   Hop Count   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                     Destination IP address                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                  Destination Sequence Number                  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Originator IP address                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           Lifetime                            |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class RrepHeader implements Header {
    /**
     * constructor
     *
     * @param prefixSize the prefix size (0)
     * @param hopCount   the hop count (0)
     * @param dst        the destination IP address
     * @param dstSeqNo   the destination sequence number
     * @param origin     the origin IP address
     * @param lifetime   the lifetime
     */
    public RrepHeader(byte prefixSize, byte hopCount, Ipv4Address dst, int dstSeqNo, Ipv4Address origin,
	    int lifetime) {
	m_prefixSize = prefixSize;
	m_hopCount = hopCount;
	m_dst = dst;
	m_dstSeqNo = dstSeqNo;
	m_origin = origin;
	m_lifeTime = lifetime;
    }

    public RrepHeader(byte prefixSize, byte hopCount, InetAddress dst, int dstSeqNo, InetAddress origin,
	    int lifetime) {
	m_prefixSize = prefixSize;
	m_hopCount = hopCount;
	m_dst = new Ipv4Address(dst);
	m_dstSeqNo = dstSeqNo;
	m_origin = new Ipv4Address(origin);
	m_lifeTime = lifetime;
    }

    public RrepHeader() {
	m_prefixSize = 0;
	m_hopCount = 0;
	m_dst = new Ipv4Address();
	m_dstSeqNo = 0;
	m_origin = new Ipv4Address();
	m_lifeTime = 0;
    }
    private byte m_flags; /// < A - acknowledgment required flag
    private byte m_prefixSize; /// < Prefix Size
    private byte m_hopCount; /// < Hop Count
    private Ipv4Address m_dst; /// < Destination IP Address
    private int m_dstSeqNo; /// < Destination Sequence Number
    private Ipv4Address m_origin; /// < Source IP Address
    private int m_lifeTime; /// < Lifetime (in milliseconds)

    @Override
    public int GetSerializedSize() {
	return 19;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_flags);
	buffer.put(m_prefixSize);
	buffer.put(m_hopCount);
	buffer.putInt(m_dst.Get());
	buffer.putInt(m_dstSeqNo);
	buffer.putInt(m_origin.Get());
	buffer.putInt(m_lifeTime);
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	m_flags = buffer.get();
	m_prefixSize = buffer.get();
	m_hopCount = buffer.get();
	byte[] dst = new byte[4];
	buffer.get(dst);
	m_dst = new Ipv4Address(Helper.byte2int(dst));
	m_dstSeqNo = buffer.getInt();
	buffer.get(dst);
	m_origin = new Ipv4Address(Helper.byte2int(dst));
	m_lifeTime = buffer.getInt();

	return buffer.position() - start;
    }

    // Fields
    /**
     * Set the hop count
     * 
     * @param count the hop count
     */
    public void SetHopCount(byte count) {
	m_hopCount = count;
    }

    /**
     * Get the hop count
     * 
     * @return the hop count
     */
    public byte GetHopCount() {
	return m_hopCount;
    }

    /**
     * Set the destination address
     * 
     * @param a the destination address
     */
    public void SetDst(Ipv4Address a) {
	m_dst = a;
    }

    /**
     * Get the destination address
     * 
     * @return the destination address
     */
    public Ipv4Address GetDst() {
	return m_dst;
    }

    /**
     * Set the destination sequence number
     * 
     * @param s the destination sequence number
     */
    public void SetDstSeqno(int s) {
	m_dstSeqNo = s;
    }

    /**
     * Get the destination sequence number
     * 
     * @return the destination sequence number
     */
    public int GetDstSeqno() {
	return m_dstSeqNo;
    }

    /**
     * Set the origin address
     * 
     * @param a the origin address
     */
    public void SetOrigin(Ipv4Address a) {
	m_origin = a;
    }

    /**
     * Get the origin address
     * 
     * @return the origin address
     */
    public Ipv4Address GetOrigin() {
	return m_origin;
    }

    /**
     * Set the lifetime
     * 
     * @param t the lifetime
     */
    public void SetLifeTime(int t) {
	m_lifeTime = t;
    }

    /**
     * Get the lifetime
     * 
     * @return the lifetime
     */
    public int GetLifeTime() {
	return m_lifeTime;
    }

    // Flags
    /**
     * Set the ack required flag
     * 
     * @param f the ack required flag
     */
    public void SetAckRequired(boolean f) {
	if (f) {
	    m_flags |= (1 << 6);
	} else {
	    m_flags &= ~(1 << 6);
	}
    }

    /**
     * get the ack required flag
     * 
     * @return the ack required flag
     */
    public boolean GetAckRequired() {
	return (m_flags & (1 << 6)) > 0;
    }

    /**
     * Set the prefix size
     * 
     * @param sz the prefix size
     */
    public void SetPrefixSize(byte sz) {
	m_prefixSize = sz;
    }

    /**
     * Set the prefix size
     * 
     * @return the prefix size
     */
    public byte GetPrefixSize() {
	return m_prefixSize;
    }

    /**
     * Configure RREP to be a Hello message
     *
     * @param src      the source IP address
     * @param srcSeqNo the source sequence number
     * @param lifetime the lifetime of the message
     */
    public void SetHello(Ipv4Address src, int srcSeqNo, int lifetime) {
	m_flags = 0;
	m_prefixSize = 0;
	m_hopCount = 0;
	m_dst = src;
	m_dstSeqNo = srcSeqNo;
	m_origin = src;
	m_lifeTime = lifetime;
    }

    @Override
    public String toString() {
	String str1 = "destination: ipv4 " + m_dst + " sequence number " + m_dstSeqNo;
	String str2 = "";
	if (m_prefixSize != 0) {
	    str2 = " prefix size " + m_prefixSize;
	}
	String str3 = " source ipv4 " + m_origin + " lifetime " + m_lifeTime + " acknowledgment required flag "
		+ GetAckRequired();
	return str1 + str2 + str3;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof RrepHeader) {
	    RrepHeader o = (RrepHeader) obj;
	    if (m_flags == o.m_flags && m_prefixSize == o.m_prefixSize && m_hopCount == o.m_hopCount
		    && m_dst.equals(o.m_dst)
		    && m_dstSeqNo == o.m_dstSeqNo && m_origin.equals(o.m_origin) && m_lifeTime == o.m_lifeTime) {
		return true;
	    }
	    return false;
	}
	return false;
    }
}
