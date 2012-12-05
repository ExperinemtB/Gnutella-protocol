package gnutella.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class QueryHitMessage extends Message {
	public static final int MIN_LENGTH = 27;

	private byte numberofHits;
	private char port;
	private InetAddress ipAddress;
	private int speed;
	private ResultSet resultSet;
	private GUID serventIdentifier;

	public QueryHitMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);

		ByteBuffer bf = ByteBuffer.wrap(payload);
		this.numberofHits = bf.get();
		this.port = bf.getChar();

		byte[] ipBytes = new byte[4];
		bf.get(ipBytes);
		try {
			this.ipAddress = InetAddress.getByAddress(ipBytes);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.speed = bf.getInt();
		this.resultSet = new ResultSet();
		for (int i = 0; i < this.numberofHits; i++) {
			int resultSetFileIndex = bf.getInt();
			int resultSetFileSize = bf.getInt();
			ByteBuffer resultSetFileNameBuffer = ByteBuffer.allocateDirect(bf.capacity() - bf.position());

			byte prevByte = -1;
			while (bf.capacity() - bf.position() > 16) {
				byte currentByte = bf.get();
				if (prevByte == 0x00 && currentByte == 0x00) {
					break;
				}
				if (currentByte != 0x00) {
					resultSetFileNameBuffer.put(currentByte);
				}
				prevByte = currentByte;
			}
			resultSetFileNameBuffer.flip();
			byte[] fileNameBytes = new byte[resultSetFileNameBuffer.limit()];
			resultSetFileNameBuffer.get(fileNameBytes);
			String resultSetFileName = new String(fileNameBytes);
			this.resultSet.add(new ResultSetContent(resultSetFileIndex, resultSetFileSize, resultSetFileName));
		}
		byte[] guidBytes = new byte[16];
		bf.get(guidBytes);
		this.serventIdentifier = new GUID(guidBytes);
	}

	public QueryHitMessage(Header header, byte numberofHits, char port, InetAddress ipAddress, int speed, ResultSet resultSet, GUID serventIdentifier) {
		this.setHeader(header);
		this.numberofHits = numberofHits;
		this.port = port;
		this.ipAddress = ipAddress;
		this.speed = speed;
		this.resultSet = resultSet;
		this.serventIdentifier = serventIdentifier;
	}

	@Override
	public byte[] getBytes() {
		int resultSetContentBytesLength = 0;
		List<byte[]> resultSetContentBytesList = new ArrayList<byte[]>();
		for (int i = 0; i < this.numberofHits; i++) {
			byte[] resultSetBytes = this.resultSet.getByFileIndex(i).getBytes();
			resultSetContentBytesLength += resultSetBytes.length;
			resultSetContentBytesList.add(resultSetBytes);
		}

		ByteBuffer bf = ByteBuffer.allocateDirect(QueryHitMessage.MIN_LENGTH + resultSetContentBytesLength);
		bf.put(this.numberofHits);
		bf.putShort((short) this.port);
		bf.put(this.ipAddress.getAddress());
		bf.putInt(this.speed);
		for (byte[] contentBytes : resultSetContentBytesList) {
			bf.put(contentBytes);
		}
		bf.put(this.serventIdentifier.getGuid());
		bf.flip();

		byte[] result = new byte[bf.limit()];
		bf.get(result);

		this.setPayload(result);
		return super.getBytes();
	}

	public byte getNumberofHits() {
		return numberofHits;
	}

	public int getPort() {
		return port;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public int getSpeed() {
		return speed;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public GUID getServentIdentifier() {
		return serventIdentifier;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+ "{" + super.toString() + " " + String.format("numberofHits:%d port:%d ipAddress:%s speed:%d resultSet:%s serventIdentifier:%s", (int) this.numberofHits, (int) this.port, this.ipAddress.getHostAddress(), this.speed, this.resultSet.toString(), serventIdentifier.toString()) + "}";
	}

}
