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
 * This class implements a tag that carries the socket-specific TTL of a packet
 * to the IP layer
 * 
 * @author zhangrui
 * @since 1.0
 */
public class SocketIpTtlTag implements Tag {
    private byte m_ttl;// !< the ttl carried by the tag

    public SocketIpTtlTag() {

    }
    /**
     * @brief Set the tag's TTL
     *
     * @param ttl the TTL
     */
    public void SetTtl(byte ttl) {
	m_ttl = ttl;
    }

    /**
     * @brief Get the tag's TTL
     *
     * @returns the TTL
     */

    public byte GetTtl() {
	return m_ttl;
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
