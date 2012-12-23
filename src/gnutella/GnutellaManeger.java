package gnutella;

import gnutella.listener.ConnectionEventListener;
import gnutella.listener.MessageReceiveListener;
import gnutella.message.GUID;
import gnutella.share.SharedFileContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GnutellaManeger {
	private static GnutellaManeger gnutellaManeger;
	private ExecutorService executor;
	private RoutingTable routingTable;
	private HostContainer hostContainer;
	private SharedFileContainer sharedFileContainer;
	private Map<GUID,List<MessageReceiveListener>> messageReceiveListenerSet;
	private ConnectionEventListener connectionEventListener;

	private final int MAX_POOL = 30;
	private final int MAX_SERVER_EXECUTE = 2;

	// private int Port = 50000;
	private int Port;
	private int speed = 0;
	private GUID UID;

	private GnutellaManeger() {
		this.executor = Executors.newFixedThreadPool(MAX_POOL);
		this.routingTable = new RoutingTable();
		this.hostContainer = new HostContainer();
		this.sharedFileContainer = new SharedFileContainer();
		this.messageReceiveListenerSet = new HashMap<GUID,List<MessageReceiveListener>>();
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

	public boolean addMessageReceiveListener(MessageReceiveListener messageReceiveListener) {
		return addMessageReceiveListener(null,messageReceiveListener);
	}
	
	public boolean addMessageReceiveListener(GUID filterGUID,MessageReceiveListener messageReceiveListener) {
		List<MessageReceiveListener> messageReceiveListenerList =  this.messageReceiveListenerSet.get(filterGUID);
		if(messageReceiveListenerList == null){
			messageReceiveListenerList = new ArrayList<MessageReceiveListener>();
			this.messageReceiveListenerSet.put(filterGUID, messageReceiveListenerList);
		}
		return messageReceiveListenerList.add(messageReceiveListener);
	}

	public List<MessageReceiveListener> getMessageReceiveListeners() {
		return getMessageReceiveListeners(null);
	}	
	
	public List<MessageReceiveListener> getMessageReceiveListeners(GUID filterGUID) {
		List<MessageReceiveListener> result = messageReceiveListenerSet.get(filterGUID);
		if(result==null){
			return Collections.emptyList();
		}else{
			return result;
		}
	}	
	public boolean removeMessageReceiveListener(MessageReceiveListener messageReceiveListener){
		List<MessageReceiveListener> messageReceiveListenerList =  this.messageReceiveListenerSet.get(null);
		if(messageReceiveListenerList == null){
			return false;
		}
		return messageReceiveListenerList.remove(messageReceiveListener);
	}
	public List<MessageReceiveListener> removeMessageReceiveListener(GUID filterGUID){
		return this.messageReceiveListenerSet.remove(filterGUID);
	}
	public boolean removeMessageReceiveListener(GUID filterGUID,MessageReceiveListener messageReceiveListener){
		List<MessageReceiveListener> messageReceiveListenerList =  this.messageReceiveListenerSet.get(filterGUID);
		if(messageReceiveListenerList == null){
			return false;
		}
		return messageReceiveListenerList.remove(messageReceiveListener);
	}

	public ConnectionEventListener getConnectionEventListener() {
		return connectionEventListener;
	}

	public void setConnectionEventListener(ConnectionEventListener connectionEventListener) {
		this.connectionEventListener = connectionEventListener;
	}

	public int getMAX_SERVER_EXECUTE() {
		return MAX_SERVER_EXECUTE;
	}	
}
