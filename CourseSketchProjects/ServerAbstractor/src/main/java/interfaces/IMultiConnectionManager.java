package interfaces;

import utilities.ConnectionException;
import protobuf.srl.request.Message;

import java.awt.event.ActionListener;

/**
 * Created by gigemjt on 10/19/14.
 */
public interface IMultiConnectionManager {
    /**
     * This value signifies that the server will connect to a local host.
     */
    boolean CONNECT_LOCALLY = true;
    /**
     * This value signifies that the server will connect to a remote host.
     */
    boolean CONNECT_REMOTE = false;

    @SuppressWarnings("checkstyle:designforextension")
    void send(Message.Request req, String sessionID, Class<? extends IConnectionWrapper> connectionType) throws ConnectionException;

    void createAndAddConnection(IServerWebSocket serv, boolean isLocal, String remoteAdress, int port,
                                boolean isSecure, Class<? extends IConnectionWrapper> connectionType) throws ConnectionException;

    void setFailedSocketListener(ActionListener listen, Class<? extends IConnectionWrapper> connectionType);

    void connectServers(IServerWebSocket parentServer);

    void addConnection(IConnectionWrapper connection, Class<? extends IConnectionWrapper> connectionType);

    @SuppressWarnings("checkstyle:designforextension")
    IConnectionWrapper getBestConnection(Class<? extends IConnectionWrapper> connectionType);

    void dropAllConnection(boolean clearTypes, boolean debugPrint);
}
