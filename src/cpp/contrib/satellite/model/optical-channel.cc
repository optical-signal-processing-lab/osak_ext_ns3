#include "optical-channel.h"

#include "ns3/names.h"
#include "ns3/pointer.h"
#include "ns3/propagation-delay-model.h"
#include "ns3/propagation-loss-model.h"
#include "ns3/simulator.h"

static double
GetLatitude(ns3::Vector3D pos)
{
    return abs(pos.z) / pos.GetLength();
}

namespace ns3
{

NS_LOG_COMPONENT_DEFINE("OpticalChannel");

NS_OBJECT_ENSURE_REGISTERED(OpticalChannel);

TypeId
OpticalChannel::GetTypeId()
{
    static TypeId tid = TypeId("ns3::OpticalChannel")
                            .SetParent<Channel>()
                            .SetGroupName("satellite")
                            .AddConstructor<OpticalChannel>();
    return tid;
}

OpticalChannel::OpticalChannel()
{
    NS_LOG_FUNCTION(this);
    auto loss = CreateObject<MatrixPropagationLossModel>();
    loss->SetDefaultLoss(0);
    m_loss = loss;
    m_delay = CreateObject<ConstantSpeedPropagationDelayModel>();
}

OpticalChannel::~OpticalChannel()
{
    NS_LOG_FUNCTION(this);
}

void
OpticalChannel::Attach(Ptr<OpticalDevice> dev)
{
    if (dev_list[0])
    {
        dev_list[1] = dev;
        last_lat[1] = GetLatitude(dev->GetMobility()->GetPosition());
    }
    else
    {
        dev_list[0] = dev;
        last_lat[0] = GetLatitude(dev->GetMobility()->GetPosition());
    }
}

void
OpticalChannel::Detach()
{
    dev_list[0] = nullptr;
    dev_list[1] = nullptr;
    last_lat[0] = 0;
    last_lat[1] = 0;
}

std::size_t
OpticalChannel::GetNDevices() const
{
    // always 2
    return 2;
}

Ptr<NetDevice>
OpticalChannel::GetDevice(std::size_t i) const
{
    return dev_list[i];
}

Time
OpticalChannel::GetDelay()
{
    return m_delay->GetDelay(dev_list[0]->GetMobility(), dev_list[1]->GetMobility());
}

void
OpticalChannel::SetPropagationLossModel(const Ptr<PropagationLossModel> loss)
{
    m_loss = loss;
}

void
OpticalChannel::SetPropagationDelayModel(const Ptr<PropagationDelayModel> delay)
{
    m_delay = delay;
}

void
OpticalChannel::SetLatitudeLimit(double limit)
{
    lat_limit = sin(limit * M_PI / 180);
    threshold = sin((limit - 3) * M_PI / 180); // 留下3°的阈值
}

bool
OpticalChannel::LatitudeCheck()
{
    double l0 = GetLatitude(dev_list[0]->GetMobility()->GetPosition());
    double l1 = GetLatitude(dev_list[1]->GetMobility()->GetPosition());
    if (l0 > lat_limit || l1 > lat_limit)
    {
        if (!m_disconnectCb.IsNull())
            m_disconnectCb(dev_list[0], dev_list[1], this);
        return false;
    }
    if ((l0 > last_lat[0] && l0 > threshold) || (l1 > last_lat[1] && l1 > threshold))
    {
        if (!m_readyBreakCb.IsNull())
            m_readyBreakCb(this, lat_limit - std::max(l0, l1));
    }
    last_lat[0] = l0;
    last_lat[1] = l1;
    return true;
}

bool
OpticalChannel::Send(Ptr<OpticalDevice> sender, Ptr<Packet> packet, Time txTime)
{
    auto receiver = GetAnother(sender);
    // 检查链路
    if (isTemporary && !LatitudeCheck())
    {
        return false;
    }
    uint32_t dstNode = receiver->GetNode()->GetId();
    // 接收
    Simulator::ScheduleWithContext(dstNode,
                                   GetDelay() + txTime, // 时延模型
                                   &OpticalChannel::Receive,
                                   sender,
                                   receiver,
                                   packet);
    return true;
}

void
OpticalChannel::Receive(Ptr<OpticalDevice> sender, Ptr<OpticalDevice> receiver, Ptr<Packet> packet)
{
    // 损耗模型判断（复杂情况用ns3的损耗模型）
    // double txPower = sender->GetTxPowerdBm();
    // double rxPower = m_loss->CalcRxPower(txPower, sender->GetMobility(),
    // receiver->GetMobility());

    // 弗里斯损耗模型
    double tx = sender->GetTxPowerdBm() + sender->GetTxGain();
    double dist = sender->GetMobility()->GetDistanceFrom(receiver->GetMobility());
    double loss = 20 * log10(sender->GetWaveLength()*1e-9 / (4 * M_PI * dist));
    double rxPower = tx + receiver->GetRxGain() + loss;
    
    if (rxPower < receiver->GetRxSensitivitydBm())
    {
        NS_LOG_DEBUG("Drop packet due to the receive power");
        return;
    }
    receiver->Receive(packet);
    return;
}

void
OpticalChannel::SetDisconnetCallback(DisconnectCallback cb)
{
    m_disconnectCb = cb;
}

void
OpticalChannel::SetReadyBreakCallback(ReadyBreakCallback cb)
{
    m_readyBreakCb = cb;
}

} // namespace ns3