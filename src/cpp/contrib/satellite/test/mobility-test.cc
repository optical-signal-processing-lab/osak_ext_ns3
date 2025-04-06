#include "ns3/core-module.h"
#include "ns3/extension.h"
#include "ns3/satellite-mobility-model.h"
#include <ns3/SimpleIni.h>

#include <cstring>
#include <iostream>
#include <spdlog/fmt/fmt.h>
#include <spdlog/spdlog.h>

using namespace ns3;
using namespace std;

OrbitalElement
loadConf(std::string filename)
{
    CSimpleIniA ini;
    ini.SetUnicode();
    SI_Error rc = ini.LoadFile(filename.c_str());
    if (rc < 0)
    {
        spdlog::error("Can't find file: {}", filename);
        abort();
    }
    OrbitalElement res;
    res.a =
        ini.GetDoubleValue("Sat", "axis", 7158.14); // 780 km
    res.e = ini.GetDoubleValue("Sat", "eccentricity", 0);
    res.i = ini.GetDoubleValue("Sat", "inclination", 86);
    res.f = ini.GetDoubleValue("Sat", "true_anormly", 15);
    res.w = ini.GetDoubleValue("Sat", "argument_of_perigee", 20);
    res.RAAN = ini.GetDoubleValue("Sat", "RAAN", 30);
    return res;
}

class MobilityTest
{
  public:
    Ptr<SatelliteMobilityModel> mobility;
    OrbitalElement sat;

    Time t_interval;
    Time t_total;

    string default_conf = "./contrib/satellite/test/mobility-test.ini";

    MobilityTest(double interval, double total)
        : t_interval(Seconds(interval)),
          t_total(Seconds(total))
    {
        mobility = CreateObject<SatelliteMobilityModel>();
        sat = loadConf(default_conf);
    }

    void main()
    {
        Run1();
    }

    void Run1()
    {
        fmt::println("\nOrbital Elements = [{}, {}, {}, {}, {}, {}]:\n",
                     sat.a,
                     sat.e,
                     sat.f,
                     sat.i,
                     sat.RAAN,
                     sat.w);
        fmt::println("{:>11}{:>16}{:>16}{:>16}{:>16}{:>16}{:>16}{:>16}",
                     "Time (s)",
                     "x (km)",
                     "y (km)",
                     "z (km)",
                     "vx (km/s)",
                     "vy (km/s)",
                     "vz (km/s)",
                     "theta (deg)");
        fmt::println("{:-^124}", "-");
        mobility->SetOrbitalElements(sat);
        Simulator::Stop(t_total);
        Simulator::ScheduleNow(&MobilityTest::DoRun, this);
        Simulator::Run();
        Simulator::Destroy();
    }

  private:
    void DoRun()
    {
        Vector init = mobility->GetPosition();
        init /= 1000;
        Vector init_v = mobility->GetVelocity();
        init_v /= 1000;
        double f = mobility->GetTrueAnomaly();

        fmt::print("{:>10.1f}s", Simulator::Now().GetSeconds());
        fmt::print("{:>16.6f}{:>16.6f}{:>16.6f}", init.x, init.y, init.z);
        fmt::print("{:>16.6f}{:>16.6f}{:>16.6f}", init_v.x, init_v.y, init_v.z);
        fmt::println("{:>16.6f}", f);
        Simulator::Schedule(t_interval, &MobilityTest::DoRun, this);
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

    MobilityTest test(t_interval, t_total);
    test.main();
}