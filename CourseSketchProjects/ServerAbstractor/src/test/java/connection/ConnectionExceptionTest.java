package connection;

import static org.junit.Assert.*;
import org.junit.Test;
import utilities.ConnectionException;


/**
 * Created by gigemjt on 10/21/14.
 */
public class ConnectionExceptionTest {
    @Test
    public void testConstructor() {
        String message = "message";
        ConnectionException con = new ConnectionException(message);
        assertEquals(con.getMessage(), message);
    }

    @Test
    public void testConstructorWithCause() {
        String message = "message";
        Exception cause = new Exception();
        ConnectionException con = new ConnectionException(message, cause);
        assertEquals(con.getMessage(), message);
        assertEquals(con.getCause(), cause);
    }
}
