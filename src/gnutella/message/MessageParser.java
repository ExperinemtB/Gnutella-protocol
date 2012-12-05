package gnutella.message;

import java.util.Arrays;

public class MessageParser {
	public static Message parse(byte[] primitive) {
		Header header = Header.parse(Arrays.copyOfRange(primitive, 0, Header.HEADER_LENGTH));

		if (primitive.length < Header.HEADER_LENGTH + header.getPayloadLength()) {
			// データ長がヘッダ+ペイロード長以下のとき
			throw new IllegalArgumentException("Size of data < size of header + size of payloadLength written in header");
		}

		byte[] payload = Arrays.copyOfRange(primitive, Header.HEADER_LENGTH, primitive.length);
		switch (header.getPayloadDescriptor()) {
		case Header.PING:
			return new PingMessage(header, payload);
		case Header.PONG:
			return new PongMessage(header, payload);
		case Header.QUERY:
			return new QueryMessage(header, payload);
		case Header.QUERYHIT:
			return new QueryHitMessage(header, payload);
		case Header.PUSH:
			return new PushMessage(header, payload);
		default:
		}
		return null;
	}
}
