package gnutella.share;

import java.io.File;

public class SharedFile {
	private File originFile;

	private int fileIndex;
	private String fileName;

	public SharedFile(int fileIndex, String fileName) {
		this.fileIndex = fileIndex;
		this.fileName = fileName;
	}

	public SharedFile(int fileIndex, String fileName, File file) {
		this(fileIndex, fileName);
		this.originFile = file;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public String getFileName() {
		return fileName;
	}

	public File getOriginFile() {
		return originFile;
	}

	public void setOriginFile(File originFile) {
		this.originFile = originFile;
	}
}
