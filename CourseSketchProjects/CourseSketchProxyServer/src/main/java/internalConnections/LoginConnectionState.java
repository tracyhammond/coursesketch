package internalconnections;

import multiconnection.MultiConnectionState;

/**
 * Holds special information about a user being logged in.
 */
public class LoginConnectionState extends MultiConnectionState {

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
     * The id of the current session.
     */
    private String sessionId = null;

    /**
     * creates a login connection state with a certian key.
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
     *
     * @param instructorFlag
     *            true if the user is an instructor.
     * @param iSessionId
     *            the userid from the server for this specific login state.
     */
    /* package-private */final void logIn(final boolean instructorFlag, final String iSessionId) {
        loggedIn = true;
        instructor = instructorFlag;
        this.sessionId = iSessionId;
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
     * @return the session id of the user who logged in (its user id)
     */
    protected final String getSessionId() {
        return sessionId;
    }
}
