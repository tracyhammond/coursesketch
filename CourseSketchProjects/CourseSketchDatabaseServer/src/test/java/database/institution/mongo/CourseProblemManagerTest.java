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

import java.util.List;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.USERS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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

    @Before
    public void before() {
        db = fongo.getDB();

        try {
            when(authChecker.isAuthenticated(any(School.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                    .thenReturn(Authentication.AuthResponse.getDefaultInstance());

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

    /**
     * checks that the course is registered for the bank problem when a course problem is inserted.
     */
    @Test
    public void registerBankProblemIfItIsNotRegistered() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        // creating the course
        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        // creating assignment
        School.SrlAssignment.Builder assignment = School.SrlAssignment.newBuilder();
        assignment.setId("ID");
        assignment.setCourseId(courseId);
        assignment.setAccessPermission(permissionBuilder);
        String assigmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER, assignment.build());

        // creating problem
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setAssignmentId(assigmentId);
        problem.setCourseId(courseId);
        problem.setProblemBankId(problemBankId);

        CourseProblemManager.mongoInsertCourseProblem(authenticator, db, ADMIN_USER, problem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(USERS)).get(0));
    }
}
