#include "constellation-helper.h"

#include "ns3/error-model.h"
#include "ns3/queue.h"
#include "ns3/optical-channel.h"
#include "ns3/optical-device.h"

#include <spdlog/spdlog.h>

static double
GetLatitude(ns3::Vector3D pos)
{
    return 180 / M_PI * asin(abs(pos.z) / pos.GetLength());
}

namespace ns3
{
NS_LOG_COMPONENT_DEFINE("ConstellationHelper");

ConstellationHelper::ConstellationHelper()
{
    m_mobility.SetTypeId("ns3::SatelliteMobilityModel");
    lct_factory.SetTypeId("ns3::OpticalDevice");
    queue_factory.SetTypeId("ns3::DropTailQueue<Packet>");
    rem_factory.SetTypeId("ns3::RateErrorModel");
    channel_factory.SetTypeId("ns3::OpticalChannel");
}

void
ConstellationHelper::SetConstellationParams(WalkerParams params)
{
    if (params.type == STAR)
        RAAN_span = 180.0;
    if (params.type == DELTA)
        RAAN_span = 360.0;
    m_params.P = params.P;
    m_params.F = params.F;
    m_params.T = params.T;
    satsInPlane = m_params.T / m_params.P;
    if (m_params.T % m_params.P != 0)
    {
        throw std::runtime_error("T%P!=0, 轨道卫星数量不为整数！");
    }
}

int
ConstellationHelper::GetSatsInPlane() const
{
    return satsInPlane;
}

int
ConstellationHelper::GetNumOfPlane() const
{
    return m_params.P;
}

NodeContainer&
ConstellationHelper::Install()
{
    nodes.Create(m_params.T);
#define T (m_params.T)
#define P (m_params.P)
#define F (m_params.F)
#define N (satsInPlane)

    m_allocateElement = m_wizardSateElement;
    // 确定相位差
    double delta_theta = 360.0 / N;
    double delta_omega = 0;
    delta_omega = 360.0 * F / T;

    spdlog::info("RAAN_span = {}, delta_omega = {}", RAAN_span, delta_omega);
    double minf = 720;
    for (int i = 0; i < P; i++)
    {
        for (int j = 0; j < N; j++)
        {
            auto node = nodes.Get(i * N + j);
            // 分配名字
            Names::Add(fmt::format("S{:02}{:02}", i, j), node);

            // 生成六根数
            m_allocateElement.f = m_wizardSateElement.f + delta_omega * i + delta_theta * j;
            m_allocateElement.RAAN = m_wizardSateElement.RAAN + RAAN_span / P * i;

            if (m_params.type == DELTA)
            {
                // 寻找delta构型中，最后一个轨道与第一个轨道的最佳连接
                if (i == P - 1)
                {
                    double theta = m_allocateElement.f > m_wizardSateElement.f + 180
                                       ? m_allocateElement.f - 360
                                       : m_allocateElement.f;
                    double diff = std::fmod(std::abs(m_wizardSateElement.f - theta), 360.0);
                    if (diff < minf)
                    {
                        minf = diff;
                        match = j;
                    }
                }
            }
            Install(node);
        }
    }
    spdlog::info("best match in {}, minf = {}", match, minf);
#undef T
#undef P
#undef F
#undef N
    return nodes;
}

void
ConstellationHelper::SetDevParams(double lambda,
                                  double txPower,
                                  double txGain,
                                  double rxGain,
                                  double rxSensitivity)
{
    m_lambda = lambda;
    m_txPower = txPower;
    m_txGain = txGain;
    m_rxGain = rxGain;
    m_rxSensitivity = rxSensitivity;
}

void
ConstellationHelper::SetWizardSatellite(OrbitalElement elem)
{
    m_wizardSateElement = elem;
}

int
ConstellationHelper::GetBestMatch()
{
    return match;
}

void
ConstellationHelper::Install(Ptr<Node> node)
{
    Ptr<Object> object = node;
    Ptr<SatelliteMobilityModel> model = object->GetObject<SatelliteMobilityModel>();
    if (!model)
    {
        model = m_mobility.Create()->GetObject<SatelliteMobilityModel>();
        if (!model)
        {
            NS_FATAL_ERROR("The requested mobility model is not a mobility model: \""
                           << m_mobility.GetTypeId().GetName() << "\"");
        }
        else
        {
            object->AggregateObject(model);
        }
    }
    model->SetOrbitalElements(m_allocateElement);
}

// =====================
NetDeviceContainer&
ConstellationHelper::InstallDev(std::string dataRate)
{
    // 准备工作
    auto rem = rem_factory.Create<RateErrorModel>();
    Ptr<UniformRandomVariable> uv = CreateObject<UniformRandomVariable>();
    rem->SetRandomVariable(uv);
    rem->SetRate(0);
    char desc[5] = "RLFB";

    for (uint32_t i = 0; i < nodes.GetN(); i++)
    {
        auto node = nodes.Get(i);
        auto name = Names::FindName(node);
        for (int j = 0; j < 4; j++)
        {
            // config optical dev
            Ptr<OpticalDevice> dev = lct_factory.Create<OpticalDevice>();
            Ptr<Queue<Packet>> queue = queue_factory.Create<Queue<Packet>>();
            OpticalDevice::Direction dst = static_cast<OpticalDevice::Direction>(j);
            dev->SetQueue(queue);
            dev->SetAddress(Mac48Address::Allocate());
            dev->SetDataRate(DataRate(dataRate));
            dev->SetReceiveErrorModel(rem);
            
            dev->SetDirection(dst);
            dev->SetWaveLength(m_lambda);
            dev->SetTxPowerdBm(m_txPower);
            dev->SetRxSensitivitydBm(m_rxSensitivity);
            dev->SetTxGain(m_txGain);
            dev->SetRxGain(m_rxGain);

            node->AddDevice(dev);
            devs.Add(dev);
            Names::Add(fmt::format("{}/eth{}", name, desc[j]), dev);
        }
    }
    LinkConfig();
    return devs;
}

void
ConstellationHelper::LinkConfig()
{
    // 构建永久链路
    int N = m_params.T / m_params.P;
    for (int i = 0; i < m_params.P; i++)
    {
        for (int j = 0; j < N; j++)
        {
            Ptr<OpticalChannel> channel = channel_factory.Create<OpticalChannel>();
            channel->SetType(OpticalChannel::FOREVER);
            auto dev1 = Names::Find<OpticalDevice>(fmt::format("S{:02}{:02}/ethF", i, j));
            auto dev2 =
                Names::Find<OpticalDevice>(fmt::format("S{:02}{:02}/ethB", i, (j + 1) % N));
            channel->Attach(dev1);
            channel->Attach(dev2);
            dev1->Attach(channel);
            dev2->Attach(channel);
        }
    }

    // 构建临时链路
    for (int i = 0; i < N; i++)
    {
        for (int j = 0; j < m_params.P - 1; j++)
        {
            Ptr<OpticalChannel> channel = channel_factory.Create<OpticalChannel>();
            channel->SetType(OpticalChannel::TEMPORARY);
            channel->SetLatitudeLimit(lat_limit);
            channel->SetDisconnetCallback(MakeCallback(&ConstellationHelper::DisConnect, this));
            channel->SetReadyBreakCallback(m_channelReadyBreakCb);
            auto dev1 = Names::Find<OpticalDevice>(fmt::format("S{:02}{:02}/ethR", j, i));
            auto dev2 = Names::Find<OpticalDevice>(fmt::format("S{:02}{:02}/ethL", j + 1, i));
            double lat1 = GetLatitude(dev1->GetMobility()->GetPosition());
            double lat2 = GetLatitude(dev2->GetMobility()->GetPosition());
            if (lat1 > lat_limit || lat2 > lat_limit)
            {
                unused_link.emplace_back(channel);
                breaked_left.emplace(dev2);
                breaked_right.emplace(dev1);
                if (lat1 > lat_limit)
                    overPolar.insert(dev1->GetNode()->GetId());
                if (lat2 > lat_limit)
                    overPolar.insert(dev2->GetNode()->GetId());
                continue;
            }
            channel->Attach(dev1);
            channel->Attach(dev2);
            dev1->Attach(channel);
            dev2->Attach(channel);
        }
    }
    Simulator::Schedule(m_linkCheckInterval, &ConstellationHelper::LinkMaintenance, this);
}

void
ConstellationHelper::LinkMaintenance()
{
    uint32_t N = m_params.T / m_params.P;
    // 检查纬度
    for (uint32_t i = 0; i < nodes.GetN(); i++)
    {
        auto node = nodes.Get(i);
        double lat = GetLatitude(node->GetObject<MobilityModel>()->GetPosition());
        // 进入极区
        if (lat > lat_limit && !overPolar.contains(node->GetId()))
        {
            overPolar.insert(node->GetId());
            Ptr<OpticalDevice> devR = DynamicCast<OpticalDevice>(node->GetDevice(0));
            Ptr<OpticalDevice> devL = DynamicCast<OpticalDevice>(node->GetDevice(1));
            auto chR = devR->GetChannel();
            auto chL = devL->GetChannel();
            if (chR)
            {
                auto ch = DynamicCast<OpticalChannel>(chR);
                auto tmpL = ch->GetAnother(devR);
                DisConnect(tmpL, devR, ch);
            }
            if (chL)
            {
                auto ch = DynamicCast<OpticalChannel>(chL);
                auto tmpR = ch->GetAnother(devL);
                DisConnect(tmpR, devL, ch);
            }
        }
        // 离开极区
        if (lat < lat_limit && overPolar.contains(node->GetId()))
        {
            uint32_t id = node->GetId();
            overPolar.erase(id);
            // 在已断连设备中查找符合条件的设备
            uint32_t adj1 = -1;
            uint32_t adj2 = -1;
            uint32_t invalid = -1;
            if (id / N != 0)
            {
                adj1 = id - N;
                if (overPolar.contains(adj1))
                    adj1 = -1;
            }
            if (id / N != (uint32_t)(m_params.P - 1))
            {
                adj2 = id + N;
                if (overPolar.contains(adj2))
                    adj2 = -1;
            }
            if (adj1 != invalid)
            {
                Ptr<OpticalDevice> adj_d1 =
                    DynamicCast<OpticalDevice>(nodes.Get(adj1)->GetDevice(0));
                if (breaked_right.contains(adj_d1))
                {
                    Ptr<OpticalDevice> devL = DynamicCast<OpticalDevice>(node->GetDevice(1));
                    Connect(adj_d1, devL);
                }
                Ptr<OpticalDevice> adj_d2 =
                    DynamicCast<OpticalDevice>(nodes.Get(adj1)->GetDevice(1));
                if (breaked_left.contains(adj_d2))
                {
                    Ptr<OpticalDevice> devR = DynamicCast<OpticalDevice>(node->GetDevice(0));
                    Connect(adj_d2, devR);
                }
            }
            if (adj2 != invalid)
            {
                Ptr<OpticalDevice> adj_d1 =
                    DynamicCast<OpticalDevice>(nodes.Get(adj2)->GetDevice(0));
                if (breaked_right.contains(adj_d1))
                {
                    Ptr<OpticalDevice> devL = DynamicCast<OpticalDevice>(node->GetDevice(1));
                    Connect(adj_d1, devL);
                }
                Ptr<OpticalDevice> adj_d2 =
                    DynamicCast<OpticalDevice>(nodes.Get(adj2)->GetDevice(1));
                if (breaked_left.contains(adj_d2))
                {
                    Ptr<OpticalDevice> devR = DynamicCast<OpticalDevice>(node->GetDevice(0));
                    Connect(adj_d2, devR);
                }
            }
        }
    }
    Simulator::Schedule(m_linkCheckInterval, &ConstellationHelper::LinkMaintenance, this);
}

void
ConstellationHelper::Connect(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b)
{
    Ptr<OpticalChannel> c = unused_link.back();
    unused_link.pop_back();
    a->Attach(c);
    b->Attach(c);
    c->Attach(a);
    c->Attach(b);
    if (a->GetDirection() == OpticalDevice::RIGHT)
    {
        breaked_right.erase(a);
        breaked_left.erase(b);
    }
    else
    {
        breaked_right.erase(b);
        breaked_left.erase(a);
    }
    m_channelConnectCb(a, b);
}

void
ConstellationHelper::DisConnect(Ptr<OpticalDevice> a,
                                Ptr<OpticalDevice> b,
                                Ptr<OpticalChannel> c)
{
    a->Detach();
    b->Detach();
    if (a->GetDirection() == OpticalDevice::RIGHT)
    {
        breaked_right.insert(a);
        breaked_left.insert(b);
    }
    else
    {
        breaked_right.insert(b);
        breaked_left.insert(a);
    }
    c->Detach();
    unused_link.emplace_back(c);
    m_channelDisconnetCb(a, b, c);
}

} // namespace ns3