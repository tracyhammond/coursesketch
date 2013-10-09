package connection;

public class ConnectionState {

	private int key;
	private boolean isLoggedIn = false; 
	private int previousMessageType = 0;
	
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
	
	public void logIn() {
		isLoggedIn = true;
	}
}
