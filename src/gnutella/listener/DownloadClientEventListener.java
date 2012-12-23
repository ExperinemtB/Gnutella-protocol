package gnutella.listener;

import gnutella.DownloadClient;

import java.io.File;
import java.util.EventListener;

public interface DownloadClientEventListener extends EventListener {
	public void onComplete(DownloadClient eventSource, int fileId, File file);

	public void onThrowable(DownloadClient eventSource, Throwable throwable);

	public void onReceiveData(DownloadClient eventSource, int fileId, byte[] receiveData);
}
