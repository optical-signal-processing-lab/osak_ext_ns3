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

/**
 * short->unsigned Byte
 * Example usage:
 * <pre>{@code
 * UnsignedByte unsignedbyte = new UnsignedByte((short)128);
 * int unsignedValue = unsignedbyte.toUnsignedInt();
 * }</pre>
 * TODO UnsignedByte
 * 
 * @author lijianjun
 * @since   1.0
 */
public class UnsignedByte{
    private short value;
    
    public UnsignedByte(byte value) {
	this.value = value;
    }
    
    public short getValue() {
	return value;
    }
    
    public void setValue(byte value) {
	this.value = value;
    }
    
    public int toUnsignedInt() {
	return (value & 0Xff);
    }

    @Override
    public String toString() {
	return "UnsignedByte [value=" + value + "]";
    }
}