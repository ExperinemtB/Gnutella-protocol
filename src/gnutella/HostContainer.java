package gnutella;

import gnutella.Host.HostType;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class HostContainer {
	private ConcurrentHashMap<InetSocketAddress, Host> hostMap;

	public HostContainer() {
		this.hostMap = new ConcurrentHashMap<InetSocketAddress, Host>();
	}

	public void addHost(InetSocketAddress address, Host host) {
		this.hostMap.put(address, host);
	}

	/**
	 * 隣接はしていないがPongで知り得たホストのインスタンスを生成する
	 * @param address ホストの接続受信可能なIPAddress, Port
	 * @return
	 */
	public Host createNetworkHost(InetSocketAddress address) {
		Host host = new Host(address);
		host.setHostType(HostType.NETWORK);
		this.hostMap.put(address, host);
		return host;
	}

	/**
	 * 隣接しているホストのインスタンスを生成する
	 * @param address ホストの接続受信可能なIPAddress, Port
	 * @param gnutellaConnection そのホストとの接続に使われるConnection
	 * @return
	 */
	public Host createNeighborHost(InetSocketAddress address, GnutellaConnection gnutellaConnection) {
		Host host = new Host(address, gnutellaConnection);
		host.setHostType(HostType.NEIGHBOR);
		this.hostMap.put(address, host);
		return host;
	}
	
	public Host createFileTransportHost(InetSocketAddress address, GnutellaConnection gnutellaConnection) {
		Host host = new Host(address, gnutellaConnection);
		host.setHostType(HostType.FILETRANSPORT);
		return host;
	}

	/**
	 * 隣接しているHost一覧を取得する
	 * @return 隣接しているHostを要素に持つ配列
	 */
	public Host[] getNeighborHosts() {
		List<Host> neighborList = new ArrayList<Host>();
		for (Entry<InetSocketAddress, Host> set : this.hostMap.entrySet()) {
			if (set.getValue().getHostType() == HostType.NEIGHBOR) {
				neighborList.add(set.getValue());
			}
		}
		return neighborList.toArray(new Host[] {});
	}

	public Host getHostByAddress(InetSocketAddress address) {
		return hostMap.get(address);
	}

	public Host removeByAddress(InetSocketAddress address) {
		return hostMap.remove(address);
	}
}
