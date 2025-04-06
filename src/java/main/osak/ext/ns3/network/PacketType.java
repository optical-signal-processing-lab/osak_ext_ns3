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
package osak.ext.ns3.network;

/**
 * TODO PacketType
 * 
 * @author zhangrui
 * @since   1.0
 */
/**
 * Packet types are used as they are in Linux. GCC name resolution on typedef
 * enum {} PacketType is broken for the foreseeable future, so if you need to
 * use ns-3 PacketType in a driver that also uses the Linux packet types you're
 * hosed unless we define a shadow type, which we do here.
 */
public enum PacketType {
    PACKET_HOST(1), // !< Packet addressed to us
    NS3_PACKET_HOST(1), PACKET_BROADCAST(2), // !< Packet addressed to all
    NS3_PACKET_BROADCAST(2), PACKET_MULTICAST(3), // !< Packet addressed to multicast group
    NS3_PACKET_MULTICAST(3), PACKET_OTHERHOST(4), // !< Packet addressed to someone else
    NS3_PACKET_OTHERHOST(4);

    int val;

    PacketType(int val) {
	this.val = val;
    }

    public static PacketType valueOf(int value) {
	switch (value) {
	case 1:
	    return PACKET_HOST;
	case 2:
	    return PACKET_BROADCAST;
	case 3:
	    return PACKET_MULTICAST;
	case 4:
	    return PACKET_OTHERHOST;
	default:
	    throw new RuntimeException("Don't have this enum value");
	}
    }

    public int value(PacketType type) {
	return type.val;
    }
};
