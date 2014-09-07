package connection;

import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

public class LoginChecker {
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
	
	/* package-private */ static Request createResponse(Request req, boolean success) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		
		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		
		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		
		// Build and send.
		return requestBuilder.build();
	}
}
