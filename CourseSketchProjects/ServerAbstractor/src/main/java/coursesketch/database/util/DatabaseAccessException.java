package coursesketch.database.util;

import utilities.CourseSketchException;

/**
 * An exception that occurs if data from the coursesketch.util.util is not accessible or has errors in it.
 * @author gigemjt
 *
 */
public class DatabaseAccessException extends CourseSketchException {

    /**
     * True if the exception is recoverable.
     */
    private boolean recoverable;

    /**
     * True if the server should send a response that is not an error proto.
     */
    private boolean sendResponse;

    /**
     * Accepts a message and if it is recoverable error.
     * @param string A message of the error.
     * @param iRecoverable True if the error is recoverable.
     */
    public DatabaseAccessException(final String string, final boolean iRecoverable) {
        super(string);
        this.recoverable = iRecoverable;
    }

    /**
     * Accepts a message and if it is recoverable error.
     * @param exception the specific exception being throw.
     * @param iRecoverable True if the error is recoverable.
     */
    public DatabaseAccessException(final Exception exception, final boolean iRecoverable) {
        super(exception);
        this.recoverable = iRecoverable;
    }

    /**
     * Only takes in a message assumes the exception is not recoverable.
     * @param string A message of the error.
     * @param exception The exception that was thrown before this.
     */
    public DatabaseAccessException(final String string, final Exception exception) {
        super(string, exception);
    }

    /**
     * Only takes in a message assumes the exception is not recoverable.
     * @param string A message of the error.
     */
    public DatabaseAccessException(final String string) {
        this(string, false);
    }

    /**
     * @return True if this is not a serious error and can be recovered.
     */
    public final boolean isRecoverable() {
        return recoverable;
    }

    /**
     * @return True if the server should still send a valid response.
     */
    public final boolean isSendResponse() {
        return sendResponse;
    }

    /**
     * @param shouldSendResponse Set to true if the server should send a response
     * @return Self.
     */
    public final DatabaseAccessException setSendResponse(final boolean shouldSendResponse) {
        this.sendResponse = shouldSendResponse;
        return this;
    }
}
