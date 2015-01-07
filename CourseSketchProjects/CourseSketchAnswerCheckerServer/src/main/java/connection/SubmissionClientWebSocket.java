package connection;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class SubmissionClientWebSocket extends ClientWebSocket {

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ClientWebSocket#connect()} or call
     * {@link ClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param destination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param parentServer
     *         The server that is using this connection wrapper.
     */
    public SubmissionClientWebSocket(final URI destination,
            final AbstractServerWebSocketHandler parentServer) {
        super(destination, parentServer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onMessage(final ByteBuffer buffer) {
        final Request req = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer); // this
        // contains
        // the
        // solution
        System.out.println(req.getSessionInfo());
        final String[] sessionInfo = req.getSessionInfo().split("\\+");
        System.out.println(sessionInfo[1]);
        final AnswerConnectionState state = (AnswerConnectionState) getStateFromId(sessionInfo[1]);
        System.out.println(state);
        if (req.getRequestType() == MessageType.DATA_REQUEST) {
            // SrlExperiment expr = state.getExperiment(sessionInfo[1]);
            // SrlSolution sol = null;
            /*
             * try { sol = SrlSolution.parseFrom(req.getOtherData()); } catch
             * (InvalidProtocolBufferException e) { e.printStackTrace(); }
             */
            // FIXME: implement comparison.
            // this could take a very very long time!

            // we need to this at least
            final Request.Builder builder = Request.newBuilder(req);
            builder.setSessionInfo(sessionInfo[0]);
            this.getParentServer().send(getConnectionFromState(state),
                    builder.build());
        } else if (req.getRequestType() == MessageType.SUBMISSION) {
            // pass up the Id to the client
            final Request.Builder builder = Request.newBuilder(req);
            builder.setSessionInfo(sessionInfo[0]);
            final SocketSession connection = getConnectionFromState(state);
            if (connection == null) {
                System.err.println("SOCKET IS NULL");
            }
            this.getParentServer().send(getConnectionFromState(state),
                    builder.build());
        }
    }
}
