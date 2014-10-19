package internalConnection;

import connection.ConnectionException;
import interfaces.IServerWebSocket;
import multiconnection.MultiConnectionManager;

public class AnswerConnectionManager extends MultiConnectionManager {
    private static final int PORT = 8883;
    public AnswerConnectionManager(final IServerWebSocket parent,
            final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    @Override
    public final void connectServers(final IServerWebSocket parent) {
        try {
            createAndAddConnection(parent, isConnectionLocal(), "srl02.tamu.edu",
                    PORT, secure, SubmissionConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
