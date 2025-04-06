#include "satellite-pcap-helper.h"

#include "ns3/queue.h"
#include "ns3/optical-device.h"

#include <spdlog/spdlog.h>

namespace ns3
{

NS_LOG_COMPONENT_DEFINE("SatellitePcapHelper");

void
SatellitePcapHelper::EnablePcapInternal(std::string prefix,
                                        Ptr<NetDevice> nd,
                                        bool promiscuous,
                                        bool explicitFilename)
{
    // All of the Pcap enable functions vector through here including the ones
    // that are wandering through all of devices on perhaps all of the nodes in
    // the system.  We can only deal with devices of type PointToPointNetDevice.
    PcapHelper helper;
    Ptr<OpticalDevice> device = nd->GetObject<OpticalDevice>();
    if (!device)
    {
        NS_LOG_INFO("Device " << device << " not of type ns3::OpticalDevice");
        return;
    }
    if (explicitFilename)
    {
        // explicitfilename 用于收集所有设备的数据包到单个pcap文件中
        if (global_file == nullptr)
        {
            global_file = helper.CreateFile(prefix, std::ios::out, PcapHelper::DLT_EN10MB);
        }
        helper.HookDefaultSink<OpticalDevice>(device, "PromiscSniffer", global_file);
        return;
    }
    std::string filename = helper.GetFilenameFromDevice(prefix, device, true);
    Ptr<PcapFileWrapper> file = helper.CreateFile(filename, std::ios::out, PcapHelper::DLT_EN10MB);
    helper.HookDefaultSink<OpticalDevice>(device, "PromiscSniffer", file);
}

void
SatellitePcapHelper::EnableAsciiInternal(Ptr<OutputStreamWrapper> stream,
                                         std::string prefix,
                                         Ptr<NetDevice> nd,
                                         bool explicitFilename)
{
    //
    // All of the ascii enable functions vector through here including the ones
    // that are wandering through all of devices on perhaps all of the nodes in
    // the system.  We can only deal with devices of type PointToPointNetDevice.
    //
    Packet::EnablePrinting();
    Ptr<OpticalDevice> device = nd->GetObject<OpticalDevice>();
    if (!device)
    {
        NS_LOG_INFO("Device " << device << " not of type ns3::OpticalDevice");
        return;
    }
    if (!stream)
    {
        AsciiTraceHelper helper;

        std::string filename;
        if (explicitFilename)
        {
            filename = prefix;
        }
        else
        {
            filename = helper.GetFilenameFromDevice(prefix, device);
        }

        Ptr<OutputStreamWrapper> theStream = helper.CreateFileStream(filename);

        helper.HookDefaultReceiveSinkWithoutContext<OpticalDevice>(device, "MacRx", theStream);
        Ptr<Queue<Packet>> queue = device->GetQueue();
        helper.HookDefaultEnqueueSinkWithoutContext<Queue<Packet>>(queue, "Enqueue", theStream);
        helper.HookDefaultDropSinkWithoutContext<Queue<Packet>>(queue, "Drop", theStream);
        helper.HookDefaultDequeueSinkWithoutContext<Queue<Packet>>(queue, "Dequeue", theStream);
        // PhyRxDrop trace source for "d" event
        helper.HookDefaultDropSinkWithoutContext<OpticalDevice>(device, "PhyRxDrop", theStream);

        return;
    }
    std::string devName = Names::FindPath(nd);

    Config::Connect(fmt::format("{}/MacRx", devName),
                    MakeBoundCallback(&AsciiTraceHelper::DefaultReceiveSinkWithContext, stream));

    Config::Connect(fmt::format("{}/TxQueue/Enqueue", devName),
                    MakeBoundCallback(&AsciiTraceHelper::DefaultEnqueueSinkWithContext, stream));

    Config::Connect(fmt::format("{}/TxQueue/Dequeue", devName),
                    MakeBoundCallback(&AsciiTraceHelper::DefaultDequeueSinkWithContext, stream));

    Config::Connect(fmt::format("{}/TxQueue/Drop", devName),
                    MakeBoundCallback(&AsciiTraceHelper::DefaultDropSinkWithContext, stream));

    Config::Connect(fmt::format("{}/PhyRxDrop", devName),
                    MakeBoundCallback(&AsciiTraceHelper::DefaultDropSinkWithContext, stream));
}

} // namespace ns3