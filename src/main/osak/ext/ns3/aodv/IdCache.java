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
import java.util.UUID;

import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO IdCache
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class IdCache {
    class UniqueId {
	/// ID is supposed to be unique in single address context (e.g. sender address)
	Ipv4Address m_context;
	/// The id
	UUID m_uuid;
	int m_id;
	/// When record will expire
	long m_expire;

	UniqueId(Ipv4Address m_context, UUID m_id, long m_expire) {
	    this.m_context = m_context;
	    this.m_uuid = m_id;
	    this.m_expire = m_expire;
	}

	UniqueId(Ipv4Address m_context, int m_id, long m_expire) {
	    this.m_context = m_context;
	    this.m_id = m_id;
	    this.m_expire = m_expire;
	}
    };

    /**
     * \brief Check if the entry is expired
     *
     * @param u UniqueId entry
     * @return true if expired, false otherwise
     */
    boolean IsExpired(UniqueId u) {
	return (u.m_expire < System.currentTimeMillis());
    }

    /// Already seen IDs
    List<UniqueId> m_idCache = new LinkedList<>();
    /// Default lifetime for ID records
    long m_lifetime;

    // ----public----
    /**
     * constructor
     * 
     * @param lifetime the lifetime for added entries
     */
    public IdCache(long lifetime) {
	this.m_lifetime = lifetime;
    }

    /**
     * Check that entry (addr, id) exists in cache. Add entry, if it doesn't exist.
     * 
     * @param addr the IP address
     * @param id   the cache entry ID
     * @returns true if the pair exists
     */
    public boolean IsDuplicate(Ipv4Address addr, UUID id) {
	Purge();
	for (UniqueId i : m_idCache) {
	    if (i.m_context.equals(addr) && i.m_uuid == id) {
		return true;
	    }
	}
	UniqueId uniqueId = new UniqueId(addr, id, m_lifetime + System.currentTimeMillis());
	m_idCache.add(uniqueId);
	return false;
    }

    /// Remove all expired entries
    public void Purge() {
	for (UniqueId i : m_idCache) {
	    if (IsExpired(i)) {
		m_idCache.remove(i);
	    }
	}
    }

    /**
     * @returns number of entries in cache
     */
    public int GetSize() {
	Purge();
	return m_idCache.size();

    }

    /**
     * Set lifetime for future added entries.
     * 
     * @param lifetime the lifetime for entries
     */
    public void SetLifetime(long lifetime) {
	m_lifetime = lifetime;
    }

    /**
     * Return lifetime for existing entries in cache
     * 
     * @returns the lifetime
     */
    long GetLifeTime() {
	return m_lifetime;
    }

    /**
     * @param origin
     * @param id
     * @return
     */
    public boolean IsDuplicate(Ipv4Address addr, int id) {
	Purge();
	for (UniqueId i : m_idCache) {
	    if (i.m_context.equals(addr) && i.m_id == id) {
		return true;
	    }
	}
	UniqueId uniqueId = new UniqueId(addr, id, m_lifetime + System.currentTimeMillis());
	m_idCache.add(uniqueId);
	return false;
    }
}
