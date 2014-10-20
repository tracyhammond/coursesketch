package multiconnection;

import interfaces.IServerWebSocket;
import interfaces.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import netty.annotation.NettyWebSocket;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 10/19/14.
 */
@NettyWebSocket()
public class WebSocketServer extends IServerWebSocket {
    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn the connection that is being opened.
     */
    @Override
    protected void openSession(SocketSession conn) {

    }

    /**
     * Called when an error occurs with the connection.
     *
     * @param session The session that has an error.
     * @param cause   The actual error.
     */
    @Override
    protected void onError(SocketSession session, Throwable cause) {

    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * @param session the session object that created the message
     * @param req
     */
    @Override
    protected void onMessage(SocketSession session, Message.Request req) {

    }

    /**
     * A helper method for sending data given a session.
     * This is a non-blocking way to send messages to a server.
     *
     * @param session The session that the message is being sent with.
     * @param req     The actual message that is being sent.
     */
    @Override
    protected void send(SocketSession session, Message.Request req) {

    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    protected void onStop() {

    }
}
