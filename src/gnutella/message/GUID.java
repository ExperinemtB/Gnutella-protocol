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

	public GUID(String hexString) {
		this.guid = toBytes(hexString);
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

	@Override
	public String toString() {
		return toHexString();
	}

	public String toHexString() {
		StringBuffer bf = new StringBuffer();
		for (byte b : this.guid) {
			bf.append(String.format("%1$02x", b & 0xff));
		}
		return bf.toString();
	}

	public static final byte[] toBytes(String hexString) {
		if (hexString == null) {
			throw new NullPointerException("HexString is null");
		}
		int length = hexString.length();
		if (length % 2 != 0) {
			throw new NumberFormatException("Hex string has odd characters: " + hexString);
		}

		byte[] data = new byte[length / 2];
		char highChar, lowChar;
		byte highNibble, lowNibble;
		for (int i = 0, offset = 0; i < length; i += 2, offset++) {
			highChar = hexString.charAt(i);
			if (highChar >= '0' && highChar <= '9') {
				highNibble = (byte) (highChar - '0');
			} else if (highChar >= 'A' && highChar <= 'F') {
				highNibble = (byte) (10 + highChar - 'A');
			} else if (highChar >= 'a' && highChar <= 'f') {
				highNibble = (byte) (10 + highChar - 'a');
			} else {
				throw new NumberFormatException("Invalid hex char: " + highChar);
			}

			lowChar = hexString.charAt(i + 1);
			if (lowChar >= '0' && lowChar <= '9') {
				lowNibble = (byte) (lowChar - '0');
			} else if (lowChar >= 'A' && lowChar <= 'F') {
				lowNibble = (byte) (10 + lowChar - 'A');
			} else if (lowChar >= 'a' && lowChar <= 'f') {
				lowNibble = (byte) (10 + lowChar - 'a');
			} else {
				throw new NumberFormatException("Invalid hex char: " + lowChar);
			}

			data[offset] = (byte) (highNibble << 4 | lowNibble);
		}
		return data;
	}
}
