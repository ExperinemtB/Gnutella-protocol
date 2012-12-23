package gnutella.listener;

import gnutella.Host;

import java.util.EventListener;

public interface ConnectionEventListener extends EventListener {
	public void onConnect(Host remoteHost);

	public void onThrowable(Throwable throwable);

	public void onClose(Host remoteHost);
}
