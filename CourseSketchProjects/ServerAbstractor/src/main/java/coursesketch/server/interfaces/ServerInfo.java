package coursesketch.server.interfaces;

/**
 * Created by gigemjt on 9/3/15.
 */
public class ServerInfo {
    private final String hostName;
    private final int port;
    private final long timeOut;
    private final boolean isSecure;
    private final boolean isLocal;

    public ServerInfo(final String hostName, final int port, final long timeOut, final boolean isSecure, final boolean isLocal) {
        this.hostName = hostName;
        this.port = port;
        this.timeOut = timeOut;
        this.isSecure = isSecure;
        this.isLocal = isLocal;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public boolean isLocal() {
        return isLocal;
    }
}
