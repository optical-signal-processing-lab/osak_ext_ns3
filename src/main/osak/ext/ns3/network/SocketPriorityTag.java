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
 * indicates whether the socket has a priority set.
 * 
 * @author zhangrui
 * @since 1.0
 */
public class SocketPriorityTag implements Tag {
    private byte m_priority; // !< the priority carried by the tag
    /**
     * 
     */
    public SocketPriorityTag() {
	// TODO Auto-generated constructor stub
    }

    /**
     * @brief Set the tag's priority
     *
     * @param priority the priority
     */
    public void SetPriority(byte priority) {
	m_priority = priority;
    }

    /**
     * @brief Get the tag's priority
     *
     * @returns the priority
     */
    public byte GetPriority() {
	return m_priority;
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
