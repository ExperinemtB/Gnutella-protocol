package gnutella.message;

import java.nio.ByteBuffer;

public class QueryMessage extends Message {
	public final static int MIN_LENGTH = 2;

	private int minimumSpeedKb;
	private String searchCriteria;

	public QueryMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);

		ByteBuffer bf = ByteBuffer.wrap(payload);
		this.minimumSpeedKb = bf.getShort();
		byte[] searchCriteriaBytes = new byte[bf.limit() - bf.position() - 1];
		bf.get(searchCriteriaBytes);
		this.searchCriteria = new String(searchCriteriaBytes);
	}

	public QueryMessage(Header header, int minimumSpeedKb, String searchCriteria) {
		this.setHeader(header);
		this.minimumSpeedKb = minimumSpeedKb;
		this.searchCriteria = searchCriteria;
	}

	public int getMinimumSpeedKb() {
		return minimumSpeedKb;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(QueryMessage.MIN_LENGTH + this.searchCriteria.getBytes().length + 1);
		bf.putShort((short) this.minimumSpeedKb);
		bf.put(searchCriteria.getBytes());
		bf.put(new byte[] { 0x00 });
		bf.flip();

		byte[] result = new byte[bf.limit()];
		bf.get(result);

		this.setPayload(result);
		return super.getBytes();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + super.toString() + " " + String.format("minimumSpeedKb:%d searchCriteria:%s", this.minimumSpeedKb, this.searchCriteria) + "}";
	}

}
