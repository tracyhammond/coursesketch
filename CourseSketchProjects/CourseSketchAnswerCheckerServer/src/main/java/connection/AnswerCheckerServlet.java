package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import internalConnection.AnswerConnectionManager;

@SuppressWarnings("serial")
public class AnswerCheckerServlet extends ServerWebSocketInitializer {

    public AnswerCheckerServlet(final long timeoutTime, final boolean secure,
            final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new AnswerCheckerServerWebSocketHandler(this);
    }

    /**
     * We do not need to manage multiple connections so we might as well just
     * make it return null
     */
    @Override
    protected final MultiConnectionManager createConnectionManager(
            final boolean connectLocally, final boolean secure) {
        return new AnswerConnectionManager(connectionServer, connectLocally,
                secure);
    }
}
