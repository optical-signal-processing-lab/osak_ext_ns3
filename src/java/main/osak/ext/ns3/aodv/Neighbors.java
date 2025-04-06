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

import java.util.ArrayList;
import java.util.List;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.Callback1;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.core.Timer;
import osak.ext.ns3.internet.ArpCache;
import osak.ext.ns3.internet.ArpCacheEntry;
import osak.ext.ns3.network.utils.EthernetHeader;
import osak.ext.ns3.network.utils.Ipv4Address;
import osak.ext.ns3.network.utils.Mac48Address;

/**
 * Neighbors: maintain list of active neighbors
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class Neighbors {
    public Neighbors(Time delay) {
	m_ntimer.SetDelay(delay);
	m_ntimer.SetFunction(() -> this.Purge());
	m_txErrorCallback = (header) -> this.ProcessTxError(header);
    }

    class Neighbor {

	public Ipv4Address m_neighborAddress;/// Neighbor IPv4 address

	public Mac48Address m_hardwareAddress;/// Neighbor MAC address

	public Time m_expireTime;/// Neighbor expire time

	public boolean close;/// Neighbor close indicator

	/**
	 * Neighbor structure constructor
	 *
	 * @param ip  Ipv4Address entry
	 * @param mac Mac48Address entry
	 * @param t   Time expire time
	 */
	public Neighbor(Ipv4Address ip, Mac48Address mac, Time t) {
	    m_neighborAddress = ip;
	    m_hardwareAddress = mac;
	    m_expireTime = t;
	    close = false;
	}
    }

    /**
     * Return expire time for neighbor node with address addr, if exists, else
     * return 0.
     * 
     * @param addr the IP address of the neighbor node
     * @returns the expire time for the neighbor node
     */
    public Time GetExpireTime(Ipv4Address addr) {
	Purge();
	for (Neighbor i : m_nb) {
	    if (i.m_neighborAddress == addr) {
		return Time.sub(i.m_expireTime, Time.Now());
	    }
	}
	return new Time(0);
    }

    /**
     * Check that node with address addr is neighbor
     * 
     * @param addr the IP address to check
     * @returns true if the node with IP address is a neighbor
     */
    public boolean IsNeighbor(Ipv4Address addr) {
	Purge();
	for (Neighbor i : m_nb) {
	    if (i.m_neighborAddress == addr) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Update expire time for entry with address addr, if it exists, else add new
     * entry
     * 
     * @param addr   the IP address to check
     * @param expire the expire time for the address
     */
    public void Update(Ipv4Address addr, Time expire) {
	for (Neighbor i : m_nb) {
	    if (i.m_neighborAddress == addr) {
		i.m_expireTime = Time.MAX(Time.add(expire, Time.Now()), i.m_expireTime);
		if (i.m_hardwareAddress.equals(new Mac48Address())) {
		    i.m_hardwareAddress = LookupMacAddress(i.m_neighborAddress);
		}
		return;
	    }
	}
	MyLog.logInfo("Neighbors::Update", "Open link to" + addr);
	Neighbor neighbor = new Neighbor(addr, LookupMacAddress(addr), Time.add(expire, Time.Now()));
	m_nb.add(neighbor);
	Purge();
    }

    // Check if the entry is expired
    private boolean CloseNeighbor(final Neighbor nb) {
	return ((nb.m_expireTime.getMillSeconds() < System.currentTimeMillis()) || nb.close);
    }

    /// Remove all expired entries
    public void Purge() {
	if (m_nb.isEmpty()) {
	    return;
	}
	if (m_handleLinkFailure != null) {
	    for (Neighbor i : m_nb) {
		if (CloseNeighbor(i)) {
		    MyLog.logInfo("Neighbors::Purge", "Close link to " + i.m_neighborAddress);
		    m_handleLinkFailure.callback(i.m_neighborAddress);
		}
	    }
	}

	for (Neighbor i : m_nb) {
	    if (CloseNeighbor(i)) {
		m_nb.remove(i);
	    }
	}
	m_ntimer.Cancel();
	m_ntimer.Schedule();
    }

    /// Schedule m_ntimer.
    public void ScheduleTimer() {
	m_ntimer.Cancel();
	m_ntimer.Schedule();
    }

    /// Remove all entries
    public void Clear() {
	m_nb.clear();
    }

    /**
     * Add ARP cache to be used to allow layer 2 notifications processing
     * 
     * @param a pointer to the ARP cache to add
     */
    public void AddArpCache(ArpCache a) {
	m_arp.add(a);
    }

    /**
     * Don't use given ARP cache any more (interface is down)
     * 
     * @param a pointer to the ARP cache to delete
     */
    public void DelArpCache(ArpCache a) {
	m_arp.remove(a);
    }

    /**
     * Get callback to ProcessTxError
     * 
     * @returns the callback function
     */
    public Callback1<EthernetHeader> GetTxErrorCallback() {
	return m_txErrorCallback;
    }

    /**
     * Set link failure callback
     * 
     * @param cb the callback function
     */
    public void SetCallback(Callback1<Ipv4Address> cb) {
	m_handleLinkFailure = cb;
    }

    /**
     * Get link failure callback
     * 
     * @returns the link failure callback
     */
    public Callback1<Ipv4Address> GetCallback() {
	return m_handleLinkFailure;
    }

    /// link failure callback
    private Callback1<Ipv4Address> m_handleLinkFailure;
    /// TX error callback
    private Callback1<EthernetHeader> m_txErrorCallback;
    /// Timer for neighbor's list. Schedule Purge().
    Timer m_ntimer = new Timer();
    /// vector of entries
    private List<Neighbor> m_nb = new ArrayList<>();
    /// list of ARP cached to be used for layer 2 notifications processing
    private List<ArpCache> m_arp = new ArrayList<>();

    /**
     * Find MAC address by IP using list of ARP caches
     *
     * @param addr the IP address to lookup
     * @returns the MAC address for the IP address
     */
    private Mac48Address LookupMacAddress(Ipv4Address addr) {
	Mac48Address hwaddr = new Mac48Address();
	for (ArpCache i : m_arp) {
	    ArpCacheEntry entry = i.Lookup(addr);
	    if (entry != null && (entry.IsAlive() || entry.IsPermanent()) && !entry.IsExpired()) {
		hwaddr = Mac48Address.ConvertFrom(entry.GetMacAddress());
		break;
	    }
	}
	return hwaddr;
    }


    /**
     * Process layer 2 TX error notification
     * @param hdr header of the packet
     */
    private void ProcessTxError(final EthernetHeader hdr) {
	// TODO: need to check if addr1 == srcmac
	Mac48Address addr = new Mac48Address();
	addr.CopyFrom(hdr.GetDstMac());
	for (Neighbor i : m_nb) {
	    if (i.m_hardwareAddress.equals(addr)) {
		i.close = true;
	    }
	}
	Purge();
    }

}
