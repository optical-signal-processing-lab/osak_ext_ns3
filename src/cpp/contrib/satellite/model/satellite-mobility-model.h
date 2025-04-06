#ifndef STATELLITE_MOBILITY_MODEL_H
#define STATELLITE_MOBILITY_MODEL_H

#include "ns3/mobility-model.h"
#include "ns3/node.h"
#include "ns3/nstime.h"

namespace ns3
{

// 轨道六根数
struct OrbitalElement
{
    double a;    //!< semimajor axis(km) 长半轴
    double e;    //!< eccentricity 离心率
    double f;    //!< true anomaly(deg) 真近点角
    double i;    //!< inclination angle(deg) 轨道倾角
    double w;    //!< argument of the perigee(deg) 近地点幅角
    double RAAN; // !< the right ascension of ascending node(deg) 右旋升交点赤经
};

/**
 * 开普勒轨道，无扰动
 */
class SatelliteMobilityModel : public MobilityModel
{
  public:
    /**
     * Register this type with the TypeId system.
     * \return the object TypeId
     */
    static TypeId GetTypeId();

    SatelliteMobilityModel(){};
    ~SatelliteMobilityModel(){};

    static double Earth_radius; //!< Km, 地球半径

    /**
     * @brief 设置轨道六根数
     * @param a 轨道半长轴(Km)
     * @param e 轨道离心率
     * @param f 真近点角(Deg)
     * @param i 轨道倾角(Deg)
     * @param w 近地点幅角(Deg)
     * @param RAAN 右旋升交点赤经(Deg)
     */
    void SetOrbitalElements(double a, double e, double f, double i, double w, double RAAN);

    /**
     * @brief 设置轨道六根数
     * @param element 六根数结构体
     * @note 长度单位为`Km`，角度单位为`Deg`
     */
    void SetOrbitalElements(OrbitalElement element);

    /**
     * @brief 设置历元时间，可用于获取该时间下的速度位置
     * @param time 时间
     */
    void SetTimeEpoch(Time time);

    /**
     * @return 获得当前时刻真近点角
     */
    double GetTrueAnomaly() const;

    /**
     * @return 获取卫星运行周期
     */
    Time GetPeriod() const;

  private:
    /* orbit params  */
    mutable OrbitalElement m_element = {0}; //!< 轨道六根数
    const double miu = 3.986 * 1e5;         //!< km^3/sec^2 中心天体引力常数（地球）
    double period = 0;                      //!< (sec) 运行周期
    double E0 = 0;                          //!< 初始偏近点角
    double M0 = 0;                          //!< 初始平近点角
    double m_p = 0;                         //!< semi latus rectum 半通径

    /* calculation params  */
    double ecc = 0;       //离心率别名
    const double prec = 1e-7;   //计算开普勒方程时的精度

    /**
     * @brief 使用设置真近点角的方式更新位置
     * @param f 目标真近点角
     */
    void SetPosition(double f);

    /* overrides */
    Vector DoGetPosition() const override;
    Vector DoGetPositionWithReference(const Vector& referencePosition) const override;
    void DoSetPosition(const Vector& position) override;
    Vector DoGetVelocity() const override;
};

} // namespace ns3

#endif