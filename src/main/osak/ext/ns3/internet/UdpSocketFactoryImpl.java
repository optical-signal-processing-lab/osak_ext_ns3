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

import osak.ext.ns3.network.Socket;

/**
 * Object to create UDP socket instances
 * <p>
 * 
 * This class implements the API for creating UDP sockets. It is a socket
 * factory (deriving from class SocketFactory).
 * 
 * @author zhangrui
 * @since 1.0
 */
public class UdpSocketFactoryImpl extends UdpSocketFactory {
    private UdpL4Protocol m_udp;// !< the associated UDP L4 protocol

    public UdpSocketFactoryImpl() {
	m_udp = null;
    }

    /**
     * @brief Set the associated UDP L4 protocol.
     * @param udp the UDP L4 protocol
     */
    public void SetUdp(UdpL4Protocol udp) {
	m_udp = udp;
    }

    /**
     * @brief Implements a method to create a Udp-based socket and return a base
     *        class smart pointer to the socket.
     *
     * @return smart pointer to Socket
     */
    @Override
    public Socket CreateSocket() {
	return m_udp.CreateSocket();
    }

}
