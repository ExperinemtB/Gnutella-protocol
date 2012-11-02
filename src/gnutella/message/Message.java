package gnutella.message;

public abstract class Message {	
	private Header header;
	private byte[] payload;
	
	public Message(){
		this.header = new Header();
	}
	
	public byte[] getBytes(){
		byte[] headerByteArray = header.getBytes();
		int headerLength = headerByteArray.length;
		byte[] payloadByteArray = getBytes();
		int payloadLength = payloadByteArray.length;
		
		byte[] result = new byte[headerLength+payloadLength];
		System.arraycopy(headerByteArray, 0,result, 0, headerLength);
		System.arraycopy(payloadByteArray, 0, result, headerLength, payloadLength);
		return result;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
}
