package coursesketch.database.interfaces;

import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.LoggerFactory;

public interface DatabaseReaderHolder {

    AbstractCourseSketchDatabaseReader createDatabaseReader(ServerInfo serverInfo);

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

    void onInitializeDatabases();

    void setDatabaseReader(AbstractCourseSketchDatabaseReader databaseReader);
}
