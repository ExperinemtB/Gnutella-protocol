package gnutella.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PongMessageTest {

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
	public void testPongMessage() {
		ByteBuffer bf = ByteBuffer.allocateDirect(PongMessage.LENGTH);

		char port = (char) 50003;
		InetAddress ipAddress = null;
		byte[] ipByte = null;
		try {
			ipAddress = InetAddress.getLocalHost();
			ipByte = ipAddress.getAddress();
		} catch (UnknownHostException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		int shareCount = 500;
		int shareSize = 100000;

		bf.putChar(port);
		bf.put(ipByte[3]);
		bf.put(ipByte[2]);
		bf.put(ipByte[1]);
		bf.put(ipByte[0]);
		bf.putInt(shareCount);
		bf.putInt(shareSize);
		bf.flip();

		byte[] pongBytes = new byte[PongMessage.LENGTH];
		bf.get(pongBytes);

		PongMessage pong = new PongMessage(new Header(Header.PONG, (byte) 7, PongMessage.LENGTH), pongBytes);

		assertEquals(port, pong.getPort());
		assertEquals(ipAddress, pong.getIpAddress());
		assertEquals(shareCount, pong.getNumberOfFilesShared());
		assertEquals(shareSize, pong.getNumberOfKilobytesShared());
	}

	@Test
	public void testgetBytes() {
		Header header = new Header(Header.PONG, (byte) 7, PongMessage.LENGTH);
		ByteBuffer bf = ByteBuffer.allocateDirect(Header.HEADER_LENGTH + PongMessage.LENGTH);

		char port = (char) 50003;
		InetAddress ipAddress = null;
		byte[] ipByte = null;
		try {
			ipAddress = InetAddress.getLocalHost();
			ipByte = ipAddress.getAddress();
		} catch (UnknownHostException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		int shareCount = 500;
		int shareSize = 100000;

		bf.put(header.getBytes());
		bf.putChar(port);
		bf.put(ipByte[3]);
		bf.put(ipByte[2]);
		bf.put(ipByte[1]);
		bf.put(ipByte[0]);
		bf.putInt(shareCount);
		bf.putInt(shareSize);
		bf.flip();

		byte[] pongBytes = new byte[Header.HEADER_LENGTH + PongMessage.LENGTH];
		bf.get(pongBytes);

		PongMessage pong = new PongMessage(header, port, ipAddress, shareCount, shareSize);
		byte[] pongBytes2 = pong.getBytes();

		assertArrayEquals(pongBytes, pongBytes2);

	}
}
