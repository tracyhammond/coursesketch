package internalConnections;

import multiConnection.MultiConnectionState;

public class LoginConnectionState extends MultiConnectionState {

	private boolean isLoggedIn = false;
	private boolean isInstructor = false; // flagged true if correct login and is instructor
	private int previousMessageType = 0;
	private int loginTries = 0;
	
	public LoginConnectionState(String key) {
		super(key);
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	/* package-private */ void logIn(boolean instructorFlag) {
		isLoggedIn = true;
		isInstructor = instructorFlag;
	}

	public void addTry() {
		loginTries++;
	}
	
	public int getTries() {
		return loginTries;
	}
}