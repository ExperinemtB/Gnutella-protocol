package gnutella.message;

import java.util.Arrays;

public class MessageParser {
	public static Message parse(byte[] primitive) {
		Header header = Header.parse(Arrays.copyOfRange(primitive, 0, Header.HEADER_LENGTH));
		
		if (header == null || primitive.length < Header.HEADER_LENGTH + header.getPayloadLength()) {
			//ヘッダのパースに失敗したか、データ長がヘッダ+ペイロード長以下のとき
			return null;
		}

		byte[] payload = Arrays.copyOfRange(primitive, Header.HEADER_LENGTH, primitive.length);
		switch (header.getPayloadDescriptor()) {
		case PING:
			return new PingMessage(header, payload);
		case PONG:
			return new PongMessage(header, payload);
		case QUERY:
			break;
		case QUERYHITS:
			break;
		case PUSH:
			break;
		default:
		}
		return null;
	}
}
