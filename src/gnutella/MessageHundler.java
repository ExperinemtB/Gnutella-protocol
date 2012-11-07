package gnutella;

import gnutella.message.Header;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.share.SharedFileContainer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MessageHundler {
	private RoutingTable routingTable;
	private SharedFileContainer sharedFileContainer;
	private HostContainer hostContainer;

	public MessageHundler() {
		GnutellaManeger maneger = GnutellaManeger.getInstance();
		this.sharedFileContainer = maneger.getSharedFileContainer();
		this.routingTable = maneger.getRoutingTable();
		this.hostContainer = GnutellaManeger.getInstance().getHostContainer();
	}

	public boolean hundlePingMessage(PingMessage pingMessage, Host remoteHost) {
		boolean accepted = true;
		Header pingHeader = pingMessage.getHeader();

		if (routingTable.isMessageAlreadyReceived(pingHeader.getGuid(), pingHeader.getPayloadDescriptor())) {
			System.out.println("isMessageAlreadyReceived");
			return false;
		}
		

		// Pongを返す
		try {
			Header pongHeader = new Header(pingHeader.getGuid(), Header.PONG, (byte) (pingHeader.getHops()+1),(byte)1, PongMessage.LENGTH);
			int numberOfFiles = sharedFileContainer.getFileCount();
			int numberOfKilobytes = sharedFileContainer.getTotalFileSizeKb();
			
			//pongのportは待受ポート
			PongMessage pong = new PongMessage(pongHeader, (char)GnutellaManeger.getInstance().getPort(), Inet4Address.getLocalHost(), numberOfFiles, numberOfKilobytes);
			remoteHost.getConnection().sendMessage(pong);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Pingを隣接ノードに送る
		int newTTL = pingHeader.getTtl() - 1;
		if (newTTL < 1) {
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
		//////////Pongの内容をローカルに保存する
		// 隣接ノードの自身のPingに対する応答の時
		if (pongMessage.getHeader().getHops() == 1) {
			InetSocketAddress connectedAddress = remoteHost.getAddress();
			InetAddress ipAddress = connectedAddress.getAddress();
			if (ipAddress.equals(pongMessage.getIpAddress())) {
				//隣接ノードが発したPongの時
				remoteHost.setSharedFileCount(pongMessage.getNumberOfFilesShared());
				remoteHost.setSharedFileTotalSizeKb(pongMessage.getNumberOfKilobytesShared());
				
				//hostのPortを接続可能な待受ポートに正しく設定し直す
				int connectedPort = connectedAddress.getPort();
				int pongPort = pongMessage.getPort();
				if (connectedPort != pongPort) {
					//相手からの接続で通信が始まった場合はhostのportは待受ポートに設定されていない
					//Pong中のportは待ち受けポートを表す
					remoteHost.setAddress(new InetSocketAddress(connectedAddress.getAddress(), pongPort));
				}
			}
		}
		else{
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
			return false;
		}

		pongHeader.setTtl((byte) newTTL);
		pongHeader.setHops((byte) (pongHeader.getHops() + 1));
		pongMessage.setHeader(pongHeader);
		
		
		Host toSendHost = routingTable.getNextHost(pongMessage);
		try {
			if (toSendHost != null) {
				toSendHost.getConnection().sendMessage(pongMessage);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
}
