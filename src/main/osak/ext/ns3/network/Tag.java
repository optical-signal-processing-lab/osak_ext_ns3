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

/**
 * TODO Tag
 * 
 * @author zhangrui
 * @since   1.0
 */
public interface Tag {
    /**
     *
     * This method is typically invoked by Packet::AddPacketTag or
     * Packet::AddByteTag just prior to calling Tag::Serialize.
     * 
     * @returns the number of bytes required to serialize the data of the tag.
     */
    int GetSerializedSize();

    /**
     *
     * Write the content of the tag in the provided tag buffer. DO NOT attempt to
     * write more bytes than you requested with Tag::GetSerializedSize.
     * 
     * @param i the buffer to write data into.
     */
    void Serialize(TagBuffer i);

    /**
     *
     * Read the content of the tag from the provided tag buffer. DO NOT attempt to
     * read more bytes than you wrote with Tag::Serialize.
     * 
     * @param i the buffer to read data from.
     */
    void Deserialize(TagBuffer i);

    /**
     *
     * This method is typically invoked from the Packet::PrintByteTags or
     * Packet::PrintPacketTags methods.
     * 
     * @param os the stream to print to
     */
    void Print(/* std::ostream& os */);
}
