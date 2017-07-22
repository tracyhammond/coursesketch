package coursesketch.database.util.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.AuthenticationUpdater;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.user.UserClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.commands.Commands;
import protobuf.srl.school.Assignment;
import protobuf.srl.school.Problem;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import static coursesketch.database.util.DatabaseStringConstants.COURSE_TOPIC;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TEXT;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.utilities.MongoUtilities.convertStringToObjectId;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 10/26/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoInstitutionTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;
    @Mock AuthenticationDataCreator dataCreator;
    @Mock AuthenticationUpdater authenticationUpdater;
    @Mock IdentityManagerInterface identityManager;

    public MongoDatabase db;
    public Authenticator authenticator;
    public MongoInstitution institution;

    public static final String VALID_REGISTRATION_KEY = "VALID KEY!";
    public static final String VALID_NAME = "Valid course name!";
    public static final String FAKE_DESCRIPTION = "DESCRIPTIONS YAY";
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String FAKE_SCRIPT = "fake script";
    public static final String TEACHER_AUTH_ID = new ObjectId().toHexString();
    public static final String STUDENT_AUTH_ID = new ObjectId().toHexString();
    public static final String MOD_AUTH_ID = new ObjectId().toHexString();
    public static final String TEACHER_USER_ID = new ObjectId().toHexString();
    public static final String STUDENT_USER_ID = new ObjectId().toHexString();
    public static final String MOD_USER_ID = new ObjectId().toHexString();

    public static final long FAKE_VALID_DATE = 1000;
    public static final Util.DateTime FAKE_VALID_DATE_OBJECT = Util.DateTime.newBuilder().setMillisecond(FAKE_VALID_DATE).build();
    public static final long FAKE_INVALID_DATE = 1001;

    public static final Commands.SrlUpdateList.Builder FAKE_UPDATELIST = Commands.SrlUpdateList.newBuilder();
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;
    public static final Assignment.AssignmentType VALID_ASSIGNMENT_TYPE = Assignment.AssignmentType.GRADED;
    public static final int VALID_ASSIGNMENT_TYPE_VALUE = Assignment.AssignmentType.GRADED_VALUE;

    private String courseId;
    private String assignmentId;
    private String courseProblemId;
    private String bankProblemId;

    private School.SrlCourse.Builder defaultCourse;
    private Assignment.SrlAssignment.Builder defaultAssignment;
    private Problem.SrlProblem.Builder defaultProblem;
    private Problem.SrlBankProblem.Builder bankProblem;

    public static void genericDatabaseMock(AuthenticationChecker authChecker, AuthenticationOptionChecker optionChecker) {
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
    }

    @Before
    public void before() {
        db = fongo.getDatabase();

        // used to make the user client use the mock database
        new UserClient(true, db);

        genericDatabaseMock(authChecker, optionChecker);
        authenticator = new Authenticator(authChecker, optionChecker);
        institution = new MongoInstitution(true, db, authenticator, authenticationUpdater, identityManager);

        defaultCourse = School.SrlCourse.newBuilder();
        defaultCourse.setId(FAKE_ID);
        defaultCourse.setAccess(Util.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);

        defaultAssignment = Assignment.SrlAssignment.newBuilder();
        defaultAssignment.setId(FAKE_ID);

        defaultProblem = Problem.SrlProblem.newBuilder();
        defaultProblem.setId(FAKE_ID);

        courseId = null;
        assignmentId = null;
        courseProblemId = null;
        bankProblemId = null;
    }

    public void insertCourse() throws DatabaseAccessException, AuthenticationException {

        // creating the course
        final School.SrlCourse.Builder course = School.SrlCourse.newBuilder();
        course.setId("ID");
        courseId = CourseManager.mongoInsertCourse(db, course.build());
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
    }

    public void insertCourseAndAssignment() throws DatabaseAccessException, AuthenticationException {

        // creating bank problem
        bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        bankProblemId = institution.insertBankProblem(null, TEACHER_AUTH_ID, bankProblem.build());
        bankProblem.setId(bankProblemId);

        // creating the course
        courseId = institution.insertCourse(null, TEACHER_AUTH_ID, defaultCourse.build());
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        assignmentId = institution.insertAssignment(null, TEACHER_AUTH_ID, defaultAssignment.build());
        updateProblemIds(courseId, assignmentId, bankProblemId);

        // sets the course able to use the bank problem
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.BANK_PROBLEM, bankProblemId, courseId,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.ASSIGNMENT, assignmentId, FAKE_VALID_DATE, true);
        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
    }

    public void updateProblemIds(String courseId, String assignmentId, String bankProblemId) {
        defaultProblem.setCourseId(courseId);
        defaultProblem.setAssignmentId(assignmentId);
        defaultProblem.clearSubgroups();

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(bankProblemId)
                .setItemType(Util.ItemType.BANK_PROBLEM));
    }

    @Test
    public void insertingBankProblemCreatesRegistrationKey() throws AuthenticationException, InvalidProtocolBufferException, DatabaseAccessException {

        Problem.SrlBankProblem.Builder bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);

        String problemBankId = institution.insertBankProblem(TEACHER_USER_ID, TEACHER_AUTH_ID, bankProblem.build());

        final MongoCollection<Document> bankProblemCollection = db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final Document mongoBankProblem = bankProblemCollection.find(convertStringToObjectId(problemBankId)).first();

        Assert.assertTrue(mongoBankProblem.containsKey(REGISTRATION_KEY));
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());

        String registrationKey = (String) mongoBankProblem.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(problemBankId), eq(Util.ItemType.BANK_PROBLEM), (String)isNull(),
                eq(registrationKey));

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(problemBankId), eq(Util.ItemType.BANK_PROBLEM),
                (String)isNull(), any(Authenticator.class));
    }

    @Test
    public void insertingCourseCreatesRegistrationKey() throws AuthenticationException, InvalidProtocolBufferException, DatabaseAccessException {

        defaultCourse.setAccess(Util.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);

        String courseId = institution.insertCourse(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultCourse.build());

        final MongoCollection<Document> courseCollection = db.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document mongoCourse = courseCollection.find(convertStringToObjectId(courseId)).first();

        Assert.assertTrue(mongoCourse.containsKey(REGISTRATION_KEY));
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.COURSE_ACCESS), Util.Accessibility.PRIVATE_VALUE);

        String registrationKey = (String) mongoCourse.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseId), eq(Util.ItemType.COURSE), (String)isNull(),
                eq(registrationKey));

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseId), eq(Util.ItemType.COURSE),
                (String)isNull(), any(Authenticator.class));
    }


    @Test
    public void insertingAssignmentAlsoCreatesUserGroup() throws Exception {
        insertCourse();

        defaultAssignment.setDescription(FAKE_DESCRIPTION);
        defaultAssignment.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setDueDate(FAKE_VALID_DATE_OBJECT);
        defaultAssignment.setName(VALID_NAME);
        defaultAssignment.setAssignmentType(VALID_ASSIGNMENT_TYPE);

        assignmentId = institution.insertAssignment(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultAssignment.build());

        final MongoCollection<Document> assignmentCollection = db.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document mongoAssignment = assignmentCollection.find(convertStringToObjectId(assignmentId)).first();

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DUE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_TYPE), VALID_ASSIGNMENT_TYPE_VALUE);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(assignmentId), eq(Util.ItemType.ASSIGNMENT), eq(courseId),
                (String) isNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(assignmentId), eq(Util.ItemType.ASSIGNMENT),
                eq(courseId), any(Authenticator.class));
    }

    @Test
    public void insertCourseProblemCreatesUserGroup() throws Exception {
        insertCourseAndAssignment();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.setName(VALID_NAME);
        defaultProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(Util.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultProblem.build());

        final MongoCollection<Document> courseProblemCollection = db.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final Document mongoProblem = courseProblemCollection.find(convertStringToObjectId(courseProblemId)).first();

        Assert.assertEquals(mongoProblem.get(DatabaseStringConstants.NAME), VALID_NAME);
        final Iterable<Document> list = (Iterable<Document>) mongoProblem.get(DatabaseStringConstants.PROBLEM_LIST);

        final Document Document = list.iterator().next();
        Assert.assertEquals(bankProblemId, Document.get(DatabaseStringConstants.ITEM_ID));

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(Util.ItemType.COURSE_PROBLEM),
                eq(assignmentId),
                (String) isNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(Util.ItemType.COURSE_PROBLEM),
                eq(assignmentId), any(Authenticator.class));
    }

    @Test
    public void insertCourseProblemInsertsBankProblemPermissions() throws Exception {
        insertCourseAndAssignment();

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.BANK_PROBLEM,
                bankProblemId, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.setName(VALID_NAME);
        defaultProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(Util.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultProblem.build());

        final MongoCollection<Document> courseProblemCollection = db.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final Document mongoProblem = courseProblemCollection.find(convertStringToObjectId(courseProblemId)).first();

        Assert.assertEquals(mongoProblem.get(DatabaseStringConstants.NAME), VALID_NAME);
        final Iterable<Document> list = (Iterable<Document>) mongoProblem.get(DatabaseStringConstants.PROBLEM_LIST);

        final Document Document = list.iterator().next();
        Assert.assertEquals(bankProblemId, Document.get(DatabaseStringConstants.ITEM_ID));

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(Util.ItemType.COURSE_PROBLEM),
                eq(assignmentId),
                (String) isNull());

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(bankProblemId), eq(Util.ItemType.BANK_PROBLEM),
                (String) isNotNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseProblemId),
                eq(Util.ItemType.COURSE_PROBLEM),
                eq(assignmentId), any(Authenticator.class));

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(Util.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test
    public void registerCourseInBankProblemWorksWithRgistrationKey() throws Exception {
        institution.putCourseInBankProblem(TEACHER_AUTH_ID, courseId, bankProblemId, VALID_REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(bankProblemId), eq(Util.ItemType.BANK_PROBLEM),
                eq(VALID_REGISTRATION_KEY));

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(Util.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseInBankProblemThrowsExceptionWithInvalidBankId() throws Exception {
        institution.putCourseInBankProblem(TEACHER_AUTH_ID, courseId, bankProblemId, null);
    }

    @Test
    public void updateCourseProblemAsInstructorWorksWithValidBankId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(Util.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(null, TEACHER_AUTH_ID, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, courseProblemId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // checking insertion
        Problem.SrlProblem problem = institution.getCourseProblem(TEACHER_AUTH_ID, Lists.newArrayList(courseProblemId)).get(0);
        new ProtobufComparisonBuilder().setFailAtFirstMisMatch(false).ignoreField(Problem.SrlProblem.getDescriptor().findFieldByName("subgroups"))
                .build().equals(defaultProblem.build(), problem);

        bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String newBankProblemId = institution.insertBankProblem(null, TEACHER_AUTH_ID, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.BANK_PROBLEM,
                newBankProblemId, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlProblem.Builder updatedProblem = Problem.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT");

        // Add bank problem information
        updatedProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(newBankProblemId)
                .setItemType(Util.ItemType.BANK_PROBLEM));

        institution.updateCourseProblem(TEACHER_AUTH_ID, updatedProblem.build());

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(newBankProblemId), eq(Util.ItemType.BANK_PROBLEM),
                (String) isNotNull());

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(Util.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test(expected = DatabaseAccessException.class)
    public void updateCourseProblemAsInstructorFailsWithInvalidBankId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = institution.insertCourseProblem(null, TEACHER_AUTH_ID, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(Util.ItemType.BANK_PROBLEM)
                .setProblem(bankProblem));

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, courseProblemId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlProblem.Builder updatedProblem = Problem.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .clearSubgroups();

        // Add bank problem information
        updatedProblem.addSubgroups(Problem.ProblemSlideHolder.newBuilder()
                .setId(DatabaseHelper.createNonExistentObjectId(bankProblemId))
                .setItemType(Util.ItemType.BANK_PROBLEM));

        institution.updateCourseProblem(TEACHER_AUTH_ID, updatedProblem.build());
    }
}
