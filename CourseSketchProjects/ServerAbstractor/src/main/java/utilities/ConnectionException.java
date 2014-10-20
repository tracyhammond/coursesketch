package utilities;

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

    /**
     * Passes up an exception as the cause of the exception.
     * @param string A message that gives details of the error
     * @param cause The cause of the exception.
     */
    public ConnectionException(final String string, final Exception cause) {
        super(string, cause);
    }

}
