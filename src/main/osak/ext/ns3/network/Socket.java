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

import java.util.ArrayList;
import java.util.List;

import osak.ext.communication.MyLog;
import osak.ext.ns3.callback.Callback1;
import osak.ext.ns3.callback.Callback2;
import osak.ext.ns3.callback.CallbackR2;
import osak.ext.ns3.network.utils.Ipv6Address;

/**
 * A low-level Socket API based loosely on the BSD Socket API.
 * <p>
 * 
 * <pre>
 * A few things to keep in mind about this type of socket:
 * - it uses ns-3 API constructs such as class osak.ext.ns3::Address instead of
 *   C-style structs
 * - in contrast to the original BSD socket API, this API is asynchronous:
 *   it does not contain blocking calls.  Sending and receiving operations
 *   must make use of the callbacks provided.
 * - It also uses class osak.ext.ns3::Packet as a fancy byte buffer, allowing
 *   data to be passed across the API using an ns-3 Packet instead of
 *   a raw data pointer.
 * - Not all of the full POSIX sockets API is supported
 *
 * Other than that, it tries to stick to the BSD API to make it
 * easier for those who know the BSD API to use this API.
 * More details are provided in the ns-3 tutorial.
 * </pre>
 * 
 * @author zhangrui
 * @since 1.0
 */
public abstract class Socket {
    public Socket() {
	m_manualIpTtl = false;
	m_ipRecvTos = false;
	m_ipRecvTtl = false;
	m_manualIpv6Tclass = false;
	m_manualIpv6HopLimit = false;
	m_ipv6RecvTclass = false;
	m_ipv6RecvHopLimit = false;
	m_boundnetdevice = null;
	m_recvPktInfo = false;

	m_priority = 0;
	m_ipTos = 0;
	m_ipTtl = 0;
	m_ipv6Tclass = 0;
	m_ipv6HopLimit = 0;
    }

    /**
     * SocketType
     * <p>
     * Enumeration of the possible socket types.
     */
    public enum SocketType {
	NS3_SOCK_STREAM, NS3_SOCK_SEQPACKET, NS3_SOCK_DGRAM, NS3_SOCK_RAW
    }

    /**
     * SocketPriority: Enumeration of the possible socket priorities.
     *
     * Names and corresponding values are derived from the Linux TC_PRIO_* macros
     */
    public enum SocketPriority {
	NS3_PRIO_BESTEFFORT((byte) 0), NS3_PRIO_FILLER((byte) 1), NS3_PRIO_BULK((byte) 2),
	NS3_PRIO_INTERACTIVE_BULK((byte) 4), NS3_PRIO_INTERACTIVE((byte) 6), NS3_PRIO_CONTROL((byte) 7);

	private byte value;

	private SocketPriority(byte value) {
	    this.value = value;
	}

	public static SocketPriority valueOf(byte value) {
	    switch (value) {
	    case 0:
		return NS3_PRIO_BESTEFFORT;
	    case 1:
		return NS3_PRIO_FILLER;
	    case 2:
		return NS3_PRIO_BULK;
	    case 4:
		return NS3_PRIO_INTERACTIVE_BULK;
	    case 6:
		return NS3_PRIO_INTERACTIVE;
	    case 7:
		return NS3_PRIO_CONTROL;
	    default:
		throw new RuntimeException("Unknown SocketPriority: " + value);
	    }
	}

	public byte value() {
	    return this.value;
	}
    };

    /**
     * Ipv6MulticastFilterMode: Enumeration of the possible filter of a socket.
     *
     * A socket can have filters on specific sources to include only
     * packets incoming from them, or to exclude packets incoming
     * from specific sources.
     * Moreover, inclusion and exclusion also works as a leave,
     * since "joining" a group without allowed sources is equivalent
     * to leaving it.
     */
    public enum Ipv6MulticastFilterMode
    {
	INCLUDE, // 1
        EXCLUDE
    };

    /**
     * This method wraps the creation of sockets that is performed on a given node
     * by a SocketFactory specified by TypeId.
     *
     * @return A smart pointer to a newly created socket.
     *
     * @param node The node on which to create the socket
     * @param tid  The TypeId of a SocketFactory class to use
     */
    public static Socket CreateSocket(Node node, String classname) {
	Socket s = null;
	assert (node != null);
	Class<?> cls;
	SocketFactory socketFactory = null;
	try {
	    cls = Class.forName(classname);
	    socketFactory = (SocketFactory) cls.getDeclaredConstructor().newInstance();
	} catch (Exception e) {
	    // return null;
	}
	assert (socketFactory != null);
	s = socketFactory.CreateSocket();
	assert (s != null);
	return s;
    }

    /**
     * Get last error number.
     *
     * @return the errno associated to the last call which failed in this socket.
     *         Each socket's errno is initialized to zero when the socket is
     *         created.
     */
    public abstract SocketErrno GetErrno();

    /**
     * @return the socket type, analogous to getsockopt (SO_TYPE)
     */
    public abstract SocketType GetSocketType();

    /**
     * Return the node this socket is associated with.
     * 
     * @returns the node
     */
    public abstract Node GetNode();

    /**
     * Specify callbacks to allow the caller to determine if the connection succeeds
     * of fails.
     * 
     * @param connectionSucceeded this callback is invoked when the connection
     *                            request initiated by the user is successfully
     *                            completed. The callback is passed back a pointer
     *                            to the same socket object.
     * @param connectionFailed    this callback is invoked when the connection
     *                            request initiated by the user is unsuccessfuly
     *                            completed. The callback is passed back a pointer
     *                            to the same socket object.
     */
    public void SetConnectCallback(Callback1<Socket> connectionSucceeded, Callback1<Socket> connectionFailed) {
	m_connectionSucceeded = connectionSucceeded;
	m_connectionFailed = connectionFailed;
    }

