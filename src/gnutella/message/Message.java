package gnutella.message;

public class Message {
	private Header header;
	private Body body;
	
	public byte[] getBytes(){
		return null;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public static Message parse(byte[] byteBuffer) {
		return new Message();
	}

}
