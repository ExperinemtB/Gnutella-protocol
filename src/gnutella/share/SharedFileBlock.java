package gnutella.share;

import java.io.File;

public class SharedFileBlock {
	private File baseFile;
	private int fileId;
	private long length;
	private int position;
	private byte[] data;

	public SharedFileBlock(int fileId,File baseFile) {
		this.fileId = fileId;
		this.baseFile = baseFile;
	}

	public File getBaseFile() {
		return baseFile;
	}

	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public long getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
