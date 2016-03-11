package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.UserUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.school.School;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.Problem.SrlProblem;
import protobuf.srl.utils.Util.State;
import protobuf.srl.services.authentication.Authentication;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.GRADE_WEIGHT;
import static database.DatabaseStringConstants.IS_SLIDE;
import static database.DatabaseStringConstants.IS_UNLOCKED;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PROBLEM_BANK_ID;
import static database.DatabaseStringConstants.PROBLEM_NUMBER;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.USERS;
import static database.DbSchoolUtility.getCollectionFromType;
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
        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));

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

        final DBCollection collection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject cursor = collection.findOne(convertStringToObjectId(problemId));
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

        if (cursor.containsField(DatabaseStringConstants.PROBLEM_LIST)) {
            createProblemSlideHolderList(authenticator, dbs, authId, exactProblem.getCourseId(), checkTime,
                    (List<DBObject>) cursor.get(DatabaseStringConstants.PROBLEM_LIST));
        }

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
            // adds a single bank problem to the list.
            exactProblem.addSubgroups(SrlProblem.ProblemSlideHolder.newBuilder().setProblem(problemBank));
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
     *
     * @param authenticator
     * @param database
     * @param authId
     * @param checkTime
     * @param slideObjects
     * @return
     * @throws AuthenticationException
     * @throws DatabaseAccessException
     */
    static List<SrlProblem.ProblemSlideHolder> createProblemSlideHolderList(final Authenticator authenticator, final DB database, final String authId,
            final String courseId, final long checkTime, final List<DBObject> slideObjects) throws AuthenticationException, DatabaseAccessException {
        final List<SrlProblem.ProblemSlideHolder> list = new ArrayList<>();
        for (int i = 0; i < slideObjects.size(); i++) {
            final DBObject dbObject = slideObjects.get(i);
            list.add(createProblemSlideHolder(authenticator, database, authId, courseId, checkTime, dbObject, i));
        }
        return list;
    }

    /**
     *
     * @param authenticator
     * @param database
     * @param authId
     * @param checkTime
     * @param data
     * @param index
     * @return
     * @throws AuthenticationException
     * @throws DatabaseAccessException
     */
    private static SrlProblem.ProblemSlideHolder createProblemSlideHolder(final Authenticator authenticator, final DB database,
            final String authId, final String courseId, final long checkTime, final DBObject data, final int index)
            throws AuthenticationException, DatabaseAccessException {
        final SrlProblem.ProblemSlideHolder.Builder holder = SrlProblem.ProblemSlideHolder.newBuilder();
        holder.setIndex(index);

        if (!(Boolean) data.get(DatabaseStringConstants.IS_UNLOCKED)) {
            // Future: add the other conditions for checking if an item is unlocked
            holder.setUnlocked(false);
            return holder.build();
        }
        holder.setUnlocked(true);

        final String id = data.get(DatabaseStringConstants.ITEM_ID).toString();

        if ((Boolean) data.get(DatabaseStringConstants.IS_SLIDE)) {
            final Lecturedata.LectureSlide lectureSlide = SlideManager
                    .mongoGetLectureSlide(authenticator, database, authId, id, checkTime);
            holder.setSlide(lectureSlide);
        } else {
            final SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, database, courseId, id);
            holder.setProblem(problem);
        }

        return holder.build();
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
        final DBCollection problemCollection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject cursor = problemCollection.findOne(convertStringToObjectId(problemId));

        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID: " + problemId);
        }

        final BasicDBObject updateObj = new BasicDBObject();

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
     *
     * @param authenticator
     * @param dbs
     * @param authId
     * @param problemId
     * @param bankProblemId
     * @throws AuthenticationException
     * @throws DatabaseAccessException
     */
    public static void insertNewProblemPart(final Authenticator authenticator, final DB dbs, final String authId, final String problemId,
            final String bankProblemId) throws AuthenticationException, DatabaseAccessException {

        final DBCollection problemCollection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject cursor = problemCollection.findOne(convertStringToObjectId(problemId));

        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID: " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        mongoInsertBankProblemIntoProblemGroup(dbs, cursor.get(DatabaseStringConstants.ASSIGNMENT_ID).toString(),
                problemId,
                bankProblemId,
                true);

    }

    /**
     * NOTE: This is meant for internal use do not make this method public.
     * <p/>
     * With that being said this allows a course to be updated adding the
     * slideId to its list of items.
     *
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param authId
     *         The user requesting the problem.
     *@param assignmentId
     *         The assignment into which the slide is being inserted into.
     * @param problemGroupId
 *         The courseProblem the slide is being inserted into.
     * @param slideId
*         The assignment that is being inserted into the course.
     * @param unlocked
*         A boolean that is true if the object is unlocked.     @return true if the assignment was inserted correctly.
     * @throws AuthenticationException The user does not have permission to update the lecture.
     * @throws DatabaseAccessException The lecture does not exist.
     */
    static boolean mongoInsertSlideIntoSlideGroup(final Authenticator authenticator, final DB dbs, final String authId, final String assignmentId,
            final String problemGroupId,
            final String slideId, final boolean unlocked)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject cursor = collection.findOne(convertStringToObjectId(problemGroupId));

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemGroupId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBObject updateObj = new BasicDBObject(DatabaseStringConstants.PROBLEM_LIST,
                createSlideProblemHolder(slideId, true, unlocked));
        collection.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), assignmentId, UserUpdateHandler.LECTURE_CLASSIFICATION);
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public.
     * <p/>
     * With that being said this allows a course to be updated adding the
     * bankProblemId to its list of items.
     *
     * @param dbs
     *         The database where the assignment is being stored.
     * @param assignmentId
     *         The assignment into which the slide is being inserted into.
     * @param problemGroupId
     *         The courseProblem the slide is being inserted into.
     * @param bankProblemId
     *         The assignment that is being inserted into the course.
     * @param unlocked
     *         A boolean that is true if the object is unlocked.
     * @return true if the assignment was inserted correctly.
     * @throws AuthenticationException The user does not have permission to update the lecture.
     * @throws DatabaseAccessException The lecture does not exist.
     */
    static boolean mongoInsertBankProblemIntoProblemGroup(final DB dbs, final String assignmentId, final String problemGroupId,
            final String bankProblemId, final boolean unlocked)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = dbs.getCollection(getCollectionFromType(School.ItemType.COURSE_PROBLEM));
        final DBObject cursor = collection.findOne(convertStringToObjectId(problemGroupId));

        final DBObject updateObj = new BasicDBObject(DatabaseStringConstants.PROBLEM_LIST,
                createSlideProblemHolder(bankProblemId, false, unlocked));
        collection.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), assignmentId, UserUpdateHandler.LECTURE_CLASSIFICATION);
        return true;
    }

    /**
     * NOTE: This is meant for internal use.
     *
     * creates an object of the IdInLecture message type from the proto file
     *
     * @param itemId
     *         the itemId of the slide or bank problem that used to create the message
     * @param isSlide
     *         a boolean that is true if the slideId param belongs to a slide
     * @param isUnlocked
     *         a boolean that is true if the user trying to access this slide is allowed
     * @return a BasicDBObject of the message type IdInLecture
     */
    private static BasicDBObject createSlideProblemHolder(final String itemId, final boolean isSlide, final boolean isUnlocked) {
        return new BasicDBObject(DatabaseStringConstants.ITEM_ID, itemId).append(IS_SLIDE, isSlide).append(IS_UNLOCKED, isUnlocked);
    }
}
