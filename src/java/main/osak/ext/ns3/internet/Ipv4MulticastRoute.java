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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Ipv4MulticastRoute
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4MulticastRoute {
    public static final int MAX_INTERFACES = 16;
    public static final int MAX_TTL = 255;
    InetAddress m_group; // !< Group
    InetAddress m_origin; // !< Source of packet
    int m_parent; // !< Source interface
    Map<Integer, Integer> m_ttls = new HashMap<>(); // !< Time to Live container

    public Ipv4MulticastRoute() {
	m_ttls.clear();
    }

    /**
     * @param group InetAddress of the multicast group
     */
    public void SetGroup(InetAddress group) {
	m_group = group;
    }

    /**
     * @return InetAddress of the multicast group
     */
    public InetAddress GetGroup() {
	return m_group;
    }

    /**
     * @param origin InetAddress of the origin address
     */
    public void SetOrigin(InetAddress origin) {
	m_origin = origin;
    }

    /**
     * @return InetAddress of the origin address
     */
    public InetAddress GetOrigin() {
	return m_origin;
    }

    /**
     * @param iif Parent (input interface) for this route
     */
    public void SetParent(int parent) {
	m_parent = parent;
    }

    /**
     * @return Parent (input interface) for this route
     */
    public int GetParent() {
	return m_parent;
    }

    /**
     * @param oif Outgoing interface index
     * @param ttl time-to-live for this route
     */
    public void SetOutputTtl(int oif, int ttl) {
	if (ttl >= MAX_TTL) {
	    if (m_ttls.containsKey(oif)) {
		m_ttls.remove(oif);
	    }
	} else {
	    m_ttls.put(oif, ttl);
	}
    }

    /**
     * @return map of output interface Ids and TTLs for this route
     */
    public Map<Integer, Integer> GetOutputTtlMap() {
	return m_ttls;
    }
}
