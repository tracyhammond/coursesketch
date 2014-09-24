package internalConnection;

import connection.ConnectionException;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;

public class AnswerConnectionManager extends MultiConnectionManager {
    private static final int PORT = 8883;
    public AnswerConnectionManager(final GeneralConnectionServer parent,
            final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    @Override
    public final void connectServers(final GeneralConnectionServer parent) {
        try {
            createAndAddConnection(parent, connectLocally, "srl02.tamu.edu",
                    PORT, secure, SubmissionConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
