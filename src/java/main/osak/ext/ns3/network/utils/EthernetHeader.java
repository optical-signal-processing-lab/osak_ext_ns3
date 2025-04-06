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
package osak.ext.ns3.network.utils;

import java.nio.ByteBuffer;

import osak.ext.ns3.network.Address;
import osak.ext.ns3.network.Header;

/**
 * TODO EthernetHeader
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class EthernetHeader implements Header {
    @Override
    public int GetSerializedSize() {
	return 14;
    }

    @Override
    public void Serialize(ByteBuffer buffer) {
	buffer.put(DstMac);
	buffer.put(SrcMac);
	buffer.putShort(Type.value());
    }

    @Override
    public int Deserialize(ByteBuffer buffer) {
	int start = buffer.position();
	buffer.get(DstMac);
	buffer.get(SrcMac);
	Type = EtherType.valueOf(buffer.getShort());
	return buffer.position() - start;
    }
    
    public byte[] GetDstMac() {
	return DstMac;
    }
    
    public byte[] GetSrcMac() {
	return SrcMac;
    }
    
    public EtherType GetType() {
	return Type;
    }
    
    public void SetDstMac(Address dstMac) {
	assert (dstMac.GetLength() == (byte) 6);
	dstMac.CopyTo(DstMac);
    }

    public void SetSrcMac(Address srcMac) {
	assert (srcMac.GetLength() == (byte) 6);
	srcMac.CopyTo(SrcMac);
    }

    public void SetType(EtherType type) {
	Type = type;
    }

    private byte[] DstMac = new byte[6];
    private byte[] SrcMac = new byte[6];
    private EtherType Type;

}
