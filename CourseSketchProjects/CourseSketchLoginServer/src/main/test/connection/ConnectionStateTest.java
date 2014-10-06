package connection;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectionStateTest {
    private final ConnectionState mConnectionState = new ConnectionState(
            "test");

    @Test
    public void testEquals() throws Exception {
        final Object notAConnectionState = new Object();
        
        assertFalse(mConnectionState.equals(notAConnectionState));
        assertTrue(mConnectionState.equals(mConnectionState));
    }
}
