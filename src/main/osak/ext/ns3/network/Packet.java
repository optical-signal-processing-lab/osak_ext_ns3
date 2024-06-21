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

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Packet
 * <p>
 * 与ns3中不同，但是接口保持一样
 * 
 * @author zhangrui
 * @since 1.0
 */
public class Packet {
    /**
     * Constructor
     * 
     * @param buffer        the packet buffer
     * @param byteTagList   the ByteTag list
     * @param packetTagList the packet's Tag list
     * @param metadata      the packet's metadata
     */
    @SuppressWarnings("unused")
    private Packet(ByteBuffer buffer, final ByteTagList byteTagList, final PacketTagList packetTagList,
	    final PacketMetadata metadata) {
	m_buffer = buffer;
	m_byteTagList = byteTagList;
	m_packetTagList = packetTagList;
	m_metadata = metadata;
	m_nixVector = null;
    }

    /**
     * Deserializes a packet.
     * 
     * @param [in] buffer the input buffer.
     * @param [in] size the buffer size.
     * @returns the number of deserialized bytes.
     */
    private int Deserialize(final byte[] buffer, int size) {
	// TODO: no need to do;
	m_bufferList.clear();
	m_size = 0;
	m_bufferList.add(buffer);
	m_size += buffer.length;
	return 0;

    }

    private ByteBuffer m_buffer = ByteBuffer.allocate(128);// !< the packet buffer (it's actual contents)
    private LinkedList<byte[]> m_bufferList = new LinkedList<>();
    private ByteTagList m_byteTagList = new ByteTagList(); // !< the ByteTag list
    private PacketTagList m_packetTagList = new PacketTagList(); // !< the packet's Tag list
    private PacketMetadata m_metadata = new PacketMetadata(); // !< the packet's metadata
    private int m_size = 0;

    /* Please see comments above about nix-vector */
    private NixVector m_nixVector; // !< the packet's Nix vector

    private UUID m_id;
    private static int m_globalUid; // !< Global counter of packets Uid

    /**
     * Returns the packet's Uid.
     *
     * A packet is allocated a new uid when it is created empty or with zero-filled
     * payload.
     *
     * Note: This uid is an internal uid and cannot be counted on to provide an
     * accurate counter of how many "simulated packets" of a particular protocol are
     * in the system. It is not trivial to make this uid into such a counter,
     * because of questions such as what should the uid be when the packet is sent
     * over broadcast media, or when fragmentation occurs. If a user wants to trace
     * actual packet counts, he or she should look at e.g. the IP ID field or
     * transport sequence numbers, or other packet or frame counters at other
     * protocol layers.
     *
     * @returns an integer identifier which uniquely identifies this packet.
     */
    public UUID GetUid() {
	return m_id;
    }

    /**
     * Create an empty packet with a new uid (as returned by getUid).
     */
    public Packet() {
	m_id = UUID.nameUUIDFromBytes(m_buffer.array());
	m_nixVector = null;
	m_globalUid++;
    }

    /**
     * Copy constructor
     * 
     * @param o object to copy
     */
    public Packet(final Packet o) {
	m_size = 0;
	for (byte[] buffer : o.m_bufferList) {
	    byte[] newBuffer = new byte[buffer.length];
	    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
	    m_size += buffer.length;
	    m_bufferList.addFirst(newBuffer);
	}
	m_buffer = o.m_buffer;
	m_byteTagList = o.m_byteTagList;
	m_packetTagList = o.m_packetTagList;
	m_metadata = o.m_metadata;
	if (o.m_nixVector != null) {
	    m_nixVector = o.m_nixVector.Copy();
	} else {
	    m_nixVector = null;
	}
    }

    /**
     * Create a packet with a zero-filled payload.
     *
     * The memory necessary for the payload is not allocated: it will be allocated
     * at any later point if you attempt to fragment this packet or to access the
     * zero-filled bytes. The packet is allocated with a new uid (as returned by
     * getUid).
     *
     * @param size the size of the zero-filled payload
     */
    public Packet(int size) {
	byte[] buffer = new byte[size];
	Arrays.fill(buffer, (byte) 0);
	m_bufferList.addFirst(buffer);
	m_size += size;
	m_nixVector = null;
	m_globalUid++;
    }

