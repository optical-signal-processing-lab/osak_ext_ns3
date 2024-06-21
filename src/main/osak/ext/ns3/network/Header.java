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
import java.nio.ByteBuffer;

/**
 * TODO Header
 * 
 * @author zhangrui
 * @since   1.0
 */
public interface Header {
    /**
     * @returns the expected size of the header.
     *
     *          This method is used by Packet::AddHeader to store a header into the
     *          byte buffer of a packet. This method should return the number of
     *          bytes which are needed to store the full header data by Serialize.
     */
    int GetSerializedSize();

    /**
     * @param start an iterator which points to where the header should be written.
     *
     *              This method is used by Packet::AddHeader to store a header into
     *              the byte buffer of a packet. The data written is expected to
     *              match bit-for-bit the representation of this header in a real
     *              network.
     */
    void Serialize(ByteBuffer buffer);

    /**
     * @param start an iterator which points to where the header should read from.
     * @returns the number of bytes read.
     *
     *          This method is used by Packet::RemoveHeader to re-create a header
     *          from the byte buffer of a packet. The data read is expected to match
     *          bit-for-bit the representation of this header in real networks.
     *
     *          Note that data is not actually removed from the buffer to which the
     *          iterator points. Both Packet::RemoveHeader() and
     *          Packet::PeekHeader() call Deserialize(), but only the RemoveHeader()
     *          has additional statements to remove the header bytes from the
     *          underlying buffer and associated metadata.
     */
    int Deserialize(ByteBuffer buffer);
}
