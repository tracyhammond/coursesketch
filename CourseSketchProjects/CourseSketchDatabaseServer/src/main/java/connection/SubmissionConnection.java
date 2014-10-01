package connection;

import java.net.URI;
import java.nio.ByteBuffer;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ExperimentReview;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class SubmissionConnection extends ConnectionWrapper {

    /**
     * @see ConnectionWrapper#ConnectionWrapper(URI, GeneralConnectionServer).
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parentServer
     *            The server that is using this connection wrapper.
     */
    public SubmissionConnection(final URI destination, final GeneralConnectionServer parentServer) {
        super(destination, parentServer);
    }

    /**
     * Splits the session info to find the correct level above to pass it up the
     * chain to the correct client.
     *
     * @param buffer Contains the sketch itself that is being passed.
     */
    @Override
    public final void onMessage(final ByteBuffer buffer) {
        final Request req = GeneralConnectionServer.Decoder.parseRequest(buffer);
        System.out.println("Got a response from the submission server!");
        System.out.println(req.getSessionInfo());
        final String[] sessionInfo = req.getSessionInfo().split("\\+");
        System.out.println(sessionInfo[1]);
        final MultiConnectionState state = getStateFromId(sessionInfo[1]);
        System.out.println(state);
        if (req.getRequestType() == MessageType.DATA_REQUEST) {
            final DataResult.Builder result2 = DataResult.newBuilder();
            // pass up the Id to the client
            try {
                final DataResult result = DataResult.parseFrom(req.getOtherData());
                result2.clearResults();
                for (ItemResult item : result.getResultsList()) {
                    if (item.hasAdvanceQuery() && item.getQuery() == ItemQuery.EXPERIMENT) {
                        // we might have to do a lot of work here!
                        final ExperimentReview rev = ExperimentReview.parseFrom(item.getAdvanceQuery());
                        if (rev.getShowUserNames()) {
                            System.err.println("Attempting to change out usernames!");
                            result2.addResults(item);
                        }
                    } else {
                        result2.addResults(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Request.Builder builder = Request.newBuilder(req);
            builder.setSessionInfo(sessionInfo[0]);
            final Session connection = getConnectionFromState(state);
            builder.setOtherData(result2.build().toByteString());
            if (connection == null) {
                System.err.println("SOCKET IS NULL");
            }
            GeneralConnectionServer.send(getConnectionFromState(state), builder.build());
        }
    }
}
