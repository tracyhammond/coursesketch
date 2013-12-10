package database.auth;

public class AuthenticationException extends Exception {

	public AuthenticationException(int earlyAccess) {
	}
	public static final int INVALID_PERMISSION = 0;
	public static final int EARLY_ACCESS = 1;
	
	public String getMessageFromValue(int value) {
		switch(value) {
			case EARLY_ACCESS: return "Can only access during valid times";
			case INVALID_PERMISSION: return "Can perform task with valid permission";
		}
		return null;
	}
}