    /**
     * Detect socket recv() events such as graceful shutdown or error.
     * <p>
     * For connection-oriented sockets, the first callback is used to signal that
     * the remote side has gracefully shut down the connection, and the second
     * callback denotes an error corresponding to cases in which a traditional
     * recv() socket call might return -1 (error), such as a connection reset. For
     * datagram sockets, these callbacks may never be invoked.
     *
     * @param normalClose this callback is invoked when the peer closes the
     *                    connection gracefully
     * @param errorClose  this callback is invoked when the connection closes
     *                    abnormally
     */
    public void SetCloseCallbacks(Callback1<Socket> normalClose, Callback1<Socket> errorClose) {
	m_normalClose = normalClose;
	m_errorClose = errorClose;
    }

    /**
     * Accept connection requests from remote hosts
     * @param connectionRequest Callback for connection request from peer.
     *        This user callback is passed a pointer to this socket, the
     *        ip address and the port number of the connection originator.
     *        This callback must return true to accept the incoming connection,
     *        false otherwise. If the connection is accepted, the
     *        "newConnectionCreated" callback will be invoked later to
     *        give access to the user to the socket created to match
     *        this new connection. If the user does not explicitly
     *        specify this callback, all incoming  connections will be refused.
     * @param newConnectionCreated Callback for new connection: when a new
     *        is accepted, it is created and the corresponding socket is passed
     *        back to the user through this callback. This user callback is
     *        passed a pointer to the new socket, and the ip address and
     *        port number of the connection originator.
     */
    public void SetAcceptCallback(CallbackR2<Boolean, Socket, Address> connectionRequest,
	    Callback2<Socket, Address> newConnectionCreated) {
	m_connectionRequest = connectionRequest;
	m_newConnectionCreated = newConnectionCreated;
    }

    /**
     * Notify application when a packet has been sent from transport
     *        protocol (non-standard socket call)
     * @param dataSent Callback for the event that data is sent from the
     *        underlying transport protocol.  This callback is passed a
     *        pointer to the socket, and the number of bytes sent.
     */
    public void SetDataSentCallback(Callback2<Socket, Integer> dataSent) {
	m_dataSent = dataSent;
    }

    /**
     * Notify application when space in transmit buffer is added
     * <p>
     *
     *        This callback is intended to notify a
     *        socket that would have been blocked in a blocking socket model
     *        that space is available in the transmit buffer and that it
     *        can call Send() again.
     *
     * @param sendCb Callback for the event that the socket transmit buffer
     *        fill level has decreased.  This callback is passed a pointer to
     *        the socket, and the number of bytes available for writing
     *        into the buffer (an absolute value).  If there is no transmit
     *        buffer limit, a maximum-sized integer is always returned.
     */
    public void SetSendCallback(Callback2<Socket, Integer> sendCb) {
	m_sendCb = sendCb;
    }

    /**
     * Notify application when new data is available to be read.
     * <p>
     * This callback is intended to notify a socket that would have been blocked in
     * a blocking socket model that data is available to be read.
     * 
     * @param receivedData Callback for the event that data is received from the
     *                     underlying transport protocol. This callback is passed a
     *                     pointer to the socket.
     */
    public void SetRecvCallback(Callback1<Socket> receivedData) {
	m_receivedData = receivedData;
    }

    /**
     * Allocate a local endpoint for this socket.
     * 
     * @param address the address to try to allocate
     * @returns 0 on success, -1 on failure.
     */
    public abstract int Bind(final Address address);

    /**
     * @brief Allocate a local IPv4 endpoint for this socket.
     *
     * @returns 0 on success, -1 on failure.
     */
    public abstract int Bind();

    /**
     * @brief Allocate a local IPv6 endpoint for this socket.
     *
     * @returns 0 on success, -1 on failure.
     */
    public abstract int Bind6();

    /**
     *
     * After the Close call, the socket is no longer valid, and cannot safely be
     * used for subsequent operations.
     * 
     * @brief Close a socket.
     * @returns zero on success, -1 on failure.
     */
    public abstract int Close();

    /**
     *
     * Do not allow any further Send calls. This method is typically implemented for
     * Tcp sockets by a half close.
     * 
     * @returns zero on success, -1 on failure.
     */
    public abstract int ShutdownSend();

    /**
     *
     * Do not allow any further Recv calls. This method is typically implemented for
     * Tcp sockets by a half close.
     * 
     * @returns zero on success, -1 on failure.
     */
    public abstract int ShutdownRecv();

    /**
     * @brief Initiate a connection to a remote host
     * @param address Address of remote.
     * @returns 0 on success, -1 on error (in which case errno is set).
     */
    public abstract int Connect(final Address address);

    /**
     * @brief Listen for incoming connections.
     * @returns 0 on success, -1 on error (in which case errno is set).
     */
    public abstract int Listen();

    /**
     * Returns the number of bytes which can be sent in a single call to Send.
     * <p>
     * For datagram sockets, this returns the number of bytes that can be passed
     * atomically through the underlying protocol.
     * <p>
     * For stream sockets, this returns the available space in bytes left in the
     * transmit buffer.
     *
     * @returns The number of bytes which can be sent in a single Send call.
     */
    public abstract int GetTxAvailable();
    
