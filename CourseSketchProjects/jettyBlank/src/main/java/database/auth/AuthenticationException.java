package database.auth;

/**
 * An exception thrown while authenticating.
 *
 * @author gigemjt
 */
public class AuthenticationException extends Exception {

    /**
     * Contains the type of issue with the authentication.
     */
    private final int exceptionType;

    /**
     * Accepts an authentication type.
     *
     * @param value
     *            An authetication exception type.
     */
    public AuthenticationException(final int value) {
        super(getMessageFromValue(value));
        exceptionType = value;
    }

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
     * @param value An exception type.
     * @return a message associated with each exception type.
     */
    public static String getMessageFromValue(final int value) {
        switch (value) {
            case INVALID_DATE:
                return "Can only access during valid times";
            case INVALID_PERMISSION:
                return "Can only perform task with valid permission";
            case NO_AUTH_SENT:
                return "No Authentication Information was recieved";
            default:
            break;
        }
        return null;
    }

    /**
     * @return The exception type.
     */
    public final int getType() {
        return exceptionType;
    }
}
