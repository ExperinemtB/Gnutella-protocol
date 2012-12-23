package gnutella;

import gnutella.listener.HttpEventListener;
import gnutella.message.GUID;
import gnutella.share.SharedFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private final static int BUF_SIZE = 65535;
	private HttpEventListener httpGetRequestResponseEventListener;
	private Map<SimpleEntry<GUID, Integer>, HttpEventListener> httpGivRequestEventListener;

	public HttpHundler() {
		this.httpGivRequestEventListener = new HashMap<SimpleEntry<GUID, Integer>, HttpEventListener>();
	}

	public static Runnable hundleHttpGetRequest(String requestLine, final Host remoteHost) {
		Matcher match = Pattern.compile(DownloadConnection.HTTP_GET_REQUEST_PATTERN).matcher(requestLine);
		match.matches();
		final int fileIndex = Integer.parseInt(match.group(1));
		// String fileName = match.group(2);

		List<String> lines = new ArrayList<String>();
		String currentLine;
		try {
			while ((currentLine = remoteHost.getConnection().readLine()) != null && currentLine.length() > 0) {
				lines.add(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		HashMap<String, String> httpFieldSet = new HashMap<String, String>();
		for (String line : lines) {
			// フィールド名: 内容
			int separatePos = line.indexOf(":");
			httpFieldSet.put(line.substring(0, separatePos).toLowerCase(), line.substring(separatePos + 2));
		}
		final Range range = parseRange(httpFieldSet.get("range"));

		// HostのConnectionに対してファイルを送る
		return new Runnable() {
			@Override
			public void run() {
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				int sendLength = 0;
				try {
					SharedFile sharedFile = GnutellaManeger.getInstance().getSharedFileContainer().getSharedFileByFileIndex(fileIndex);
					OutputStream os = remoteHost.getConnection().getOutputStream();

					File file = sharedFile.getOriginFile();
					byte[] buf = new byte[BUF_SIZE];
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					int length = -1;

					// ヘッダを書き込む
					long offset = range.start != null ? range.start : 0;
					long leftLength = range.end != null ? range.end - offset : file.length() - offset;
					range.length = file.length();
					String headerString = generateHttpGetRequestResponseHeaderString(Status.OK, leftLength, range);
					os.write(headerString.getBytes());

					// Rangeパラメータに応じた量を返す
					bis.skip(offset);
					while (leftLength > 0 && (length = bis.read(buf, 0, Math.min((int) leftLength, buf.length))) != -1) {
						os.write(buf, 0, length);
						leftLength -= length;
						sendLength += length;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					try {
						fis.close();
						bis.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					System.out.println(String.format("sendLength:%d", sendLength));
				}
			}
		};
	}

	public File hundleHttpGetRequestResponse(final Host remoteHost, String fileName) {
		// HTTP 200 OK\r\n
		// Server: Gnutella\r\n
		// Content-type: application/binary\r\n
		// Content-length: 4356789\r\n
		// \r\n
		// String requestLine = null;
		// try {
		// requestLine = remoteHost.getConnection().readLine();
		// } catch (IOException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
		//
		// Matcher match = Pattern.compile(DownloadConnection.HTTP_RESPONSE_PATTERN).matcher(requestLine);
		// match.matches();
		// final int statusCode = Integer.parseInt(match.group(1));

		List<String> lines = new ArrayList<String>();
		String currentLine;
		try {
			while ((currentLine = remoteHost.getConnection().readLine()) != null && currentLine.length() > 0 && !currentLine.equals("\r\n")) {
				lines.add(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (httpGetRequestResponseEventListener != null) {
				httpGetRequestResponseEventListener.onThrowable(e);
			}
			return null;
		}

		HashMap<String, String> httpFieldSet = new HashMap<String, String>();
		for (String line : lines) {
			if (line.isEmpty()) {
				continue;
			}
			// フィールド名: 内容
			int separatePos = line.indexOf(":");
			httpFieldSet.put(line.substring(0, separatePos), line.substring(separatePos + 2));
			System.out.println(line);
		}

		//final Range range = parseRange(httpFieldSet.get("Content-Range"));
		long contentLength = Long.parseLong(httpFieldSet.get("Content-length"));
		// ファイルを受け取る
		try {
			byte[] byteBuffer = new byte[BUF_SIZE];
			InputStream is = remoteHost.getConnection().getInputStream();
			FileOutputStream fos = new FileOutputStream(fileName);

			int length = 0, totalLength = 0;
			while (contentLength > 0 && (length = is.read(byteBuffer)) != -1) {
				fos.write(byteBuffer, 0, length);
				
				if(this.httpGetRequestResponseEventListener!=null){
					this.httpGetRequestResponseEventListener.onReceiveData(byteBuffer);
				}
				
				byteBuffer = new byte[BUF_SIZE];
				totalLength += length;
				contentLength -= length;
			}
			fos.flush();
			fos.close();

			System.out.println(String.format("FileRecieveCompleat:%s size:%d ", fileName, totalLength));
		} catch (Exception ex) {
		}
		
		// TODO:ソケットのクローズremoteHost.getConnection().close();
		File resultFile = new File(fileName);
		if(this.httpGetRequestResponseEventListener!=null){
			this.httpGetRequestResponseEventListener.onComplete(resultFile);
		}
		
		return resultFile;
	}

	public Runnable hundleHttpGivRequest(final String requestLine, final Host remoteHost) {
		// GIV <File Index>:<Servent Identifier>/<File Name>\n\n

		Matcher match = Pattern.compile(DownloadConnection.HTTP_GIV_REQUEST_PATTERN).matcher(requestLine);
		match.matches();
		final int fileIndex = Integer.parseInt(match.group(1));
		final String serventIdentifierHexString = match.group(2);
		final GUID guid = new GUID(serventIdentifierHexString);
		// final String fileName = match.group(3);
		
		return new Runnable() {
			@Override
			public void run() {
				HttpEventListener GivRequestEventListener = httpGivRequestEventListener.get(new SimpleEntry<GUID, Integer>(guid, fileIndex));
				if (GivRequestEventListener != null) {
					GivRequestEventListener.onHundleRequest(requestLine, remoteHost);
				}
			}
		};
	}

	public static Runnable hundleHttpGivRequestResponse(final Host remoteHost) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					String requestLine = null;
					while ((requestLine = remoteHost.getConnection().readLine()) != null) {
						hundleHttpGetRequest(requestLine, remoteHost).run();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@SuppressWarnings("unused")
	private static String generateHttpGetRequestResponseHeaderString(Status status, long contentLength) {
		return generateHttpGetRequestResponseHeaderString(status, contentLength, null);
	}

	private static String generateHttpGetRequestResponseHeaderString(Status status, long contentLength, Range range) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("HTTP %d OK\r\n", status.code, status.reason));
		sb.append("Content-type: application/binary\r\n");
		sb.append(String.format("Content-length: %d\r\n", contentLength));
		if (range != null) {
			sb.append(String.format("Content-Range: bytes %d-%d1/%d\r\n", range.start, range.end, range.length));
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public void setHttpGetRequestResponseEventListener(HttpEventListener httpEventListener) {
		this.httpGetRequestResponseEventListener = httpEventListener;
	}

	public void addHundleGivRequestEventListener(HttpEventListener httpEventListener, GUID guid, int fileIndex) {
		this.httpGivRequestEventListener.put(new SimpleEntry<GUID, Integer>(guid, fileIndex), httpEventListener);
	}

	protected static Range parseRange(String rengeFieldString) {
		try {
			int valueStartPos = rengeFieldString.indexOf("bytes=") + 6;
			String rangeParamsString = rengeFieldString.substring(valueStartPos);
			String[] rangeParams = rangeParamsString.split(",");
			for (String paramString : rangeParams) {
				if (paramString.startsWith("-")) {
					return (new HttpHundler()).new Range((long) 0, Long.parseLong(paramString.substring(1)));
				} else if (paramString.endsWith("-")) {
					return (new HttpHundler()).new Range(Long.parseLong(paramString.substring(0, paramString.length() - 1)), null);
				} else if (paramString.matches("[0-9]+-[0-9]+")) {
					String[] params = paramString.split("-");
					return (new HttpHundler()).new Range(Long.parseLong(params[0]), Long.parseLong(params[1]));
				}
			}
		} catch (Exception ex) {
			return (new HttpHundler()).new Range(null, null);
		}
		return (new HttpHundler()).new Range(null, null);
	}

	public class Range {
		public Long start = null;
		public Long end = null;
		public Long length = null;

		public Range(Long start, Long end) {
			this.start = start;
			this.end = end;
		}

		public Range(Long start, Long end, Long length) {
			this.start = start;
			this.end = end;
			this.length = length;
		}
	}
}
