package gnutella;

import gnutella.listener.DownloadClientEventListener;
import gnutella.listener.DownloadWorkerEventListener;
import gnutella.listener.HttpEventListener;
import gnutella.message.Header;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.ResultSetContent;
import gnutella.share.SharedFileBlock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloadWorker implements Runnable {
	public enum DownloadStateType {
		PREPARING, MEASURING_THROUGHPUT, DOWNLOADING, COMPLETE,ERROR
	}

	private final int MeasureThroughoutTime = 5000;
	
	
	private DownloadStateType downloadState = DownloadStateType.PREPARING;

	private final List<SimpleEntry<Integer, QueryHitMessage>> queryHitMessageSet;
	private Server server;

	private String saveFileName;
	private long fileSize;
	private int minimumSpeedKb;

	private int splitCount;
	private List<DownloadClient> measureThroughputCompleteHostList;
	private ConcurrentLinkedQueue<DownloadClient> dlQueue;
	private ConcurrentHashMap<Integer, SharedFileBlock> dlComleteList;
	private ConcurrentLinkedQueue<DownloadClient> dlComleteClientList;

	private DownloadWorkerEventListener downloadWorkerEventListener;
	private long totalDownloadLength;

	private DownloadClientEventListener downloadEventListener = new DownloadClientEventListener() {

		@Override
		public synchronized void onComplete(DownloadClient eventSource, int fileId, File file) {
			if(downloadState==DownloadStateType.ERROR){
				System.out.println("Download Error");
				return;
			}
			
			removeDlQueue(eventSource);
			putDlComleteList(fileId, new SharedFileBlock(fileId, file));
			dlComleteClientList.add(eventSource);
			
			if (dlQueue.isEmpty()) {
				File resultFile = new File(saveFileName);
				// 分割DLを行った場合はファイルのマージを行う
				if (measureThroughputCompleteHostList.size() > 1) {
					BufferedOutputStream outStream = null;
					BufferedInputStream is = null;
					try {
						SharedFileBlock[] sortedArray = new SharedFileBlock[dlComleteList.entrySet().size()];
						for (Entry<Integer, SharedFileBlock> set : dlComleteList.entrySet()) {
							sortedArray[set.getKey()] = set.getValue();
						}
						outStream = new BufferedOutputStream(new FileOutputStream(resultFile));
						for (SharedFileBlock fileBlock : sortedArray) {
							File fileRead = fileBlock.getBaseFile();
							is = new BufferedInputStream(new FileInputStream(fileRead));
							System.out.println(String.format("FileMerging:%s", fileRead));

							int readData;
							while ((readData = is.read()) != -1) {
								outStream.write(readData);
							}
							is.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							is.close();
							outStream.flush();
							outStream.close();
							//分割されたファイルを削除
							for (Entry<Integer, SharedFileBlock> set : dlComleteList.entrySet()) {
								set.getValue().getBaseFile().delete();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else {
					file.renameTo(resultFile);
				}
				
				downloadState = DownloadStateType.COMPLETE;
				if (downloadWorkerEventListener != null) {
					downloadWorkerEventListener.onComplete(DownloadWorker.this, 0, file);
				}
				// ダウンロード用の接続を切断する
				for (DownloadClient client : dlComleteClientList) {
					try {
						client.closeConnection();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onThrowable(DownloadClient eventSource, Throwable throwable) {
			if(downloadState==DownloadStateType.ERROR){
				System.out.println("Download Error");
				return;
			}
			//あるノードからの分割DL失敗
			//他のノードから変わりにDLする
			
			//DL総合量を減らす
			addTotalDownloadLength(-eventSource.getLength());
			
			//DLに失敗したノードは候補から外す
			measureThroughputCompleteHostList.remove(eventSource);
			
			//もしも候補が残っていなかったらエラー
			if(measureThroughputCompleteHostList.size()<1){
				//とりあえずExceptionクラスで
				downloadWorkerEventListener.onThrowable(DownloadWorker.this, new Exception("ダウンロード失敗"));
				downloadState = DownloadStateType.ERROR;
				
				//元のクライアントはキューから除く
				removeDlQueue(eventSource);
				
				throwable.printStackTrace();
				return;
			}
			
			DownloadClient maxSpeedClient =measureThroughputCompleteHostList.get(0);
			for (int i = 1; i < measureThroughputCompleteHostList.size(); i++) {
				DownloadClient currentClient = measureThroughputCompleteHostList.get(i);
				if(currentClient.getThroughput()>maxSpeedClient.getThroughput()){
					maxSpeedClient = currentClient;
				}
			}
			maxSpeedClient.initialize(eventSource.getFileName(), eventSource.getFileId(), eventSource.getStartPosition(), eventSource.getLength());
			maxSpeedClient.setDownloadClientEventListener(downloadEventListener);
			addDlQueue(maxSpeedClient);
			
			//分割ダウンロード開始
			GnutellaManeger.getInstance().executeOnThreadPool(maxSpeedClient);
			
			//元のクライアントはキューから除く
			removeDlQueue(eventSource);
		}

		@Override
		public void onReceiveData(DownloadClient eventSource, int fileId, int receivedLength) {
			if (downloadState == DownloadStateType.ERROR) {
				System.out.println("Download Error");
				return;
			}

			addTotalDownloadLength(receivedLength);
		}
	};

	private DownloadClientEventListener measureThroughputEventListener = new DownloadClientEventListener() {

		@Override
		public synchronized void onComplete(DownloadClient eventSource, int fileId, File file) {
			if (downloadState == DownloadStateType.MEASURING_THROUGHPUT) {
				measureThroughputCompleteHostList.add(eventSource);
				eventSource.setThroughput((double) file.length() / (System.currentTimeMillis() - eventSource.getDownloadStartTime()));
				if (queryHitMessageSet.size() == measureThroughputCompleteHostList.size()) {
					doDownload();
				}
			}
		}

		@Override
		public void onThrowable(DownloadClient eventSource, Throwable throwable) {
			throwable.printStackTrace();
		}

		@Override
		public void onReceiveData(DownloadClient eventSource, int fileId, int receivedLength) {
		}
	};

	public DownloadWorker(String saveFileName, Server server, List<SimpleEntry<Integer, QueryHitMessage>> queryHitMessageSet) {
		this.server = server;
		this.queryHitMessageSet = queryHitMessageSet;
		this.saveFileName = saveFileName;
		this.minimumSpeedKb = 1; 

		this.measureThroughputCompleteHostList = new ArrayList<DownloadClient>();
		this.dlQueue = new ConcurrentLinkedQueue<DownloadClient>();
		this.dlComleteList = new ConcurrentHashMap<Integer, SharedFileBlock>();
		this.dlComleteClientList = new ConcurrentLinkedQueue<DownloadClient>();

		this.totalDownloadLength = 0;
	}

	@Override
	public void run() {
		downloadState = DownloadStateType.MEASURING_THROUGHPUT;

		int fileId = 0;
		for (Entry<Integer, QueryHitMessage> queryHitMessageSet : this.queryHitMessageSet) {
			final Integer fileIndex = queryHitMessageSet.getKey();
			final QueryHitMessage queryHit = queryHitMessageSet.getValue();

			this.fileSize = queryHit.getResultSet().getByFileIndex(fileIndex).getFileSize();

			// ローカルのアドレスの時はPush
			if (queryHit.getIpAddress().isSiteLocalAddress()) {
				GnutellaManeger maneger = GnutellaManeger.getInstance();
				Header header = new Header(queryHit.getHeader().getGuid(), Header.PUSH, (byte) (queryHit.getHeader().getHops() + 1), (byte) 1, PushMessage.LENGTH);
				// Header header = new Header(new GUID(), Header.PUSH, (byte) (queryHit.getHeader().getHops() + 1), (byte) 1, PushMessage.LENGTH);
				final int fFileId = fileId;
				HttpEventListener hundleGivRequestEventListener = new HttpEventListener() {

					@Override
					public void onThrowable(Throwable throwable) {
					}

					@Override
					public void onReceiveData(byte[] receiveData, int length) {
					}

					@Override
					public void onHundleRequest(String requestLine, Host remoteHost) {
						try {
							ResultSetContent resultSetContent = queryHit.getResultSet().getByFileIndex(fileIndex);
							long splitedSize = Math.min(resultSetContent.getFileSize(),caluclateTestFileSize(minimumSpeedKb,MeasureThroughoutTime));
							DownloadClient client = new DownloadClient((DownloadConnection) remoteHost.getConnection(), fileIndex, resultSetContent.getFileName(), fFileId, splitedSize * fFileId, splitedSize);
							client.setDownloadClientEventListener(measureThroughputEventListener);

							// まずはスループットを計測する
							GnutellaManeger.getInstance().executeOnThreadPool(client);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onComplete(File file) {
					}
				};

				server.getHttpHundler().addHundleGivRequestEventListener(hundleGivRequestEventListener, queryHit.getServentIdentifier(), fileIndex);
				try {
					PushMessage push = new PushMessage(header, queryHit.getServentIdentifier(), fileIndex, InetAddress.getLocalHost(), (char) maneger.getPort());
					Host nextHost = maneger.getRoutingTable().getNextHost(push);
					nextHost.getConnection().sendMessage(push);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				ResultSetContent resultSetContent = queryHit.getResultSet().getByFileIndex(fileIndex);
				long splitedSize = Math.min(resultSetContent.getFileSize(),caluclateTestFileSize(this.minimumSpeedKb,this.MeasureThroughoutTime));

				DownloadClient client = new DownloadClient(queryHit.getIpAddress(), queryHit.getPort(), fileIndex, queryHit.getResultSet().getByFileIndex(fileIndex).getFileName(), fileId, splitedSize * fileId, splitedSize);
				client.setDownloadClientEventListener(measureThroughputEventListener);

				// まずはスループットを計測する
				GnutellaManeger.getInstance().executeOnThreadPool(client);
			}
			fileId++;
		}

		// スループットの計測待ち
		GnutellaManeger.getInstance().executeOnThreadPool(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(MeasureThroughoutTime);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				doDownload();
			}
		});
	}

	private synchronized void doDownload() {
		System.out.println("doDownload from "+String.valueOf(measureThroughputCompleteHostList.size())+" node");
		
		if(downloadState==DownloadStateType.ERROR){
			System.out.println("Download Error");
			return;
		}
		if (this.downloadState == DownloadStateType.COMPLETE || this.downloadState == DownloadStateType.DOWNLOADING) {
			System.out.println("Already start Downloading");
			return;
		}
		this.downloadState = DownloadStateType.DOWNLOADING;

		// DLを行う
		// スループットに応じた割り当てをする
		double sum = 0;
		splitCount = measureThroughputCompleteHostList.size();
		for (DownloadClient downloadClient : measureThroughputCompleteHostList) {
			sum += downloadClient.getThroughput();
		}

		long currentStartPosition = 0;
		for (int i = 0; i < measureThroughputCompleteHostList.size(); i++) {
			DownloadClient downloadClient = measureThroughputCompleteHostList.get(i);
			long currentLength = (long) (this.fileSize * downloadClient.getThroughput() / sum);
			if (i == measureThroughputCompleteHostList.size() - 1) {
				downloadClient.initialize(saveFileName, i, currentStartPosition, this.fileSize - currentStartPosition);
			} else {
				downloadClient.initialize(saveFileName, i, currentStartPosition, currentLength);
			}
			currentStartPosition += currentLength;

			downloadClient.setDownloadClientEventListener(downloadEventListener);
			addDlQueue(downloadClient);
		}

		// 分割ダウンロード開始
		for (DownloadClient downloadClient : measureThroughputCompleteHostList) {
			GnutellaManeger.getInstance().executeOnThreadPool(downloadClient);
		}
	}

	private boolean addDlQueue(DownloadClient client) {
		return this.dlQueue.add(client);
	}

	private boolean removeDlQueue(DownloadClient client) {
		return this.dlQueue.remove(client);
	}

	private SharedFileBlock putDlComleteList(Integer fileId, SharedFileBlock fileBlock) {
		return this.dlComleteList.put(fileId, fileBlock);
	}

	public void setDownloadWorkerEventListener(DownloadWorkerEventListener downloadWorkerEventListener) {
		this.downloadWorkerEventListener = downloadWorkerEventListener;
	}

	private synchronized void addTotalDownloadLength(long amount) {
		this.totalDownloadLength += amount;

		if (this.downloadWorkerEventListener != null) {
			this.downloadWorkerEventListener.onReceiveData(DownloadWorker.this, saveFileName, totalDownloadLength, fileSize);
		}
	}
	
	private long caluclateTestFileSize(int needSppedInKb,int measureThroughoutTime){
		return (long)needSppedInKb*measureThroughoutTime * 1024L;
	}
}
