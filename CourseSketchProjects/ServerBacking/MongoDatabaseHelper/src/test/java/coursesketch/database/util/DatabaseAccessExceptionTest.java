package coursesketch.database.util;

import org.junit.Test;
import utilities.ConnectionException;

import static org.junit.Assert.assertEquals;


/**
 * Created by gigemjt on 10/21/14.
 */
public class DatabaseAccessExceptionTest {
    @Test
    public void testConstructor() {
        String message = "message";
        DatabaseAccessException con = new DatabaseAccessException(message, true);
        assertEquals(message, con.getMessage());
        assertEquals(true, con.isRecoverable());
    }

    @Test
    public void testConstructorWithCause() {
        String message = "message";
        Exception cause = new Exception();
        DatabaseAccessException con = new DatabaseAccessException(message, cause);
        con.setSendResponse(true);
        assertEquals(message, con.getMessage());
        assertEquals(cause, con.getCause());
        assertEquals(true, con.isSendResponse());
    }
}