    /**
     * <b> Send data (or dummy data) to the remote host</b>
     * <p>
     * This function matches closely in semantics to the send() function call in the
     * standard C library (libc): ssize_t send (int s, const void *msg, size_t len,
     * int flags); except that the send I/O is asynchronous. This is the primary
     * Send method at this low-level API and must be implemented by subclasses.
     * <p>
     * In a typical blocking sockets model, this call would block upon lack of space
     * to hold the message to be sent. In ns-3 at this API, the call returns
     * immediately in such a case, but the callback registered with
     * SetSendCallback() is invoked when the socket has space (when it conceptually
     * unblocks); this is an asynchronous I/O model for send().
     * <p>
     * This variant of Send() uses class osak.ext.ns3::Packet to encapsulate data, rather
     * than providing a raw pointer and length field. This allows an ns-3
     * application to attach tags if desired (such as a flow ID) and may allow the
     * simulator to avoid some data copies. Despite the appearance of sending
     * Packets on a stream socket, just think of it as a fancy byte buffer with
     * streaming semantics.
     * <p>
     * If either the message buffer within the Packet is too long to pass atomically
     * through the underlying protocol (for datagram sockets), or the message buffer
     * cannot entirely fit in the transmit buffer (for stream sockets), -1 is
     * returned and SocketErrno is set to ERROR_MSGSIZE. If the packet does not fit,
     * the caller can split the Packet (based on information obtained from
     * GetTxAvailable) and reattempt to send the data.
     * <p>
     * The flags argument is formed by or'ing one or more of the values:
     * 
     * <pre>
     *        MSG_OOB        process out-of-band data
     *        MSG_DONTROUTE  bypass routing, use direct interface
     * </pre>
     * 
     * These flags are _unsupported_ as of ns-3.1.
     *
     * @param p     osak.ext.ns3::Packet to send
     * @param flags Socket control flags
     * @returns the number of bytes accepted for transmission if no error occurs,
     *          and -1 otherwise.
     *
     * @see SetSendCallback
     */
    public abstract int Send(Packet p, int flags);

    /**
     * Send data to a specified peer.
     * <p>
     * This method has similar semantics to Send () but subclasses may want to
     * provide checks on socket state, so the implementation is pushed to
     * subclasses.
     *
     * @param p         packet to send
     * @param flags     Socket control flags
     * @param toAddress IP Address of remote host
     * @return -1 in case of error or the number of bytes copied in the internal
     *         buffer and accepted for transmission.
     */
    public abstract int SendTo(Packet p, int flags, final Address toAddress);
    
    /**
     * Return number of bytes which can be returned from one or
     * multiple calls to Recv.
     * Must be possible to call this method from the Recv callback.
     *
     * @returns the number of bytes which can be returned from one or
     *          multiple Recv calls.
     */
    public abstract int GetRxAvailable();
    
    /**
     * <h> Read data from the socket</h>
     *<p>
     * This function matches closely in semantics to the recv() function
     * call in the standard C library (libc):
     *   ssize_t recv (int s, void *buf, size_t len, int flags);
     * except that the receive I/O is asynchronous.  This is the
     * primary Recv method at this low-level API and must be implemented
     * by subclasses.
     *<p>
     * This method is normally used only on a connected socket.
     * In a typical blocking sockets model, this call would block until
     * at least one byte is returned or the connection closes.
     * In ns-3 at this API, the call returns immediately in such a case
     * and returns 0 if nothing is available to be read.
     * However, an application can set a callback, osak.ext.ns3::SetRecvCallback,
     * to be notified of data being available to be read
     * (when it conceptually unblocks); this is an asynchronous
     * I/O model for recv().
     *<p>
     * This variant of Recv() uses class osak.ext.ns3::Packet to encapsulate
     * data, rather than providing a raw pointer and length field.
     * This allows an ns-3 application to attach tags if desired (such
     * as a flow ID) and may allow the simulator to avoid some data
     * copies.  Despite the appearance of receiving Packets on a stream
     * socket, just think of it as a fancy byte buffer with streaming
     * semantics.
     *
     * The semantics depend on the type of socket.  For a datagram socket,
     * each Recv() returns the data from at most one Send(), and order
     * is not necessarily preserved.  For a stream socket, the bytes
     * are delivered in order, and on-the-wire packet boundaries are
     * not preserved.
     *<p>
     * The flags argument is formed by or'ing one or more of the values:
     * <pre>
     *        MSG_OOB             process out-of-band data
     *        MSG_PEEK            peek at incoming message
     * </pre>
     * None of these flags are supported for now.
     *<p>
     * Some variants of Recv() are supported as additional API,
     * including RecvFrom(), overloaded Recv() without arguments,
     * and variants that use raw character buffers.
     *
     * @param maxSize reader will accept packet up to maxSize
     * @param flags Socket control flags
     * @returns Ptr<Packet> of the next in-sequence packet.  Returns
     * 0 if the socket cannot return a next in-sequence packet conforming
     * to the maxSize and flags.
     *
     * @see SetRecvCallback
     */
    public abstract Packet Recv(int maxSize, int flags);
    
    /**
     * Read a single packet from the socket and retrieve the sender
     * address.
     *<p>
     * Calls Recv(maxSize, flags) with maxSize
     * implicitly set to maximum sized integer, and flags set to zero.
     *<p>
     * This method has similar semantics to Recv () but subclasses may
     * want to provide checks on socket state, so the implementation is
     * pushed to subclasses.
     *<p>
     * @param maxSize reader will accept packet up to maxSize
     * @param flags Socket control flags
     * @param fromAddress output parameter that will return the
     * address of the sender of the received packet, if any.  Remains
     * untouched if no packet is received.
     * @returns Ptr<Packet> of the next in-sequence packet.  Returns
     * 0 if the socket cannot return a next in-sequence packet.
     */
    public abstract Packet RecvFrom(int maxSize, int flags, Address fromAddress);
    
    /////////////////////////////////////////////////////////////////////
    //   The remainder of these public methods are overloaded methods  //
    //   or variants of Send() and Recv(), and they are non-virtual    //
    /////////////////////////////////////////////////////////////////////

    /**
    * Send data (or dummy data) to the remote host
    *<p>
    * Overloaded version of Send(..., flags) with flags set to zero.
    *
    * @param p osak.ext.ns3::Packet to send
    * @returns the number of bytes accepted for transmission if no error
    *          occurs, and -1 otherwise.
    */
    public int Send(Packet p) {
	return Send(p, 0);
    }
    
    /**
     * Send data (or dummy data) to the remote host
     * <p>
     * This method is provided so as to have an API which is closer in
     * appearance to that of real network or BSD sockets.
     *
     * @param buf A pointer to a raw byte buffer of some data to send.  If
     * this buffer is 0, we send dummy data whose size is specified by the
     * second parameter
     * @param size the number of bytes to copy from the buffer
     * @param flags Socket control flags
     * @returns the number of bytes accepted for transmission if no error
     *          occurs, and -1 otherwise.
     */
    public int Send(final byte[] buf, int size, int flags) {
	Packet p;
	if (buf == null) {
	    p = new Packet(buf, size);
	} else {
	    p = new Packet(size);
	}
	return Send(p, flags);
    }
    
