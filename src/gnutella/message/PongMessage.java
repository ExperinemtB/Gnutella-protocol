package gnutella.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.sun.xml.internal.ws.org.objectweb.asm.ByteVector;

public class PongMessage extends Message {
	public static final int LENGTH = 23;
	private int fileCount;
	private int fileSize;
	private int port;

	public PongMessage(Header header, byte[] payload) {
		this.setHeader(header);
		this.setPayload(payload);
		ByteBuffer bf = ByteBuffer.wrap(payload);
		this.port = bf.getInt();
	}

	public PongMessage(Header header, int localPort, InetAddress localHost, int numberOfFiles, int numberOfKilobytes) {
		this.setHeader(header);
		this.setPayload(new byte[LENGTH]);
		this.port = localPort;
	}

	public int getFileCount() {
		return fileCount;
	}

	public int getFileSize() {
		return fileSize;
	}

	public InetAddress getAddress() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public int getPort() {
		// TODO 自動生成されたメソッド・スタブ
		return this.port;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(4);
		bf.putInt(this.port);
		bf.flip();
		byte[] portBytes = new byte[4];
		bf.get(portBytes);
		byte[] resBytes = new byte[23];
		for(int i=0;i<resBytes.length;i++){
			resBytes[i]=0;
			if(i<4){
				resBytes[i]=portBytes[i];
			}
		}
		
		this.setPayload(resBytes);
		return super.getBytes();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " " + String.format("port:%d", this.port) + " " + super.toString();
	}
}