    /**
     *  Create a new packet from the serialized buffer.
     *
     * This new packet
     * is identical to the serialized packet contained in the buffer
     * and is magically deserialized for you
     *
     * @param buffer the serialized packet to be created
     * @param size the size of the packet for deserialization
     * @param magic allows packet deserialization;
     *        asserts when set to false
     */
    public Packet(final byte[] buffer, int size, boolean magic) {
	// TODO:need to check;
	// m_buffer(0, false),
	m_nixVector = null;
	assert (magic);
	Deserialize(buffer, size);
    }

    /**
     *  Create a packet with payload filled with the content
     * of this buffer.
     *
     * The input data is copied: the input
     * buffer is untouched.
     *
     * @param buffer the data to store in the packet.
     * @param size the size of the input buffer.
     */
    public Packet(final byte[] buffer, int size) {
	m_globalUid++;
	m_bufferList.clear();
	m_size = 0;
	m_bufferList.addFirst(buffer);
	m_size += buffer.length;
    }

    /**
     *  Create a new packet which contains a fragment of the original
     * packet.
     *
     * The returned packet shares the same uid as this packet.
     *
     * @param start offset from start of packet to start of fragment to create
     * @param length length of fragment to create
     * @returns a fragment of the original packet
     */
    // TODO:
    public Packet CreateFragment(int start, int length) {
	return null;
    }

    /**
     *  Returns the the size in bytes of the packet (including the zero-filled
     * initial payload).
     *
     * @returns the size in bytes of the packet
     */
    public int GetSize() {
	return m_size;
    }

    /**
     * Add header to this packet.
     *
     * This method invokes the Header::GetSerializedSize and Header::Serialize
     * methods to reserve space in the buffer and request the header to serialize
     * itself in the packet buffer.
     *
     * @param header a reference to the header to add to this packet.
     */
    // TODO: check
    public void AddHeader(Header header) {
	// put data to m_buffer
	int size = header.GetSerializedSize();
	ByteBuffer buffer = ByteBuffer.allocate(size);
	header.Serialize(buffer);
	buffer.flip();
	m_bufferList.addFirst(buffer.array());
	m_size += size;

    }

    private void mergeList() {
	if(m_bufferList.size()>1) {
	    byte[] nb = new byte[m_size];
	    int start = 0;
	    for(byte[] buffer:m_bufferList) {
		System.arraycopy(buffer, 0, nb, start, buffer.length);
		start += buffer.length;
	    }
	    m_bufferList.clear();
	    m_bufferList.add(nb);
	} else {
	    return;
	}
    }

    /**
     *  Deserialize and remove the header from the internal buffer.
     *
     * This method invokes Header::Deserialize (begin) and should be used for
     * fixed-length headers.
     *
     * @param header a reference to the header to remove from the internal buffer.
     * @returns the number of bytes removed from the packet.
     */
    // TODO:涉及到元数据、Tag等元素的添加和删除
    public int RemoveHeader(Header header) {
	mergeList();
	ByteBuffer buffer = ByteBuffer.wrap(m_bufferList.get(0));
	int deserialized = header.Deserialize(buffer);
	m_size -= deserialized;
	byte[] nb = new byte[m_size];
	System.arraycopy(m_bufferList.getFirst(), deserialized, nb, 0, m_size);
	m_bufferList.set(0, nb);
	return deserialized;
    }

    /**
     *  Deserialize and remove the header from the internal buffer.
     *
     * This method invokes Header::Deserialize (begin, end) and should be
     * used for variable-length headers (where the size is determined somehow
     * by the caller).
     *
     * @param header a reference to the header to remove from the internal buffer.
     * @param size number of bytes to deserialize
     * @returns the number of bytes removed from the packet.
     */
    // TODO: 没有这个用法
    public int RemoveHeader(Header header, int size) {
	int deserialized = header.Deserialize(m_buffer);
	return deserialized;
    }

