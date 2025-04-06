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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;

import osak.ext.ns3.aodv.RrepHeader;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.network.utils.Ipv4Address;
/**
 * TODO RrepHeaderTest
 * 
 * @author zhangrui
 * @since   1.0
 */
class RrepHeaderTest {

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
	RrepHeader header = new RrepHeader();
	header.SetAckRequired(true);
	header.SetOrigin(new Ipv4Address("10.1.1.3"));
	header.SetDst(new Ipv4Address("10.1.1.255"));
	header.SetPrefixSize((byte) 12);
	header.SetDstSeqno(100);
	header.SetHopCount((byte) 128);
	header.SetLifeTime((int) new Time(2, TimeUnit.SECONDS).getMillSeconds());

	byte[] rawdata = { (byte) 0x40, (byte) 0x0c, (byte) 0x80, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0xff,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		(byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xd0 };

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
	RrepHeader header = new RrepHeader();
	byte[] rawdata = { (byte) 0x40, (byte) 0x0c, (byte) 0x80, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0xff,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		(byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xd0 };

	header.Deserialize(ByteBuffer.wrap(rawdata));
	assertTrue(header.GetAckRequired());
	Ipv4Address dst = new Ipv4Address("10.1.1.255");
	Ipv4Address origin = new Ipv4Address("10.1.1.3");
	assertAll(() -> assertTrue(header.GetDst().equals(dst)),
		() -> assertTrue(header.GetOrigin().equals(origin)),
		() -> assertEquals(header.GetPrefixSize(), 12),
		() -> assertTrue(header.GetHopCount() == (byte) 128), // 不能直接与int进行比较
		() -> assertEquals(header.GetLifeTime(), 2000), 
		() -> assertEquals(header.GetDstSeqno(), 100));
    }

    @Test
    void test_Equals() {
	RrepHeader header = new RrepHeader();
	RrepHeader header2 = new RrepHeader();
	byte[] rawdata = { (byte) 0x40, (byte) 0x0c, (byte) 0x80, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0xff,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		(byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xd0 };
	header.Deserialize(ByteBuffer.wrap(rawdata));
	header2.Deserialize(ByteBuffer.wrap(rawdata));
	assertAll(
		() -> assertTrue(header.equals(header2)), 
		() -> assertFalse(header == header2));
    }


}
