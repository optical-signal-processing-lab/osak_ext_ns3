#ifndef CONSTELLATION_HELPER_H
#define CONSTELLATION_HELPER_H

#include <ns3/core-module.h>
#include <ns3/net-device-container.h>
#include <ns3/node-container.h>
#include <ns3/satellite-mobility-model.h>

#include <unordered_set>

namespace ns3
{
class OpticalDevice;
class OpticalChannel;
enum ConstellationType
{
    DELTA,
    STAR
};

// 星座参数
struct WalkerParams
{
    ConstellationType type;
    int T;
    int P;
    int F;
};
/**
 * @brief 用于构建Walker星座
 * @details
 * 1.
 * 使用SetWizardSatellite()设置向导卫星，用于定义星座运行的高度、倾斜角、离心率以及近地点幅角等<p>
 * 2. 使用SetConstellationParams()定义星座轨道数、轨道卫星数以及相位因子
 * 3. 使用SetConstellationType()定义星座构型(STAR/DELTA)
 * 4. 使用CreateNodes()创建星座卫星，并使用Install()安装运动模块
 */
class ConstellationHelper
{
  public:
    ConstellationHelper();
    ~ConstellationHelper() = default;
    /**
     * @brief 获得轨道平面数
     * @return 轨道平面数
     */
    int GetNumOfPlane() const;
    /**
     * @brief 获得轨道上卫星数
     * @return 轨道上卫星数
     */
    int GetSatsInPlane() const;
    /**
     * @brief 为卫星节点安装移动模块
     * @param nodes 由CreateNodes()创建的卫星节点集合
     */
    NodeContainer& Install();
    NetDeviceContainer& InstallDev(std::string dataRate);
    /**
     * @brief 设置星座参数
     * @param param 星座参数
     */
    void SetConstellationParams(WalkerParams params);
    /**
     * @brief 设置设备参数
     * @param lambda 激光波长（nm）
     * @param txPower 发射功率（dBm）
     * @param txGain 发射增益（dB）
     * @param rxGain 接收增益（dB）
     * @param rxSensitivity 接收灵敏度（dBm）
     */
    void SetDevParams(double lambda, double txPower, double txGain, double rxGain, double rxSensitivity);
    /**
     * @brief 设置向导卫星，用于定义星座高度、倾角等参数
     * @param elem 轨道六根数
     */
    void SetWizardSatellite(OrbitalElement elem);

    void SetChannelConnetCallback(Callback<void, Ptr<OpticalDevice>, Ptr<OpticalDevice>> cb)
    {
        m_channelConnectCb = cb;
    }

    void SetChannelDisconnetCallback(
        Callback<void, Ptr<OpticalDevice>, Ptr<OpticalDevice>, Ptr<OpticalChannel>> cb)
    {
        m_channelDisconnetCb = cb;
    }

    void SetChannelReadyBreakCallback(Callback<void, Ptr<OpticalChannel>, double> cb)
    {
        m_channelReadyBreakCb = cb;
    }

    void SetLinkCheckInterval(Time interval)
    {
        m_linkCheckInterval = interval;
    }

    void SetLatitudeLimit(double limit)
    {
        lat_limit = limit;
    }

    int GetBestMatch();

  private:
    void Install(Ptr<Node> node);
    void LinkConfig();
    void LinkMaintenance();
    void Connect(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b);
    void DisConnect(Ptr<OpticalDevice> a, Ptr<OpticalDevice> b, Ptr<OpticalChannel> c);
    ObjectFactory m_mobility;
    struct OrbitalElement m_wizardSateElement = {0};
    struct OrbitalElement m_allocateElement = {0};

    // 铱星构型
    struct WalkerParams m_params = {STAR, 66, 6, 1};
    double lat_limit = 60;
    int satsInPlane = 11;
    double RAAN_span = 180;

    // 距离S0000最近的卫星S(P-1)(match)
    int match = 0;

    // 链路管理相关
    NodeContainer nodes;
    NetDeviceContainer devs;
    Time m_linkCheckInterval = Seconds(1);
    std::vector<Ptr<OpticalChannel>> good_link;
    std::vector<Ptr<OpticalChannel>> unused_link;
    std::unordered_set<Ptr<OpticalDevice>> breaked_left;
    std::unordered_set<Ptr<OpticalDevice>> breaked_right;
    std::unordered_set<uint32_t> overPolar;

    // 设备相关
    double m_lambda = 1550;
    double m_txPower = 20;
    double m_txGain = 120;
    double m_rxGain = 120;
    double m_rxSensitivity = -40;

    // 工厂类
    ObjectFactory lct_factory;
    ObjectFactory queue_factory;
    ObjectFactory rem_factory;
    ObjectFactory channel_factory;
    ObjectFactory propagation_loss_factory;
    ObjectFactory propagation_delay_factory;

    Callback<void, Ptr<OpticalDevice>, Ptr<OpticalDevice>> m_channelConnectCb;
    Callback<void, Ptr<OpticalDevice>, Ptr<OpticalDevice>, Ptr<OpticalChannel>>
        m_channelDisconnetCb;
    Callback<void, Ptr<OpticalChannel>, double> m_channelReadyBreakCb;
};

} // namespace ns3

#endif