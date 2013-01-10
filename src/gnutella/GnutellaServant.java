package gnutella;

import gnutella.listener.ClientEventListener;
import gnutella.listener.ConnectionEventListener;
import gnutella.listener.DownloadWorkerEventListener;
import gnutella.listener.MessageReceiveListener;
import gnutella.listener.ServerEventListener;
import gnutella.message.GUID;
import gnutella.message.Header;
import gnutella.message.PingMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;
import gnutella.share.SharedFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

public class GnutellaServant {
	private GnutellaManeger maneger;
	private Server server;

	/**
	 * テスト用
	 * @param args[0] 0:サーバのみ 1:指定したアドレスに接続
	 * @param args[1] ローカルのポート
	 * @param args[2] 接続先ポート(args[0]=1の時のみ)
	 */
	public static void main(String[] args) {
		GnutellaServant servernt = new GnutellaServant();

		if (args.length >= 2) {
			int port = Integer.parseInt(args[1]);
			if (args[0].equals("0")) {
				servernt.start(port);
			}
			if (args[0].equals("1")) {
				servernt.start(port);
				int destPort = Integer.parseInt(args[2]);
				try {
					// テスト時はローカルで
					servernt.connect(InetAddress.getLocalHost(), destPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}

		// コンソールからstart *やconnect *という入力を受け付ける
		try {
			String inputStr = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while ((inputStr = br.readLine()) != null) {
				String[] cmd = inputStr.split(" ");
				try {
					if (cmd[0].equals("start")) {
						servernt.start(Integer.parseInt(cmd[1]));
					} else if (cmd[0].equals("connect")) {
						servernt.connect(InetAddress.getByName(cmd[1]), Integer.parseInt(cmd[2]));
					} else if (cmd[0].equals("sendPing")) {
						servernt.sendPing();
					} else if (cmd[0].equals("addFile")) {
						servernt.addFile(cmd[1]);
					} else if (cmd[0].equals("sendQuery")) {
						servernt.sendQuery(cmd[1], Integer.parseInt(cmd[2]));
					} else if (cmd[0].equals("sendPush")) {
						// とりあえずHeaderのGUIDとServerIdentifier,TTL指定する
						servernt.sendPush(new QueryHitMessage(new Header(new GUID(cmd[1]), Header.QUERYHIT, (byte) 0, (byte) Integer.parseInt(cmd[2]), PushMessage.LENGTH), (byte) 0, (char) 0, null, 0, null, new GUID(cmd[3])), Integer.parseInt(cmd[4]));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public GnutellaServant() {
		this.maneger = GnutellaManeger.getInstance();
	}

	/**
	 * サーバとしての動作を開始する。
	 * @param port メッセージを受信するポート番号
	 */
	public void start(int port) {
		start(port, null);
	}

	/**
	 * サーバとしての動作を開始する。
	 * @param port メッセージを受信するポート番号
	 * @param serverEventListener 起動成功時、エラー時、終了時に呼ばれるイベントのハンドラ
	 */
	public void start(int port, ServerEventListener serverEventListener) {
		server = new Server(port);
		if (serverEventListener != null) {
			server.setServerEventListener(serverEventListener);
		}
		this.maneger.executeOnThreadPool(server);
	}

	/**
	 * IPアドレスとポート番号を指定してGnutellaネットワークに参加中のノードにTCP/IPレベルで接続し、Gnutellaネットワークへの参加要求も送信する。
	 * @param ipAddress 接続先ノードのIPアドレス
	 * @param port 接続先ノードのポート番号
	 */
	public void connect(InetAddress ipAddress, int port) {
		connect(ipAddress, port, null);
	}

	/**
	 * IPアドレスとポート番号を指定してGnutellaネットワークに参加中のノードにTCP/IPレベルで接続し、Gnutellaネットワークへの参加要求も送信する。
	 * @param ipAddress 接続先ノードのIPアドレス
	 * @param port 接続先ノードのポート番号
	 * @param clientEventListener TCP/IPレベルでの接続成功時、エラー時、接続切断時に呼ばれるイベントのハンドラ
	 */
	public void connect(InetAddress ipAddress, int port, ClientEventListener clientEventListener) {
		Client client = new Client(ipAddress, port);
		if (clientEventListener != null) {
			client.setClientEventListener(clientEventListener);
		}
		this.maneger.executeOnThreadPool(client);
	}

	/**
	 * 隣接ノードにPingメッセージを送信する。
	 * @return 送信したPingメッセージのGUID
	 */
	public GUID sendPing() {
		return sendPing(null);

	}

	/**
	 * 隣接ノードにPingメッセージをを送信する。
	 * @param messageReceiveListener 送信したPingメッセージのGUIDに対するメッセージを受信した時に呼ばれるイベントのハンドラ
	 * @return 送信したPingメッセージのGUID
	 */
	public GUID sendPing(MessageReceiveListener messageReceiveListener) {
		Host[] neighborHosts = this.maneger.getHostContainer().getNeighborHosts();
		Header header = new Header(Header.PING, (byte) 7, 0);
		PingMessage ping = new PingMessage(header);
		if (messageReceiveListener != null) {
			addMessageReceiveListener(header.getGuid(), messageReceiveListener);
		}
		for (Host host : neighborHosts) {
			try {
				host.getConnection().sendMessage(ping);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header.getGuid();
	}

	/**
	 * 隣接ノードにPushメッセージを送信する。
	 * @deprecated ダウンロード時にPushメッセージを送る必要があるかどうかは{@link gnutella.DownloadManeger DownloadManeger}クラスにて自動的に判断される
	 * @param queryHit Pushメッセージが応答結果となる元のQueryHitメッセージ
	 * @param fileIndex fileIndex
	 * @return 送信したPushメッセージのGUID
	 */
	public GUID sendPush(QueryHitMessage queryHit, int fileIndex) {
		return sendPush(queryHit, fileIndex, null);
	}

	/**
	 * 隣接ノードにPushメッセージを送信する。
	 * @deprecated ダウンロード時にPushメッセージを送る必要があるかどうかは{@link gnutella.DownloadManeger DownloadManeger}クラスにて自動的に判断されます
	 * @param queryHit Pushメッセージが応答結果となる元のQueryHitメッセージ
	 * @param fileIndex fileIndex
	 * @param messageReceiveListener 送信したPushメッセージのGUIDに対するメッセージを受信した時に呼ばれるイベントのハンドラ
	 * @return 送信したPushメッセージのGUID
	 */
	private GUID sendPush(QueryHitMessage queryHit, int fileIndex, MessageReceiveListener messageReceiveListener) {
		GnutellaManeger maneger = this.maneger;
		Header header = new Header(queryHit.getHeader().getGuid(), Header.PUSH, (byte) (queryHit.getHeader().getHops() + 1), (byte) 1, PushMessage.LENGTH);
		if (messageReceiveListener != null) {
			addMessageReceiveListener(header.getGuid(), messageReceiveListener);
		}
		try {
			PushMessage push = new PushMessage(header, queryHit.getServentIdentifier(), fileIndex, InetAddress.getLocalHost(), (char) maneger.getPort());
			Host nextHost = maneger.getRoutingTable().getNextHost(push);
			nextHost.getConnection().sendMessage(push);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return header.getGuid();
	}

	/**
	 * 隣接ノードにQueryメッセージを送信する。
	 * @param keyword 検索ワード
	 * @param minimumSpeedKb 相手ノードに求める最低通信速度(KB/s)
	 * @return 送信したQueryメッセージのGUID
	 */
	public GUID sendQuery(String keyword, int minimumSpeedKb) {
		return sendQuery(keyword, minimumSpeedKb, null);
	}

	/**
	 * 隣接ノードにQueryメッセージを送信する
	 * @param keyword 検索ワード
	 * @param minimumSpeedKb 相手ノードに求める最低通信速度(KB/s)
	 * @param messageReceiveListener 送信したQueryメッセージのGUIDに対するメッセージを受信した時に呼ばれるイベントのハンドラ
	 * @return 送信したQueryメッセージのGUID
	 */
	public GUID sendQuery(String keyword, int minimumSpeedKb, MessageReceiveListener messageReceiveListener) {
		Host[] neighborHosts = this.maneger.getHostContainer().getNeighborHosts();
		Header header = new Header(Header.QUERY, (byte) 7, QueryMessage.MIN_LENGTH + keyword.getBytes().length + 1);
		if (messageReceiveListener != null) {
			addMessageReceiveListener(header.getGuid(), messageReceiveListener);
		}
		QueryMessage query = new QueryMessage(header, minimumSpeedKb, keyword);
		for (Host host : neighborHosts) {
			try {
				host.getConnection().sendMessage(query);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header.getGuid();
	}

	/**
	 * 共有するファイルを追加する。<br>
	 * ここで追加したフィアルが相手から受信可能となる
	 * @param path 共有するファイルのパス
	 * @return 追加に成功したかどうか
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public boolean addFile(String path) throws IOException {
		File localFile = new File(path);
		boolean addSuccess = false;
		try {
			addSuccess = this.maneger.getSharedFileContainer().addFile(localFile);
		} catch (NoSuchAlgorithmException e) {
			addSuccess = false;
			e.printStackTrace();
		}
		if (addSuccess) {
			System.out.println("addedFile:" + localFile.getName());
		}
		return addSuccess;
	}

	/**
	 * ファイルのダウンロードリクエストを送信する。<br>
	 * Pushメッセージを送るべきか直接HTTP接続を行うかは自動的に判断される。<br>
	 * 以前に受信したことのあるQueryHitメッセージと、それに含まれるResultSet中の目的のファイルのFileIndexをセットにして渡す。複数渡すと分割DLを行う。<br>
	 * <br>
	 * 例 <blockquote> {@code
	 *   List<SimpleEntry<Integer, QueryHitMessage>> maps = new ArrayList<SimpleEntry<Integer, QueryHitMessage>>();
	 *   maps.add(new SimpleEntry<Integer, QueryHitMessage>(0, queryHitMessage));
	 *   servernt.sendDownloadRequest(maps);
	 * } </blockquote>
	 * @param maps FileIndexをキー、QueryHitメッセージを値としたSimpleEntryのリスト
	 */
	public void sendDownloadRequest(String saveFileName,List<SimpleEntry<Integer, QueryHitMessage>> maps) {
		sendDownloadRequest(saveFileName, maps, null);
	}

	/**
	 * ファイルのダウンロードリクエストを送信する。<br>
	 * Pushメッセージを送るべきか直接HTTP接続を行うかは自動的に判断される。<br>
	 * 以前に受信したことのあるQueryHitメッセージと、それに含まれるResultSet中の目的のファイルのFileIndexをセットにして渡す。複数渡すと分割DLを行う。<br>
	 * <br>
	 * 例 <blockquote> {@code
	 *   List<SimpleEntry<Integer, QueryHitMessage>> maps = new ArrayList<SimpleEntry<Integer, QueryHitMessage>>();
	 *   maps.add(new SimpleEntry<Integer, QueryHitMessage>(0, queryHitMessage));
	 *   servernt.sendDownloadRequest(maps);
	 * } </blockquote>
	 * @param maps FileIndexをキー、QueryHitメッセージを値としたSimpleEntryのリスト
	 * @param downloadWorkerEventListener エラー発生時、ファイル転送時、ファイル転送完了時に呼ばれるイベントのハンドラ
	 */
	public void sendDownloadRequest(String saveFileName,List<SimpleEntry<Integer, QueryHitMessage>> maps, DownloadWorkerEventListener downloadWorkerEventListener) {
		DownloadWorker worker = new DownloadWorker(saveFileName, server, maps);
		if (worker != null) {
			worker.setDownloadWorkerEventListener(downloadWorkerEventListener);
		}
		this.maneger.executeOnThreadPool(worker);
	}

	/**
	 * 全ての動作を止める。
	 * @deprecated 未実装
	 */
	public void stop() {
		throw new UnsupportedOperationException("未実装");
	}

	/**
	 * メッセージを受信した時に呼ばれるイベントのハンドラをセットする。<br>
	 * 受信した全てのメッセージに対してイベントは呼ばれる。
	 * @param messageReceiveListener Ping,Pong,Query,QueryHit,Pushメッセージ受信時に呼ばれるイベントのハンドラ
	 * @return セットに成功したかどうか
	 */
	public boolean addMessageReceiveListener(MessageReceiveListener messageReceiveListener) {
		return addMessageReceiveListener(null, messageReceiveListener);
	}

	/**
	 * メッセージを受信した時に呼ばれるイベントのハンドラをセットする。<br>
	 * メッセージが指定したGUIDをもつときのみイベントが呼ばれる。
	 * @param filterGUID フィルターするGUID
	 * @param messageReceiveListener 指定したGUIDを持つPing,Pong,Query,QueryHit,Pushメッセージ受信時に呼ばれるイベントのハンドラ
	 * @return セットに成功したかどうか
	 */
	public boolean addMessageReceiveListener(GUID filterGUID, MessageReceiveListener messageReceiveListener) {
		return this.maneger.addMessageReceiveListener(filterGUID, messageReceiveListener);
	}

	/**
	 * メッセージを受信した時に呼ばれるイベントのハンドラを削除する。
	 * @param messageReceiveListener 削除するイベントハンドラ
	 * @return 削除に成功したかどうか
	 */
	public boolean removeMessageReceiveListener(MessageReceiveListener messageReceiveListener) {
		return this.maneger.removeMessageReceiveListener(messageReceiveListener);
	}

	/**
	 * 指定したGUIDでフィルタリングしているイベントハンドラを全て削除する
	 * @param filterGUID 削除対象のイベントハンドラがフィルタリングしているGUID
	 * @return 削除に成功したかどうか
	 */
	public List<MessageReceiveListener> removeMessageReceiveListener(GUID filterGUID) {
		return this.maneger.removeMessageReceiveListener(filterGUID);
	}

	/**
	 * 指定したGUIDでフィルタリングしている特定のイベントハンドラを削除する
	 * @param filterGUID 削除対象のイベントハンドラがフィルタリングしているGUID
	 * @param messageReceiveListener 削除するイベントハンドラ
	 * @return 削除に成功したかどうか
	 */
	public boolean removeMessageReceiveListener(GUID filterGUID, MessageReceiveListener messageReceiveListener) {
		return this.maneger.removeMessageReceiveListener(filterGUID, messageReceiveListener);
	}

	/**
	 * 他ノードからの接続時、他ノードとの接続切断時、エラー発生時に呼ばれるイベントのハンドラをセットする
	 * @param connectionEventListener 他ノードからの接続時、他ノードとの接続切断時、エラー発生時に呼ばれるイベントのハンドラ
	 */
	public void setConnectionEventListener(ConnectionEventListener connectionEventListener) {
		this.maneger.setConnectionEventListener(connectionEventListener);
	}

	/**
	 * Gnutellaネットワークに申告する自身の最大アップロード速度(KB/s)を設定する<br>
	 * Queryメッセージ受信時の応答条件と、QueryHitメッセージで用いられる
	 * @param speed 自身の最大アップロード速度(KB/s)
	 */
	public void setSpeed(int speed) {
		this.maneger.setSpeed(speed);
	}

	/**
	 * 現在設定中のGnutellaネットワークに申告する自身の最大アップロード速度(KB/s)を取得する<br>
	 * @return 自身の最大アップロード速度(KB/s)
	 */
	public int getSpped() {
		return this.maneger.getSpeed();
	}

	/**
	 * メッセージ受信用に用いているポート番号を取得する
	 * @return ポート番号
	 */
	public int getPort() {
		return this.maneger.getPort();
	}

	/**
	 * 自身のUIDを取得する
	 * @return 自身のUID
	 */
	public GUID getUID() {
		return this.maneger.getUID();
	}

	/**
	 * TCP/IP接続で直接接続している隣接ノードを取得する
	 * @return TCP/IP接続で直接接続している隣接ノード
	 */
	public Host[] getNeighborHosts() {
		return this.maneger.getHostContainer().getNeighborHosts();
	}
	
	public Host[] getOnlineHosts(){
		return this.maneger.getHostContainer().getOnlineHosts();
	}

	/**
	 * 現在自身が共有可能しているファイルの一覧を取得する
	 * @return 現在自身が共有可能しているファイルの一覧
	 */
	public SharedFile[] getSharedFiles() {
		return this.maneger.getSharedFileContainer().getsharedFileArray();
	}

	/**
	 * 自身が共有可能にしているファイル一覧から特定のファイルを削除する
	 * @param filePath 自身が共有可能にしているファイル一覧から削除するファイルのパス
	 * @return 削除に成功したかどうか
	 */
	public boolean removeSharedFile(String filePath) {
		return this.maneger.getSharedFileContainer().removeSharedFileByFilePath(filePath);
	}
}