    /**
     * Send data to a specified peer.
     *<p>
     * This method is provided so as to have an API which is closer in
     * appearance to that of real network or BSD sockets.
     *
     * @param buf A pointer to a raw byte buffer of some data to send.
     * If this is 0, we send dummy data whose size is specified by the
     * third parameter
     * @param size the number of bytes to copy from the buffer
     * @param flags Socket control flags
     * @param address IP Address of remote host
     * @returns -1 in case of error or the number of bytes copied in the
     *          internal buffer and accepted for transmission.
     *
     */
    public int SendTo(final byte[] buf, int size, int flags, final Address toAddress) {
	Packet p;
	if (buf == null) {
	    p = new Packet(buf, size);
	} else {
	    p = new Packet(size);
	}
	return SendTo(p, flags, toAddress);
    }
    
    /**
     * Read a single packet from the socket
     *<p>
     * Overloaded version of Recv(maxSize, flags) with maxSize
     * implicitly set to maximum sized integer, and flags set to zero.
     *
     * @returns Ptr<Packet> of the next in-sequence packet.  Returns
     * 0 if the socket cannot return a next in-sequence packet.
     */
    public Packet Recv() {
	return Recv(Integer.MAX_VALUE, 0);
    }
    
    /**
     * Recv data (or dummy data) from the remote host
     *<p>
     * This method is provided so as to have an API which is closer in
     * appearance to that of real network or BSD sockets.
     *<p>
     * If the underlying packet was carring null (fake) data, this buffer
     * will be zeroed up to the length specified by the return value.
     *
     * @param buf A pointer to a raw byte buffer to write the data to.
     * @param size Number of bytes (at most) to copy to buf
     * @param flags any flags to pass to the socket
     * @returns number of bytes copied into buf
     */
    public int Recv(byte[] buf, int size, int flags) {
	Packet p = Recv(size, flags);// read up to "size" bytes
	if (p == null) {
	    return 0;
	}
	p.CopyData(buf, p.GetSize());
	return p.GetSize();
    }
    
    /**
     * Read a single packet from the socket and retrieve the sender
     * address.
     *<p>
     * Calls RecvFrom (maxSize, flags, fromAddress) with maxSize
     * implicitly set to maximum sized integer, and flags set to zero.
     *
     * @param fromAddress output parameter that will return the
     * address of the sender of the received packet, if any.  Remains
     * untouched if no packet is received.
     * @returns Ptr<Packet> of the next in-sequence packet.  Returns
     * 0 if the socket cannot return a next in-sequence packet.
     */
    public Packet RecvFrom(Address fromAddress) {
	return RecvFrom(Integer.MAX_VALUE, 0, fromAddress);
    }
    
    /**
     * Read a single packet from the socket and retrieve the sender
     * address.
     *<p>
     * This method is provided so as to have an API which is closer in
     * appearance to that of real network or BSD sockets.
     *
     * @param buf A pointer to a raw byte buffer to write the data to.
     * If the underlying packet was carring null (fake) data, this buffer
     * will be zeroed up to the length specified by the return value.
     * @param size Number of bytes (at most) to copy to buf
     * @param flags any flags to pass to the socket
     * @param fromAddress output parameter that will return the
     * address of the sender of the received packet, if any.  Remains
     * untouched if no packet is received.
     * @returns number of bytes copied into buf
     */
    public int RecvFrom(byte[] buf, int size, int flags, Address fromAddress) {
	Packet p = RecvFrom(size, flags, fromAddress);
	if (p == null) {
	    return 0;
	}
	p.CopyData(buf, p.GetSize());
	return p.GetSize();
    }
    
    /**
     * Get socket address.
     * 
     * @param address the address name this socket is associated with.
     * @returns 0 if success, -1 otherwise
     */
    public abstract int GetSockName(Address address);
    
    /**
     * @brief Get the peer address of a connected socket.
     * @param address the address this socket is connected to.
     * @returns 0 if success, -1 otherwise
     */
    public abstract int GetPeerName(Address address);

    /**
     * Bind a socket to specific device.
     * <p>
     * This method corresponds to using setsockopt() SO_BINDTODEVICE of real network
     * or BSD sockets. If set on a socket, this option will force packets to leave
     * the bound device regardless of the device that IP routing would naturally
     * choose. In the receive direction, only packets received from the bound
     * interface will be delivered.
     * <p>
     * This option has no particular relationship to binding sockets to an address
     * via Socket::Bind (). It is possible to bind sockets to a specific IP address
     * on the bound interface by calling both Socket::Bind (address) and
     * Socket::BindToNetDevice (device), but it is also possible to bind to
     * mismatching device and address, even if the socket can not receive any
     * packets as a result.
     *
     * @param netdevice Pointer to NetDevice of desired interface
     */
    public void BindToNetDevice(NetDevice netdevice) {
	if (netdevice != null) {
	    boolean found = false;
	    for (int i = 0; i < GetNode().GetNDevices(); i++) {
		if (GetNode().GetDevice(i).equals(netdevice)) {
		    found = true;
		    break;
		}
	    }
	    assert found : "Socket cannot be bound to a NetDevice not existing on the Node";
	}
	m_boundnetdevice = netdevice;
    }

    /**
     * Returns socket's bound NetDevice, if any.
     * <p>
     * This method corresponds to using getsockopt() SO_BINDTODEVICE of real network
     * or BSD sockets.
     *
     *
     * @returns Pointer to interface.
     */
    public NetDevice GetBoundNetDevice() {
	return m_boundnetdevice;
    }

