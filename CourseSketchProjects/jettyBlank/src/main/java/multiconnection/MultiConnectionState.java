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
    private final String key;

    /**
     * contains the value of if the connection is PENDING.
     *
     * This is false if the state has not yet started PENDING OR if the state
     * already finished PENDING.
     */
    private static final boolean PENDING = false;

    /**
     * Creates a {@link MultiConnectionState} with the given Key.
     *
     * @param inputKey
     *            Uniquely Identifies this connection from any other connection.
     */
    public MultiConnectionState(final String inputKey) {
        this.key = inputKey;
    }

    /**
     * Compares the keys of the multi connection state.
     * @param obj Another MultiConnectionState that is being compared to this one.
     * @return true if they are considered equal.
     */
    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof MultiConnectionState)) {
            return false;
        }
        return ((MultiConnectionState) obj).key.equals(this.key);
    }

    /**
     * @return the hashcode of the key.
     */
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
     * @return true if the state is currently PENDING, false otherwise.
     */
    public final boolean isPending() {
        return PENDING;
    }
}
