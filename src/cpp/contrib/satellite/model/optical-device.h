#ifndef OPTICAL_DEVICE_H
#define OPTICAL_DEVICE_H

#include "ns3/address.h"
#include "ns3/callback.h"
#include "ns3/data-rate.h"
#include "ns3/mac48-address.h"
#include "ns3/mobility-model.h"
#include "ns3/net-device.h"
#include "ns3/node.h"
#include "ns3/nstime.h"
#include "ns3/packet.h"
#include "ns3/ptr.h"
#include "ns3/queue-fwd.h"
#include "ns3/traced-callback.h"

namespace ns3
{

class ErrorModel;
class OpticalChannel;

class OpticalDevice : public NetDevice
{
  public:
    /**
     * \brief Get the type ID.
     * \return the object TypeId
     */
    static TypeId GetTypeId();

    OpticalDevice();
    ~OpticalDevice() override;

    OpticalDevice(const OpticalDevice& o) = delete;
    OpticalDevice& operator=(const OpticalDevice&) = delete;

  public:
    //         override from ns3::NetDevice           //
    // ============================================== //
    void SetIfIndex(const uint32_t index) override;
    uint32_t GetIfIndex() const override;
    Ptr<Channel> GetChannel() const override;
    void SetAddress(Address address) override;
    Address GetAddress() const override;
    bool SetMtu(const uint16_t mtu) override;
    uint16_t GetMtu() const override;
    bool IsLinkUp() const override;
    void AddLinkChangeCallback(Callback<void> callback) override;
    bool IsBroadcast() const override;
    Address GetBroadcast() const override;
    bool IsMulticast() const override;
    Address GetMulticast(Ipv4Address multicastGroup) const override;
    bool IsPointToPoint() const override;
    bool IsBridge() const override;
    bool Send(Ptr<Packet> packet, const Address& dest, uint16_t protocolNumber) override;
    bool SendFrom(Ptr<Packet> packet,
                  const Address& source,
                  const Address& dest,
                  uint16_t protocolNumber) override;
    Ptr<Node> GetNode() const override;
    void SetNode(Ptr<Node> node) override;
    bool NeedsArp() const override;
    void SetReceiveCallback(NetDevice::ReceiveCallback cb) override;
    Address GetMulticast(Ipv6Address addr) const override;
    void SetPromiscReceiveCallback(PromiscReceiveCallback cb) override;
    bool SupportsSendFrom() const override;

  protected:
    void DoDispose() override;
    void NotifyLinkUp();
    // ============================================== //

    //                   NetDevice                    //
    // ============================================== //
  public:
    void SetDataRate(DataRate bps);
    void SetQueue(Ptr<Queue<Packet>> queue);
    Ptr<Queue<Packet>> GetQueue() const;
    void SetReceiveErrorModel(Ptr<ErrorModel> em);
    void Attach(Ptr<OpticalChannel> channel);
    void Detach();
    void Receive(Ptr<Packet> p);
    Ptr<MobilityModel> GetMobility() const;

  private:
    bool TransmitStart(Ptr<Packet> packet);
    void TransmitComplete();

    // Enumeration of the states of the transmit machine of the net device.
    enum TxMachineState
    {
        READY, /**< The transmitter is ready to begin transmission of a packet */
        BUSY   /**< The transmitter is busy transmitting a packet */
    };

    TxMachineState m_txMachineState = READY;

    DataRate m_bps;
    Ptr<Queue<Packet>> m_queue;
    Ptr<ErrorModel> m_receiveErrorModel;

    Ptr<Node> m_node;              //!< Node owning this NetDevice
    Ptr<OpticalChannel> m_channel; //!< Channel the device attach to
    Ptr<MobilityModel> m_mobility;
    Ptr<Packet> m_currentPkt = nullptr;       //!< Current packet processed
    static const uint16_t DEFAULT_MTU = 1500; //!< Default MTU
    uint32_t m_mtu;

