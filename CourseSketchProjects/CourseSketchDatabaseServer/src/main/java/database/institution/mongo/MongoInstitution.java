package database.institution.mongo;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import coursesketch.server.interfaces.MultiConnectionManager;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.institution.Institution;
import database.submission.SubmissionManager;
import database.user.GroupManager;
import database.user.UserClient;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.request.Message;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlGroup;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.utils.Util.SrlPermission;
import utilities.LoggingConstants;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.DATABASE;
import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.UPDATE_COLLECTION;
import static database.DatabaseStringConstants.USER_COLLECTION;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

/**
 * A Mongo implementation of the Institution it inserts and gets courses as
 * needed.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CommentRequired", "PMD.TooManyMethods" })
public final class MongoInstitution implements Institution {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MongoInstitution.class);

    /**
     * A single instance of the mongo institution.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static volatile MongoInstitution instance;

    /**
     * Holds an Authenticator used to authenticate specific users.
     */
    private Authenticator auth;

    /**
     * A private Database that stores all of the data used by mongo.
     */
    private DB database;

    /**
     * A private institution that accepts a url for the database location.
     *
     * @param url
     *         The location that the server is taking place.
     */
    private MongoInstitution(final String url) {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(url);
        } catch (UnknownHostException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
        if (mongoClient == null) {
            return;
        }
        database = mongoClient.getDB(DATABASE);
    }

    /**
     * A default constructor that creates an instance at a specific database
     * location.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private MongoInstitution() {
        this("192.168.56.201");
        // this("localhost");
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test
     * instance that can only access a test database.
     * @param testOnly
     *         if true it uses the test database. Otherwise it uses the real
     *         name of the database.
     * @param fakeDB The fake database.
     * @param authenticator What is used to authenticate access to the different resources.
     */
    public MongoInstitution(final boolean testOnly, final DB fakeDB, final Authenticator authenticator) {
        if (testOnly && fakeDB != null) {
            database = fakeDB;
        } else {
            MongoClient mongoClient = null;
            try {
                mongoClient = new MongoClient("localhost");
            } catch (UnknownHostException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
            if (mongoClient == null) {
                return;
            }
            if (testOnly) {
                database = mongoClient.getDB("test");
            } else {
                database = mongoClient.getDB(DATABASE);

            }
        }
        instance = this;
        instance.auth = auth;
    }

    /**
     * @return An instance of the mongo client. Creates it if it does not exist.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Double-checked_locking">Double Checked Locking</a>.
     * @param authenticator What is used to authenticate access to the different resources.
     */
    @SuppressWarnings("checkstyle:innerassignment")
    public static MongoInstitution getInstance(final Authenticator authenticator) {
        MongoInstitution result = instance;
        if (result == null) {
            synchronized (MongoInstitution.class) {
                if (result == null) {
                    result = instance;
                    instance = result = new MongoInstitution();
                    result.auth = authenticator;
                }
            }
        }
        return result;
    }

    @Override
    public void setUpIndexes() {
        LOG.info("Setting up the indexes");
        database.getCollection(USER_COLLECTION).ensureIndex(new BasicDBObject(SELF_ID, 1).append("unique", true));
        database.getCollection(UPDATE_COLLECTION).ensureIndex(new BasicDBObject(SELF_ID, 1).append("unique", true));
    }

    @Override
    public ArrayList<SrlCourse> getCourses(final List<String> courseIds, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlCourse> allCourses = new ArrayList<SrlCourse>();
        for (String courseId : courseIds) {
            allCourses.add(CourseManager.mongoGetCourse(getInstance(null).auth, getInstance(null).database, courseId, userId, currentTime));
        }
        return allCourses;
    }

    @Override
    public ArrayList<SrlProblem> getCourseProblem(final List<String> problemID, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlProblem> allCourses = new ArrayList<SrlProblem>();
        for (int index = 0; index < problemID.size(); index++) {
            try {
                allCourses.add(CourseProblemManager.mongoGetCourseProblem(
                        getInstance(null).auth, getInstance(null).database, problemID.get(index), userId, currentTime));
            } catch (DatabaseAccessException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (!e.isRecoverable()) {
                    throw e;
                }
            } catch (AuthenticationException e) {
                if (e.getType() != AuthenticationException.INVALID_DATE) {
                    throw e;
                }
            }
        }
        return allCourses;
    }

    @Override
    public ArrayList<SrlAssignment> getAssignment(final List<String> assignmentID, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
        for (int assignments = assignmentID.size() - 1; assignments >= 0; assignments--) {
            try {
                allAssignments.add(AssignmentManager.mongoGetAssignment(
                        getInstance(null).auth, getInstance(null).database, assignmentID.get(assignments), userId, currentTime));
            } catch (DatabaseAccessException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (!e.isRecoverable()) {
                    throw e;
                }
            } catch (AuthenticationException e) {
                if (e.getType() != AuthenticationException.INVALID_DATE) {
                    throw e;
                }
            }
        }
        return allAssignments;
    }

    @Override
    public ArrayList<Lecture> getLecture(final List<String> lectureId, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<Lecture> allLectures = new ArrayList<Lecture>();
        for (int lectures = lectureId.size() - 1; lectures >= 0; lectures--) {
            try {
                allLectures.add(LectureManager.mongoGetLecture(getInstance(null).auth, getInstance(null).database, lectureId.get(lectures),
                        userId, currentTime));
            } catch (DatabaseAccessException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (!e.isRecoverable()) {
                    throw e;
                }
            } catch (AuthenticationException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (e.getType() != AuthenticationException.INVALID_DATE) {
                    throw e;
                }
            }
        }
        return allLectures;
    }

    @Override
    public ArrayList<LectureSlide> getLectureSlide(final List<String> slideId, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<LectureSlide> allSlides = new ArrayList<LectureSlide>();
        for (int slides = slideId.size() - 1; slides >= 0; slides--) {
            try {
                allSlides.add(SlideManager.mongoGetLectureSlide(getInstance(null).auth, getInstance(null).database, slideId.get(slides),
                        userId, currentTime));
            } catch (DatabaseAccessException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (!e.isRecoverable()) {
                    throw e;
                }
            } catch (AuthenticationException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                if (e.getType() != AuthenticationException.INVALID_DATE) {
                    throw e;
                }
            }
        }
        return allSlides;
    }

    @Override
    public ArrayList<SrlBankProblem> getProblem(final List<String> problemID, final String userId)
            throws AuthenticationException, DatabaseAccessException {
        final ArrayList<SrlBankProblem> allProblems = new ArrayList<>();
        for (int problem = problemID.size() - 1; problem >= 0; problem--) {
            allProblems.add(BankProblemManager.mongoGetBankProblem(
                    getInstance(null).auth, getInstance(null).database, problemID.get(problem), userId));
        }
        return allProblems;
    }

    @Override
    public List<SrlCourse> getAllPublicCourses() {
        return CourseManager.mongoGetAllPublicCourses(getInstance(null).database);
    }

    @Override
    public String insertCourse(final String userId, final SrlCourse course) throws DatabaseAccessException {

        // Creates the default permissions for the courses.
        SrlPermission permission = null;
        if (course.hasAccessPermission()) {
            permission = course.getAccessPermission();
        }

        final SrlGroup.Builder courseGroup = SrlGroup.newBuilder();
        courseGroup.addAdmin(userId);
        courseGroup.setGroupName(course.getName() + "_User");
        courseGroup.clearUserId();
        if (permission != null && permission.getUserPermissionCount() > 0) {
            courseGroup.addAllUserId(permission.getUserPermissionList());
        }
        final String userGroupId = GroupManager.mongoInsertGroup(getInstance(null).database, courseGroup.buildPartial());

        courseGroup.setGroupName(course.getName() + "_Mod");
        courseGroup.clearUserId();
        if (permission != null && permission.getModeratorPermissionCount() > 0) {
            courseGroup.addAllUserId(permission.getModeratorPermissionList());
        }
        final String modGroupId = GroupManager.mongoInsertGroup(getInstance(null).database, courseGroup.buildPartial());

        courseGroup.setGroupName(course.getName() + "_Admin");
        courseGroup.clearUserId();
        if (permission != null && permission.getAdminPermissionCount() > 0) {
            courseGroup.addAllUserId(permission.getAdminPermissionList());
        }
        courseGroup.addUserId(userId); // an admin will always exist
        final String adminGroupId = GroupManager.mongoInsertGroup(getInstance(null).database, courseGroup.buildPartial());

        // overwrites the existing permissions with the new user specific course
        // permission
        final SrlCourse.Builder builder = SrlCourse.newBuilder(course);
        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        permissions.addAdminPermission(GROUP_PREFIX + adminGroupId);
        permissions.addModeratorPermission(GROUP_PREFIX + modGroupId);
        permissions.addUserPermission(GROUP_PREFIX + userGroupId);
        builder.setAccessPermission(permissions.build());
        final String resultId = CourseManager.mongoInsertCourse(getInstance(null).database, builder.buildPartial());

        // links the course to the group!
        CourseManager.mongoInsertDefaultGroupId(getInstance(null).database, resultId, userGroupId, modGroupId, adminGroupId);

        // adds the course to the users list
        final boolean success = this.putUserInCourse(resultId, userId);
        if (!success) {
            throw new DatabaseAccessException("No success: ", false);
        }

        // FUTURE: try to undo what has been done! (and more error handling!)

        return resultId;
    }

    @Override
    public String insertAssignment(final String userId, final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        final String resultId = AssignmentManager.mongoInsertAssignment(getInstance(null).auth, getInstance(null).database, userId, assignment);

        final List<String>[] ids = CourseManager.mongoGetDefaultGroupList(getInstance(null).database, assignment.getCourseId());
        AssignmentManager.mongoInsertDefaultGroupId(getInstance(null).database, resultId, ids);

        return resultId;
    }

    @Override
    public String insertLecture(final String userId, final Lecture lecture) throws AuthenticationException, DatabaseAccessException {
        final String resultId = LectureManager.mongoInsertLecture(getInstance(null).auth, getInstance(null).database, userId, lecture);

        final List<String>[] ids = CourseManager.mongoGetDefaultGroupList(getInstance(null).database, lecture.getCourseId());
        LectureManager.mongoInsertDefaultGroupId(getInstance(null).database, resultId, ids);

        return resultId;
    }

    @Override
    public String insertLectureSlide(final String userId, final LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException {
        return SlideManager.mongoInsertSlide(getInstance(null).auth, getInstance(null).database, userId, lectureSlide);
    }

    @Override
    public String insertCourseProblem(final String userId, final SrlProblem problem) throws AuthenticationException, DatabaseAccessException {
        final String resultId = CourseProblemManager.mongoInsertCourseProblem(getInstance(null).auth, getInstance(null).database, userId, problem);

        final List<String>[] ids = AssignmentManager.mongoGetDefaultGroupId(getInstance(null).database, problem.getAssignmentId());
        CourseProblemManager.mongoInsertDefaultGroupId(getInstance(null).database, resultId, ids);
        return resultId;
    }

    @Override
    public String insertBankProblem(final String userId, final SrlBankProblem problem) throws AuthenticationException {
        final SrlBankProblem.Builder builder = SrlBankProblem.newBuilder(problem);
        final SrlPermission.Builder permissions = SrlPermission.newBuilder(problem.getAccessPermission());

        // sanitize admin permissions.
        permissions.clearAdminPermission();

        // add the person creating the problem the admin
        permissions.addAdminPermission(userId);

        builder.setAccessPermission(permissions);
        return BankProblemManager.mongoInsertBankProblem(getInstance(null).database, builder.build());
    }

    @Override
    public void updateLecture(final String userId, final Lecture lecture) throws AuthenticationException, DatabaseAccessException {
        LectureManager.mongoUpdateLecture(getInstance(null).auth, getInstance(null).database, lecture.getId(), userId, lecture);
    }

    @Override
    public void updateCourse(final String userId, final SrlCourse course) throws AuthenticationException, DatabaseAccessException {
        CourseManager.mongoUpdateCourse(getInstance(null).auth, getInstance(null).database, course.getId(), userId, course);
    }

    @Override
    public void updateAssignment(final String userId, final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        AssignmentManager.mongoUpdateAssignment(getInstance(null).auth, getInstance(null).database, assignment.getId(), userId, assignment);
    }

    @Override
    public void updateCourseProblem(final String userId, final SrlProblem srlProblem) throws AuthenticationException, DatabaseAccessException {
        CourseProblemManager.mongoUpdateCourseProblem(getInstance(null).auth, getInstance(null).database, srlProblem.getId(), userId, srlProblem);
    }

    @Override
    public void updateBankProblem(final String userId, final SrlBankProblem srlBankProblem) throws AuthenticationException, DatabaseAccessException {
        BankProblemManager.mongoUpdateBankProblem(getInstance(null).auth, getInstance(null).database, srlBankProblem.getId(), userId, srlBankProblem);
    }

    @Override
    public void updateLectureSlide(final String userId, final LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException {
        SlideManager.mongoUpdateLectureSlide(getInstance(null).auth, getInstance(null).database, lectureSlide.getId(), userId, lectureSlide);
    }

    @Override
    public boolean putUserInCourse(final String courseId, final String userId) throws DatabaseAccessException {
        // this actually requires getting the data from the course itself
        final String userGroupId = CourseManager.mongoGetDefaultGroupId(getInstance(null).database, courseId)[2]; // user
        // group!

        // FIXME: when mongo version 2.5.5 java client comes out please change
        // this!
        /*
        final ArrayList<String> hack = new ArrayList<String>();
        hack.add(GROUP_PREFIX + userGroupId);
        if (getInstance(null).auth.checkAuthentication(userId, hack)) {
            return false;
        }
        */
        // DO NOT USE THIS CODE ANY WHERE ESLE
        final DBRef myDbRef = new DBRef(getInstance(null).database, USER_GROUP_COLLECTION, new ObjectId(userGroupId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection courses = getInstance(null).database.getCollection(USER_GROUP_COLLECTION);
        final BasicDBObject object = new BasicDBObject("$addToSet", new BasicDBObject(USER_LIST, userId));
        courses.update(corsor, object);

        UserClient.addCourseToUser(userId, courseId);
        return true;
    }

    @Override
    public ArrayList<SrlCourse> getUserCourses(final String userId) throws AuthenticationException, DatabaseAccessException {
        return this.getCourses(UserClient.getUserCourses(userId), userId);
    }

    @Override
    public void insertSubmission(final String userId, final String problemId, final String submissionId,
            final boolean experiment)
            throws DatabaseAccessException {
        SubmissionManager.mongoInsertSubmission(getInstance(null).database, userId, problemId, submissionId, experiment);
    }

    @Override
    public void getExperimentAsUser(final String userId, final String problemId, final Message.Request sessionInfo,
            final MultiConnectionManager internalConnections) throws DatabaseAccessException {
        LOG.debug("Getting experiment for user: {}", userId);
        LOG.info("Problem: {}", problemId);
        SubmissionManager.mongoGetExperiment(getInstance(null).database, userId, problemId, sessionInfo, internalConnections);
    }

    @Override
    public void getExperimentAsInstructor(final String userId, final String problemId, final Message.Request sessionInfo,
            final MultiConnectionManager internalConnections, final ByteString review) throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoGetAllExperimentsAsInstructor(getInstance(null).auth, getInstance(null).database, userId, problemId, sessionInfo,
                internalConnections, review);
    }

    @Override
    public List<SrlBankProblem> getAllBankProblems(final String userId, final String courseId, final int page)
            throws AuthenticationException, DatabaseAccessException {
        return BankProblemManager.mongoGetAllBankProblems(getInstance(null).auth, getInstance(null).database, userId, courseId, page);
    }
}
