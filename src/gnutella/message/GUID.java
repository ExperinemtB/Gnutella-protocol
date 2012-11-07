package gnutella.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class GUID {
	private byte[] guid;

	public final static int LENGTH = 16;

	public GUID() {
		Random rand = new Random(System.currentTimeMillis());
		this.guid = new byte[LENGTH];
		rand.nextBytes(this.guid);
	}
		
	public GUID(InetAddress ipAddress) {
		byte[] temp = ipAddress.getAddress();
		int number = temp.length;
		int len = 0;
		Random rnd = new Random(System.currentTimeMillis());
		for (int i = 0; i < LENGTH; i++) {
			if (i < 15 - number) {
				this.guid[i] = (byte) rnd.nextInt(255);
			} else {
				this.guid[i] = temp[len];
				len++;
			}
		}
	}

	public GUID(byte[] guid) {
		if (guid.length != LENGTH) {
			throw new IllegalArgumentException("Size of guid should be " + String.valueOf(LENGTH) + " byte");
		}
		this.guid = Arrays.copyOf(guid, LENGTH);
	}

	@Override
	public int hashCode() {
		ByteBuffer wb = ByteBuffer.wrap(this.guid);
		int intGuid = wb.getInt();
		return intGuid;
		
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GUID)) {
			return false;
		}
		if (o == this) {
			return true;
		}
		GUID ex = (GUID) o;
		return this.hashCode() == ex.hashCode() && Arrays.equals(this.guid, ex.getGuid());
	}

	public byte[] getGuid() {
		return guid;
	}

}
