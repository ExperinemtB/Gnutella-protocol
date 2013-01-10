package gnutella;

import gnutella.listener.MessageReceiveListener;
import gnutella.message.Header;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;
import gnutella.message.ResultSet;
import gnutella.message.ResultSetContent;
import gnutella.share.SharedFile;
import gnutella.share.SharedFileContainer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class MessageHundler {
	private GnutellaManeger maneger;
	private RoutingTable routingTable;
	private SharedFileContainer sharedFileContainer;
	private HostContainer hostContainer;

	public MessageHundler() {
		this.maneger = GnutellaManeger.getInstance();
		this.sharedFileContainer = maneger.getSharedFileContainer();
		this.routingTable = maneger.getRoutingTable();
		this.hostContainer = GnutellaManeger.getInstance().getHostContainer();
	}

	public boolean hundlePingMessage(PingMessage pingMessage, Host remoteHost) {
		boolean accepted = true;
		Header pingHeader = pingMessage.getHeader();

		List<MessageReceiveListener> messageReceiveListenerList = maneger.getMessageReceiveListeners();
		for (MessageReceiveListener messageReceiveListener : messageReceiveListenerList) {
			messageReceiveListener.onReceivePingMessage(pingMessage, remoteHost);
		}
		List<MessageReceiveListener> filteredMessageReceiveListenerList = maneger.getMessageReceiveListeners(pingHeader.getGuid());
		for (MessageReceiveListener messageReceiveListener : filteredMessageReceiveListenerList) {
			messageReceiveListener.onReceivePingMessage(pingMessage, remoteHost);
		}

		if (routingTable.isMessageAlreadyReceived(pingHeader.getGuid(), pingHeader.getPayloadDescriptor())) {
			System.out.println("isMessageAlreadyReceived");
			return false;
		}

		// Pongを返す
		try {
			Header pongHeader = new Header(pingHeader.getGuid(), Header.PONG, (byte) (pingHeader.getHops() + 1), (byte) 1, PongMessage.LENGTH);
			int numberOfFiles = sharedFileContainer.getFileCount();
			int numberOfKilobytes = sharedFileContainer.getTotalFileSizeKb();

			// pongのportは待受ポート
			PongMessage pong = new PongMessage(pongHeader, (char) GnutellaManeger.getInstance().getPort(), Inet4Address.getLocalHost(), numberOfFiles, numberOfKilobytes);
			remoteHost.getConnection().sendMessage(pong);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Pingを隣接ノードに送る
		int newTTL = pingHeader.getTtl() - 1;
		if (newTTL < 1) {
			System.out.println("TTL < 1");
			return false;
		}

		pingHeader.setTtl((byte) newTTL);
		pingHeader.setHops((byte) (pingHeader.getHops() + 1));

		Host[] neighborHosts = hostContainer.getNeighborHosts();
		for (Host host : neighborHosts) {
			if (!host.getAddress().equals(remoteHost.getAddress())) {
				pingMessage.setHeader(pingHeader);
				try {
					host.getConnection().sendMessage(pingMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return accepted;
	}

	public boolean hundlePongMessage(PongMessage pongMessage, Host remoteHost) {
		List<MessageReceiveListener> messageReceiveListenerList = maneger.getMessageReceiveListeners();
		for (MessageReceiveListener messageReceiveListener : messageReceiveListenerList) {
			messageReceiveListener.onReceivePongMessage(pongMessage, remoteHost);
		}
		List<MessageReceiveListener> filteredMessageReceiveListenerList = maneger.getMessageReceiveListeners(pongMessage.getHeader().getGuid());
		for (MessageReceiveListener messageReceiveListener : filteredMessageReceiveListenerList) {
			messageReceiveListener.onReceivePongMessage(pongMessage, remoteHost);
		}

		// ////////Pongの内容をローカルに保存する
		// 隣接ノードの自身のPingに対する応答の時
		if (pongMessage.getHeader().getHops() == 1) {
			InetSocketAddress connectedAddress = remoteHost.getAddress();
			InetAddress ipAddress = connectedAddress.getAddress();
			if (ipAddress.equals(pongMessage.getIpAddress())) {
				// 隣接ノードが発したPongの時
				remoteHost.setSharedFileCount(pongMessage.getNumberOfFilesShared());
				remoteHost.setSharedFileTotalSizeKb(pongMessage.getNumberOfKilobytesShared());

				// hostのPortを接続可能な待受ポートに正しく設定し直す
				int connectedPort = connectedAddress.getPort();
				int pongPort = pongMessage.getPort();
				if (connectedPort != pongPort) {
					// 相手からの接続で通信が始まった場合はhostのportは待受ポートに設定されていない
					// Pong中のportは待ち受けポートを表す
					remoteHost.setAddress(new InetSocketAddress(connectedAddress.getAddress(), pongPort));
				}
			}
		} else {
			InetSocketAddress soureceAddress = new InetSocketAddress(pongMessage.getIpAddress(), pongMessage.getPort());
			Host sourceHost = hostContainer.getHostByAddress(soureceAddress);
			if (sourceHost == null) {
				sourceHost = hostContainer.createNetworkHost(soureceAddress);
			}
			sourceHost.setSharedFileCount(pongMessage.getNumberOfFilesShared());
			sourceHost.setSharedFileTotalSizeKb(pongMessage.getNumberOfKilobytesShared());
		}

		// Pongをリレーする
		Header pongHeader = pongMessage.getHeader();
		int newTTL = pongHeader.getTtl() - 1;
		if (newTTL < 1) {
			System.out.println("TTL < 1");
			return false;
		}

		pongHeader.setTtl((byte) newTTL);
		pongHeader.setHops((byte) (pongHeader.getHops() + 1));
		pongMessage.setHeader(pongHeader);

		Host toSendHost = routingTable.getNextHost(pongMessage);
		try {
			if (toSendHost != null) {
				toSendHost.getConnection().sendMessage(pongMessage);
			} else {
				System.err.println("Host to forward is not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean hundleQueryMessage(QueryMessage queryMessage, Host remoteHost) {
		List<MessageReceiveListener> messageReceiveListenerList = maneger.getMessageReceiveListeners();
		for (MessageReceiveListener messageReceiveListener : messageReceiveListenerList) {
			messageReceiveListener.onReceiveQueryMessage(queryMessage, remoteHost);
		}
		List<MessageReceiveListener> filteredMessageReceiveListenerList = maneger.getMessageReceiveListeners(queryMessage.getHeader().getGuid());
		for (MessageReceiveListener messageReceiveListener : filteredMessageReceiveListenerList) {
			messageReceiveListener.onReceiveQueryMessage(queryMessage, remoteHost);
		}

		Header queryHeader = queryMessage.getHeader();

		if (routingTable.isMessageAlreadyReceived(queryHeader.getGuid(), queryHeader.getPayloadDescriptor())) {
			System.out.println("isMessageAlreadyReceived");
			return false;
		}

		GnutellaManeger maneger = GnutellaManeger.getInstance();

		// クライアントが求める最低速度よりも自身が速い時のみ応答する
		if (queryMessage.getMinimumSpeedKb() >= maneger.getSpeed()) {
			// 目的のファイルが見つかればqueryhitsを返す
			SharedFile[] hitFiles = sharedFileContainer.searchFilesByKeyword(queryMessage.getSearchCriteria());
			if (hitFiles.length > 0) {
				try {
					ResultSet resultSet = new ResultSet();
					for (int i = 0; i < hitFiles.length; i++) {
						resultSet.add(new ResultSetContent(hitFiles[i].getFileIndex(), hitFiles[i].getOriginFile().length(), hitFiles[i].getFileName(),hitFiles[i].getFileMD5Digest()));
					}
					Header queryHitHeader = new Header(queryHeader.getGuid(), Header.QUERYHIT, (byte) (queryHeader.getHops() + 1), (byte) 1, QueryHitMessage.MIN_LENGTH + resultSet.getByteLength());

					// queryHitのportは待受ポート
					QueryHitMessage queryHit = new QueryHitMessage(queryHitHeader, (byte) hitFiles.length, (char) maneger.getPort(), Inet4Address.getLocalHost(), maneger.getSpeed(), resultSet, maneger.getUID());
					remoteHost.getConnection().sendMessage(queryHit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Queryを隣接ノードに送る
		int newTTL = queryHeader.getTtl() - 1;
		if (newTTL < 1) {
			System.out.println("TTL < 1");
			return false;
		}

		queryHeader.setTtl((byte) newTTL);
		queryHeader.setHops((byte) (queryHeader.getHops() + 1));

		Host[] neighborHosts = hostContainer.getNeighborHosts();
		for (Host host : neighborHosts) {
			if (!host.getAddress().equals(remoteHost.getAddress())) {
				queryMessage.setHeader(queryHeader);
				try {
					host.getConnection().sendMessage(queryMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public boolean hundleQueryHitMessage(QueryHitMessage queryHitMessage, Host remoteHost) {
		List<MessageReceiveListener> messageReceiveListenerList = maneger.getMessageReceiveListeners();
		for (MessageReceiveListener messageReceiveListener : messageReceiveListenerList) {
			messageReceiveListener.onReceiveQueryHitMessage(queryHitMessage, remoteHost);
		}
		List<MessageReceiveListener> filteredMessageReceiveListenerList = maneger.getMessageReceiveListeners(queryHitMessage.getHeader().getGuid());
		for (MessageReceiveListener messageReceiveListener : filteredMessageReceiveListenerList) {
			messageReceiveListener.onReceiveQueryHitMessage(queryHitMessage, remoteHost);
		}

		// QueryHitをリレーする
		Header queryHitHeader = queryHitMessage.getHeader();
		int newTTL = queryHitHeader.getTtl() - 1;
		if (newTTL < 1) {
			System.out.println("TTL < 1");
			return true;
		}

		queryHitHeader.setTtl((byte) newTTL);
		queryHitHeader.setHops((byte) (queryHitHeader.getHops() + 1));
		queryHitMessage.setHeader(queryHitHeader);

		Host toSendHost = routingTable.getNextHost(queryHitMessage);
		try {
			if (toSendHost != null) {
				toSendHost.getConnection().sendMessage(queryHitMessage);
			} else {
				System.err.println("Host to forward is not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean hundlePushMessage(PushMessage pushMessage, Host remoteHost) {
		List<MessageReceiveListener> messageReceiveListenerList = maneger.getMessageReceiveListeners();
		for (MessageReceiveListener messageReceiveListener : messageReceiveListenerList) {
			messageReceiveListener.onReceivePushMessage(pushMessage, remoteHost);
		}
		List<MessageReceiveListener> filteredMessageReceiveListenerList = maneger.getMessageReceiveListeners(pushMessage.getHeader().getGuid());
		for (MessageReceiveListener messageReceiveListener : filteredMessageReceiveListenerList) {
			messageReceiveListener.onReceivePushMessage(pushMessage, remoteHost);
		}

		// 自身に対するPushのとき
		if (pushMessage.getServentIdentifier().equals(GnutellaManeger.getInstance().getUID())) {
			// ファイル転送専用のコネクションを新たに張る
			DownloadConnection fileTransportConnection = new DownloadConnection();
			try {
				InetSocketAddress fileTransportRemoteAddress = new InetSocketAddress(pushMessage.getIpAddress(), pushMessage.getPort());
				HostContainer hostContainer = GnutellaManeger.getInstance().getHostContainer();
				fileTransportConnection.connect(pushMessage.getIpAddress(), pushMessage.getPort());
				Host fileTransportHost = hostContainer.createFileTransportHost(fileTransportRemoteAddress, fileTransportConnection);

				SharedFile file = sharedFileContainer.getSharedFileByFileIndex(pushMessage.getFileIndex());
				fileTransportConnection.sendHttpGivRequest(GnutellaManeger.getInstance().getUID().toHexString(), pushMessage.getFileIndex(), file.getFileName());

				Runnable hundleHttpGivRequestResponse = HttpHundler.hundleHttpGivRequestResponse(fileTransportHost);
				GnutellaManeger.getInstance().executeOnThreadPool(hundleHttpGivRequestResponse);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return false;
		} else {
			// Pushをリレーする
			Header pushHeader = pushMessage.getHeader();
			int newTTL = pushHeader.getTtl() - 1;
			if (newTTL < 1) {
				System.out.println("TTL < 1");
				return false;
			}

			pushHeader.setTtl((byte) newTTL);
			pushHeader.setHops((byte) (pushHeader.getHops() + 1));
			pushMessage.setHeader(pushHeader);

			Host toSendHost = routingTable.getNextHost(pushMessage);
			try {
				if (toSendHost != null) {
					toSendHost.getConnection().sendMessage(pushMessage);
				} else {
					System.err.println("Host to forward is not found");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
