package gnutella;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class DownloadConnection extends GnutellaConnection {
	public static final String HTTP_GET_REQUEST_PREFIX = "GET";
	public static final String HTTP_GET_REQUEST_PATTERN = "GET /get/([0-9]+?)/(.+?)/ HTTP/1\\.0";
	public static final String HTTP_RESPONSE_PATTERN = "HTTP ([0-9]+?) (.*?)";
	public static final String HTTP_GIV_REQUEST_PREFIX = "GIV";
	public static final String HTTP_GIV_REQUEST_PATTERN = "GIV ([0-9]+?):(.+?)/(.+?)";

	public DownloadConnection() {
	}

	public DownloadConnection(Socket socket) {
		super(socket);
	}

	@Override
	public void initConnect(InetAddress address, int port) {
		try {
			connect(address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendHttpGetRequest(int fileIndex, String fileName) throws IOException {
		String httpGetString = generateHttpGetMessage(fileIndex, fileName);
		System.out.println("send http get request:" + httpGetString);
		sendBytes(httpGetString.getBytes());
	}

	public void sendHttpGivRequest(String serventIdentifierString, int fileIndex, String fileName) throws IOException {
		String httpGivString = generateHttpGivMessage(serventIdentifierString, fileIndex, fileName);
		System.out.println("send http giv request:" + httpGivString);
		sendBytes(httpGivString.getBytes());
	}

	private static String generateHttpGetMessage(int fileIndex, String fileName) {
		// GET /get/<File Index>/<File Name>/ HTTP/1.0\r\n
		// Connection: Keep-Alive\r\n
		// \r\n
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("GET /get/%d/%s/ HTTP/1.0\r\n", fileIndex, fileName));
		sb.append("Connection: Keep-Alive\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	private static String generateHttpGivMessage(String serventIdentifierString, int fileIndex, String fileName) {
		// GIV <File Index>:<Servent Identifier>/<File Name>\n\n
		return String.format("GIV %d:%s/%s\n\n", fileIndex, serventIdentifierString, fileName);
	}
}
