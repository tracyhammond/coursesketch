package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import database.UserUpdateHandler;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.utils.Util.State;
import protobuf.srl.services.authentication.Authentication;

import java.util.List;

import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_COLLECTION;
import static database.DatabaseStringConstants.GRADE_WEIGHT;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PROBLEM_BANK_ID;
import static database.DatabaseStringConstants.PROBLEM_NUMBER;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.USERS;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Manages course problems for the mongo database.
 *
 * These are problem parts (a,b,c,d) in a normal assignment and the lecture groups in a lecture.
 * In the mongo database, a course problem has the following structure.
 *
 * CourseProblem
 * {
 *     // Ids
 *     _id: ID,
 *     courseId: ID,
 *     assignmentId: ID,
 *
 *     name: String,
 *
 *     // Grade info (overwrites grade policy)
 *     gradeWeight: float,
 *
 *     // The problems contained within this group.
 *     bankProblems: [
 *         { slide: slideUUID },
 *         { problem: problemUUID },
 *         ...
 *     ],
 *     nextGroupId: (if blank it goes through next problem group in list)
 *     questionActions: {
 *         // (if a question has an action it is the last problem in the list)
 *         // these will be how the to proceed on incorrect or correct answers to the problem [this is taken from lectures]
 *         // which depends on the problem
 *      }
 *      problemRestriction: {
 *          // FUTURE: figure these out
 *          // you can only restrict a problem group, problem parts only exist in sequential order
 *          // OR make it so a problem group is a complete then move
 *      }
 *  }
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.UselessParentheses",
        "PMD.CommentSize", "PMD.NPathComplexity" })
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
     * @param authId
     *         The user that is asking to insert a course problem.
     * @param problem
     *         The data of the course problem being inserted.
     * @return The mongo database Id of the course problem.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static String mongoInsertCourseProblem(final Authenticator authenticator, final DB dbs, final String authId, final SrlProblem problem)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection courseProblemCollection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        // Make sure person is mod or admin for the assignment.
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, problem.getAssignmentId(), authId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For assignment: " + problem.getAssignmentId(), AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, problem.getCourseId()).append(ASSIGNMENT_ID, problem.getAssignmentId())
                .append(PROBLEM_BANK_ID, problem.getProblemBankId())
                .append(GRADE_WEIGHT, problem.getGradeWeight())
                .append(NAME, problem.getName())
                .append(PROBLEM_NUMBER, problem.getProblemNumber());
        courseProblemCollection.insert(query);
        final String selfId = query.get(SELF_ID).toString();

        // inserts the id into the previous the course
        AssignmentManager.mongoInsert(dbs, problem.getAssignmentId(), selfId);

        return selfId;
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
     * @param authId
     *         The user requesting the problem.
     * @param problemId
     *         The problem being requested.
     * @param checkTime
     *         The time at which the problem was requested.
     * @return An SrlProblem if it exists and all checks pass.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to get the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static SrlProblem mongoGetCourseProblem(final Authenticator authenticator, final DB dbs, final String authId, final String problemId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, convertStringToObjectId(problemId));
        final DBObject cursor = myDbRef.fetch();
        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, authId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        // Throws an exception if a user (only) is trying to get a course problem when the class is not in session.
        final Authentication.AuthType assignmentAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder assignmentResponder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, (String) cursor.get(ASSIGNMENT_ID), authId, checkTime, assignmentAuthType);

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

        // Past this point, the item is either published or the responder is at least a mod.
        stateBuilder.setPublished(responder.isItemPublished());

        final SrlProblem.Builder exactProblem = extractProblemData(cursor);
        exactProblem.setId(problemId);

        // problem manager get problem from bank (as a user!)
        SrlBankProblem problemBank = null;
        try {
            problemBank = BankProblemManager.mongoGetBankProblem(authenticator, dbs, (String) exactProblem.getCourseId(),
                    (String) cursor.get(PROBLEM_BANK_ID));
        } catch (DatabaseAccessException e) {
            // Students are the only users that cannot view a problem that doesn't have problem info.
            // FUTURE: check to see if this is the best option!
            if (!responder.hasModeratorPermission() && assignmentResponder.isItemPublished()) {
                throw new DatabaseAccessException(e, false);
            }
        }
        if (problemBank != null) {
            exactProblem.setProblemInfo(problemBank);
        }

        return exactProblem.build();

    }

    /**
     * Extracts the problem data from the {@link DBObject} into the {@link SrlProblem}.
     *
     * @param dbProblem Contains the data from the database.
     * @return An {@link SrlProblem} problem that is being filled with the data.
     */
    private static SrlProblem.Builder extractProblemData(final DBObject dbProblem) {
        final SrlProblem.Builder problem = SrlProblem.newBuilder();
        problem.setCourseId((String) dbProblem.get(COURSE_ID));
        problem.setAssignmentId((String) dbProblem.get(ASSIGNMENT_ID));
        problem.setProblemBankId((String) dbProblem.get(PROBLEM_BANK_ID));
        problem.setGradeWeight((String) dbProblem.get(GRADE_WEIGHT));
        problem.setName((String) dbProblem.get(NAME));
        problem.setProblemNumber((Integer) dbProblem.get(PROBLEM_NUMBER));

        return problem;
    }

    /**
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param authId
     *         The user requesting the problem.
     * @param problemId
     *         The problem being updated.
     * @param problem
     *         The data of the problem itself.
     * @return True if the data was updated successfully.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static boolean mongoUpdateCourseProblem(final Authenticator authenticator, final DB dbs, final String authId, final String problemId,
            final SrlProblem problem) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, convertStringToObjectId(problemId));
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
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        // Past this point the user is at least a moderator.
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
            update = true;
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

        final BasicDBObject updateQuery = new BasicDBObject(); //MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        LOG.info("Updated Query: ", updateQuery);
        problems.update(corsor, updateQuery);
    }
}
