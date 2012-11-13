package gnutella.share;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SharedFileContainer {
	private List<SharedFile> sharedFileList;
	private int totalFileSizeKb;

	public SharedFileContainer() {
		sharedFileList = new ArrayList<SharedFile>();
		totalFileSizeKb = 0;
	}

	public int getTotalFileSizeKb() {
		return this.totalFileSizeKb;
	}

	public void addSharedFile(SharedFile shareFile) {
		File file = shareFile.getOriginFile();
		if (getFileByAbsolutePath(file.getAbsolutePath()) != null || !file.exists()) {
			return;
		}
		totalFileSizeKb += file.length() / 1024;
		sharedFileList.add(shareFile);
	}

	public SharedFile getFileByName(String fileName) {
		for (SharedFile file : this.sharedFileList) {
			if (file.getFileName().equals(fileName)) {
				return file;
			}
		}
		return null;
	}

	public SharedFile getFileByAbsolutePath(String absolutePath) {
		for (SharedFile file : this.sharedFileList) {
			if (file.getOriginFile().getAbsolutePath().equals(absolutePath)) {
				return file;
			}

		}
		return null;
	}

	public int getFileCount() {
		return this.sharedFileList.size();
	}

	/**
	 * 自分の共有中のファイルの中からキーワードをファイル名に含むものを検索する
	 * @param keyword キーワード
	 * @return 見つかったSharedFileの配列
	 */
	public SharedFile[] searchFilesByKeyword(String keyword) {
		List<SharedFile> resultList = new ArrayList<SharedFile>();
		for (SharedFile file : this.sharedFileList) {
			if (file.getFileName().contains(keyword)) {
				resultList.add(file);
			}
		}
		return resultList.toArray(new SharedFile[] {});
	}
}