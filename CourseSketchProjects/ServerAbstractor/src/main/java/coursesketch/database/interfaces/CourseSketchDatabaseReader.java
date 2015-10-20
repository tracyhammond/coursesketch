package coursesketch.database.interfaces;

import coursesketch.server.interfaces.ServerInfo;

/**
 * An abstract class used when creating software that reads from the database.
 *
 * Created by dtracers on 10/19/2015.
 */
public abstract class CourseSketchDatabaseReader {

    /**
     * Contains Information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * Is set to true when the database has started.
     */
    private boolean databaseStarted = false;

    /**
     * Takes in a list of addressess where the database can be found and a name of the database.
     * @param serverInfo Information about the server.
     */
    public CourseSketchDatabaseReader(final ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Called to start the database.
     *
     * This does use double check locking on this object when initialing the database.
     */
    public final void startDatabase() {
        if (!databaseStarted) {
            synchronized (this) {
                if (!databaseStarted) {
                    onStartDatabase();
                }
            }
        }
    }

    /**
     * Called when startDatabase is called if the database has not already been started.
     *
     * This method should be synchronous.
     */
    protected abstract void onStartDatabase();

    /**
     * Called when the database has started.
     */
    protected final void databaseStarted() {
        databaseStarted = true;
    }

    /**
     * @return {@link ServerInfo} server information.
     */
    protected final ServerInfo getServerInfo() {
        return serverInfo;
    }
}
