package coursesketch.database.util.institution.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.RequestConverter;
import coursesketch.database.util.UserUpdateHandler;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;
import protobuf.srl.utils.Util.SrlPermission;
import protobuf.srl.utils.Util.State;
import utilities.LoggingConstants;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ACCESS_DATE;
import static coursesketch.database.util.DatabaseStringConstants.ADD_SET_COMMAND;
import static coursesketch.database.util.DatabaseStringConstants.ADMIN;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_LIST;
import static coursesketch.database.util.DatabaseStringConstants.CLOSE_DATE;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_ACCESS;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_SEMESTER;
import static coursesketch.database.util.DatabaseStringConstants.DESCRIPTION;
import static coursesketch.database.util.DatabaseStringConstants.IMAGE;
import static coursesketch.database.util.DatabaseStringConstants.LECTURE_LIST;
import static coursesketch.database.util.DatabaseStringConstants.MOD;
import static coursesketch.database.util.DatabaseStringConstants.NAME;
import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.SET_COMMAND;
import static coursesketch.database.util.DatabaseStringConstants.USERS;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Interfaces with the coursesketch.util.util to manage course data.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.UselessParentheses",
        "PMD.TooManyMethods" })
public final class CourseManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CourseManager.class);

    /**
     * Private constructor.
     */
    private CourseManager() {
    }

    /**
     * @param dbs The coursesketch.util.util where the assignment is being stored.
     * @param course The data of the course that is being inserted.
     * @return The id of the course that was inserted.
     */
    static String mongoInsertCourse(final MongoDatabase dbs, final SrlCourse course) {
        final MongoCollection<Document> courseCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE));

        final Document query = new Document(DESCRIPTION, course.getDescription()).append(NAME, course.getName())
                .append(COURSE_ACCESS, course.getAccess().getNumber()).append(COURSE_SEMESTER, course.getSemester())
                .append(ACCESS_DATE, course.getAccessDate().getMillisecond())
                .append(IMAGE, course.getImageUrl())
                .append(REGISTRATION_KEY, course.getRegistrationKey())
                .append(DatabaseStringConstants.STATE_PUBLISHED, true);

        // Sets a default date in the instance that a date was not given.
        if (!course.hasCloseDate()) {
            query.append(CLOSE_DATE, RequestConverter.getMaxTime());
        } else {
            query.append(CLOSE_DATE, course.getCloseDate().getMillisecond());
        }

        if (course.getAssignmentListList() != null) {
            query.append(ASSIGNMENT_LIST, course.getAssignmentListList());
        }
        courseCollection.insertOne(query);
        return query.get(SELF_ID).toString();
    }

    /**
     * @param authenticator the object that is performing authentication.
     * @param dbs The coursesketch.util.util where the assignment is being stored.
     * @param authId the user requesting the course.
     * @param courseId the id of what course is being grabbed.
     * @param checkTime the time at which the course was requested.
     * @return The course if all of the checks pass.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException Thrown if there are problems retrieving the course.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity" })
    static SrlCourse mongoGetCourse(final Authenticator authenticator, final MongoDatabase dbs, final String authId, final String courseId,
            final long checkTime)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> courseCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document cursor = courseCollection.find(convertStringToObjectId(courseId)).first();

        if (cursor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .setCheckDate(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId.trim(), authId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For course: " + courseId, AuthenticationException.INVALID_PERMISSION);
        }

        final SrlCourse.Builder exactCourse = SrlCourse.newBuilder();
        exactCourse.setDescription((String) cursor.get(DESCRIPTION));
        exactCourse.setName((String) cursor.get(NAME));
        if (cursor.get(COURSE_SEMESTER) != null) {
            exactCourse.setSemester((String) cursor.get(COURSE_SEMESTER));
        }

        exactCourse.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(ACCESS_DATE)).longValue()));
        exactCourse.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(CLOSE_DATE)).longValue()));
        exactCourse.setId(courseId);

        // states
        final State.Builder stateBuilder = State.newBuilder();
        if (exactCourse.getCloseDate().getMillisecond() > checkTime) {
            stateBuilder.setPastDue(true);
        }

        if (!responder.isItemPublished() && !responder.hasModeratorPermission()) {
            throw new DatabaseAccessException("The specific course is not published yet: " + courseId, true);
        }

        // Past this point, the item is either published or the responder is at least a mod.
        stateBuilder.setPublished(responder.isItemPublished());

        if (cursor.get(IMAGE) != null) {
            exactCourse.setImageUrl((String) cursor.get(IMAGE));
        }

        // if you are a user, the course must be open to view the assignments
        if ((responder.hasAccess() && responder.isItemOpen())
                || responder.hasPeerTeacherPermission()) {
            final Object assignmentList = cursor.get(ASSIGNMENT_LIST);
            final Object lectureList = cursor.get(LECTURE_LIST);
            if (assignmentList != null) {
                exactCourse.addAllAssignmentList((List) assignmentList);
            }
            if (lectureList != null) {
                exactCourse.addAllLectureList((List) lectureList);
            }
            stateBuilder.setAccessible(true);
        } else if (responder.hasAccess() && !responder.isItemOpen() && !responder.hasPeerTeacherPermission()) {
            LOG.info("USER CLASS TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            LOG.info("TIME OPEN: {} \n CURRENT TIME: {} \n TIME CLOSED: {} \n", exactCourse.getAccessDate().getMillisecond(), checkTime,
                    exactCourse.getCloseDate().getMillisecond());
            stateBuilder.setAccessible(false);
        }

        exactCourse.setState(stateBuilder);

        if (responder.hasTeacherPermission()) {
            try {
                exactCourse.setAccess(Util.Accessibility.valueOf((Integer) cursor.get(COURSE_ACCESS))); // admin
            } catch (ClassCastException exception) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, exception);
            }
        }
        return exactCourse.build();

    }

    /**
     * @param authenticator the object that is performing authentication.
     * @param dbs The coursesketch.util.util where the assignment is being stored.
     * @param authId The id of the user that is updating the course.  Used to check permissions.
     * @param courseId The id of the course being updated.
     * @param course the course data that is being updated.
     * @return true if the update is successful.
     * @throws AuthenticationException Thrown if the user did not have the authentication to update the course.
     * @throws DatabaseAccessException Thrown if there are problems updating the course.
     */
    @SuppressWarnings("PMD.NPathComplexity")
    static boolean mongoUpdateCourse(final Authenticator authenticator, final MongoDatabase dbs, final String authId, final String courseId,
            final SrlCourse course) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final MongoCollection<Document> courseCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document cursor = courseCollection.find(convertStringToObjectId(courseId)).first();

        if (cursor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID: " + courseId);
        }

        final Document updateObj = new Document();

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId.trim(), authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For course: " + courseId, AuthenticationException.INVALID_PERMISSION);
        }

        if (responder.hasTeacherPermission()) {
            if (course.hasSemester()) {
                updateObj.append(COURSE_SEMESTER, course.getSemester());
                update = true;
            }
            if (course.hasAccessDate()) {
                updateObj.append(ACCESS_DATE, course.getAccessDate().getMillisecond());
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (course.hasCloseDate()) {
                updateObj.append(CLOSE_DATE, course.getCloseDate().getMillisecond());
                update = true;
            }

            if (course.hasImageUrl()) {
                updateObj.append(IMAGE, course.getImageUrl());
                update = true;
            }
            if (course.hasDescription()) {
                updateObj.append(DESCRIPTION, course.getDescription());
                update = true;
            }
            if (course.hasName()) {
                updateObj.append(NAME, course.getName());
                update = true;
            }
            if (course.hasAccess()) {
                updateObj.append(COURSE_ACCESS, course.getAccess().getNumber());
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (course.hasAccessPermission()) {
                LOG.info("Updating permissions!");
                final SrlPermission permissions = course.getAccessPermission();
                if (permissions.getAdminPermissionList() != null) {
                    updateObj.append(ADMIN, permissions.getAdminPermissionList());
                }
                if (permissions.getModeratorPermissionList() != null) {
                    updateObj.append(MOD, permissions.getModeratorPermissionList());
                }
                if (permissions.getUserPermissionList() != null) {
                    updateObj.append(USERS, permissions.getUserPermissionList());
                }
            }
        }

        // get user list send updates
        if (update) {
            courseCollection.updateOne(cursor, new Document(SET_COMMAND, updateObj));
            UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        }
        return true;

    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     * <p/>
     * With that being said this allows a course to be updated adding the
     * assignmentId to its list of items.
     *
     * @param dbs The coursesketch.util.util where the assignment is being stored.
     * @param courseId the course into which the assignment is being inserted into
     * @param assignmentId the assignment that is being inserted into the course.
     * @return true if the assignment was inserted correctly.
     * @throws AuthenticationException The user does not have permission to update the assignment.
     * @throws DatabaseAccessException The assignment does not exist.
     */
    static boolean mongoInsertAssignmentIntoCourse(final MongoDatabase dbs, final String courseId, final String assignmentId)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> courseCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document cursor = courseCollection.find(convertStringToObjectId(courseId)).first();

        Document updateObj = null;
        updateObj = new Document(ASSIGNMENT_LIST, assignmentId);
        courseCollection.updateOne(cursor, new Document(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        return true;
    }

    /**
     * @param dbs The coursesketch.util.util where the course is being stored.
     * @return a list of all public courses.
     * <p/>
     * FUTURE: this should probably be paginated so it does not crush
     * the coursesketch.util.util.
     */
    public static List<SrlCourse> mongoGetAllPublicCourses(final MongoDatabase dbs) {
        final MongoCollection<Document> courseTable = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE));

        final List<SrlCourse> resultList = new ArrayList<SrlCourse>();

        // checks for all public courses.
        final Document publicCheck = new Document(COURSE_ACCESS, Util.Accessibility.PUBLIC.getNumber());
        buildCourseForSearching(courseTable.find(publicCheck).iterator(), resultList);

        // checks for all super public courses.
        final Document superPublicCheck = new Document(COURSE_ACCESS, Util.Accessibility.SUPER_PUBLIC.getNumber());
        buildCourseForSearching(courseTable.find(superPublicCheck).iterator(), resultList);

        LOG.debug("Found {} courses in the current search", resultList.size());
        return resultList;
    }

    /**
     * @param cursor The pointer to the coursesketch.util.util object
     * @param resultList The list that the results are added to.  This list is modified by this method.
     */
    private static void buildCourseForSearching(final MongoCursor<Document> cursor, final List<SrlCourse> resultList) {
        while (cursor.hasNext()) {
            final SrlCourse.Builder build = SrlCourse.newBuilder();
            final Document foundCourse = cursor.next();
            build.setId(foundCourse.get(SELF_ID).toString());
            build.setDescription(foundCourse.get(DESCRIPTION).toString());
            build.setName(foundCourse.get(NAME).toString());
            build.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
            build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
            resultList.add(build.build());
        }
        cursor.close();
    }

    /**
     * Returns the registration key of the given course if the constraints are met, null is returned in all other cases.
     *
     * @param authenticator Used to ensure the user has access to the registration key.
     * @param database The coursesketch.util.util that contains the registration key.
     * @param authId The id of the user that is updating the course.  Used to check permissions.
     * @param courseId The id of the course that contains the registration key.
     * @param checkTeacher True if the fact that the user is an admin needs to be checked.  Otherwise it is not checked.
     * @return The registration key of the given course if the constraints are met, null is returned in all other cases.
     * @throws AuthenticationException Thrown if there are problems checking the users authentication.
     * @throws DatabaseAccessException Thrown if the course does not exist.
     */
    public static String mongoGetRegistrationKey(final Authenticator authenticator, final MongoDatabase database, final String authId,
            final String courseId,
            final boolean checkTeacher)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> courseCollection = database.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        final Document cursor = courseCollection.find(convertStringToObjectId(courseId)).first();
        if (cursor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .setCheckingAdmin(checkTeacher)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId.trim(), authId, 0, authType);

        if (responder.hasTeacherPermission() || (!responder.isRegistrationRequired() && responder.isItemPublished())) {
            return (String) cursor.get(DatabaseStringConstants.REGISTRATION_KEY);
        }
        return null;
    }
}
