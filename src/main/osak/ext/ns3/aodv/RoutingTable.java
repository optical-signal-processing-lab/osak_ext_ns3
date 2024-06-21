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
import java.util.HashMap;
import java.util.Map;

import osak.ext.communication.MyLog;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.internet.Ipv4InterfaceAddress;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO RoutingTable
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class RoutingTable {
    /// The routing table
    private Map<Ipv4Address, RoutingTableEntry> m_ipv4AddressEntry = new HashMap<>();
    // TODO:Time
    /// Deletion time for invalid routes
    private Time m_badLinkLifetime;

    /**
     * constructor
     * 
     * @param t the routing table entry lifetime
     */
    public RoutingTable(Time t) {
	m_badLinkLifetime = t;
    }

    /// \name Handle lifetime of invalid route
    // \{
    /**
     * Get the lifetime of a bad link
     *
     * @return the lifetime of a bad link
     */
    public Time GetBadLinkLifetime() {
	return m_badLinkLifetime;
    }

    /**
     * Set the lifetime of a bad link
     *
     * @param t the lifetime of a bad link
     */
    public void SetBadLinkLifetime(Time t) {
	m_badLinkLifetime = t;
    }

    // \}
    /**
     * Add routing table entry if it doesn't yet exist in routing table
     * 
     * @param r routing table entry
     * @return true in success
     */
    public boolean AddRoute(RoutingTableEntry r) {
	Purge();
	if (r.GetFlag() != RouteFlags.IN_SEARCH) {
	    r.SetRreqCnt((byte) 0);
	}
	m_ipv4AddressEntry.put(r.GetDestination(), r);
	return true;
    }

    /**
     * Delete routing table entry with destination address dst, if it exists.
     * 
     * @param dst destination address
     * @return true on success
     */
    public boolean DeleteRoute(Ipv4Address dst) {
	Purge();
	if (m_ipv4AddressEntry.remove(dst) != null) {
	    MyLog.logInfo("DeleteRoute", "Route deletion to " + dst + " successful");
	    return true;
	}
	MyLog.logInfo("DeleteRoute", "Route deletion to " + dst + " not successful");
	return false;
    }

    /**
     * Lookup routing table entry with destination address dst
     * 
     * @param dst destination address
     * @param rt  entry with destination address dst, if exists
     * @return true on success
     */
    public boolean LookupRoute(Ipv4Address dst, RoutingTableEntry rt) {
	Purge();
	if (m_ipv4AddressEntry.isEmpty()) {
	    MyLog.logInfo("LookupRoute", "Route to " + dst + " not found; m_ipv4AddressEntry is empty");
	    return false;
	}
	if (!m_ipv4AddressEntry.containsKey(dst)) {
	    MyLog.logInfo("LookupRoute", "Route to " + dst + " not found");
	    return false;
	}
	rt.Copy(m_ipv4AddressEntry.get(dst));
	MyLog.logInfo("LookupRoute", "Route to " + dst + " found");
	return true;
    }

    /**
     * Lookup route in VALID state
     * 
     * @param dst destination address
     * @param rt  entry with destination address dst, if exists
     * @return true on success
     */
    public boolean LookupValidRoute(Ipv4Address dst, RoutingTableEntry rt) {
	if (!LookupRoute(dst, rt)) {
	    MyLog.logInfo("LookupValidRoute", "Route to " + dst + " not found");
	    return false;
	}
	MyLog.logInfo("LookupValidRoute", "Route to " + dst + " flag is " + rt.GetFlag());
	return (rt.GetFlag() == RouteFlags.VALID);
    }

    /**
     * Update routing table
     * 
     * @param rt entry with destination address dst, if exists
     * @return true on success
     */
    public boolean Update(RoutingTableEntry rt) {
	if (!m_ipv4AddressEntry.containsKey(rt.GetDestination())) {
	    MyLog.logInfo("Update", "Route update to " + rt.GetDestination() + " fails; not found");
	    return false;
	}
	// TODO:need to check
	if (rt.GetFlag() != RouteFlags.IN_SEARCH) {
	    MyLog.logInfo("Update", "Route update to " + rt.GetDestination() + " set RreqCnt to 0");
	    rt.SetRreqCnt((byte) 0);
	}
	m_ipv4AddressEntry.put(rt.GetDestination(), rt);
	return true;
    }

    /**
     * Set routing table entry flags
     * 
     * @param dst   destination address
     * @param state the routing flags
     * @return true on success
     */
    public boolean SetEntryState(Ipv4Address dst, RouteFlags state) {
	if (!m_ipv4AddressEntry.containsKey(dst)) {
	    MyLog.logInfo("SetEntryState", "Route set entry state to " + dst + " fails; not found");
	    return false;
	}
	RoutingTableEntry rt = m_ipv4AddressEntry.get(dst);
	rt.SetFlag(state);
	rt.SetRreqCnt((byte) 0);
	MyLog.logInfo("SetEntryState", "Route set entry state to " + dst + ": new state is " + state);
	return true;
    }

    /**
     * Lookup routing entries with next hop Address dst and not empty list of
     * precursors.
     *
     * @param nextHop     the next hop IP address
     * @param unreachable
     */
    public void GetListOfDestinationWithNextHop(Ipv4Address nextHop, Map<Ipv4Address, Integer> unreachable) {
	Purge();
	for (Ipv4Address i : m_ipv4AddressEntry.keySet()) {
	    RoutingTableEntry rt = m_ipv4AddressEntry.get(i);
	    if (rt.GetNextHop() == nextHop) {
		MyLog.logInfo("GetListOfDestinationWithNextHop",
			"Unreachable insert " + rt.GetDestination() + " " + rt.GetSeqNo());
		unreachable.put(i, rt.GetSeqNo());
	    }
	}
    }

    /**
     * Update routing entries with this destination as follows: 1. The destination
     * sequence number of this routing entry, if it exists and is valid, is
     * incremented. 2. The entry is invalidated by marking the route entry as
     * invalid 3. The Lifetime field is updated to current time plus DELETE_PERIOD.
     * 
     * @param unreachable routes to invalidate
     */
    public void InvalidateRoutesWithDst(Map<Ipv4Address, Integer> unreachable) {
	Purge();
	for (Ipv4Address k : m_ipv4AddressEntry.keySet()) {
	    RoutingTableEntry v = m_ipv4AddressEntry.get(k);
	    if (unreachable.containsKey(k) && v.GetFlag() == RouteFlags.VALID) {
		MyLog.logInfo("InvalidateRoutesWithDst", "Invalidate route with destination address " + k);
		v.Invalidate(m_badLinkLifetime);
		m_ipv4AddressEntry.put(k, v);
	    }
	}
    }

    /**
     * Delete all route from interface with address iface
     * 
     * @param iface the interface IP address
     */
    public void DeleteAllRoutesFromInterface(Ipv4InterfaceAddress iface) {
	if (m_ipv4AddressEntry.isEmpty()) {
	    return;
	}
	for (Ipv4Address k : m_ipv4AddressEntry.keySet()) {
	    RoutingTableEntry v = m_ipv4AddressEntry.get(k);
	    if (v.GetInterface() == iface) {
		m_ipv4AddressEntry.remove(k);
	    }
	}
    }

    /// Delete all entries from routing table
    public void Clear() {
	m_ipv4AddressEntry.clear();
    }

    /// Delete all outdated entries and invalidate valid entry if Lifetime is
    /// expired
    public void Purge() {
	if (m_ipv4AddressEntry.isEmpty()) {
	    return;
	}
	for (Ipv4Address key : m_ipv4AddressEntry.keySet()) {
	    RoutingTableEntry value = m_ipv4AddressEntry.get(key);
	    if (value.GetLifeTime().getLong() < 0) {
		if (value.GetFlag() == RouteFlags.INVALID) {
		    m_ipv4AddressEntry.remove(key);
		} else if (value.GetFlag() == RouteFlags.VALID) {
		    MyLog.logInfo("Purge", "Invalidate route with destination address " + key);
		    value.Invalidate(m_badLinkLifetime);
		    m_ipv4AddressEntry.replace(key, value);
		} else
		    continue;
	    }
	}

    }

    /**
     * Mark entry as unidirectional (e.g. add this neighbor to "blacklist" for
     * blacklistTimeout period)
     * 
     * @param neighbor         neighbor address link to which assumed to be
     *                         unidirectional
     * @param blacklistTimeout time for which the neighboring node is put into the
     *                         blacklist
     * @return true on success
     */
    public boolean MarkLinkAsUnidirectional(Ipv4Address neighbor, Time blacklistTimeout) {
	if (!m_ipv4AddressEntry.containsKey(neighbor)) {
	    MyLog.logInfo("MarkLinkAsUnidirectional", "Mark link unidirectional to  " + neighbor + " fails; not found");
	    return false;
	}
	RoutingTableEntry val = m_ipv4AddressEntry.get(neighbor);
	val.SetUnidirectional(true);
	val.SetBlacklistTimeout(blacklistTimeout);
	val.SetRreqCnt((byte) 0);
	MyLog.logInfo("MarkLinkAsUnidirectional", "Set link to " + neighbor + " to unidirectional");
	m_ipv4AddressEntry.replace(neighbor, val);
	return true;
    }

    // TODO:print routing table
}
