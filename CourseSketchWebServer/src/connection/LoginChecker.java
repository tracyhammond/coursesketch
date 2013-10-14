package connection;

import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

public class LoginChecker {
	
	public static final int STUDENT_LOGIN = 0;
	public static final int INSTRUCTOR_LOGIN = 1;
	public static final int ERROR = -1;
	
	/* package-private */ static boolean checkLogin(Request req) {
		// Check log in information
		// return failed log in information on failure
		if(req.getRequestType() == Request.MessageType.LOGIN) {
			String name = req.getLogin().getUsername();
			String password = req.getLogin().getPassword();
			System.out.println("USERNAME: " + name +"\nPASSWORD: " + password);
			if( (name.equalsIgnoreCase("matt") && password.equalsIgnoreCase("japan"))) {
				return true;
			}
		}
		return false;
	}
	
	/* package-private */ static Request createLoginResponse(Request req, boolean success, String message, boolean instructorIntent) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		requestBuilder.setResponseText(message);
		
		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		loginBuilder.setIsInstructor(instructorIntent);

		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		
		// Build and send.
		return requestBuilder.build();
	}

	/**
	 * Checks to see if the request to login as an instructor is valid.
	 *
	 * If the request asked to log in as a student then it returns a student type immediately.
	 * If there is an error {@link LoginChecker#ERROR} is returned.
	 * This method will not throw any exception.
	 */
	public static int checkInstructor(Request req) {
		if (!req.hasLogin()) 
			return ERROR;
		if (!req.getLogin().getIsInstructor()) {
			return STUDENT_LOGIN; // Everyone is a student.
		} else {
			// Check for the ability to be an instructor
			return INSTRUCTOR_LOGIN; // EVERYONE IS AN INSTRUCTOR! TODO: make some people not an instructor.
		}
	}

	/**
	 * Returns the permission that is allowed by this user.
	 *
	 * Returns {@link Permission#ERROR_PERMISSON} if there is an error at any point.
	 * This method will not throw any exception.
	 */
	public static Permission assignPermission(ConnectionState state, Request req) {
		// TODO Auto-generated method stub
		System.out.println("Applying instructor permission");
		return Permission.INSTRUCTOR;
	}
}
