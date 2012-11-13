package gnutella.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryHitMessageTest {

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
	public void testGetBytes() {
		byte numberofHits = 3;
		int port = 50000;
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int speed = 50;
		ResultSet resultSet = new ResultSet();
		for (int i = 0; i < numberofHits; i++) {
			ResultSetContent content = new ResultSetContent(i, i * 50, "testFile:" + String.valueOf(i));
			resultSet.add(content);
		}
		GUID serventIdentifier = new GUID();

		Header header = new Header(Header.QUERYHIT, (byte) 7, QueryHitMessage.MIN_LENGTH + resultSet.getByteLength());
		ByteBuffer bf = ByteBuffer.allocateDirect(Header.HEADER_LENGTH + QueryHitMessage.MIN_LENGTH + resultSet.getByteLength());

		bf.put(header.getBytes());
		bf.put(numberofHits);
		bf.putShort((short) port);
		bf.put(ipAddress.getAddress());
		bf.putInt(speed);
		bf.put(resultSet.getBytes());
		bf.put(serventIdentifier.getGuid());
		bf.flip();

		byte[] queryHitBytes = new byte[bf.limit()];
		bf.get(queryHitBytes);

		QueryHitMessage queryHit = new QueryHitMessage(header, numberofHits, (char) port, ipAddress, speed, resultSet, serventIdentifier);
		byte[] queryHitBytes2 = queryHit.getBytes();

		assertArrayEquals(queryHitBytes, queryHitBytes2);
	}

	@Test
	public void testQueryHitMessageHeaderByteIntInetAddressIntResultSetContentArrayGUID() {
		byte numberofHits = 3;
		int port = 50000;
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int speed = 50;
		ResultSet resultSet = new ResultSet();
		for (int i = 0; i < numberofHits; i++) {
			ResultSetContent content = new ResultSetContent(i, i * 50, "testFile:" + String.valueOf(i));
			resultSet.add(content);
		}
		GUID serventIdentifier = new GUID();

		Header header = new Header(Header.QUERYHIT, (byte) 7, QueryHitMessage.MIN_LENGTH + resultSet.getByteLength());
		ByteBuffer bf = ByteBuffer.allocateDirect(QueryHitMessage.MIN_LENGTH + resultSet.getByteLength());

		bf.put(numberofHits);
		bf.putChar((char) port);
		bf.put(((Inet4Address) ipAddress).getAddress());
		bf.putInt(speed);
		bf.put(resultSet.getBytes());
		bf.put(serventIdentifier.getGuid());
		bf.flip();

		byte[] queryHitBytes = new byte[bf.limit()];
		bf.get(queryHitBytes);

		QueryHitMessage queryHit = new QueryHitMessage(header, queryHitBytes);

		assertEquals(numberofHits, queryHit.getNumberofHits());
		assertEquals(port, queryHit.getPort());
		assertEquals(ipAddress, queryHit.getIpAddress());
		assertEquals(speed, queryHit.getSpeed());
		for (int i = 0; i < numberofHits; i++) {
			assertEquals(resultSet.getByFileIndex(i).getFileIndex(), queryHit.getResultSet().getByFileIndex(i).getFileIndex());
			assertEquals(resultSet.getByFileIndex(i).getFileSize(), queryHit.getResultSet().getByFileIndex(i).getFileSize());
			assertTrue(resultSet.getByFileIndex(i).getFileName().equals(queryHit.getResultSet().getByFileIndex(i).getFileName()));
		}
		assertEquals(serventIdentifier, queryHit.getServentIdentifier());
	}
}
