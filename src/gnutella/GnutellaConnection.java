package gnutella;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class GnutellaConnection extends Connection {
	public static final String GNUTELLA_CONNECT = "GNUTELLA CONNECT/0.4\n\n";
	public static final int GNUTELLA_CONNECT_LENGTH = GNUTELLA_CONNECT.getBytes().length;
	public static final String GNUTELLA_OK = "GNUTELLA OK\n\n";
	public static final int GNUTELLA_OK_LENGTH = GNUTELLA_OK.getBytes().length;

	public enum ConnectionStateType {
		COLSE, CONNECTING, CONNECT
	}

	private ConnectionStateType connectionState = ConnectionStateType.COLSE;

	public GnutellaConnection() {
	}

	public GnutellaConnection(Socket socket) {
		super(socket);
	}

	@Override
	public void initConnect(InetAddress address, int port) throws IOException {
		connect(address, port);
		setConnectionState(ConnectionStateType.CONNECTING);
		sendString(GNUTELLA_CONNECT);
	}

	public ConnectionStateType getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(ConnectionStateType connectionState) {
		this.connectionState = connectionState;
	}
}
