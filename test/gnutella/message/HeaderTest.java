package gnutella.message;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeaderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() {
		ByteBuffer bf = ByteBuffer.allocateDirect(Header.HEADER_LENGTH);

		GUID guid = new GUID(new byte[GUID.LENGTH]);
		byte payload = Header.PONG;
		byte ttl = 7;
		byte hops = 0;
		int payloadLength = 20;

		bf.put(guid.getGuid());
		bf.put(payload);
		bf.put(ttl);
		bf.put(hops);
		bf.putInt(payloadLength);

		bf.flip();
		byte[] headerBytes = new byte[Header.HEADER_LENGTH];
		bf.get(headerBytes);

		Header header = Header.parse(headerBytes);

		assertArrayEquals(header.getGuid().getGuid(), guid.getGuid());
		assertEquals(header.getPayloadDescriptor(), payload);
		assertEquals(header.getHops(), hops);
		assertEquals(header.getPayloadLength(), payloadLength);
		assertEquals(header.getTtl(), ttl);
	}
}
