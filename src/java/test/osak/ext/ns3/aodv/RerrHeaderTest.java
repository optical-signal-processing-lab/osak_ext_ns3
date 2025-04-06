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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;

import osak.ext.ns3.aodv.RerrHeader;
import osak.ext.ns3.network.utils.Ipv4Address;
/**
 * TODO RerrHeaderTest
 * 
 * @author zhangrui
 * @since   1.0
 */
class RerrHeaderTest {

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
    void test_RerrHeaderByte() {
	RerrHeader rerrHeader = new RerrHeader();
	rerrHeader.AddUnDestination(new Ipv4Address("10.1.1.3"), 0);
	rerrHeader.AddUnDestination(new Ipv4Address("10.1.1.5"), 1);
	rerrHeader.SetNoDelete(true);

	ByteBuffer buffer = ByteBuffer.allocate(128);
	rerrHeader.Serialize(buffer);

	int buffersize = buffer.position();
	buffer.flip();
	byte[] serializeBuffer = new byte[buffersize];
	buffer.get(serializeBuffer);
	byte header[] = { (byte) 0x80, (byte) 0x00, (byte) 0x02, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x05,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
	assertArrayEquals(serializeBuffer, header);
    }

    @Test
    void test_RerrHeaderFlag() {
	RerrHeader rerrHeader = new RerrHeader();
	rerrHeader.SetNoDelete(true);
	assertTrue(rerrHeader.GetNoDelete());
    }

    @Test
    void test_RerrHeaderDestCount() {
	RerrHeader rerrHeader = new RerrHeader();
	rerrHeader.AddUnDestination(new Ipv4Address("10.1.1.3"), 0);
	assertEquals(rerrHeader.GetDestCount(), 1);
	rerrHeader.AddUnDestination(new Ipv4Address("10.1.1.5"), 1);
	assertEquals(rerrHeader.GetDestCount(), 2);
	assertEquals(rerrHeader.GetSerializedSize(), 19);
	rerrHeader.RemoveUnDestination(new Ipv4Address("10.1.1.5"), 1);
	assertEquals(rerrHeader.GetDestCount(), 1);
	rerrHeader.Clear();
	assertEquals(rerrHeader.GetDestCount(), 0);
    }

    @Test
    void test_RerrHeaderRemove() {
	RerrHeader rerrHeader = new RerrHeader();
	rerrHeader.AddUnDestination(new Ipv4Address("10.1.1.3"), 0);
	rerrHeader.RemoveUnDestination(new Ipv4Address("10.1.1.3"), 1);
	assertEquals(rerrHeader.GetDestCount(), 1);
	rerrHeader.RemoveUnDestination(new Ipv4Address("10.1.1.3"), 0);
	assertEquals(rerrHeader.GetDestCount(), 0);
    }

    @Test
    void test_RerrHeaderEquals() {
	byte[] rawData = { (byte) 0x80, (byte) 0x00, (byte) 0x02, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x03,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x01, (byte) 0x01, (byte) 0x05,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
	RerrHeader rerrHeader = new RerrHeader();
	RerrHeader rerrHeader2 = new RerrHeader();
	assertEquals(rerrHeader.Deserialize(ByteBuffer.wrap(rawData)), 19);
	rerrHeader2.Deserialize(ByteBuffer.wrap(rawData));
	assertTrue(rerrHeader.equals(rerrHeader2));
	assertFalse(rerrHeader == rerrHeader2);

    }

}
