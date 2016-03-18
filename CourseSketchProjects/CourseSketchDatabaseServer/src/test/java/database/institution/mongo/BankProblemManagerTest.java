package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.commands.Commands;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.school.Problem;
import protobuf.srl.school.School;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.List;

import static database.DatabaseStringConstants.BASE_SKETCH;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.REGISTRATION_KEY;
import static database.DatabaseStringConstants.SCRIPT;
import static database.DbSchoolUtility.getCollectionFromType;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;
import static database.institution.mongo.BankProblemManager.mongoUpdateBankProblem;
import static database.utilities.MongoUtilities.convertStringToObjectId;
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
    @Mock AuthenticationDataCreator dataCreator;

    public DB db;
    public Authenticator authenticator;
    public static final String VALID_REGISTRATION_KEY = "VALID KEY!";
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

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM));
        final DBObject mongoBankProblem = collection.findOne(convertStringToObjectId(problemBankId));

        Assert.assertEquals(mongoBankProblem.get(REGISTRATION_KEY), VALID_REGISTRATION_KEY);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());
    }

    // TODO: test insertion of permissions
    @Test
    public void insertBankProblemPermissions() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM));
        final DBObject cursor = collection.findOne(convertStringToObjectId(problemBankId));

        Assert.assertNotNull(cursor);
    }

    @Test(expected = AuthenticationException.class)
    public void getBankProblemNoPermissions() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        BankProblemManager.mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);
    }

    @Test
    public void getBankProblemAdminAccess() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);

        Assert.assertEquals(resultBankProblem.getQuestionText(), FAKE_QUESTION_TEXT);
    }

    @Test
    public void getBankRegistrationKeyAdminAccessShouldReturnKey() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = BankProblemManager.mongoGetRegistrationKey(authenticator, db, ADMIN_USER, problemBankId);

        Assert.assertEquals(VALID_REGISTRATION_KEY, key);
    }

    @Test
    public void getBankRegistrationKeyNoAccessShouldBeNull() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = BankProblemManager.mongoGetRegistrationKey(authenticator, db, USER_USER, problemBankId);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getBankRegistrationKeyNoAccessWithNoRegistrationNotPublishedShouldBeNull() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, null, School.ItemType.BANK_PROBLEM,
                problemBankId, false);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = BankProblemManager.mongoGetRegistrationKey(authenticator, db, USER_USER, problemBankId);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getBankRegistrationKeyNoAccessWithRegistrationPublishedShouldBeNull() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, null, School.ItemType.BANK_PROBLEM,
                problemBankId, true);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.BANK_PROBLEM,
                problemBankId, true);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = BankProblemManager.mongoGetRegistrationKey(authenticator, db, USER_USER, problemBankId);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getBankRegistrationKeyNoAccessWithNoRegistrationIsPublishedShouldBeKey() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.BANK_PROBLEM,
                problemBankId, true);

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, dataCreator, School.ItemType.BANK_PROBLEM,
                problemBankId, false);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                problemBankId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = BankProblemManager.mongoGetRegistrationKey(authenticator, db, USER_USER, problemBankId);

        Assert.assertEquals(VALID_REGISTRATION_KEY, key);
    }

    @Test
    public void getBankProblemUserAccess() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        when(authChecker.isAuthenticated(eq(School.ItemType.BANK_PROBLEM), eq(problemBankId), eq(USER_USER), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.STUDENT)
                        .build());

        Problem.SrlBankProblem resultBankProblem = BankProblemManager.mongoGetBankProblem(authenticator, db, USER_USER, problemBankId);

    }

    @Test
    public void getAllBankProblemsPageZero() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
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

        List<Problem.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, ADMIN_USER, courseId, 0);
        Assert.assertEquals(2, resultBankProblem.size());
    }

    @Test(expected = AuthenticationException.class)
    public void getAllBankProblemsNoPermission() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
        permissionBuilder.addAdminPermission(ADMIN_USER);

        String problemBankId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        bankProblem.setId("NOT REAL ID2");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT + "2");
        String problemBankId2 = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());

        School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        course.setAccessPermission(permissionBuilder);
        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        List<Problem.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, USER_USER, courseId, 0);
    }

    @Test
    public void getAllBankProblemsPageOne() throws Exception {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
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

        List<Problem.SrlBankProblem> resultBankProblem = BankProblemManager.mongoGetAllBankProblems(authenticator, db, ADMIN_USER, courseId, 1);
        Assert.assertEquals(0, resultBankProblem.size());
    }

    @Test
    public void updateBankProblemAsInstructor() throws Exception {
        SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String bankProblemId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        bankProblem.setId(bankProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, db, ADMIN_USER, bankProblemId);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(bankProblem.build(), problem);

        Problem.SrlBankProblem updatedProblem = Problem.SrlBankProblem.newBuilder(bankProblem.build())
                .setQuestionText("New QuestionText")
                .setCourseTopic("New course topic")
                .addOtherKeywords("Keywords")
                .setSubTopic("Topic")
                .setSource("New source")
                .setSolutionId("New solutionId")
                .setQuestionType(Util.QuestionType.CHECK_BOX)
                .build();

        BankProblemManager.mongoUpdateBankProblem(authenticator, db, ADMIN_USER, bankProblemId, updatedProblem);

        Problem.SrlBankProblem updatedBankProblemResult = BankProblemManager.mongoGetBankProblem(authenticator, db,
                ADMIN_USER, bankProblemId);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedBankProblemResult);
    }

    @Test
    public void updateBankProblemAsInstructorButNothingChanges() throws Exception {
        SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String bankProblemId = BankProblemManager.mongoInsertBankProblem(db, bankProblem.build());
        bankProblem.setId(bankProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, bankProblemId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, db, ADMIN_USER, bankProblemId);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(bankProblem.build(), problem);

        Problem.SrlBankProblem updatedProblem = Problem.SrlBankProblem.newBuilder(bankProblem.build())
                .build();

        BankProblemManager.mongoUpdateBankProblem(authenticator, db, ADMIN_USER, bankProblemId, updatedProblem);

        Problem.SrlBankProblem updatedBankProblemResult = BankProblemManager.mongoGetBankProblem(authenticator, db,
                ADMIN_USER, bankProblemId);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedBankProblemResult);
    }

    @Test
    public void testSetScript() throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);

        mongoInsertBankProblem(db, bankProblem.build());
        DBCursor curse = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM)).find();
        System.out.println(curse);
        DBObject obj = curse.next();
        String testString = obj.get(SCRIPT).toString();
        Assert.assertEquals(FAKE_SCRIPT, testString);
    }

    @Test
    public void testSetBaseSketch() throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);

        final Lecturedata.LectureElement.Builder lectureElement = Lecturedata.LectureElement.newBuilder();

        // sets the base sketch data
        lectureElement.setSketchArea(Lecturedata.SketchArea.newBuilder().setRecorededSketch(FAKE_UPDATELIST));
        bankProblem.setSpecialQuestionData(lectureElement);

        mongoInsertBankProblem(db, bankProblem.build());
        DBCursor curse = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM)).find();
        System.out.println(curse);
        DBObject obj = curse.next();
        final Lecturedata.LectureElement elementFromQuery = SlideManager
                .createElementFromQuery((DBObject) obj.get(DatabaseStringConstants.SPECIAL_QUESTION_DATA));
        Commands.SrlUpdateList UpdateList = elementFromQuery.getSketchArea().getRecorededSketch();
        Assert.assertEquals(FAKE_UPDATELIST.build(), UpdateList);
    }

    /*
     * testGetScript tests getScript member function by inserting and then getting the value that was inserted.
     */
    @Test
    public void testGetScript() throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(FAKE_SCRIPT, testString);
    }

    @Test
    public void testGetBaseSketch() throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        final Lecturedata.LectureElement.Builder lectureElement = Lecturedata.LectureElement.newBuilder();

        // sets the base sketch data
        lectureElement.setSketchArea(Lecturedata.SketchArea.newBuilder().setRecorededSketch(FAKE_UPDATELIST));

        // sets the base sketch data
        bankProblem.setSpecialQuestionData(lectureElement);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);
        final Lecturedata.LectureElement specialQuestionData = getProblem.getSpecialQuestionData();
        new ProtobufComparisonBuilder().build().equals(lectureElement.build(), specialQuestionData);
    }

    /*
     * testUpdateScript tests updateScript member function by inserting, updating,
     * and then getting the value that was inserted.
     */
    @Test
    public void testUpdateScript () throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        String newFakeScript = "Faker Script";
        Problem.SrlBankProblem.Builder updateBankProblem = SrlBankProblem.newBuilder();
        updateBankProblem.setId(FAKE_ID);
        updateBankProblem.setScript(newFakeScript);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        boolean isUpdated = mongoUpdateBankProblem(authenticator, db, ADMIN_USER, problemBankId, updateBankProblem.build());
        Assert.assertEquals(true, isUpdated);
        final SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(newFakeScript, testString);
    }

    /*
     * testUpdateScript tests updateScript member function by inserting, updating,
     * and then getting the value that was inserted.
     */
    @Test(expected = AuthenticationException.class)
    public void testUpdateScriptFailsWithInvalidPermission() throws Exception {
        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId(FAKE_ID);
        bankProblem.setScript(FAKE_SCRIPT);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String problemBankId = mongoInsertBankProblem(db, bankProblem.build());

        String newFakeScript = "Faker Script";
        Problem.SrlBankProblem.Builder updateBankProblem = SrlBankProblem.newBuilder();
        updateBankProblem.setId(FAKE_ID);
        updateBankProblem.setScript(newFakeScript);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM, problemBankId, ADMIN_USER, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        boolean isUpdated = mongoUpdateBankProblem(authenticator, db, ADMIN_USER, problemBankId, updateBankProblem.build());
        Assert.assertEquals(true, isUpdated);
        final SrlBankProblem getProblem = mongoGetBankProblem(authenticator, db, ADMIN_USER, problemBankId);
        String testString = getProblem.getScript().toString();
        Assert.assertEquals(newFakeScript, testString);
    }
}
