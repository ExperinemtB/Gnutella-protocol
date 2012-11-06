package gnutella.message;

import java.nio.ByteBuffer;

public class PongMessage extends Message {
	int port, ipAddress, numberOfFilesShared, numberOfKilobytesShared;
	public PongMessage(Header header, byte[] payload) {
		byte[] data = new byte[4];

		data[0] = payload[0];
		data[1] = payload[1];
		ByteBuffer wb0 = ByteBuffer.wrap(data);
		this.port = wb0.getInt();

		for(int i = 0; i < 4; i++){
			data[i] = payload[2+i];
		}
		ByteBuffer wb1 = ByteBuffer.wrap(data);
		this.ipAddress = wb1.getInt();

		for(int i = 0; i < 4; i++){
			data[i] = payload[6+i];
		}
		ByteBuffer wb2 = ByteBuffer.wrap(data);
		this.numberOfFilesShared = wb2.getInt();

		for(int i = 0; i < 4; i++){
			data[i] = payload[10+i];
		}
		ByteBuffer wb3 = ByteBuffer.wrap(data);
		this.numberOfKilobytesShared = wb3.getInt();
	}
}
