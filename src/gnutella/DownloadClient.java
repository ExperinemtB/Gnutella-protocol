package gnutella;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * HTTP GET要求を送り、それを受け取るRunner
 * @author generic
 *
 */
public class DownloadClient implements Runnable {
	private InetAddress address;
	private int port;
	private int fileIndex;
	private String fileName;

	public DownloadClient(InetAddress address, int port,int fileIndex,String fileName) {
		this.address = address;
		this.port = port;
		this.fileIndex = fileIndex;
		this.fileName = fileName;
	}

	@Override
	public void run() {
		try {
			DownloadConnection connection = new DownloadConnection();
			connection.initConnect(address, port);
			Host remoteHost = GnutellaManeger.getInstance().getHostContainer().createFileTransportHost(new InetSocketAddress(address, port), connection);
			
			//QueryHitの一覧中から任意の結果を選択し、そのIP:Portに対して新たなコネクションを作成、HTTP GET送信する
			((DownloadConnection)remoteHost.getConnection()).sendHttpGetRequest(fileIndex, fileName);
			
			HttpHundler.hundleHttpGetRequestResponse(remoteHost,this.fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}