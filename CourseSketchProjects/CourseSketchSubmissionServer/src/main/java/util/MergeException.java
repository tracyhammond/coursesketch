package util;

/**
 * Created by gigemjt on 12/28/14.
 * Thrown if there are any issues with merging.
 */
public class MergeException extends Exception {
    /**
     * @param message
     *         The message of the exception.
     */
    public MergeException(final String message) {
        super(message);
    }

    /**
     * @param message
     *         The message of the exception.
     * @param cause
     *         The cause of this exception.
     */
    public MergeException(final String message, final Exception cause) {
        super(message, cause);
    }
}
