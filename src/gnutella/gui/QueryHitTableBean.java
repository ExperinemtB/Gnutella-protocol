package gnutella.gui;

/**
 * QueryHitの受信結果をMD5ハッシュでまとめてテーブルに表示するときの各要素を表すクラス
 */
public class QueryHitTableBean {
	//MD5でまとめる
	protected byte[] MD5Digest;
	
	//同じMD5を持つファイル名
protected String[] fileNames;
	
	//同じMD5を持つファイルを共有しているノードの数
	protected int fileSharingNodeCount;

	public byte[] getMD5Digest() {
		return MD5Digest;
	}

	public void setMD5Digest(byte[] mD5Digest) {
		MD5Digest = mD5Digest;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public int getFileSharingNodeCount() {
		return fileSharingNodeCount;
	}

	public void setFileSharingNodeCount(int fileSharingNodeCount) {
		this.fileSharingNodeCount = fileSharingNodeCount;
	}
	
}
