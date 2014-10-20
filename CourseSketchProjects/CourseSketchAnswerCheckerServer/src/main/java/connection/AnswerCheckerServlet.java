package connection;

import interfaces.IMultiConnectionManager;
import coursesketch.jetty.multiconnection.ServerWebSocket;
import coursesketch.jetty.multiconnection.GeneralConnectionServlet;
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
    protected final IMultiConnectionManager createConnectionManager(
            final boolean connectLocally, final boolean secure) {
        return new AnswerConnectionManager(connectionServer, connectLocally,
                secure);
    }
}
