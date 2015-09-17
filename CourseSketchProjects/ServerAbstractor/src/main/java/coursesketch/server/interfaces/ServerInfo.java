package coursesketch.server.interfaces;

import com.mongodb.ServerAddress;

import java.util.List;

/**
 * Contains information about the sever.
 *
 * This class is used to wrap up all of the information about the server that the program may need to know.
 * It is created and set in the AbstractGeneralConnectionRunner where it is then passed around by all of the other classes.
 * Created by gigemjt on 9/3/15.
 */
public class ServerInfo {

    /**
     * The address that the server is running on.
     *
     * If it is running on localhost then it should say localhost.
     * It may be the ip address that the server is running on.
     **/
    private final String hostName;

    /**
     * The port that the server is running on.
     *
     * This is the port that a client would specify to connect to this server.
     */
    private final int port;

    /**
     * The time in milliseconds before the connection times out.
     */
    private final long timeOut;

    /**
     * <code>true</code> if the servlet should be secure, <code>false</code> otherwise.
     **/
    private final boolean isSecureVar;

    /**
     * <code>true</code> if the server is running locally, <code>false</code> otherwise.
     **/
    private final boolean isLocalVar;
    private List<ServerAddress> databaseUrl;
    private String databaseName;

    /**
     * Creates a server info with all of the information.
     *
     * @param hostName {@link #hostName}.
     * @param port {@link #port}.
     * @param timeOut {@link #timeOut}.
     * @param isSecure {@link #isSecureVar}.
     * @param isLocal {@link #isLocalVar}.
     */
    public ServerInfo(final String hostName, final int port, final long timeOut, final boolean isSecure, final boolean isLocal) {
        this.hostName = hostName;
        this.port = port;
        this.timeOut = timeOut;
        this.isSecureVar = isSecure;
        this.isLocalVar = isLocal;
    }

    /**
     * @return The host name that the server runs on.
     * @see #hostName
     */
    public final String getHostName() {
        return hostName;
    }

    /**
     * @return The port that the server is running on
     * @see #port
     */
    public final int getPort() {
        return port;
    }

    /**
     * @return The time before a connection times out.
     * @see #timeOut
     */
    public final long getTimeOut() {
        return timeOut;
    }

    /**
     * @return True if the server should be running in secure mode
     * @see #isSecureVar
     */
    public final boolean isSecure() {
        return isSecureVar;
    }

    /**
     * @return True if the server is running on a local dev machine
     * @see #isLocalVar
     */
    public final boolean isLocal() {
        return isLocalVar;
    }

    public List<ServerAddress> getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(final List<ServerAddress> databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }
}
