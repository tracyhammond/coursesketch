package internalConnection;

import interfaces.AbstractServerWebSocketHandler;
import utilities.ConnectionException;
import interfaces.MultiConnectionManager;

public class AnswerConnectionManager extends MultiConnectionManager {
    private static final int PORT = 8883;
    public AnswerConnectionManager(final AbstractServerWebSocketHandler parent,
            final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    @Override
    public final void connectServers(final AbstractServerWebSocketHandler parent) {
        try {
            createAndAddConnection(parent, isConnectionLocal(), "srl02.tamu.edu",
                    PORT, secure, SubmissionClientConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
