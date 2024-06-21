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
package osak.ext.ns3.network;

/**
 * TODO SocketSetDontFragmentTag
 * 
 * @author zhangrui
 * @since   1.0
 */
public class SocketSetDontFragmentTag implements Tag {
    private boolean m_dontFragment; // !< DF bit value for outgoing packets.
    /**
     * 
     */
    public SocketSetDontFragmentTag() {
    }

    /**
     * \brief Enables the DF (Don't Fragment) flag
     */
    public void Enable() {
	m_dontFragment = true;
    }

    /**
     * \brief Disables the DF (Don't Fragment) flag
     */
    public void Disable() {
	m_dontFragment = false;
    }

    /**
     * \brief Checks if the DF (Don't Fragment) flag is set
     *
     * \returns true if DF is set.
     */
    public boolean IsEnabled() {
	return m_dontFragment;
    }

    @Override
    public int GetSerializedSize() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void Serialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Deserialize(TagBuffer i) {
	// TODO Auto-generated method stub

    }

    @Override
    public void Print() {
	// TODO Auto-generated method stub

    }

}
