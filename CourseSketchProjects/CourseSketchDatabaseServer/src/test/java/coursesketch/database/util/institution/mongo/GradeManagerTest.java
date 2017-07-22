package coursesketch.database.util.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.DocumentComparisonOptions;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import coursesketch.server.authentication.HashManager;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.RequestConverter;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;
import protobuf.srl.school.School.SrlCourse;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.COMMENT;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_ID;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.CURRENT_GRADE;
import static coursesketch.database.util.DatabaseStringConstants.EXTERNAL_GRADE;
import static coursesketch.database.util.DatabaseStringConstants.GRADED_DATE;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_COLLECTION;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_HISTORY;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_VALUE;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.USER_ID;
import static coursesketch.database.util.DatabaseStringConstants.WHO_CHANGED;
import static coursesketch.database.util.institution.mongo.MongoInstitutionTest.genericDatabaseMock;

/**
 * Tests for GradeManager.
 * @see GradeManager
 *
 * Created by matt on 4/11/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class GradeManagerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock
    AuthenticationChecker authChecker;
    @Mock
    AuthenticationOptionChecker optionChecker;

    public MongoDatabase db;
    private Authenticator authenticator;
    private SrlCourse.Builder courseBuilder = SrlCourse.newBuilder();

    private ProtoGrade.Builder protoGradeGrabber = ProtoGrade.newBuilder();
    private ProtoGrade.Builder fakeProtoGrade = ProtoGrade.newBuilder();
    private GradeHistory.Builder fakeProtoHistory1 = GradeHistory.newBuilder();
    private GradeHistory.Builder fakeProtoHistory2 = GradeHistory.newBuilder();

    private Document fakeMongoGrade = new Document();
    private List<Document> fakeMongoHistory = new ArrayList<>();

    private static final String FAKE_COURSE_ID = "courseId";
    private static final String FAKE_USER_ID = "userId";
    private static final String FAKE_ADMIN_ID = "adminId";
    private static final String FAKE_ASGN_ID = "assignmentId";
    private static final String FAKE_PROB_ID = "problemId";
    private static final float FAKE_CURRENT_GRADE = 95;
    private static final float FAKE_OLD_GRADE = 75;
    private static final float FAKE_NEW_GRADE = 95;
    private static final String FAKE_COMMENT = "hi";
    private static final boolean FAKE_EXTERNAL_GRADE = false;
    private static final long FAKE_OLD_MILLISECONDS = 12345;
    private static final long FAKE_NEW_MILLISECONDS = 223456;

    private DocumentComparisonOptions documentComparisonOptions = new DocumentComparisonOptions();

    @Before
    public void before() throws NoSuchAlgorithmException, AuthenticationException {
        db = fongo.getDatabase();

        documentComparisonOptions.ignoreFloatDoubleComparisons = true;

        genericDatabaseMock(authChecker, optionChecker);
        authenticator = new Authenticator(authChecker, optionChecker);

        /**
         * Setting up fake Proto data.
         */
        fakeProtoHistory1.setGradeValue(FAKE_OLD_GRADE).setComment(FAKE_COMMENT + "1").setWhoChanged(FAKE_ADMIN_ID)
                .setGradedDate(RequestConverter.getProtoFromMilliseconds(FAKE_OLD_MILLISECONDS));
        fakeProtoHistory2.setGradeValue(FAKE_NEW_GRADE).setComment(FAKE_COMMENT + "2").setWhoChanged(FAKE_ADMIN_ID)
                .setGradedDate(RequestConverter.getProtoFromMilliseconds(FAKE_NEW_MILLISECONDS));

        final String userIdHash = HashManager.createHash(FAKE_USER_ID, HashManager.generateUnSecureSalt(FAKE_COURSE_ID));
        fakeProtoGrade.setCourseId(FAKE_COURSE_ID).setUserId(userIdHash).setAssignmentId(FAKE_ASGN_ID)
                .setProblemId(FAKE_PROB_ID).setCurrentGrade(FAKE_CURRENT_GRADE).setExternalGrade(FAKE_EXTERNAL_GRADE);
        fakeProtoGrade.addGradeHistory(fakeProtoHistory1.build());
        fakeProtoGrade.addGradeHistory(fakeProtoHistory2.build());

        protoGradeGrabber.setCourseId(FAKE_COURSE_ID).setUserId(userIdHash).setAssignmentId(FAKE_ASGN_ID)
                .setProblemId(FAKE_PROB_ID);

        /**
         * Setting up fake Mongo data.
         */
        Document mongoHistory1 = new Document(GRADE_VALUE, FAKE_OLD_GRADE)
                .append(COMMENT, FAKE_COMMENT + "1").append(WHO_CHANGED, FAKE_ADMIN_ID)
                .append(GRADED_DATE, FAKE_OLD_MILLISECONDS);
        Document mongoHistory2 = new Document(GRADE_VALUE, FAKE_NEW_GRADE)
                .append(COMMENT, FAKE_COMMENT + "2").append(WHO_CHANGED, FAKE_ADMIN_ID)
                .append(GRADED_DATE, FAKE_NEW_MILLISECONDS);
        fakeMongoHistory.add(mongoHistory1);
        fakeMongoHistory.add(mongoHistory2);

        fakeMongoGrade.append(COURSE_ID, FAKE_COURSE_ID).append(USER_ID, HashManager.toHex(userIdHash.getBytes()))
                .append(ASSIGNMENT_ID, FAKE_ASGN_ID)
                .append(COURSE_PROBLEM_ID, FAKE_PROB_ID).append(CURRENT_GRADE, FAKE_CURRENT_GRADE)
                .append(GRADE_HISTORY, fakeMongoHistory).append(EXTERNAL_GRADE, FAKE_EXTERNAL_GRADE);

        courseBuilder.setId(FAKE_COURSE_ID);
    }

    @Test
    public void buildMongoGradeHistoryTest() {
        Document mongoHistory = GradeManager.buildMongoGradeHistory(fakeProtoHistory2.build());
        DatabaseHelper.assertDocumentEquals(fakeMongoHistory.get(1), mongoHistory, documentComparisonOptions);
    }

    @Test
    public void addGradeTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        Document testGrade = db.getCollection(GRADE_COLLECTION).find(new Document(COURSE_ID, courseId))
                .projection(Projections.exclude(SELF_ID)).first();
        fakeMongoGrade.put(COURSE_ID, courseId);
        DatabaseHelper.assertDocumentEquals(fakeMongoGrade, testGrade, documentComparisonOptions);
    }

    @Test
    public void addCourseGradeTest() throws Exception {
        // This will test a grade with no assignmentId or problemId
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearAssignmentId();
        fakeProtoGrade.clearProblemId();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        Document testGrade = db.getCollection(GRADE_COLLECTION).find(new Document(COURSE_ID, courseId))
                .projection(Projections.exclude(SELF_ID)).first();
        fakeMongoGrade.put(COURSE_ID, courseId);
        fakeMongoGrade.remove(ASSIGNMENT_ID);
        fakeMongoGrade.remove(COURSE_PROBLEM_ID);
        DatabaseHelper.assertDocumentEquals(fakeMongoGrade, testGrade, documentComparisonOptions);
    }

    @Test(expected = AuthenticationException.class)
    public void userAddingGradeHasNoPermission() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);

        GradeManager.addGrade(authenticator, db, FAKE_USER_ID, fakeProtoGrade.build());
    }

    @Test // FUTURE: make this test pass (expected = DatabaseAccessException.class)
    public void userNotInCourseExternalGradeTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.setExternalGrade(true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
    }

    @Test // FUTURE: make this test pass (expected = DatabaseAccessException.class)
    public void userNotAssignedProblemTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
    }

    @Test // FUTURE: make this test pass (expected = DatabaseAccessException.class)
    public void userNotAssignedAssignmentTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearProblemId();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, FAKE_ASGN_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
    }

    @Test // FUTURE: make this test pass (expected = DatabaseAccessException.class)
    public void userNotInCourseTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearProblemId();
        fakeProtoGrade.clearAssignmentId();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void gradeHasNoHistoryTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearGradeHistory();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
    }

    @Test
    public void noCurrentGradeTest1() throws Exception {
        // Tests the gradeHistory applies the correct currentGrade if it does not exist and gradeHistory is in chronological order (old to new)
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearCurrentGrade();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        Document testGrade = db.getCollection(GRADE_COLLECTION).find(new Document(COURSE_ID, courseId))
                .projection(Projections.exclude(SELF_ID)).first();
        fakeMongoGrade.put(COURSE_ID, courseId);
        DatabaseHelper.assertDocumentEquals(fakeMongoGrade, testGrade, documentComparisonOptions);
    }

    @Test
    public void noCurrentGradeTest2() throws Exception {
        // Tests the gradeHistory applies the correct currentGrade if it does not exist and gradeHistory is in chronological order (new to old)
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.clearCurrentGrade();
        fakeProtoGrade.clearGradeHistory();
        fakeProtoGrade.addGradeHistory(fakeProtoHistory2);
        fakeProtoGrade.addGradeHistory(fakeProtoHistory1);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        Document testGrade = db.getCollection(GRADE_COLLECTION).find(new Document(COURSE_ID, courseId))
                .projection(Projections.exclude(SELF_ID)).first();
        fakeMongoGrade.put(COURSE_ID, courseId);

        // The assert fails if the list contents are in reverse order, so reversing to make the assert not fail due to that.
        List<Document> reverseMongoHistory = new ArrayList<>();
        reverseMongoHistory.add(fakeMongoHistory.get(1));
        reverseMongoHistory.add(fakeMongoHistory.get(0));
        fakeMongoGrade.put(GRADE_HISTORY, reverseMongoHistory);
        DatabaseHelper.assertDocumentEquals(fakeMongoGrade, testGrade, documentComparisonOptions);
    }

    @Test
    public void buildProtoGradeHistoryTest() throws Exception {
        GradeHistory testHistory = GradeManager.buildProtoGradeHistory(fakeMongoHistory.get(0));
        Assert.assertEquals(fakeProtoHistory1.build(), testHistory);
    }

    @Test(expected = DatabaseAccessException.class)
    public void buildProtoGradeHistoryNoValuesTest() throws Exception {
        GradeManager.buildProtoGradeHistory(new Document());
    }

    @Test
    public void buildProtoGradeTest() throws Exception {
        ProtoGrade testGrade = GradeManager.buildProtoGrade(fakeMongoGrade);
        Assert.assertEquals(fakeProtoGrade.build(), testGrade);
    }

    @Test(expected = DatabaseAccessException.class)
    public void buildProtoGradeNoCourseIdTest() throws Exception {
        fakeMongoGrade.remove(COURSE_ID);
        GradeManager.buildProtoGrade(fakeMongoGrade);
    }

    @Test(expected = DatabaseAccessException.class)
    public void buildProtoGradeNoUserIdTest() throws Exception {
        fakeMongoGrade.remove(USER_ID);
        GradeManager.buildProtoGrade(fakeMongoGrade);
    }

    @Test
    public void getProbGradeTest() throws Exception {

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());

        // Tests that both the user of the grade and a course admin can get a valid problem grade.
        ProtoGrade testUserGrade = GradeManager.getGrade(authenticator, db, FAKE_USER_ID,
                FAKE_USER_ID, protoGradeGrabber.build());

        // admin getting the grade!
        ProtoGrade testAdminGrade = GradeManager.getGrade(authenticator, db, FAKE_ADMIN_ID, FAKE_ADMIN_ID,
                protoGradeGrabber.build());

        Assert.assertEquals(fakeProtoGrade.build(), testUserGrade);
        Assert.assertEquals(fakeProtoGrade.build(), testAdminGrade);
    }

    @Test(expected = AuthenticationException.class)
    public void getProbGradeTestOtherUserShouldThrowException() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        protoGradeGrabber.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());

        // Tests that both the user of the grade and a course admin can get a valid problem grade.
        ProtoGrade testUserGrade = GradeManager.getGrade(authenticator, db, FAKE_USER_ID,
                FAKE_USER_ID + "Different", protoGradeGrabber.build());

        Assert.assertEquals(fakeProtoGrade.build(), testUserGrade);
    }

    @Test
    public void getCourseGradeTest() throws Exception {
        fakeProtoGrade.clearAssignmentId();
        fakeProtoGrade.clearProblemId();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, FAKE_COURSE_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, FAKE_COURSE_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);


        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        protoGradeGrabber.clearAssignmentId().clearProblemId();

        // Tests that both the user of the grade and a course admin can get a valid course grade.
        ProtoGrade testUserGrade = GradeManager.getGrade(authenticator, db, FAKE_USER_ID, FAKE_USER_ID,
                protoGradeGrabber.build());
        ProtoGrade testAdminGrade = GradeManager.getGrade(authenticator, db, FAKE_ADMIN_ID, FAKE_ADMIN_ID,
                protoGradeGrabber.build());
        Assert.assertEquals(fakeProtoGrade.build(), testUserGrade);
        Assert.assertEquals(fakeProtoGrade.build(), testAdminGrade);
    }

    @Test
    public void nonAdminRequestingDifferentUserGradeTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoGrade.setCourseId(courseId);
        protoGradeGrabber.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());

        exception.expect(AuthenticationException.class);

        // User should be able to get their grade still. FAKE_ADMIN cannot because they are not on the adminList.
        ProtoGrade testUserGrade = GradeManager.getGrade(authenticator, db, FAKE_USER_ID, FAKE_USER_ID,
                protoGradeGrabber.build());

        Assert.assertEquals(fakeProtoGrade.build(), testUserGrade);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.PEER_TEACHER);

        GradeManager.getGrade(authenticator, db, FAKE_ADMIN_ID, FAKE_ADMIN_ID,
                protoGradeGrabber.build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void gradeNotInDatabaseTest() throws Exception {
        // Not adding grade here like previous tests so we can get the exception!

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, FAKE_PROB_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradeManager.getGrade(authenticator, db, FAKE_USER_ID, FAKE_USER_ID,
                protoGradeGrabber.build());
    }

    @Test
    public void getAllAssignmentGradesInstructorTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, FAKE_ASGN_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> expectedGrades = new ArrayList<>();
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.setUserId(FAKE_USER_ID + "1");
        fakeProtoGrade.clearProblemId();
        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        fakeProtoGrade.setUserId(FAKE_USER_ID + "2");
        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> testGrades = GradeManager.getAllAssignmentGradesInstructor(authenticator, db, courseId, FAKE_ADMIN_ID);
        Assert.assertEquals(expectedGrades, testGrades);
    }

    @Test
    public void getAllAssignmentGradesInstructorThrowsWhenNoCourseAuthEvenThoughTheyAddedAssignment() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, FAKE_ASGN_ID,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> expectedGrades = new ArrayList<>();
        fakeProtoGrade.setCourseId(courseId);
        fakeProtoGrade.setUserId(FAKE_USER_ID + "1");
        fakeProtoGrade.clearProblemId();
        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        fakeProtoGrade.setUserId(FAKE_USER_ID + "2");
        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        exception.expect(AuthenticationException.class);

        List<ProtoGrade> testGrades = GradeManager.getAllAssignmentGradesInstructor(authenticator, db, courseId, FAKE_ADMIN_ID);
        Assert.assertEquals(expectedGrades, testGrades);
    }

    @Test(expected = AuthenticationException.class)
    public void getAllAssignmentGradesInstructorNotAuthorizedTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());

        List<ProtoGrade> testGrades = GradeManager.getAllAssignmentGradesInstructor(authenticator, db, courseId, FAKE_USER_ID);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getAllAssignmentGradesInstructorNoGradesTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> testGrades = GradeManager.getAllAssignmentGradesInstructor(authenticator, db, courseId, FAKE_ADMIN_ID);
    }

    @Test
    public void getAllAssignmentGradesStudentTest() throws Exception {

        // for all assignments teacher permission
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, null,
                FAKE_ADMIN_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> expectedGrades = new ArrayList<>();
        fakeProtoGrade.setAssignmentId(FAKE_ASGN_ID + "1");
        fakeProtoGrade.clearProblemId();
        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        fakeProtoGrade.setAssignmentId(FAKE_ASGN_ID + "2");


        GradeManager.addGrade(authenticator, db, FAKE_ADMIN_ID, fakeProtoGrade.build());
        expectedGrades.add(fakeProtoGrade.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, FAKE_COURSE_ID,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<ProtoGrade> testGrades = GradeManager.getAllAssignmentGradesStudent(authenticator, db, FAKE_COURSE_ID, FAKE_USER_ID, FAKE_USER_ID);
        Assert.assertEquals(expectedGrades, testGrades);
    }

    @Test(expected = AuthenticationException.class)
    public void getAllAssignmentGradesStudentNotInCourseTest() throws Exception {
        GradeManager.getAllAssignmentGradesStudent(authenticator, db, FAKE_COURSE_ID, FAKE_USER_ID, FAKE_USER_ID);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getAllAssignmentGradesStudentNoGradesTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId,
                FAKE_USER_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradeManager.getAllAssignmentGradesStudent(authenticator, db, courseId, FAKE_USER_ID, FAKE_USER_ID);
    }
}
