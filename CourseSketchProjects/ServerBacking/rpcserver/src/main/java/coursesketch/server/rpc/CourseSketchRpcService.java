package coursesketch.server.rpc;

import com.google.protobuf.Service;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.interfaces.DatabaseReaderHolder;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Rpc Service that contains specific fields that all CourseSketch Rpc Services should have.
 *
 * Created by gigemjt on 9/3/15.
 */
public interface CourseSketchRpcService extends Service, DatabaseReaderHolder {

    /**
     * Performs some initialization.
     *
     * This is called before the server is started.
     * This is called by {@link #initialize()}.
     */
    void onInitialize();

    default void initialize() {
        onInitialize();
    }
}
