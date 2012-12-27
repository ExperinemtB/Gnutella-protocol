package gnutella.gui;

import gnutella.GnutellaServant;
import gnutella.listener.DownloadWorkerEventListener;
import gnutella.listener.MessageReceiveListener;
import gnutella.listener.ServerEventListener;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;

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

	private GnutellaServant servant;
	private ServerEventListener serverEventListener;
	private MessageReceiveListener messageReceiveListener;
	private DownloadWorkerEventListener downloadWorkerEventListener;

	public Model(){
		this.servant = new GnutellaServant();
		this.servant.addMessageReceiveListener(new MessageReceiveListener()) {
			@Override
			public void onReceivePingMessage(PingMessage ping) {
			}
			@Override
			public void onReceivePongMessage(PongMessage pong) {
				setStateMessage("pongを受信");
				setChanged();
				notifyObservers();
			}
			@Override
			public void onReceiveQueryMessage(QueryMessage query) {
				setStateMessage("queryを受信");
				setChanged();
				notifyObservers();
			}
			@Override
			public void onReceiveQueryHitMessage(QueryHitMessage queryHit) {
				setStateMessage("queryHitを受信");
				setChanged();
				notifyObservers();
			}
		}
	}
	public void start() {
		setStateMessage(String.format("ポート%dでサーバ起動開始", this.port));
		setChanged();
		notifyObservers();
		this.servant.start(this.port, serverEventListener);
	}

	public void sendPing() {
		setStateMessage("ping送信");
		setChanged();
		notifyObservers();
		this.servant.sendPing(messageReceiveListener);
	}

	public void sendQuery() {
		setStateMessage("query送信");
		setChanged();
		notifyObservers();
		this.servant.sendQuery(this.keyword, this.minimumSpeedKB, messageReceiveListener);
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
		this.servant.sendDownloadRequest(this.maps, downloadWorkerEventListener);
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

	public void setfileName(String Keyword) {
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


}
