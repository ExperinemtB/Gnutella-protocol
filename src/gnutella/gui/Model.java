package gnutella.gui;

import gnutella.DownloadWorker;
import gnutella.GnutellaServant;
import gnutella.Host;
import gnutella.listener.ClientEventListener;
import gnutella.listener.DownloadWorkerEventListener;
import gnutella.listener.MessageReceiveListener;
import gnutella.listener.ServerEventListener;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;
import gnutella.message.ResultSet;
import gnutella.message.ResultSetContent;
import gnutella.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

public class Model extends Observable {

	private String logMessage;

	private int port;
	private String keyword;
	private int minimumSpeedKB;

	private GnutellaServant servant;
	private Host[] onlineHosts;

	private ArrayList<QueryHitMessage> receiveQueryHitMessageList;
	private HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>> sameMD5ResultSetContentList;
	private String selectedMD5Digist;

	public Model() {
		this.receiveQueryHitMessageList = new ArrayList<QueryHitMessage>();

		this.servant = new GnutellaServant();
		this.servant.addMessageReceiveListener(new MessageReceiveListener() {
			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {

			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}

	public void start() {
		this.servant.start(this.port, new ServerEventListener() {

			@Override
			public void onStart(int port, InetAddress address) {
				appendLogMessage(String.format("ポート%dでサーバ起動開始", port));
			}

			@Override
			public void onThrowable(Throwable throwable) {
				appendLogMessage(throwable.getMessage());
			}

			@Override
			public void onStop() {
				appendLogMessage("サーバ停止");
			}
		});
	}

	public void connect(InetAddress ipAddress, int port) {
		this.servant.connect(ipAddress, port, new ClientEventListener() {

			@Override
			public void onThrowable(Throwable throwable) {
				appendLogMessage(throwable.getMessage());
			}

			@Override
			public void onConnect(int port, InetAddress address) {
				appendLogMessage(String.format("%s:%dとTCP/IPレベルでの接続完了", address.getHostName(), port));
			}

			@Override
			public void onClose(int port, InetAddress address) {
				appendLogMessage(String.format("%s:%dとの接続切断", address.getHostName(), port));
			}
		});
	}

	public void sendPing() {
		appendLogMessage("ping送信");
		setChanged();
		notifyObservers();
		this.servant.sendPing(new MessageReceiveListener() {

			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {
				// TODO とりあえずtoStringだけど見にくい
				appendLogMessage("Pong受信:" + pong.toString());
				setOnlineHosts(servant.getOnlineHosts());
			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {

			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}

	public void sendQuery() {
		clearReceiveQueryHitMessage();
		appendLogMessage("query送信");
		this.servant.sendQuery(this.keyword, this.minimumSpeedKB, new MessageReceiveListener() {

			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {
			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {
				// TODO とりあえずtoStringだけど見にくい
				appendLogMessage("QueryHit受信:" + queryHit.toString());
				addReceiveQueryHitMessage(queryHit);

				// MD5ハッシュごとにまとめて表示する
				HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>> sameMD5ResultSetContentMap = new HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>>();
				for (QueryHitMessage queryHitMessage : receiveQueryHitMessageList) {
					ResultSet resultSet = queryHitMessage.getResultSet();
					for (int i = 0; i < queryHitMessage.getNumberofHits(); i++) {
						ResultSetContent content = resultSet.getElementAt(i);
						ArrayList<SimpleEntry<QueryHitMessage, Integer>> sameMD5QueryHitMessageList = sameMD5ResultSetContentMap.get(FileUtils.getDigestStringExpression(content.getFileMD5digest()));
						if (sameMD5QueryHitMessageList == null) {
							sameMD5QueryHitMessageList = new ArrayList<SimpleEntry<QueryHitMessage, Integer>>();
							sameMD5ResultSetContentMap.put(FileUtils.getDigestStringExpression(content.getFileMD5digest()), sameMD5QueryHitMessageList);
						}
						sameMD5QueryHitMessageList.add(new SimpleEntry<QueryHitMessage, Integer>(queryHitMessage, content.getFileIndex()));
					}
				}
				setSameMD5ResultSetContentList(sameMD5ResultSetContentMap);
			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}

	public void addFile(String filePath) throws IOException {
		appendLogMessage("addFile");
		setChanged();
		notifyObservers();
		this.servant.addFile(filePath);
	}

	public void sendDownloadRequest() {
		appendLogMessage("DownloadRequest送信");
		setChanged();
		notifyObservers();

		List<SimpleEntry<Integer, QueryHitMessage>> resultSetList = new ArrayList<SimpleEntry<Integer, QueryHitMessage>>();
		for (QueryHitMessage queryHitMessage : receiveQueryHitMessageList) {
			ResultSet resultSet = queryHitMessage.getResultSet();
			for (int i = 0; i < queryHitMessage.getNumberofHits(); i++) {
				ResultSetContent content = resultSet.getElementAt(i);
				if (FileUtils.getDigestStringExpression(content.getFileMD5digest()).equals(this.selectedMD5Digist)) {
					resultSetList.add(new SimpleEntry<Integer, QueryHitMessage>(content.getFileIndex(), queryHitMessage));
				}
			}
		}

		// とりあえずファイル名は適当に
		// テキストボックスに入力させるとかした方がよさげ
		this.servant.sendDownloadRequest(resultSetList.get(0).getValue().getResultSet().getByFileIndex(resultSetList.get(0).getKey()).getFileName(), resultSetList, new DownloadWorkerEventListener() {
			@Override
			public void onComplete(DownloadWorker eventSource, int fileIndex, File file) {
				appendLogMessage(String.format("ダウンロード完了:%s", file.getAbsolutePath()));
			}

			@Override
			public void onThrowable(DownloadWorker eventSource, Throwable throwable) {
				appendLogMessage(throwable.getMessage());
			}

			@Override
			public void onReceiveData(DownloadWorker eventSource, String fileName, long totalReceivedLength, long totalFileLength) {
				appendLogMessage(String.format("ダウンロード中(%s):%d/%d", fileName, totalReceivedLength, totalFileLength));
			}
		});
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public void appendLogMessage(String logMessage) {
		if (this.logMessage == null) {
			this.logMessage = "";
		}
		this.logMessage += (logMessage + "\n");
		setChanged();
		notifyObservers();
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setfileName(String keyword) {
		this.keyword = keyword;
	}

	public void setSpeed(int miniSpeedKB) {
		this.minimumSpeedKB = miniSpeedKB;
	}

	private void setOnlineHosts(Host[] hosts) {
		this.onlineHosts = hosts;
		setChanged();
		notifyObservers("onlineHosts");
	}

	public Host[] getOnlineHosts() {
		return onlineHosts;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	private void addReceiveQueryHitMessage(QueryHitMessage queryHit) {
		this.receiveQueryHitMessageList.add(queryHit);
	}

	private void clearReceiveQueryHitMessage() {
		this.receiveQueryHitMessageList.clear();
	}

	public ArrayList<QueryHitMessage> getReceiveQueryHitMessageList() {
		return receiveQueryHitMessageList;
	}

	public String getSelectedMD5Digist() {
		return selectedMD5Digist;
	}

	public void setSelectedMD5Digist(String selectedMD5Digist) {
		this.selectedMD5Digist = selectedMD5Digist;
	}

	public HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>> getSameMD5ResultSetContentList() {
		return sameMD5ResultSetContentList;
	}

	public void setSameMD5ResultSetContentList(HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>> sameMD5ResultSetContentMap) {
		this.sameMD5ResultSetContentList = sameMD5ResultSetContentMap;
		setChanged();
		notifyObservers("sameMD5ResultSetContentList");
	}
}
