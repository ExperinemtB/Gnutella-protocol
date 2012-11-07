package gnutella;

import gnutella.message.GUID;
import gnutella.message.Header;
import gnutella.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
	Map<GUID, Host> pingTable = new HashMap<GUID, Host>();
	Map<GUID, Host> pongTable = new HashMap<GUID, Host>();
	Map<GUID, Host> queryTable = new HashMap<GUID, Host>();
	Map<GUID, Host> queryHitTable = new HashMap<GUID, Host>();
	Map<GUID, Host> routingTable = new HashMap<GUID, Host>();
	ArrayList<GUID> guidList = new ArrayList<GUID>();

	public synchronized void add(Host remoteHost, GUID guid, byte payloadType) {
		switch (payloadType) {
		case Header.PING:
			pingTable.put(guid, remoteHost);
			break;
		case Header.PONG:
			pongTable.put(guid, remoteHost);
			break;
		case Header.QUERY:
			queryTable.put(guid, remoteHost);
			break;
		case Header.QUERYHIT:
			queryHitTable.put(guid, remoteHost);
			break;
		default:
			System.out.println("This is not in PayloadDiscriptorType.");
		}
		routingTable.put(guid, remoteHost);
		if (guidList.contains(guid) == false) {
			guidList.add(guid);
		}
	}

	public Host getNextHost(Message message) {
		GUID guid = message.getHeader().getGuid();
		Host returnHost = null;
		switch (message.getHeader().getPayloadDescriptor()) {
		case Header.PING:
			returnHost = pingTable.get(guid);
			break;
		case Header.PONG:
			returnHost = pingTable.get(guid);
			break;
		case Header.QUERY:
			returnHost = queryTable.get(guid);
			break;
		case Header.QUERYHIT:
			returnHost = queryTable.get(guid);
			break;
		default:
			System.out.println("This is not in PayloadDiscriptorType.");
		}
		return returnHost;
	}

	public Boolean isMessageAlreadyReceived(GUID guid, byte payloadType) {
		Boolean need = false;
		switch (payloadType) {
		case Header.PING:
			need = pingTable.containsKey(guid);
			break;
		case Header.PONG:
			need = pongTable.containsKey(guid);
			break;
		case Header.QUERY:
			need = queryTable.containsKey(guid);
			break;
		case Header.QUERYHIT:
			need = queryHitTable.containsKey(guid);
			break;
		}
		return need;
	}
}
