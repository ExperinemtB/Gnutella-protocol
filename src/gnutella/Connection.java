package gnutella;

import gnutella.message.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Connection {
	public static final String GNUTELLA_CONNECT = "GNUTELLA CONNECT/0.4\n\n";
	public static final int GNUTELLA_CONNECT_LENGTH = GNUTELLA_CONNECT.getBytes().length;
	public static final String GNUTELLA_OK = "GNUTELLA OK\n\n";
	public static final int GNUTELLA_OK_LENGTH = GNUTELLA_OK.getBytes().length;
	

	public enum ConnectionStateType {
		COLSE, CONNECTING, CONNECT
	}

	private Socket socket;
	private ConnectionStateType connectionState = ConnectionStateType.COLSE;

	public Connection() {
	}

	public Connection(Socket socket) {
		this.socket = socket;
	}

	private void connect(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);
	}

	public void initConnect(InetAddress address, int port) {
		try {
			connect(address, port);
			setConnectionState(ConnectionStateType.CONNECTING);
			sendString(GNUTELLA_CONNECT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	public ConnectionStateType getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(ConnectionStateType connectionState) {
		this.connectionState = connectionState;
	}

	public void sendMessage(Message message) throws IOException {
		OutputStream os = this.socket.getOutputStream();
		os.write(message.getBytes());
	}

	public void sendString(String str) throws IOException {
		OutputStream os = this.socket.getOutputStream();
		os.write(str.getBytes());
	}
}
