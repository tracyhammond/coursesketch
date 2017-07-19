package coursesketch.database;

import utilities.CourseSketchException;

/**
 * Exceptions that occur during registration.
 *
 * Created by gigemjt on 10/17/14.
 */
public class RegistrationException extends CourseSketchException {
    /**
     * @param message
     *            takes in a message.
     */
    RegistrationException(final String message) {
        super(message);
    }
}
