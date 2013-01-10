package gnutella;

import gnutella.GnutellaConnection.ConnectionStateType;
import gnutella.listener.ConnectionEventListener;
import gnutella.message.Header;
import gnutella.message.Message;
import gnutella.message.MessageParser;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConnectionDataReceiver implements Runnable {
	private GnutellaManeger maneger = GnutellaManeger.getInstance();
	private List<Byte> receiveByteQueue;
	private final static int BUFSIZE = 1024;
	private Host remoteHost;

	public ConnectionDataReceiver(Host remoteHost) {
		this.receiveByteQueue = new ArrayList<Byte>();
		this.remoteHost = remoteHost;
	}

	public synchronized void receiveData() throws IOException {
		byte[] byteBuffer = new byte[BUFSIZE];
		InputStream is = remoteHost.getConnection().getInputStream();
		int length = 0;
		while ((length = is.read(byteBuffer)) != -1) {
			System.out.println(String.format("\nreceive:%s from:%s:%d", new String(byteBuffer), remoteHost.getConnection().getInetAddress().getHostAddress(), remoteHost.getConnection().getPort()));

			for (int i = 0; i < length; i++) {
				receiveByteQueue.add(byteBuffer[i]);
			}
			try {
				parseData();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			byteBuffer = new byte[BUFSIZE];
		}
		is.close();
	}

	private synchronized void parseData() {
		ConnectionStateType connectionState = ((GnutellaConnection) remoteHost.getConnection()).getConnectionState();
		if (connectionState == ConnectionStateType.COLSE) {
			if (this.receiveByteQueue.size() >= GnutellaConnection.GNUTELLA_CONNECT_LENGTH) {
				int readLength = GnutellaConnection.GNUTELLA_CONNECT_LENGTH;
				String receiveString = new String(toPrimitive(receiveByteQueue.subList(0, readLength).toArray(new Byte[] {})));

				if (receiveString.equals(GnutellaConnection.GNUTELLA_CONNECT)) {
					try {
						System.out.println("Connection Request Received");

						remoteHost.getConnection().sendString(GnutellaConnection.GNUTELLA_OK);
						((GnutellaConnection) remoteHost.getConnection()).setConnectionState(ConnectionStateType.CONNECT);

						ConnectionEventListener connectionEventListener = maneger.getConnectionEventListener();
						if (connectionEventListener != null) {
							maneger.getConnectionEventListener().onConnect(remoteHost);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				this.receiveByteQueue = this.receiveByteQueue.subList(readLength, this.receiveByteQueue.size());
				
				//残りのバッファ部分もパースできないか確認する
				parseData();
			}
		} else if (connectionState == ConnectionStateType.CONNECTING) {
			if (this.receiveByteQueue.size() >= GnutellaConnection.GNUTELLA_OK_LENGTH) {
				int readLength = GnutellaConnection.GNUTELLA_OK_LENGTH;
				String receiveString = new String(toPrimitive(receiveByteQueue.subList(0, readLength).toArray(new Byte[] {})));
				if (receiveString.equals(GnutellaConnection.GNUTELLA_OK)) {
					System.out.println("Connection Accepted");

					((GnutellaConnection) remoteHost.getConnection()).setConnectionState(ConnectionStateType.CONNECT);

					ConnectionEventListener connectionEventListener = maneger.getConnectionEventListener();
					if (connectionEventListener != null) {
						maneger.getConnectionEventListener().onConnect(remoteHost);
					}
				}

				this.receiveByteQueue = this.receiveByteQueue.subList(readLength, this.receiveByteQueue.size());

				//残りのバッファ部分もパースできないか確認する
				parseData();
			}
		} else if (connectionState == ConnectionStateType.CONNECT) {

			if (this.receiveByteQueue.size() >= Header.HEADER_LENGTH) {
				Message message = MessageParser.parse(toPrimitive(receiveByteQueue.toArray(new Byte[] {})));
				if (message == null) {
					System.out.println("invalid message");
					return;
				}
				System.out.println("receive:" + message.toString());

				int readLength = Header.HEADER_LENGTH + message.getHeader().getPayloadLength();
				this.receiveByteQueue = this.receiveByteQueue.subList(readLength, this.receiveByteQueue.size());

				boolean accepted = false;
				MessageHundler hundler = new MessageHundler();
				switch (message.getHeader().getPayloadDescriptor()) {
				case Header.PING:
					accepted = hundler.hundlePingMessage((PingMessage) message, remoteHost);
					break;
				case Header.PONG:
					accepted = hundler.hundlePongMessage((PongMessage) message, remoteHost);
					break;
				case Header.QUERY:
					accepted = hundler.hundleQueryMessage((QueryMessage) message, remoteHost);
					break;
				case Header.QUERYHIT:
					accepted = hundler.hundleQueryHitMessage((QueryHitMessage) message, remoteHost);
					break;
				case Header.PUSH:
					accepted = hundler.hundlePushMessage((PushMessage) message, remoteHost);
					break;
				default:
					break;
				}

				if (accepted) {
					GnutellaManeger.getInstance().getRoutingTable().add(remoteHost, message);
				}
				//残りのバッファ部分もパースできないか確認する
				parseData();
			}
		}
	}

	public static byte[] toPrimitive(Byte[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return new byte[0];
		}
		final byte[] result = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].byteValue();
		}
		return result;
	}

	@Override
	public void run() {
		try {
			receiveData();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
			// 想定していない例外のためイベント発火
			ConnectionEventListener connectionEventListener = maneger.getConnectionEventListener();
			if (connectionEventListener != null) {
				connectionEventListener.onThrowable(ex);
			}
		}
		Host deletedHost = maneger.getHostContainer().removeByAddress(remoteHost.getAddress());
		if (deletedHost != null) {
			((GnutellaConnection) deletedHost.getConnection()).setConnectionState(ConnectionStateType.COLSE);
			ConnectionEventListener connectionEventListener = maneger.getConnectionEventListener();
			if (connectionEventListener != null) {
				maneger.getConnectionEventListener().onClose(deletedHost);
			}
		}

	}
}
