package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.drafts.Draft;

import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class LoginConnection extends WrapperConnection {

	public LoginConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent, Request req, LoginConnectionState state) {
		this( serverUri, draft );
		// get connection state
		// if log success = state -> login
		// if log fail state -> addTry
		// state -> stopPending
		// send result
		//boolean instructor = false;
	
		//return createLoginResponse(req, false, "An Error Occured While Logging in: Wrong Message Type.", false);
	}

	public LoginConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {

		this( serverUri, draft );
	}

	public void onMessage(ByteBuffer buffer) {
		Request r = MultiInternalConnectionServer.Decoder.parseRequest(buffer);
		LoginConnectionState state = (LoginConnectionState) getStateFromId(r.getSessionInfo());
		if (r.getLogin().getIsLoggedIn()) {
			state.logIn(r.getLogin().getIsInstructor(), r.getSessionId());
		}

		System.out.println("is logged in? " + r.getLogin().getIsLoggedIn());
		System.out.println("session info? " + r.getSessionInfo());
		System.out.println("response " + r.getResponseText());
		System.out.println("instructor " + r.getLogin().getIsInstructor());
		System.out.println("userId " + r.getSessionId());
		System.out.println("otherUserId " + r.getLogin().getUserId());

		Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
		getConnectionFromState(state).send(result.toByteArray());
	}

	public LoginConnection( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public LoginConnection( URI serverURI ) {
		super( serverURI );
	}
}