package gnutella;

import gnutella.Connection.ConnectionStateType;
import gnutella.message.Header;
import gnutella.message.Message;
import gnutella.message.MessageParser;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionDataReceiver implements Runnable {
	private List<Byte> receiveByteQueue;
	private final static int BUFSIZE = 1024;
	private Host remoteHost;

	public ConnectionDataReceiver(Host remoteHost) {
		this.receiveByteQueue = new ArrayList<Byte>();
		this.remoteHost = remoteHost;
	}

	public synchronized void receiveData() throws IOException {
		byte[] byteBuffer = new byte[BUFSIZE];
		Connection connection = remoteHost.getConnection();
		InputStream is = connection.getInputStream();
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
		throw new SocketException("Connection closed");
	}

	private synchronized void parseData() {
		ConnectionStateType connectionState = remoteHost.getConnection().getConnectionState();
		if (connectionState == ConnectionStateType.COLSE) {
			if (this.receiveByteQueue.size() >= Connection.GNUTELLA_CONNECT_LENGTH) {
				int readLength = Connection.GNUTELLA_CONNECT_LENGTH;
				String receiveString = new String(toPrimitive(receiveByteQueue.subList(0, readLength).toArray(new Byte[] {})));

				if (receiveString.equals(Connection.GNUTELLA_CONNECT)) {
					try {
						System.out.println("Connection Request Received");

						remoteHost.getConnection().sendString(Connection.GNUTELLA_OK);
						remoteHost.getConnection().setConnectionState(ConnectionStateType.CONNECT);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				this.receiveByteQueue = this.receiveByteQueue.subList(readLength, this.receiveByteQueue.size());
			}
		} else if (connectionState == ConnectionStateType.CONNECTING) {
			if (this.receiveByteQueue.size() >= Connection.GNUTELLA_OK_LENGTH) {
				int readLength = Connection.GNUTELLA_OK_LENGTH;
				String receiveString = new String(toPrimitive(receiveByteQueue.subList(0, readLength).toArray(new Byte[] {})));
				if (receiveString.equals(Connection.GNUTELLA_OK)) {
					System.out.println("Connection Accepted");

					remoteHost.getConnection().setConnectionState(ConnectionStateType.CONNECT);
				}

				this.receiveByteQueue = this.receiveByteQueue.subList(readLength, this.receiveByteQueue.size());
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
					break;
				case Header.QUERYHIT:
					break;
				case Header.PUSH:
					break;
				default:
					break;
				}

				if (accepted) {
					GnutellaManeger.getInstance().getRoutingTable().add(remoteHost, message.getHeader().getGuid(), message.getHeader().getPayloadDescriptor());
				}

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
		}
		GnutellaManeger.getInstance().getHostContainer().removeByAddress(remoteHost.getAddress());
		remoteHost.getConnection().setConnectionState(ConnectionStateType.COLSE);
	}
}
