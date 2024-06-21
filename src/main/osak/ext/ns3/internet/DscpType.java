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
 * DscpType DiffServ codepoints
 * 
 * The values correspond to the 6-bit DSCP codepoint within the 8-bit DS field
 * defined in \RFC{2474}. ECN bits are separately set with the SetEcn() method.
 * Codepoints are defined in Assured Forwarding (AF) \RFC{2597}, Expedited
 * Forwarding (EF) \RFC{2598}, and Default and Class Selector (CS) \RFC{2474}.
 * 
 * @author zhangrui
 * @since 1.0
 */
public enum DscpType {
    DscpDefault((byte) 0x00),
    // Prefixed with "DSCP" to avoid name clash (bug 1723)
    DSCP_CS1((byte) 0x08), // octal 020
    DSCP_AF11((byte) 0x0A), // octal 022
    DSCP_AF12((byte) 0x0C), // octal 024
    DSCP_AF13((byte) 0x0E), // octal 026

    DSCP_CS2((byte) 0x10), // octal 020
    DSCP_AF21((byte) 0x12), // octal 022
    DSCP_AF22((byte) 0x14), // octal 024
    DSCP_AF23((byte) 0x16), // octal 026

    DSCP_CS3((byte) 0x18), // octal 030
    DSCP_AF31((byte) 0x1A), // octal 032
    DSCP_AF32((byte) 0x1C), // octal 034
    DSCP_AF33((byte) 0x1E), // octal 036

    DSCP_CS4((byte) 0x20), // octal 040
    DSCP_AF41((byte) 0x22), // octal 042
    DSCP_AF42((byte) 0x24), // octal 044
    DSCP_AF43((byte) 0x26), // octal 046

    DSCP_CS5((byte) 0x28), // octal 050
    DSCP_EF((byte) 0x2E), // octal 056

    DSCP_CS6((byte) 0x30), // octal 060
    DSCP_CS7((byte) 0x38); // octal 070

    private byte value = 0;

    private DscpType(byte value) {
	this.value = value;
    }

    public static DscpType valueOf(byte value) {
	switch (value) {
	case 0x0:
	    return DscpDefault;

	case 0x8:
	    return DSCP_CS1;

	case 0xA:
	    return DSCP_AF11;

	case 0xC:
	    return DSCP_AF12;

	case 0xE:
	    return DSCP_AF13;

	case 0x10:
	    return DSCP_CS2;

	case 0x12:
	    return DSCP_AF21;

	case 0x14:
	    return DSCP_AF22;

	case 0x16:
	    return DSCP_AF23;

	case 0x18:
	    return DSCP_CS3;

	case 0x1A:
	    return DSCP_AF31;

	case 0x1C:
	    return DSCP_AF32;

	case 0x1E:
	    return DSCP_AF33;

	case 0x20:
	    return DSCP_CS4;

	case 0x22:
	    return DSCP_AF41;

	case 0x24:
	    return DSCP_AF42;

	case 0x26:
	    return DSCP_AF43;

	case 0x28:
	    return DSCP_CS5;

	case 0x2E:
	    return DSCP_EF;

	case 0x30:
	    return DSCP_CS6;

	case 0x38:
	    return DSCP_CS7;
	default:
	    throw new RuntimeException("Don't have this enum value");
	}
    }

    public byte value() {
	return this.value;
    }

}
