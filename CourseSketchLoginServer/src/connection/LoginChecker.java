package connection;

import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

public class LoginChecker {
	public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect username or password";
	public static final String INCORRECT_LOGIN_TYPE_MESSAGE = "You do not have the ability to login as that type!";
	public static final String PERMISSION_ERROR_MESSAGE = "There was an error assigning permissions";

	public static final int STUDENT_LOGIN = 0;
	public static final int INSTRUCTOR_LOGIN = 1;
	public static final int ERROR = -1;
	
	/* package-private */ static Request checkLogin(Request req, ConnectionState state) {
		// Check log in information
		// return failed log in information on failure
		if (req.getRequestType() == Request.MessageType.LOGIN && req.hasLogin()) {
			LoginInformation loginInfo = req.getLogin();
			if (checkLogin(loginInfo)) {
				int loginType = checkInstructor(loginInfo);
				Permission permission = Permission.NO_PERMISSION;
				boolean instructor = false;
				boolean success = true;
				String message = "";
				switch (loginType) {
					case LoginChecker.INSTRUCTOR_LOGIN:
						System.out.println("Welcome instructor");
						permission = LoginChecker.assignPermission(state,req);
						if (permission == Permission.ERROR_PERMISSON) {
							success = false;
							message = PERMISSION_ERROR_MESSAGE;
							System.out.println("ERROR PERMISSIONS");
						} else {
							instructor = true;
						}
					break;

					case LoginChecker.ERROR:
						success = false;
						message = INCORRECT_LOGIN_TYPE_MESSAGE;
					break;

					default:
				}

				if (success) {
					state.logIn(instructor, permission);
				}
				return createLoginResponse(req, success, message, instructor);
			} else {
				state.addTry();
				return createLoginResponse(req, false, INCORRECT_LOGIN_MESSAGE, false);
			}
		}
		return createLoginResponse(req, false, "An Error Occured While Logging in: Wrong Message Type.", false);
	}

	/**
	 * Checks the login information to see if it is valid.
	 */
	private static boolean checkLogin(LoginInformation information) {
		String name = information.getUsername();
		String password = information.getPassword();
		if (name == null || password == null) {
			return false;
		}

		if (name.equalsIgnoreCase("student") && password.equalsIgnoreCase("pass")) {
			System.out.println("Logging in as student");
			return true;
		}

		if (name.equalsIgnoreCase("instructor") && password.equalsIgnoreCase("pass")) {
			System.out.println("Logging in as instructor");
			return true;
		}

		if (name.equalsIgnoreCase("matt") && password.equalsIgnoreCase("japan")) {
			System.out.println("Logging in as matt");
			return true;
		}

		if (information.hasSessionInfo() && information.getSessionInfo().equals("SESSION_KEY")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks to see if the request to login as an instructor is valid.
	 *
	 * If the request asked to log in as a student then it returns a student type immediately.
	 * If there is an error {@link LoginChecker#ERROR} is returned.
	 * This method will not throw any exception.
	 */
	protected static int checkInstructor(LoginInformation login) {
		if (!login.hasIsInstructor() && login.hasUsername()) 
			return ERROR;
		/*
		 * TODO: Check server for whether the person is an instructor or a student.
		 */
		String name = login.getUsername();
		if (name.equalsIgnoreCase("student")) {
			return STUDENT_LOGIN;
		}

		if (name.equalsIgnoreCase("instructor")) {
			return INSTRUCTOR_LOGIN;
		}
		return ERROR;
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

	/**
	 * Creates a {@link Request} to return on login request.
	 */
	/* package-private */private static Request createLoginResponse(Request req, boolean success, String message, boolean instructorIntent) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		requestBuilder.setResponseText(message);
		
		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		loginBuilder.setIsInstructor(instructorIntent);
		loginBuilder.setSessionInfo("SESSION_KEY");

		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		// Build and send.
		return requestBuilder.build();
	}
}
