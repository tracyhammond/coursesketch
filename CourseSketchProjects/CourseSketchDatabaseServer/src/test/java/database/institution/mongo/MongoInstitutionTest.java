package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.sun.tools.javac.util.List;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.AuthenticationUpdater;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.DbSchoolUtility;
import database.user.UserClient;
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

import static database.DatabaseStringConstants.BASE_SKETCH;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.REGISTRATION_KEY;
import static database.DbSchoolUtility.getCollectionFromType;
import static database.utilities.MongoUtilities.convertStringToObjectId;
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

    public DB db;
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

    @Before
    public void before() {
        db = fongo.getDB();

        // used to make the user client use the mock database
        new UserClient(true, db);

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
        institution = new MongoInstitution(true, db, authenticator, authenticationUpdater, identityManager);

        defaultCourse = School.SrlCourse.newBuilder();
        defaultCourse.setId(FAKE_ID);
        defaultCourse.setAccess(School.SrlCourse.Accessibility.PRIVATE);
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
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        dataCreator = AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);
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
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // creating assignment
        defaultAssignment.setCourseId(courseId);

        assignmentId = institution.insertAssignment(null, TEACHER_AUTH_ID, defaultAssignment.build());
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
        defaultProblem.clearSubgroups();

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(bankProblemId)
                .setItemType(School.ItemType.BANK_PROBLEM));
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

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM));
        final DBObject mongoBankProblem = collection.findOne(convertStringToObjectId(problemBankId));

        Assert.assertTrue(mongoBankProblem.containsField(REGISTRATION_KEY));
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());

        String registrationKey = (String) mongoBankProblem.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(problemBankId), eq(School.ItemType.BANK_PROBLEM), (String)isNull(),
                eq(registrationKey));

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(problemBankId), eq(School.ItemType.BANK_PROBLEM),
                (String)isNull(), any(Authenticator.class));
    }

    @Test
    public void insertingCourseCreatesRegistrationKey() throws AuthenticationException, InvalidProtocolBufferException, DatabaseAccessException {

        defaultCourse.setAccess(School.SrlCourse.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);

        String courseId = institution.insertCourse(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultCourse.build());

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.COURSE));
        final DBObject mongoCourse = collection.findOne(convertStringToObjectId(courseId));

        Assert.assertTrue(mongoCourse.containsField(REGISTRATION_KEY));
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.COURSE_ACCESS), School.SrlCourse.Accessibility.PRIVATE_VALUE);

        String registrationKey = (String) mongoCourse.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseId), eq(School.ItemType.COURSE), (String)isNull(),
                eq(registrationKey));

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseId), eq(School.ItemType.COURSE),
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

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.ASSIGNMENT));
        final DBObject mongoAssignment = collection.findOne(convertStringToObjectId(assignmentId));

        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.DUE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoAssignment.get(DatabaseStringConstants.ASSIGNMENT_TYPE), VALID_ASSIGNMENT_TYPE_VALUE);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(assignmentId), eq(School.ItemType.ASSIGNMENT), eq(courseId),
                (String) isNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(assignmentId), eq(School.ItemType.ASSIGNMENT),
                eq(courseId), any(Authenticator.class));
    }

    @Test
    public void insertCourseProblemCreatesUserGroup() throws Exception {
        insertCourseAndAssignment();

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.setName(VALID_NAME);
        defaultProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(School.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultProblem.build());

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject mongoProblem = collection.findOne(convertStringToObjectId(courseProblemId));

        Assert.assertEquals(mongoProblem.get(DatabaseStringConstants.NAME), VALID_NAME);
        final Iterable<DBObject> list = (Iterable<DBObject>) mongoProblem.get(DatabaseStringConstants.PROBLEM_LIST);

        final DBObject dbObject = list.iterator().next();
        Assert.assertEquals(bankProblemId, dbObject.get(DatabaseStringConstants.ITEM_ID));

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(School.ItemType.COURSE_PROBLEM),
                eq(assignmentId),
                (String) isNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(School.ItemType.COURSE_PROBLEM),
                eq(assignmentId), any(Authenticator.class));
    }

    @Test
    public void insertCourseProblemInsertsBankProblemPermissions() throws Exception {
        insertCourseAndAssignment();

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                bankProblemId, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        defaultProblem.setName(VALID_NAME);
        defaultProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(School.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(TEACHER_USER_ID, TEACHER_AUTH_ID, defaultProblem.build());

        final DBCollection collection = db.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject mongoProblem = collection.findOne(convertStringToObjectId(courseProblemId));

        Assert.assertEquals(mongoProblem.get(DatabaseStringConstants.NAME), VALID_NAME);
        final Iterable<DBObject> list = (Iterable<DBObject>) mongoProblem.get(DatabaseStringConstants.PROBLEM_LIST);

        final DBObject dbObject = list.iterator().next();
        Assert.assertEquals(bankProblemId, dbObject.get(DatabaseStringConstants.ITEM_ID));

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(TEACHER_AUTH_ID), eq(courseProblemId), eq(School.ItemType.COURSE_PROBLEM),
                eq(assignmentId),
                (String) isNull());

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(bankProblemId), eq(School.ItemType.BANK_PROBLEM),
                (String) isNotNull());

        verify(identityManager, atLeastOnce()).createNewItem(eq(TEACHER_USER_ID), eq(TEACHER_AUTH_ID), eq(courseProblemId),
                eq(School.ItemType.COURSE_PROBLEM),
                eq(assignmentId), any(Authenticator.class));

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(School.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test
    public void registerCourseInBankProblemWorksWithRgistrationKey() throws Exception {
        institution.putCourseInBankProblem(TEACHER_AUTH_ID, courseId, bankProblemId, VALID_REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(bankProblemId), eq(School.ItemType.BANK_PROBLEM),
                eq(VALID_REGISTRATION_KEY));

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(School.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseInBankProblemThrowsExceptionWithInvalidBankId() throws Exception {
        institution.putCourseInBankProblem(TEACHER_AUTH_ID, courseId, bankProblemId, null);
    }

    @Test
    public void updateCourseProblemAsInstructorWorksWithValidBankId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(School.ItemType.BANK_PROBLEM)
                .setUnlocked(true)
                .setProblem(bankProblem));

        courseProblemId = institution.insertCourseProblem(null, TEACHER_AUTH_ID, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        // checking insertion
        Problem.SrlProblem problem = institution.getCourseProblem(TEACHER_AUTH_ID, Lists.newArrayList(courseProblemId)).get(0);
        new ProtobufComparisonBuilder().setFailAtFirstMisMatch(false).ignoreField(Problem.SrlProblem.getDescriptor().findFieldByName("subgroups"))
                .build().equals(defaultProblem.build(), problem);

        bankProblem = Problem.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);

        String newBankProblemId = institution.insertBankProblem(null, TEACHER_AUTH_ID, bankProblem.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.BANK_PROBLEM,
                newBankProblemId, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlProblem.Builder updatedProblem = Problem.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT");

        // Add bank problem information
        updatedProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(newBankProblemId)
                .setItemType(School.ItemType.BANK_PROBLEM));

        institution.updateCourseProblem(TEACHER_AUTH_ID, updatedProblem.build());

        verify(authenticationUpdater, atLeastOnce()).registerUser(eq(courseId), eq(newBankProblemId), eq(School.ItemType.BANK_PROBLEM),
                (String) isNotNull());

        verify(identityManager, atLeastOnce()).registerUserInItem(eq(courseId), eq(TEACHER_AUTH_ID), eq(bankProblemId),
                eq(School.ItemType.BANK_PROBLEM), any(Authenticator.class));
    }

    @Test(expected = DatabaseAccessException.class)
    public void updateCourseProblemAsInstructorFailsWithInvalidBankId() throws Exception {
        insertCourseAndAssignment();
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.ASSIGNMENT, assignmentId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseProblemId = institution.insertCourseProblem(null, TEACHER_AUTH_ID, defaultProblem.build());
        defaultProblem.setId(courseProblemId);

        // Add bank problem information
        defaultProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(bankProblem.getId())
                .setItemType(School.ItemType.BANK_PROBLEM)
                .setProblem(bankProblem));

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseProblemId, TEACHER_AUTH_ID,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        Problem.SrlProblem.Builder updatedProblem = Problem.SrlProblem.newBuilder(defaultProblem.build())
                .setGradeWeight("NEW GRADE WEIGHT")
                .clearSubgroups();

        // Add bank problem information
        updatedProblem.addSubgroups(Problem.SrlProblem.ProblemSlideHolder.newBuilder()
                .setId(DatabaseHelper.createNonExistentObjectId(bankProblemId))
                .setItemType(School.ItemType.BANK_PROBLEM));

        institution.updateCourseProblem(TEACHER_AUTH_ID, updatedProblem.build());
    }
}
