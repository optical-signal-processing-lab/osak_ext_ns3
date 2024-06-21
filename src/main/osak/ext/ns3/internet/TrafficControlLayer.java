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

import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.PacketType;
import osak.ext.ns3.network.utils.QueueDiscItem;

/**
 * TODO TrafficControlLayer
 * 
 * @author zhangrui
 * @since   1.0
 */
public class TrafficControlLayer {

    /**
     * @param m_device
     * @param p
     * @param protNumber
     * @param address
     * @param address2
     * @param packetHost
     */
    public void Receive(NetDevice m_device, Packet p, short protNumber, Address address, Address address2,
	    PacketType packetHost) {
	// TODO Auto-generated method stub

    }

    /**
     * @param m_device
     * @param ipv4QueueDiscItem
     */
    public void Send(NetDevice device, QueueDiscItem item) {
	// TODO Auto-generated method stub
	device.Send(item.GetPacket(), item.GetAddress(), item.GetProtocol());
    }

}
