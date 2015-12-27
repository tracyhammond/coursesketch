package database.submission;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.submission.Submission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.SELF_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 12/19/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmissionManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    public DB db;

    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;
    public Authenticator authenticator;

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
        authenticator = new Authenticator(authChecker, optionChecker);
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

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionThrowsWhenDatabaseIsEmpty() throws DatabaseAccessException, AuthenticationException {
        // when(submissionManagerInterface.getSubmission(USER_USER, any(Authenticator.class), SUBMISSION_ID))

        SubmissionManager.mongoGetExperiment(db, USER_USER, PROBLEM_ID, submissionManagerInterface);
    }

    @Test
    public void getSubmissionReturnsCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID), eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(db, USER_USER, PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionThrowsDatabaseExceptionWhenNoExperimentsInList() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        List<Submission.SrlExperiment> experimentList = new ArrayList<>();
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID), eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(db, USER_USER, PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = AuthenticationException.class)
    public void getSubmissionHandlesAuthExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(AuthenticationException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(db, USER_USER, PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionHandlesDatabaseExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(DatabaseAccessException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(db, ADMIN_USER, PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }


    @Test(expected = DatabaseAccessException.class)
    public void getExperimentsAsInstructorThrowsWhenDataDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        final List<Submission.SrlExperiment> experiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                USER_USER, PROBLEM_ID, submissionManagerInterface);
    }

    @Test
    public void getExperimentsAsInstructorThrows() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);
        final List<Submission.SrlExperiment> experiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, PROBLEM_ID, submissionManagerInterface);
    }
}
