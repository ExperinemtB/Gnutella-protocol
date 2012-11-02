package gnutella;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client implements Runnable {
	private InetAddress address;
	private int port;

	public Client(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			Connection connection = new Connection();
			connection.initConnect(address, port);
			Host host = GnutellaManeger.getInstance().getHostContainer().createNeighborHost(new InetSocketAddress(address, port), connection);
			ConnectionDataReceiver dataReceiver = new ConnectionDataReceiver(host);
			dataReceiver.receiveData();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
