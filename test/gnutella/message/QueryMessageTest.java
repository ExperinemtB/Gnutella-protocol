package gnutella.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryMessageTest {

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
	public void testQueryMessageHeaderByteArray() {
		int minimumSpeedKb = 10;
		String searchCriteria = "test keyword";
		ByteBuffer bf = ByteBuffer.allocateDirect(QueryMessage.MIN_LENGTH + searchCriteria.getBytes().length + 1);

		bf.putShort((short) minimumSpeedKb);
		bf.put(searchCriteria.getBytes());
		bf.put(new byte[] { 0x00 });
		bf.flip();

		byte[] queryBytes = new byte[bf.capacity()];
		bf.get(queryBytes);

		QueryMessage query = new QueryMessage(new Header(Header.QUERY, (byte) 7, queryBytes.length), queryBytes);

		assertEquals(minimumSpeedKb, query.getMinimumSpeedKb());
		assertEquals(searchCriteria, query.getSearchCriteria());
	}

	@Test
	public void testGetBytes() {
		int minimumSpeedKb = 10;
		String searchCriteria = "test keyword";
		Header header = new Header(Header.QUERY, (byte) 7, searchCriteria.getBytes().length + 1);
		ByteBuffer bf = ByteBuffer.allocateDirect(Header.HEADER_LENGTH + QueryMessage.MIN_LENGTH + searchCriteria.getBytes().length + 1);

		bf.put(header.getBytes());
		bf.putShort((short) minimumSpeedKb);
		bf.put(searchCriteria.getBytes());
		bf.put(new byte[] { 0x00 });
		bf.flip();

		byte[] queryBytes = new byte[bf.capacity()];
		bf.get(queryBytes);

		QueryMessage query = new QueryMessage(header, minimumSpeedKb, searchCriteria);
		byte[] queryBytes2 = query.getBytes();

		assertArrayEquals(queryBytes, queryBytes2);
	}

}
