package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationChecker;
import database.auth.AuthenticationDataCreator;
import database.auth.AuthenticationException;
import database.auth.AuthenticationOptionChecker;
import database.auth.Authenticator;
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
import utilities.AuthenticationHelper;

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

    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    private String courseId;
    private String assignmentId;
    private String courseProblemId;

    private School.SrlProblem.Builder defaultProblem;

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
        updateProblemIds(courseId, assignmentId);
    }

    public void updateProblemIds(String courseId, String assignmentId) {
        defaultProblem.setCourseId(courseId);
        defaultProblem.setAssignmentId(assignmentId);
    }

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

    // TODO TESTS
    // test that throws exception if assignment is out of data and person is user of course problem
    // test that throws exception if course problem is grabbed by someone without access
    // test that throws exception if someone without permission attempts to update course problem
    // test that grabs problem correctly if user is accessing within valid timeframe
    // test that updates problem correctly if has valid permission

    /**
     * checks that the course is registered for the bank problem when a course problem is inserted.
     */
    @Test
    public void registerBankProblemIfItIsNotRegistered() throws Exception  {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        // creating problem
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setAssignmentId(assignmentId);
        problem.setCourseId(courseId);
        problem.setProblemBankId(problemBankId);

        CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, problem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        // TODO(dtracers): change what is being tested to better reflect what is being asked.
        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(USERS)).get(0));
    }
}
