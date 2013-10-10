package connection;

import protobuf.srl.request.Message.Request;

public class LoginChecker {
	public static boolean checkLogin(Request req) {
		// Check log in information
		// return failed log in information on failure
		if(req.getRequestType() == Request.MessageType.LOGIN) {
			String name = req.getLogin().getUsername();
			String password = req.getLogin().getPassword();
			System.out.println("USERNAME: " + name +"\nPASSWORD: " + password);
			if((name.equalsIgnoreCase("matt") && password.equalsIgnoreCase("japan"))) {
				return true;
			}
		}
		return false;
	}
}
