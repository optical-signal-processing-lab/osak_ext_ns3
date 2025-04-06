/*
 * Copyright 2024 OSPLAB (Optical Signal Processing Lab Of UESTC)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package osak.ext.ns3.internet;

import java.net.Socket;

import osak.ext.ns3.network.NetDevice;
import osak.ext.ns3.network.Packet;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO Ipv4L3Protocol
 * 
 * @author zhangrui
 * @since   1.0
 */
public class Ipv4L3Protocol implements Ipv4 {
    public static final short PROT_NUMBER = 0x0800;

    @Override
    public void SetRoutingProtocol(Ipv4RoutingProtocol routingProtocol) {
	// TODO Auto-generated method stub

    }

    @Override
    public Ipv4RoutingProtocol GetRoutingProtocol() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int AddInterface(NetDevice device) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int GetNInterfaces() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int GetInterfaceForAddress(Ipv4Address address) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void Send(Packet packet, Ipv4Address source, Ipv4Address destination, byte protocol, Ipv4Route route) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SendWithHeader(Packet packet, Ipv4Header ipHeader, Ipv4Route route) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Insert(IpL4Protocol protocol) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Insert(IpL4Protocol protocol, int ifaceIndex) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Remove(IpL4Protocol protocol) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Remove(IpL4Protocol protocol, int interfaceIndex) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean IsDestinationAddress(Ipv4Address address, int iif) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public int GetInterfaceForPrefix(Ipv4Address address, Ipv4Mask mask) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public NetDevice GetNetDevice(int iface) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int GetInterfaceForDevice(NetDevice device) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean AddAddress(int iface, Ipv4InterfaceAddress address) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public int GetNAddresses(int iface) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public Ipv4InterfaceAddress GetAddress(int iface, int addressIndex) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean RemoveAddress(int iface, int addressIndex) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean RemoveAddress(int iface, Ipv4Address address) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Ipv4Address SelectSourceAddress(NetDevice device, Ipv4Address dst,
	    Ipv4InterfaceAddress.InterfaceAddressScope_e scope) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void SetMetric(int iface, short metric) {
	// TODO Auto-generated method stub

    }

    @Override
    public short GetMetric(int iface) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public short GetMtu(int iface) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean IsUp(int iface) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void SetUp(int iface) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SetDown(int iface) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean IsForwarding(int iface) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void SetForwarding(int iface, boolean val) {
	// TODO Auto-generated method stub

    }

    @Override
    public Ipv4Address SourceAddressSelection(int iface, Ipv4Address dest) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IpL4Protocol GetProtocol(int protocolNumber) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IpL4Protocol GetProtocol(int protocolNumber, int interfaceIndex) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Socket CreateRawSocket() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void DeleteRawSocket(Socket socket) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SetIpForward(boolean forward) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean GetIpForward() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void SetWeakEsModel(boolean model) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean GetWeakEsModel() {
	// TODO Auto-generated method stub
	return false;
    }
}
