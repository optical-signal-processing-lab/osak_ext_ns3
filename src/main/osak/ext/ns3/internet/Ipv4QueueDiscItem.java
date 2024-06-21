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

import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.QueueDiscItem;

/**
 * TODO Ipv4QueueDiscItem
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4QueueDiscItem extends QueueDiscItem {
    private Ipv4Header m_header;
    private boolean m_headerAdded;
    
    /**
     * @brief Create an IPv4 queue disc item containing an IPv4 packet.
     * @param p        the packet included in the created item.
     * @param addr     the destination MAC address
     * @param protocol the protocol number
     * @param header   the IPv4 header
     */
    public Ipv4QueueDiscItem(Packet p, final Address addr, short protocol, final Ipv4Header header) {
	super(p, addr, protocol);
	m_header = header;
	m_headerAdded = false;
    }

}
