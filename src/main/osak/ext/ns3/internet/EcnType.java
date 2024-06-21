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

/**
 * TODO EcnType
 * 
 * @author zhangrui
 * @since   1.0
 */
public enum EcnType {
    ECN_NotECT((byte) 0), ECN_ECT1((byte) 1), ECN_ECT0((byte) 2), ECN_CE((byte) 3);

    private byte value = 0;

    private EcnType(byte value) {
	this.value = value;
    }

    public static EcnType valueOf(byte value) {
	switch (value) {
	case 0:
	    return ECN_NotECT;
	case 1:
	    return ECN_ECT1;
	case 2:
	    return ECN_ECT0;
	case 3:
	    return ECN_CE;
	default:
	    throw new RuntimeException("Don't have this enum value");
	}
    }

    public byte value() {
	return this.value;
    }

}
