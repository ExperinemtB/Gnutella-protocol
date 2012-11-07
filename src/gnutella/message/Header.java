package gnutella.message;

import java.nio.ByteBuffer;

public class Header {

	public static final int HEADER_LENGTH = 23;

	public static final byte PING = (byte) 0x00;
	public static final byte PONG = (byte) 0x01;
	public static final byte PUSH = (byte) 0x40;
	public static final byte QUERY = (byte) 0x80;
	public static final byte QUERYHIT = (byte) 0x81;

	private GUID guid;
	private byte payloadDescriptor;
	private byte ttl;
	private byte hops;
	private int payloadLength;

	public Header(byte payload, byte ttl, int payloadLength) {
		this(new GUID(), payload, ttl, (byte) 0, payloadLength);
	}

	public Header(GUID guid, byte payloadDescriptor, byte ttl, int payloadLength) {
		this(guid, payloadDescriptor, ttl, (byte) 0, payloadLength);
	}

	public Header(GUID guid, byte payloadDescriptor, byte ttl, byte hops, int payloadLength) {
		this.guid = guid;
		this.payloadDescriptor = payloadDescriptor;
		this.ttl = ttl;
		this.hops = hops;
		this.payloadLength = payloadLength;
	}

	public GUID getGuid() {
		return guid;
	}

	public void setGuid(GUID guid) {
		this.guid = guid;
	}

	public byte getPayloadDescriptor() {
		return payloadDescriptor;
	}

	public void setPayloadDescriptor(byte payloadDescriptor) {
		this.payloadDescriptor = payloadDescriptor;
	}

	public byte getTtl() {
		return ttl;
	}

	public void setTtl(byte ttl) {
		this.ttl = ttl;
	}

	public byte getHops() {
		return hops;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public void setPayloadLength(int payloadLength) {
		this.payloadLength = payloadLength;
	}

	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(Header.HEADER_LENGTH);

		bf.put(guid.getGuid());
		bf.put(payloadDescriptor);
		bf.put(ttl);
		bf.put(hops);
		bf.putInt(payloadLength);

		bf.flip();
		byte[] headerBytes = new byte[Header.HEADER_LENGTH];
		bf.get(headerBytes);
		return headerBytes;
	}

	public static Header parse(byte[] data) {
		if (data.length < HEADER_LENGTH) {
			throw new IllegalArgumentException("Size of header should be " + String.valueOf(HEADER_LENGTH) + " byte");
		}
		ByteBuffer buffer = ByteBuffer.wrap(data);
		byte[] guid = new byte[GUID.LENGTH];
		buffer.get(guid);
		byte payload = buffer.get();
		byte ttl = buffer.get();
		byte hops = buffer.get();
		int payloadLength = buffer.getInt();
		return new Header(new GUID(guid), payload, ttl, hops, payloadLength);
	}

	@Override
	public String toString() {
		return String.format("{GUID:%s payloadDescriptor:%d ttl:%d hops:%d pauloadLength:%d}", new String(guid.getGuid()), payloadDescriptor, ttl, hops, payloadLength);
	}

}