    /**
     * Configure whether broadcast datagram transmissions are allowed
     * <p>
     * This method corresponds to using setsockopt() SO_BROADCAST of real network or
     * BSD sockets. If set on a socket, this option will enable or disable packets
     * to be transmitted to broadcast destination addresses.
     *
     * @param allowBroadcast Whether broadcast is allowed
     * @return true if operation succeeds
     */
    public abstract boolean SetAllowBroadcast(boolean allowBroadcast);

    /**
     * Query whether broadcast datagram transmissions are allowed
     * <p>
     * This method corresponds to using getsockopt() SO_BROADCAST of real network or
     * BSD sockets.
     *
     * @returns true if broadcast is allowed, false otherwise
     */
    public abstract boolean GetAllowBroadcast();

    /**
     * @brief Enable/Disable receive packet information to socket.
     *
     *        For IP_PKTINFO/IP6_PKTINFO. This method is only usable for Raw socket
     *        and Datagram Socket. Not supported for Stream socket.
     *
     *        Method doesn't make distinction between IPv4 and IPv6. If it is
     *        enabled, it is enabled for all types of sockets that supports packet
     *        information
     *
     * @param flag Enable/Disable receive information
     */
    public void SetRecvPktInfo(boolean flag) {
	m_recvPktInfo = flag;
    }

    /**
     * @brief Get status indicating whether enable/disable packet information to
     *        socket
     *
     * @returns True if packet information should be sent to socket
     */
    public boolean IsRecvPktInfo() {
	return m_recvPktInfo;
    }

    /**
     * @brief Manually set the socket priority
     *
     *        This method corresponds to using setsockopt () SO_PRIORITY of real
     *        network or BSD sockets. On Linux, the socket priority can be set to a
     *        value in the range [0..6], unless the user process has the
     *        CAP_NET_ADMIN capability (see the man page for socket). ns-3 allows
     *        users to set the socket priority to any 8-bit non-negative value,
     *        which is equivalent to assuming that the CAP_NET_ADMIN capability is
     *        set.
     *
     * @param priority The socket priority
     */
    public void SetPriority(byte priority) {
	m_priority = priority;
    }

    /**
     * @brief Query the priority value of this socket
     *
     *        This method corresponds to using getsockopt () SO_PRIORITY of real
     *        network or BSD sockets.
     *
     * @return The priority value
     */
    public byte GetPriority() {
	return m_priority;
    }

    /**
     * <h>Return the priority corresponding to a given TOS value</h>
     * <p>
     * This function is implemented after the Linux rt_tos2priority function. The
     * usage of the TOS byte has been originally defined by RFC 1349
     * (http://www.ietf.org/rfc/rfc1349.txt):
     * 
     * <pre>
     *               0     1     2     3     4     5     6     7
     *           +-----+-----+-----+-----+-----+-----+-----+-----+
     *           |   PRECEDENCE    |          TOS          | MBZ |
     *           +-----+-----+-----+-----+-----+-----+-----+-----+
     * </pre>
     * 
     * where MBZ stands for 'must be zero'.
     * <p>
     * The Linux rt_tos2priority function ignores the precedence bits and maps each
     * of the 16 values coded in bits 3-6 as follows:
     * 
     * <pre>
     * Bits 3-6 | Means                   | Linux Priority
     * ---------|-------------------------|----------------
     *     0    |  Normal Service         | Best Effort (0)
     *     1    |  Minimize Monetary Cost | Best Effort (0)
     *     2    |  Maximize Reliability   | Best Effort (0)
     *     3    |  mmc+mr                 | Best Effort (0)
     *     4    |  Maximize Throughput    | Bulk (2)
     *     5    |  mmc+mt                 | Bulk (2)
     *     6    |  mr+mt                  | Bulk (2)
     *     7    |  mmc+mr+mt              | Bulk (2)
     *     8    |  Minimize Delay         | Interactive (6)
     *     9    |  mmc+md                 | Interactive (6)
     *    10    |  mr+md                  | Interactive (6)
     *    11    |  mmc+mr+md              | Interactive (6)
     *    12    |  mt+md                  | Int. Bulk (4)
     *    13    |  mmc+mt+md              | Int. Bulk (4)
     *    14    |  mr+mt+md               | Int. Bulk (4)
     *    15    |  mmc+mr+mt+md           | Int. Bulk (4)
     * </pre>
     * 
     * RFC 2474 (http://www.ietf.org/rfc/rfc2474.txt) redefines the TOS byte:
     * 
     * <pre>
     *               0     1     2     3     4     5     6     7
     *           +-----+-----+-----+-----+-----+-----+-----+-----+
     *           |              DSCP                 |     CU    |
     *           +-----+-----+-----+-----+-----+-----+-----+-----+
     * </pre>
     * 
     * where DSCP is the Differentiated Services Code Point and CU stands for
     * 'currently unused' (actually, RFC 3168 proposes to use these two bits for ECN
     * purposes). The table above allows to determine how the Linux rt_tos2priority
     * function maps each DSCP value to a priority value. Such a mapping is shown
     * below.
     * 
     * <pre>
     * DSCP | Hex  | TOS (binary) | bits 3-6 | Linux Priority
     * -----|------|--------------|----------|----------------
     * EF   | 0x2E |   101110xx   |  12-13   |  Int. Bulk (4)
     * AF11 | 0x0A |   001010xx   |   4-5    |  Bulk (2)
     * AF21 | 0x12 |   010010xx   |   4-5    |  Bulk (2)
     * AF31 | 0x1A |   011010xx   |   4-5    |  Bulk (2)
     * AF41 | 0x22 |   100010xx   |   4-5    |  Bulk (2)
     * AF12 | 0x0C |   001100xx   |   8-9    |  Interactive (6)
     * AF22 | 0x14 |   010100xx   |   8-9    |  Interactive (6)
     * AF32 | 0x1C |   011100xx   |   8-9    |  Interactive (6)
     * AF42 | 0x24 |   100100xx   |   8-9    |  Interactive (6)
     * AF13 | 0x0E |   001110xx   |  12-13   |  Int. Bulk (4)
     * AF23 | 0x16 |   010110xx   |  12-13   |  Int. Bulk (4)
     * AF33 | 0x1E |   011110xx   |  12-13   |  Int. Bulk (4)
     * AF43 | 0x26 |   100110xx   |  12-13   |  Int. Bulk (4)
     * CS0  | 0x00 |   000000xx   |   0-1    |  Best Effort (0)
     * CS1  | 0x08 |   001000xx   |   0-1    |  Best Effort (0)
     * CS2  | 0x10 |   010000xx   |   0-1    |  Best Effort (0)
     * CS3  | 0x18 |   011000xx   |   0-1    |  Best Effort (0)
     * CS4  | 0x20 |   100000xx   |   0-1    |  Best Effort (0)
     * CS5  | 0x28 |   101000xx   |   0-1    |  Best Effort (0)
     * CS6  | 0x30 |   110000xx   |   0-1    |  Best Effort (0)
     * CS7  | 0x38 |   111000xx   |   0-1    |  Best Effort (0)
     * </pre>
     * 
     * @param ipTos the TOS value (in the range 0..255)
     * @return The priority value corresponding to the given TOS value
     */
    public static byte IpTos2Priority(byte ipTos) {
	byte prio = SocketPriority.NS3_PRIO_BESTEFFORT.value();
	ipTos &= 0x1e;
	switch (ipTos >>> 1) {
	case 0:
	case 1:
	case 2:
	case 3:
	    prio = SocketPriority.NS3_PRIO_BESTEFFORT.value();
	    break;
	case 4:
	case 5:
	case 6:
	case 7:
	    prio = SocketPriority.NS3_PRIO_BULK.value();
	    break;
	case 8:
	case 9:
	case 10:
	case 11:
	    prio = SocketPriority.NS3_PRIO_INTERACTIVE.value();
	    break;
	case 12:
	case 13:
	case 14:
	case 15:
	    prio = SocketPriority.NS3_PRIO_INTERACTIVE_BULK.value();
	    break;
	}
	return prio;

    }

