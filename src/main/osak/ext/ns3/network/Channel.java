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
 * Abstract Channel Base Class.
 * <p>
 * A channel is a logical path over which information flows. The path can be as
 * simple as a short piece of wire, or as complicated as space-time.
 * <p>
 * Subclasses must use Simulator::ScheduleWithContext to correctly update event
 * contexts when scheduling an event from one node to another one.
 * 
 * @author zhangrui
 * @since 1.0
 */
public interface Channel {
    /**
     * This method must be implemented by subclasses.
     * 
     * @returns the number of NetDevices connected to this Channel.
     *
     */
    long GetNDevices();

    /**
     * This method must be implemented by subclasses.
     * 
     * @param i index of NetDevice to retrieve
     * @returns one of the NetDevices connected to this channel.
     *
     */
    NetDevice GetDevice(long i);

}
