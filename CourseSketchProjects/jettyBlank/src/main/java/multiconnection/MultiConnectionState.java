package multiconnection;

/**
 * An object that contains the state of connection with another server.
 *
 * @author gigemjt
 *
 */
public class MultiConnectionState {

    /**
     * The unique key that separates this connection from a different
     * connection.
     */
    private String key;

    /**
     * contains the value of if the connection is pending.
     *
     * This is false if the state has not yet started pending OR if the state
     * already finished pending.
     */
    private boolean pending = false;

    /**
     * Creates a {@link MultiConnectionState} with the given Key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public MultiConnectionState(final String inputKey) {
        this.key = inputKey;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof MultiConnectionState)) {
            return false;
        }
        return ((MultiConnectionState) obj).key == this.key;
    }

    @Override
    public final int hashCode() {
        return key.hashCode();
    }

    /**
     * @return the key
     */
    public final String getKey() {
        return key;
    }

    /**
     * @return true if the state is currently pending, false otherwise.
     */
    public final boolean isPending() {
        return pending;
    }
}
