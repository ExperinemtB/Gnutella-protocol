package gnutella.listener;

import java.net.InetAddress;
import java.util.EventListener;

public interface ClientEventListener extends EventListener {
	public void onConnect(int port,InetAddress address);

	public void onThrowable(Throwable throwable);
	
	public void onClose(int port,InetAddress address);
}
