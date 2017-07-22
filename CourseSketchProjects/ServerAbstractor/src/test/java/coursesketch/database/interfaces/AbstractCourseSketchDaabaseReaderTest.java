package coursesketch.database.interfaces;

import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

public class AbstractCourseSketchDaabaseReaderTest {

    @Mock
    ServerInfo serverInfo;

    @Test
    public void databaseStarts() throws DatabaseAccessException {

        final boolean[] startCalled = new boolean[1];
        final boolean[] indexesSetup = new boolean[1];

        AbstractCourseSketchDatabaseReader read = new AbstractCourseSketchDatabaseReader(serverInfo) {

            @Override
            protected void setUpIndexes() {
                indexesSetup[0] = true;
            }

            @Override
            protected void onStartDatabase() throws DatabaseAccessException {
                startCalled[0] = true;
            }
        };
        read.startDatabase();
        read.setUpIndexes();

        Assert.assertTrue(startCalled[0]);
        Assert.assertTrue(indexesSetup[0]);
    }
}
