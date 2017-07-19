package coursesketch.serverfront;

import connection.LoginConnectionState;

/**
 * An extension of the login connection state this handles timing and the id.
 */
public final class ProxyConnectionState extends LoginConnectionState {
    /**
     * Creates a {@link coursesketch.server.interfaces.MultiConnectionState} with the given
     * Key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public ProxyConnectionState(final String inputKey) {
        super(inputKey);
    }

    /**
     * @return the authentication id of the user who logged in (its auth id)
     */
    /* package-private */ String getAuthId() {
        return getServerAuthId();
    }

    /**
     * @return the identification id of the user who logged in (its user id)
     */
    /* package-private */ String getUserId() {
        return getServerUserId();
    }
}
