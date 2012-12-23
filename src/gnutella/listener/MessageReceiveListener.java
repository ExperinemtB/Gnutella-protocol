package gnutella.listener;

import gnutella.Host;
import gnutella.message.PingMessage;
import gnutella.message.PongMessage;
import gnutella.message.PushMessage;
import gnutella.message.QueryHitMessage;
import gnutella.message.QueryMessage;

import java.util.EventListener;

public interface MessageReceiveListener extends EventListener {

	public void onReceivePingMessage(PingMessage ping, Host remoteHost);

	public void onReceivePongMessage(PongMessage pong, Host remoteHost);

	public void onReceiveQueryMessage(QueryMessage query, Host remoteHost);

	public void onReceiveQueryHitMessage(QueryHitMessage queryHit, Host remoteHost);

	public void onReceivePushMessage(PushMessage push, Host remoteHost);
}