    /**
     * <h>Manually set IP Type of Service field</h>
     * <p>
     * This method corresponds to using setsockopt () IP_TOS of real network or BSD
     * sockets. This option is for IPv4 only. Setting the IP TOS also changes the
     * socket priority as stated in the man page.
     *
     * @param ipTos The desired TOS value for IP headers
     */
    public void SetIpTos(byte tos) {
	Address address = new Address();
	// TODO: need to check
	GetSockName(address);
	if (GetSocketType() == SocketType.NS3_SOCK_STREAM) {
	    // preserve the least two significant bits of the current TOS
	    // value, which are used for ECN
	    tos &= 0xfc;
	    tos |= m_ipTos & 0x3;
	}
	m_ipTos = tos;
	m_priority = IpTos2Priority(tos);

    }

    /**
     * @brief Query the value of IP Type of Service of this socket
     *
     *        This method corresponds to using getsockopt () IP_TOS of real network
     *        or BSD sockets.
     *
     * @return The raw IP TOS value
     */
    public byte GetIpTos() {
	return m_ipTos;
    }

    /**
     * @brief Tells a socket to pass information about IP Type of Service up the
     *        stack
     *
     *        This method corresponds to using setsockopt () IP_RECVTOS of real
     *        network or BSD sockets. In our implementation, the socket simply adds
     *        a SocketIpTosTag tag to the packet before passing the packet up the
     *        stack.
     *
     * @param ipv4RecvTos Whether the socket should add SocketIpv4TosTag tag to the
     *                    packet
     */
    public void SetIpRecvTos(boolean ipv4RecvTos) {
	m_ipRecvTos = ipv4RecvTos;
    }

    /**
     * @brief Ask if the socket is currently passing information about IP Type of
     *        Service up the stack
     *
     *        This method corresponds to using getsockopt () IP_RECVTOS of real
     *        network or BSD sockets.
     *
     * @return Whether the IP_RECVTOS is set
     */
    public boolean IsIpRecvTos() {
	return m_ipRecvTos;
    }

    /**
     * @brief Manually set IPv6 Traffic Class field
     *
     *        This method corresponds to using setsockopt () IPV6_TCLASS of real
     *        network or BSD sockets. This option is for IPv6 only. Setting the
     *        IPV6_TCLASSS to -1 clears the option and let the socket uses the
     *        default value.
     *
     * @param ipTclass The desired TCLASS value for IPv6 headers
     */
    public void SetIpv6Tclass(int tclass) {
	Address address = new Address();
	GetSockName(address);

	// If -1 or invalid values, use default
	if (tclass == -1 || tclass < -1 || tclass > 0xff) {
	    // Print a warning
	    if (tclass < -1 || tclass > 0xff) {
		MyLog.logOut("Invalid IPV6_TCLASS value. Using default.", MyLog.DEBUG);
	    }
	    m_manualIpv6Tclass = false;
	    m_ipv6Tclass = 0;
	} else {
	    m_manualIpv6Tclass = true;
	    m_ipv6Tclass = (byte) (tclass & 0xff);
	}
    }

    /**
     * @brief Query the value of IPv6 Traffic Class field of this socket
     *
     *        This method corresponds to using getsockopt () IPV6_TCLASS of real
     *        network or BSD sockets.
     *
     * @return The raw IPV6_TCLASS value
     */
    public byte GetIpv6Tclass() {
	return m_ipv6Tclass;
    }

    /**
     * @brief Tells a socket to pass information about IPv6 Traffic Class up the
     *        stack
     *
     *        This method corresponds to using setsockopt () IPV6_RECVTCLASS of real
     *        network or BSD sockets. In our implementation, the socket simply adds
     *        a SocketIpv6TclasssTag tag to the packet before passing the packet up
     *        the stack.
     *
     * @param ipv6RecvTclass Whether the socket should add SocketIpv6TclassTag tag
     *                       to the packet
     */
    public void SetIpv6RecvTclass(boolean ipv6RecvTclass) {
	m_ipv6RecvTclass = ipv6RecvTclass;
    }

