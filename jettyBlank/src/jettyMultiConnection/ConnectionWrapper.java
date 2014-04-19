package jettyMultiConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * Basic Echo Client Socket
 */
@WebSocket()
public class ConnectionWrapper {

	public static final int CLOSE_ABNORMAL = 1006;
	public static final String CLOSE_EOF = "(EOF)*";
	private static final int MAX_FAILED_STARTS = 10;
	protected GeneralConnectionServer parentServer;
	protected MultiConnectionManager parentManager;

	private WebSocketClient client;
    private Session session;
    private URI destination;
    private boolean connected = false;
    private boolean EOFReached = false;
    private boolean started = false;
    private int failedStarts = 0;
    private boolean queing = false;
    ArrayList<ByteBuffer> queuedMessages = new ArrayList<ByteBuffer>();
    ActionListener socketFailedListener;

	public ConnectionWrapper(URI destination, GeneralConnectionServer parentServer) {
    	this.parentServer = parentServer;
    	this.destination = destination;
    	started = false;
    }

	protected void setFailedSocketListener(ActionListener listen) {
		socketFailedListener = listen;
	}
	
	public void connect() throws Throwable {
		client = new WebSocketClient();
		try {
			client.start();
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(this, destination, request);
			} catch (Throwable t) {
				t.printStackTrace();
				client.stop();
				throw t;
			}
	}

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    	connected = false;
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        if (statusCode == CLOSE_ABNORMAL && reason.matches(CLOSE_EOF) || EOFReached) {
        	EOFReached = true;
        }
        this.session = null;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
    	failedStarts = 0;
    	started = true;
    	EOFReached = false;
    	connected = true;
    	queing = false;
    	this.session = session;
        System.out.println("Connection was succesful for: " + this.getClass().getSimpleName());
    }

    @SuppressWarnings("unused")
	@OnWebSocketMessage
    public final void onMessage(byte[] data, int offset, int length) {
    	onMessage(ByteBuffer.wrap(data));
    }

    @SuppressWarnings("static-method")
   	@OnWebSocketError
   	public void onError(Session session, Throwable cause) {
    	if (cause instanceof java.io.EOFException || cause instanceof java.net.SocketTimeoutException) {
    		EOFReached = true;
    		System.out.println("This websocket timed out!");
    	}
    	if (session != null) {
    		System.err.println("Socket: " + this.getClass().getSimpleName() + "Session Error: " + cause);
    	} else {
    		System.err.println("Socket: " + this.getClass().getSimpleName() + " Error: " + cause);
    		if (cause instanceof java.net.ConnectException) {
    			//nonExistantServer = true;
    			// TODO: log this error
    		}
    	}
	}

    /**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
    public void onMessage(ByteBuffer buffer) {
    	MultiConnectionState state = getStateFromId(GeneralConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());
    	session.getRemote().sendBytesByFuture(buffer);
		getConnectionFromState(state).getRemote().sendBytesByFuture(buffer);
	}

    protected MultiConnectionState getStateFromId(String key) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToState() == null) {
			System.out.println("null getIdToState");
		}
		return parentServer.getIdToState().get(key);
	}

	protected Session getConnectionFromState(MultiConnectionState state) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToConnection() == null) {
			System.out.println("null IdToConnection");
		}
		return parentServer.getIdToConnection().get(state);
	}
	
	/**
	 * Sends a binary message over the connection.
	 * 
	 * If the connection fails then a reconnect is attempted.  If the attempt fails more than 10 times then an exception is thrown
	 * @param buffer
	 * @throws ConnectionException 
	 */
	public void send(final ByteBuffer buffer) throws ConnectionException {
		if (connected) {
			System.out.println("Sending message from: " + this.getClass().getSimpleName());
			session.getRemote().sendBytesByFuture(buffer);
			if (queing) {
				while (queuedMessages.size() > 0) {
					session.getRemote().sendBytesByFuture(queuedMessages.remove(0));
				}
				queing = false;
			}
		} else if ((started || EOFReached) && failedStarts < MAX_FAILED_STARTS) {
			System.err.println("Trying to reconnect " + this.getClass().getSimpleName() + ". It has ended because of a timeout");
			if (!queing) {
				failedStarts += 1;
				System.err.println("attempt " + failedStarts +" out of " + MAX_FAILED_STARTS);
				queing = true;
				// maybe try reconnecting here?
				Thread d = new Thread() {
					@Override
					public void run() {
						try {
							connect();
							Thread.sleep(1000);
							queing = false;
							send(buffer);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				};
				d.start();
			} else {
				System.err.println("Adding a queuedMessage");
				queuedMessages.add(buffer);
			}
		} else if (failedStarts < MAX_FAILED_STARTS) { // connections has not been established yet
			System.err.println("Trying to wait on " + this.getClass().getSimpleName() + ". It has not connected yet.");
			if (!queing) {
				queing = true;
				failedStarts += 1;
				System.err.println("attempt " + failedStarts +" out of " + MAX_FAILED_STARTS);
				Thread d = new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
							queing = false;
							send(buffer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ConnectionException e) {
							e.printStackTrace();
						}
					}
				};
				d.start();
			} else {
				System.err.println("Adding a queuedMessage");
				queuedMessages.add(buffer);
			}
		} else if (failedStarts >= MAX_FAILED_STARTS) { // failedStarts >= MAX_FAILED_STARTS
			queing = false;
			System.out.println(failedStarts);
			System.out.println(MAX_FAILED_STARTS);
			queuedMessages.add(buffer); // adds this version because it has not been added before
			System.err.println(this.getClass().getSimpleName() + " failed to connect after multiple tries");
			socketFailedListener.actionPerformed(new ActionEvent(queuedMessages, 0, this.getClass().getName()));
			queuedMessages.clear(); // all messages are empty after the actions is finished
			throw new ConnectionException("" + this.getClass().getSimpleName() + " failed to connect after multiple tries");
		}
	}

	/**
	 * Sends a binary message over the connection
	 * @param bytes
	 * @throws ConnectionException 
	 */
	public final void send(byte[] bytes) throws ConnectionException {
		send(ByteBuffer.wrap(bytes));
	}

	public void close() {
		System.out.println("Closing connection: " + this.getClass().getSimpleName());
		if (session != null) {
			session.close();
		}
	}

	public void close(int statusCode, String args) {
		System.out.println("Closing connection: " + this.getClass().getSimpleName());
		if (session != null) {
			session.close(statusCode, args);
		}
	}

	/**
	 * Returns a copy of the URI that is also normalized
	 * @return
	 */
	public URI getURI() {
		return destination.normalize();
	}

	public boolean isConnected() {
		return connected;
	}
}
