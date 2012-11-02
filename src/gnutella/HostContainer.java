package gnutella;

import gnutella.Host.HostType;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class HostContainer {
	private List<Host> hostList;

	public HostContainer() {
		this.hostList = new ArrayList<Host>();
	}

	public void addHost(Host host) {
		this.hostList.add(host);
	}

	/**
	 * 隣接はしていないがPongで知り得たホストのインスタンスを生成する
	 * @param address ホストのIPAddress, Port
	 * @return
	 */
	public Host createNetworkHost(InetSocketAddress address) {
		Host host = new Host(address);
		host.setHostType(HostType.NETWORK);
		this.hostList.add(host);
		return host;
	}

	/**
	 * 隣接しているホストのインスタンスを生成する
	 * @param address ホストのIPAddress, Port
	 * @param connection そのホストとの接続に使われるConnection
	 * @return
	 */
	public Host createNeighborHost(InetSocketAddress address, Connection connection) {
		Host host = new Host(address, connection);
		host.setHostType(HostType.NEIGHBOR);
		this.hostList.add(host);
		return host;
	}
	
	/**
	 * 隣接しているHost一覧を取得する
	 * @return 隣接しているHostを要素に持つ配列
	 */
	public Host[] getNeighborHosts(){
		List<Host> neighborList = new ArrayList<Host>();
		for(Host host:this.hostList){
			if(host.getHostType() == HostType.NEIGHBOR){
				neighborList.add(host);
			}
		}
		return neighborList.toArray(new Host[]{});
	}
}
