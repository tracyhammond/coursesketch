package connection;

public class ConnectionState {
/***
 * testing first push
 */
	private String key;
	private boolean isLoggedIn = false;
	private boolean isInstructor = false; // flagged true if correct login and is instructor
	private int previousMessageType = 0;
	private int loginTries = 0;
	
	public ConnectionState(String key) {
		this.key = key;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ConnectionState)) {
		return false;
		}
		return ((ConnectionState)obj).key == this.key;
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void addTry() {
		loginTries++;
	}
	
	public int getTries() {
		return loginTries;
	}
}