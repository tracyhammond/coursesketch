package coursesketch.database.auth;

import utilities.CourseSketchException;

/**
 * An exception thrown while authenticating.
 *
 * @author gigemjt
 */
public class AuthenticationException extends CourseSketchException {

    /**
     * Indicates that the exception thrown is one of invalid permission.
     */
    public static final int INVALID_PERMISSION = 0;

    /**
     * Indicates that the exception thrown means that the date is invalid.
     */
    public static final int INVALID_DATE = 1;

    /**
     * Indicates that the exception thrown means that there was no
     * authentication sent to the server.
     */
    public static final int NO_AUTH_SENT = 2;

    /**
     * Indicates that the exception thrown is not known by its type.
     */
    public static final int OTHER = 3;

    /**
     * Indicates that something occured to prevent a good hash from being created.
     */
    public static final int INSUFFICIENT_HASH = 4;

    /**
     * Contains the type of issue with the authentication.
     */
    private final int exceptionType;

    /**
     * Accepts an authentication type.
     *
     * @param value
     *         An authentication exception type.
     */
    public AuthenticationException(final int value) {
        super(getMessageFromValue(value));
        exceptionType = value;
    }

    /**
     * Accepts an authentication type.
     *
     * @param exception
     *         What started an exception.
     */
    public AuthenticationException(final Exception exception) {
        super(exception);
        exceptionType = OTHER;
    }

    /**
     * Accepts an authentication type and a message.
     *
     * @param message
     *         A custom message to add more details.
     * @param value
     *         An authentication exception type.
     */
    public AuthenticationException(final String message, final int value) {
        super(getMessageFromValue(value) + message);
        exceptionType = value;
    }

    /**
     * @param value
     *         An exception type.
     * @return a message associated with each exception type.
     */
    public static String getMessageFromValue(final int value) {
        switch (value) {
            case INVALID_DATE:
                return "Can only access during valid times: ";
            case INVALID_PERMISSION:
                return "Can only perform task with valid permission: ";
            case NO_AUTH_SENT:
                return "No Authentication Information was received: ";
            case INSUFFICIENT_HASH:
                return "A problem occurred so a hash of sufficient strength could not be created: ";
            default:
                return null;
        }
    }

    /**
     * @return The exception type.
     */
    public final int getType() {
        return exceptionType;
    }
}
