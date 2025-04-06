#include "optical-device.h"

#include "optical-channel.h"
#include "satellite-mobility-model.h"

#include "ns3/error-model.h"
#include "ns3/ethernet-header.h"
#include "ns3/ethernet-trailer.h"
#include "ns3/llc-snap-header.h"
#include "ns3/log.h"
#include "ns3/mac48-address.h"
#include "ns3/pointer.h"
#include "ns3/ppp-header.h"
#include "ns3/queue.h"
#include "ns3/simulator.h"
#include "ns3/trace-source-accessor.h"
#include "ns3/uinteger.h"

namespace ns3
{

NS_LOG_COMPONENT_DEFINE("OpticalDevice");

NS_OBJECT_ENSURE_REGISTERED(OpticalDevice);

// ============================================== //
//         override from ns3::NetDevice           //
// ============================================== //
TypeId
OpticalDevice::GetTypeId()
{
    static TypeId tid =
        TypeId("ns3::OpticalDevice")
            .SetParent<NetDevice>()
            .SetGroupName("satellite")
            .AddConstructor<OpticalDevice>()
            .AddAttribute("Mtu",
                          "The MAC-level Maximum Transmission Unit",
                          UintegerValue(DEFAULT_MTU),
                          MakeUintegerAccessor(&OpticalDevice::SetMtu, &OpticalDevice::GetMtu),
                          MakeUintegerChecker<uint16_t>())
            .AddAttribute("Address",
                          "The MAC address of this device.",
                          Mac48AddressValue(Mac48Address("ff:ff:ff:ff:ff:ff")),
                          MakeMac48AddressAccessor(&OpticalDevice::m_address),
                          MakeMac48AddressChecker())
            .AddAttribute("DataRate",
                          "The default data rate for point to point links",
                          DataRateValue(DataRate("32768b/s")),
                          MakeDataRateAccessor(&OpticalDevice::m_bps),
                          MakeDataRateChecker())
            .AddAttribute("ReceiveErrorModel",
                          "The receiver error model used to simulate packet loss",
                          PointerValue(),
                          MakePointerAccessor(&OpticalDevice::m_receiveErrorModel),
                          MakePointerChecker<ErrorModel>())
            //
            // Transmit queueing discipline for the device which includes its own set
            // of trace hooks.
            //
            .AddAttribute("TxQueue",
                          "A queue to use as the transmit queue in the device.",
                          PointerValue(),
                          MakePointerAccessor(&OpticalDevice::m_queue),
                          MakePointerChecker<Queue<Packet>>())

            //
            // Trace sources at the "top" of the net device, where packets transition
            // to/from higher layers.
            //
            .AddTraceSource("MacTx",
                            "Trace source indicating a packet has arrived "
                            "for transmission by this device",
                            MakeTraceSourceAccessor(&OpticalDevice::m_macTxTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("MacTxDrop",
                            "Trace source indicating a packet has been dropped "
                            "by the device before transmission",
                            MakeTraceSourceAccessor(&OpticalDevice::m_macTxDropTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("MacPromiscRx",
                            "A packet has been received by this device, "
                            "has been passed up from the physical layer "
                            "and is being forwarded up the local protocol stack.  "
                            "This is a promiscuous trace,",
                            MakeTraceSourceAccessor(&OpticalDevice::m_macPromiscRxTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("MacRx",
                            "A packet has been received by this device, "
                            "has been passed up from the physical layer "
                            "and is being forwarded up the local protocol stack.  "
                            "This is a non-promiscuous trace,",
                            MakeTraceSourceAccessor(&OpticalDevice::m_macRxTrace),
                            "ns3::Packet::TracedCallback")
#if 0
    // Not currently implemented for this device
    .AddTraceSource ("MacRxDrop",
                     "Trace source indicating a packet was dropped "
                     "before being forwarded up the stack",
                     MakeTraceSourceAccessor (&OpticalDevice::m_macRxDropTrace),
                     "ns3::Packet::TracedCallback")
#endif
            //
            // Trace sources at the "bottom" of the net device, where packets transition
            // to/from the channel.
            //
            .AddTraceSource("PhyTxBegin",
                            "Trace source indicating a packet has begun "
                            "transmitting over the channel",
                            MakeTraceSourceAccessor(&OpticalDevice::m_phyTxBeginTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("PhyTxEnd",
                            "Trace source indicating a packet has been "
                            "completely transmitted over the channel",
                            MakeTraceSourceAccessor(&OpticalDevice::m_phyTxEndTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("PhyTxDrop",
                            "Trace source indicating a packet has been "
                            "dropped by the device during transmission",
                            MakeTraceSourceAccessor(&OpticalDevice::m_phyTxDropTrace),
                            "ns3::Packet::TracedCallback")
#if 0
    // Not currently implemented for this device
    .AddTraceSource ("PhyRxBegin",
                     "Trace source indicating a packet has begun "
                     "being received by the device",
                     MakeTraceSourceAccessor (&OpticalDevice::m_phyRxBeginTrace),
                     "ns3::Packet::TracedCallback")
#endif
            .AddTraceSource("PhyRxEnd",
                            "Trace source indicating a packet has been "
                            "completely received by the device",
                            MakeTraceSourceAccessor(&OpticalDevice::m_phyRxEndTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("PhyRxDrop",
                            "Trace source indicating a packet has been "
                            "dropped by the device during reception",
                            MakeTraceSourceAccessor(&OpticalDevice::m_phyRxDropTrace),
                            "ns3::Packet::TracedCallback")

            //
            // Trace sources designed to simulate a packet sniffer facility (tcpdump).
            // Note that there is really no difference between promiscuous and
            // non-promiscuous traces in a point-to-point link.
            //
            .AddTraceSource("Sniffer",
                            "Trace source simulating a non-promiscuous packet sniffer "
                            "attached to the device",
                            MakeTraceSourceAccessor(&OpticalDevice::m_snifferTrace),
                            "ns3::Packet::TracedCallback")
            .AddTraceSource("PromiscSniffer",
                            "Trace source simulating a promiscuous packet sniffer "
                            "attached to the device",
                            MakeTraceSourceAccessor(&OpticalDevice::m_promiscSnifferTrace),
                            "ns3::Packet::TracedCallback");
    return tid;
}

OpticalDevice::OpticalDevice()
{
    NS_LOG_FUNCTION(this);
}

OpticalDevice::~OpticalDevice()
{
    NS_LOG_FUNCTION(this);
}

void
OpticalDevice::DoDispose()
{
    NS_LOG_FUNCTION(this);
    m_node = nullptr;
    m_receiveErrorModel = nullptr;
    m_currentPkt = nullptr;
    m_queue = nullptr;
    m_channel = nullptr;
    m_mobility = nullptr;
    NetDevice::DoDispose();
}

Ptr<Channel>
OpticalDevice::GetChannel() const
{
    return m_channel;
}

void
OpticalDevice::NotifyLinkUp()
{
    NS_LOG_FUNCTION(this);
    m_linkUp = true;
    m_linkChangeCallbacks();
}

void
OpticalDevice::SetIfIndex(const uint32_t index)
{
    NS_LOG_FUNCTION(this);
    m_ifIndex = index;
}

uint32_t
OpticalDevice::GetIfIndex() const
{
    return m_ifIndex;
}

//
// This is a point-to-point device, so we really don't need any kind of address
// information.  However, the base class NetDevice wants us to define the
// methods to get and set the address.  Rather than be rude and assert, we let
// clients get and set the address, but simply ignore them.

void
OpticalDevice::SetAddress(Address address)
{
    NS_LOG_FUNCTION(this << address);
    m_address = Mac48Address::ConvertFrom(address);
}

Address
OpticalDevice::GetAddress() const
{
    return m_address;
}

bool
OpticalDevice::IsLinkUp() const
{
    NS_LOG_FUNCTION(this);
    return m_linkUp;
}

void
OpticalDevice::AddLinkChangeCallback(Callback<void> callback)
{
    NS_LOG_FUNCTION(this);
    // std::ostringstream oss;
    // oss << "NodeList/" << GetNode()->GetId() << "/DeviceList/" << m_ifIndex << "/";
    // m_linkChangeCallbacks.Connect(callback, oss.str());
}

//
// This is a point-to-point device, so every transmission is a broadcast to
// all of the devices on the network.
//
bool
OpticalDevice::IsBroadcast() const
{
    NS_LOG_FUNCTION(this);
    return true;
}

//
// We don't really need any addressing information since this is a
// point-to-point device.  The base class NetDevice wants us to return a
// broadcast address, so we make up something reasonable.
//
Address
OpticalDevice::GetBroadcast() const
{
    NS_LOG_FUNCTION(this);
    return Mac48Address::GetBroadcast();
}

bool
OpticalDevice::IsMulticast() const
{
    NS_LOG_FUNCTION(this);
    return false;
}

Address
OpticalDevice::GetMulticast(Ipv4Address multicastGroup) const
{
    NS_LOG_FUNCTION(this);
    return Mac48Address::GetMulticast(multicastGroup);
}

Address
OpticalDevice::GetMulticast(Ipv6Address addr) const
{
    NS_LOG_FUNCTION(this << addr);
    return Mac48Address::GetMulticast(addr);
}

bool
OpticalDevice::IsPointToPoint() const
{
    NS_LOG_FUNCTION(this);
    return false;
}

bool
OpticalDevice::IsBridge() const
{
    NS_LOG_FUNCTION(this);
    return false;
}

bool
OpticalDevice::Send(Ptr<Packet> packet, const Address& dest, uint16_t protocolNumber)
{
    NS_LOG_FUNCTION(this << packet << dest << protocolNumber);

    return SendFrom(packet, m_address, dest, protocolNumber);
}

bool
OpticalDevice::SendFrom(Ptr<Packet> packet,
                          const Address& source,
                          const Address& dest,
                          uint16_t protocolNumber)
{
    NS_LOG_FUNCTION(this << packet << source << dest << protocolNumber);
    NS_ASSERT(Mac48Address::IsMatchingType(dest));
    NS_ASSERT(Mac48Address::IsMatchingType(source));

    Mac48Address realTo = Mac48Address::ConvertFrom(dest);
    Mac48Address realFrom = Mac48Address::ConvertFrom(source);

    // If IsLinkUp() is false it means there is no channel to send any packet
    // over so we just hit the drop trace on the packet and return an error.
    if (!IsLinkUp() || m_channel == nullptr)
    {
        NS_LOG_INFO("Link Down drop." << packet);
        m_macTxDropTrace(packet);
        return false;
    }

    m_macTxTrace(packet);

    // Add Ethernet header
    EthernetHeader header(false);
    header.SetSource(realFrom);
    header.SetDestination(realTo);

    EthernetTrailer trailer;
    // USE DIX:
    header.SetLengthType(protocolNumber);
    if (packet->GetSize() < 46)
    {
        uint8_t buffer[46];
        memset(buffer, 0, 46);
        Ptr<Packet> padd = Create<Packet>(buffer, 46 - packet->GetSize());
        packet->AddAtEnd(padd);
    }
    packet->AddHeader(header);
    if (Node::ChecksumEnabled())
    {
        trailer.EnableFcs(true);
    }
    trailer.CalcFcs(packet);
    packet->AddTrailer(trailer);
    // add over

    // We should enqueue and dequeue the packet to hit the tracing hooks.
    if (m_queue->Enqueue(packet))
    {
        // If the channel is ready for transition we send the packet right now
        if (m_txMachineState == READY)
        {
            packet = m_queue->Dequeue();
            m_snifferTrace(packet);
            m_promiscSnifferTrace(packet);
            bool ret = TransmitStart(packet);
            return ret;
        }
        return true;
    }
    // Enqueue may fail (overflow)
    NS_LOG_INFO("Overflow drop." << packet);
    m_macTxDropTrace(packet);
    return false;
}

Ptr<Node>
OpticalDevice::GetNode() const
{
    return m_node;
}

void
OpticalDevice::SetNode(Ptr<Node> node)
{
    NS_LOG_FUNCTION(this);
    m_node = node;
    m_mobility = m_node->GetObject<MobilityModel>();
    NS_ASSERT_MSG(m_mobility != nullptr, "Can't find mobility model in node.");
}

bool
OpticalDevice::NeedsArp() const
{
    NS_LOG_FUNCTION(this);
    return false;
}

void
OpticalDevice::SetReceiveCallback(NetDevice::ReceiveCallback cb)
{
    m_rxCallback = cb;
}

void
OpticalDevice::SetPromiscReceiveCallback(NetDevice::PromiscReceiveCallback cb)
{
    m_promiscCallback = cb;
}

bool
OpticalDevice::SupportsSendFrom() const
{
    NS_LOG_FUNCTION(this);
    return true;
}

bool
OpticalDevice::SetMtu(uint16_t mtu)
{
    NS_LOG_FUNCTION(this << mtu);
    m_mtu = mtu;
    return true;
}

uint16_t
OpticalDevice::GetMtu() const
{
    NS_LOG_FUNCTION(this);
    return m_mtu;
}
} // namespace ns3

// ============================================== //
//                   NetDevice                    //
// ============================================== //
namespace ns3
{
void
OpticalDevice::SetDataRate(DataRate bps)
{
    NS_LOG_FUNCTION(this);
    m_bps = bps;
}

void
OpticalDevice::SetQueue(Ptr<Queue<Packet>> q)
{
    NS_LOG_FUNCTION(this << q);
    m_queue = q;
}

Ptr<Queue<Packet>>
OpticalDevice::GetQueue() const
{
    NS_LOG_FUNCTION(this);
    return m_queue;
}

void
OpticalDevice::SetReceiveErrorModel(Ptr<ErrorModel> em)
{
    NS_LOG_FUNCTION(this << em);
    m_receiveErrorModel = em;
}

void
OpticalDevice::Attach(Ptr<OpticalChannel> channel)
{
    m_channel = channel;
    NotifyLinkUp();
}

void
OpticalDevice::Detach()
{
    m_channel = nullptr;
    m_linkUp = false;
    m_linkChangeCallbacks();
}

void
OpticalDevice::Receive(Ptr<Packet> packet)
{
    NS_LOG_FUNCTION(this << packet);

    if (!IsLinkUp())
    {
        NS_LOG_INFO("Link Down drop." << packet);
        m_macRxDropTrace(packet);
        return;
    }
    Ptr<Packet> copy = packet->Copy();
    // receive end here
    m_phyRxEndTrace(packet);

    if (m_receiveErrorModel && m_receiveErrorModel->IsCorrupt(copy))
    {
        // If we have an error model and it indicates that it is time to lose a
        // corrupted packet, don't forward this packet up, let it go.
        NS_LOG_INFO("Error Model drop. " << packet);
        m_phyRxDropTrace(packet);
        return;
    }

    EthernetTrailer trailer;
    copy->RemoveTrailer(trailer);
    if (Node::ChecksumEnabled())
    {
        trailer.EnableFcs(true);
    }

    bool crcGood = trailer.CheckFcs(copy);
    if (!crcGood)
    {
        NS_LOG_INFO("CRC error on Packet " << packet);
        m_phyRxDropTrace(packet);
        return;
    }

    EthernetHeader header(false);
    copy->RemoveHeader(header);
    //
    // If the length/type is less than 1500, it corresponds to a length
    // interpretation packet.  In this case, it is an 802.3 packet and
    // will also have an 802.2 LLC header.  If greater than 1500, we
    // find the protocol number (Ethernet type) directly.
    //
    uint16_t protocol;
    if (header.GetLengthType() <= 1500)
    {
        NS_ASSERT(copy->GetSize() >= header.GetLengthType());
        uint32_t padlen = copy->GetSize() - header.GetLengthType();
        NS_ASSERT(padlen <= 46);
        if (padlen > 0)
        {
            copy->RemoveAtEnd(padlen);
        }

        LlcSnapHeader llc;
        copy->RemoveHeader(llc);
        protocol = llc.GetType();
    }
    else
    {
        protocol = header.GetLengthType();
    }

    Mac48Address to = header.GetDestination();
    Mac48Address from = header.GetSource();

    NetDevice::PacketType packetType;
    if (to == m_address)
    {
        packetType = NetDevice::PACKET_HOST;
    }
    else if (to.IsBroadcast())
    {
        packetType = NetDevice::PACKET_BROADCAST;
    }
    else if (to.IsGroup())
    {
        packetType = NetDevice::PACKET_MULTICAST;
    }
    else
    {
        packetType = NetDevice::PACKET_OTHERHOST;
    }

    // Trace sinks will expect complete packets, not packets without some of the
    // headers.

    m_promiscSnifferTrace(packet);
    if (!m_promiscCallback.IsNull())
    {
        m_macPromiscRxTrace(packet);
        m_promiscCallback(this, copy, protocol, from, to, packetType);
    }

    if (packetType != NetDevice::PACKET_OTHERHOST)
    {
        m_macRxTrace(packet);
        m_snifferTrace(packet);
        m_rxCallback(this, copy, protocol, from);
    }
}

Ptr<MobilityModel>
OpticalDevice::GetMobility() const
{
    return m_mobility;
}

bool
OpticalDevice::TransmitStart(Ptr<Packet> p)
{
    NS_LOG_FUNCTION(this << p);
    NS_LOG_LOGIC("UID is " << p->GetUid() << ")");

    //
    // This function is called to start the process of transmitting a packet.
    // We need to tell the channel that we've started wiggling the wire and
    // schedule an event that will be executed when the transmission is complete.
    //
    NS_ASSERT_MSG(m_txMachineState == READY, "Must be READY to transmit");
    m_txMachineState = BUSY;
    m_currentPkt = p;
    m_phyTxBeginTrace(m_currentPkt);

    Time txTime = m_bps.CalculateBytesTxTime(p->GetSize());
    Time txCompleteTime = txTime;

    NS_LOG_LOGIC("Schedule TransmitCompleteEvent in " << txCompleteTime.As(Time::S));
    Simulator::Schedule(txCompleteTime, &OpticalDevice::TransmitComplete, this);

    // 再确认一遍channel是否为空
    if (!m_channel)
    {
        m_phyTxDropTrace(p);
        return false;
    }

    bool result = m_channel->Send(this, p, txTime);
    if (!result)
    {
        m_phyTxDropTrace(p);
    }
    return result;
}

void
OpticalDevice::TransmitComplete()
{
    NS_LOG_FUNCTION(this);

    //
    // This function is called to when we're all done transmitting a packet.
    // We try and pull another packet off of the transmit queue.  If the queue
    // is empty, we are done, otherwise we need to start transmitting the
    // next packet.
    //
    NS_ASSERT_MSG(m_txMachineState == BUSY, "Must be BUSY if transmitting");
    m_txMachineState = READY;

    NS_ASSERT_MSG(m_currentPkt, "OpticalDevice::TransmitComplete(): m_currentPkt zero");

    m_phyTxEndTrace(m_currentPkt);
    m_currentPkt = nullptr;

    Ptr<Packet> p = m_queue->Dequeue();
    if (!p)
    {
        NS_LOG_LOGIC("No pending packets in device queue after tx complete");
        return;
    }

    // Got another packet off of the queue, so start the transmit process again.
    m_snifferTrace(p);
    m_promiscSnifferTrace(p);
    TransmitStart(p);
}
} // namespace ns3