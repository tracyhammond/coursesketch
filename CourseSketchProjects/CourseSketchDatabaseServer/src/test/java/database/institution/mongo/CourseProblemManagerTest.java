package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
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

import java.util.List;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.USERS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Created by gigemjt on 3/22/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CourseProblemManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public DB db;
    public Authenticator authenticator;

    public static final long FAKE_VALID_DATE = 1000;
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    private String courseId;
    private String assignmentId;
    private String courseProblemId;
    private String bankProblemId;

    private School.SrlProblem.Builder defaultProblem;
    private AuthenticationDataCreator dataCreator;

    private School.SrlBankProblem.Builder bankProblem;

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
        courseProblemId = null;

        defaultProblem = School.SrlProblem.newBuilder();
        defaultProblem.setId("ID");
    }

    public void insertCourseAndAssignment() throws DatabaseAccessException, AuthenticationException {

        // creating bank problem
        bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        bankProblemId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        bankProblem.setId(bankProblemId);

        // creating the course
        final School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        courseId = CourseManager.mongoInsertCourse(db, course.build());
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        final School.SrlAssignment.Builder assignment = School.SrlAssignment.newBuilder();
        assignment.setId("ID");
        assignment.setCourseId(courseId);

        assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, assignment.build());
        updateProblemIds(courseId, assignmentId, bankProblemId);

        // sets the course able to use the bank problem
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId, courseId,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, FAKE_VALID_DATE, true);
        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
    }

    public void updateProblemIds(String courseId, String assignmentId, String bankProblemId) {
        defaultProblem.setCourseId(courseId);
        defaultProblem.setAssignmentId(assignmentId);
        defaultProblem.setProblemBankId(bankProblemId);
    }


    // INSERTION TESTS
    @Test(expected = DatabaseAccessException.class)
    public void insertCourseProblemFailsWithNoAssignmentData() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
    }

    @Test(expected = AuthenticationException.class)
    public void insertCourseProblemFailsWithInvalidPermission() throws Exception {
        insertCourseAndAssignment();
        CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
    }

    @Test
    public void insertCourseProblemIntoAssignmentWithValidPermission() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
    }

    // GETTING TEST

    // Precondition tests
    @Test(expected = DatabaseAccessException.class)
    public void getCourseProblemWithInvalidObjectId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.clearProblemBankId();
        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        CourseProblemManager.mongoGetCourseProblem(authenticator, db,
                DatabaseHelper.createNonExistentObjectId(courseProblemId), ADMIN_USER, FAKE_VALID_DATE);
    }

    // Student grabbing test
    @Test(expected = DatabaseAccessException.class)
    public void getCourseProblemThatDoesNotExistWithMalformedObjectId() throws Exception {
        CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getCourseProblemWithInvalidPermission() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());

        CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getCourseProblemWithStudentPermissionButOutSideOfSchoolHours() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, FAKE_INVALID_DATE);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseProblemWithStudentPermissionButNotPublished() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, FAKE_VALID_DATE);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseProblemAsStudentWithNoBankProblemId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.clearProblemBankId();

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, FAKE_VALID_DATE);
    }

    @Test
    public void getCourseProblemAsStudentWithValidDatePublishedAndBankProblemId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        defaultProblem.setProblemInfo(bankProblem);

        final School.SrlProblem problem = CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, USER_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .build().equals(defaultProblem.build(), problem);
    }

    // Teacher grabbing permissions

    @Test
    public void getCourseProblemAsInstructorWithNoBankProblemIdShouldWork() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.clearProblemBankId();
        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.ASSIGNMENT, assignmentId, true);

        School.SrlProblem problem = CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                //.ignoreField(School.SrlProblem.getDescriptor().findFieldByName("accessPermission"))
                .build().equals(defaultProblem.build(), problem);
    }

    @Test
    public void getCourseProblemAsInstructorWithWrongDateAndNotPublishedShouldWork() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);
        defaultProblem.setProblemInfo(bankProblem);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlProblem problem = CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                //.ignoreField(School.SrlProblem.getDescriptor().findFieldByName("accessPermission"))
                .build().equals(defaultProblem.build(), problem);
    }

    // UPDATING TESTS

    @Test(expected = DatabaseAccessException.class)
    public void updateCourseProblemWithInvalidObjectId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.clearProblemBankId();
        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        CourseProblemManager.mongoUpdateCourseProblem(authenticator, db, DatabaseHelper.createNonExistentObjectId(courseProblemId),
                ADMIN_USER, defaultProblem.build());
    }

    @Test
    public void updateCourseProblemAsInstructor() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);
        defaultProblem.setProblemInfo(bankProblem);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlProblem problem = CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .build().equals(defaultProblem.build(), problem);

        School.SrlProblem updatedProblem = School.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .setName("New name")
                .build();

        CourseProblemManager.mongoUpdateCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, updatedProblem);

        School.SrlProblem updatedProblemResult = CourseProblemManager.mongoGetCourseProblem(authenticator, db,
                courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedProblemResult);
    }

    @Test
    public void updateCourseProblemAsInstructorWithNewBankId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());
        defaultProblem.setId(courseProblemId);
        defaultProblem.setProblemInfo(bankProblem);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlProblem problem = CourseProblemManager.mongoGetCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .build().equals(defaultProblem.build(), problem);


        final School.SrlBankProblem.Builder bankProblem2 = School.SrlBankProblem.newBuilder();
        bankProblem2.setId("NOT REAL ID");
        bankProblem2.setQuestionText(FAKE_QUESTION_TEXT + "NEW");

        String bankProblemId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem2.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId2, courseId,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        School.SrlProblem.Builder updatedProblem = School.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .setProblemBankId(bankProblemId2);

        CourseProblemManager.mongoUpdateCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, updatedProblem.build());

        // change the data contained in the update problem bc it should now contain new data.
        updatedProblem.setProblemInfo(bankProblem2.setId(bankProblemId2));

        School.SrlProblem updatedProblemResult = CourseProblemManager.mongoGetCourseProblem(authenticator, db,
                courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem.build(), updatedProblemResult);
    }

    @Test(expected = AuthenticationException.class)
    public void updateCourseProblemAsStudentFails() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, defaultProblem.build());

        School.SrlProblem updatedProblem = School.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .build();

        CourseProblemManager.mongoUpdateCourseProblem(authenticator, db, courseProblemId, USER_USER, updatedProblem);
    }


}
