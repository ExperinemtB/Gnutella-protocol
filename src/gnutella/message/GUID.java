package gnutella.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class GUID {
	private byte[] guid;


	public byte[] GUID(InetAddress ipAddress){
		byte[] guid = new byte[16];
		guid = ipAddress.getAddress();
		return guid;
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
		return this.hashCode() == ex.hashCode() && this.guid.equals(o);
	}
}

