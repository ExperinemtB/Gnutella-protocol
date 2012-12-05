package gnutella;

import gnutella.message.GUID;
import gnutella.message.Header;
import gnutella.message.PingMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GnutellaServant {

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
						servernt.connect(InetAddress.getLocalHost(), Integer.parseInt(cmd[1]));
					} else if (cmd[0].equals("sendPing")) {
						servernt.sendPing();
					} else if (cmd[0].equals("addFile")) {
						servernt.addFile(cmd[1]);
					} else if (cmd[0].equals("sendQuery")) {
						servernt.sendQuery(cmd[1], Integer.parseInt(cmd[2]));
					} else if (cmd[0].equals("sendDownloadRequest")) {
						servernt.sendDownloadRequest(InetAddress.getLocalHost(), Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), cmd[3]);
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

	public void start(int port) {
		Server server = new Server(port);
		GnutellaManeger.getInstance().executeOnThreadPool(server);
	}

	public void connect(InetAddress ipAddress, int port) {
		Client client = new Client(ipAddress, port);
		GnutellaManeger.getInstance().executeOnThreadPool(client);
	}

	public void sendPing() {
		Host[] neighborHosts = GnutellaManeger.getInstance().getHostContainer().getNeighborHosts();
		Header header = new Header(Header.PING, (byte) 7, 0);
		PingMessage ping = new PingMessage(header);
		for (Host host : neighborHosts) {
			try {
				host.getConnection().sendMessage(ping);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendPush(QueryHitMessage queryHit, int fileIndex) {
		GnutellaManeger maneger = GnutellaManeger.getInstance();
		Header header = new Header(queryHit.getHeader().getGuid(), Header.PUSH, (byte) (queryHit.getHeader().getHops() + 1), (byte) 1, PushMessage.LENGTH);
		try {
			PushMessage push = new PushMessage(header, queryHit.getServentIdentifier(), fileIndex, InetAddress.getLocalHost(), (char) maneger.getPort());
			Host nextHost = maneger.getRoutingTable().getNextHost(push);
			nextHost.getConnection().sendMessage(push);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendQuery(String keyword, int minimumSpeedKb) {
		Host[] neighborHosts = GnutellaManeger.getInstance().getHostContainer().getNeighborHosts();
		Header header = new Header(Header.QUERY, (byte) 7, QueryMessage.MIN_LENGTH + keyword.getBytes().length + 1);

		QueryMessage query = new QueryMessage(header, minimumSpeedKb, keyword);
		for (Host host : neighborHosts) {
			try {
				host.getConnection().sendMessage(query);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addFile(String path) {
		File localFile = new File(path);
		Boolean addSuccess = GnutellaManeger.getInstance().getSharedFileContainer().addFile(localFile);
		if (addSuccess) {
			System.out.println("addedFile:" + localFile.getName());
		}
	}

	public void sendDownloadRequest(InetAddress address, int port, int fileIndex, String fileName) {
		// TODO: 対象ファイルと1対1対応するDownloadEnginクラス(Runable)を実行し、その中でHTTP GET/GIVを送ったり、分割DLの管理を行う
		// QueryHitの一覧中から任意の結果を選択し、そのIP:Portに対して新たなコネクションを作成、HTTP GETを試みる。
		// ただし、相手がファイアーウォール内にいるときはGnutellNetwork上でPushメッセージを送り、(向こうからのコネクション作成を待機する)
		// HTTP GETにはFileIndex,FileNameが必要。QueryHitMessageのインスタンスで十分
		
		DownloadClient client = new DownloadClient(address, port, fileIndex, fileName);
		GnutellaManeger.getInstance().executeOnThreadPool(client);
	}

	public void stop() {

	}
}
