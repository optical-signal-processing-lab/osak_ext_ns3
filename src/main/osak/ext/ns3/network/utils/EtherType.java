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
package osak.ext.ns3.network.utils;

/**
 * TODO EtherType
 * 
 * @author zhangrui
 * @since   1.0
 */
public enum EtherType {
    IP((short) 0x0800), ARP((short) 0x0806);

    private short val = 0;

    private EtherType(short val) {
	this.val = val;
    }

    public static EtherType valueOf(short value) {
	switch (value) {
	case 0x0800:
	    return IP;
	case 0x0806:
	    return ARP;
	default:
	    throw new RuntimeException("Don't have this enum value");
	}
    }

    public short value() {
	return this.val;
    }
}
