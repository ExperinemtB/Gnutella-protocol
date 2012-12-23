package gnutella.listener;

import java.net.InetAddress;
import java.util.EventListener;

public interface ServerEventListener extends EventListener {
	public void onStart(int port,InetAddress address);

	public void onThrowable(Throwable throwable);

	public void onStop();
}
