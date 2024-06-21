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
 * This class implements a tag that carries the socket-specific HOPLIMIT of a
 * packet to the IPv6 layer
 * 
 * @author zhangrui
 * @since 1.0
 */
public class SocketIpv6HopLimitTag implements Tag {
    private byte m_hopLimit; // !< the Hop Limit carried by the tag

    public SocketIpv6HopLimitTag() {

    }

    /**
     * @brief Set the tag's Hop Limit
     *
     * @param hopLimit the Hop Limit
     */
    public void SetHopLimit(byte hopLimit) {
	m_hopLimit = hopLimit;
    }

    /**
     * @brief Get the tag's Hop Limit
     *
     * @returns the Hop Limit
     */
    public byte GetHopLimit() {
	return m_hopLimit;
    }

    @Override
    public int GetSerializedSize() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void Serialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Deserialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Print() {
	// TODO Auto-generated method stub

    }

}
