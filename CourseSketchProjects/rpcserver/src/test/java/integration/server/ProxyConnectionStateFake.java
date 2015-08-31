package integration.server;

/**
 * An extension of the login connection state this handles timing and the id.
 */
public final class ProxyConnectionStateFake extends FakeLoginConnectionState {
    /**
     * Creates a {@link coursesketch.server.interfaces.MultiConnectionState} with the given
     * Key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public ProxyConnectionStateFake(final String inputKey) {
        super(inputKey);
    }

    /**
     * @return the user id of this connection.
     */
    /* package-private */String getUserId() {
        return getSessionId();
    }
}
