package gnutella.message;

import java.util.ArrayList;
import java.util.List;

public class ResultSet {
	private List<ResultSetContent> resultSetContentList;

	public ResultSet() {
		this.resultSetContentList = new ArrayList<ResultSetContent>();
	}

	public void add(ResultSetContent content) {
		this.resultSetContentList.add(content);
	}

	public byte[] getBytes() {
		List<byte[]> resultSetContentBytesList = new ArrayList<byte[]>();
		int byteLength = 0;
		for (ResultSetContent content : this.resultSetContentList) {
			byte[] resultSetBytes = content.getBytes();
			byteLength += resultSetBytes.length;
			resultSetContentBytesList.add(resultSetBytes);
		}

		byte[] result = new byte[byteLength];
		int currentPosition = 0;
		for (byte[] bytes : resultSetContentBytesList) {
			System.arraycopy(bytes, 0, result, currentPosition, bytes.length);
			currentPosition += bytes.length;
		}
		return result;
	}

	public int getByteLength() {
		int byteLength = 0;
		for (ResultSetContent content : this.resultSetContentList) {
			byteLength += content.getBytes().length;
		}
		return byteLength;
	}

	public ResultSetContent getByFileIndex(int fileIndex) {
		// TODO Map<index,resultSet>等にして計算量を減らす
		for (ResultSetContent content : this.resultSetContentList) {
			if (content.getFileIndex() == fileIndex) {
				return content;
			}
		}
		return null;
	}
}
