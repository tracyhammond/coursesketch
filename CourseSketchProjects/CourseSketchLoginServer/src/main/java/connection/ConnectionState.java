package connection;

public class ConnectionState {

    private String key;
    private boolean isLoggedIn = false;
    private boolean isInstructor = false; // flagged true if correct login and
                                          // is instructor
    private int previousMessageType = 0;
    private int loginTries = 0;

    public ConnectionState(final String stateKey) {
        this.key = stateKey;
    }

    public final boolean equals(final Object obj) {
        if (!(obj instanceof ConnectionState)) {
            return false;
        }
        return ((ConnectionState) obj).key == this.key;
    }

    public final int hashCode() {
        return key.hashCode();
    }

    public final String getKey() {
        return key;
    }

    public final boolean isLoggedIn() {
        return isLoggedIn;
    }

    public final void addTry() {
        loginTries++;
    }

    public final int getTries() {
        return loginTries;
    }
}
