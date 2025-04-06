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
 * TODO SocketErrno
 * 
 * @author zhangrui
 * @since   1.0
 */
public enum SocketErrno {
    ERROR_NOTERROR, ERROR_ISCONN, ERROR_NOTCONN, ERROR_MSGSIZE, ERROR_AGAIN, ERROR_SHUTDOWN, ERROR_OPNOTSUPP,
    ERROR_AFNOSUPPORT, ERROR_INVAL, ERROR_BADF, ERROR_NOROUTETOHOST, ERROR_NODEV, ERROR_ADDRNOTAVAIL, ERROR_ADDRINUSE,
    SOCKET_ERRNO_LAST
}
