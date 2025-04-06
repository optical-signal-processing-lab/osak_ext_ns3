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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import osak.ext.communication.MyLog;
import osak.ext.ns3.core.Time;

/**
 * A network Node.
 * <p>
 * 
 * <pre>
 * This class holds together:
 *   - a list of NetDevice objects which represent the network interfaces
 *     of this node which are connected to other Node instances through
 *     Channel instances.
 *   - a list of Application objects which represent the userspace
 *     traffic generation applications which interact with the Node
 *     through the Socket API.
 *   - a node Id: a unique per-node identifier.
 *   - a system Id: a unique Id used for parallel simulations.
 *
 * Every Node created is added to the NodeList automatically.
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public class Node {

    /**
     * @brief Get the type ID.
     * @return the object TypeId
     */
    // static TypeId GetTypeId();

    public Node() {
	m_id = 0;
	m_sid = 0;
	Construct();
    }

    /**
     * @param systemId a unique integer used for parallel simulations.
     */
    public Node(int systemId) {
	m_id = 0;
	m_sid = systemId;
	Construct();
    }


    /**
     *
     * This unique id happens to be also the index of the Node into the NodeList.
     * 
     * @returns the unique id of this node.
     */
    public int GetId() {
	return m_id;
    }

    /**
     * In the future, osak.ext.ns3 nodes may have clock that returned a local time different
     * from the virtual time Simulator::Now(). This function is currently a
     * placeholder to ease the development of this feature. For now, it is only an
     * alias to Simulator::Now()
     *
     * @return The time as seen by this node
     */
    public Time GetLocalTime() {
	return Time.Now();
    }

    /**
     * @returns the system id for parallel simulations associated to this node.
     */
    public int GetSystemId() {
	return m_sid;
    }

    /**
     * @brief Associate a NetDevice to this node.
     *
     * @param device NetDevice to associate to this node.
     * @returns the index of the NetDevice into the Node's list of NetDevice.
     */
    public int AddDevice(NetDevice device) {
	int index = m_devices.size();
	m_devices.add(device);
	device.SetNode(this);
	device.SetIfIndex(index);
	device.SetReceiveCallback((a, b, c, d) -> this.NonPromiscReceiveFromDevice(a, b, c, d));
	//Simulator::ScheduleWithContext(GetId(), Seconds(0.0), &NetDevice::Initialize, device);
	NotifyDeviceAdded(device);
	return index;
    }

    /**
     * @brief Retrieve the index-th NetDevice associated to this node.
     *
     * @param index the index of the requested NetDevice
     * @returns the requested NetDevice.
     */
    public NetDevice GetDevice(int index) {
	assert (index < m_devices.size());
	return m_devices.get(index);
    }

    /**
     * @returns the number of NetDevice instances associated to this Node.
     */
    public int GetNDevices() {
	return m_devices.size();
    }

    /**
     * @brief Associate an Application to this Node.
     *
     * @param application Application to associate to this node.
     * @returns the index of the Application within the Node's list of Application.
     */
    public int AddApplication(Application application) {
	int index = m_applications.size();
	m_applications.add(application);
	application.SetNode(this);
	//Simulator::ScheduleWithContext(GetId(), Seconds(0.0), &Application::Initialize, application);
	return index;
    }

    /**
     * @brief Retrieve the index-th Application associated to this node.
     *
     * @param index the index of the requested Application
     * @returns the requested Application.
     */
    public Application GetApplication(int index) {
	assert (index < m_applications.size());
	return m_applications.get(index);
    }

    /**
     * @returns the number of Application instances associated to this Node.
     */
    public int GetNApplications() {
	return m_applications.size();
    }

    /**
     * A protocol handler
     *
     * @param device     a pointer to the net device which received the packet
     * @param packet     the packet received
     * @param protocol   the 16 bit protocol number associated with this packet.
     *                   This protocol number is expected to be the same protocol
     *                   number given to the Send method by the user on the sender
     *                   side.
     * @param sender     the address of the sender
     * @param receiver   the address of the receiver; Note: this value is only valid
     *                   for promiscuous mode protocol handlers. Note: If the L2
     *                   protocol does not use L2 addresses, the address reported
     *                   here is the value of device->GetAddress().
     * @param packetType type of packet received
     *                   (broadcast/multicast/unicast/otherhost); Note: this value
     *                   is only valid for promiscuous mode protocol handlers.
     */
    public interface ProtocolHandler {
	void callback(NetDevice device, final Packet p, short protocol, final Address addr, final Address addr2,
		PacketType pt);
    }

    // typedef Callback<void,
    // Ptr<NetDevice>,
    // Ptr<const Packet>,
    // uint16_t,
    // const Address&,
    // const Address&,
    // NetDevice::PacketType>
    // ProtocolHandler;
    /**
     * @param handler the handler to register
     * @param protocolType the type of protocol this handler is
     *        interested in. This protocol type is a so-called
     *        EtherType, as registered here:
     *        http://standards.ieee.org/regauth/ethertype/eth.txt
     *        the value zero is interpreted as matching all
     *        protocols.
     * @param device the device attached to this handler. If the
     *        value is zero, the handler is attached to all
     *        devices on this node.
     * @param promiscuous whether to register a promiscuous mode handler
     */
    public void RegisterProtocolHandler(ProtocolHandler handler, short protocolType, NetDevice device,
	    boolean promiscuous) {

	ProtocolHandlerEntry entry = new ProtocolHandlerEntry();
	entry.handler = handler;
	entry.protocol = protocolType;
	entry.device = device;
	entry.promiscuous = promiscuous;

	// On demand enable promiscuous mode in netdevices
	if(promiscuous) {
	    if(device==null) {
		for(NetDevice dev:m_devices) {
		    dev.SetPromiscReceiveCallback((a,b,c,d,e,f)->this.PromiscReceiveFromDevice(a,b,c,d,e,f));
		}
	    }
	    else {
		device.SetPromiscReceiveCallback((a,b,c,d,e,f)->this.PromiscReceiveFromDevice(a,b,c,d,e,f));
	    }
	}
	m_handlers.add(entry);
    }

    public void RegisterProtocolHandler(ProtocolHandler handler, short protocolType, NetDevice device) {

	RegisterProtocolHandler(handler, protocolType, device, false);
    }

    /**
     *
     * After this call returns, the input handler will never be invoked anymore.
     * 
     * @param handler the handler to unregister
     */
    public void UnregisterProtocolHandler(ProtocolHandler handler) {
	for (ProtocolHandlerEntry h : m_handlers) {
	    // TODO: need to check
	    if (h.handler.equals(handler)) {
		m_handlers.remove(h);
		break;
	    }
	}
    }

    /**
     * A callback invoked whenever a device is added to a node.
     */
    public interface DeviceAdditionListener {
	void callback(NetDevice device);
    }

    // typedef Callback<void, Ptr<NetDevice>> DeviceAdditionListener;
    /**
     *
     * Add a new listener to the list of listeners for the device-added event. When
     * a new listener is added, it is notified of the existence of all already-added
     * devices to make discovery of devices easier.
     * 
     * @param listener the listener to add
     */
    public void RegisterDeviceAdditionListener(DeviceAdditionListener listener) {
	m_deviceAdditionListeners.add(listener);
	// and, then, notify the new listener about all existing devices.
	for (NetDevice i : m_devices) {
	    listener.callback(i);
	}
    }

    /**
     *
     * Remove an existing listener from the list of listeners for the device-added
     * event.
     * 
     * @param listener the listener to remove
     */
    public void UnregisterDeviceAdditionListener(DeviceAdditionListener listener) {
	for (DeviceAdditionListener i : m_deviceAdditionListeners) {
	    if (i.equals(listener)) {
		m_deviceAdditionListeners.remove(i);
		break;
	    }
	}
    }

    /**
     * @returns true if checksums are enabled, false otherwise.
     */
    public static boolean ChecksumEnabled() {
	// TODO: A global switch to enable all checksums for all protocols.
	return true;
    }

    /**
     * The dispose method. Subclasses must override this method
     * and must chain up to it by calling Node::DoDispose at the
     * end of their own DoDispose method.
     */
    protected void DoDispose() {
	m_deviceAdditionListeners.clear();
	m_handlers.clear();
	m_devices.clear();
	m_applications.clear();
    }

    protected void DoInitialize() {
	//
    }

    /**
     * @brief Notifies all the DeviceAdditionListener about the new device added.
     * @param device the added device to notify.
     */
    private void NotifyDeviceAdded(NetDevice device) {
	for (DeviceAdditionListener i : m_deviceAdditionListeners) {
	    i.callback(device);
	}
    }

    /**
     * @brief Receive a packet from a device in non-promiscuous mode.
     * @param device   the device
     * @param packet   the packet
     * @param protocol the protocol
     * @param from     the sender
     * @returns true if the packet has been delivered to a protocol handler.
     */
    private boolean NonPromiscReceiveFromDevice(NetDevice device, final Packet packet, short protocol,
	    final Address from) {
	return ReceiveFromDevice(device, packet, protocol, from, device.GetAddress(), PacketType.valueOf(0), false);
    }

    /**
     * @brief Receive a packet from a device in promiscuous mode.
     * @param device     the device
     * @param packet     the packet
     * @param protocol   the protocol
     * @param from       the sender
     * @param to         the destination
     * @param packetType the packet type
     * @returns true if the packet has been delivered to a protocol handler.
     */
    private boolean PromiscReceiveFromDevice(NetDevice device, final Packet packet, short protocol, final Address from,
	    final Address to, PacketType packetType) {
	return ReceiveFromDevice(device, packet, protocol, from, to, packetType, true);
    }

    /**
     * @brief Receive a packet from a device.
     * @param device     the device
     * @param packet     the packet
     * @param protocol   the protocol
     * @param from       the sender
     * @param to         the destination
     * @param packetType the packet type
     * @param promisc    true if received in promiscuous mode
     * @returns true if the packet has been delivered to a protocol handler.
     */
    private boolean ReceiveFromDevice(NetDevice device, final Packet packet, short protocol, final Address from,
	    final Address to, PacketType packetType, boolean promisc) {
	MyLog.logOut("Node " + GetId() + " ReceiveFromDevice:  dev " + device.GetIfIndex() + " (type="
		+ /* device.GetInstanceTypeId().GetName() + */ ") Packet UID " + packet.GetUid(), MyLog.DEBUG);
	boolean found = false;
	for (ProtocolHandlerEntry i : m_handlers) {
	    if (i.device == null || i.device.equals(device)) {
		if (i.protocol == 0 || i.protocol == protocol) {
		    if (promisc == i.promiscuous) {
			i.handler.callback(device, packet, protocol, from, to, packetType);
			found = true;
		    }
		}
	    }
	}
	return found;
    }

    /**
     * @brief Finish node's construction by setting the correct node ID.
     */
    private void Construct() {
	// m_id = NodeList::Add(this);
    }

    /**
     * @brief Protocol handler entry. This structure is used to demultiplex all the
     *        protocols.
     */
    private class ProtocolHandlerEntry {
	ProtocolHandler handler = null; // !< the protocol handler
	NetDevice device = null; // !< the NetDevice
	short protocol; // !< the protocol number
	boolean promiscuous; // !< true if it is a promiscuous handler
    };

    /// Typedef for protocol handlers container
    // typedef std::vector<struct Node::ProtocolHandlerEntry> ProtocolHandlerList;
    /// Typedef for NetDevice addition listeners container
    // typedef std::vector<DeviceAdditionListener> DeviceAdditionListenerList;

    private int m_id; // !< Node id for this node
    private int m_sid; // !< System id for this node
    private List<NetDevice> m_devices = new LinkedList<>(); // !< Devices associated to this node
    private List<Application> m_applications = new LinkedList<>(); // !< Applications associated to this node
    private List<ProtocolHandlerEntry> m_handlers = new LinkedList<>(); // !< Protocol handlers in the node
    /* Device addition listeners in the node */
    private List<DeviceAdditionListener> m_deviceAdditionListeners = new LinkedList<>();

    @Override
    public int hashCode() {
	return Objects.hash(m_applications, m_deviceAdditionListeners, m_devices, m_handlers, m_id, m_sid);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!(obj instanceof Node))
	    return false;
	Node other = (Node) obj;
	return Objects.equals(m_applications, other.m_applications)
		&& Objects.equals(m_deviceAdditionListeners, other.m_deviceAdditionListeners)
		&& Objects.equals(m_devices, other.m_devices) && Objects.equals(m_handlers, other.m_handlers)
		&& m_id == other.m_id && m_sid == other.m_sid;
    }
}
