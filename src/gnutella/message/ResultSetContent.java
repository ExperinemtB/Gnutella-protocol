package gnutella.message;

import java.nio.ByteBuffer;

public class ResultSetContent {
	public static int MIN_LENGTH = 8;

	private int fileIndex;
	private int fileSize;
	private String fileName;
	private byte[] fileMD5digest;

	public ResultSetContent(byte[] resultSetContentBytes) {
	}

	public ResultSetContent(int fileIndex, int fileSize, String fileName,byte[] fileMD5digest) {
		this.fileIndex = fileIndex;
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.fileMD5digest = new byte[fileMD5digest.length];
		System.arraycopy(fileMD5digest, 0, this.fileMD5digest, 0, fileMD5digest.length);
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getBytes() {
		ByteBuffer bf = ByteBuffer.allocateDirect(ResultSetContent.MIN_LENGTH + this.fileName.getBytes().length + 16 + 2);
		bf.putInt(this.fileIndex);
		bf.putInt(this.fileSize);
		bf.put(this.fileMD5digest);
		bf.put(this.fileName.getBytes());
		bf.put(new byte[] { 0x00 });
		bf.put(new byte[] { 0x00 });
		bf.flip();

		byte[] result = new byte[bf.limit()];
		bf.get(result);
		return result;
	}

	@Override
	public String toString() {
		return String.format("{fileIndex:%d fileSize:%d fileName:%s}", this.fileIndex, this.fileSize, this.fileName);
	}

	public byte[] getFileMD5digest() {
		return fileMD5digest;
	}
}
