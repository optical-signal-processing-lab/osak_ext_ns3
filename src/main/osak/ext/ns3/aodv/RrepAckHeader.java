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
import java.nio.ByteBuffer;

import osak.ext.ns3.network.Header;

/**
 * Route Reply Acknowledgment (RREP-ACK) Message Format
 * 
 * <pre>
 * 0                   1
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Type      |   Reserved    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class RrepAckHeader implements Header {
    private byte m_reserved = 0;/// < Not used (must be 0)
    
    public RrepAckHeader() {
    }
    @Override
    public int GetSerializedSize() {
	return 1;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_reserved);
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	m_reserved = buffer.get();
	return buffer.position() - start;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof RrepAckHeader) {
	    RrepAckHeader o = (RrepAckHeader) obj;
	    return m_reserved == o.m_reserved;
	}
	return false;
    }

}
