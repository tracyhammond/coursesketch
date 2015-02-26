package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.State;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.ASSIGNMENT_COLLECTION;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_COLLECTION;
import static database.DatabaseStringConstants.GRADE_WEIGHT;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PROBLEM_BANK_ID;
import static database.DatabaseStringConstants.PROBLEM_NUMBER;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.USERS;

/**
 * Manages course problems for the mongo database.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.UselessParentheses",
        "PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts" })
public final class CourseProblemManager {

    /**
     * Private constructor.
     */
    private CourseProblemManager() {
    }

    /**
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the course problem is being stored.
     * @param userId
     *         The user that is asking to insert a course problem.
     * @param problem
     *         The data of the course problem being inserted.
     * @return The mongo database Id of the course problem.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static String mongoInsertCourseProblem(final Authenticator authenticator, final DB dbs, final String userId, final SrlProblem problem)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection courseProblemCollection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        // make sure person is mod or admin for the assignment
        final AuthType auth = new AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(ASSIGNMENT_COLLECTION, problem.getAssignmentId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, problem.getCourseId()).append(ASSIGNMENT_ID, problem.getAssignmentId())
                .append(PROBLEM_BANK_ID, problem.getProblemBankId()).append(GRADE_WEIGHT, problem.getGradeWeight())
                .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
                .append(MOD, problem.getAccessPermission().getModeratorPermissionList())
                .append(USERS, problem.getAccessPermission().getUserPermissionList()).append(NAME, problem.getName())
                .append(PROBLEM_NUMBER, problem.getProblemNumber());
        courseProblemCollection.insert(query);
        final DBObject corsor = courseProblemCollection.findOne(query);

        // inserts the id into the previous the course
        AssignmentManager.mongoInsert(dbs, problem.getAssignmentId(), corsor.get(SELF_ID).toString());

        return corsor.get(SELF_ID).toString();
    }

    /**
     * Returns an SrlProblem given the problemId
     *
     * If a problem is not within a valid date an exception is thrown.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param problemId
     *         The problem being requested.
     * @param userId
     *         The user requesting the problem.
     * @param checkTime
     *         The time at which the problem was requested.
     * @return An SrlProblem if it exists and all checks pass.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to get the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static SrlProblem mongoGetCourseProblem(final Authenticator authenticator, final DB dbs, final String problemId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID " + problemId);
        }

        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (ArrayList<String>) corsor.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (ArrayList<String>) corsor.get(MOD));
        isUsers = authenticator.checkAuthentication(userId, (ArrayList<String>) corsor.get(USERS));

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the problem is within the time period that the
        // assignment is open and the user is in the assignment
        final AuthType auth = new AuthType();
        auth.setCheckDate(true);
        auth.setUser(true);
        // Throws an exception if a user (only) is trying to get a course problem when the class is not in session.
        if (isUsers && !isAdmin && !isMod && !authenticator
                .isAuthenticated(ASSIGNMENT_COLLECTION, (String) corsor.get(ASSIGNMENT_ID), userId, checkTime, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_DATE);
        }
        // states
        final State.Builder stateBuilder = State.newBuilder();

        // FUTURE: add this to all fields!
        // A course is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
            if (published) {
                stateBuilder.setPublished(true);
            } else {
                if (!isAdmin || !isMod) {
                    throw new DatabaseAccessException("The specific course problem is not published yet", true);
                }
                stateBuilder.setPublished(false);
            }
        }

        final SrlProblem.Builder exactProblem = SrlProblem.newBuilder();

        exactProblem.setId(problemId);
        exactProblem.setCourseId((String) corsor.get(COURSE_ID));
        exactProblem.setAssignmentId((String) corsor.get(ASSIGNMENT_ID));
        exactProblem.setGradeWeight((String) corsor.get(GRADE_WEIGHT));
        exactProblem.setName((String) corsor.get(NAME));

        // problem manager get problem from bank (as a user!)
        final SrlBankProblem problemBank = BankProblemManager.mongoGetBankProblem(authenticator, dbs, (String) corsor.get(PROBLEM_BANK_ID),
                (String) exactProblem.getCourseId()); // problem bank look up
        if (problemBank != null) {
            exactProblem.setProblemInfo(problemBank);
        }

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (isAdmin) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin can change this
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin can change this
        }
        if (isAdmin || isMod) {
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // mod can change this
            exactProblem.setAccessPermission(permissions.build());
        }
        return exactProblem.build();

    }

    /**
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param problemId
     *         The problem being updated.
     * @param userId
     *         The user requesting the problem.
     * @param problem
     *         The data of the problem itself.
     * @return True if the data was updated successfully.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static boolean mongoUpdateCourseProblem(final Authenticator authenticator, final DB dbs, final String problemId, final String userId,
            final SrlProblem problem) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId));
        final DBObject cursor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection problemCollection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        boolean isAdmin, isMod;
        isAdmin = authenticator.checkAuthentication(userId, (ArrayList) cursor.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (ArrayList) cursor.get(MOD));

        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin || isMod) {
            if (problem.hasName()) {
                updateObj = new BasicDBObject(NAME, problem.getName());
                problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (problem.hasGradeWeight()) {
                updateObj = new BasicDBObject(GRADE_WEIGHT, problem.getGradeWeight());
                problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (problem.hasProblemBankId()) {
                updateObj = new BasicDBObject(PROBLEM_BANK_ID, problem.getProblemBankId());
                problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (problem.hasAccessPermission()) {
                final SrlPermission permissions = problem.getAccessPermission();
                if (isAdmin) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
                        problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
                        problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
                    problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
                }
            }
        }
        if (update) {
            problemCollection.update(cursor, updated);
            UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), problemId, UserUpdateHandler.COURSE_PROBLEM_CLASSIFICATION);
        }
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * This is used to copy permissions from the parent assignment into the
     * current problem.
     *
     * @param dbs
     *         The database where the assignment is being stored.
     * @param courseProblemId
     *         The problem that the group is being inserted into.
     * @param ids
     *         The list of id groupings that contain the ids being copied over.
     */
    static void mongoInsertDefaultGroupId(final DB dbs, final String courseProblemId, final List<String>... ids) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(courseProblemId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection problems = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        final BasicDBObject updateQuery = MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        System.out.println(updateQuery);
        problems.update(corsor, updateQuery);
    }
}
