package database;

/**
 * Created by gigemjt on 10/17/14.
 */
public class LoginException extends Exception {
    /**
     * @param message takes in a message.
     */
    public LoginException(final String message) {
        super(message);
    }

    /**
     * @param message takes in a message.
     * @param cause and a cause.
     */
    public LoginException(final String message, final Exception cause) {
        super(message, cause);
    }
}
