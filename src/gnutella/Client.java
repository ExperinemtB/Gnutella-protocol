package gnutella;

import gnutella.Host.HostType;
import gnutella.listener.ClientEventListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client implements Runnable {
	private InetAddress address;
	private int port;
	private ClientEventListener clientEventListener;

	public Client(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			GnutellaConnection gnutellaConnection = new GnutellaConnection();
			gnutellaConnection.initConnect(address, port);

			// TCP/IPレベルでの接続完了
			if (this.clientEventListener != null) {
				this.clientEventListener.onConnect(port, address);
			}

			HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
			InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
			Host host = hostContainer.getHostByAddress(remoteAddress);
			if (host != null) {
				host.setHostType(HostType.NEIGHBOR);
				host.setConnection(gnutellaConnection);
			} else {
				host = hostContainer.createNeighborHost(remoteAddress, gnutellaConnection);
			}
			ConnectionDataReceiver dataReceiver = new ConnectionDataReceiver(host);
			dataReceiver.run();
		} catch (Exception ex) {
			if (this.clientEventListener != null) {
				this.clientEventListener.onThrowable(ex);
			} else {
				ex.printStackTrace();
			}
		}

		if (this.clientEventListener != null) {
			this.clientEventListener.onClose(port, address);
		}
	}

	public void setClientEventListener(ClientEventListener clientEventListener) {
		this.clientEventListener = clientEventListener;
	}
}
