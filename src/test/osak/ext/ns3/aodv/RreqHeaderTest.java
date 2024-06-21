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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;

import osak.ext.ns3.aodv.RreqHeader;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO RreqHeaderTest
 * 
 * @author zhangrui
 * @since   1.0
 */
class RreqHeaderTest {
    static byte[] rawdata = { (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
	    (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	    (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
    /**
     * @throws java.lang.Exception
     */
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test_Serialized() {
	RreqHeader header = new RreqHeader();
	header.SetGratuitousRrep(true);
	header.SetId(1);
	header.SetDst(new Ipv4Address("10.1.1.5"));
	header.SetOrigin(new Ipv4Address("10.1.1.1"));
	header.SetDstSeqno(0);
	header.SetOriginSeqno(1);
	header.SetUnknownSeqno(true);

	ByteBuffer buffer = ByteBuffer.allocate(128);
	header.Serialize(buffer);
	int size = header.GetSerializedSize();
	byte[] sdata = new byte[size];
	buffer.flip();
	buffer.get(sdata);
	assertArrayEquals(sdata, rawdata);
    }

    @Test
    void test_DeSerialized() {
	RreqHeader header = new RreqHeader();
	header.Deserialize(ByteBuffer.wrap(rawdata));
	assertAll(
		() -> assertTrue(header.GetGratuitousRrep()),
		() -> assertEquals(header.GetId(), 1),
		() -> assertTrue(header.GetDst().equals(new Ipv4Address("10.1.1.5"))),
		() -> assertTrue(header.GetOrigin().equals(new Ipv4Address("10.1.1.1"))),
		() -> assertEquals(header.GetDstSeqno(), 0),
		() -> assertEquals(header.GetOriginSeqno(), 1),
		() -> assertTrue(header.GetUnknownSeqno()),
		() -> assertFalse(header.GetDestinationOnly())
		);
    }

    @Test
    void test_Equals() {
	RreqHeader header = new RreqHeader();
	header.Deserialize(ByteBuffer.wrap(rawdata));
	RreqHeader header2 = new RreqHeader();
	header2.Deserialize(ByteBuffer.wrap(rawdata));
	assertAll(
		() -> assertTrue(header.equals(header2)), 
		() -> assertFalse(header == header2));
    }
}
