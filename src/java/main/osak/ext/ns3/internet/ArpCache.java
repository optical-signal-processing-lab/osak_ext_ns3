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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.Callback2;
import osak.ext.ns3.core.Pair;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.core.Timer;
import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.Ipv4Address;
/**
 * TODO ArpCache
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class ArpCache {
    public ArpCache() {

    }

    /**
     * Set the NetDevice and Ipv4Interface associated with the ArpCache
     *
     * @param device    The hardware NetDevice associated with this ARP cache
     * @param interface the Ipv4Interface associated with this ARP cache
     */
    public void SetDevice(NetDevice device, Ipv4Interface iface) {
	m_device = device;
	m_interface = iface;
    }

    /**
     * Returns the NetDevice that this ARP cache is associated with
     * 
     * @return The NetDevice that this ARP cache is associated with
     */
    public NetDevice GetDevice() {
	return m_device;
    }

    /**
     * Returns the Ipv4Interface that this ARP cache is associated with
     * 
     * @return the Ipv4Interface that this ARP cache is associated with
     */
    public Ipv4Interface GetInterface() {
	return m_interface;
    }

    /**
     * Set the time the entry will be in ALIVE state (unless refreshed)
     * 
     * @param aliveTimeout the Alive state timeout
     */
    public void SetAliveTimeout(Time aliveTimeout) {
	m_aliveTimeout = aliveTimeout;
    }

    /**
     * Set the time the entry will be in DEAD state before being removed
     * 
     * @param deadTimeout the Dead state timeout
     */
    public void SetDeadTimeout(Time deadTimeout) {
	m_deadTimeout = deadTimeout;
    }

    /**
     * Set the time the entry will be in WAIT_REPLY state
     * 
     * @param waitReplyTimeout the WAIT_REPLY state timeout
     */
    public void SetWaitReplyTimeout(Time waitReplyTimeout) {
	m_waitReplyTimeout = waitReplyTimeout;
    }

    /**
     * Get the time the entry will be in ALIVE state (unless refreshed)
     * 
     * @returns the Alive state timeout
     */
    public Time GetAliveTimeout() {
	return m_aliveTimeout;
    }

    /**
     * Get the time the entry will be in DEAD state before being removed
     * 
     * @returns the Dead state timeout
     */
    public Time GetDeadTimeout() {
	return m_deadTimeout;
    }

    /**
     * Get the time the entry will be in WAIT_REPLY state
     * 
     * @returns the WAIT_REPLY state timeout
     */
    public Time GetWaitReplyTimeout() {
	return m_waitReplyTimeout;
    }

    /**
     * This callback is set when the ArpCache is set up and allows
     * the cache to generate an Arp request when the WaitReply
     * time expires and a retransmission must be sent
     *
     * @param arpRequestCallback Callback for transmitting an Arp request.
     */
    public void SetArpRequestCallback(Callback2<ArpCache, Ipv4Address> arpRequestCallback) {
	m_arpRequestCallback = arpRequestCallback;
    }

    /**
     * This method will schedule a timeout at WaitReplyTimeout interval in the
     * future, unless a timer is already running for the cache, in which case this
     * method does nothing.
     */
    public void StartWaitReplyTimer() {
	if (!m_waitReplyTimer.IsRunning()) {
	    MyLog.logInfo("ArpCache::StartWaitReplyTimer",
		    "Starting WaitReplyTimer at " + System.currentTimeMillis() + " for " + m_waitReplyTimeout);
	    m_waitReplyTimer.Schedule(m_aliveTimeout, () -> this.HandleWaitReplyTimeout());
	}
    }

    /**
     * Do lookup in the ARP cache against an IP address
     * 
     * @param destination The destination IPv4 address to lookup the MAC address of
     * @return An ArpCache::Entry with info about layer 2
     */
    public ArpCacheEntry Lookup(Ipv4Address destination) {
	if (m_arpCache.containsKey(destination)) {
	    return m_arpCache.get(destination);
	}
	return null;
    }

    /**
     * Do lookup in the ARP cache against a MAC address
     * 
     * @param destination The destination MAC address to lookup of
     * @return A std::list of ArpCache::Entry with info about layer 2
     */
    public List<ArpCacheEntry> LookupInverse(Address destination) {
	List<ArpCacheEntry> entryList = new ArrayList<>();
	for (ArpCacheEntry i : m_arpCache.values()) {
	    if (i.GetMacAddress().equals(destination)) {
		entryList.add(i);
	    }
	}
	return entryList;
    }

    /**
     * Add an Ipv4Address to this ARP cache
     * 
     * @param to the destination address of the ARP entry.
     * @returns A pointer to a new ARP Entry.
     */
    public ArpCacheEntry Add(Ipv4Address to) {
	assert (m_arpCache.containsKey(to));
	ArpCacheEntry entry = new ArpCacheEntry(this);
	m_arpCache.put(to, entry);
	entry.SetIpv4Address(to);
	return entry;
    }

    /**
     *  Remove an entry.
     * @param entry pointer to delete it from the list
     */
    public void Remove(ArpCacheEntry entry) {
	for (Map.Entry<Ipv4Address, ArpCacheEntry> i : m_arpCache.entrySet()) {
	    if (i.getValue() == entry) {
		m_arpCache.remove(i.getKey());
		entry.ClearPendingPacket();// clear the pending packets for entry's ipaddress
		entry = null;
		return;
	    }
	}
    }

    /**
     * Clear the ArpCache of all entries
     */
    public void Flush() {
	m_arpCache.clear();
	while (m_waitReplyTimer.IsRunning()) {
	    Thread.yield();
	    continue;
	}
	m_waitReplyTimer.Cancel();
    }

    /**
     * Print the ARP cache entries
     *
     * @param stream the ostream the ARP cache entries is printed to
     */
