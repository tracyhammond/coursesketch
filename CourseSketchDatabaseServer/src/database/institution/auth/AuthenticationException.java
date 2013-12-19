package database.institution.auth;

public class AuthenticationException extends Exception {

	private int exceptionType;
	public AuthenticationException(int value) {
		super(getMessageFromValue(value));
		exceptionType = value;
	}
	public static final int INVALID_PERMISSION = 0;
	public static final int INVALID_DATE = 1;
	public static final int NO_AUTH_SENT = 2;
	
	public static String getMessageFromValue(int value) {
		switch(value) {
			case INVALID_DATE: return "Can only access during valid times";
			case INVALID_PERMISSION: return "Can only perform task with valid permission";
			case NO_AUTH_SENT: return "No Authentication Information was recieved";
		}
		return null;
	}

	public int getType() {
		return exceptionType;
	}
}
