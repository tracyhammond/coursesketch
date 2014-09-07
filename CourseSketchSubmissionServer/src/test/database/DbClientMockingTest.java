package database;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;


public class DbClientMockingTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
	}

	public DatabaseClient getMockedVersion() {
		DatabaseClient cl = mock(DatabaseClient.class);
		when(cl.getDB()).thenReturn(mock(DB.class));
		return cl;
	}
}
