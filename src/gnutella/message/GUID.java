package gnutella.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

public class GUID {
	private byte[] guid;

	public final static int LENGTH = 16;

	public GUID(InetAddress ipAddress){
		byte[] temp = ipAddress.getAddress();
		int number = temp.length;
		int len = 0;
		for(int i = 0; i < 16; i++){
			if(i < 15-number){
				Random rnd = new Random();
				this.guid[i] = (byte) rnd.nextInt(10);
			}
			else{
				this.guid[i] = temp[len];
				len++;
			}
		}
	}

	public GUID(byte[] guid){
		if (guid.length != LENGTH) {
			throw new IllegalArgumentException("Size of guid should be " + String.valueOf(LENGTH) + " byte");
		}
		this.guid = Arrays.copyOf(guid, LENGTH);
	}
	@Override
	public int hashCode(){
		ByteBuffer wb = ByteBuffer.wrap(this.guid);
		int intGuid = wb.getInt();
		return intGuid;
	}

	@Override
	public boolean equals(Object o){
		if(o == null) return false;
		if(o == this) return true;
		if(o.getClass() != getClass()) return false;
		GUID ex = (GUID)o;
		return this.hashCode() == ex.hashCode() && this.guid == ex.guid;
	}
}

	public byte[] getGuid() {
		return guid;
	}
