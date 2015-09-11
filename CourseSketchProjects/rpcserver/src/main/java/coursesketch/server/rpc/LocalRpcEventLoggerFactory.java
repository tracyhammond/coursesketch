package coursesketch.server.rpc;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import org.slf4j.Logger;

/**
 * A factory for creating A logger for RpcEvents.
 *
 * Created by gigemjt on 9/3/15.
 */
public final class LocalRpcEventLoggerFactory {

    /**
     * private constructor.
     */
    private LocalRpcEventLoggerFactory() {
    }

    /**
     * A local event logger.
     *
     * @param log The logger that will be logging the information.
     * @return An RpcConnectionEventNotifier that will notify of an event.
     */
    public static RpcConnectionEventNotifier createLocalEventLogger(final Logger log) {
        // setup a RPC event listener - it just logs what happens
        final RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
        @SuppressWarnings("PMD.CommentRequired")
        final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

            @Override
            public void connectionReestablished(final RpcClientChannel clientChannel) {
                log.info("connectionReestablished " + clientChannel);
            }

            @Override
            public void connectionOpened(final RpcClientChannel clientChannel) {
                log.info("connectionOpened " + clientChannel);
            }

            @Override
            public void connectionLost(final RpcClientChannel clientChannel) {
                log.info("connectionLost " + clientChannel);
            }

            @Override
            public void connectionChanged(final RpcClientChannel clientChannel) {
                log.info("connectionChanged " + clientChannel);
            }
        };
        rpcEventNotifier.setEventListener(listener);
        return rpcEventNotifier;
    }
}
