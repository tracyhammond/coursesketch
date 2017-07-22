package utilities;

import protobuf.srl.request.Message;

/**
 * A generic exception that is thrown by this library.
 *
 * @author gigemjt
 *
 */
@SuppressWarnings("serial")
public class ConnectionException extends CourseSketchException {

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

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message
     *            The message for the exception.
     * @param protoException
     *         An existing chain of exceptions.  This could come from a different server even.
     */
    public ConnectionException(final String message, final Message.ProtoException protoException) {
        super(message, protoException);
    }
}
