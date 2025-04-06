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

import osak.ext.communication.MyLog;
import osak.ext.ns3.core.Time;
import osak.ext.ns3.network.Address;
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
public class QueueDiscItem extends QueueItem {
    private Address m_address;// !< MAC destination address
    private short m_protocol; // !< L3 Protocol number
    private byte m_txq; // !< Transmission queue index
    private Time m_tstamp; // !< timestamp when the packet was enqueued

    /**
     * @brief Create a queue disc item.
     * @param p        the packet included in the created item.
     * @param addr     the destination MAC address
     * @param protocol the L3 protocol number
     */
    public QueueDiscItem(Packet p, final Address addr, short protocol){
	super(p);
	m_address = addr;
	m_protocol = protocol;
	m_txq = 0;
    }
    
    /*
     * Delete default constructor, copy constructor and assignment operator to avoid
     * misuse
     */
    // QueueDiscItem() = delete;
    // QueueDiscItem(const QueueDiscItem&) = delete;
    // QueueDiscItem& operator=(const QueueDiscItem&) = delete;

    /**
     * @brief Get the MAC address included in this item
     * @return the MAC address included in this item.
     */
    public Address GetAddress() {
	return m_address;
    }

    /**
     * @brief Get the L3 protocol included in this item
     * @return the L3 protocol included in this item.
     */
    public short GetProtocol() {
	return m_protocol;
    }

    /**
     * @brief Get the transmission queue index included in this item
     * @return the transmission queue index included in this item.
     */
    public byte GetTxQueueIndex() {
	return m_txq;
    }

    /**
     * @brief Set the transmission queue index to store in this item
     * @param txq the transmission queue index to store in this item.
     */
    public void SetTxQueueIndex(byte txq) {
	m_txq = txq;
    }

    /**
     * @brief Get the timestamp included in this item
     * @return the timestamp included in this item.
     */
    public Time GetTimeStamp() {
	return m_tstamp;
    }

    /**
     * @brief Set the timestamp included in this item
     * @param t the timestamp to include in this item.
     */
    public void SetTimeStamp(Time t) {
	m_tstamp = t;
    }

    /**
     * @brief Add the header to the packet
     *
     *        Subclasses may keep header and payload separate to allow manipulating
     *        the header, so this method allows to add the header to the packet
     *        before sending the packet to the device.
     */
    public void AddHeader() {
	// virtual
    }

    /**
     * @brief Print the item contents.
     * @param os output stream in which the data should be printed.
     */
    @Override
    public void Print(OutputStream os) {

    }

    /**
     * @brief Marks the packet as a substitute for dropping it, such as for Explicit
     *        Congestion Notification
     *
     * @return true if the packet is marked by this method or is already marked,
     *         false otherwise
     */
    public boolean Mark() {
	// abs virtual
	MyLog.logOut("The Mark method should be redefined by subclasses", MyLog.WARNING);
	return false;
    }

    /**
     * @brief Computes the hash of various fields of the packet header
     *
     *        This method just returns 0. Subclasses should implement a reasonable
     *        hash for their protocol type, such as hashing on the TCP/IP 5-tuple.
     *
     * @param perturbation hash perturbation value
     * @return the hash of various fields of the packet header
     */
    public int Hash(int perturbation) {
	// abs virtual
	MyLog.logOut("The Hash method should be redefined by subclasses", MyLog.WARNING);
	return 0;
    }
}
