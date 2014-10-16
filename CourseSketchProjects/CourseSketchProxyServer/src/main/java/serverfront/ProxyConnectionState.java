package serverfront;

import internalconnections.LoginConnectionState;

public final class ProxyConnectionState extends LoginConnectionState {

    private long lastActive;
    public ProxyConnectionState(final String key) {
		super(key);
	}

    /**
     * @return the user id of this connection.
     */
	/* package-private */ String getUserId() {
		return getSessionId();
	}

	long getTimeSinceLastActive() {
		return System.currentTimeMillis() - lastActive;
	}
	
	public void updateActivityTime() {
		lastActive = System.currentTimeMillis();
	}
}
