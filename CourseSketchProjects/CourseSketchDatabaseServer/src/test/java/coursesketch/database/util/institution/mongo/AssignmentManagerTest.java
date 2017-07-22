package coursesketch.database.util.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.RequestConverter;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.school.Assignment;
import protobuf.srl.school.Problem;
import protobuf.srl.school.School;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;
import utilities.TimeManager;

import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.institution.mongo.MongoInstitutionTest.genericDatabaseMock;
import static coursesketch.database.util.utilities.MongoUtilities.convertStringToObjectId;
import static org.mockito.Matchers.any;

/**
 * Created by gigemjt on 3/22/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TimeManager.class)
public class AssignmentManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public MongoDatabase db;
    public Authenticator authenticator;

    public static final String VALID_NAME = "Valid course name!";
    public static final String FAKE_DESCRIPTION = "DESCRIPTIONS YAY";
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final long FAKE_VALID_DATE = 1000;
    public static final Util.DateTime FAKE_VALID_DATE_OBJECT = Util.DateTime.newBuilder().setMillisecond(FAKE_VALID_DATE).build();
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    private static final Assignment.AssignmentType VALID_ASSIGNMENT_TYPE = Assignment.AssignmentType.PRACTICE;
    private static final Assignment.NavigationType VALID_NAVIGATION_TYPE = Assignment.NavigationType.LOOPING;

    private String courseId;
    private String assignmentId;

    private Assignment.SrlAssignment.Builder defaultAssignment;
    private AuthenticationDataCreator dataCreator;

    // TODO TESTS
    // To be done in second refactor
    // test that ensures that when inserting course problem permissions are copied over correctly
    //          this can be done by having someone who only has permission to a certain part and check that now new permissions are added.

    @Before
    public void before() throws Exception {

        PowerMockito.mockStatic(TimeManager.class);
        PowerMockito.when(TimeManager.class, "getSystemTime").thenReturn(100L);
        db = fongo.getDatabase();

        genericDatabaseMock(authChecker, optionChecker);
        authenticator = new Authenticator(authChecker, optionChecker);
        courseId = null;
        assignmentId = null;

        defaultAssignment = Assignment.SrlAssignment.newBuilder();
        defaultAssignment.setId(FAKE_ID);
        defaultAssignment.setAssignmentCatagory(DatabaseStringConstants.HOMEWORK_CATEGORY);
    }

    public void insertCourse() throws DatabaseAccessException, AuthenticationException {

        // creating the course
        final School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId(FAKE_ID);
        courseId = CourseManager.mongoInsertCourse(db, course.build());
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
    }

    // INSERTION TESTS
    @Test(expected = AuthenticationException.class)
    public void insertAssignmentFailsWithInvalidPermission() throws Exception {
        insertCourse();

        // reset permission level to be no permisison
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.NO_PERMISSION);
        AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
    }

    @Test
    public void insertAssignmentIntoAssignmentWithValidPermission() throws Exception {
        insertCourse();

        AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
    }

    @Test
    public void insertAssignmentIntoAssignmentSetsDefaultsCorrectly() throws Exception {
        defaultAssignment.clear();

        // this must be done after the clear because this sets the course id for the assignment
        insertCourse();

        defaultAssignment.setId(FAKE_ID);
        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        final MongoCollection<Document> collection = db.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document mongoAssignment = collection.find(convertStringToObjectId(assignmentId)).first();

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ACCESS_DATE), TimeManager.getSystemTime());
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.CLOSE_DATE), RequestConverter.getMaxTime());
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DUE_DATE), RequestConverter.getMaxTime());
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.REVIEW_OPEN_DATE), -1);

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_CATEGORY), DatabaseStringConstants.HOMEWORK_CATEGORY);

        // compares against value not enum
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_TYPE), Assignment.AssignmentType.GRADED_VALUE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAVIGATION_TYPE), Assignment.NavigationType.DEFAULT_VALUE);

        // empty fields
        Assert.assertFalse(mongoAssignment.containsKey(DatabaseStringConstants.GRADE_WEIGHT));
        Assert.assertFalse(mongoAssignment.containsKey(DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE));
        Assert.assertFalse(mongoAssignment.containsKey(DatabaseStringConstants.LATE_POLICY_RATE));
        Assert.assertFalse(mongoAssignment.containsKey(DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE));
        Assert.assertFalse(mongoAssignment.containsKey(DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE));
    }

    @Test
    public void insertAssignmentIntoAssignmentSetsValuesCorrectly() throws Exception {
        insertCourse();

        defaultAssignment.setDescription(FAKE_DESCRIPTION);
        defaultAssignment.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setDueDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setReviewOpenDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setName(VALID_NAME);
        defaultAssignment.setAssignmentType(VALID_ASSIGNMENT_TYPE);
        defaultAssignment.setNavigationType(VALID_NAVIGATION_TYPE);
        defaultAssignment.setAssignmentCatagory(DatabaseStringConstants.QUIZ_CATEGORY);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        final MongoCollection<Document> assignmentCollection = db.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document mongoAssignment = assignmentCollection.find(convertStringToObjectId(assignmentId)).first();

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DUE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.REVIEW_OPEN_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_TYPE), VALID_ASSIGNMENT_TYPE.getNumber());
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAVIGATION_TYPE), VALID_NAVIGATION_TYPE.getNumber());
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_CATEGORY), DatabaseStringConstants.QUIZ_CATEGORY);
    }

    // GETTING TEST

    // Precondition tests
    @Test(expected = DatabaseAccessException.class)
    public void getAssignmentWithInvalidObjectId() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AssignmentManager.mongoGetAssignment(authenticator, db,
                ADMIN_USER, DatabaseHelper.createNonExistentObjectId(assignmentId), FAKE_VALID_DATE);
    }

    // Student grabbing test
    @Test(expected = DatabaseAccessException.class)
    public void getAssignmentThatDoesNotExistWithMalformedObjectId() throws Exception {
        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getAssignmentWithInvalidPermission() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getAssignmentWithStudentPermissionButOutSideOfSchoolHours() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_INVALID_DATE);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getAssignmentWithStudentPermissionButNotPublished() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
    }

    @Test
    public void getAssignmentAsStudentWithValidDateAndPublished() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, true);

        final Assignment.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentThatIsOpenReturnsProblemList() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    Problem.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemGroups(problemId);
        }

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, true);

        final Assignment.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentThatIsClosedReturnsNoProblemList() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            // We do not save the newly created problemIds because an empty problem list should be returned.
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    Problem.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
        }

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, null, Util.ItemType.ASSIGNMENT, assignmentId, true);

        // makes the assignment closed
        AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, FAKE_VALID_DATE, false);

        final Assignment.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    // Teacher grabbing permissions

    @Test
    public void getAssignmentAsInstructorWithWrongDateAndNotPublishedShouldWork() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Assignment.SrlAssignment problem = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), problem);
    }

    @Test
    public void getAssignmentReturnsProblemListWhenOpenAndPublishedAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    Problem.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemGroups(problemId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, true);

        final Assignment.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), srlAssignment);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, false);
    }

    @Test
    public void getAssignmentReturnsProblemListWhenNotPublishedAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    Problem.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemGroups(problemId);
        }
        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, false);

        final Assignment.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentReturnsProblemListWhenNotOpenAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    Problem.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemGroups(problemId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, false);

        final Assignment.SrlAssignment srlAssignment3 = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId,
                FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), srlAssignment3);
    }

    // UPDATING TESTS

    @Test(expected = DatabaseAccessException.class)
    public void updateAssignmentWithInvalidObjectId() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AssignmentManager.mongoUpdateAssignment(authenticator, db, ADMIN_USER, DatabaseHelper.createNonExistentObjectId(assignmentId),
                defaultAssignment.build());
    }

    @Test
    public void updateAssignmentAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Assignment.SrlAssignment problem = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), problem);

        Assignment.SrlAssignment updatedProblem = Assignment.SrlAssignment.newBuilder(defaultAssignment.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .setName("New Name")
                .setDescription("New Description")
                .setAssignmentType(Assignment.AssignmentType.FLASHCARD)
                .setNavigationType(Assignment.NavigationType.RANDOM)
                .setAssignmentCatagory("OTHERTYPE")
                .setReviewOpenDate(FAKE_VALID_DATE_OBJECT)
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, ADMIN_USER, assignmentId, updatedProblem);

        Assignment.SrlAssignment updatedAssignmentResult = AssignmentManager.mongoGetAssignment(authenticator, db,
                ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedAssignmentResult);
    }

    @Test
    public void updateAssignmentDoesNotUpdateProblemList() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Assignment.SrlAssignment assignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), assignment);

        Assignment.SrlAssignment updatedAssignment = Assignment.SrlAssignment.newBuilder(defaultAssignment.build())
                .addProblemGroups("NEW PROBLEM VALUE!")
                .addProblemGroups("NEW PROBLEM VALUE2")
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, ADMIN_USER, assignmentId, updatedAssignment);

        Assignment.SrlAssignment updatedAssignmentResult = AssignmentManager.mongoGetAssignment(authenticator, db,
                ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), updatedAssignmentResult);
    }

    @Test(expected = AuthenticationException.class)
    public void updateAssignmentAsStudentFails() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        Assignment.SrlAssignment updatedProblem = Assignment.SrlAssignment.newBuilder(defaultAssignment.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, USER_USER, assignmentId, updatedProblem);
    }
}
