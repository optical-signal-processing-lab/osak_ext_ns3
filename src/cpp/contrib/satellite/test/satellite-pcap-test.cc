#include "spdlog/spdlog.h"

#include "ns3/error-model.h"
#include "ns3/internet-module.h"
#include "ns3/on-off-helper.h"
#include "ns3/packet-sink-helper.h"
#include "ns3/queue.h"
#include "ns3/optical-channel.h"
#include "ns3/optical-device.h"
#include "ns3/satellite-mobility-model.h"
#include "ns3/satellite-pcap-helper.h"

using namespace std;
using namespace ns3;

class SatellitePcapHelperTest
{
  public:
    SatellitePcapHelperTest()
    {
        lct_factory.SetTypeId("ns3::OpticalDevice");
        queue_factory.SetTypeId("ns3::DropTailQueue<Packet>");
        channel_factory.SetTypeId("ns3::OpticalChannel");

        rem = CreateObject<RateErrorModel>();
        Ptr<UniformRandomVariable> uv = CreateObject<UniformRandomVariable>();
        rem->SetRandomVariable(uv);
        rem->SetRate(0);
    }

    Ptr<OpticalDevice> InstallDev(Ptr<Node> node, OpticalDevice::Direction direction)
    {
        Ptr<OpticalDevice> dev = lct_factory.Create<OpticalDevice>();
        Ptr<Queue<Packet>> queue = queue_factory.Create<Queue<Packet>>();
        dev->SetDirection(direction);
        dev->SetQueue(queue);
        dev->SetAddress(Mac48Address::Allocate());
        dev->SetDataRate(DataRate("100Mbps"));
        dev->SetReceiveErrorModel(rem);
        node->AddDevice(dev);
        return dev;
    }

    void Connect(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b)
    {
        Ptr<OpticalChannel> c = channel_factory.Create<OpticalChannel>();
        a->Attach(c);
        b->Attach(c);
        c->Attach(a);
        c->Attach(b);
    }

    void InstallStack()
    {
        InternetStackHelper stack;
        stack.Install(nodes);
        NetDeviceContainer n01 = NetDeviceContainer(devices.Get(0), devices.Get(1));
        NetDeviceContainer n02 = NetDeviceContainer(devices.Get(2), devices.Get(3));

        Ipv4AddressHelper helper;
        helper.SetBase("192.168.1.0", "255.255.255.0");
        Ipv4InterfaceContainer i01 = helper.Assign(n01);
        helper.SetBase("192.168.2.0", "255.255.255.0");
        Ipv4InterfaceContainer i02 = helper.Assign(n02);
        Ipv4GlobalRoutingHelper::PopulateRoutingTables();

        uint16_t port = 9;
        // from node 1/2 to node 0
        OnOffHelper onoff("ns3::UdpSocketFactory",
                          Address(InetSocketAddress(i01.GetAddress(0), port)));
        onoff.SetConstantRate(DataRate("300kb/s"));

        ApplicationContainer apps = onoff.Install(nodes.Get(1));
        apps.Start(Seconds(1.1));
        apps.Stop(Seconds(10.0));
        ApplicationContainer apps2 = onoff.Install(nodes.Get(2));
        apps.Start(Seconds(1.1));
        apps.Stop(Seconds(10.0));

        // 接收
        PacketSinkHelper sink("ns3::UdpSocketFactory",
                              Address(InetSocketAddress(Ipv4Address::GetAny(), port)));
        apps = sink.Install(nodes.Get(0));
        apps.Start(Seconds(1.0));
        apps.Stop(Seconds(10.0));
    }

    void Init()
    {
        nodes.Create(3);
        OrbitalElement sat = {780 + 6378.14, 0.0, 0.0, 86.4, 0.0, 0.0};
        Ptr<SatelliteMobilityModel> sat1Mobility = CreateObject<SatelliteMobilityModel>();
        sat1Mobility->SetOrbitalElements(sat);
        nodes.Get(0)->AggregateObject(sat1Mobility); // 基本

        sat.RAAN = 20;
        Ptr<SatelliteMobilityModel> sat2Mobility = CreateObject<SatelliteMobilityModel>();
        sat2Mobility->SetOrbitalElements(sat);
        nodes.Get(1)->AggregateObject(sat2Mobility); // 右

        sat.RAAN = 0;
        sat.f = 10;
        Ptr<SatelliteMobilityModel> sat3Mobility = CreateObject<SatelliteMobilityModel>();
        sat3Mobility->SetOrbitalElements(sat);
        nodes.Get(2)->AggregateObject(sat3Mobility); // 上

        devices.Add(InstallDev(nodes.Get(0), OpticalDevice::RIGHT));
        devices.Add(InstallDev(nodes.Get(1), OpticalDevice::LEFT));
        devices.Add(InstallDev(nodes.Get(0), OpticalDevice::FORWARD));
        devices.Add(InstallDev(nodes.Get(2), OpticalDevice::BACKWARD));

        Connect(DynamicCast<OpticalDevice>(devices.Get(0)),
                DynamicCast<OpticalDevice>(devices.Get(1)));
        Connect(DynamicCast<OpticalDevice>(devices.Get(2)),
                DynamicCast<OpticalDevice>(devices.Get(3)));

        InstallStack();
    }

    void TestEnablePcapNode()
    {
        Init();
        SatellitePcapHelper helper;
        helper.EnablePcap("contrib/satellite/test/N", nodes.Get(0), true, false);
        Simulator::Stop(Seconds(10));
        Simulator::Run();
        Simulator::Destroy();
    }

    void TestEnablePcapAll()
    {
        Init();
        SatellitePcapHelper helper;
        helper.EnablePcapAll("contrib/satellite/test/ALL.pcap", true, true);
        Simulator::Stop(Seconds(10));
        Simulator::Run();
        Simulator::Destroy();
    }

    void Main(bool all)
    {
        Config::SetDefault("ns3::OnOffApplication::PacketSize", UintegerValue(210));
        Config::SetDefault("ns3::OnOffApplication::DataRate", StringValue("300b/s"));
        if (all)
        {
            spdlog::info("TestEnablePcapAll");
            TestEnablePcapAll();
        }
        else
        {
            spdlog::info("TestEnablePcapNode");
            TestEnablePcapNode();
        }
    }

    NodeContainer nodes;
    NetDeviceContainer devices;

    ObjectFactory lct_factory;
    ObjectFactory queue_factory;
    ObjectFactory rem_factory;
    ObjectFactory channel_factory;
    ObjectFactory propagation_loss_factory;
    ObjectFactory propagation_delay_factory;
    Ptr<RateErrorModel> rem;
};

int
main(int argc, char** argv)
{

    bool all = false;
    CommandLine cmd(__FILE__);
    cmd.AddValue("all", "capture all packets", all);
    cmd.Parse(argc, argv);
    SatellitePcapHelperTest test;
    test.Main(all);
    return 0;
}