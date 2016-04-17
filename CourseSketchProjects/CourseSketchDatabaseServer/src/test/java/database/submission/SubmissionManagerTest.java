package database.submission;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.CourseSketchMatcher;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.submission.SubmissionManagerInterface;
import database.DatabaseAccessException;
import database.institution.mongo.MongoInstitution;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.SELF_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
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
    @Mock
    IdentityManagerInterface identityManagerInterface;

    Submission.SrlExperiment experiment;


    public static final String COURSE_ID = new ObjectId().toHexString();
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
                .setSubmission(Submission.SrlSubmission.newBuilder().setId(SUBMISSION_ID))
                .build();

        try {
            // general rules
            AuthenticationHelper.setMockPermissions(authChecker, null, null, null, null, Authentication.AuthResponse.PermissionLevel.NO_PERMISSION);

            when(optionChecker.authenticateDate(any(AuthenticationDataCreator.class), anyLong()))
                    .thenReturn(false);

            when(optionChecker.isItemPublished(any(AuthenticationDataCreator.class)))
                    .thenReturn(false);

            when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                    .thenReturn(true);

        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

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
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);


        SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID, PROBLEM_ID, submissionManagerInterface);
    }

    @Test
    public void getSubmissionReturnsCorrectly() throws DatabaseAccessException, AuthenticationException {

        final String hashedUserId = MongoInstitution.hashUserId(USER_USER, COURSE_ID);

        SubmissionManager.mongoInsertSubmission(db, hashedUserId, PROBLEM_ID, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID,
                PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionThrowsDatabaseExceptionWhenNoExperimentsInList() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, null, null, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        List<Submission.SrlExperiment> experimentList = new ArrayList<>();
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID,
                PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = AuthenticationException.class)
    public void getSubmissionHandlesAuthExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(AuthenticationException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID,
                PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionHandlesDatabaseExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(DatabaseAccessException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, ADMIN_USER, ADMIN_USER, COURSE_ID,
                PROBLEM_ID, submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getExperimentsAsInstructorThrowsWhenDataDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        final List<Submission.SrlExperiment> experiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                USER_USER, PROBLEM_ID, submissionManagerInterface, null);
    }

    @Test(expected = AuthenticationException.class)
    public void getExperimentsAsInstructorThrowsWhenInvalidPermissionIsUsed() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);
        final List<Submission.SrlExperiment> experiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, PROBLEM_ID, submissionManagerInterface, null);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getExperimentsAsInstructorThrowsWhenNoExperimentsExist() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, PROBLEM_ID, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        final List<Submission.SrlExperiment> experiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, PROBLEM_ID, submissionManagerInterface, identityManagerInterface);
    }

    @Test
    public void getExperimentsAsInstructorReturnsExperimentsWhenEverythingExists() throws DatabaseAccessException, AuthenticationException {
        List<String> submissionIds = new ArrayList<>();
        List<Submission.SrlExperiment> experiments = new ArrayList<>();
        Map<String, String> userIdList = new HashMap<>();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<Submission.SrlExperiment> experimentList = Arrays.asList(experiment);

        for (int k = 0; k < 10; k++) {
            String userId = new ObjectId().toString();
            String submissionId = new ObjectId().toString();
            String userName = "" + Math.random();
            final String hashedUserId = MongoInstitution.hashUserId(userId, COURSE_ID);
            SubmissionManager.mongoInsertSubmission(db, hashedUserId, PROBLEM_ID, submissionId, true);
            submissionIds.add(submissionId);
            experiments.add(Submission.SrlExperiment.newBuilder(experiment)
                    .setUserId(userName)
                    .setSubmission(Submission.SrlSubmission.newBuilder(
                            experiment.getSubmission()).setId(submissionId)).build());
            userIdList.put(hashedUserId, userName);
        }

        when(identityManagerInterface.getItemRoster(anyString(), anyString(), any(Util.ItemType.class), anyList(), any(Authenticator.class)))
                .thenReturn(userIdList);

        when(submissionManagerInterface.getSubmission(anyString(), any(Authenticator.class), anyString(),
                Mockito.argThat(CourseSketchMatcher.iterableEqualAnyOrder(submissionIds))))
                        .thenReturn(experiments);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        final List<Submission.SrlExperiment> actualExperiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, PROBLEM_ID, submissionManagerInterface, identityManagerInterface);

        for (int k = 0; k < experimentList.size(); k++) {
            new ProtobufComparisonBuilder().build().equals(experiments.get(k), actualExperiments.get(k));
        }

        verify(submissionManagerInterface).getSubmission(eq(ADMIN_USER), any(Authenticator.class), eq(PROBLEM_ID),
                Mockito.argThat(CourseSketchMatcher.iterableEqualAnyOrder(submissionIds)));
    }
}
