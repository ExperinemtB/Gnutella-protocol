package gnutella;

import gnutella.message.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Connection {

	private Socket socket;

	public Connection() {
	}

	public Connection(Socket socket) {
		this.socket = socket;
	}

	protected void connect(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);
	}

	public void initConnect(InetAddress address, int port) {
		try {
			connect(address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	public String readLine() throws IOException {
		StringBuffer lineBuffer = new StringBuffer();
		InputStream is = getInputStream();
		int c;
		while (true) {
			c = is.read();
			if (c < 0) {
				if (lineBuffer.length() == 0) {
					return null;
				} else {
					break;
				}
			} else if (c == '\r') {
				continue;
			} else if (c == '\n') {
				break;
			} else {
				lineBuffer.append((char) c);
			}
		}
		return lineBuffer.toString();
	}

	public void sendString(String str) throws IOException {
		System.out.println("sendString:" + str);
		sendBytes(str.getBytes());
	}

	public void sendMessage(Message message) throws IOException {
		System.out.println("sendMessage to:" + String.valueOf(this.getPort()) + " " + message.toString());
		sendBytes(message.getBytes());
	}

	protected void sendBytes(byte[] data) throws IOException {
		OutputStream os = this.getOutputStream();
		os.write(data);
	}

	public int getLocalPort() {
		return socket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	public int getPort() {
		return socket.getPort();
	}

}
