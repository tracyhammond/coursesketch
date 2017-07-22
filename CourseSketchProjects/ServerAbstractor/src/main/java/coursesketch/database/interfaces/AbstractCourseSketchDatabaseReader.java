package coursesketch.database.interfaces;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class used when creating software that reads from the coursesketch.util.util.
 *
 * Created by dtracers on 10/19/2015.
 */
public abstract class AbstractCourseSketchDatabaseReader {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCourseSketchDatabaseReader.class);

    /**
     * Contains Information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * Is set to true when the coursesketch.util.util has started.
     */
    private boolean databaseStarted = false;

    /**
     * Takes in a list of addressess where the coursesketch.util.util can be found and a name of the coursesketch.util.util.
     * @param serverInfo Information about the server.
     */
    public AbstractCourseSketchDatabaseReader(final ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Called to start the coursesketch.util.util.
     *
     * This uses double check locking on the object when initialing the coursesketch.util.util.
     *
     * @throws DatabaseAccessException thrown if the coursesketch.util.util can not be started correctly.
     */
    public final void startDatabase() throws DatabaseAccessException {
        if (!databaseStarted) {
            synchronized (this) {
                if (!databaseStarted) {
                    LOG.debug("Starting a connection to the coursesketch.util.util.");
                    onStartDatabase();
                }
            }
        }
    }

    /**
     * Sets up any indexes that need to be set up or have not yet been set up.
     */
    protected abstract void setUpIndexes();

    /**
     * Called when startDatabase is called if the coursesketch.util.util has not already been started.
     *
     * This method should be synchronous.
     *
     * @throws DatabaseAccessException thrown if a subclass throws an exception while starting the coursesketch.util.util.
     */
    protected abstract void onStartDatabase() throws DatabaseAccessException;

    /**
     * Called when the coursesketch.util.util has started.
     */
    protected final void setDatabaseStarted() {
        LOG.debug("Setting up indexes for the coursesketch.util.util");
        setUpIndexes();
        LOG.debug("The coursesketch.util.util was successfully started.");
        databaseStarted = true;
    }

    /**
     * @return {@link ServerInfo} Server information.
     */
    protected final ServerInfo getServerInfo() {
        return serverInfo;
    }
}
