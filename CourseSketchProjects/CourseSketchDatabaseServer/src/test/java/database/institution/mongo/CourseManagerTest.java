package database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.DatabaseHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import database.DatabaseAccessException;
import database.auth.AuthenticationChecker;
import database.auth.AuthenticationDataCreator;
import database.auth.AuthenticationException;
import database.auth.AuthenticationOptionChecker;
import database.auth.Authenticator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Created by gigemjt on 3/22/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class CourseManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public DB db;
    public Authenticator authenticator;

    public static final long FAKE_VALID_DATE = 1000;
    public static final long FAKE_INVALID_DATE = 1001;
    public static final String FAKE_QUESTION_TEXT = "Question Texts";
    public static final String ADMIN_USER = "adminUser";
    public static final String USER_USER = "userUser";
    public static final Util.QuestionType FAKE_QUESTION_TYPE = Util.QuestionType.FREE_RESP;

    private String courseId;

    private School.SrlCourse.Builder defaultCourse;
    private AuthenticationDataCreator dataCreator;

    // TODO TESTS
    // To be done in second refactor
    // test that ensures that when inserting course problem permissions are copied over correctly
    //          this can be done by having someone who only has permission to a certain part and check that now new permissions are added.

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
        courseId = null;

        defaultCourse = School.SrlCourse.newBuilder();
        defaultCourse.setId("ID");
    }

    // INSERTION TESTS

    @Test
    public void insertCourseIntoCourseWithValidPermission() throws Exception {
        CourseManager.mongoInsertCourse(db, defaultCourse.build());
    }

    // GETTING TEST

    // Precondition tests
    @Test(expected = DatabaseAccessException.class)
    public void getCourseWithInvalidObjectId() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        CourseManager.mongoGetCourse(authenticator, db,
                DatabaseHelper.createNonExistentObjectId(courseId), ADMIN_USER, FAKE_VALID_DATE);
    }

    // Student grabbing test
    @Test(expected = DatabaseAccessException.class)
    public void getCourseThatDoesNotExistWithMalformedObjectId() throws Exception {
        CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, 0);
    }

    @Test(expected = AuthenticationException.class)
    public void getCourseWithInvalidPermission() throws Exception {
        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, 0);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseWithStudentPermissionButNotPublished() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, FAKE_VALID_DATE);
    }

    @Test
    public void getCourseAsStudentWithValidDateAndPublished() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseThatIsOpenReturnsProblemList() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    School.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, true);
        AuthenticationHelper.setMockDate(optionChecker, dataCreator, School.ItemType.COURSE, courseId, FAKE_VALID_DATE, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseThatIsClosedReturnsNoProblemList() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        for (int i = 0; i < 5; i++) {
            // We do not need to save the ids because we expect an empty list.
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    School.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
        }

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, USER_USER,
                null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        dataCreator = AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, courseId, USER_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    // Teacher grabbing permissions

    @Test
    public void getCourseAsInstructorWithWrongDateAndNotPublishedShouldWork() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse problem = CourseManager.mongoGetCourse(authenticator, db, courseId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder().setIsDeepEquals(false).build()
                .equals(defaultCourse.build(), problem);
    }

    @Test
    public void getCourseReturnsProblemListWhenOpenAndPublishedAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    School.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, true);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, courseId, ADMIN_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, false);
    }


    @Test
    public void getCourseReturnsProblemListWhenNotPublishedAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    School.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }
        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, false);

        final School.SrlCourse srlCourse = CourseManager.mongoGetCourse(authenticator, db, courseId, ADMIN_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse);
    }

    @Test
    public void getCourseReturnsProblemListWhenNotOpenAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);
        defaultCourse.setId(courseId);

        for (int i = 0; i < 5; i++) {
            String assignmentId = AssignmentManager.mongoInsertAssignment(authenticator, db, ADMIN_USER,
                    School.SrlAssignment.newBuilder()
                            .setId("ID")
                            .setCourseId(courseId)
                            .build());
            defaultCourse.addAssignmentList(assignmentId);
        }

        AuthenticationHelper.setMockPublished(optionChecker, dataCreator, School.ItemType.COURSE, courseId, false);

        final School.SrlCourse srlCourse3 = CourseManager.mongoGetCourse(authenticator, db, courseId,
                ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), srlCourse3);
    }

    // UPDATING TESTS

    @Test(expected = DatabaseAccessException.class)
    public void updateCourseWithInvalidObjectId() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        CourseManager.mongoUpdateCourse(authenticator, db, DatabaseHelper.createNonExistentObjectId(courseId),
                ADMIN_USER, defaultCourse.build());
    }

    @Test
    public void updateCourseAsInstructor() throws Exception {

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse problem = CourseManager.mongoGetCourse(authenticator, db, courseId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), problem);

        School.SrlCourse updatedProblem = School.SrlCourse.newBuilder(defaultCourse.build())
                .setAccess(School.SrlCourse.Accessibility.SUPER_PUBLIC)
                .setName("New Name")
                .setDescription("New Description")
                .setSemester("Fall")
                .setImageUrl("IMAGEURL")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, courseId, ADMIN_USER, updatedProblem);

        School.SrlCourse updatedCourseResult = CourseManager.mongoGetCourse(authenticator, db,
                courseId, ADMIN_USER, FAKE_VALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .setFailAtFirstMisMatch(false)
                .build().equals(updatedProblem, updatedCourseResult);
    }

    @Test
    public void updateCourseDoesNotUpdateAssignmentList() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());
        defaultCourse.setId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE_PROBLEM, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        School.SrlCourse course = CourseManager.mongoGetCourse(authenticator, db, courseId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), course);

        School.SrlCourse updatedCourse = School.SrlCourse.newBuilder(defaultCourse.build())
                .addAssignmentList("NEW PROBLEM VALUE!")
                .addAssignmentList("NEW PROBLEM VALUE2")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, courseId, ADMIN_USER, updatedCourse);

        School.SrlCourse updatedCourseResult = CourseManager.mongoGetCourse(authenticator, db,
                courseId, ADMIN_USER, FAKE_INVALID_DATE);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(false)
                .build().equals(defaultCourse.build(), updatedCourseResult);
    }

    @Test(expected = AuthenticationException.class)
    public void updateCourseAsStudentFails() throws Exception {
        AuthenticationHelper.setMockPermissions(authChecker, School.ItemType.COURSE, courseId, ADMIN_USER,
                null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        courseId = CourseManager.mongoInsertCourse(db, defaultCourse.build());

        School.SrlCourse updatedCourse = School.SrlCourse.newBuilder(defaultCourse.build())
                .setName("New name!")
                .build();

        CourseManager.mongoUpdateCourse(authenticator, db, courseId, USER_USER, updatedCourse);
    }
}
