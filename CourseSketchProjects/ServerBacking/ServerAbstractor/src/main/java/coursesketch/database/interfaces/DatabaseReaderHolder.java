package coursesketch.database.interfaces;

import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.LoggerFactory;

/**
 * An interface for classes that hold {@link AbstractCourseSketchDatabaseReader}.
 */
public interface DatabaseReaderHolder {

    /**
     * Creates an {@link AbstractCourseSketchDatabaseReader}.
     *
     * @param serverInfo Information about the server for creating the database.
     * @return A newly created database.
     */
    AbstractCourseSketchDatabaseReader createDatabaseReader(ServerInfo serverInfo);

    /**
     * Gets the default database and calls some basic methods on it.
     *
     * @param serverInfo Information about the server for creating the database.
     */
    default void initializeDatabase(ServerInfo serverInfo) {
        final AbstractCourseSketchDatabaseReader databaseReader = createDatabaseReader(serverInfo);
        if (databaseReader != null) {
            try {
                databaseReader.startDatabase();
            } catch (DatabaseAccessException e) {
                LoggerFactory.getLogger(DatabaseReaderHolder.class)
                        .error("An error was created starting the database for the server", e);
            }
        }
        setDatabaseReader(databaseReader);
        onInitializeDatabases();
    }

    /**
     * Called after the database has been initialized.
     */
    void onInitializeDatabases();

    /**
     * Sets the database reader.
     * @param databaseReader The {@link AbstractCourseSketchDatabaseReader}.
     */
    void setDatabaseReader(AbstractCourseSketchDatabaseReader databaseReader);
}
