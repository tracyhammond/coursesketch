package connection;

import multiconnection.ServerWebSocket;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;
import internalConnection.AnswerConnectionManager;

@SuppressWarnings("serial")
public class AnswerCheckerServlet extends GeneralConnectionServlet {

    public AnswerCheckerServlet(final long timeoutTime, final boolean secure,
            final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    @Override
    public final ServerWebSocket createServerSocket() {
        return new AnswerCheckerServerWebSocket(this);
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