    /**
     *  Deserialize but does _not_ remove the header from the internal buffer.
     * s
     * This method invokes Header::Deserialize.
     *
     * @param header a reference to the header to read from the internal buffer.
     * @returns the number of bytes read from the packet.
     */
    public int PeekHeader(Header header) {
	ByteBuffer buffer = ByteBuffer.allocate(m_size);
	if (m_bufferList.size() > 1) {
	    for (int i = 0; i < m_bufferList.size(); i++) {
		buffer.put(m_bufferList.get(i));
	    }
	} else {
	    buffer.put(m_bufferList.get(0));
	}
	buffer.flip();

	int deserialized = header.Deserialize(m_buffer);
	return deserialized;
    }

    /**
     *  Deserialize but does _not_ remove the header from the internal buffer.
     * s
     * This method invokes Header::Deserialize (begin, end) and should be used
     * for variable-length headers (where the size is determined somehow
     * by the caller).
     *
     * @param header a reference to the header to read from the internal buffer.
     * @param size number of bytes to deserialize
     * @returns the number of bytes read from the packet.
     */
    public int PeekHeader(Header header, int size) {
	return PeekHeader(header);
    }

    /**
     *  Add trailer to this packet.
     *
     * This method invokes the
     * Trailer::GetSerializedSize and Trailer::Serialize
     * methods to reserve space in the buffer and request the trailer
     * to serialize itself in the packet buffer.
     *
     * @param trailer a reference to the trailer to add to this packet.
     */
    public void AddTrailer(final Trailer trailer) {
	@SuppressWarnings("unused")
	int size = trailer.GetSerializedSize();
	trailer.Serialize(m_buffer);
    }

    /**
     *  Remove a deserialized trailer from the internal buffer.
     *
     * This method invokes the Deserialize method.
     *
     * @param trailer a reference to the trailer to remove from the internal buffer.
     * @returns the number of bytes removed from the end of the packet.
     */
    public int RemoveTrailer(Trailer trailer) {
	int size = trailer.GetSerializedSize();
	trailer.Serialize(m_buffer);
	return size;
    }

    /**
     *  Deserialize but does _not_ remove a trailer from the internal buffer.
     *
     * This method invokes the Trailer::Deserialize method.
     *
     * @param trailer a reference to the trailer to read from the internal buffer.
     * @returns the number of bytes read from the end of the packet.
     */
    public int PeekTrailer(Trailer trailer) {
	int size = trailer.GetSerializedSize();
	trailer.Serialize(m_buffer);
	return size;
    }

    /**
     *  Concatenate the input packet at the end of the current
     * packet.
     *
     * This does not alter the uid of either packet.
     *
     * @param packet packet to concatenate
     */
    // TODO:not used
    public void AddAtEnd(final Packet packet) {

    }

    /**
     * Add a zero-filled padding to the packet.
     *
     * @param size number of padding bytes to add.
     */
    public void AddPaddingAtEnd(int size) {
	byte padding[] = new byte[size];
	for (int i = 0; i < size; i++) {
	    padding[i] = 0;
	}
	m_buffer.put(padding);
    }

    /**
     * Remove size bytes from the end of the current packet.
     *
     * It is safe to remove more bytes than are present in the packet.
     *
     * @param size number of bytes from remove
     */
    public void RemoveAtEnd(int size) {
	byte end[] = new byte[size];
	m_buffer.get(end, 0, size);
    }

    /**
     * Remove size bytes from the start of the current packet.
     *
     * It is safe to remove more bytes than are present in the packet.
     *
     * @param size number of bytes from remove
     */
    public void RemoveAtStart(int size) {
	// TODO:
    }

