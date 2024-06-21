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

import osak.ext.ns3.network.Tag;
import osak.ext.ns3.network.TagBuffer;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4PacketInfoTag
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4PacketInfoTag implements Tag {

    // Linux IP_PKTINFO ip(7) implementation
    //
    // struct in_pktinfo {
    // unsigned int ipi_ifindex; /* Interface index */
    // struct in_addr ipi_spec_dst; /* Local address */
    // struct in_addr ipi_addr; /* Header Destination
    // address */
    // };

    private Ipv4Address m_addr; // !< Header destination address
    private int m_ifindex; // !< interface index

    // Used for IP_RECVTTL, though not implemented yet.
    private byte m_ttl; // !< Time to Live

    public Ipv4PacketInfoTag() {
	m_addr = new Ipv4Address();
	m_ifindex = 0;
	m_ttl = 0;
    }

    /**
     * \brief Set the tag's address
     *
     * \param addr the address
     */
    public void SetAddress(Ipv4Address addr) {
	m_addr = addr;
    }

    /**
     * \brief Get the tag's address
     *
     * \returns the address
     */
    public Ipv4Address GetAddress() {
	return m_addr;
    }

    /**
     * \brief Set the tag's receiving interface
     *
     * \param ifindex the interface index
     */
    public void SetRecvIf(int ifindex) {
	m_ifindex = ifindex;
    }

    /**
     * \brief Get the tag's receiving interface
     *
     * \returns the interface index
     */
    public int GetRecvIf() {
	return m_ifindex;
    }

    /**
     * \brief Set the tag's Time to Live Implemented, but not used in the stack yet
     * \param ttl the TTL
     */
    public void SetTtl(byte ttl) {
	m_ttl = ttl;
    }

    /**
     * \brief Get the tag's Time to Live Implemented, but not used in the stack yet
     * \returns the TTL
     */
    public byte GetTtl() {
	return m_ttl;
    }

    @Override
    public int GetSerializedSize() {
	return 4 + 4 + 1;
    }

    @Override
    public void Serialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Deserialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Print() {
	// TODO Auto-generated method stub

    }

}
