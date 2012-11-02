package gnutella;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GnutellaManeger {
	private static GnutellaManeger gnutellaManeger;
	private ExecutorService executor;
	private RoutingTable routingTable;
	private HostContainer hostContainer;
	private final int MAX_POOL = 10;

	private GnutellaManeger() {
		this.executor = Executors.newFixedThreadPool(MAX_POOL);
		this.routingTable = new RoutingTable();
		this.hostContainer = new HostContainer();
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

}
