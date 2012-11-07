package gnutella;

import gnutella.Host.HostType;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
	private int port;

	public Server(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {

			serverSocket = new ServerSocket(this.port);
			GnutellaManeger.getInstance().setPort(port);
			
			System.out.println("Start Server on:"+String.valueOf(this.port));			

			while (true) {
				Socket socket = serverSocket.accept();

				Connection connection = new Connection(socket);
				InetSocketAddress remoteAddress =  new InetSocketAddress(socket.getInetAddress(), socket.getPort());
				HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
				Host host =hostContainer.getHostByAddress(remoteAddress);
				if(host!=null){
					host.setHostType(HostType.NEIGHBOR);
					host.setConnection(connection);
				}else{
					//remoteAddressのportは待ち受けポートではない
					//Pongを受け取った際に更新する
					host = hostContainer.createNeighborHost(remoteAddress, connection);
				}
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
