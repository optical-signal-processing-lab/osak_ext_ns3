#include "satellite-mobility-model.h"
#include "ns3/extension.h"
#include "ns3/simulator.h"

#include <Eigen/Geometry>
#include <cmath>

namespace ns3
{
#define RAD(x) x* M_PI / 180

NS_OBJECT_ENSURE_REGISTERED(SatelliteMobilityModel);
NS_LOG_COMPONENT_DEFINE("SatelliteMobilityModel");

double SatelliteMobilityModel::Earth_radius = 6378.14;

TypeId
SatelliteMobilityModel::GetTypeId()
{
    static TypeId tid = TypeId("ns3::SatelliteMobilityModel")
                            .SetParent<MobilityModel>()
                            .AddConstructor<SatelliteMobilityModel>()
                            .SetGroupName("Mobility");
    return tid;
}

void
SatelliteMobilityModel::SetOrbitalElements(double a,
                                           double e,
                                           double f,
                                           double i,
                                           double w,
                                           double RAAN)
{
    m_element.a = a;
    m_element.e = e;
    m_element.f = RAD(f);
    m_element.i = RAD(i);
    m_element.w = RAD(w);
    m_element.RAAN = RAD(RAAN);
    m_p = a * (1 - e * e);

    period = 2 * M_PI * std::sqrt(std::pow(a, 3) / miu);
    E0 = 2 * std::atan(std::sqrt((1 - e) / (1 + e)) * std::tan(m_element.f / 2));
    M0 = E0 - e * std::sin(E0); 
    ecc = e;
}

void
SatelliteMobilityModel::SetOrbitalElements(OrbitalElement element)
{
    SetOrbitalElements(element.a, element.e, element.f, element.i, element.w, element.RAAN);
}

void
SatelliteMobilityModel::SetPosition(double f)
{
    m_element.f = f;
    NotifyCourseChange();
}

double
SatelliteMobilityModel::GetTrueAnomaly() const
{
    return fmod(m_element.f * 180 / M_PI, 360);
}

Vector
SatelliteMobilityModel::DoGetPosition() const
{
    // 间接调用非const函数
    SatelliteMobilityModel* obj = (SatelliteMobilityModel*)this;
    obj->SetTimeEpoch(Simulator::Now());

    double r = m_p / (1 + m_element.e * std::cos(m_element.f)); // 距离矢量

    /* 轨道平面坐标 */
    double x = r * std::cos(m_element.f);
    double y = r * std::sin(m_element.f);
    double z = 0;

    /* 欧拉角序列3-1-3，用于将轨道平面坐标系转到地球惯性坐标系（Earth Inertial Axes）
     *  m = C3(RAAN)C1(i)C3(w)
     */
    Eigen::Matrix3d m;
    m = Eigen::AngleAxisd(m_element.RAAN, Eigen::Vector3d::UnitZ()) *
        Eigen::AngleAxisd(m_element.i, Eigen::Vector3d::UnitX()) *
        Eigen::AngleAxisd(m_element.w, Eigen::Vector3d::UnitZ());

    /* 坐标系转换 */
    Eigen::Vector3d res = m * Eigen::Vector3d(x, y, z) * 1e3;

    return {res[0], res[1], res[2]};
}

Vector
SatelliteMobilityModel::DoGetVelocity() const
{
    // 间接调用非const函数
    SatelliteMobilityModel* obj = (SatelliteMobilityModel*)this;
    obj->SetTimeEpoch(Simulator::Now());
    // velocity vector coordinates
    double Vx_ = -1 * std::sqrt(miu / m_p) * std::sin(m_element.f);
    double Vy_ = std::sqrt(miu / m_p) * (m_element.e + std::cos(m_element.f));
    double Vz_ = 0;

    // Euler angles sequence 3-1-3
    Eigen::Matrix3d m;
    m = Eigen::AngleAxisd(m_element.RAAN, Eigen::Vector3d::UnitZ()) *
        Eigen::AngleAxisd(m_element.i, Eigen::Vector3d::UnitX()) *
        Eigen::AngleAxisd(m_element.w, Eigen::Vector3d::UnitZ());

    // axes transform
    Eigen::Vector3d res = m * Eigen::Vector3d(Vx_, Vy_, Vz_) * 1e3;
    return {res[0], res[1], res[2]};
}

Vector
SatelliteMobilityModel::DoGetPositionWithReference(const Vector& referencePosition) const
{
    NS_LOG_UNCOND("GetPositionWithReference: Ensure your referencePosition unit is (Km, Km, Km)");
    Vector distance = DoGetPosition() - referencePosition;
    return distance;
}

void
SatelliteMobilityModel::DoSetPosition(const Vector& position)
{
    NS_LOG_UNCOND(
        "Warning: Method `SetPosition(const Vector& position)` is not support in this class");
    return;
}

void
SatelliteMobilityModel::SetTimeEpoch(Time time)
{
    double t = time.GetSeconds();
    t = std::fmod(t, period);
    // t时刻后的平近点角
    double M = 2 * M_PI / period * t + M0;
    // 圆
    if(ecc == 0){
        m_element.f = M;
        return;
    }
    // 椭圆
    // t时刻后的偏近点角
    double E = 0;
    if (M < M_PI)
        E = M + ecc / 2;
    else
        E = M - ecc / 2;

    double fE, dfE;
    double ratio = 1.0;
    while (true)
    {
        fE = E - ecc * sin(E) - M;
        dfE = 1 - ecc * cos(E);
        ratio = fE / dfE;
        if (abs(ratio) > prec)
            E = E - ratio;
        else
            break;
    }
    // 计算真近点角
    double f = 2 * atan(sqrt((1 + ecc) / (1 - ecc)) * tan(E / 2));
    m_element.f = fmod(f, 2 * M_PI);
}

Time
SatelliteMobilityModel::GetPeriod() const
{
    return Seconds(period);
}

#undef RAD
} // namespace ns3
