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

import osak.ext.ns3.internet.Ipv4Header;
import osak.ext.ns3.network.Packet;

/**
 * TODO DuplicatePacketDetection
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class DuplicatePacketDetection {
    private IdCache m_idCache;

    /**
     * Constructor
     * 
     * @param lifetime the lifetime for added entries
     */
    public DuplicatePacketDetection(long lifetime) {
	this.m_idCache = new IdCache(lifetime);
    }

    /**
     * Check if the packet is a duplicate. If not, save information about this
     * packet.
     * 
     * @param p      the packet to check
     * @param header the IP header to check
     * @returns true if duplicate
     */
    public boolean IsDuplicate(Packet p, Ipv4Header header) {
	return m_idCache.IsDuplicate(header.GetSource(), p.GetUid());
    }

    /**
     * Set duplicate record lifetime
     * 
     * @param lifetime the lifetime for duplicate records
     */
    public void SetLifetime(long lifetime) {
	m_idCache.SetLifetime(lifetime);
    }

    /**
     * Get duplicate record lifetime
     * 
     * @returns the duplicate record lifetime
     */
    public long GetLifetime() {
	return m_idCache.GetLifeTime();
    }
}
