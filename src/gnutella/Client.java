package gnutella;

import gnutella.Host.HostType;

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
			
			HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
			InetSocketAddress remoteAddress =  new InetSocketAddress(address, port);
			Host host =hostContainer.getHostByAddress(remoteAddress);
			if(host!=null){
				host.setHostType(HostType.NEIGHBOR);
				host.setConnection(connection);
			}else{
				host = hostContainer.createNeighborHost(remoteAddress, connection);
			}
			ConnectionDataReceiver dataReceiver = new ConnectionDataReceiver(host);
			dataReceiver.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
