package database;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.submission.Submission;
import util.SubmissionMergerTest;

import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseClientTest {

    @Rule
    public FongoRule fongoRule = new FongoRule();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUpdateListSubmitsCorrectly() throws DatabaseAccessException {

        // once you have a DB instance, you can interact with it
        // just like you would with a real one.
        DB db = fongoRule.getDB();
        DatabaseClient client = getMockedVersion(db);

        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setUpdateList(SubmissionMergerTest.createSimpleDatabaseList(100));
        String id = DatabaseClient.saveExperiment(getFakeExperiment("User1", build.build()), 100, client);

        db.getCollection(EXPERIMENT_COLLECTION);
    }

    public Submission.SrlExperiment getFakeExperiment(String userId, Submission.SrlSubmission sub) {
        Submission.SrlExperiment.Builder build = Submission.SrlExperiment.newBuilder();
        build.setUserId(userId).setCourseId("Course1").setAssignmentId("Assignment1").setProblemId("Problem1")
                .setSubmission(sub);
        return build.build();
    }

    public DatabaseClient getMockedVersion(DB fakeDb) {
        DatabaseClient cl = mock(DatabaseClient.class);
        when(cl.getDb()).thenReturn(fakeDb);
        return cl;
    }
}
