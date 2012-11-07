package gnutella.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class PongMessage extends Message {
	public static final int LENGTH = 14;
	private InetAddress ipAddress;
	private char port;
	private int numberOfFilesShared;
	private int numberOfKilobytesShared;

	public PongMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);

		byte[] data = new byte[2];
		data[0] = payload[0];
		data[1] = payload[1];
		ByteBuffer wb0 = ByteBuffer.wrap(data);
		this.port = wb0.getChar();

		data = new byte[4];
		data[0] = payload[5];
		data[1] = payload[4];
		data[2] = payload[3];
		data[3] = payload[2];
		try {
			this.ipAddress = InetAddress.getByAddress(data);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 4; i++) {
			data[i] = payload[6 + i];
		}
		ByteBuffer wb2 = ByteBuffer.wrap(data);
		this.numberOfFilesShared = wb2.getInt();

		for (int i = 0; i < 4; i++) {
			data[i] = payload[10 + i];
		}
		ByteBuffer wb3 = ByteBuffer.wrap(data);
		this.numberOfKilobytesShared = wb3.getInt();
	}

	public PongMessage(Header header, char localPort, InetAddress ipAddress, int numberOfFilesShared, int numberOfKilobytesShared) {
		this.setHeader(header);
		this.setPayload(new byte[LENGTH]);
		this.port = localPort;
		this.ipAddress = ipAddress;
		this.numberOfFilesShared = numberOfFilesShared;
		this.numberOfKilobytesShared = numberOfKilobytesShared;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(LENGTH);
		bf.putChar(this.port);

		byte[] ipAddressByte = this.ipAddress.getAddress();
		bf.put(ipAddressByte[3]);
		bf.put(ipAddressByte[2]);
		bf.put(ipAddressByte[1]);
		bf.put(ipAddressByte[0]);

		bf.putInt(this.numberOfFilesShared);
		bf.putInt(this.numberOfKilobytesShared);
		bf.flip();
		byte[] result = new byte[LENGTH];
		bf.get(result);

		this.setPayload(result);
		return super.getBytes();
	}

	public InetAddress getIpAddress() {
		return this.ipAddress;
	}

	public int getPort() {
		return port;
	}

	public int getNumberOfFilesShared() {
		return numberOfFilesShared;
	}

	public int getNumberOfKilobytesShared() {
		return numberOfKilobytesShared;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + super.toString() + " " + String.format("port:%d ipAddress:%s fileCount:%d fileSize:%d", (int) this.port, this.ipAddress.getHostAddress(), this.numberOfFilesShared, this.numberOfKilobytesShared)+"}";
	}
}
