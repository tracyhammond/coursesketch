package database.institution.mongo;

import static database.DatabaseStringConstants.DATABASE;
import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.UPDATE_COLLECTION;
import static database.DatabaseStringConstants.USER_COLLECTION;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
import multiconnection.MultiConnectionManager;
=======
import com.google.protobuf.InvalidProtocolBufferException;
>>>>>>> origin/master

import coursesketch.server.interfaces.MultiConnectionManager;
import org.bson.types.ObjectId;

import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlGroup;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.submission.Submission.SrlExperiment;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import database.institution.Institution;
import database.submission.SubmissionManager;
import database.user.GroupManager;
import database.user.UserClient;

/**
 * A Mongo implementation of the Institution it inserts and gets courses as
 * needed.
 *
 * @author gigemjt
 *
 */
@SuppressWarnings("PMD.CommentRequired")
public final class MongoInstitution implements Institution {
    /**
     * A single instance of the mongo institution.
     */
    private static MongoInstitution instance;

    /**
     * Holds an Authenticator used to authenticate specific users.
     */
    private Authenticator auth;

    /**
     * A private Database that stores all of the data used by mongo.
     */
    private DB db;

    /**
     * A private institution that accepts a url for the database location.
     *
     * @param url
     *            The location that the server is taking place.
     */
    private MongoInstitution(final String url) {
        try {
            final MongoClient mongoClient = new MongoClient(url);
            db = mongoClient.getDB(DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A default constructor that creates an instance at a specific database
     * location.
     */
    private MongoInstitution() {
        this("goldberglinux.tamu.edu");
        // this("localhost");
    }

    /**
     * @return An instance of the mongo client. Creates it if it does not exist.
     */
    public static MongoInstitution getInstance() {
        if (instance == null) {
            instance = new MongoInstitution();
            instance.auth = new Authenticator(new MongoAuthenticator(instance.db));
        }
        return instance;
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test
     * instance that can only access a test database.
     *
     * @param testOnly
     *            if true it uses the test database. Otherwise it uses the real
     *            name of the database.
     * @param fakeDB
     *            uses a fake DB for its unit tests. This is typically used for
     *            unit test.
     */
    public MongoInstitution(final boolean testOnly, final DB fakeDB) {
        if (testOnly && fakeDB != null) {
            db = fakeDB;
        } else {
            try {
                final MongoClient mongoClient = new MongoClient("localhost");
                if (testOnly) {
                    db = mongoClient.getDB("test");
                } else {
                    db = mongoClient.getDB(DATABASE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        instance = this;
    }

    /*
     * (non-Javadoc)
     *
     * @see database.institution.mongo.Institution#setUpIndexes()
     */
    @Override
    public void setUpIndexes() {
        System.out.println("Setting up the indexes");
        db.getCollection(USER_COLLECTION).ensureIndex(new BasicDBObject(SELF_ID, 1).append("unique", true));
        db.getCollection(UPDATE_COLLECTION).ensureIndex(new BasicDBObject(SELF_ID, 1).append("unique", true));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetCourses(java.util.List,
     * java.lang.String)
     */
    @Override
    public ArrayList<SrlCourse> getCourses(final List<String> courseIds, final String userId) throws AuthenticationException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlCourse> allCourses = new ArrayList<SrlCourse>();
        for (String courseId : courseIds) {
            try {
                allCourses.add(CourseManager.mongoGetCourse(getInstance().auth, getInstance().db, courseId, userId, currentTime));
            } catch (DatabaseAccessException e) {
                e.printStackTrace();
            }
        }
        return allCourses;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetCourseProblem(java.util
     * .List, java.lang.String)
     */
    @Override
    public ArrayList<SrlProblem> getCourseProblem(final List<String> problemID, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlProblem> allCourses = new ArrayList<SrlProblem>();
        for (int index = 0; index < problemID.size(); index++) {
            try {
                allCourses.add(CourseProblemManager.mongoGetCourseProblem(getInstance().auth, getInstance().db, problemID.get(index), userId,
                        currentTime));
            } catch (DatabaseAccessException e) {
                e.printStackTrace();
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

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetAssignment(java.util.List,
     * java.lang.String)
     */
    @Override
    public ArrayList<SrlAssignment> getAssignment(final List<String> assignementID, final String userId) throws AuthenticationException,
            DatabaseAccessException {
        final long currentTime = System.currentTimeMillis();
        final ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
        for (int assignments = assignementID.size() - 1; assignments >= 0; assignments--) {
            try {
                allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().auth, getInstance().db, assignementID.get(assignments), userId,
                        currentTime));
            } catch (DatabaseAccessException e) {
                e.printStackTrace();
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

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetProblem(java.util.List,
     * java.lang.String)
     */
    @Override
    public ArrayList<SrlBankProblem> getProblem(final List<String> problemID, final String userId) throws AuthenticationException {
        final ArrayList<SrlBankProblem> allProblems = new ArrayList<SrlBankProblem>();
        for (int problem = problemID.size() - 1; problem >= 0; problem--) {
            allProblems.add(BankProblemManager.mongoGetBankProblem(getInstance().auth, getInstance().db, problemID.get(problem), userId));
        }
        return allProblems;
    }

    /*
     * (non-Javadoc)
     *
     * @see database.institution.mongo.Institution#getAllPublicCourses()
     */
    @Override
    public ArrayList<SrlCourse> getAllPublicCourses() {
        return CourseManager.mongoGetAllPublicCourses(getInstance().db);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertCourse(java.lang.String
     * , protobuf.srl.school.School.SrlCourse)
     */
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
        final String userGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

        courseGroup.setGroupName(course.getName() + "_Mod");
        courseGroup.clearUserId();
        if (permission != null && permission.getModeratorPermissionCount() > 0) {
            courseGroup.addAllUserId(permission.getModeratorPermissionList());
        }
        final String modGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

        courseGroup.setGroupName(course.getName() + "_Admin");
        courseGroup.clearUserId();
        if (permission != null && permission.getAdminPermissionCount() > 0) {
            courseGroup.addAllUserId(permission.getAdminPermissionList());
        }
        courseGroup.addUserId(userId); // an admin will always exist
        final String adminGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

        // overwrites the existing permissions with the new user specific course
        // permission
        final SrlCourse.Builder builder = SrlCourse.newBuilder(course);
        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        permissions.addAdminPermission(GROUP_PREFIX + adminGroupId);
        permissions.addModeratorPermission(GROUP_PREFIX + modGroupId);
        permissions.addUserPermission(GROUP_PREFIX + userGroupId);
        builder.setAccessPermission(permissions.build());
        final String resultId = CourseManager.mongoInsertCourse(getInstance().db, builder.buildPartial());

        // links the course to the group!
        CourseManager.mongoInsertDefaultGroupId(getInstance().db, resultId, userGroupId, modGroupId, adminGroupId);

        // adds the course to the users list
        final boolean success = this.putUserInCourse(resultId, userId);
        if (!success) {
            throw new DatabaseAccessException("No success: ", false);
        }

        // FUTURE: try to undo what has been done! (and more error handling!)

        return resultId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertAssignment(java.lang
     * .String, protobuf.srl.school.School.SrlAssignment)
     */
    @Override
    public String insertAssignment(final String userId, final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        final String resultId = AssignmentManager.mongoInsertAssignment(getInstance().auth, getInstance().db, userId, assignment);

        final ArrayList<String>[] ids = CourseManager.mongoGetDefaultGroupList(getInstance().db, assignment.getCourseId());
        AssignmentManager.mongoInsertDefaultGroupId(getInstance().db, resultId, ids);

        return resultId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertCourseProblem(java.
     * lang.String, protobuf.srl.school.School.SrlProblem)
     */
    @Override
    public String insertCourseProblem(final String userId, final SrlProblem problem) throws AuthenticationException, DatabaseAccessException {
        final String resultId = CourseProblemManager.mongoInsertCourseProblem(getInstance().auth, getInstance().db, userId, problem);

        final ArrayList<String>[] ids = AssignmentManager.mongoGetDefaultGroupId(getInstance().db, problem.getAssignmentId());
        CourseProblemManager.mongoInsertDefaultGroupId(getInstance().db, resultId, ids);
        return resultId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertBankProblem(java.lang
     * .String, protobuf.srl.school.School.SrlBankProblem)
     */
    @Override
    public String insertBankProblem(final String userId, final SrlBankProblem problem) throws AuthenticationException {
        return BankProblemManager.mongoInsertBankProblem(getInstance().db, problem);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#putUserInCourse(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean putUserInCourse(final String courseId, final String userId) throws DatabaseAccessException {
        // this actually requires getting the data from the course itself
        final String userGroupId = CourseManager.mongoGetDefaultGroupId(getInstance().db, courseId)[2]; // user
                                                                                                        // group!

        // FIXME: when mongo version 2.5.5 java client comes out please change
        // this!
        final ArrayList<String> hack = new ArrayList<String>();
        hack.add(GROUP_PREFIX + userGroupId);
        if (getInstance().auth.checkAuthentication(userId, hack)) {
            return false;
        }
        // DO NOT USE THIS CODE ANY WHERE ESLE
        final DBRef myDbRef = new DBRef(getInstance().db, USER_GROUP_COLLECTION, new ObjectId(userGroupId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection courses = getInstance().db.getCollection(USER_GROUP_COLLECTION);
        final BasicDBObject object = new BasicDBObject("$addToSet", new BasicDBObject(USER_LIST, userId));
        courses.update(corsor, object);

        UserClient.addCourseToUser(userId, courseId);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#getUserCourses(java.lang.String)
     */
    @Override
    public ArrayList<SrlCourse> getUserCourses(final String userId) throws AuthenticationException, DatabaseAccessException {
        return this.getCourses(UserClient.getUserCourses(userId), userId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertSubmission(protobuf
     * .srl.request.Message.Request)
     */
    @Override
    public void insertSubmission(final Request req) throws DatabaseAccessException {
        try {
            final SrlExperiment exp = SrlExperiment.parseFrom(req.getOtherData());
            insertSubmission(exp.getProblemId(), req.getServersideId(), exp.getSubmission().getId(), true);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
            // FUTURE: change how this process works for instructors!
            //final SrlSolution exp = SrlSolution.parseFrom(req.getOtherData());
            throw new DatabaseAccessException("Instructors need to be authenticated first!");
            // SubmissionManager.mongoInsertSubmission(exp.getProblemBankId(),
            // exp.getProblemBankId(), exp.getSubmission().getId(), false);
            // return;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoInsertSubmission(java.lang
     * .String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void insertSubmission(final String problemId, final String userId, final String submissionId, final boolean experiment)
            throws DatabaseAccessException {
        SubmissionManager.mongoInsertSubmission(getInstance().db, problemId, userId, submissionId, experiment);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetExperimentAsUser(java.
     * lang.String, java.lang.String, java.lang.String,
     * coursesketch.server.interfaces.MultiConnectionManager)
     */
    @Override
    public void getExperimentAsUser(final String userId, final String problemId, final String sessionInfo,
            final MultiConnectionManager internalConnections) throws DatabaseAccessException {
        try {
            System.out.println("Getting experiment for user: " + userId + " problem: " + problemId);
            SubmissionManager.mongoGetExperiment(getInstance().db, userId, problemId, sessionInfo, internalConnections);
            return;
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * database.institution.mongo.Institution#mongoGetExperimentAsInstructor
     * (java.lang.String, java.lang.String, java.lang.String,
     * coursesketch.server.interfaces.MultiConnectionManager, com.google.protobuf.ByteString)
     */
    @Override
    public void getExperimentAsInstructor(final String userId, final String problemId, final String sessionInfo,
            final MultiConnectionManager internalConnections, final ByteString review) {
        try {
            SubmissionManager.mongoGetAllExperimentsAsInstructor(getInstance().auth, getInstance().db, userId, problemId, sessionInfo,
                    internalConnections, review);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
