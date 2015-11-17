package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.AuthenticationResponder;
import database.auth.Authenticator;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.SrlPermission;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.IDS_IN_LECTURE;
import static database.DatabaseStringConstants.IS_SLIDE;
import static database.DatabaseStringConstants.IS_UNLOCKED;
import static database.DatabaseStringConstants.LECTURE_COLLECTION;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.SLIDES;
import static database.DatabaseStringConstants.USERS;

/**
 * Manages lectures for mongo.
 *
 * @author Devin Tuchsen
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.UselessParentheses" })
public final class LectureManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LectureManager.class);

    /**
     * Private constructor.
     */
    private LectureManager() {
    }

    /**
     * Inserts a lecture into the mongo database.
     *
     * @param authenticator
     *         the object that is performing authenticaton.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param userId
     *         The id of the user that asking to insert the assignment.
     * @param lecture
     *         The lecture that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException
     *         Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertLecture(final Authenticator authenticator, final DB dbs, final String userId, final Lecture lecture)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection newUser = dbs.getCollection(LECTURE_COLLECTION);

        // make sure person is mod or admin for the assignment
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE, lecture.getCourseId(), userId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject query = new BasicDBObject(COURSE_ID, lecture.getCourseId())
                .append(NAME, lecture.getName())
                .append(DESCRIPTION, lecture.getDescription())
                .append(ADMIN, lecture.getAccessPermission().getAdminPermissionList())
                .append(MOD, lecture.getAccessPermission().getModeratorPermissionList())
                .append(USERS, lecture.getAccessPermission().getUserPermissionList());
        if (lecture.hasAccessDate()) {
            query.append(ACCESS_DATE, lecture.getAccessDate().getMillisecond());
        } else {
            query.append(ACCESS_DATE, RequestConverter.getProtoFromMilliseconds(0).getMillisecond());
        }
        if (lecture.hasCloseDate()) {
            query.append(CLOSE_DATE, lecture.getCloseDate().getMillisecond());
        } else {
            query.append(CLOSE_DATE, RequestConverter.getProtoFromMilliseconds(RequestConverter.getMaxTime()).getMillisecond());
        }
        if (lecture.getIdListList() != null) {
            final ArrayList<BasicDBObject> objects = new ArrayList<>();
            for (protobuf.srl.lecturedata.Lecturedata.IdsInLecture id : lecture.getIdListList()) {
                objects.add(createIdInLecture(id.getId(), id.getIsSlide(), id.getUnlocked()));
            }
            query.append(SLIDES, objects);
        }

        newUser.insert(query);
        final DBObject cursor = newUser.findOne(query);

        // inserts the id into the previous the course
        CourseManager.mongoInsertLectureIntoCourse(dbs, lecture.getCourseId(), cursor.get(SELF_ID).toString());

        return cursor.get(SELF_ID).toString();
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * This is used to copy permissions from the parent course into the current
     * lecture.
     *
     * @param dbs
     *         the database where the data is stored.
     * @param lectureId
     *         the id of the assignment that is getting permissions.
     * @param ids
     *         the list of list of permissions that is getting added.
     */
    /*package-private*/static void mongoInsertDefaultGroupId(final DB dbs, final String lectureId, final List<String>... ids) {
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject cursor = myDbRef.fetch();
        final DBCollection lectures = dbs.getCollection(LECTURE_COLLECTION);

        final BasicDBObject updateQuery = new BasicDBObject(); // MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        LOG.info("Updated Query: {}", updateQuery);
        lectures.update(cursor, updateQuery);
    }

    /**
     * Grabs the lecture from mongo and performs checks making sure the user is valid before returning the lecture.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param lectureId
     *         the id of the lecture that is being grabbed.
     * @param userId
     *         The id of the user that asking to insert the lecture.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the lecture is valid)
     * @return The lecture from the database.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the
     *         assignment.
     * @throws DatabaseAccessException
     *         Thrown if there are problems retrieving the lecture.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    public static Lecture mongoGetLecture(final Authenticator authenticator, final DB dbs, final String lectureId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Lecture was not found with the following ID " + lectureId, true);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.LECTURE, lectureId, userId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For lecture: " + lectureId, AuthenticationException.INVALID_PERMISSION);
        }


        // Throws an exception if a user (only) is trying to get a course problem when the class is not in session.
        final Authentication.AuthType assignmentAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build();
        final AuthenticationResponder assignmentResponder = authenticator
                .checkAuthentication(School.ItemType.COURSE, (String) corsor.get(COURSE_ID), userId, checkTime, assignmentAuthType);

        // Throws an exception if a user (only) is trying to get an assignment when the class is not in session.
        if (responder.hasAccess() && !responder.hasPeerTeacherPermission() && !assignmentResponder.isItemOpen()) {
            throw new AuthenticationException("For lecture: " + lectureId, AuthenticationException.INVALID_DATE);
        }

        // states
        final School.State.Builder stateBuilder = School.State.newBuilder();
        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met
        if (!responder.isItemPublished() && !responder.hasModeratorPermission()) {
            throw new DatabaseAccessException("The specific lecture is not published yet: " + lectureId, true);
        }

        // now all possible exceptions have already been thrown.
        final Lecture.Builder exactLecture = Lecture.newBuilder();

        exactLecture.setId(lectureId);

        // sets the majority of the assignment data
        setLectureData(exactLecture, corsor);

        setLectureStateAndDate(exactLecture, corsor);

        // if you are a user, the lecture must be open to view the insides
        if (responder.hasModeratorPermission() || (responder.hasAccess()
                && Authenticator.isTimeValid(checkTime, exactLecture.getAccessDate(), exactLecture.getCloseDate()))) {
            if (corsor.get(SLIDES) != null) {
                for (BasicDBObject obj : (List<BasicDBObject>) corsor.get(SLIDES)) {
                    final Lecturedata.IdsInLecture.Builder builder = Lecturedata.IdsInLecture.newBuilder();
                    builder.setId((String) obj.get(SELF_ID));
                    builder.setIsSlide((Boolean) obj.get(IS_SLIDE));
                    builder.setUnlocked((Boolean) obj.get(IS_UNLOCKED));
                    exactLecture.addIdList(builder.build());
                }
            }

            stateBuilder.setAccessible(true);
        } else if (responder.hasAccess() && !Authenticator.isTimeValid(checkTime, exactLecture.getAccessDate(), exactLecture.getCloseDate())) {
            stateBuilder.setAccessible(false);
            LOG.info("USER LECTURE TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            LOG.info("TIME OPEN: {} \n CURRENT TIME: {} \n TIME CLOSED: {} \n", exactLecture.getAccessDate().getMillisecond(),
                    checkTime, exactLecture.getCloseDate().getMillisecond());
        }

        exactLecture.setState(stateBuilder);

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (responder.hasTeacherPermission()) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
        }
        if (responder.hasModeratorPermission()) {
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // mod
            exactLecture.setAccessPermission(permissions.build());
        }
        return exactLecture.build();
    }

    /**
     * sets data of the lecture from the given cursor.
     *
     * @param exactLecture
     *         The lecture that the data is being set to.
     * @param cursor
     *         The database cursor pointing to a specific lecture.
     */
    private static void setLectureData(final Lecture.Builder exactLecture, final DBObject cursor) {
        exactLecture.setCourseId((String) cursor.get(COURSE_ID));
        exactLecture.setName((String) cursor.get(NAME));
        exactLecture.setDescription((String) cursor.get(DESCRIPTION));
    }

    /**
     * Sets data about the state of the lecture and its date.
     *
     * @param exactLecture
     *         a protobuf lecture builder.
     * @param corsor
     *         the current database pointer for the lecture.
     */
    private static void setLectureStateAndDate(final Lecture.Builder exactLecture, final DBObject corsor) {
        final Object accessDate = corsor.get(ACCESS_DATE);
        final Object closeDate = corsor.get(CLOSE_DATE);
        if (accessDate == null) {
            exactLecture.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
        } else {
            exactLecture.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) accessDate).longValue()));
        }
        if (closeDate == null) {
            exactLecture.setCloseDate(RequestConverter.getProtoFromMilliseconds(RequestConverter.getMaxTime()));
        } else {
            exactLecture.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) closeDate).longValue()));
        }
    }

    /**
     * updates data from an assignment.
     * @param authenticator the object that is performing authentication.
     * @param dbs The database where the lecture is being stored.
     * @param lectureId the id of the lecture that is being updated.
     * @param userId The id of the user that asking to insert the assignment.
     * @param lecture The lecture that is being inserted.
     * @return true if the lecture was updated successfully.
     * @throws AuthenticationException The user does not have permission to update the lecture.
     * @throws DatabaseAccessException The lecture does not exist.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static boolean mongoUpdateLecture(final Authenticator authenticator, final DB dbs, final String lectureId, final String userId,
            final Lecture lecture) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject cursor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection lectures = dbs.getCollection(LECTURE_COLLECTION);

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.LECTURE, lectureId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For lecture: " + lectureId, AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject updated = new BasicDBObject();
        if (responder.hasModeratorPermission()) {
            if (lecture.hasName()) {
                updateObj = new BasicDBObject(NAME, lecture.getName());
                lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (lecture.hasDescription()) {
                updateObj = new BasicDBObject(DESCRIPTION, lecture.getDescription());
                lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (lecture.getIdListCount() > 0) {
                updateObj = new BasicDBObject(IDS_IN_LECTURE, lecture.getIdListList());
                lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (lecture.hasAccessDate()) {
                updateObj = new BasicDBObject(ACCESS_DATE, lecture.getAccessDate().getMillisecond());
                lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (lecture.hasCloseDate()) {
                updateObj = new BasicDBObject(CLOSE_DATE, lecture.getCloseDate().getMillisecond());
                lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (lecture.hasAccessPermission()) {
                final SrlPermission permissions = lecture.getAccessPermission();
                if (responder.hasTeacherPermission()) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updated.append(SET_COMMAND, new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
                        updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
                        lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
                        lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
                    lectures.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                }
            }
        }
        if (update) {
            UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), lectureId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
        }
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public.
     * <p/>
     * With that being said this allows a course to be updated adding the
     * slideId to its list of items.
     *
     * @param dbs
     *         The database where the assignment is being stored.
     * @param lectureId
     *         the course into which the assignment is being inserted into
     * @param slideId
     *         the assignment that is being inserted into the course.
     * @param unlocked
     *         a boolean that is true if the object is unlocked
     * @return true if the assignment was inserted correctly.
     * @throws AuthenticationException The user does not have permission to update the lecture.
     * @throws DatabaseAccessException The lecture does not exist.
     */
    static boolean mongoInsertSlideIntoLecture(final DB dbs, final String lectureId, final String slideId, final boolean unlocked)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject cursor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection lectures = dbs.getCollection(LECTURE_COLLECTION);
        updateObj = createIdInLecture(slideId, true, unlocked);
        lectures.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), lectureId, UserUpdateHandler.LECTURE_CLASSIFICATION);
        return true;
    }

    /**
     * NOTE: This is meant for internal use.
     *
     * creates an object of the IdInLecture message type from the proto file
     *
     * @param slideId
     *         the slideId of the slide that used to create the message
     * @param isSlide
     *         a boolean that is true if the slideId param belongs to a slide
     * @param isUnlocked
     *         a boolean that is true if the user trying to access this slide is allowed
     * @return a BasicDBObject of the message type IdInLecture
     */
    private static BasicDBObject createIdInLecture(final String slideId, final boolean isSlide, final boolean isUnlocked) {
        return new BasicDBObject(SLIDES, new BasicDBObject(SELF_ID, slideId).append(IS_SLIDE, isSlide).append(IS_UNLOCKED, isUnlocked));
    }
}