    /**
     * @brief Ask if the socket is currently passing information about IPv6 Traffic
     *        Class up the stack
     *
     *        This method corresponds to using getsockopt () IPV6_RECVTCLASS of real
     *        network or BSD sockets.
     *
     * @return Whether the IPV6_RECVTCLASS is set
     */
    public boolean IsIpv6RecvTclass() {
	return m_ipv6RecvTclass;
    }

    /**
     * @brief Manually set IP Time to Live field
     *
     *        This method corresponds to using setsockopt () IP_TTL of real network
     *        or BSD sockets.
     *
     * @param ipTtl The desired TTL value for IP headers
     */
    public void SetIpTtl(byte ipTtl) {
	m_manualIpTtl = true;
	m_ipTtl = ipTtl;
    }

    /**
     * @brief Query the value of IP Time to Live field of this socket
     *
     *        This method corresponds to using getsockopt () IP_TTL of real network
     *        or BSD sockets.
     *
     * @return The raw IP TTL value
     */
    public byte GetIpTtl() {
	return m_ipTtl;
    }

    /**
     * @brief Tells a socket to pass information about IP_TTL up the stack
     *
     *        This method corresponds to using setsockopt () IP_RECVTTL of real
     *        network or BSD sockets. In our implementation, the socket simply adds
     *        a SocketIpTtlTag tag to the packet before passing the packet up the
     *        stack.
     *
     * @param ipv4RecvTtl Whether the socket should add SocketIpv4TtlTag tag to the
     *                    packet
     */
    public void SetIpRecvTtl(boolean ipv4RecvTtl) {
	m_ipRecvTtl = ipv4RecvTtl;
    }

    /**
     * @brief Ask if the socket is currently passing information about IP_TTL up the
     *        stack
     *
     *        This method corresponds to using getsockopt () IP_RECVTTL of real
     *        network or BSD sockets.
     *
     * @return Whether the IP_RECVTTL is set
     */
    public boolean IsIpRecvTtl() {
	return m_ipRecvTtl;
    }

    /**
     * @brief Manually set IPv6 Hop Limit
     *
     *        This method corresponds to using setsockopt () IPV6_HOPLIMIT of real
     *        network or BSD sockets.
     *
     * @param ipHopLimit The desired Hop Limit value for IPv6 headers
     */
    public void SetIpv6HopLimit(byte ipHopLimit) {
	m_manualIpv6HopLimit = true;
	m_ipv6HopLimit = ipHopLimit;
    }

    /**
     * @brief Query the value of IP Hop Limit field of this socket
     *
     *        This method corresponds to using getsockopt () IPV6_HOPLIMIT of real
     *        network or BSD sockets.
     *
     * @return The raw IPv6 Hop Limit value
     */
    public byte GetIpv6HopLimit() {
	return m_ipv6HopLimit;
    }

    /**
     * @brief Tells a socket to pass information about IPv6 Hop Limit up the stack
     *
     *        This method corresponds to using setsockopt () IPV6_RECVHOPLIMIT of
     *        real network or BSD sockets. In our implementation, the socket simply
     *        adds a SocketIpv6HopLimitTag tag to the packet before passing the
     *        packet up the stack.
     *
     * @param ipv6RecvHopLimit Whether the socket should add SocketIpv6HopLimitTag
     *                         tag to the packet
     */
    public void SetIpv6RecvHopLimit(boolean ipv6RecvHopLimit) {
	m_ipv6RecvHopLimit = ipv6RecvHopLimit;
    }

    /**
     * @brief Ask if the socket is currently passing information about IPv6 Hop
     *        Limit up the stack
     *
     *        This method corresponds to using getsockopt () IPV6_RECVHOPLIMIT of
     *        real network or BSD sockets.
     *
     * @return Whether the IPV6_RECVHOPLIMIT is set
     */
    public boolean IsIpv6RecvHopLimit() {
	return m_ipv6RecvHopLimit;
    }

    /**
     * @brief Joins a IPv6 multicast group.
     *
     *        Based on the filter mode and source addresses this can be interpreted
     *        as a join, leave, or modification to source filtering on a multicast
     *        group.
     *
     *        Mind that a socket can join only one multicast group. Any attempt to
     *        join another group will remove the old one.
     *
     *
     * @param address         Requested multicast address.
     * @param filterMode      Socket filtering mode (INCLUDE | EXCLUDE).
     * @param sourceAddresses All the source addresses on which socket is interested
     *                        or not interested.
     */
    public void Ipv6JoinGroup(Ipv6Address address, Ipv6MulticastFilterMode filterMode,
	    List<Ipv6Address> sourceAddresses) {
	assert false : "Ipv6JoinGroup not implemented on this socket";
    }

    /**
     * @brief Joins a IPv6 multicast group without filters.
     *
     *        A socket can join only one multicast group. Any attempt to join
     *        another group will remove the old one.
     *
     * @param address Group address on which socket wants to join.
     */
    public void Ipv6JoinGroup(Ipv6Address address) {
	// Join Group.
	// Note that joining a group with no sources means joining without source
	// restrictions.
	List<Ipv6Address> sourceAddresses = new ArrayList<>();
	Ipv6JoinGroup(address, Ipv6MulticastFilterMode.EXCLUDE, sourceAddresses);
    }

    /**
     * @brief Leaves IPv6 multicast group this socket is joined to.
     */
    public void Ipv6LeaveGroup() {
	if (m_ipv6MulticastGroupAddress.IsAny()) {
	    MyLog.logInfo(" The socket was not bound to any group.");
	    return;
	}
	// Leave Group. Note that joining a group with no sources means leaving it.
	List<Ipv6Address> sourceAddresses = new ArrayList<>();
	Ipv6JoinGroup(m_ipv6MulticastGroupAddress, Ipv6MulticastFilterMode.INCLUDE, sourceAddresses);
	m_ipv6MulticastGroupAddress = Ipv6Address.GetAny();
    }

    /**
     * @brief Notify through the callback (if set) that the connection has been
     *        established.
     */
    protected void NotifyConnectionSucceeded() {
	if (m_connectionSucceeded != null) {
	    m_connectionSucceeded.callback(this);
	}
    }

