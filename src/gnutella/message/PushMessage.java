package gnutella.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class PushMessage extends Message {
	public final static int LENGTH = 26;

	private GUID serventIdentifier;
	private int fileIndex;
	private InetAddress ipAddress;
	private char port;

	public PushMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);

		ByteBuffer bf = ByteBuffer.wrap(payload);
		byte[] guidBytes = new byte[16];
		bf.get(guidBytes);
		this.serventIdentifier = new GUID(guidBytes);

		this.fileIndex = bf.getInt();
		byte[] ipBytes = new byte[4];
		bf.get(ipBytes);
		try {
			this.ipAddress = InetAddress.getByAddress(ipBytes);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = bf.getChar();
	}

	public PushMessage(Header header, GUID serventIdentifier, int fileIndex, InetAddress ipAddress, char port) {
		this.setHeader(header);
		this.serventIdentifier = serventIdentifier;
		this.fileIndex = fileIndex;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(PushMessage.LENGTH);
		bf.put(this.serventIdentifier.getGuid());
		bf.putInt(this.fileIndex);
		bf.put(this.ipAddress.getAddress());
		bf.putChar(this.port);
		bf.flip();

		byte[] result = new byte[bf.limit()];
		bf.get(result);
		this.setPayload(result);
		return super.getBytes();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + super.toString() + " " + String.format("GUID:%s fileIndex:%d ipAddress:%s port:%d", serventIdentifier.toString(), this.fileIndex, this.ipAddress.toString(), (int)this.port) + "}";
	}

	public GUID getServentIdentifier() {
		return serventIdentifier;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public char getPort() {
		return port;
	}

}
