package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.school.School;
import protobuf.srl.utils.Util;

import java.util.List;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.USERS;

/**
 * Created by gigemjt on 3/22/15.
 */
public class CourseProblemManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;

    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    @Before
    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));
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
        String assigmentId = AssignmentManager.mongoInsertAssignment(fauth, db, ADMIN_USER, assignment.build());

        // creating problem
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setAssignmentId(assigmentId);
        problem.setCourseId(courseId);
        problem.setProblemBankId(problemBankId);

        CourseProblemManager.mongoInsertCourseProblem(fauth, db, ADMIN_USER, problem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(USERS)).get(0));
    }
}
