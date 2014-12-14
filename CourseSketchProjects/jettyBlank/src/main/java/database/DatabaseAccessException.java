package database;

/**
 * An exception that occurs if data from the database is not accessible or has errors in it.
 * @author gigemjt
 *
 */
public class DatabaseAccessException extends Exception {

    /**
     * True if the exception is recoverable.
     */
    private boolean recoverable = false;

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
}
