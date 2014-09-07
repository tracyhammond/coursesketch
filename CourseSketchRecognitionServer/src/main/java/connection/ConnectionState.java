package connection;

public class ConnectionState {

	private int key;
	private boolean isLoggedIn = false; 
	private int loginTries = 0;
	
	public ConnectionState(int key) {
		this.key = key;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ConnectionState)) {
		return false;
		}
		return ((ConnectionState)obj).key == this.key;
	}
	
	public int hashCode() {
		return key;
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	/* package-private */ void logIn() {
		isLoggedIn = true;
	}

	public void addTry() {
		loginTries++;
	}
	
	public int getTries() {
		return loginTries;
	}
}