    /**
     *  Copy the packet contents to a byte buffer.
     *
     * @param buffer a pointer to a byte buffer where the packet data
     *        should be copied.
     * @param size the size of the byte buffer.
     * @returns the number of bytes read from the packet
     *
     * No more than \b size bytes will be copied by this function.
     */
    public int CopyData(byte[] buffer, int size) {
	m_bufferList.clear();
	m_size = 0;
	m_bufferList.add(buffer);
	m_size += buffer.length;
	return buffer.length;
    }

    /**
     *  Copy the packet contents to an output stream.
     *
     * @param os pointer to output stream in which we want
     *        to write the packet data.
     * @param size the maximum number of bytes we want to write
     *        in the output stream.
     */
    // TODO: std::ostream
    public void CopyData(OutputStream os, int size) {

    }

    /**
     *  performs a COW copy of the packet.
     *
     * @returns a COW copy of the packet.
     *
     * The returns packet will behave like an independent copy of
     * the original packet, even though they both share the
     * same datasets internally.
     */
    public Packet Copy() {
	// we need to invoke the copy constructor directly
	// rather than calling Create because the copy constructor
	// is private.
	// TODO: 明明是public，而且构造得不一样
	return new Packet(this);
    }

    /**
     *  Print the packet contents.
     *
     * @param os output stream in which the data should be printed.
     *
     * Iterate over the headers and trailers present in this packet,
     * from the first header to the last trailer and invoke, for
     * each of them, the user-provided method Header::DoPrint or
     * Trailer::DoPrint methods.
     */
    // TODO:std::ostream
    public void Print() {
	mergeList();
	System.out.println(Arrays.toString(m_bufferList.getFirst()));
    }

    /**
     *  Return a string representation of the packet
     *
     * An empty string is returned if you haven't called EnablePrinting ()
     *
     * @return String representation
     */
    // TODO:
    public String ToString() {
	return "";
    }

    /**
     *  Returns an iterator which points to the first 'item'
     * stored in this buffer.
     *
     * Note that this iterator will point
     * to an empty array of items if you don't call EnablePrinting
     * or EnableChecking before.
     *
     * @returns an iterator
     *
     * \sa EnablePrinting EnableChecking
     */
    public PacketMetadata.ItemIterator BeginItem() {
	// TODO:
	return null;
    }

    /**
     * Enable printing packets metadata.
     *
     * By default, packets do not keep around enough metadata to perform the
     * operations requested by the Print methods. If you want to be able the
     * Packet::Print method, you need to invoke this method at least once during the
     * simulation setup and before any packet is created.
     */
    public static void EnablePrinting() {
	// TODO:PacketMetadata::Enable();
    }

    /**
     * Enable packets metadata checking.
     *
     * The packet metadata is also used to perform extensive sanity checks at
     * runtime when performing operations on a Packet. For example, this metadata is
     * used to verify that when you remove a header from a packet, this same header
     * was actually present at the front of the packet. These errors will be
     * detected and will abort the program.
     */
    public static void EnableChecking() {
	// TODO: PacketMetadata::EnableChecking();
    }

    /**
     *  Returns number of bytes required for packet
     * serialization.
     *
     * @returns number of bytes required for packet
     * serialization
     *
     * For packet serialization, the total size is checked
     * in order to determine the size of the buffer
     * required for serialization
     */
    public int GetSerializedSize() {
	int size = 0;
	if (m_nixVector != null) {
	    // increment total size by the size of the nix-vector
	    // ensuring 4-byte boundary
	    size += ((m_nixVector.GetSerializedSize() + 3) & (~3));

	    // add 4-bytes for entry of total length of nix-vector
	    size += 4;
	}
	else {
	    // if no nix-vector, still have to add 4-bytes
	    // to account for the entry of total size for
	    // nix-vector in the buffer
	    size += 4;
	}
	// increment total size by size of packet tag list
	// ensuring 4-byte boundary
	size += ((m_packetTagList.GetSerializedSize() + 3) & (~3));

	// add 4-bytes for entry of total length of packet tag list
	size += 4;

	// increment total size by size of byte tag list
	// ensuring 4-byte boundary
	size += ((m_byteTagList.GetSerializedSize() + 3) & (~3));

	// add 4-bytes for entry of total length of byte tag list
	size += 4;

	// increment total size by size of meta-data
	// ensuring 4-byte boundary
	size += ((m_metadata.GetSerializedSize() + 3) & (~3));

	// add 4-bytes for entry of total length of meta-data
	size += 4;

	// increment total size by size of buffer
	// ensuring 4-byte boundary
	// TODO:need to check;
	size += ((m_buffer.remaining() + 3) & (~3));

	// add 4-bytes for entry of total length of buffer
	size += 4;

	return size;
    }

