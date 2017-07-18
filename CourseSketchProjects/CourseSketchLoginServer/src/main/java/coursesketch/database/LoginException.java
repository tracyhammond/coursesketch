package coursesketch.database;

import utilities.CourseSketchException;

/**
 * Exceptions that occur during login.
 *
 * Created by gigemjt on 10/17/14.
 */
public class LoginException extends CourseSketchException {
    /**
     * @param message
     *            takes in a message.
     */
    LoginException(final String message) {
        super(message);
    }

    /**
     * @param message
     *            takes in a message.
     * @param cause
     *            and a cause.
     */
    LoginException(final String message, final Exception cause) {
        super(message, cause);
    }
}
