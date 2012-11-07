package gnutella.message;

public class PingMessage extends Message {
	public PingMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);
	}

	public PingMessage(Header header) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.setHeader(header);
		this.setPayload(new byte[0]);
	}

	@Override
	public byte[] getBytes() {
		return super.getBytes();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + super.toString();
	}
}
