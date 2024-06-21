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

import java.io.OutputStream;

import osak.ext.communication.MyLog;
import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.Ipv4Address;
import osak.ext.ns3.network.utils.QueueDiscItem;

/**
 * TODO ArpQueueDiscItem
 * 
 * @author zhangrui
 * @since   1.0
 */
public class ArpQueueDiscItem extends QueueDiscItem {
    private ArpHeader m_header;
    private boolean m_headerAdded;

    /**
     * @brief Create an ARP queue disc item containing an ARP packet.
     * @param p        the packet included in the created item.
     * @param addr     the destination MAC address
     * @param protocol the protocol number
     * @param header   the ARP header
     */
    public ArpQueueDiscItem(Packet p, final Address addr, short protocol, final ArpHeader header) {
	super(p, addr, protocol);
	m_header = header;
	m_headerAdded = false;
    }

    /**
     * @return the correct packet size (header plus payload).
     */
    @Override
    public int GetSize() {
	Packet p = GetPacket();
	assert (p != null);
	int ret = p.GetSize();
	if (!m_headerAdded) {
	    ret += m_header.GetSerializedSize();
	}
	return ret;
    }

    /**
     * @return the header stored in this item..
     */
    public ArpHeader GetHeader() {
	return m_header;
    }

    /**
     * @brief Add the header to the packet
     */
    @Override
    public void AddHeader() {
	Packet p = GetPacket();
	assert (p != null);
	p.AddHeader(m_header);
	m_headerAdded = true;
    }

    /**
     * @brief Print the item contents.
     * @param os output stream in which the data should be printed.
     */
    @Override
    public void Print(OutputStream os) {

    }

    /**
     * @brief Inherited from the base class, but we cannot mark ARP packets
     * @return false
     */
    @Override
    public boolean Mark() {
	return false;
    }

    /**
     * @brief Computes the hash of the packet's 5-tuple
     *
     * @param perturbation hash perturbation value
     * @return the hash of the packet's 5-tuple
     */
    @Override
    public int Hash(int perturbation) {
	Ipv4Address ipv4Src = m_header.GetSourceIpv4Address();
	Ipv4Address ipv4Dst = m_header.GetDestinationIpv4Address();
	Address macSrc = m_header.GetSourceHardwareAddress();
	Address macDst = m_header.GetDestinationHardwareAddress();
	byte type = m_header.IsRequest() ? (byte) 1 : (byte) 2;

	/* serialize the addresses and the perturbation in buf */
	byte tmp = (byte) (8 + macSrc.GetLength() + macDst.GetLength());
	byte[] buf = new byte[tmp + 5];
	ipv4Src.Serialize(buf);
	ipv4Dst.Serialize(buf, 4);
	macSrc.CopyTo(buf, 8);
	macDst.CopyTo(buf, 8 + macSrc.GetLength());
	buf[tmp] = type;
	buf[tmp + 1] = (byte) ((perturbation >>> 24) & 0xff);
	buf[tmp + 2] = (byte) ((perturbation >>> 16) & 0xff);
	buf[tmp + 3] = (byte) ((perturbation >>> 8) & 0xff);
	buf[tmp + 4] = (byte) (perturbation & 0xff);

	// Linux calculates jhash2 (jenkins hash), we calculate murmur3 because it is
	// already available in ns-3

	int hash = buf.hashCode();

	MyLog.logOut("Hash value " + hash, MyLog.DEBUG);

	return hash;
    }
}
