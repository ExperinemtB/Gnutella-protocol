package gnutella.message;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GUIDTest {

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
	public void testEquals() {
		GUID guid1 = new GUID(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		GUID guid2 = new GUID(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		GUID guid3 = new GUID(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 });
		assertEquals(true, guid1.equals(guid2));
		assertEquals(false, guid1.equals(guid3));
	}
	
	@Test
	public void testGUIDHexString() throws UnknownHostException {
		GUID guid1 = new GUID();
		GUID guid2 = new GUID(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		GUID guid3 = new GUID(new byte[] { 0,-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15 });
		GUID guid4 = new GUID(InetAddress.getLocalHost());

		assertThat(guid1,is(equalTo(new GUID(guid1.toHexString()))));
		assertThat(guid2,is(equalTo(new GUID(guid2.toHexString()))));
		assertThat(guid3,is(equalTo(new GUID(guid3.toHexString()))));
		assertThat(guid4,is(equalTo(new GUID(guid4.toHexString()))));
		
		for(int i=0;i<100000;i++){
			GUID randomGuid = new GUID();
			assertThat(randomGuid,is(equalTo(new GUID(randomGuid.toHexString()))));
		}
	}
}