//    public void PrintArpCache(Ptr<OutputStreamWrapper> stream);

    /**
     * Clear the ArpCache of all Auto-Generated entries
     */
    public void RemoveAutoGeneratedEntries() {
	for (Map.Entry<Ipv4Address, ArpCacheEntry> i : m_arpCache.entrySet()) {
	    if (i.getValue().IsAutoGenerated()) {
		i.getValue().ClearPendingPacket();// clear the pending packets for entry's ipaddress
		m_arpCache.remove(i.getKey());
	    }
	}
    }
    
    private NetDevice m_device = null; // !< NetDevice associated with the cache
    private Ipv4Interface m_interface = null; // !< Ipv4Interface associated with the cache
    private Time m_aliveTimeout; // !< cache alive state timeout
    private Time m_deadTimeout; // !< cache dead state timeout
    private Time m_waitReplyTimeout; // !< cache reply state timeout
    private Timer m_waitReplyTimer; // !< cache alive state timer
    private Callback2<ArpCache, Ipv4Address> m_arpRequestCallback; // !< reply timeout callback
    private int m_maxRetries; // !< max retries for a resolution

    /**
     * This function is an event handler for the event that the
     * ArpCache wants to check whether it must retry any Arp requests.
     * If there are no Arp requests pending, this event is not scheduled.
     */
    private void HandleWaitReplyTimeout() {
	boolean restartWaitReplyTimer = false;
	for (Map.Entry<Ipv4Address, ArpCacheEntry> i : m_arpCache.entrySet()) {
	    ArpCacheEntry entry = i.getValue();
	    if (entry != null && entry.IsWaitReply()) {
		if (entry.GetRetries() < m_maxRetries) {
		    m_arpRequestCallback.callback(this, entry.GetIpv4Address());
		    restartWaitReplyTimer = true;
		    entry.IncrementRetries();
		}

		else {
		    entry.MarkDead();
		    entry.ClearRetries();
		    Pair<Packet, Ipv4Header> pending = entry.DequeuePending();
		    while (pending.first() != null) {
			// add the Ipv4 header for tracing purposes
			pending.first().AddHeader(pending.second());
			// TODO:need to check
			// m_dropTrace.callback(pending.first());
			pending = entry.DequeuePending();
		    }
		}
	    }
	}
	if (restartWaitReplyTimer) {
	    m_waitReplyTimer.Schedule(m_waitReplyTimeout, () -> HandleWaitReplyTimeout());
	}
    }

    protected int m_pendingQueueSize; // !< number of packets waiting for a resolution
    private Map<Ipv4Address, ArpCacheEntry> m_arpCache = new HashMap<>(); // !< the ARP cache
    // TracedCallback<Ptr<const Packet>> m_dropTrace; //!< trace for packets dropped by the ARP cache queue
}
