package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.DbSchoolUtility;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Created by gigemjt on 3/22/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AssignmentManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public DB db;
    public Authenticator authenticator;

    public static final String VALID_NAME = "Valid course name!";
    public static final String FAKE_DESCRIPTION = "DESCRIPTIONS YAY";
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final long FAKE_VALID_DATE = 1000;
    public static final Util.DateTime FAKE_VALID_DATE_OBJECT = Util.DateTime.newBuilder().setMillisecond(FAKE_VALID_DATE).build();
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    private static final School.SrlAssignment.AssignmentType VALID_ASSIGNMENT_TYPE = School.SrlAssignment.AssignmentType.EXAM;
    private static final int VALID_ASSIGNMENT_TYPE_VALUE = School.SrlAssignment.AssignmentType.EXAM_VALUE;

    private String courseId;
    private String assignmentId;

    private School.SrlAssignment.Builder defaultAssignment;
    private AuthenticationDataCreator dataCreator;

    // TODO TESTS
    // To be done in second refactor
    // test that ensures that when inserting course problem permissions are copied over correctly
    //          this can be done by having someone who only has permission to a certain part and check that now new permissions are added.

    @Before
    public void before() {
        db = fongo.getDB();

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
        courseId = null;
        assignmentId = null;

        defaultAssignment = School.SrlAssignment.newBuilder();
        defaultAssignment.setId("ID");
    }

    public void insertCourse() throws DatabaseAccessException, AuthenticationException {

        // creating the course
        final School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        courseId = CourseManager.mongoInsertCourse(db, course.build());
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
    }

    // INSERTION TESTS
    @Test(expected = AuthenticationException.class)
    public void insertAssignmentFailsWithInvalidPermission() throws Exception {
        insertCourse();

        // reset permission level to be no permisison
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.NO_PERMISSION);
        AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
    }

    @Test
    public void insertAssignmentIntoAssignmentWithValidPermission() throws Exception {
        insertCourse();

        AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
    }

    @Test
    public void insertAssignmentIntoAssignmentSetsValuesCorrectly() throws Exception {
        insertCourse();

        defaultAssignment.setDescription(FAKE_DESCRIPTION);
        defaultAssignment.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setDueDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setName(VALID_NAME);
        defaultAssignment.setAssignmentType(VALID_ASSIGNMENT_TYPE);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        final DBRef myDbRef = new DBRef(db, DbSchoolUtility.getCollectionFromType(School.ItemType.ASSIGNMENT, true), new ObjectId(assignmentId));
        final DBObject mongoAssignment = myDbRef.fetch();

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DUE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_TYPE), VALID_ASSIGNMENT_TYPE_VALUE);
    }

    // GETTING TEST

    // Precondition tests
    @Test(expected = DatabaseAccessException.class)
    public void getAssignmentWithInvalidObjectId() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
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
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getAssignmentWithStudentPermissionButOutSideOfSchoolHours() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_INVALID_DATE);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getAssignmentWithStudentPermissionButNotPublished() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
    }

    @Test
    public void getAssignmentAsStudentWithValidDateAndPublished() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        final School.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentThatIsOpenReturnsProblemList() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    School.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemList(problemId);
        }

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        final School.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentThatIsClosedReturnsNoProblemList() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            // We do not save the newly created problemIds because an empty problem list should be returned.
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    School.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
        }

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, null, School.ItemType.ASSIGNMENT, assignmentId, true);

        // makes the assignment closed
        AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, FAKE_VALID_DATE, false);

        final School.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, USER_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    // Teacher grabbing permissions

    @Test
    public void getAssignmentAsInstructorWithWrongDateAndNotPublishedShouldWork() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlAssignment problem = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), problem);
    }

    @Test
    public void getAssignmentReturnsProblemListWhenOpenAndPublishedAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    School.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemList(problemId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        final School.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), srlAssignment);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, false);
    }


    @Test
    public void getAssignmentReturnsProblemListWhenNotPublishedAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    School.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemList(problemId);
        }
        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, false);

        final School.SrlAssignment srlAssignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultAssignment.build(), srlAssignment);
    }

    @Test
    public void getAssignmentReturnsProblemListWhenNotOpenAsInstructor() throws Exception {
        insertCourse();

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultAssignment.setId(assignmentId);

        for (int i = 0; i < 5; i++) {
            String problemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER,
                    School.SrlProblem.newBuilder()
                            .setId("ID")
                            .setAssignmentId(assignmentId)
                            .setCourseId(courseId)
                            .build());
            defaultAssignment.addProblemList(problemId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, false);

        final School.SrlAssignment srlAssignment3 = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId,
                FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), srlAssignment3);
    }

    // UPDATING TESTS

    @Test(expected = DatabaseAccessException.class)
    public void updateAssignmentWithInvalidObjectId() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
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

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlAssignment problem = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), problem);

        School.SrlAssignment updatedProblem = School.SrlAssignment.newBuilder(defaultAssignment.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .setName("New Name")
                .setDescription("New Description")
                .setAssignmentType(School.SrlAssignment.AssignmentType.EXAM)
                .setOther("OTHERTYPE")
                .addLinks("NEW LINK. http")
                .setImageUrl("IMAGEURL")
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, ADMIN_USER, assignmentId, updatedProblem);

        School.SrlAssignment updatedAssignmentResult = AssignmentManager.mongoGetAssignment(authenticator, db,
                ADMIN_USER, assignmentId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedAssignmentResult);
    }

    @Test
    public void updateAssignmentDoesNotUpdateProblemList() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());
        defaultAssignment.setId(assignmentId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlAssignment assignment = AssignmentManager.mongoGetAssignment(authenticator, db, ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), assignment);

        School.SrlAssignment updatedAssignment = School.SrlAssignment.newBuilder(defaultAssignment.build())
                .addProblemList("NEW PROBLEM VALUE!")
                .addProblemList("NEW PROBLEM VALUE2")
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, ADMIN_USER, assignmentId, updatedAssignment);

        School.SrlAssignment updatedAssignmentResult = AssignmentManager.mongoGetAssignment(authenticator, db,
                ADMIN_USER, assignmentId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultAssignment.build(), updatedAssignmentResult);
    }

    @Test(expected = AuthenticationException.class)
    public void updateAssignmentAsStudentFails() throws Exception {
        insertCourse();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, defaultAssignment.build());

        School.SrlAssignment updatedProblem = School.SrlAssignment.newBuilder(defaultAssignment.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .build();

        AssignmentManager.mongoUpdateAssignment(authenticator, db, USER_USER, assignmentId, updatedProblem);
    }
}
