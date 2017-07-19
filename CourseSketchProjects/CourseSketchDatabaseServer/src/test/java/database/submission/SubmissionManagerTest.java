package database.submission;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.CourseSketchMatcher;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.submission.SubmissionManagerInterface;
import database.DatabaseAccessException;
import database.institution.mongo.MongoInstitution;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.commands.Commands;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;
import protobuf.srl.tutorial.TutorialOuterClass;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.TUTORIAL_COLLECTION;
import static database.DatabaseStringConstants.UPDATELIST;
import static database.DatabaseStringConstants.URL;
import static database.DatabaseStringConstants.URL_HASH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmissionManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    public MongoDatabase db;

    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;
    private Authenticator authenticator;

    @Mock
    private SubmissionManagerInterface submissionManagerInterface;
    @Mock
    private IdentityManagerInterface identityManagerInterface;

    private Submission.SrlExperiment experiment;
    private List<String> idList;

    TutorialOuterClass.Tutorial.Builder tutorialObject;
    Commands.SrlUpdateList.Builder steps;

    private static final String id = "randomWhateverWeDontCare";
    private static final String name = "TestTutorial";
    private static final String url = "http://www.reddit.com";
    private static final String description = "This is a test lol";
    private static final String COURSE_ID = new ObjectId().toHexString();
    private static final String SUBMISSION_ID = new ObjectId().toHexString();
    private static final String SUBMISSION_ID2 = new ObjectId().toHexString();
    private static final String PROBLEM_ID = new ObjectId().toHexString();
    private static final String PART_ID = "5";
    private static final String ADMIN_USER = new ObjectId().toHexString();
    private static final String USER_USER = new ObjectId().toHexString();
    private Document fakeDocument;

    @Before
    public void setUp() throws Exception {
        db = fongo.getDatabase();
        experiment = Submission.SrlExperiment.newBuilder()
                .setProblemId(PROBLEM_ID)
                .setAssignmentId(new ObjectId().toString())
                .setCourseId(new ObjectId().toString())
                .setUserId(USER_USER)
                .setSubmission(Submission.SrlSubmission.newBuilder().setId(SUBMISSION_ID))
                .build();

        // general rules
        AuthenticationHelper.setMockPermissions(authChecker, null, null, null, null, Authentication.AuthResponse.PermissionLevel.NO_PERMISSION);

        when(optionChecker.authenticateDate(any(AuthenticationDataCreator.class), anyLong()))
                .thenReturn(false);

        when(optionChecker.isItemPublished(any(AuthenticationDataCreator.class)))
                .thenReturn(false);

        when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                .thenReturn(true);

        authenticator = new Authenticator(authChecker, optionChecker);

        idList = Lists.newArrayList(PROBLEM_ID, PART_ID);

        tutorialObject = TutorialOuterClass.Tutorial.newBuilder();
        tutorialObject.setId(id);
        tutorialObject.setName(name);
        tutorialObject.setUrl(url);
        tutorialObject.setDescription(description);
        steps = Commands.SrlUpdateList.newBuilder();
        tutorialObject.setSteps(steps.build());

        fakeDocument = new Document(DESCRIPTION, tutorialObject.getDescription()).append(NAME, tutorialObject.getName())
                .append(URL, tutorialObject.getUrl()).append(URL_HASH, tutorialObject.getUrl().hashCode())
                .append(UPDATELIST, tutorialObject.getSteps().toByteArray());
    }

    @Test
    public void insertingSubmissionCreatesCorrectDataInDatabase() {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);

        Document result = db.getCollection(EXPERIMENT_COLLECTION).find().first();
        Assert.assertEquals(PROBLEM_ID, result.get(SELF_ID).toString());
        Assert.assertEquals(SUBMISSION_ID, ((Document) result.get(PART_ID)).get(USER_USER).toString());
    }

    @Test
    public void insertingSubmissionCreatesCorrectDataInDatabaseAndUpdates() {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);
        SubmissionManager.mongoInsertSubmission(db, ADMIN_USER, idList, SUBMISSION_ID2, true);

        Document result = db.getCollection(EXPERIMENT_COLLECTION).find().first();
        Assert.assertEquals(PROBLEM_ID, result.get(SELF_ID).toString());
        Assert.assertEquals(SUBMISSION_ID, ((Document) result.get(PART_ID)).get(USER_USER));
        Assert.assertEquals(SUBMISSION_ID2, ((Document) result.get(PART_ID)).get(ADMIN_USER));
    }

    @Test
    public void insertingSubmissionCreatesCorrectDataInDatabaseAndUpdates2Parts() {
        String partId2 = PART_ID + "5";
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);
        SubmissionManager.mongoInsertSubmission(db, ADMIN_USER, Lists.newArrayList(PROBLEM_ID, partId2),
                SUBMISSION_ID2, true);

        Document result = db.getCollection(EXPERIMENT_COLLECTION).find().first();
        Assert.assertEquals(PROBLEM_ID, result.get(SELF_ID).toString());
        Assert.assertEquals(SUBMISSION_ID, ((Document) result.get(PART_ID)).get(USER_USER));
        Assert.assertEquals(SUBMISSION_ID2, ((Document) result.get(partId2)).get(ADMIN_USER));
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionThrowsWhenDatabaseIsEmpty() throws DatabaseAccessException, AuthenticationException {
        // when(submissionManagerInterface.getSubmission(USER_USER, any(Authenticator.class), SUBMISSION_ID))
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);


        SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID, idList, submissionManagerInterface);
    }

    @Test
    public void getSubmissionReturnsCorrectly() throws DatabaseAccessException, AuthenticationException {

        final String hashedUserId = MongoInstitution.hashUserId(USER_USER, COURSE_ID);

        SubmissionManager.mongoInsertSubmission(db, hashedUserId, idList, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        List<Submission.SrlExperiment> experimentList = Collections.singletonList(experiment);
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID, idList
                , submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionThrowsDatabaseExceptionWhenNoExperimentsInList() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, null, null, USER_USER, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        List<Submission.SrlExperiment> experimentList = new ArrayList<>();
        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenReturn(experimentList);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID, idList
                , submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = AuthenticationException.class)
    public void getSubmissionHandlesAuthExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);

        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(AuthenticationException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, USER_USER, USER_USER, COURSE_ID, idList
                , submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getSubmissionHandlesDatabaseExceptionCorrectly() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        when(submissionManagerInterface.getSubmission(eq(USER_USER), any(Authenticator.class), eq(PROBLEM_ID),
                eq(SUBMISSION_ID))).thenThrow(DatabaseAccessException.class);

        Submission.SrlExperiment actualExperiment = SubmissionManager.mongoGetExperiment(authenticator, db, ADMIN_USER, ADMIN_USER, COURSE_ID, idList
                , submissionManagerInterface);
        new ProtobufComparisonBuilder().build().equals(experiment, actualExperiment);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getExperimentsAsInstructorThrowsWhenDataDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                USER_USER, idList, submissionManagerInterface, null);
    }

    @Test(expected = AuthenticationException.class)
    public void getExperimentsAsInstructorThrowsWhenInvalidPermissionIsUsed() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);
        SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, idList, submissionManagerInterface, null);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getExperimentsAsInstructorThrowsWhenNoExperimentsExist() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoInsertSubmission(db, USER_USER, idList, SUBMISSION_ID, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, idList, submissionManagerInterface, identityManagerInterface);
    }

    @Test
    public void getExperimentsAsInstructorReturnsExperimentsWhenEverythingExists() throws DatabaseAccessException, AuthenticationException {
        List<String> submissionIds = new ArrayList<>();
        List<Submission.SrlExperiment> experiments = new ArrayList<>();
        Map<String, String> userIdList = new HashMap<>();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<Submission.SrlExperiment> experimentList = Collections.singletonList(experiment);

        for (int k = 0; k < 10; k++) {
            String userId = new ObjectId().toString();
            String submissionId = new ObjectId().toString();
            String userName = "" + Math.random();
            final String hashedUserId = MongoInstitution.hashUserId(userId, COURSE_ID);
            SubmissionManager.mongoInsertSubmission(db, hashedUserId, idList, submissionId, true);
            submissionIds.add(submissionId);
            experiments.add(Submission.SrlExperiment.newBuilder(experiment)
                    .setUserId(userName)
                    .setSubmission(Submission.SrlSubmission.newBuilder(
                            experiment.getSubmission()).setId(submissionId)).build());
            userIdList.put(hashedUserId, userName);
        }

        when(identityManagerInterface.getItemRoster(anyString(), anyString(), any(Util.ItemType.class), anyList(), any
                (Authenticator.class)))
                .thenReturn(userIdList);

        when(submissionManagerInterface.getSubmission(anyString(), any(Authenticator.class), anyString(),
                Mockito.argThat(CourseSketchMatcher.iterableEqualAnyOrder(submissionIds))))
                .thenReturn(experiments);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM,
                PROBLEM_ID, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        final List<Submission.SrlExperiment> actualExperiments = SubmissionManager.mongoGetAllExperimentsAsInstructor(authenticator, db,
                ADMIN_USER, idList, submissionManagerInterface, identityManagerInterface);

        for (int k = 0; k < experimentList.size(); k++) {
            new ProtobufComparisonBuilder().build().equals(experiments.get(k), actualExperiments.get(k));
        }

        verify(submissionManagerInterface).getSubmission(eq(ADMIN_USER), any(Authenticator.class), eq(PROBLEM_ID),
                Mockito.argThat(CourseSketchMatcher.iterableEqualAnyOrder(submissionIds)));
    }

    @Test
    public void testTutorialInsertCorrectly() throws Exception {
        String tutorialObjectId = SubmissionManager.mongoInsertTutorial(authenticator, db, "userId", tutorialObject.build());

        fakeDocument.append(SELF_ID, new ObjectId(tutorialObjectId));
        Document first = db.getCollection(TUTORIAL_COLLECTION).find(new Document(SELF_ID, new ObjectId(tutorialObjectId))).first();

        Assert.assertEquals(fakeDocument, first);
    }

    @Test
    public void testTutorialGetCorrectly() throws Exception {
        String tutorialObjectId = SubmissionManager.mongoInsertTutorial(authenticator, db, "userId", tutorialObject.build());

        TutorialOuterClass.Tutorial tutorial = SubmissionManager.mongoGetTutorial(authenticator, db, "userId", tutorialObjectId);
        tutorialObject.setId(tutorialObjectId);
        Assert.assertEquals(tutorialObject.build(), tutorial);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testTutorialThrowsExceptionWhenItDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoGetTutorial(authenticator, db, "userId", new ObjectId().toString());
    }

    @Test
    public void testTutorialListGetCorrectly() throws Exception {
        String tutorialObjectId1 = SubmissionManager.mongoInsertTutorial(authenticator, db, "userId", tutorialObject.build());
        tutorialObject.setName("Tutorial 1");
        String tutorialObjectId2 = SubmissionManager.mongoInsertTutorial(authenticator, db, "userId", tutorialObject.build());

        List<TutorialOuterClass.Tutorial> tutorialList = SubmissionManager.mongoGetTutorialList(authenticator, db, "userId", url, 0);
        Assert.assertEquals(2, tutorialList.size());
    }
}