    bool m_linkUp = false;                               //!< Identify if the link is up or not
    uint32_t m_ifIndex;                                  //!< Index of the interface
    Mac48Address m_address;                              //!< Mac48Address of this NetDevice
    NetDevice::ReceiveCallback m_rxCallback;             //!< Receive callback
    NetDevice::PromiscReceiveCallback m_promiscCallback; //!< Receive callback
                                                         //   (promisc data)

    TracedCallback<> m_linkChangeCallbacks; //!< Callback for the link change event
    TracedCallback<Ptr<const Packet>> m_macTxTrace;
    TracedCallback<Ptr<const Packet>> m_macTxDropTrace;
    TracedCallback<Ptr<const Packet>> m_macPromiscRxTrace;
    TracedCallback<Ptr<const Packet>> m_macRxTrace;
    TracedCallback<Ptr<const Packet>> m_macRxDropTrace;
    TracedCallback<Ptr<const Packet>> m_phyTxBeginTrace;
    TracedCallback<Ptr<const Packet>> m_phyTxEndTrace;
    TracedCallback<Ptr<const Packet>> m_phyTxDropTrace;
    TracedCallback<Ptr<const Packet>> m_phyRxBeginTrace;
    TracedCallback<Ptr<const Packet>> m_phyRxEndTrace;
    TracedCallback<Ptr<const Packet>> m_phyRxDropTrace;
    TracedCallback<Ptr<const Packet>> m_snifferTrace;
    TracedCallback<Ptr<const Packet>> m_promiscSnifferTrace;
    // ============================================== //

  public:
    //         Laser communication terminal           //
    // ============================================== //
    // 指向方向
    enum Direction
    {
        RIGHT = 0,
        LEFT,
        FORWARD,
        BACKWARD,
        UP,
        DOWN
    };

    void SetLinkDown()
    {
        m_linkUp = false;
        m_linkChangeCallbacks();
    }

    bool GetIsInitialized() const
    {
        return m_initialized;
    }

    /**
     * @param d 指向方向
     */
    void SetDirection(Direction d)
    {
        m_direction = d;
        m_initialized = true;
    }

    Direction GetDirection() const
    {
        return m_direction;
    }

    /**
     * @param txPower 发送功率（dBm）
     */
    void SetTxPowerdBm(double txPower)
    {
        m_TxPowerDbm = txPower;
    }

    double GetTxPowerdBm() const
    {
        return m_TxPowerDbm;
    }

    /**
     * @param rxSensitivityDbm 接收灵敏度（dBm）
     */
    void SetRxSensitivitydBm(double rxSensitivityDbm)
    {
        m_rxSensitivityDbm = rxSensitivityDbm;
    }

    double GetRxSensitivitydBm() const
    {
        return m_rxSensitivityDbm;
    }

    /**
     * @param wl 通信波长(nm)
     */
    void SetWaveLength(double wl)
    {
        m_waveLength = wl * 1e-9;
    }

    double GetWaveLength() const
    {
        return m_waveLength * 1e9;
    }

    /**
     * @param txGain 发送总增益
     */
    void SetTxGain(double txGain)
    {
        m_TxGain = txGain;
    }

    double GetTxGain() const
    {
        return m_TxGain;
    }

    /**
     * @param rxGain 接收总增益
     */
    void SetRxGain(double rxGain)
    {
        m_RxGain = rxGain;
    }

    double GetRxGain() const
    {
        return m_RxGain;
    }

  private:
    bool m_initialized = false;
    Direction m_direction;           // 激光终端安装方位
    double m_TxPowerDbm = 20;        // 发射功率(dBm)
    double m_rxSensitivityDbm = -40; // 接收灵敏度阈值(dBm)

    double m_waveLength = 1550 * 1e-9; // 发射波长(m)
    double m_TxGain = 110;             // 发射总增益(dB)
    double m_RxGain = 110;             // 接收总增益(dB)
    // ============================================== //
};

} // namespace ns3

#endif