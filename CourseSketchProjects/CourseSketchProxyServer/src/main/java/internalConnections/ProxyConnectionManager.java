package internalConnections;

import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import utilities.ConnectionException;
import coursesketch.server.interfaces.MultiConnectionManager;
import protobuf.srl.request.Message.Request;


<<<<<<< HEAD
/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ProxyConnectionManager extends MultiConnectionManager {

	public ProxyConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer serv) {
		//System.out.println("Open Recognition...");
		System.out.println("Open Login...");
		System.out.println(isConnectionLocal());
		System.out.println(secure);
		try {
			createAndAddConnection(serv, isConnectionLocal(), "srl02.tamu.edu", 8886, secure, LoginConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Open Data...");
		try {
			createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", 8885, secure, DataConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Open Answer...");
		try {
			createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", 8884, secure, AnswerConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Open Answer Checker Server...");
		//createAndAddConnection(serv, true, 8884, AnswerConnection.class);
	}

	public static final Request createClientRequest(Request r) {
		Request.Builder build = Request.newBuilder(r);
		build.clearServersideId();
		build.clearSessionInfo();
		return build.build();
	}

	public void send(Request req, String sessionId, Class<? extends ConnectionWrapper> connectionType, String userId) throws ConnectionException {
		Request.Builder builder = Request.newBuilder(req);
		builder.clearServersideId();
		builder.setServersideId(userId);
		super.send(builder.build(), sessionId, connectionType);
	}
}
=======
    /**
     * Port for the login server.
     */
    private static final int LOGIN_PORT = 8886;

    /**
     * Port for the Database server.
     */
    private static final int DATABASE_PORT = 8885;

    /**
     * Port for the Answer checker server.
     */
    private static final int ANSWER_PORT = 8884;

    /**
     * Creates a manager for the proxy connections.
     *
     * @param parent
     *            {@link serverfront.ProxyServerWebSocketHandler}
     * @param connectType
     *            true if connection is local.
     * @param secure
     *            true if all connections should be secure.
     */
    public ProxyConnectionManager(final AbstractServerWebSocketHandler parent, final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    /**
     * connects to other servers.
     *
     * @param serv
     *            an instance of the local server (
     *            {@link serverfront.ProxyServerWebSocketHandler}) in this case.
     */
    @Override
    public void connectServers(final AbstractServerWebSocketHandler serv) {
        // System.out.println("Open Recognition...");
        System.out.println("Open Login...");
        System.out.println(isConnectionLocal());
        System.out.println(isSecure());
        try {
            createAndAddConnection(serv, isConnectionLocal(), "srl02.tamu.edu", LOGIN_PORT, isSecure(), LoginClientWebSocket.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }

        System.out.println("Open Data...");
        try {
            createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", DATABASE_PORT, isSecure(), DataClientWebSocket.class);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Open Answer...");
        try {
            createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", ANSWER_PORT, isSecure(), AnswerClientWebSocket.class);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println("Open Answer Checker Server...");
        // createAndAddConnection(serv, true, 8884, AnswerConnection.class);
    }

    /**
     * Creates a request for the web client and strips out all information that
     * should not be sent to the client.
     *
     * @param request
     *            The request that is being sent.
     * @return A clean version of this request.
     */
    public static Request createClientRequest(final Request request) {
        final Request.Builder build = Request.newBuilder(request);
        build.clearServersideId();
        build.clearSessionInfo();
        return build.build();
    }

    /**
     * Sends a request to a backend server. (clears the request and replaces the
     * id with the server id)
     *
     * @param req
     *            The message being sent to the client.
     * @param sessionId
     *            the session of the connection the message is being sent to.
     * @param connectionType
     *            the type that the connection is being sent to.
     * @param userId
     *            the sever side id.
     * @throws ConnectionException
     *             thrown if there are problems sending the message.
     */
    public void send(final Request req, final String sessionId, final Class<? extends AbstractClientWebSocket> connectionType, final String userId)
            throws ConnectionException {
        final Request.Builder builder = Request.newBuilder(req);
        builder.clearServersideId();
        builder.setServersideId(userId);
        super.send(builder.build(), sessionId, connectionType);
    }
}
>>>>>>> origin/master
