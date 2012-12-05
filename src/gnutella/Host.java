package gnutella;

import java.net.InetSocketAddress;

public class Host {
	public enum HostType {
		NETWORK, NEIGHBOR , FILETRANSPORT
	};

	private HostType hostType;
	private InetSocketAddress address;
	private Connection connection;
	private int sharedFileCount;
	private int sharedFileTotalSizeKb;

	public Host(InetSocketAddress address) {
		this.setAddress(address);
	}

	public Host(InetSocketAddress address, GnutellaConnection gnutellaConnection) {
		this.setAddress(address);
		this.setConnection(gnutellaConnection);
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public int getSharedFileCount() {
		return sharedFileCount;
	}

	public void setSharedFileCount(int sharedFileCount) {
		this.sharedFileCount = sharedFileCount;
	}

	public HostType getHostType() {
		return hostType;
	}

	public void setHostType(HostType hostType) {
		this.hostType = hostType;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public int getSharedFileTotalSizeKb() {
		return sharedFileTotalSizeKb;
	}

	public void setSharedFileTotalSizeKb(int sharedFileTotalSizeKb) {
		this.sharedFileTotalSizeKb = sharedFileTotalSizeKb;
	}

}
