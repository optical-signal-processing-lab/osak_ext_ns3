#ifndef OPTICAL_CHANNEL_H
#define OPTICAL_CHANNEL_H

#include "optical-device.h"

#include "ns3/callback.h"
#include "ns3/channel.h"
#include "ns3/packet.h"
#include "ns3/traced-callback.h"

namespace ns3
{
class PropagationLossModel;
class PropagationDelayModel;

class OpticalChannel : public Channel
{
  public:
    enum ChannelType
    {
        FOREVER,
        TEMPORARY
    };

    /**
     * \brief Get the type ID.
     * \return the object TypeId
     */
    static TypeId GetTypeId();

    OpticalChannel();
    ~OpticalChannel() override;

    void SetType(ChannelType type)
    {
        isTemporary = (type == TEMPORARY);
    }

    /**
     * @brief 向信道中添加激光终端
     * @param lct 目标激光终端
     * @return 恒为真
     */
    void Attach(Ptr<OpticalDevice> lct);
    void Detach();
    /**
     * @brief 向信道中发送数据
     * @param sender 发送者(通过this指针传递)
     * @param packet 数据包
     * @param txTime 传输时延
     * @return 恒为真
     */
    bool Send(Ptr<OpticalDevice> sender, Ptr<Packet> packet, Time txTime);

    typedef Callback<void, Ptr<OpticalDevice>, Ptr<OpticalDevice>, Ptr<OpticalChannel>>
        DisconnectCallback;
    typedef Callback<void, Ptr<OpticalChannel>, double> ReadyBreakCallback;

    // 设置回调，用于Debug
    void SetDisconnetCallback(DisconnectCallback cb);
    void SetReadyBreakCallback(ReadyBreakCallback cb);

    /**
     * @brief 获得信道中设备的数量
     * @return 根据连接情况
     */
    std::size_t GetNDevices() const override;
    /**
     * @brief 获得指定位置的设备
     * @param i 设备序号
     * @return 设备指针
     */
    Ptr<NetDevice> GetDevice(std::size_t i) const override;

    Ptr<OpticalDevice> GetAnother(Ptr<OpticalDevice> self)
    {
        return self == dev_list[0] ? dev_list[1] : dev_list[0];
    }

    Time GetDelay();

    /**
     * @param loss 新的传播损耗模型
     */
    void SetPropagationLossModel(const Ptr<PropagationLossModel> loss);
    /**
     * \param delay 新的传播时延模型
     */
    void SetPropagationDelayModel(const Ptr<PropagationDelayModel> delay);
    /**
     * \param limit 极区纬度
     */
    void SetLatitudeLimit(double limit);
    /**
     * @brief 接收数据包
     * @param sender 发送设备
     * @param receiver 接收设备
     * @param packet 数据包
     * @note 通常通过Schedule()调用
     */
    static void Receive(Ptr<OpticalDevice> sender,
                        Ptr<OpticalDevice> receiver,
                        Ptr<Packet> packet);

  private:
    bool LatitudeCheck();
    Ptr<PropagationLossModel> m_loss;   //!< 传播损耗模型
    Ptr<PropagationDelayModel> m_delay; //!< 传播时延模型
    double lat_limit;            // 纬度限制

    DisconnectCallback m_disconnectCb;
    ReadyBreakCallback m_readyBreakCb;
    std::array<Ptr<OpticalDevice>, 2> dev_list;
    std::array<double,2> last_lat{-1};
    bool isTemporary = false;
    double threshold = 3;
};
} // namespace ns3

#endif