package gnutella;

import gnutella.share.SharedFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHundler {
	public enum Status {
		OK(200, "OK"),
		CREATED(201, "Created"),
		ACCEPTED(202, "Accepted"),
		NO_CONTENT(204, "No Content"),
		MOVED_PERMANENTLY(301, "Moved Permanently"),
		SEE_OTHER(303, "See Other"),
		NOT_MODIFIED(304, "Not Modified"),
		TEMPORARY_REDIRECT(307, "Temporary Redirect"),
		BAD_REQUEST(400, "Bad Request"),
		UNAUTHORIZED(401, "Unauthorized"),
		FORBIDDEN(403, "Forbidden"),
		NOT_FOUND(404, "Not Found"),
		NOT_ACCEPTABLE(406, "Not Acceptable"),
		CONFLICT(409, "Conflict"), GONE(410, "Gone"),
		PRECONDITION_FAILED(412, "Precondition Failed"),
		UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
		INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
		SERVICE_UNAVAILABLE(503, "Service Unavailable");

		private final int code;
		private final String reason;

		Status(final int statusCode, final String reasonPhrase) {
			this.code = statusCode;
			this.reason = reasonPhrase;
		}

		public int getStatusCode() {
			return code;
		}

		@Override
		public String toString() {
			return reason;
		}

		public static Status fromStatusCode(final int statusCode) {
			for (Status s : Status.values()) {
				if (s.code == statusCode) {
					return s;
				}
			}
			return null;
		}
	}

	private final static int BUF_SIZE = 65536;

	public static Runnable hundleHttpGetRequest(String requestLine, final Host remoteHost) {
		Matcher match = Pattern.compile(DownloadConnection.HTTP_GET_REQUEST_PATTERN).matcher(requestLine);
		match.matches();
		final int fileIndex = Integer.parseInt(match.group(1));
		// String fileName = match.group(2);

		StringBuffer sb = new StringBuffer();
		String currentLine;
		try {
			while ((currentLine = remoteHost.getConnection().readLine()) != null && currentLine.length() > 0) {
				sb.append(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] lines = sb.toString().split("\r\n");

		HashMap<String, String> httpFieldSet = new HashMap<String, String>();
		for (String line : lines) {
			// フィールド名: 内容
			int separatePos = line.indexOf(":");
			httpFieldSet.put(line.substring(0, separatePos + 1), line.substring(separatePos + 2));
		}

		// HostのConnectionに対してファイルを送る
		return new Runnable() {
			@Override
			public void run() {
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				try {
					SharedFile sharedFile = GnutellaManeger.getInstance().getSharedFileContainer().getSharedFileByFileIndex(fileIndex);

					// Rangeパラメータに応じた内容を返す
					OutputStream os = remoteHost.getConnection().getOutputStream();

					File file = sharedFile.getOriginFile();
					byte[] buf = new byte[BUF_SIZE];
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					int length = -1;

					// ヘッダを書き込む
					String headerString = generateHttpGetRequestResponseHeaderString(Status.OK, file.length());
					os.write(headerString.getBytes());
					while ((length = bis.read(buf)) != -1) {
						os.write(buf, 0, length);
					}
					os.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					try {
						fis.close();
						bis.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};
	}

	public static void hundleHttpGetRequestResponse(final Host remoteHost, String fileName) {
		// HTTP 200 OK\r\n
		// Server: Gnutella\r\n
		// Content-type: application/binary\r\n
		// Content-length: 4356789\r\n
		// \r\n
//		String requestLine = null;
//		try {
//			requestLine = remoteHost.getConnection().readLine();
//		} catch (IOException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
//
//		Matcher match = Pattern.compile(DownloadConnection.HTTP_RESPONSE_PATTERN).matcher(requestLine);
//		match.matches();
//		final int statusCode = Integer.parseInt(match.group(1));

		StringBuffer sb = new StringBuffer();
		String currentLine;
		try {
			while ((currentLine = remoteHost.getConnection().readLine()) != null && currentLine.length() > 0 && !currentLine.equals("\r\n")) {
				sb.append(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] lines = sb.toString().split("\r\n");

		HashMap<String, String> httpFieldSet = new HashMap<String, String>();
		for (String line : lines) {
			// フィールド名: 内容
			int separatePos = line.indexOf(":");
			httpFieldSet.put(line.substring(0, separatePos + 1), line.substring(separatePos + 2));
		}

		// ファイルを受け取る
		try {
			byte[] byteBuffer = new byte[BUF_SIZE];
			InputStream is = remoteHost.getConnection().getInputStream();
			FileOutputStream fos = new FileOutputStream(fileName);

			int length = 0, totalLength = 0;
			while ((length = is.read(byteBuffer)) != -1) {
				System.out.println(String.format("\nreceive:%d from:%s:%d", length, remoteHost.getConnection().getInetAddress().getHostAddress(), remoteHost.getConnection().getPort()));

				fos.write(byteBuffer, 0, length);
				byteBuffer = new byte[BUF_SIZE];
				totalLength += length;
			}
			is.close();
			fos.flush();
			fos.close();
			System.out.println(String.format("FileRecieveCompleat:%s size:%d ", fileName, totalLength));
		} catch (Exception ex) {
		}
		// TODO:ソケットのクローズremoteHost.getConnection().close();
	}

	public static void hundleHttpGivRequest(String requestLine, final Host remoteHost) {
		// GIV <File Index>:<Servent Identifier>/<File Name>\n\n

		Matcher match = Pattern.compile(DownloadConnection.HTTP_GIV_REQUEST_PATTERN).matcher(requestLine);
		match.matches();
		final int fileIndex = Integer.parseInt(match.group(1));
		// final String serventIdentifierHexString = match.group(2);
		final String fileName = match.group(3);

		// HTTP GET 要求を送る
		try {
			((DownloadConnection) remoteHost.getConnection()).sendHttpGetRequest(fileIndex, fileName);
			HttpHundler.hundleHttpGetRequestResponse(remoteHost, fileName);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static void hundleHttpGivRequestResponse(final Host remoteHost) {
		String requestLine = null;
		try {
			requestLine = remoteHost.getConnection().readLine();
			hundleHttpGetRequest(requestLine, remoteHost).run();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private static String generateHttpGetRequestResponseHeaderString(Status status, long contentLength) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("HTTP %d OK\r\n", status.code, status.reason));
		sb.append("Content-type: application/binary\r\n");
		sb.append(String.format("Content-length: %d\r\n", contentLength));
		sb.append("\r\n");
		return sb.toString();
	}
}
