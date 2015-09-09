package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.AuthenticationResponder;
import database.auth.Authenticator;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.State;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.SrlPermission;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADMIN;
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
import static database.DatabaseStringConstants.USERS;
import static database.utilities.MongoUtilities.createId;

/**
 * Manages course problems for the mongo database.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.UselessParentheses",
        "PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts" })
public final class CourseProblemManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CourseProblemManager.class);

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
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, problem.getAssignmentId(), userId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For assignment: " + problem.getAssignmentId(), AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, problem.getCourseId()).append(ASSIGNMENT_ID, problem.getAssignmentId())
                .append(PROBLEM_BANK_ID, problem.getProblemBankId()).append(GRADE_WEIGHT, problem.getGradeWeight())
                .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
                .append(MOD, problem.getAccessPermission().getModeratorPermissionList())
                .append(USERS, problem.getAccessPermission().getUserPermissionList()).append(NAME, problem.getName())
                .append(PROBLEM_NUMBER, problem.getProblemNumber());
        courseProblemCollection.insert(query);
        final DBObject cursor = courseProblemCollection.findOne(query);

        // inserts the id into the previous the course
        AssignmentManager.mongoInsert(dbs, problem.getAssignmentId(), cursor.get(SELF_ID).toString());

        if (problem.hasProblemBankId()) {
            BankProblemManager.mongoRegisterCourseProblem(authenticator, dbs, userId, problem);
        }

        return cursor.get(SELF_ID).toString();
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
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, createId(problemId));
        final DBObject cursor = myDbRef.fetch();
        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, userId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        // Throws an exception if a user (only) is trying to get a course problem when the class is not in session.
        final Authentication.AuthType assignmentAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder assignmentResponder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, (String) cursor.get(ASSIGNMENT_ID), userId, checkTime, assignmentAuthType);

        // Throws an exception if a user (only) is trying to get an problem when the assignment is closed.
        if (responder.hasAccess() && !responder.hasPeerTeacherPermission() && !assignmentResponder.isItemOpen()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_DATE);
        }

        // states
        final State.Builder stateBuilder = State.newBuilder();
        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met

        // NOTE: we are assuming that only assignments as a whole can be published not specific problems.
        if (!assignmentResponder.isItemPublished() && !responder.hasModeratorPermission()) {
            throw new DatabaseAccessException("The specific problem is not published yet: " + problemId, true);
        }

        // Post this point either item is published OR responder is at least responder.
        stateBuilder.setPublished(responder.isItemPublished());

        final SrlProblem.Builder exactProblem = SrlProblem.newBuilder();
        exactProblem.setId(problemId);
        extractProblemData(exactProblem, cursor);

        // problem manager get problem from bank (as a user!)
        SrlBankProblem problemBank = null;
        try {
            problemBank = BankProblemManager.mongoGetBankProblem(authenticator, dbs, (String) cursor.get(PROBLEM_BANK_ID),
                    (String) exactProblem.getCourseId());
        } catch (DatabaseAccessException e) {
            // only a student can't view a problem with no problem info.
            // TODO: check to see if this is the best option!
            if (!responder.hasModeratorPermission() && assignmentResponder.isItemPublished()) {
                throw new DatabaseAccessException(e, false);
            }
        }
        if (problemBank != null) {
            exactProblem.setProblemInfo(problemBank);
        }

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (responder.hasTeacherPermission()) {
            permissions.addAllAdminPermission((ArrayList) cursor.get(ADMIN)); // admin can change this
            permissions.addAllModeratorPermission((ArrayList) cursor.get(MOD)); // admin can change this
        }
        if (responder.hasModeratorPermission()) {
            permissions.addAllUserPermission((ArrayList) cursor.get(USERS)); // mod can change this
            exactProblem.setAccessPermission(permissions.build());
        }
        return exactProblem.build();

    }

    private static void extractProblemData(final SrlProblem.Builder problem, final DBObject dbProblem) {
        problem.setCourseId((String) dbProblem.get(COURSE_ID));
        problem.setAssignmentId((String) dbProblem.get(ASSIGNMENT_ID));
        problem.setProblemBankId((String) dbProblem.get(PROBLEM_BANK_ID));
        problem.setGradeWeight((String) dbProblem.get(GRADE_WEIGHT));
        problem.setName((String) dbProblem.get(NAME));
        problem.setProblemNumber((Integer) dbProblem.get(PROBLEM_NUMBER));
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

        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID: " + problemId);
        }

        final BasicDBObject updateObj = new BasicDBObject();
        final DBCollection problemCollection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        if (responder.hasModeratorPermission()) {
            if (problem.hasName()) {
                updateObj.append(NAME, problem.getName());
                update = true;
            }
            if (problem.hasGradeWeight()) {
                updateObj.append(GRADE_WEIGHT, problem.getGradeWeight());
                update = true;
            }
            if (problem.hasProblemBankId()) {
                updateObj.append(PROBLEM_BANK_ID, problem.getProblemBankId());

                // updates the bank problem associated with this course problem
                LOG.warn("Changing the bank problem id. This feature may be removed in the future");
                BankProblemManager.mongoRegisterCourseProblem(authenticator, dbs, userId, problem);
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (problem.hasAccessPermission()) {
                final SrlPermission permissions = problem.getAccessPermission();
                if (responder.hasTeacherPermission()) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updateObj.append(ADMIN, permissions.getAdminPermissionList());
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updateObj.append(MOD, permissions.getModeratorPermissionList());
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updateObj.append(USERS, permissions.getUserPermissionList());
                }
            }
        }
        if (update) {
            problemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
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

        /*
        final BasicDBObject updateQuery = MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        LOG.info("Updated Query: ", updateQuery);
        problems.update(corsor, updateQuery);
        */
    }
}
