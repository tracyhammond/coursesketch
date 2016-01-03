package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.RequestConverter;
import database.UserUpdateHandler;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.State;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.SrlPermission;
import utilities.LoggingConstants;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.ADMIN_GROUP_ID;
import static database.DatabaseStringConstants.ASSIGNMENT_LIST;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_ACCESS;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_SEMESTER;
import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.IMAGE;
import static database.DatabaseStringConstants.LECTURE_LIST;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.MOD_GROUP_ID;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PERMISSION_LEVELS;
import static database.DatabaseStringConstants.REGISTRATION_KEY;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.USERS;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_GROUP_ID;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Interfaces with the database to manage course data.
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
     * @param dbs
     *         The database where the assignment is being stored.
     * @param course
     *         The data of the course that is being inserted.
     * @return The id of the course that was inserted.
     */
    static String mongoInsertCourse(final DB dbs, final SrlCourse course) {
        final DBCollection courseCollection = dbs.getCollection(COURSE_COLLECTION);

        final BasicDBObject query = new BasicDBObject(DESCRIPTION, course.getDescription()).append(NAME, course.getName())
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
        courseCollection.insert(query);
        return query.get(SELF_ID).toString();
    }

    /**
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param authId
     *         the user requesting the course.
     * @param courseId
     *         the id of what course is being grabbed.
     * @param checkTime
     *         the time at which the course was requested.
     * @return The course if all of the checks pass.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException
     *         Thrown if there are problems retrieving the course.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity" })
    static SrlCourse mongoGetCourse(final Authenticator authenticator, final DB dbs, final String authId, final String courseId,
            final long checkTime)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, convertStringToObjectId(courseId));
        final DBObject cursor = myDbRef.fetch();
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
                .checkAuthentication(School.ItemType.COURSE, courseId.trim(), authId, checkTime, authType);

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
                exactCourse.setAccess(SrlCourse.Accessibility.valueOf((Integer) cursor.get(COURSE_ACCESS))); // admin
            } catch (ClassCastException exception) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, exception);
            }
        }
        return exactCourse.build();

    }

    /**
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param authId
     *         The id of the user that is updating the course.  Used to check permissions.
     * @param courseId
     *         The id of the course being updated.
     * @param course
     *         the course data that is being updated.
     * @return true if the update is successful.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to update the course.
     * @throws DatabaseAccessException
     *         Thrown if there are problems updating the course.
     */
    @SuppressWarnings("PMD.NPathComplexity")
    static boolean mongoUpdateCourse(final Authenticator authenticator, final DB dbs, final String authId, final String courseId,
            final SrlCourse course) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, convertStringToObjectId(courseId));
        final DBObject cursor = myDbRef.fetch();

        if (cursor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID: " + courseId);
        }

        final BasicDBObject updateObj = new BasicDBObject();
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE, courseId.trim(), authId, 0, authType);

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
            courses.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
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
     * @param dbs
     *         The database where the assignment is being stored.
     * @param courseId
     *         the course into which the assignment is being inserted into
     * @param assignmentId
     *         the assignment that is being inserted into the course.
     * @return true if the assignment was inserted correctly.
     * @throws AuthenticationException
     *         The user does not have permission to update the assignment.
     * @throws DatabaseAccessException
     *         The assignment does not exist.
     */
    static boolean mongoInsertAssignmentIntoCourse(final DB dbs, final String courseId, final String assignmentId)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, convertStringToObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
        updateObj = new BasicDBObject(ASSIGNMENT_LIST, assignmentId);
        courses.update(corsor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) corsor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        return true;

    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     * <p/>
     * With that being said this allows a course to be updated adding the
     * lectureId to its list of items.
     *
     * @param dbs
     *         The database where the assignment is being stored.
     * @param courseId
     *         the course into which the assignment is being inserted into
     * @param lectureId
     *         the assignment that is being inserted into the course.
     * @return true if the assignment was inserted correctly.
     * @throws AuthenticationException
     *         The user does not have permission to update the course.
     * @throws DatabaseAccessException
     *         The course does not exist.
     */
    static boolean mongoInsertLectureIntoCourse(final DB dbs, final String courseId, final String lectureId)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, convertStringToObjectId(courseId));
        final DBObject cursor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
        updateObj = new BasicDBObject(LECTURE_LIST, lectureId);
        courses.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        return true;

    }

    /**
     * @param dbs
     *         The database where the course is being stored.
     * @return a list of all public courses.
     * <p/>
     * FUTURE: this should probably be paginated so it does not crush
     * the database.
     */
    public static List<SrlCourse> mongoGetAllPublicCourses(final DB dbs) {
        final DBCollection courseTable = dbs.getCollection(COURSE_COLLECTION);

        final List<SrlCourse> resultList = new ArrayList<SrlCourse>();

        // checks for all public courses.
        final DBObject publicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.PUBLIC.getNumber());
        buildCourseForSearching(courseTable.find(publicCheck), resultList);

        // checks for all super public courses.
        final DBObject superPublicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.SUPER_PUBLIC.getNumber());
        buildCourseForSearching(courseTable.find(superPublicCheck), resultList);

        LOG.debug("Found {} courses in the current search", resultList.size());
        return resultList;
    }

    /**
     * @param cursor
     *         The pointer to the database object
     * @param resultList
     *         The list that the results are added to.  This list is modified by this method.
     */
    private static void buildCourseForSearching(final DBCursor cursor, final List<SrlCourse> resultList) {
        while (cursor.hasNext()) {
            final SrlCourse.Builder build = SrlCourse.newBuilder();
            final DBObject foundCourse = cursor.next();
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
     * NOTE: This is meant for internal use do not make this method public.
     * <p/>
     * With that being said this allows the default ids to be inserted.
     *
     * @param dbs
     *         The database where the course is being stored.
     * @param courseId
     *         the course that inserts the default id.
     * @param userGroupId
     *         the group id that is being inserted for users.
     * @param modGroupId
     *         the group id that is being inserted for moderators.
     * @param adminGroupId
     *         the group id that is being inserted for admins.
     */
    static void mongoInsertDefaultGroupId(final DB dbs, final String courseId, final String userGroupId, final String modGroupId,
            final String adminGroupId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
        final BasicDBObject listQueries = new BasicDBObject(ADMIN_GROUP_ID, adminGroupId).append(MOD_GROUP_ID, modGroupId).append(USER_GROUP_ID,
                userGroupId);
        final DBObject courseQuery = new BasicDBObject(SET_COMMAND, listQueries);
        courses.update(corsor, courseQuery);
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     * <p/>
     * Returns a list of Id for the default group for an assignment.
     * <p/>
     * The list are ordered as so: AdminGroup, ModGroup, UserGroup
     *
     * @param dbs
     *         The database where the course is being stored.
     * @param courseId
     *         the course that the groups are being grabbed from.
     * @return a list of usergroups.
     */
    static List<String>[] mongoGetDefaultGroupList(final DB dbs, final String courseId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final ArrayList<String>[] returnValue = new ArrayList[PERMISSION_LEVELS];
        returnValue[0] = (ArrayList) corsor.get(ADMIN);
        returnValue[1] = (ArrayList) corsor.get(MOD);
        returnValue[2] = (ArrayList) corsor.get(USERS);
        return returnValue;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     * <p/>
     * Returns a list of Ids for the default group for a course.
     * <p/>
     * The Ids are ordered as so: AdminGroup, ModGroup, UserGroup
     *
     * @param dbs
     *         The database where the course is being stored.
     * @param courseId
     *         the course whose user group is being requested.
     * @return a list of user group ids.
     */
    static String[] mongoGetDefaultGroupId(final DB dbs, final String courseId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final String[] returnValue = new String[PERMISSION_LEVELS];
        returnValue[0] = corsor.get(ADMIN_GROUP_ID).toString();
        returnValue[1] = corsor.get(MOD_GROUP_ID).toString();
        returnValue[2] = corsor.get(USER_GROUP_ID).toString();
        return returnValue;
    }

    /**
     * Returns the registration key of the given course if the constraints are met, null is returned in all other cases.
     *
     * @param authenticator
     *         Used to ensure the user has access to the registration key.
     * @param database
     *         The database that contains the registration key.
     * @param authId
     *         The id of the user that is updating the course.  Used to check permissions.
     * @param courseId
     *         The id of the course that contains the registration key.
     * @param checkTeacher
     *         True if the fact that the user is an admin needs to be checked.  Otherwise it is not checked.
     * @return The registration key of the given course if the constraints are met, null is returned in all other cases.
     * @throws AuthenticationException
     *         Thrown if there are problems checking the users authentication.
     * @throws DatabaseAccessException
     *         Thrown if the course does not exist.
     */
    public static String mongoGetRegistrationKey(final Authenticator authenticator, final DB database, final String authId, final String courseId,
            final boolean checkTeacher)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(database, COURSE_COLLECTION, convertStringToObjectId(courseId));
        final DBObject cursor = myDbRef.fetch();
        if (cursor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .setCheckingAdmin(checkTeacher)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE, courseId.trim(), authId, 0, authType);

        if (responder.hasTeacherPermission() || (!responder.isRegistrationRequired() && responder.isItemPublished())) {
            return (String) cursor.get(DatabaseStringConstants.REGISTRATION_KEY);
        }
        return null;
    }
}
