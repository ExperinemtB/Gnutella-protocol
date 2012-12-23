package gnutella.listener;

import gnutella.DownloadWorker;

import java.io.File;
import java.util.EventListener;

public interface DownloadWorkerEventListener extends EventListener {
	public void onComplete(DownloadWorker eventSource, int fileIndex, File file);

	public void onThrowable(DownloadWorker eventSource, Throwable throwable);

	public void onReceiveData(DownloadWorker eventSource, String fileName, long totalReceivedLength,long totalFileLength);

}
