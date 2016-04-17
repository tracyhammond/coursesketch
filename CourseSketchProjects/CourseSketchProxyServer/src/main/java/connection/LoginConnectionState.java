package connection;

import coursesketch.server.interfaces.MultiConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds special information about a user being logged in.
 */
public class LoginConnectionState extends MultiConnectionState {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyConnectionManager.class);

    /**
     * true if the user is logged in.
     */
    private boolean loggedIn = false;

    /**
     * true if the user is logged in as an instructor.
     */
    private boolean instructor = false; // flagged true if correct login and
                                          // is instructor

    /**
     * The number of tries that the user has attempted to log in.
     */
    private int loginTries = 0;

    /**
     * The server side id that is used by this particular user for authentication purposes only.
     * (Also called the authentication id)
     */
    private String serverAuthId = null;

    /**
     * The server side id that is used by this particular user for identification purposes only.
     * (Also called the identity id)
     */
    private String serverUserId = null;

    /**
     * creates a login connection state with a certain key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public LoginConnectionState(final String inputKey) {
        super(inputKey);
    }

    /**
     * @return true if the user is logged in false otherwise.
     */
    public final boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Logs in the connection.
     *  @param instructorFlag
     *            true if the user is an instructor.
     * @param authId The authentication id for this specific login state
     * @param userIdentity The userIdentity for this specific login state
     */
    /* package-private */ final void logIn(final boolean instructorFlag, final String authId, final String userIdentity) {
        LOG.debug("logging in user {}", userIdentity);
        loggedIn = true;
        instructor = instructorFlag;
        this.serverAuthId = authId;
        this.serverUserId = userIdentity;
    }

    /**
     * Add a try to the number of login attempts by this connection.
     */
    public final void addTry() {
        loginTries++;
    }

    /**
     * @return number of times the user has attempted to log in.
     */
    public final int getTries() {
        return loginTries;
    }

    /**
     * @return true if the user is acting as an instructor.
     */
    public final boolean isInstructor() {
        return instructor;
    }

    /**
     * @return the authentication id of the user who logged in (its auth id)
     */
    protected final String getServerAuthId() {
        return serverAuthId;
    }


    /**
     * @return the identification id of the user who logged in (its user id)
     */
    protected final String getServerUserId() {
        return serverUserId;
    }
}
