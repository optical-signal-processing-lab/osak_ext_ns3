#ifndef SATELLITE_PCAP_HELPER_H
#define SATELLITE_PCAP_HELPER_H

#include "ns3/core-module.h"
#include "ns3/trace-helper.h"

namespace ns3
{

class SatellitePcapHelper : public PcapHelperForDevice, public AsciiTraceHelperForDevice
{
  public:
    SatellitePcapHelper() = default;
    ~SatellitePcapHelper() = default;

    // 覆盖父类实现
    void EnablePcapAll(std::string prefix, bool promiscuous, bool explicitFilename)
    {
        EnablePcap(prefix, NodeContainer::GetGlobal(), promiscuous, explicitFilename);
    }

    void EnablePcap(std::string prefix, NodeContainer n, bool promiscuous, bool explicitFilename)
    {
        NetDeviceContainer devs;
        for (auto i = n.Begin(); i != n.End(); ++i)
        {
            Ptr<Node> node = *i;
            for (uint32_t j = 0; j < node->GetNDevices(); ++j)
            {
                PcapHelperForDevice::EnablePcap(prefix, node->GetDevice(j), promiscuous, explicitFilename);
            }
        }
    }

  private:
    /**
     * \brief Enable pcap output the indicated net device.
     *
     * NetDevice-specific implementation mechanism for hooking the trace and
     * writing to the trace file.
     *
     * \param prefix Filename prefix to use for pcap files.
     * \param nd Net device for which you want to enable tracing.
     * \param promiscuous If true capture all possible packets available at the device.
     * \param explicitFilename Treat the prefix as an explicit filename if true
     */
    void EnablePcapInternal(std::string prefix,
                            Ptr<NetDevice> nd,
                            bool promiscuous,
                            bool explicitFilename) override;

    /**
     * \brief Enable ascii trace output on the indicated net device.
     *
     * NetDevice-specific implementation mechanism for hooking the trace and
     * writing to the trace file.
     *
     * \param stream The output stream object to use when logging ascii traces.
     * \param prefix Filename prefix to use for ascii trace files.
     * \param nd Net device for which you want to enable tracing.
     * \param explicitFilename Treat the prefix as an explicit filename if true
     */
    void EnableAsciiInternal(Ptr<OutputStreamWrapper> stream,
                             std::string prefix,
                             Ptr<NetDevice> nd,
                             bool explicitFilename) override;

  private:
    Ptr<PcapFileWrapper> global_file;
};

} // namespace ns3

#endif