package gnutella.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class PongMessage extends Message {
	public static final int LENGTH = 23;
	private int ipAddress;
	private int port;
	private int numberOfFilesShared;
	private int numberOfKilobytesShared;

	public PongMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);

		byte[] data = new byte[4];

		data[0] = payload[0];
		data[1] = payload[1];
		ByteBuffer wb0 = ByteBuffer.wrap(data);
		this.port = wb0.getInt();

		for (int i = 0; i < 4; i++) {
			data[i] = payload[2 + i];
		}
		ByteBuffer wb1 = ByteBuffer.wrap(data);
		this.ipAddress = wb1.getInt();

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

	public PongMessage(Header header, int localPort, InetAddress localHost, int numberOfFiles, int numberOfKilobytes) {
		this.setHeader(header);
		this.setPayload(new byte[LENGTH]);
		this.port = localPort;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(4);
		bf.putInt(this.port);
		bf.flip();
		byte[] portBytes = new byte[4];
		bf.get(portBytes);
		byte[] resBytes = new byte[23];
		for (int i = 0; i < resBytes.length; i++) {
			resBytes[i] = 0;
			if (i < 4) {
				resBytes[i] = portBytes[i];
			}
		}

		this.setPayload(resBytes);
		return super.getBytes();
	}

	public static int getLength() {
		return LENGTH;
	}

	public int getIpAddress() {
		return ipAddress;
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
		return this.getClass().getName() + " " + String.format("port:%d", this.port) + " " + super.toString();
	}
}
