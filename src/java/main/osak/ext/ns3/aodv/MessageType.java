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
/**
 * MessageType 
 * adov 消息类型
 * 
 * @author zhangrui
 * @since 1.0
 */
public enum MessageType {
    AODVTYPE_RREQ((byte) 1), AODVTYPE_RREP((byte) 2), AODVTYPE_RERR((byte) 3), AODVTYPE_RREP_ACK((byte) 4);

    private byte value = 0;

    private MessageType(byte value) {
	this.value = value;
    }

    public static MessageType valueOf(byte value) {
	switch (value) {
	case 1:
	    return AODVTYPE_RREQ;
	case 2:
	    return AODVTYPE_RREP;
	case 3:
	    return AODVTYPE_RERR;
	case 4:
	    return AODVTYPE_RREP_ACK;
	default:
	    throw new RuntimeException("Don't have this enum value");
	}
    }

    public byte value() {
	return this.value;
    }

}