    /**
     *  Serialize a packet, tags, and metadata into a byte buffer.
     *
     * @param buffer a raw byte buffer to which the packet will be serialized
     * @param maxSize the max size of the buffer for bounds checking
     *
     * @returns one if all data were serialized, zero if buffer size was too small.
     */
    public int Serialize(byte[] buffer, int maxSize) {
	mergeList();
	System.arraycopy(m_bufferList.getFirst(), 0, buffer, 0, m_size);
	return 0;

    }

    /**
     *  Tag each byte included in this packet with a new byte tag.
     *
     * @param tag the new tag to add to this packet
     *
     * Note that adding a tag is a const operation which is pretty
     * un-intuitive. The rationale is that the content and behavior of
     * a packet is _not_ changed when a tag is added to a packet: any
     * code which was not aware of the new tag is going to work just
     * the same if the new tag is added. The real reason why adding a
     * tag was made a const operation is to allow a trace sink which gets
     * a packet to tag the packet, even if the packet is const (and most
     * trace sources should use const packets because it would be
     * totally evil to allow a trace sink to modify the content of a
     * packet).
     */
    public void AddByteTag(final Tag tag) {
	// TODO:

    }

    /**
     *  Tag the indicated byte range of this packet with a new byte tag.
     *
     * As parameters for this method, we do not use indexes, but byte position.
     * Moreover, as there is no 0-th position, the first position is 1.
     *
     * As example, if you want to tag the first 10 bytes, you have to call
     * the method in this way:
     *
     * @code{.cpp}
         Ptr<Packet> p = ... ;
         SomeTag tag;
         p->AddByteTag (tag, 1, 10);
       @endcode
     *
     * @param tag the new tag to add to this packet
     * @param start the position of the first byte tagged by this tag
     * @param end the position of the last byte tagged by this tag
     */
    public void AddByteTag(final Tag tag, int start, int end) {
	// TODO:

    }

    /**
     *  Returns an iterator over the set of byte tags included in this packet
     *
     * @returns an iterator over the set of byte tags included in this packet.
     */
    public ByteTagIterator GetByteTagIterator() {
	// TODO:
	return null;
    }

    /**
     *  Finds the first tag matching the parameter Tag type
     *
     * @param tag the byte tag type to search in this packet
     * @returns true if the requested tag type was found, false otherwise.
     *
     * If the requested tag type is found, it is copied in the user's
     * provided tag instance.
     */
    public boolean FindFirstMatchingByteTag(Tag tag) {
	// TODO:
	return false;
    }

    /**
     * Remove all byte tags stored in this packet.
     */
    public void RemoveAllByteTags() {
	// TODO:

    }

    /**
     * @param os output stream in which the data should be printed.
     *
     *  Iterate over the byte tags present in this packet, and
     * invoke the Print method of each tag stored in the packet.
     */
    // TODO:std::ostream
    public void PrintByteTags(OutputStream os) {
	// TODO:

    }

    /**
     *  Add a packet tag.
     *
     * @param tag the packet tag type to add.
     *
     * Note that this method is const, that is, it does not
     * modify the state of this packet, which is fairly
     * un-intuitive.  See AddByteTag"()" discussion.
     */
    public void AddPacketTag(final Tag tag) {
	// TODO:
	
    }

    /**
     *  Remove a packet tag.
     *
     * @param tag the packet tag type to remove from this packet.
     *        The tag parameter is set to the value of the tag found.
     * @returns true if the requested tag is found, false
     *          otherwise.
     */
    public boolean RemovePacketTag(Tag tag) {
	// TODO:
	return false;
    }

