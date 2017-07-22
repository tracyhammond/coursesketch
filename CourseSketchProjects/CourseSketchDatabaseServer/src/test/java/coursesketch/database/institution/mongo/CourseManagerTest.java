package coursesketch.database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.Assignment;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.institution.mongo.MongoInstitutionTest.genericDatabaseMock;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static org.mockito.Matchers.any;

/**
 * Created by gigemjt on 3/22/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CourseManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;
    @Mock AuthenticationDataCreator dataCreator;

    public MongoDatabase db;
    public Authenticator authenticator;

    public static final String VALID_NAME = "Valid course name!";
    public static final String FAKE_DESCRIPTION = "DESCRIPTIONS YAY";
    public static final String FAKE_ID = "507f1f77bcf86cd799439011";
    public static final String VALID_REGISTRATION_KEY = "VALID KEY!";
    public static final long FAKE_VALID_DATE = 1000;
    public static final Util.DateTime FAKE_VALID_DATE_OBJECT = Util.DateTime.newBuilder().setMillisecond(FAKE_VALID_DATE).build();
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";

    private String courseId;

    private School.SrlCourse.Builder defaultCourse;

    // TODO TESTS
    // To be done in second refactor
    // test that ensures that when inserting course problem permissions are copied over correctly
    //          this can be done by having someone who only has permission to a certain part and check that now new permissions are added.

    @Before
    public void before() {
        db = fongo.getDatabase();

        genericDatabaseMock(authChecker, optionChecker);
        authenticator = new Authenticator(authChecker, optionChecker);
        courseId = null;
        courseId = null;

        defaultCourse = School.SrlCourse.newBuilder();
        defaultCourse.setId(FAKE_ID);
    }

    // INSERTION TESTS

    @Test
    public void insertCourseIntoCourseWithValidPermission() throws Exception {
        CourseManager.mongoInsertCourse(db, defaultCourse.build());
    }

    @Test
    public void insertCourseIntoCourseWithValidPermissionAndAllValuesAreSetCorrectly() throws Exception {
        defaultCourse.setRegistrationKey(VALID_REGISTRATION_KEY);
        defaultCourse.setAccess(Util.Accessibility.PRIVATE);
        defaultCourse.setDescription(FAKE_DESCRIPTION);
        defaultCourse.setAccessDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setCloseDate(FAKE_VALID_DATE_OBJECT);
        defaultCourse.setName(VALID_NAME);
        String courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        final MongoCollection<Document> courseCollection = db.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document mongoCourse = courseCollection.find(convertStringToObjectId(courseId)).first();


        Assert.assertEquals(mongoCourse.get(REGISTRATION_KEY), VALID_REGISTRATION_KEY);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.NAME), VALID_NAME);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.DESCRIPTION), FAKE_DESCRIPTION);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.ACCESS_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.CLOSE_DATE), FAKE_VALID_DATE);
        Assert.assertEquals(mongoCourse.get(DatabaseStringConstants.COURSE_ACCESS), Util.Accessibility.PRIVATE_VALUE);
    }

    // GETTING TEST

    // Precondition tests
    @Test(expected = DatabaseAccessException.class)
    public void getCourseWithInvalidObjectId() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        CourseManager.mongoGetCourse(authenticator, db,
                ADMIN_USER, DatabaseHelper.createNonExistentObjectId(courseId), FAKE_VALID_DATE);
    }

    // Student grabbing test
    @Test(expected = DatabaseAccessException.class)
    public void getCourseThatDoesNotExistWithMalformedObjectId() throws Exception {
        CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getCourseWithInvalidPermission() throws Exception {
        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, 0);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseWithStudentPermissionButNotPublished() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, FAKE_VALID_DATE);
    }

    @Test
    public void getCourseAsStudentWithValidDateAndPublished() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseThatIsOpenReturnsProblemList() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    Assignment.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, true);
        AuthenticationHelper.setMockDate(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseThatIsClosedReturnsNoProblemList() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            // We do not need to save the ids because we expect an empty list.
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    Assignment.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
        }

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, USER_USER, courseId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    // Teacher grabbing permissions

    @Test
    public void getCourseAsInstructorWithWrongDateAndNotPublishedShouldWork() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse problem = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultCourse.build(), problem);
    }

    @Test
    public void getCourseReturnsProblemListWhenOpenAndPublishedAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    Assignment.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, false);
    }


    @Test
    public void getCourseReturnsProblemListWhenNotPublishedAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    Assignment.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }
        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, false);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseReturnsProblemListWhenNotOpenAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    Assignment.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE, courseId, false);

        final School.SrlCourse srlCourse3 = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId,
                FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse3);
    }

    // UPDATING TESTS

    @Test(expected = DatabaseAccessException.class)
    public void updateCourseWithInvalidObjectId() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        CourseManager.mongoUpdateCourse(authenticator, db, ADMIN_USER, DatabaseHelper.createNonExistentObjectId(courseId),
                defaultCourse.build());
    }

    @Test
    public void updateCourseAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse problem = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), problem);

        School.SrlCourse updatedProblem = School.SrlCourse.newBuilder(defaultCourse.build())
                .setAccess(Util.Accessibility.SUPER_PUBLIC)
                .setName("New Name")
                .setDescription("New Description")
                .setSemester("Fall")
                .setImageUrl("IMAGEURL")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, ADMIN_USER, courseId, updatedProblem);

        School.SrlCourse updatedCourseResult = CourseManager.mongoGetCourse(authenticator, db,
                ADMIN_USER, courseId, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedCourseResult);
    }

    @Test
    public void updateCourseDoesNotUpdateAssignmentList() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE_PROBLEM, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse course = CourseManager.mongoGetCourse(authenticator, db, ADMIN_USER, courseId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), course);

        School.SrlCourse updatedCourse = School.SrlCourse.newBuilder(defaultCourse.build())
                .addAssignmentList("NEW PROBLEM VALUE!")
                .addAssignmentList("NEW PROBLEM VALUE2")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, ADMIN_USER, courseId, updatedCourse);

        School.SrlCourse updatedCourseResult = CourseManager.mongoGetCourse(authenticator, db,
                ADMIN_USER, courseId, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), updatedCourseResult);
    }

    @Test(expected = AuthenticationException.class)
    public void updateCourseAsStudentFails() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        School.SrlCourse updatedCourse = School.SrlCourse.newBuilder(defaultCourse.build())
                .setName("New name!")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, USER_USER, courseId, updatedCourse);
    }


    @Test
    public void getCourseRegistrationKeyNoAccessShouldBeNull() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, true);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getCourseRegistrationKeyNoAccessNotCheckingAdminShouldBeNull() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, false);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getCourseRegistrationKeyNoAccessWithNoRegistrationNotPublishedShouldBeNull() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, null, Util.ItemType.COURSE,
                courseId, false);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, true);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getCourseRegistrationKeyNoAccessWithRegistrationPublishedShouldBeNull() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, null, Util.ItemType.COURSE,
                courseId, true);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE,
                courseId, true);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, true);

        Assert.assertEquals(null, key);
    }

    @Test
    public void getCourseRegistrationKeyNoAccessWithNoRegistrationIsPublishedShouldBeKey() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE,
                courseId, true);

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, dataCreator, Util.ItemType.COURSE,
                courseId, false);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, true);

        Assert.assertEquals(VALID_REGISTRATION_KEY, key);
    }

    @Test
    public void getCourseRegistrationKeyNoAccessWithNoRegistrationIsPublishedNoAdminCheckingShouldBeKey() throws Exception {
        School.SrlCourse.Builder course = defaultCourse;
        course.setRegistrationKey(VALID_REGISTRATION_KEY);

        String courseId = CourseManager.mongoInsertCourse(db, course.build());

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, Util.ItemType.COURSE,
                courseId, true);

        AuthenticationHelper.setMockRegistrationRequired(optionChecker, dataCreator, Util.ItemType.COURSE,
                courseId, false);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE,
                courseId, ADMIN_USER, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        String key = CourseManager.mongoGetRegistrationKey(authenticator, db, USER_USER, courseId, false);

        Assert.assertEquals(VALID_REGISTRATION_KEY, key);
    }
}
