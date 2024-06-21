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

import osak.ext.ns3.callback.ErrorCallback;
import osak.ext.ns3.callback.UnicastForwardCallback;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.internet.*;
import osak.ext.ns3.network.Packet;

/**
 * QueueEntry : AODV Queue Entry
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class QueueEntry {
    Packet m_packet = null;/// Data packet
    Ipv4Header m_header = null;/// IP header
    UnicastForwardCallback m_ucb = null;/// Unicast forward callback
    ErrorCallback m_ecb = null;/// Error callback
    Time m_expire = Time.Now();/// Expire time for queue entry

    public QueueEntry(Packet pa, Ipv4Header h, UnicastForwardCallback ucb, ErrorCallback ecb, Time exp) {
	m_packet = pa;
	m_header = h;
	m_ucb = ucb;
	m_ecb = ecb;
	m_expire = exp.add(Time.Now());
    }

    /**
     * 
     */
    public QueueEntry() {
	m_packet = null;
	m_header = null;
	m_ucb = null;
	m_ecb = null;
	m_expire = Time.Now();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof QueueEntry) {
	    QueueEntry o = (QueueEntry) obj;
	    return ((m_packet == o.m_packet) && (m_header.GetDestination() == o.m_header.GetDestination())
		    && (m_expire == o.m_expire));
	}
	return false;
    }

    // Fields
    /**
     * Get unicast forward callback
     * 
     * @returns unicast callback
     */
    public UnicastForwardCallback GetUnicastForwardCallback() {
	return m_ucb;
    }

    /**
     * Set unicast forward callback
     * 
     * @param ucb The unicast callback
     */
    public void SetUnicastForwardCallback(UnicastForwardCallback ucb) {
	m_ucb = ucb;
    }

    /**
     * Get error callback
     * 
     * @returns the error callback
     */
    public ErrorCallback GetErrorCallback() {
	return m_ecb;
    }

    /**
     * Set error callback
     * 
     * @param ecb The error callback
     */
    public void SetErrorCallback(ErrorCallback ecb) {
	m_ecb = ecb;
    }

    /**
     * Get packet from entry
     * 
     * @returns the packet
     */
    public Packet GetPacket() {
	return m_packet;
    }

    /**
     * Set packet in entry
     * 
     * @param p The packet
     */
    public void SetPacket(Packet p) {
	m_packet = p;
    }

    /**
     * Get IPv4 header
     * 
     * @returns the IPv4 header
     */
    public Ipv4Header GetIpv4Header() {
	return m_header;
    }

    /**
     * Set IPv4 header
     * 
     * @param h the IPv4 header
     */
    public void SetIpv4Header(Ipv4Header h) {
	m_header = h;
    }

    /**
     * Set expire time
     * 
     * @param exp The expiration time
     */
    public void SetExpireTime(Time exp) {
	m_expire = exp.add(Time.Now());
    }

    /**
     * Get expire time
     * 
     * @returns the expiration time
     */
    public Time GetExpireTime() {
	return Time.sub(m_expire, Time.Now());
    }


}
