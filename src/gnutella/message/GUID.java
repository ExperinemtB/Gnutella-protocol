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
		this.guid = new byte[LENGTH];
		byte[] ipBytes = ipAddress.getAddress();
		byte[] diffBytes = new byte[LENGTH - ipBytes.length];
		(new Random(System.currentTimeMillis())).nextBytes(diffBytes);

		System.arraycopy(ipBytes, 0, this.guid, 0, ipBytes.length);
		System.arraycopy(diffBytes, 0, this.guid, ipBytes.length, diffBytes.length);
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
