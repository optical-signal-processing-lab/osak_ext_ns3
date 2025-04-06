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
 * TODO Trailer
 * 
 * @author zhangrui
 * @since   1.0
 */
public interface Trailer {
    /**
     * @returns the expected size of the trailer.
     *
     *          This method is used by Packet::AddTrailer to store a trailer into
     *          the byte buffer of a packet. This method should return the number of
     *          bytes which are needed to store the full trailer data by Serialize.
     */
    int GetSerializedSize();

    /**
     * @param start an iterator which points to where the trailer should be written.
     *
     *              This method is used by Packet::AddTrailer to store a header into
     *              the byte buffer of a packet. The data written is expected to
     *              match bit-for-bit the representation of this trailer in real
     *              networks. The input iterator points to the end of the area where
     *              the data shall be written. This method is thus expected to call
     *              Buffer::Iterator::Prev prior to actually writing any data.
     */
    void Serialize(ByteBuffer buffer);

    /**
     * @param end an iterator which points to the end of the buffer where the
     *            trailer should be read from.
     * @returns the number of bytes read.
     *
     *          This method is used by Packet::RemoveTrailer to re-create a trailer
     *          from the byte buffer of a packet. The data read is expected to match
     *          bit-for-bit the representation of this trailer in real networks. The
     *          input iterator points to the end of the area where the data shall be
     *          read from. This method is thus expected to call
     *          Buffer::Iterator::Prev prior to actually reading any data.
     */
    int Deserialize(ByteBuffer buffer);
}
