#include "ns3/core-module.h"
#include "ns3/extension.h"
#include <ns3/SimpleIni.h>
#include <ns3/satellite-module.h>

#include <cstring>
#include <iostream>
#include <spdlog/fmt/fmt.h>
#include <spdlog/spdlog.h>

using namespace ns3;
using namespace std;

static double
GetLatitude(Ptr<OpticalDevice> a)
{
    auto pos = a->GetMobility()->GetPosition();
    return 180 / M_PI * asin(abs(pos.z) / pos.GetLength());
}

void
linkbreak(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b, Ptr<OpticalChannel> c)
{
    std::string na = Names::FindPath(a);
    std::string nb = Names::FindPath(b);
    fmt::println("\033[0;36m{:>4}s: {}----x----{} [{:.2f}, {:.2f}]\033[0m", Simulator::Now().GetSeconds(), na, nb, GetLatitude(a), GetLatitude(b));
}

void
linkconnect(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b)
{
    std::string na = Names::FindPath(a);
    std::string nb = Names::FindPath(b);
    fmt::println("\033[0;32m{:>4}s: {}---------{} [{:.2f}, {:.2f}]\033[0m", Simulator::Now().GetSeconds(), na, nb, GetLatitude(a), GetLatitude(b));
}

void
linkreadybreak(Ptr<OpticalChannel> c, double t)
{
    std::string na = Names::FindName(c->GetDevice(0));
    std::string nb = Names::FindName(c->GetDevice(1));
    spdlog::info("{}s: {}--ready--{}: {}", Simulator::Now().GetSeconds(), na, nb, t);
}

class ConstellationTest
{
  public:
    OrbitalElement sat;
    WalkerParams params;
    Time t_interval;
    Time t_total;
    double lat_limit;
    double update_interval;

    // 设备相关
    double m_lambda;
    double m_txPower;
    double m_txGain;
    double m_rxGain;
    double m_rxSensitivity;
    string m_data_rate;

    string default_conf = "./contrib/satellite/test/constellation-test.ini";

    ConstellationTest(double interval, double total)
        : t_interval(Seconds(interval)),
          t_total(Seconds(total))
    {
    }

    void main()
    {
        loadConf(default_conf);
        ConstellationHelper helper;
        helper.SetWizardSatellite(sat);
        helper.SetConstellationParams(params);
        helper.SetLatitudeLimit(lat_limit);
        helper.SetLinkCheckInterval(Seconds(update_interval));
        helper.SetDevParams(m_lambda, m_txPower, m_txGain, m_rxGain, m_rxSensitivity);

        helper.SetChannelConnetCallback(MakeCallback(&linkconnect));
        helper.SetChannelDisconnetCallback(MakeCallback(&linkbreak));
        helper.SetChannelReadyBreakCallback(MakeCallback(&linkreadybreak));

        helper.Install();
        helper.InstallDev(m_data_rate);
        Simulator::Stop(t_total);
        Simulator::Run();
        Simulator::Destroy();
    }

    void loadConf(std::string filename)
    {
        CSimpleIniA ini;
        ini.SetUnicode();
        SI_Error rc = ini.LoadFile(filename.c_str());
        if (rc < 0)
        {
            spdlog::error("Can't find file: {}", filename);
            abort();
        }
        params.type =
            std::string(ini.GetValue("Constellation", "Type", "STAR")) == "STAR" ? STAR : DELTA;
        params.T = ini.GetLongValue("Constellation", "T", 66);
        params.P = ini.GetLongValue("Constellation", "P", 6);
        params.F = ini.GetLongValue("Constellation", "F", 1);

        sat.a = ini.GetDoubleValue("Sat", "axis", 7158.14); // altitude = 780
        sat.e = ini.GetDoubleValue("Sat", "eccentricity", 0);
        sat.i = ini.GetDoubleValue("Sat", "inclination", 86.4);
        sat.f = ini.GetDoubleValue("Sat", "true_anormly", 0);
        sat.w = ini.GetDoubleValue("Sat", "argument_of_Perigee", 0);
        sat.RAAN = ini.GetDoubleValue("Sat", "RAAN", 0);

        lat_limit = ini.GetDoubleValue("Other", "lat limit", 60);
        update_interval = ini.GetDoubleValue("Other", "update interval", 1);

        m_lambda = ini.GetDoubleValue("Dev", "lambda", 1550);
        m_txPower = ini.GetDoubleValue("Dev", "tx_power", 20);
        m_txGain = ini.GetDoubleValue("Dev", "tx_gain", 120);
        m_rxGain = ini.GetDoubleValue("Dev", "rx_gain", 120);
        m_rxSensitivity = ini.GetDoubleValue("Dev", "rx", -40);
        m_data_rate = ini.GetValue("Dev", "data_rate", "1Gbps");
    }
};

int
main(int argc, char** argv)
{
    double t_interval = 60;
    double t_total = 360;

    CommandLine cmd(__FILE__);
    cmd.AddValue("interval", "time interval", t_interval);
    cmd.AddValue("time", "total simulate time", t_total);
    cmd.Parse(argc, argv);

    ConstellationTest test(t_interval, t_total);
    try
    {
        test.main();
    }
    catch (const std::exception& e)
    {
        spdlog::error("{}", e.what());
    }
    return 0;
}