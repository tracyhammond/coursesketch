package jettyMultiConnection;

public class MultiConnectionState {

	private String key;
	private boolean pending = false;

	public MultiConnectionState(String key) {
		this.key = key;
	}

	public boolean equals(Object obj) {
		if(!(obj instanceof MultiConnectionState)) {
		return false;
		}
		return ((MultiConnectionState)obj).key == this.key;
	}

	public int hashCode() {
		return key.hashCode();
	}

	public String getKey() {
		return key;
	}

	public boolean isPending() {
		return pending;
	}
}