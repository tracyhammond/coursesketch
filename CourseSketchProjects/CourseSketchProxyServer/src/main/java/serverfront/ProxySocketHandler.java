package serverfront;

import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralSocketHandler;

/**
 * Created by gigemjt on 10/18/14.
 */
public class ProxySocketHandler extends GeneralSocketHandler {
    /**
     * @param timeoutTime
     *            The time it takes before a connection times out.
     * @param secure
     *            True if the connection is allowing SSL connections.
     * @param connectLocally
     *            True if the server is connecting locally.
     */
    public ProxySocketHandler(final long timeoutTime, final boolean secure, final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link GeneralConnectionServer}
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Class<? extends GeneralConnectionServer> createServerSocket() {
        return ProxyServer.class;
    }
}
