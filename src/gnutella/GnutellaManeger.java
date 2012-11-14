package gnutella;

import gnutella.message.GUID;
import gnutella.share.SharedFileContainer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GnutellaManeger {
	private static GnutellaManeger gnutellaManeger;
	private ExecutorService executor;
	private RoutingTable routingTable;
	private HostContainer hostContainer;
	private SharedFileContainer sharedFileContainer;
	private final int MAX_POOL = 10;

	private int Port = 50000;
	private int speed = 0;
	private GUID UID;

	private GnutellaManeger() {
		this.executor = Executors.newFixedThreadPool(MAX_POOL);
		this.routingTable = new RoutingTable();
		this.hostContainer = new HostContainer();
		this.sharedFileContainer = new SharedFileContainer();
	}

	public static GnutellaManeger getInstance() {
		if (gnutellaManeger == null) {
			gnutellaManeger = new GnutellaManeger();
		}
		return gnutellaManeger;
	}

	/**
	 * スレッドプールで実行する
	 * @param runnable
	 */
	public void executeOnThreadPool(Runnable runnable) {
		executor.submit(runnable);
	}

	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public HostContainer getHostContainer() {
		return hostContainer;
	}

	public SharedFileContainer getSharedFileContainer() {
		return sharedFileContainer;
	}

	public int getPort() {
		return Port;
	}

	public void setPort(int port) {
		Port = port;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public GUID getUID() {
		return UID;
	}

	public void setUID(GUID uID) {
		UID = uID;
	}
}
