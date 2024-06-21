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
import java.util.LinkedList;
import java.util.List;

import osak.ext.ns3.core.Time;
import osak.ext.ns3.network.SocketErrno;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * AODV route request queue
 * <p>
 * Since AODV is an on demand routing we queue requests while looking for route.
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class RequestQueue {
    /// The queue
    List<QueueEntry> m_queue = new LinkedList<>();
    /// The maximum number of packets that we allow a routing protocol to buffer.
    int m_maxLen;
    /// The maximum period of time that a routing protocol is allowed to buffer a
    /// packet for seconds.
    // TODO:this is for seconds
    Time m_queueTimeout;
    /// Remove all expired entries
    void Purge() {
	for (QueueEntry i : m_queue) {
	    if (i.GetExpireTime().getLong() < 0) {
		Drop(i, "Drop outdated packet ");
		m_queue.remove(i);
	    }
	}
    }
    
    /**
     * Notify that packet is dropped from queue by timeout
     * 
     * @param en     the queue entry to drop
     * @param reason the reason to drop the entry
     */
    void Drop(QueueEntry en, String reason) {
	en.GetErrorCallback().callback(en.GetPacket(), en.GetIpv4Header(), SocketErrno.ERROR_NOROUTETOHOST);
    }
    
    /**
     * constructor
     *
     * @param maxLen              the maximum length
     * @param routeToQueueTimeout the route to queue timeout
     */
    public RequestQueue(int maxLen, Time routeToQueueTimeout)
    {
	m_maxLen = maxLen;
	m_queueTimeout = routeToQueueTimeout;
    }

    /**
     * Push entry in queue, if there is no entry with the same packet and destination address in
     * queue.
     * @param entry the queue entry
     * @return true if the entry is queued
     */
    public boolean Enqueue(QueueEntry entry) {
	Purge();
	for (QueueEntry i : m_queue) {
	    if ((i.GetPacket().GetUid() == entry.GetPacket().GetUid())
		    && i.GetIpv4Header().GetDestination() == entry.GetIpv4Header().GetDestination()) {
		return false;
	    }
	}
	entry.SetExpireTime(m_queueTimeout);
	if(m_queue.size()==m_maxLen) {
	    Drop(m_queue.get(0),"Drop the most aged packet");
	    m_queue.remove(0);
	}
	m_queue.add(entry);
	return true;
    }
    /**
     * Return first found (the earliest) entry for given destination
     *
     * @param dst the destination IP address
     * @param entry the queue entry
     * @return true if the entry is dequeued
     */
    public boolean Dequeue(Ipv4Address dst, QueueEntry entry) {
	Purge();
	for (QueueEntry i : m_queue) {
	    if (i.GetIpv4Header().GetDestination().equals(dst)) {
		entry = i;
		m_queue.remove(i);
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Remove all packets with destination IP address dst
     * 
     * @param dst the destination IP address
     */
    public void DropPacketWithDst(Ipv4Address dst) {
	Purge();
	for (QueueEntry i : m_queue) {
	    if (i.GetIpv4Header().GetDestination().equals(dst)) {
		Drop(i, "DropPacketWithDst");
		m_queue.remove(i);
	    }
	}

    }
    
    /**
     * Finds whether a packet with destination dst exists in the queue
     *
     * @param dst the destination IP address
     * @return true if an entry with the IP address is found
     */
    public boolean Find(Ipv4Address dst) {
	for (QueueEntry i : m_queue) {
	    if (i.GetIpv4Header().GetDestination().equals(dst)) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * @return the number of entries
     */
    public int GetSize() {
	Purge();
	return m_queue.size();
    }

    // Fields
    /**
     * Get maximum queue length
     * 
     * @return the maximum queue length
     */
    public int GetMaxQueueLen()
    {
        return m_maxLen;
    }

    /**
     * Set maximum queue length
     * 
     * @param len The maximum queue length
     */
    public void SetMaxQueueLen(int len)
    {
        m_maxLen = len;
    }

    /**
     * Get queue timeout
     * 
     * @return the queue timeout
     */
    public Time GetQueueTimeout()
    {
        return m_queueTimeout;
    }

    /**
     * Set queue timeout
     * 
     * @param t The queue timeout
     */
    public void SetQueueTimeout(Time t)
    {
        m_queueTimeout = t;
    }
}
