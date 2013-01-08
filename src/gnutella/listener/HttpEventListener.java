package gnutella.listener;

import gnutella.Host;

import java.io.File;
import java.util.EventListener;

public interface HttpEventListener extends EventListener {
	public void onHundleRequest(String requestLine,Host remoteHost);
	
	public void onComplete(File file);

	public void onThrowable(Throwable throwable);

	public void onReceiveData(byte[] receiveData,int length);
}
