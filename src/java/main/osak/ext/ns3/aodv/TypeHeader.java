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

import osak.ext.communication.MyLog;
import osak.ext.ns3.network.Header;

/**
 * TODO TypeHeader
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class TypeHeader implements Header {
    private MessageType m_type = MessageType.AODVTYPE_RREQ;
    private boolean m_valid = true;

    public TypeHeader() {

    }

    public TypeHeader(MessageType t) {
	this.m_type = t;
    }
    @Override
    public int GetSerializedSize() {
	return 1;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(m_type.value());
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	byte type = buffer.get();
	try {
	    m_type = MessageType.valueOf(type);
	} catch (RuntimeException e) {
	    m_valid = false;
	    MyLog.logInfo("TypeHeader", "Deserialize type failed");
	}
	return buffer.position() - start;
    }

    public MessageType Get() {
	return m_type;
    }

    public boolean IsValid() {
	return m_valid;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof TypeHeader) {
	    TypeHeader type = (TypeHeader) obj;
	    if (type.m_valid == this.m_valid && type.m_type == this.m_type) {
		return true;
	    }
	    return false;
	}
	return false;
    }

}
