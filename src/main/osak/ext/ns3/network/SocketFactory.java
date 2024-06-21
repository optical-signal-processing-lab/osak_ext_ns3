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
 * Object to create transport layer instances that provide a socket API to
 * applications.
 * <p>
 * This base class defines the API for creating sockets. The socket factory also
 * can hold the global variables used to initialize newly created sockets, such
 * as values that are set through the sysctl or proc interfaces in Linux.
 * <p>
 * If you want to write a new transport protocol accessible through sockets, you
 * need to subclass this factory class, implement CreateSocket, instantiate the
 * object, and aggregate it to the node.
 * 
 * @see Udp
 * @see UdpImpl
 * @author zhangrui
 * @since 1.0
 */
public abstract class SocketFactory {

    public SocketFactory() {

    }
    /**
     * Base class method for creating socket instances.
     * 
     * @return smart pointer to Socket
     *
     */
    public abstract Socket CreateSocket();

}
