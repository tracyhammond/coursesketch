package database.institution.mongo;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.school.School;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.utils.Util;

import java.util.List;

import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.SCRIPT;
import static database.DatabaseStringConstants.USERS;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;
import static database.institution.mongo.BankProblemManager.mongoUpdateBankProblem;

public class BankProblemManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String FAKE_SCRIPT = "fake script";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    @Before
    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));
    }

    @Test
    public void insertBankProblemNoPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        // checks it does not exist.
        Assert.assertFalse(bankProblem.hasAccessPermission());

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());
    }

    @Test
    public void insertBankProblemPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);
        permissionBuilder.addUserPermission(USER_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(mongoBankProblem.get(USERS), permissionBuilder.getUserPermissionList());
        Assert.assertEquals(mongoBankProblem.get(ADMIN), permissionBuilder.getAdminPermissionList());
    }

    @Test(expected = AuthenticationException.class)
    public void getBankProblemNoPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        // checks it does not exist.
        Assert.assertFalse(bankProblem.hasAccessPermission());

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        BankProblemManager.mongoGetBankProblem(fauth, db, problemBankId, ADMIN_USER);
    }

    @Test
    public void getBankProblemAdminAccess() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);
        permissionBuilder.addUserPermission(USER_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(fauth, db, problemBankId, ADMIN_USER);

        Assert.assertTrue(resultBankProblem.hasAccessPermission());
        Assert.assertEquals(resultBankProblem.getAccessPermission().getUserPermissionList(), permissionBuilder.getUserPermissionList());
        Assert.assertEquals(resultBankProblem.getAccessPermission().getAdminPermissionList(), permissionBuilder.getAdminPermissionList());
    }

    @Test
    public void getBankProblemUserAccess() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);
        permissionBuilder.addUserPermission(USER_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(fauth, db, problemBankId, USER_USER);

        Assert.assertFalse(resultBankProblem.hasAccessPermission());
    }

    @Test
    public void getAllBankProblemsPageZero() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(fauth, db, ADMIN_USER, courseId, 0);
        Assert.assertEquals(2, resultBankProblem.size());
    }

    @Test(expected = AuthenticationException.class)
    public void getAllBankProblemsNoPermission() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(fauth, db, USER_USER, courseId, 0);
    }

    @Test
    public void getAllBankProblemsPageOne() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(fauth, db, ADMIN_USER, courseId, 1);
        Assert.assertEquals(0, resultBankProblem.size());
    }

    @Test
    public void registerCourseInBankProblem() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setCourseId(courseId);
        problem.setProblemBankId(problemBankId);

        BankProblemManager.mongoRegisterCourseProblem(fauth, db, USER_USER, problem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(USERS)).get(0));
    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoBankId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setCourseId("Course id");

        BankProblemManager.mongoRegisterCourseProblem(fauth, db, USER_USER, problem.build());

    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoCourseId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setProblemBankId("Bank id");

        BankProblemManager.mongoRegisterCourseProblem(fauth, db, USER_USER, problem.build());

    }

    @Test
    public void testSetScript() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        mongoInsertBankProblem(db, bankProblem.build());
        DBCursor curse = db.getCollection(PROBLEM_BANK_COLLECTION).find();
        System.out.println(curse);
        DBObject obj = curse.next();
        String testString = obj.get(SCRIPT).toString();
        Assert.assertEquals(FAKE_SCRIPT, testString);
    }

    /*
     * testGetScript tests getScript member function by inserting and then getting the value that was inserted.
     */
    @Test
    public void testGetScript() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);
        permissionBuilder.addUserPermission(USER_USER);
        bankProblem.setAccessPermission(permissionBuilder);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());
        SrlBankProblem getProblem = mongoGetBankProblem(fauth, db, problemBankId, ADMIN_USER);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(FAKE_SCRIPT, testString);
    }

    /*
     * testUpdateScript tests updateScript member function by inserting, updating,
     * and then getting the value that was inserted.
     */
    @Test
    public void testUpdateScript () throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);
        permissionBuilder.addUserPermission(USER_USER);
        bankProblem.setAccessPermission(permissionBuilder);
        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        String newFakeScript = "Faker Script";
        School.SrlBankProblem.Builder updateBankProblem = SrlBankProblem.newBuilder();
        updateBankProblem.setId(FAKE_ID);
        updateBankProblem.setScript(newFakeScript);

        boolean isUpdated = mongoUpdateBankProblem(fauth, db, problemBankId, ADMIN_USER, updateBankProblem.build());
        Assert.assertEquals(true, isUpdated);
        final SrlBankProblem getProblem = mongoGetBankProblem(fauth, db, problemBankId, ADMIN_USER);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(newFakeScript, testString);
    }
}