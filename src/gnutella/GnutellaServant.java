package gnutella;

import java.io.BufferedReader;
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

	public void stop() {

	}
}
