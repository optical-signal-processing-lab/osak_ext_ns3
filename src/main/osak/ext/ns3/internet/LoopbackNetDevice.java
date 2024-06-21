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

import java.net.Inet6Address;

import osak.ext.ns3.callback.Callback0;
import osak.ext.ns3.callback.CallbackR4;
import osak.ext.ns3.callback.CallbackR6;
import osak.ext.ns3.network.*;
import osak.ext.ns3.network.utils.Ipv4Address;

/**
 * TODO LoopbackNetDevice
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class LoopbackNetDevice implements NetDevice {

    @Override
    public void SetIfIndex(int index) {
	// TODO Auto-generated method stub

    }

    @Override
    public int GetIfIndex() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public Channel GetChannel() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void SetAddress(Address address) {
	// TODO Auto-generated method stub

    }

    @Override
    public Address GetAddress() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean SetMtu(short mtu) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public short GetMtu() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean IsLinkUp() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void AddLinkChangeCallback(Callback0 callback) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean IsBroadcast() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Address GetBroadcast() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean IsMulticast() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Address GetMulticast(Ipv4Address multicastGroup) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Address GetMulticast(Inet6Address addr) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean IsBridge() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean IsPointToPoint() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean Send(Packet packet, Address dest, short protocolNumber) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean SendFrom(Packet packet, Address source, Address dest, short protocolNumber) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Node GetNode() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void SetNode(Node node) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean NeedsArp() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void SetReceiveCallback(CallbackR4<Boolean, NetDevice, Packet, Short, Address> cb) {
	// TODO Auto-generated method stub

    }

    @Override
    public void SetPromiscReceiveCallback(
	    CallbackR6<Boolean, NetDevice, Packet, Short, Address, Address, PacketType> cb) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean SupportsSendFrom() {
	// TODO Auto-generated method stub
	return false;
    }

}