    /**
     *  Replace the value of a packet tag.
     *
     * @param tag the packet tag type to replace.  To get the old
     *        value of the tag, use PeekPacketTag first.
     * @returns true if the requested tag is found, false otherwise.
     *        If the tag isn't found, Add is performed instead (so
     *        the packet is guaranteed to have the new tag value
     *        either way).
     */
    public boolean ReplacePacketTag(Tag tag) {
	// TODO:
	return false;
    }

    /**
     *  Search a matching tag and call Tag::Deserialize if it is found.
     *
     * @param tag the tag to search in this packet
     * @returns true if the requested tag is found, false
     *          otherwise.
     */
    public boolean PeekPacketTag(Tag tag) {
	// TODO:
	return false;
    }

    /**
     *  Remove all packet tags.
     */
    public void RemoveAllPacketTags() {
	// TODO:
	
    }

    /**
     *  Print the list of packet tags.
     *
     * @param os the stream on which to print the tags.
     *
     * @sa Packet::AddPacketTag, Packet::RemovePacketTag, Packet::PeekPacketTag,
     *  Packet::RemoveAllPacketTags
     */
    // TODO:std::ostream
    public void PrintPacketTags(OutputStream os) {
	// TODO:
	
    }

    /**
     *  Returns an object which can be used to iterate over the list of
     *  packet tags.
     *
     * @returns an object which can be used to iterate over the list of
     *  packet tags.
     */
    public PacketTagIterator GetPacketTagIterator() {
	// TODO:
	return null;
    }

    /**
     *  Set the packet nix-vector.
     *
     * @note Note: This function supports a temporary solution
     * to a specific problem in this generic class, i.e.
     * how to associate something specific like nix-vector
     * with a packet.  This design methodology
     * should _not_ be followed, and is only here as an
     * impetus to fix this general issue.
     *
     * @warning For real this function is not const, as it is the
     * setter for a mutable variable member. The const qualifier
     * is needed to set a private mutable variable of const objects.
     *
     * @param nixVector the nix vector
     */
    public void SetNixVector(NixVector nixVector) {
	m_nixVector = nixVector;
    }

    /**
     *  Get the packet nix-vector.
     *
     * See the comment on SetNixVector
     *
     * @returns the Nix vector
     */
    public NixVector GetNixVector() {
	return m_nixVector;
    }

    /**
     * TracedCallback signature for Ptr<Packet>
     *
     * @param [in] packet The packet.
     */
    // typedef void (*TracedCallback)(Ptr<const Packet> packet);

    /**
     * TracedCallback signature for packet and Address.
     *
     * @param [in] packet The packet.
     * @param [in] address The address.
     */
    // typedef void (*AddressTracedCallback)(Ptr<const Packet> packet, const Address& address);

    /**
     * TracedCallback signature for packet and source/destination addresses.
     *
     * @param [in] packet The packet.
     * @param [in] srcAddress The source address.
     * @param [in] destAddress The destination address.
     */
    
    /*
    typedef void (*TwoAddressTracedCallback)(const Ptr<const Packet> packet,
                                             const Address& srcAddress,
                                             const Address& destAddress);
     */
    
    /**
     * TracedCallback signature for packet and Mac48Address.
     *
     * @param [in] packet The packet.
     * @param [in] mac The Mac48Address.
     */
    // typedef void (*Mac48AddressTracedCallback)(Ptr<const Packet> packet, Mac48Address mac);

    /**
     * TracedCallback signature for changes in packet size.
     *
     * @param [in] oldSize The previous packet's size.
     * @param [in] newSize The actual packet's size.
     */
    // typedef void (*SizeTracedCallback)(int oldSize, int newSize);

    /**
     * TracedCallback signature for packet and SINR.
     *
     * @param [in] packet The packet.
     * @param [in] sinr The received SINR.
     */
    // typedef void (*SinrTracedCallback)(Ptr<const Packet> packet, double sinr);
}
