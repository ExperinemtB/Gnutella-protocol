package gnutella.share;

import gnutella.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class SharedFile {
	private File originFile;

	private int fileIndex;
	private String fileName;
	private byte[] fileMD5Digest;

	public SharedFile(int fileIndex, String fileName) {
		this.fileIndex = fileIndex;
		this.fileName = fileName;
	}

	public SharedFile(int fileIndex, String fileName, File file) throws NoSuchAlgorithmException, IOException {
		this(fileIndex, fileName);
		this.originFile = file;
		this.fileMD5Digest = FileUtils.getFileMD5Digest(this.originFile);
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

	public byte[] getFileMD5Digest() {
		return fileMD5Digest;
	}

}
