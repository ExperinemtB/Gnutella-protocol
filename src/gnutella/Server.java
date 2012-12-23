package gnutella;

import gnutella.GnutellaConnection.ConnectionStateType;
import gnutella.Host.HostType;
import gnutella.message.GUID;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class Server implements Runnable {
	private int port;
	private HttpHundler httpHundler;
	private ServerEventListener serverEventListener;

	public Server(int port) {
		this.port = port;
		this.httpHundler = new HttpHundler();
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {

			serverSocket = new ServerSocket(this.port);
			GnutellaManeger.getInstance().setPort(port);
			GnutellaManeger.getInstance().setUID(new GUID(InetAddress.getLocalHost()));

			System.out.println("Start Server on:" + String.valueOf(this.port));

			while (true) {
				Socket socket = serverSocket.accept();
				Connection connection = new Connection(socket);
				String requestLine = connection.readLine();

				if ((requestLine + "\n\n").startsWith(GnutellaConnection.GNUTELLA_CONNECT)) {
					System.out.println("Connection Request Received");
					// 改行2行で区切られているためもう一行進める
					connection.readLine();

					GnutellaConnection gnutellaConnection = new GnutellaConnection(socket);
					InetSocketAddress remoteAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
					HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
					Host host = hostContainer.getHostByAddress(remoteAddress);
					if (host != null) {
						host.setHostType(HostType.NEIGHBOR);
						host.setConnection(gnutellaConnection);
					} else {
						// remoteAddressのportは待ち受けポートではない
						// Pongを受け取った際に更新する
						host = hostContainer.createNeighborHost(remoteAddress, gnutellaConnection);
					}

					for (int i = 0; i < GnutellaManeger.getInstance().getMAX_SERVER_EXECUTE(); i++) {
						ConnectionDataReceiver dataReceiver = new ConnectionDataReceiver(host);
						GnutellaManeger.getInstance().executeOnThreadPool(dataReceiver);
					}

					host.getConnection().sendString(GnutellaConnection.GNUTELLA_OK);
					((GnutellaConnection) host.getConnection()).setConnectionState(ConnectionStateType.CONNECT);
				} else if (Pattern.matches(DownloadConnection.HTTP_GET_REQUEST_PATTERN, requestLine)) {
					System.out.println("Hundle Get Request");

					HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
					InetSocketAddress remoteAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
					DownloadConnection downloadConnection = new DownloadConnection(socket);
					Host host = hostContainer.createFileTransportHost(remoteAddress, downloadConnection);

					// リクエストをパースし、要求されたファイルを返す
					// 要求元が新しいコネクションを貼ってきているため、hostのConnectionに対してファイルの内容を送る
					Runnable getRequestHundler = HttpHundler.hundleHttpGetRequest(requestLine, host);
					GnutellaManeger.getInstance().executeOnThreadPool(getRequestHundler);
				} else if (Pattern.matches(DownloadConnection.HTTP_GIV_REQUEST_PATTERN, requestLine)) {
					System.out.println("Hundle Giv Request");

					HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
					InetSocketAddress remoteAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());

					// 相手側からコネクションを作成してくれたため、このコネクションにGETリクエストを送る
					DownloadConnection filrTransportConnection = new DownloadConnection(socket);
					Host remoteHost = hostContainer.createFileTransportHost(remoteAddress, filrTransportConnection);
					GnutellaManeger.getInstance().executeOnThreadPool(httpHundler.hundleHttpGivRequest(requestLine, remoteHost));
				}
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
	public HttpHundler getHttpHundler() {
		return httpHundler;
	}

	public void setServerEventListener(ServerEventListener serverEventListener) {
		this.serverEventListener = serverEventListener;
	}
}
