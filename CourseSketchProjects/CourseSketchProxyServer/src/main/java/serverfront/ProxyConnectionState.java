package serverfront;

import internalconnections.LoginConnectionState;

/**
 * An extension of the login connection state this handles timing and the id.
 */
public final class ProxyConnectionState extends LoginConnectionState {
    /**
     * Creates a {@link interfaces.MultiConnectionState} with the given
     * Key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public ProxyConnectionState(final String inputKey) {
        super(inputKey);
    }

    /**
     * @return the user id of this connection.
     */
    /* package-private */String getUserId() {
        return getSessionId();
    }
}
