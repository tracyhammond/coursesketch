package database.submission;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import coursesketch.database.submission.SubmissionManagerInterface;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.submission.Submission;

import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.SELF_ID;

/**
 * Created by dtracers on 12/19/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmissionManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    public DB db;

    @Mock
    SubmissionManagerInterface submissionManagerInterface;

    Submission.SrlExperiment experiment;

    public static final String SUBMISSION_ID = new ObjectId().toHexString();
    public static final String SUBMISSION_ID2 = new ObjectId().toHexString();
    public static final String PROBLEM_ID = new ObjectId().toHexString();
    public static final String ADMIN_USER = new ObjectId().toHexString();
    public static final String USER_USER = new ObjectId().toHexString();

    @Before
    public void setUp() {
        db = fongo.getDB();
        experiment = Submission.SrlExperiment.newBuilder()
                .setProblemId(PROBLEM_ID)
                .setAssignmentId(new ObjectId().toString())
                .setCourseId(new ObjectId().toString())
                .setUserId(USER_USER)
                .setSubmission(Submission.SrlSubmission.getDefaultInstance())
                .build();
    }

    @Test
    public void insertingSubmissionCreatesCorrectDataInDatabase() {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        DBObject result = db.getCollection(EXPERIMENT_COLLECTION).find().next();
        Assert.assertEquals(PROBLEM_ID, result.get(SELF_ID).toString());
        Assert.assertEquals(SUBMISSION_ID, result.get(USER_USER));
    }

    @Test
    public void insertingSubmissionCreatesCorrectDataInDatabaseAndUpdates() {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);
        SubmissionManager.mongoInsertSubmission(db, ADMIN_USER, PROBLEM_ID, SUBMISSION_ID2, true);

        DBObject result = db.getCollection(EXPERIMENT_COLLECTION).find().next();
        Assert.assertEquals(PROBLEM_ID, result.get(SELF_ID).toString());
        Assert.assertEquals(SUBMISSION_ID, result.get(USER_USER));
        Assert.assertEquals(SUBMISSION_ID2, result.get(ADMIN_USER));
    }
}
