package proxyServer;

import internalConnections.LoginConnectionState;

public final class ProxyConnectionState extends LoginConnectionState {

	public ProxyConnectionState(String key) {
		super(key);
	}

	protected String getUserId() {
		return this.sessionId;
	}
}
