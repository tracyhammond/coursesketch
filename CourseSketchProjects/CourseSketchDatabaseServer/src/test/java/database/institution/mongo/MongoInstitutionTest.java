package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.github.fakemongo.junit.FongoRule;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.AuthenticationUpdater;
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
import protobuf.srl.commands.Commands;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.List;

import static database.DatabaseStringConstants.BASE_SKETCH;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.REGISTRATION_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 10/26/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoInstitutionTest {



    private static final String VALID_REGISTRATION_KEY = "VALID KEY!";
    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;
    @Mock AuthenticationDataCreator creator;
    @Mock AuthenticationUpdater authenticationUpdater;

    public DB db;
    public Authenticator authenticator;
    public MongoInstitution institution;
    public static final String VALID_NAME = "Valid course name!";
    public static final String FAKE_DESCRIPTION = "DESCRIPTIONS YAY";
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final long FAKE_VALID_DATE = 1000;
    public static final Util.DateTime FAKE_VALID_DATE_OBJECT = Util.DateTime.newBuilder().setMillisecond(FAKE_VALID_DATE).build();
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String FAKE_SCRIPT = "fake script";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Commands.SrlUpdateList.Builder FAKE_UPDATELIST = Commands.SrlUpdateList.newBuilder();
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    private School.SrlCourse.Builder defaultCourse;

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
        institution = new MongoInstitution(true, db, authenticator, authenticationUpdater);

        defaultCourse = School.SrlCourse.newBuilder();
        defaultCourse.setId(FAKE_ID);
        defaultCourse.setAccess(School.SrlCourse.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);
    }

    @Test
    public void insertingBankProblemCreatesRegistrationKey() throws AuthenticationException, InvalidProtocolBufferException {

        School.SrlBankProblem.Builder bankProblem = School.SrlBankProblem.newBuilder();
        bankProblem.setId("NOT REAL ID");
        bankProblem.setRegistrationKey(VALID_REGISTRATION_KEY);
        bankProblem.setQuestionText(FAKE_QUESTION_TEXT);
        bankProblem.setCourseTopic(FAKE_QUESTION_TEXT);
        bankProblem.setQuestionType(FAKE_QUESTION_TYPE);
        bankProblem.setBaseSketch(FAKE_UPDATELIST.build());

        String problemBankId = institution.insertBankProblem(ADMIN_USER, bankProblem.build());

        final DBRef myDbRef = new DBRef(db, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertTrue(mongoBankProblem.containsField(REGISTRATION_KEY));
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TEXT), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(COURSE_TOPIC), FAKE_QUESTION_TEXT);
        Assert.assertEquals(mongoBankProblem.get(QUESTION_TYPE), FAKE_QUESTION_TYPE.getNumber());
        Assert.assertEquals(Commands.SrlUpdateList.parseFrom((byte[]) mongoBankProblem.get(BASE_SKETCH)),
                FAKE_UPDATELIST.build());

        String registrationKey = (String) mongoBankProblem.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(School.ItemType.BANK_PROBLEM), eq(problemBankId), (String)isNull(), eq(ADMIN_USER),
                eq(registrationKey));
    }


    @Test
    public void insertingCourseCreatesRegistrationKey() throws AuthenticationException, InvalidProtocolBufferException, DatabaseAccessException {

        defaultCourse.setAccess(School.SrlCourse.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);

        String courseId = institution.insertCourse(ADMIN_USER, defaultCourse.build());

        final DBRef myDbRef = new DBRef(db, DbSchoolUtility.getCollectionFromType(School.ItemType.COURSE, true), new ObjectId(courseId));
        final DBObject mongoCourse = myDbRef.fetch();

        Assert.assertTrue(mongoCourse.containsField(REGISTRATION_KEY));
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.COURSE_ACCESS), School.SrlCourse.Accessibility.PRIVATE_VALUE);

        String registrationKey = (String) mongoCourse.get(REGISTRATION_KEY);

        verify(authenticationUpdater, atLeastOnce()).createNewItem(eq(School.ItemType.COURSE), eq(courseId), (String)isNull(), eq(ADMIN_USER),
                eq(registrationKey));
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


        final DBRef myDbRef = new DBRef(db, DatabaseStringConstants.PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(DatabaseStringConstants.USERS)).get(0));
    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoBankId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setCourseId("Course id");


    }

    @Test(expected = DatabaseAccessException.class)
    public void registerCourseNoCourseId() throws Exception {
        School.SrlProblem.Builder problem = School.SrlProblem.newBuilder();
        problem.setId("ID");
        problem.setProblemBankId("Bank id");


    }


    /*
    @Test(expected = DatabaseAccessException.class)
    public void updateCourseProblemAsInstructorFailsWithInvalidBankId() throws Exception {
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
                .setProblemBankId(DatabaseHelper.createNonExistentObjectId(bankProblemId))
                .build();

        CourseProblemManager.mongoUpdateCourseProblem(authenticator, db, courseProblemId, ADMIN_USER, updatedProblem);

        School.SrlProblem updatedProblemResult = CourseProblemManager.mongoGetCourseProblem(authenticator, db,
                courseProblemId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedProblemResult);
    }

    // checks that the course is registered for the bank problem when a course problem is inserted.
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

        final DBRef myDbRef = new DBRef(db, DatabaseStringConstants.PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject mongoBankProblem = myDbRef.fetch();

        // TODO(dtracers): change what is being tested to better reflect what is being asked.
        Assert.assertEquals(courseId, ((List) mongoBankProblem.get(DatabaseStringConstants.USERS)).get(0));
    }
    */

}
