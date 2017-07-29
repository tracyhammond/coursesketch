package coursesketch.server.rpc;

import com.google.protobuf.Service;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Rpc Service that contains specific fields that all CourseSketch Rpc Services should have.
 *
 * Created by gigemjt on 9/3/15.
 */
public interface CourseSketchRpcService extends Service {
    /**
     * Declaration/Definition of Logger.
     */
    Logger LOG = LoggerFactory.getLogger(GeneralConnectionRunner.class);

    AbstractCourseSketchDatabaseReader createDatabaseReader(ServerInfo serverInfo);

    void setDatabaseReader(AbstractCourseSketchDatabaseReader databaseReader);

    default void initialize(ServerInfo serverInfo) {
        final AbstractCourseSketchDatabaseReader databaseReader = createDatabaseReader(serverInfo);
        if (databaseReader != null) {
            try {
                databaseReader.startDatabase();
            } catch (DatabaseAccessException e) {
                LOG.error("An error was created starting the database for the server", e);
            }
        }
        setDatabaseReader(databaseReader);
        onInitialize();
    }

    /**
     * Performs some initialization.
     *
     * This is called before the server is started.
     * This is called by {@link #initialize(ServerInfo)}.
     */
    void onInitialize();

}
