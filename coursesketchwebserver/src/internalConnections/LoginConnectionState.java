package internalConnections;

import jettyMultiConnection.MultiConnectionState;

public class LoginConnectionState extends MultiConnectionState {

	private boolean isLoggedIn = false;
	private boolean isInstructor = false; // flagged true if correct login and is instructor
	private int loginTries = 0;
	protected String sessionId = null;
	
	public LoginConnectionState(String key) {
		super(key);
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	/* package-private */ void logIn(boolean instructorFlag, String sessionId) {
		isLoggedIn = true;
		isInstructor = instructorFlag;
		this.sessionId = sessionId;
	}

	public void addTry() {
		loginTries++;
	}
	
	public int getTries() {
		return loginTries;
	}
	
	public boolean getIsInstructor() {
		return isInstructor;
	}
}