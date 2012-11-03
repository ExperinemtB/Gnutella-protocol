package gnutella.message;

public class Header {

	public static final int HEADER_LENGTH = 23;

	public static final byte PING = (byte) 0x00;
	public static final byte PONG = (byte) 0x01;
	public static final byte PUSH = (byte) 0x40;
	public static final byte QUERY = (byte) 0x80;
	public static final byte QUERYHIT = (byte) 0x81;

	private GUID guid;
	private int ttl;
	private int hops;
	private byte payloadDescriptor;
	private int payloadLength;
	
	public Header(){
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
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public int getHops() {
		return hops;
	}
	public void setHops(int hops) {
		this.hops = hops;
	}
	public int getPayloadLength() {
		return payloadLength;
	}
	public void setPayloadLength(int payloadLength) {
		this.payloadLength = payloadLength;
	}
	
	public byte[] getBytes(){
		return new byte[0];
	}
	
	public static Header parse(byte[] data){
		if(data.length<HEADER_LENGTH){
			return null;
		}
		
		return new Header();
	}
}