    /**
     * @brief Notify through the callback (if set) that the connection has not been
     *        established due to an error.
     */
    protected void NotifyConnectionFailed() {
	if (m_connectionFailed != null) {
	    m_connectionFailed.callback(this);
	}
    }

    /**
     * @brief Notify through the callback (if set) that the connection has been
     *        closed.
     */
    protected void NotifyNormalClose() {
	if (m_normalClose != null) {
	    m_normalClose.callback(this);
	}
    }

    /**
     * @brief Notify through the callback (if set) that the connection has been
     *        closed due to an error.
     */
    protected void NotifyErrorClose() {
	if (m_errorClose != null) {
	    m_errorClose.callback(this);
	}
    }

    /**
     * @brief Notify through the callback (if set) that an incoming connection is
     *        being requested by a remote host.
     *
     *        This function returns true by default (i.e., accept all the incoming
     *        connections). The callback (if set) might restrict this behaviour by
     *        returning zero for a connection that should be refused.
     *
     * @param from the address the connection is incoming from
     * @returns true if the connection must be accepted, false otherwise.
     */
    protected boolean NotifyConnectionRequest(final Address from) {
	if (m_connectionRequest != null) {
	    return m_connectionRequest.callback(this, from);
	} else {
	    // accept all incoming connections by default.
	    // this way people writing code don't have to do anything
	    // special like register a callback that returns true
	    // just to get incoming connections
	    return true;
	}
    }

    /**
     * @brief Notify through the callback (if set) that a new connection has been
     *        created.
     * @param socket The socket receiving the new connection.
     * @param from   The address of the node initiating the connection.
     */
    protected void NotifyNewConnectionCreated(Socket socket, final Address from) {
	if (m_newConnectionCreated != null) {
	    m_newConnectionCreated.callback(socket, from);
	}
    }

    /**
     * @brief Notify through the callback (if set) that some data have been sent.
     *
     * @param size number of sent bytes.
     */
    protected void NotifyDataSent(int size) {
	if (m_dataSent != null) {
	    m_dataSent.callback(this, size);
	}
    }

    /**
     * @brief Notify through the callback (if set) that some data have been sent.
     *
     * @param spaceAvailable the number of bytes available in the transmission
     *                       buffer.
     */
    protected void NotifySend(int spaceAvailable) {
	if (m_sendCb != null) {
	    m_sendCb.callback(this, spaceAvailable);
	}
    }

    /**
     * @brief Notify through the callback (if set) that some data have been
     *        received.
     */
    protected void NotifyDataRecv() {
	if (m_receivedData != null) {
	    m_receivedData.callback(this);
	}
    }

    // inherited function, no doc necessary
    protected void DoDispose() {
	m_connectionSucceeded = null;
	m_connectionFailed = null;
	m_normalClose = null;
	m_errorClose = null;
	m_connectionRequest = null;
	m_newConnectionCreated = null;
	m_dataSent = null;
	m_sendCb = null;
	m_receivedData = null;
    }

    /**
     * @brief Checks if the socket has a specific IPv6 Tclass set
     *
     * @returns true if the socket has a IPv6 Tclass set, false otherwise.
     */
    protected boolean IsManualIpv6Tclass() {
	return m_manualIpv6Tclass;
    }

    /**
     * @brief Checks if the socket has a specific IPv4 TTL set
     *
     * @returns true if the socket has a IPv4 TTL set, false otherwise.
     */
    protected boolean IsManualIpTtl() {
	return m_manualIpTtl;
    }

    /**
     * @brief Checks if the socket has a specific IPv6 Hop Limit set
     *
     * @returns true if the socket has a IPv6 Hop Limit set, false otherwise.
     */
    protected boolean IsManualIpv6HopLimit() {
	return m_manualIpv6HopLimit;
    }

    protected NetDevice m_boundnetdevice; // !< the device this socket is bound to (might be null).
    protected boolean m_recvPktInfo; // !< if the socket should add packet info tags to the packet forwarded to L4.
    protected Ipv6Address m_ipv6MulticastGroupAddress = new Ipv6Address(); // !< IPv6 multicast group address.

    private Callback1<Socket> m_connectionSucceeded = null; // !< connection succeeded callback
    private Callback1<Socket> m_connectionFailed = null; // !< connection failed callback
    private Callback1<Socket> m_normalClose = null; // !< connection closed callback
    private Callback1<Socket> m_errorClose = null; // !< connection closed due to errors callback
    private CallbackR2<Boolean, Socket, Address> m_connectionRequest = null; // !< connection request callback
    private Callback2<Socket, Address> m_newConnectionCreated; // !< connection created callback
    private Callback2<Socket, Integer> m_dataSent = null; // !< data sent callback
    private Callback2<Socket, Integer> m_sendCb = null; // !< packet sent callback
    private Callback1<Socket> m_receivedData = null; // !< data received callback

    private byte m_priority; // !< the socket priority

    // IPv4 options
    private boolean m_manualIpTtl; // !< socket has IPv4 TTL set
    private boolean m_ipRecvTos; // !< socket forwards IPv4 TOS tag to L4
    private boolean m_ipRecvTtl; // !< socket forwards IPv4 TTL tag to L4

    private byte m_ipTos; // !< the socket IPv4 TOS
    private byte m_ipTtl; // !< the socket IPv4 TTL

    // IPv6 options
    private boolean m_manualIpv6Tclass; // !< socket has IPv6 Tclass set
    private boolean m_manualIpv6HopLimit; // !< socket has IPv6 Hop Limit set
    private boolean m_ipv6RecvTclass; // !< socket forwards IPv6 Tclass tag to L4
    private boolean m_ipv6RecvHopLimit; // !< socket forwards IPv6 Hop Limit tag to L4

    private byte m_ipv6Tclass; // !< the socket IPv6 Tclass
    private byte m_ipv6HopLimit; // !< the socket IPv6 Hop Limit
}
