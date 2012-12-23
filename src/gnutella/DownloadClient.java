package gnutella;

import gnutella.HttpHundler.Range;
import gnutella.listener.DownloadClientEventListener;
import gnutella.listener.HttpEventListener;

import java.io.File;
import java.io.IOException;
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
	private int fileId;
	private long startPosition;
	private long length;
	private DownloadConnection connection;
	private double throughput;
	private long downloadStartTime;
	private DownloadClientEventListener downloadClientEventListener;

	public DownloadClient(InetAddress address, int port, int fileIndex, String fileName, int fileId,long startPosition,long length) {
		this.address = address;
		this.port = port;
		this.fileIndex = fileIndex;
		this.fileName = fileName;
		this.fileId = fileId;
		this.startPosition = startPosition;
		this.length = length;
		this.downloadStartTime = 0;
	}

	public DownloadClient(DownloadConnection connection, int fileIndex, String fileName, int fileId,long startPosition,long length) {
		this(connection.getInetAddress(),connection.getPort(),fileIndex,fileName,fileId,startPosition,length);
		this.connection = connection;
	}
	
	public void initialize(String fileName, int fileId,long startPosition,long length){
		System.out.println(String.format("DowonloadManeger initialized startPosition:%d length:%d",startPosition,length));
		this.fileName = fileName;
		this.fileId = fileId;
		this.startPosition = startPosition;
		this.length = length;
		this.downloadStartTime = 0;
	}

	@Override
	public void run() {
		try {
			if (this.connection == null || !this.connection.isConnected()) {
				this.connection = new DownloadConnection();
				connection.initConnect(address, port);
			}
			Host remoteHost = GnutellaManeger.getInstance().getHostContainer().createFileTransportHost(new InetSocketAddress(address, port), connection);

			// QueryHitの一覧中から任意の結果を選択し、そのIP:Portに対して新たなコネクションを作成、HTTP GET送信する
			Range range = (new HttpHundler()).new Range(this.startPosition, this.startPosition+this.length);
			this.downloadStartTime = System.currentTimeMillis();
			((DownloadConnection) remoteHost.getConnection()).sendHttpGetRequest(fileIndex, fileName,range);

			String fileName = String.format("%s_%d", this.fileName,fileId);
			
			//TODO:とりあえず
			remoteHost.getConnection().readLine();
			remoteHost.getConnection().readLine();
			
			HttpHundler httpHundler = new HttpHundler();
			httpHundler.setHttpGetRequestResponseEventListener(new HttpEventListener() {
				
				@Override
				public void onThrowable(Throwable throwable) {
					if (downloadClientEventListener != null) {
						downloadClientEventListener.onThrowable(DownloadClient.this,throwable);
					}	
				}
				
				@Override
				public void onReceiveData(byte[] receiveData) {
					if (downloadClientEventListener != null) {
						downloadClientEventListener.onReceiveData(DownloadClient.this, fileId, receiveData);
					}
				}
				
				@Override
				public void onHundleRequest(String requestLine, Host remoteHost) {
				}
				
				@Override
				public void onComplete(File file) {
					if (downloadClientEventListener != null) {
						downloadClientEventListener.onComplete(DownloadClient.this,fileId, file);
					}					
				}
			});
			httpHundler.hundleHttpGetRequestResponse(remoteHost,fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (this.downloadClientEventListener != null) {
				this.downloadClientEventListener.onThrowable(this,ex);
			}
		}
	}

	public void setDownloadClientEventListener(DownloadClientEventListener downloadClientEventListener) {
		this.downloadClientEventListener = downloadClientEventListener;
	}
	
	public double getThroughput() {
		return throughput;
	}

	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}

	public long getDownloadStartTime() {
		return downloadStartTime;
	}
	
	public void closeConnection() throws IOException{
		this.connection.close();
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileId() {
		return fileId;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public long getLength() {
		return length;
	}
}