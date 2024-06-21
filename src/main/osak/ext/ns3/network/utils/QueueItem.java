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

import java.io.OutputStream;

import osak.ext.ns3.network.Packet;

/**
 * Base class to represent items of packet Queues
 * <p>
 * An item stored in an ns-3 packet Queue contains a packet and possibly other
 * information. An item of the base class only contains a packet. Subclasses can
 * be derived from this base class to allow items to contain additional
 * information.
 * 
 * @author zhangrui
 * @since 1.0
 */
public class QueueItem {
    private Packet m_packet;

    /**
     * Create a queue item containing a packet.
     * 
     * @param p the packet included in the created item.
     */
    public QueueItem(Packet p) {
	m_packet = p;
    }

    /*
     * Delete default constructor, copy constructor and assignment operator to avoid
     * misuse
     */
    // QueueItem() = delete;
    // QueueItem(const QueueItem&) = delete;
    // QueueItem& operator=(const QueueItem&) = delete;

    /**
     * \return the packet included in this item.
     */
    public Packet GetPacket() {
	return m_packet;
    }

    /**
     * Use this method (instead of GetPacket ()->GetSize ()) to get the packet size
     *
     * Subclasses may keep header and payload separate to allow manipulating the
     * header, so using this method ensures that the correct packet size is
     * returned.
     *
     * @return the size of the packet included in this item.
     */
    public int GetSize() {
	// virtual
	assert (m_packet != null);
	return m_packet.GetSize();
    }

    /**
     * @enum Uint8Values
     * @brief 1-byte fields of the packet whose value can be retrieved, if present
     */
    public enum Uint8Values {
	IP_DSFIELD
    };

    /**
     * Retrieve the value of a given field from the packet, if present
     * 
     * @param field the field whose value has to be retrieved
     * @param value the output parameter to store the retrieved value
     *
     * @return true if the requested field is present in the packet, false
     *         otherwise.
     */
    public boolean GetUint8Value(Uint8Values field, byte value) {
	// virtual
	return false;
    }

    /**
     * @brief Print the item contents.
     * @param os output stream in which the data should be printed.
     */
    public void Print(OutputStream os) {
	// os << GetPacket();
    }
}
