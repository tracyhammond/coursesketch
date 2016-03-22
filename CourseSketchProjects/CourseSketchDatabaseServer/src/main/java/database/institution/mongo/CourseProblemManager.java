package database.institution.mongo;

import com.mongodb.BasicDBList;
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
import protobuf.srl.school.Problem;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.Problem.SrlProblem;
import protobuf.srl.utils.Util;
import protobuf.srl.utils.Util.State;
import protobuf.srl.services.authentication.Authentication;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.GRADE_WEIGHT;
import static database.DatabaseStringConstants.IS_UNLOCKED;
import static database.DatabaseStringConstants.NAME;
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
 *         { id: sludeUUID,
 *           type: slideType,
 *           isUnlocked: true
 *         }
 *         { id: problemUUID,
 *           type: problemType,
 *           isUnlocked: false
 *         },
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
        "PMD.CommentSize", "PMD.NPathComplexity", "PMD.TooManyMethods" })
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
        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));

        // Make sure person is mod or admin for the assignment.
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.ASSIGNMENT, problem.getAssignmentId(), authId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For assignment: " + problem.getAssignmentId(), AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, problem.getCourseId()).append(ASSIGNMENT_ID, problem.getAssignmentId())
                .append(NAME, problem.getName());

        if (problem.hasGradeWeight()) {
            query.append(GRADE_WEIGHT, problem.getGradeWeight());
        }

        if (problem.hasProblemNumber()) {
            query.append(PROBLEM_NUMBER, problem.getProblemNumber());
        }

        query.append(DatabaseStringConstants.PROBLEM_LIST, createProblemHolderList(problem.getSubgroupsList()));

        courseProblemCollection.insert(query);
        final String selfId = query.get(SELF_ID).toString();

        // inserts the id into the previous the course
        AssignmentManager.mongoInsertProblemGroupIntoAssignment(dbs, problem.getAssignmentId(), selfId);

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

        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final DBObject cursor = courseProblemCollection.findOne(convertStringToObjectId(problemId));
        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemId, authId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For course problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        // Throws an exception if a user (only) is trying to get a course problem when the class is not in session.
        final Authentication.AuthType assignmentAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder assignmentResponder = authenticator
                .checkAuthentication(Util.ItemType.ASSIGNMENT, (String) cursor.get(ASSIGNMENT_ID), authId, checkTime, assignmentAuthType);

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
            final List<Problem.ProblemSlideHolder> problemSlideHolderList = createProblemSlideHolderList(authenticator, dbs, authId,
                    exactProblem.getCourseId(), checkTime,
                    (List<DBObject>) cursor.get(DatabaseStringConstants.PROBLEM_LIST));
            exactProblem.addAllSubgroups(problemSlideHolderList);
        }

        return exactProblem.build();
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
        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final DBObject cursor = courseProblemCollection.findOne(convertStringToObjectId(problemId));

        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID: " + problemId);
        }

        final BasicDBObject updateObj = new BasicDBObject();

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For courseProblem: " + problemId, AuthenticationException.INVALID_PERMISSION);
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
        // Only allows you to set them.
        if (problem.getSubgroupsCount() > 0) {
            updateObj.append(DatabaseStringConstants.PROBLEM_LIST, createProblemHolderList(problem.getSubgroupsList()));
            update = true;
        }
        if (update) {
            final BasicDBObject setData = new BasicDBObject();
            if (updateObj.size() > 0) {
                setData.append(SET_COMMAND, updateObj);
            }

            // We only replace the list right now instead of adding new objects.  There are other methods for that.
            /*
            if (problem.getSubgroupsCount() > 0) {
                final BasicDBObject problemList = new BasicDBObject(DatabaseStringConstants.PROBLEM_LIST,
                        createProblemHolderList(problem.getSubgroupsList()));
                setData.append(ADD_SET_COMMAND, problemList);
            }
            */

            courseProblemCollection.update(cursor, setData);
            UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), problemId, UserUpdateHandler.COURSE_PROBLEM_CLASSIFICATION);
        }
        return true;
    }

    /**
     * Creates a mongo object query for the list of {@link protobuf.srl.school.Problem.ProblemSlideHolder}.
     * @param holder
     *      The list of problems that are being inserted into the database.
     * @return
     *      A mongo object that represents the list passed in.
     * @throws DatabaseAccessException Thrown if there are problems creating the list.
     */
    private static DBObject createProblemHolderList(final List<Problem.ProblemSlideHolder> holder) throws DatabaseAccessException {
        final BasicDBList basicDBList = new BasicDBList();
        for (Problem.ProblemSlideHolder problemSlideHolder: holder) {
            try {
                basicDBList.add(createSlideProblemHolderQuery(problemSlideHolder.getId(),
                        problemSlideHolder.getItemType(), problemSlideHolder.getUnlocked()));
            } catch (DatabaseAccessException e) {
                throw new DatabaseAccessException("Invalid query for slide or problem: " + problemSlideHolder.getId(), e);
            }
        }
        return basicDBList;
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

        problem.setName((String) dbProblem.get(NAME));

        if (dbProblem.containsField(GRADE_WEIGHT)) {
            problem.setGradeWeight((String) dbProblem.get(GRADE_WEIGHT));
        }

        if (dbProblem.containsField(PROBLEM_NUMBER)) {
            problem.setProblemNumber((Integer) dbProblem.get(PROBLEM_NUMBER));
        }

        return problem;
    }

    /**
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param database
     *         The database where the assignment is being stored.
     * @param authId
     *         The user requesting the problem.
     * @param courseId
     *         Used to authenticate the course for the bank problem.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the assignment is valid)
     * @param slideObjects
     *         A list of Slide objects or problem objects that need to be decoded.
     * @return A protobuf representation of a problem slide holder.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    static List<Problem.ProblemSlideHolder> createProblemSlideHolderList(final Authenticator authenticator, final DB database, final String authId,
            final String courseId, final long checkTime, final List<DBObject> slideObjects) throws AuthenticationException, DatabaseAccessException {
        final List<Problem.ProblemSlideHolder> list = new ArrayList<>();
        for (int i = 0; i < slideObjects.size(); i++) {
            final DBObject dbObject = slideObjects.get(i);
            list.add(createProblemSlideHolder(authenticator, database, authId, courseId, checkTime, dbObject, i));
        }
        return list;
    }

    /**
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param database
     *         The database where the assignment is being stored.
     * @param authId
     *         The user requesting the problem.
     * @param courseId
     *         Used to authenticate the course for the bank problem.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the assignment is valid)
     * @param data
     *         The data from the database about a specific problem or slide.
     * @param index
     *         The location of the slide or problem in the list.
     * @return A protobuf representation of a problem slide holder.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    private static Problem.ProblemSlideHolder createProblemSlideHolder(final Authenticator authenticator, final DB database,
            final String authId, final String courseId, final long checkTime, final DBObject data, final int index)
            throws AuthenticationException, DatabaseAccessException {
        final Problem.ProblemSlideHolder.Builder holder = Problem.ProblemSlideHolder.newBuilder();
        holder.setIndex(index);

        if (!(Boolean) data.get(DatabaseStringConstants.IS_UNLOCKED)) {
            LOG.warn("The slide or problem is not unlocked");
            // Future: add the other conditions for checking if an item is unlocked
            holder.setUnlocked(false);
            return holder.build();
        }
        holder.setUnlocked(true);

        final String itemId = data.get(DatabaseStringConstants.ITEM_ID).toString();
        holder.setId(itemId);

        final Util.ItemType itemType = Util.ItemType.valueOf((int) data.get(DatabaseStringConstants.SCHOOL_ITEM_TYPE));
        holder.setItemType(itemType);
        switch (itemType) {
            case BANK_PROBLEM:
                final SrlBankProblem problem = BankProblemManager.mongoGetBankProblem(authenticator, database, courseId, itemId);
                holder.setProblem(problem);
                break;
            case SLIDE:
                final Lecturedata.LectureSlide lectureSlide = SlideManager
                        .mongoGetLectureSlide(authenticator, database, authId, itemId, checkTime);
                holder.setSlide(lectureSlide);
                break;
            default:
                throw new DatabaseAccessException("Invalid item type is present: " + itemType.name());
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
     *         The id of the problem that the data is being inserted into.
     * @param bankProblemId
     *         The id of the bank problem being inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the course problem.
     * @throws DatabaseAccessException
     *         Thrown if there is data that is missing.
     */
    public static void insertNewProblemPart(final Authenticator authenticator, final DB dbs, final String authId, final String problemId,
            final String bankProblemId) throws AuthenticationException, DatabaseAccessException {

        final DBCollection problemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final DBObject cursor = problemCollection.findOne(convertStringToObjectId(problemId));

        if (cursor == null) {
            throw new DatabaseAccessException("Course problem was not found with the following ID: " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For  problem: " + problemId, AuthenticationException.INVALID_PERMISSION);
        }

        mongoInsertBankProblemIntoProblemGroup(dbs, cursor.get(DatabaseStringConstants.ASSIGNMENT_ID).toString(),
                problemId,
                bankProblemId,
                true);

    }

    /**
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
    public static boolean mongoInsertSlideIntoSlideGroup(final Authenticator authenticator, final DB dbs, final String authId,
            final String assignmentId, final String problemGroupId, final String slideId, final boolean unlocked)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final DBObject cursor = courseProblemCollection.findOne(convertStringToObjectId(problemGroupId));

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemGroupId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBObject updateObj = new BasicDBObject(DatabaseStringConstants.PROBLEM_LIST,
                createSlideProblemHolderQuery(slideId, Util.ItemType.SLIDE, unlocked));
        courseProblemCollection.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

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
    private static boolean mongoInsertBankProblemIntoProblemGroup(final DB dbs, final String assignmentId, final String problemGroupId,
            final String bankProblemId, final boolean unlocked)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection courseProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.COURSE_PROBLEM));
        final DBObject cursor = courseProblemCollection.findOne(convertStringToObjectId(problemGroupId));

        final DBObject updateObj = new BasicDBObject(DatabaseStringConstants.PROBLEM_LIST,
                createSlideProblemHolderQuery(bankProblemId, Util.ItemType.BANK_PROBLEM, unlocked));
        courseProblemCollection.update(cursor, new BasicDBObject(ADD_SET_COMMAND, updateObj));

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
     * @param itemType
     *         the type of subitem it is.  Which can be either a slide or a bank problem.
     * @param isUnlocked
     *         a boolean that is true if the user trying to access this slide is allowed
     * @return a BasicDBObject of the message type IdInLecture
     * @throws DatabaseAccessException An invalid ItemType was attempted to be inserted.
     */
    private static BasicDBObject createSlideProblemHolderQuery(final String itemId, final Util.ItemType itemType, final boolean isUnlocked)
            throws DatabaseAccessException {
        if (itemType != Util.ItemType.SLIDE && itemType != Util.ItemType.BANK_PROBLEM) {
            throw new DatabaseAccessException("Attempting to create query with invalid item type: " + itemType.name());
        }
        return new BasicDBObject(DatabaseStringConstants.ITEM_ID, itemId)
                .append(DatabaseStringConstants.SCHOOL_ITEM_TYPE, itemType.getNumber())
                .append(IS_UNLOCKED, isUnlocked);

    }
}
