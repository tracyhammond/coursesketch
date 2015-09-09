package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCursor;
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
import protobuf.srl.commands.Commands;
import protobuf.srl.school.School;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.List;

import static database.DatabaseStringConstants.BASE_SKETCH;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.SCRIPT;
import static database.DatabaseStringConstants.USERS;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;
import static database.institution.mongo.BankProblemManager.mongoUpdateBankProblem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankProblemManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public DB db;
    public Authenticator authenticator;
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String FAKE_SCRIPT = "fake script";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Commands.SrlUpdateList.Builder FAKE_UPDATELIST = Commands.SrlUpdateList.newBuilder();
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    @Before
    public void before() {
        db = fongo.getDB();

        try {
            // general results
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
    public void insertBankProblemNoPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);
        bankProblem.setBaseSketch(FAKE_UPDATELIST.build());

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());
        Assert.assertEquals(Commands.SrlUpdateList.parseFrom((byte[]) mongoBankProblem.get(BASE_SKETCH)),
                FAKE_UPDATELIST.build());
    }

    // TODO: test insertion of permissions
    @Test
    public void insertBankProblemPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

    }

    @Test(expected = AuthenticationException.class)
    public void getBankProblemNoPermissions() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        BankProblemManager.mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);
    }

    @Test
    public void getBankProblemAdminAccess() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);

        Assert.assertEquals(resultBankProblem.getQuestionText(), FAKE_QUESTION_TEXT);
    }

    @Test
    public void getBankProblemUserAccess() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        when(authChecker.isAuthenticated(eq(School.ItemType.BANK_PROBLEM), eq(problemBankId), eq(USER_USER), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.STUDENT)
                        .build());

        School.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(authenticator, db, problemBankId, USER_USER);

        Assert.assertFalse(resultBankProblem.hasAccessPermission());
    }

    @Test
    public void getAllBankProblemsPageZero() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, ADMIN_USER, courseId, 0);
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

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, USER_USER, courseId, 0);
    }

    @Test
    public void getAllBankProblemsPageOne() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        String courseId = CourseManager.mongoInsertCourse(db, course.build());
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        List<School.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, ADMIN_USER, courseId, 1);
        Assert.assertEquals(0, resultBankProblem.size());
    }

    @Test
    public void registerCourseInBankProblem() throws Exception {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setCourseId(courseId);
        problem.setProblemBankId(problemBankId);

        BankProblemManager.mongoRegisterCourseProblem(authenticator, db, USER_USER, problem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(USERS)).get(0));
    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoBankId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setCourseId("Course id");

        BankProblemManager.mongoRegisterCourseProblem(authenticator, db, USER_USER, problem.build());

    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoCourseId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setProblemBankId("Bank id");

        BankProblemManager.mongoRegisterCourseProblem(authenticator, db, USER_USER, problem.build());

    }

    @Test
    public void updateBankProblemAsInstructor() throws Exception {
        SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String bankProblemId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        bankProblem.setId(bankProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, db, bankProblemId, ADMIN_USER);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(bankProblem.build(), problem);

        School.SrlBankProblem updatedProblem = School.SrlBankProblem.newBuilder(bankProblem.build())
                .setQuestionText("New QuestionText")
                .setCourseTopic("New course topic")
                .setImage("image")
                .addOtherKeywords("Keywords")
                .setSubTopic("Topic")
                .setSource("New source")
                .setSolutionId("New solutionId")
                .setQuestionType(Util.QuestionType.CHECK_BOX)
                .build();

        BankProblemManager.mongoUpdateBankProblem(authenticator, db, bankProblemId, ADMIN_USER, updatedProblem);

        School.SrlBankProblem updatedBankProblemResult = BankProblemManager.mongoGetBankProblem(authenticator, db,
                bankProblemId, ADMIN_USER);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedBankProblemResult);
    }

    @Test
    public void updateBankProblemAsInstructorButNothingChanges() throws Exception {
        SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String bankProblemId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        bankProblem.setId(bankProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, db, bankProblemId, ADMIN_USER);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(bankProblem.build(), problem);

        School.SrlBankProblem updatedProblem = School.SrlBankProblem.newBuilder(bankProblem.build())
                .build();

        BankProblemManager.mongoUpdateBankProblem(authenticator, db, bankProblemId, ADMIN_USER, updatedProblem);

        School.SrlBankProblem updatedBankProblemResult = BankProblemManager.mongoGetBankProblem(authenticator, db,
                bankProblemId, ADMIN_USER);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedBankProblemResult);
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

    @Test
    public void testSetBaseSketch() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setBaseSketch(FAKE_UPDATELIST.build());

        mongoInsertBankProblem(db, bankProblem.build());
        DBCursor curse = db.getCollection(PROBLEM_BANK_COLLECTION).find();
        System.out.println(curse);
        DBObject obj = curse.next();
        Commands.SrlUpdateList UpdateList = Commands.SrlUpdateList.parseFrom((byte[]) obj.get(BASE_SKETCH));
        Assert.assertEquals(FAKE_UPDATELIST.build(), UpdateList);
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

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(FAKE_SCRIPT, testString);
    }

    @Test
    public void testGetBaseSketch() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setBaseSketch(FAKE_UPDATELIST.build());

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);
        Commands.SrlUpdateList testUpdateList = getProblem.getBaseSketch();
        Assert.assertEquals(FAKE_UPDATELIST.build(), testUpdateList);
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

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        String newFakeScript = "Faker Script";
        School.SrlBankProblem.Builder updateBankProblem = SrlBankProblem.newBuilder();
        updateBankProblem.setId(FAKE_ID);
        updateBankProblem.setScript(newFakeScript);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        boolean isUpdated = mongoUpdateBankProblem(authenticator, db, problemBankId, ADMIN_USER, updateBankProblem.build());
        Assert.assertEquals(true, isUpdated);
        final SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(newFakeScript, testString);
    }

    /*
     * testUpdateScript tests updateScript member function by inserting, updating,
     * and then getting the value that was inserted.
     */
    @Test(expected = AuthenticationException.class)
    public void testUpdateScriptFailsWithInvalidPermission() throws Exception {
        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        String newFakeScript = "Faker Script";
        School.SrlBankProblem.Builder updateBankProblem = SrlBankProblem.newBuilder();
        updateBankProblem.setId(FAKE_ID);
        updateBankProblem.setScript(newFakeScript);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        boolean isUpdated = mongoUpdateBankProblem(authenticator, db, problemBankId, ADMIN_USER, updateBankProblem.build());
        Assert.assertEquals(true, isUpdated);
        final SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, problemBankId, ADMIN_USER);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(newFakeScript, testString);
    }
}
