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

import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * ipv4Routing
 * <p>
 * 
 * IPv4 route cache entry (similar to Linux struct rtable)
 * <p>
 * 
 * This is a reference counted object. In the future, we will add other entries
 * from struct dst_entry, struct rtable, and struct dst_ops as needed.
 * 
 * @author zhangrui
 * @since 1.0
 */
public class Ipv4Route {
    Ipv4Address m_dest; // !< Destination address.
    Ipv4Address m_source; // !< Source address.
    Ipv4Address m_gateway; // !< Gateway address.
    NetDevice m_outputDevice; // !< Output device.

    public Ipv4Route() {

    }

    public void SetDestination(Ipv4Address dest) {
	m_dest = dest;
    }

    /**
     * @return Destination Ipv4Address of the route
     */
    public Ipv4Address GetDestination() {
	return m_dest;
    }

    /**
     * @param src Source Ipv4Address
     */
    public void SetSource(Ipv4Address src) {
	m_source = src;
    }

    /**
     * @return Source Ipv4Address of the route
     */
    public Ipv4Address GetSource() {
	return m_source;
    }

    /**
     * @param gw Gateway (next hop) Ipv4Address
     */
    public void SetGateway(Ipv4Address gw) {
	m_gateway = gw;
    }

    /**
     * @return Ipv4Address of the gateway (next hop)
     */
    public Ipv4Address GetGateway() {
	return m_gateway;
    }

    /**
     * Equivalent in Linux to dst_entry.dev
     *
     * @param outputDevice pointer to NetDevice for outgoing packets
     */
    public void SetOutputDevice(NetDevice outputDevice) {
	m_outputDevice = outputDevice;
    }

    /**
     * @return pointer to NetDevice for outgoing packets
     */
    public NetDevice GetOutputDevice() {
	return m_outputDevice;
    }
}
