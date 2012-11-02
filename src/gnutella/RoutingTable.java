package gnutella;

import gnutella.message.GUID;
import gnutella.message.Message;
import gnutella.message.PayloadDescriptorType;
import gnutella.message.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RoutingTable {
	Map<GUID, Host> pingTable = new HashMap<GUID, Host>();
	Map<GUID, Host> pongTable = new HashMap<GUID, Host>();
	Map<GUID, Host> queryTable = new HashMap<GUID, Host>();
	Map<GUID, Host> queryHitTable = new HashMap<GUID, Host>();
	Map<GUID, Host> routingTable = new HashMap<GUID, Host>();
	ArrayList<GUID> guidList = new ArrayList<GUID>();

	public void add(Host remoteHost, GUID guid, PayloadDescriptorType payloadType){
		switch(payloadType){
		case PING:
			pingTable.put(guid, remoteHost);
			break;
		case PONG:
			pongTable.put(guid, remoteHost);
			break;
		case QUERY:
			queryTable.put(guid, remoteHost);
			break;
		case QUERYHITS:
			queryHitTable.put(guid, remoteHost);
			break;
		default:
			System.out.println("This is not in PayloadDiscriptorType.");
		}
		routingTable.put(guid, remoteHost);
		if(guidList.contains(guid) == false){
			guidList.add(guid);
		}
	}

	public Host getNextHost(Message message){
		GUID guid = message.getHeader().getGuid();
		PayloadDescriptorType payloadType = message.getHeader().getPayloadDescriptor();
		Host returnHost = null;
		switch(payloadType){
		case PING:
			returnHost = pingTable.get(guid);
			break;
		case PONG:
			returnHost = pongTable.get(guid);
			break;
		case QUERY:
			returnHost = queryTable.get(guid);
			break;
		case QUERYHITS:
			returnHost = queryHitTable.get(guid);
			break;
		default:
			System.out.println("This is not in PayloadDiscriptorType.");
		}
		return returnHost;
	}

	public Host[] getNeedMulticast(Message message){
		ArrayList<Host> hostList = new ArrayList<Host>();
		Host[] returnHost = null;
		GUID guid = message.getHeader().getGuid();
		int number = guidList.size();
		for(int i = 0; i < number; i++){
			hostList.add(routingTable.get(guidList.get(i)));
		}
		returnHost = (Host[])hostList.toArray();
		return returnHost;
	}
}
