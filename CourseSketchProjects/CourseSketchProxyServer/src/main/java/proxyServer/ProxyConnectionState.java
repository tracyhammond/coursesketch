package proxyServer;

import internalConnections.LoginConnectionState;

public final class ProxyConnectionState extends LoginConnectionState {

    private long lastActive;
    public ProxyConnectionState(final String key) {
		super(key);
	}

	protected String getUserId() {
		return this.sessionId;
	}
	
	
	long getTimeSinceLastActive() {
		return System.currentTimeMillis() - lastActive;
	}
	
	public void updateActivityTime() {
		lastActive = System.currentTimeMillis();
	}
}
