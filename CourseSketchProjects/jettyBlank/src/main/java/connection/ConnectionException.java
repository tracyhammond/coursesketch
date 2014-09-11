package connection;

/**
 * A generic exception that is thrown by this library.
 *
 * @author gigemjt
 *
 */
@SuppressWarnings("serial")
public class ConnectionException extends Exception {

    /**
     * A simple constructor.
     *
     * @param string
     *            The message for the exception.
     */
    public ConnectionException(final String string) {
        super(string);
    }

}
