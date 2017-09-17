package coursesketch.server.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * An Rpc Session that is only created server side.
 */
public class ServerRpcSession extends RpcSession {
    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerRpcSession.class);

    /**
     * Used as a callback.
     */
    private final RpcCallback<Message.Request> callback;

    /**
     * Creates a new session with the controller.
     * @param session The Callback data.
     * @param controller The controller.
     */
    ServerRpcSession(final RpcCallback<Message.Request> session, final RpcController controller) {
        super(controller);
        callback = session;
    }

    @Override
    public final String getRemoteAddress() {
        return null;
    }

    @Override
    public final void close() {
        callback.run(null);
    }

    @Override
    public final Future<Void> send(Message.Request req) {
        callback.run(req);
        return null;
    }

    @Override
    public final Future<Void> send(ByteBuffer buffer) {
        try {
            return send(Message.Request.parseFrom(buffer.array()));
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Unable to send request", e);
        }
        return null;
    }

    @Override
    public final void close(int statusCode, String reason) {
        close();
    }
}
