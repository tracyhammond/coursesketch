package database;

public class DatabaseAccessException extends Exception {

	/**
     * 
     */
    private static final long serialVersionUID = -2942324631280650223L;
    
    private boolean recoverable = false;
	public DatabaseAccessException(String string, boolean recoverable) {
		super(string);
		this.recoverable = recoverable;
	}
	
	public DatabaseAccessException(String string) {
		this(string, false);
	}

	/**
	 * Returns true if this is not a serious error and can be recovered.
	 * @return
	 */
	public boolean isRecoverable() {
		return recoverable;
	}
}
