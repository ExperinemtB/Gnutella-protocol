package gnutella;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
	private int port = 50000;

	public Server(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {

			serverSocket = new ServerSocket(this.port);
			System.out.println("Start Server on:"+String.valueOf(this.port));			

			while (true) {
				Socket socket = serverSocket.accept();

				Connection connection = new Connection(socket);
				Host host = GnutellaManeger.getInstance().getHostContainer().createNeighborHost(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), connection);
				ConnectionDataReceiver dataReceiver = new ConnectionDataReceiver(host);
				GnutellaManeger.getInstance().executeOnThreadPool(dataReceiver);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
