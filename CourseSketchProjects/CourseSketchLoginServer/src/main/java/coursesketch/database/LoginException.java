package coursesketch.database;

import utilities.CourseSketchException;

/**
 * Created by gigemjt on 10/17/14.
 */
public class LoginException extends CourseSketchException {
    /**
     * @param message
     *            takes in a message.
     */
    public LoginException(final String message) {
        super(message);
    }

    /**
     * @param message
     *            takes in a message.
     * @param cause
     *            and a cause.
     */
    public LoginException(final String message, final Exception cause) {
        super(message, cause);
    }
}
