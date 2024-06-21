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

import osak.ext.communication.Helper;
import osak.ext.ns3.network.Header;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * RreqHeader Route Request (RREQ) Message Format
 * 
 * <pre>
  0                   1                   2                   3
  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |     Type      |J|R|G|D|U|   Reserved          |   Hop Count   |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                            RREQ ID                            |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                    Destination IP Address                     |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                  Destination Sequence Number                  |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                    Originator IP Address                      |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                  Originator Sequence Number                   |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class RreqHeader implements Header {
    /**
     * constructor
     *
     * @param flags       the message flags (0)
     * @param reserved    the reserved bits (0)
     * @param hopCount    the hop count
     * @param requestID   the request ID
     * @param dst         the destination IP address
     * @param dstSeqNo    the destination sequence number
     * @param origin      the origin IP address
     * @param originSeqNo the origin sequence number
     */
    private byte m_flags = 0; /// < |J|R|G|D|U| bit flags, see RFC
    private byte m_reserved = 0; /// < Not used (must be 0)
    private byte m_hopCount = 0; /// < Hop Count
    private int m_requestID = 0; /// < RREQ ID
    private Ipv4Address m_dst; /// < Destination IP Address
    private int m_dstSeqNo = 0; /// < Destination Sequence Number
    private Ipv4Address m_origin; /// < Originator IP Address
    private int m_originSeqNo = 0; /// < Source Sequence Number

    public RreqHeader() {
    }

    @Override
    public int GetSerializedSize() {
	return 23;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_flags);
	buffer.put(m_reserved);
	buffer.put(m_hopCount);
	buffer.putInt(m_requestID); // BIGEND
	buffer.putInt(m_dst.Get());
	buffer.putInt(m_dstSeqNo);
	buffer.putInt(m_origin.Get());
	buffer.putInt(m_originSeqNo);
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	m_flags = buffer.get();
	m_reserved = buffer.get();
	m_hopCount = buffer.get();
	m_requestID = buffer.getInt();
	byte[] dst = new byte[4];
	buffer.get(dst);
	m_dst = new Ipv4Address(Helper.byte2int(dst));
	m_dstSeqNo = buffer.getInt();
	buffer.get(dst);
	m_origin = new Ipv4Address(Helper.byte2int(dst));
	m_originSeqNo = buffer.getInt();
	return buffer.position() - start;
    }

    @Override
    public String toString() {
	return "RREQ ID " + m_requestID + " destination: ipv4 " + m_dst + " sequence number " + m_dstSeqNo
		+ " source: ipv4 " + m_origin + " sequence number " + m_originSeqNo + " flags:" + " Gratuitous RREP "
		+ GetGratuitousRrep() + " Destination only " + GetDestinationOnly() + " Unknown sequence number "
		+ GetUnknownSeqno();
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
     * Set the request ID
     * 
     * @param id the request ID
     */
    public void SetId(int id) {
	m_requestID = id;
    }

    /**
     * Get the request ID
     * 
     * @return the request ID
     */
    public int GetId() {
	return m_requestID;
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
     * Set the origin sequence number
     * 
     * @param s the origin sequence number
     */
    public void SetOriginSeqno(int s) {
	m_originSeqNo = s;
    }

    /**
     * Get the origin sequence number
     * 
     * @return the origin sequence number
     */
    public int GetOriginSeqno() {
	return m_originSeqNo;
    }

    // Flags
    /**
     * Set the gratuitous RREP flag
     * 
     * @param f the gratuitous RREP flag
     */
    public void SetGratuitousRrep(boolean f) {
	if (f) {
	    m_flags |= (1 << 5);
	} else {
	    m_flags &= ~(1 << 5);
	}
    }

    /**
     * Get the gratuitous RREP flag
     * 
     * @return the gratuitous RREP flag
     */
    public boolean GetGratuitousRrep() {
	return (m_flags & (1 << 5)) > 0;
    }

    /**
     * Set the Destination only flag
     * 
     * @param f the Destination only flag
     */
    public void SetDestinationOnly(boolean f) {
	if (f) {
	    m_flags |= (1 << 4);
	} else {
	    m_flags &= ~(1 << 4);
	}
    };

    /**
     * Get the Destination only flag
     * 
     * @return the Destination only flag
     */
    public boolean GetDestinationOnly() {
	return (m_flags & (1 << 4)) > 0;
    };

    /**
     * Set the unknown sequence number flag
     * 
     * @param f the unknown sequence number flag
     */
    public void SetUnknownSeqno(boolean f) {
	if (f) {
	    m_flags |= (1 << 3);
	} else {
	    m_flags &= ~(1 << 3);
	}
    };

    /**
     * Get the unknown sequence number flag
     * 
     * @return the unknown sequence number flag
     */
    public boolean GetUnknownSeqno() {
	return (m_flags & (1 << 3)) > 0;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof RreqHeader) {
	    RreqHeader o = (RreqHeader) obj;
	    if (m_flags == o.m_flags && m_reserved == o.m_reserved && m_hopCount == o.m_hopCount
		    && m_requestID == o.m_requestID && m_dst.equals(o.m_dst) && m_dstSeqNo == o.m_dstSeqNo
		    && m_origin.equals(o.m_origin) && m_originSeqNo == o.m_originSeqNo) {
		return true;
	    }
	    return false;
	}
	return false;
    }
}
