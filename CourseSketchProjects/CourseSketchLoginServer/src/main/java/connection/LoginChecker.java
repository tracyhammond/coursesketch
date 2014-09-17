package connection;

import protobuf.srl.request.Message.LoginInformation;

public final class LoginChecker {
    /**
     * Empty constructor; this is a utility class.
     */
    private LoginChecker() {

    }

    public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect username "
            + "or password";
    public static final String INCORRECT_LOGIN_TYPE_MESSAGE = "You do not have "
            + "the ability to login as that type!";
    public static final String PERMISSION_ERROR_MESSAGE = "There was an error "
            + "assigning permissions";

    public static final int STUDENT_LOGIN = 0;
    public static final int INSTRUCTOR_LOGIN = 1;
    public static final int ERROR = -1;

    /* private static Request checkLogin(final Request req,
            final ConnectionState state) {
        // Check log in information
        // return failed log in information on failure
        if (req.getRequestType() == Request.MessageType.LOGIN
                && req.hasLogin()) {
            LoginInformation loginInfo = req.getLogin();
            if (checkLogin(loginInfo)) {
                int loginType = checkInstructor(loginInfo);
                Permission permission = Permission.NO_PERMISSION;
                boolean instructor = false;
                boolean success = true;
                String message = "";
                switch (loginType) {
                case LoginChecker.INSTRUCTOR_LOGIN:
                    System.out.println("Welcome " + "instructor");
                    permission = LoginChecker.assignPermission(state, req);
                    if (permission == Permission.ERROR_PERMISSON) {
                        success = false;
                        message = PERMISSION_ERROR_MESSAGE;
                        System.out.println("ERROR " + "PERMISSIONS");
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
            }
        }
    } */

    /**
     * Checks to see if the request to login as an instructor is valid.
     *
     * If the request asked to log in as a student then it returns a student
     * type immediately. If there is an error {@link LoginChecker#ERROR} is
     * returned. This method will not throw any exception.
     *
     *  @param login object containing login information to check against
     *
     * @return <code>connection.LoginChecker.STUDENT_LOGIN</code>,
     *         <code>connection.LoginChecker.INSTRUCTOR_LOGIN</code>, or
     *         <code>connection.LoginChecker.ERROR</code>
     */
    protected static int checkInstructor(final LoginInformation login) {
        if (!login.hasIsInstructor() && login.hasUsername()) {
            return ERROR;
        }
        /*
         * TODO Check server for whether the person is an instructor or a
         * student.
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

}
