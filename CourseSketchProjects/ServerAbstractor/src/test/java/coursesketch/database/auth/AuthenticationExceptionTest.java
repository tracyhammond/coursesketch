package coursesketch.database.auth;

import coursesketch.database.util.DatabaseAccessException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created by gigemjt on 10/21/14.
 */
public class AuthenticationExceptionTest {
    @Test
    public void testConstructor() {
        String message = "Can only perform task with valid permission: ";
        AuthenticationException con = new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        assertEquals(con.getType(), AuthenticationException.INVALID_PERMISSION);
        assertEquals(con.getMessage(), message);
    }

    @Test
    public void testConstructorWithCause() {
        String message = "message";
        Exception cause = new Exception();
        AuthenticationException con = new AuthenticationException(message, cause);
        assertEquals(con.getMessage(), message);
        assertEquals(con.getCause(), cause);
    }
}
