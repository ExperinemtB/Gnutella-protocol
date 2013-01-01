package gnutella.gui;

import gnutella.DownloadWorker;
import gnutella.GnutellaServant;
import gnutella.Host;
import gnutella.listener.DownloadWorkerEventListener;
import gnutella.listener.MessageReceiveListener;
import gnutella.listener.ServerEventListener;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;

import java.io.File;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Observable;

public class Model extends Observable{

	private String stateMessage;

	private int port;
	private String keyword;
	private int minimumSpeedKB;
	private String path;
	private List<SimpleEntry<Integer, QueryHitMessage>> maps;
	private String portTextFieldString;

	private GnutellaServant servant;


	public Model(){
		this.servant = new GnutellaServant();
		this.servant.addMessageReceiveListener(new MessageReceiveListener() {

			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {

			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}
	public void start() {
		setStateMessage(String.format("ポート%dでサーバ起動開始", this.port));
		setChanged();
		notifyObservers();
		this.servant.start(this.port, new ServerEventListener(){

			@Override
			public void onStart(int port, InetAddress address) {

			}

			@Override
			public void onThrowable(Throwable throwable) {

			}

			@Override
			public void onStop() {

			}

		});
	}

	public void sendPing() {
		setStateMessage("ping送信");
		setChanged();
		notifyObservers();
		this.servant.sendPing(new MessageReceiveListener(){

			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {

			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}

	public void sendQuery() {
		setStateMessage("query送信");
		setChanged();
		notifyObservers();
		this.servant.sendQuery(this.keyword, this.minimumSpeedKB, new MessageReceiveListener(){

			@Override
			public void onReceivePingMessage(PingMessage ping, Host remoteHost) {

			}

			@Override
			public void onReceivePongMessage(PongMessage pong, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryMessage(QueryMessage query, Host remoteHost) {

			}

			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost) {

			}

			@Override
			public void onReceivePushMessage(PushMessage push, Host remoteHost) {

			}

		});
	}

	public void addFile() {
		setStateMessage("addFile");
		setChanged();
		notifyObservers();
		this.servant.addFile(this.path);
	}

	public void sendDownloadRequest() {
		setStateMessage("DownloadRequest送信");
		setChanged();
		notifyObservers();
		this.servant.sendDownloadRequest(this.maps, new DownloadWorkerEventListener(){
			@Override
			public void onComplete(DownloadWorker eventSource, int fileIndex, File file) {
			}

			@Override
			public void onThrowable(DownloadWorker eventSource, Throwable throwable) {
			}

			@Override
			public void onReceiveData(DownloadWorker eventSource, String fileName, long totalReceivedLength, long totalFileLength) {
			}
		});
	}

	public void setStateMessage(String stateMessage){
		this.stateMessage = stateMessage;
	}

	public String getStateMessage() {
		return stateMessage;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setfileName(String keyword) {
		this.keyword = keyword;
	}

	public void setSpeed(int miniSpeedKB){
		this.minimumSpeedKB = miniSpeedKB;
	}

	public void setPath(String path){
		this.path = path;
	}

	public void setMaps(List<SimpleEntry<Integer, QueryHitMessage>> maps){
		this.maps = maps;
	}

	public void setPortTextFieldString(String str){
		this.portTextFieldString = str;
	}

	public String getPortTextFieldString(){
		return portTextFieldString;
	}


}
