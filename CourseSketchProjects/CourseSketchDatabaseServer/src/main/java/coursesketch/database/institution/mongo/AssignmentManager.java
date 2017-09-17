package coursesketch.database.institution.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.MongoUtilities;
import coursesketch.database.util.RequestConverter;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.school.Assignment;
import protobuf.srl.school.Assignment.SrlAssignment;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;
import protobuf.srl.utils.Util.State;
import utilities.TimeManager;

import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ACCESS_DATE;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_CATEGORY;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_RESOURCES;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.CLOSE_DATE;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_ID;
import static coursesketch.database.util.DatabaseStringConstants.DESCRIPTION;
import static coursesketch.database.util.DatabaseStringConstants.DUE_DATE;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_WEIGHT;
import static coursesketch.database.util.DatabaseStringConstants.IMAGE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_RATE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.NAME;
import static coursesketch.database.util.DatabaseStringConstants.NAVIGATION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.PROBLEM_LIST;
import static coursesketch.database.util.DatabaseStringConstants.REVIEW_OPEN_DATE;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.SET_COMMAND;
import static coursesketch.database.util.DatabaseStringConstants.STATE_PUBLISHED;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;

/**
 * Manages assignments for mongo.
 *
 * In the mongo database, an assignment has the following structure.
 * <pre>
 * Assignment
 * {
 *     // IDs
 *     _id : ID,
 *     courseId: ID,
 *
 *     // Human information
 *     name: String,
 *     description: String,
 *
 *     // Display and navigation information
 *     assignmentType: AssignmentType,
 *     navigationType: NavigationType,
 *
 *     // Grading (these values being set override the grade policy)
 *     assignmentCategory: String,
 *     latePolicy: LatePolicy,
 *     gradeWeight: float,
 *
 *     // Date related
 *     accessDate: milliseconds,
 *     dueDate: milliseconds,
 *     closeDate: milliseconds,
 *     reviewOpenDate: milliseconds,  // zero means not reviewable (which is default)
 *
 *     published: boolean,
 *     problemGroups: [
 *         "groupUUid1",
 *         "groupUUid2",
 *     ],
 * }
 *
 * AssignmentTypes:
 *     FLASHCARD, (this means infinite questions in random order)
 *     LECTURE, (this means it will look like a lecture)
 *     GRADED, (this is the default students can not see any answers till the assignment is closed)
 *     PRACTICE (this allows students to see the correct answer immediately)
 *
 * NavigationTypes:
 *     DEFAULT, (chooses what the default type is for the specific assignmentType
 *     LOOPING, (this means the question order loops when the end is reached)
 *     NO_BACK_TRAVEL, (what it sounds like, as soon as they progress they can not navigate backwards [this one might require server navigation])
 *     RANDOM (pulls the next one at a random order)
 * </pre>
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.CommentSize", "PMD.UselessParentheses" })
public final class AssignmentManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AssignmentManager.class);

    /**
     * Private constructor.
     */
    private AssignmentManager() {
    }

    /**
     * Inserts an assignment into the mongo database.
     *
     * @param authenticator The object that is performing authenticaton.
     * @param dbs The database where the assignment is being stored.
     * @param authId The id of the user that asking to insert the assignment.
     * @param assignment The assignment that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws AuthenticationException Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException Thrown if there are problems inserting the assignment.
     */
    static String mongoInsertAssignment(final Authenticator authenticator, final MongoDatabase dbs, final String authId,
            final SrlAssignment assignment)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> assignmentCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, assignment.getCourseId().trim(), authId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For course: " + assignment.getCourseId(), AuthenticationException.INVALID_PERMISSION);
        }

        final Document query = new Document(COURSE_ID, assignment.getCourseId())
                .append(NAME, assignment.getName())
                .append(DESCRIPTION, assignment.getDescription())
                .append(STATE_PUBLISHED, false);

        setAssignmentTypeInformation(assignment, query, true);

        setDateInformation(assignment, query, true);

        setGradeInformation(assignment, query, true);

        if (assignment.getProblemGroupsList() != null) {
            query.append(PROBLEM_LIST, assignment.getProblemGroupsList());
        }

        assignmentCollection.insertOne(query);
        final String selfId = query.get(SELF_ID).toString();

        // Inserts the id into the parent course.
        CourseManager.mongoInsertAssignmentIntoCourse(dbs, assignment.getCourseId(), selfId);

        return selfId;
    }

    /**
     * Sets the date information for inserting an assignment into the database.
     *
     * Specifically sets accessDate, dueDate, closeDate and reviewOpenDate.  Sets defaults if the information does not already exist.
     *
     * @param assignment The assignment that contains the date information
     * @param query The object that the date information is being set into.
     * @param isInsertion true if the object is being inserted.  If false then defaults are not set and instead nothing is.
     * @return True if the query was modified, false otherwise.
     */
    private static boolean setDateInformation(final SrlAssignment assignment, final Document query, final boolean isInsertion) {
        if (assignment.hasAccessDate()) {
            query.append(ACCESS_DATE, assignment.getAccessDate().getMillisecond());
        } else if (isInsertion) {
            // The default is the current server time.
            query.append(ACCESS_DATE, TimeManager.getSystemTime());
        }

        // Sets a default date in the instance that a date was not given.
        if (assignment.hasDueDate()) {
            query.append(DUE_DATE, assignment.getDueDate().getMillisecond());
        } else if (isInsertion) {
            query.append(DUE_DATE, RequestConverter.getMaxTime());
        }

        // Sets a default date in the instance that a date was not given.
        if (assignment.hasCloseDate()) {
            query.append(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
        } else if (isInsertion) {
            query.append(CLOSE_DATE, RequestConverter.getMaxTime());
        }

        if (assignment.hasReviewOpenDate()) {
            query.append(REVIEW_OPEN_DATE, assignment.getReviewOpenDate().getMillisecond());
        } else if (isInsertion) {
            query.append(REVIEW_OPEN_DATE, -1);
        }

        return isInsertion || assignment.hasAccessDate() || assignment.hasDueDate() || assignment.hasCloseDate() || assignment.hasReviewOpenDate();
    }

    /**
     * Sets the grade information for inserting an assignment into the database.
     *
     * Does not set an information if it is already set.
     *
     * @param assignment The assignment that contains the date information
     * @param query The object that the date information is being set into.
     * @param isInsertion true if the object is being inserted.  If false then defaults are not set and instead nothing is.
     * @return True if the query was modified, false otherwise.
     */
    private static boolean setAssignmentTypeInformation(final SrlAssignment assignment, final Document query, final boolean isInsertion) {
        // Grade data
        if (assignment.hasAssignmentType()) {
            query.append(ASSIGNMENT_TYPE, assignment.getAssignmentType().getNumber());
        } else if (isInsertion) {
            // The default is the current server time.
            query.append(ASSIGNMENT_TYPE, Assignment.AssignmentType.GRADED.getNumber());
        }

        // Sets a default date in the instance that a date was not given.
        if (assignment.hasNavigationType()) {
            query.append(NAVIGATION_TYPE, assignment.getNavigationType().getNumber());
        } else if (isInsertion) {
            query.append(NAVIGATION_TYPE, Assignment.NavigationType.DEFAULT.getNumber());
        }

        return isInsertion || assignment.hasAssignmentType() || assignment.hasNavigationType();
    }

    /**
     * Sets the grade information for inserting an assignment into the database.
     *
     * Does not set an information if it is already set.
     *
     * @param assignment The assignment that contains the date information
     * @param query The object that the date information is being set into.
     * @param isInsertion true if the object is being inserted.  If false then defaults are not set and instead nothing is.
     * @return True if the query was modified, false otherwise.
     */
    private static boolean setGradeInformation(final SrlAssignment assignment, final Document query, final boolean isInsertion) {
        // Grade data
        if (assignment.hasLatePolicy()) {
            query.append(LATE_POLICY_FUNCTION_TYPE, assignment.getLatePolicy().getFunctionType().getNumber())
                    .append(LATE_POLICY_RATE, assignment.getLatePolicy().getRate())
                    .append(LATE_POLICY_SUBTRACTION_TYPE, assignment.getLatePolicy().getSubtractionType().getNumber());

            query.append(LATE_POLICY_TIME_FRAME_TYPE, assignment.getLatePolicy().getTimeFrameType().getNumber());
        }

        if (assignment.hasGradeWeight()) {
            query.append(GRADE_WEIGHT, assignment.getGradeWeight());
        }

        // The default is homework.
        if (assignment.hasAssignmentCatagory()) {
            query.append(ASSIGNMENT_CATEGORY, assignment.getAssignmentCatagory());
        } else if (isInsertion) {
            query.append(ASSIGNMENT_CATEGORY, DatabaseStringConstants.HOMEWORK_CATEGORY);
        }

        return isInsertion || assignment.hasLatePolicy() || assignment.hasGradeWeight() || assignment.hasAssignmentCatagory();
    }

    /**
     * Grabs the assignment from mongo and performs checks making sure the user is valid before returning the assignment.
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database where the assignment is being stored.
     * @param authId The id of the user that asking to insert the assignment.
     * @param assignmentId The id of the assignment that is being grabbed.
     * @param checkTime The time that the assignment was asked to be grabbed. (used to
     * check if the assignment is valid)
     * @return The assignment from the database.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the
     * assignment.
     * @throws DatabaseAccessException Thrown if there are problems retrieving the assignment.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    static SrlAssignment mongoGetAssignment(final Authenticator authenticator, final MongoDatabase dbs, final String authId,
            final String assignmentId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> assignmentCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document cursor = assignmentCollection.find(convertStringToObjectId(assignmentId)).first();
        if (cursor == null) {
            throw new DatabaseAccessException("Assignment was not found with the following ID " + assignmentId, true);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.ASSIGNMENT, assignmentId, authId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For assignment: " + assignmentId, AuthenticationException.INVALID_PERMISSION);
        }

        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build();
        final AuthenticationResponder courseResponder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, (String) cursor.get(COURSE_ID), authId, checkTime, courseAuthType);

        // Throws an exception if a user (only) is trying to get an assignment when the class is not in session.
        if (responder.hasAccess() && !responder.hasPeerTeacherPermission() && !courseResponder.isItemOpen()) {
            throw new AuthenticationException("For assignment: " + assignmentId, AuthenticationException.INVALID_DATE);
        }

        final State.Builder stateBuilder = State.newBuilder();
        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met
        if (!responder.isItemPublished() && !responder.hasModeratorPermission()) {
            throw new DatabaseAccessException("The specific assignment is not published yet: " + assignmentId, true);
        }

        // Post this point either item is published OR responder is at least responder.
        stateBuilder.setPublished(responder.isItemPublished());

        // now all possible exceptions have already been thrown.
        final SrlAssignment.Builder exactAssignment = SrlAssignment.newBuilder();

        exactAssignment.setId(assignmentId);

        // sets the majority of the assignment data
        setAssignmentData(exactAssignment, cursor);

        setAssignmentStateAndDate(exactAssignment, stateBuilder, cursor,
                responder.hasTeacherPermission(), responder.hasModeratorPermission(), checkTime);

        if (cursor.get(IMAGE) != null) {
            exactAssignment.setImageUrl((String) cursor.get(IMAGE));
        }

        // if you are a user, the assignment must be open to view the problems
        if (responder.hasPeerTeacherPermission() || (responder.hasAccess()
                && responder.isItemOpen())) {
            if (cursor.get(PROBLEM_LIST) != null) {
                List<String> problemList = MongoUtilities.getNonNullList(cursor, PROBLEM_LIST);
                exactAssignment.addAllProblemGroups(problemList);
            }
            stateBuilder.setAccessible(true);
        } else if (responder.hasAccess() && !responder.isItemOpen()) {
            stateBuilder.setAccessible(false);
            LOG.info("USER ASSIGNMENT TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            LOG.info("TIME OPEN: {} \n CURRENT TIME: {} \n TIME CLOSED: {} \n", exactAssignment.getAccessDate().getMillisecond(), checkTime,
                    exactAssignment.getCloseDate().getMillisecond());
            stateBuilder.setAccessible(false);
        }

        exactAssignment.setState(stateBuilder);

        return exactAssignment.build();
    }

    /**
     * Sets data of the assignment from the given cursor.
     *
     * @param exactAssignment The assignment that the data is being set to.
     * @param cursor The database cursor pointing to a specific assignment.
     */
    private static void setAssignmentData(final SrlAssignment.Builder exactAssignment, final Document cursor) {
        exactAssignment.setCourseId((String) cursor.get(COURSE_ID));
        exactAssignment.setName((String) cursor.get(NAME));
        if (cursor.containsKey(ASSIGNMENT_TYPE)) {
            exactAssignment.setAssignmentType(Assignment.AssignmentType.valueOf((Integer) cursor.get(ASSIGNMENT_TYPE)));
        }
        if (cursor.containsKey(NAVIGATION_TYPE)) {
            exactAssignment.setNavigationType(Assignment.NavigationType.valueOf((Integer) cursor.get(NAVIGATION_TYPE)));
        }
        if (cursor.containsKey(ASSIGNMENT_CATEGORY)) {
            exactAssignment.setAssignmentCatagory((String) cursor.get(ASSIGNMENT_CATEGORY));
        } else {
            exactAssignment.setAssignmentCatagory(DatabaseStringConstants.HOMEWORK_CATEGORY);
        }
        exactAssignment.setDescription((String) cursor.get(DESCRIPTION));
        if (cursor.containsKey(GRADE_WEIGHT)) {
            exactAssignment.setGradeWeight((String) cursor.get(GRADE_WEIGHT));
        }
    }

    /**
     * Sets data about the state of the assignment and its date.
     *
     * @param exactAssignment A protobuf assignment builder.
     * @param stateBuilder A protobuf state builder.
     * @param cursor The current database pointer for the assignment.
     * @param isAdmin True if the user is acting as an admin.
     * @param isMod True if the user is acting as a moderator.
     * @param checkTime The time that the check was performed.
     */
    private static void setAssignmentStateAndDate(final SrlAssignment.Builder exactAssignment, final State.Builder stateBuilder,
            final Document cursor, final boolean isAdmin, final boolean isMod, final long checkTime) {
        if (isAdmin || isMod) {
            final LatePolicy.Builder latePolicy = LatePolicy.newBuilder();
            if (cursor.get(LATE_POLICY_FUNCTION_TYPE) == null) {
                latePolicy.setFunctionType(LatePolicy.FunctionType.STEPPING_FUNCTION);
            } else {
                latePolicy.setFunctionType(LatePolicy.FunctionType.valueOf((Integer) cursor.get(LATE_POLICY_FUNCTION_TYPE)));
            }

            if (cursor.get(LATE_POLICY_RATE) == null) {
                latePolicy.setRate(1.0F);
            } else {
                latePolicy.setRate(Float.parseFloat("" + cursor.get(LATE_POLICY_RATE)));
            }

            if (cursor.get(LATE_POLICY_SUBTRACTION_TYPE) == null) {
                latePolicy.setSubtractionType(LatePolicy.SubtractionType.CAP);
            } else {
                try {
                    final Object subType = cursor.get(LATE_POLICY_SUBTRACTION_TYPE);
                    if (subType != null) {
                        final boolean subtractionType = (Boolean) subType; // true is cap score.
                        if (subtractionType) {
                            latePolicy.setSubtractionType(LatePolicy.SubtractionType.CAP);
                        } else {
                            latePolicy.setSubtractionType(LatePolicy.SubtractionType.PERCENT);
                        }
                    } else {
                        latePolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((Integer) cursor.get(LATE_POLICY_SUBTRACTION_TYPE)));
                    }
                } catch (ClassCastException e) {
                    latePolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((Integer) cursor.get(LATE_POLICY_SUBTRACTION_TYPE)));
                }
            }

            if (latePolicy.getFunctionType() != LatePolicy.FunctionType.EXPONENTIAL) {
                if (cursor.get(LATE_POLICY_TIME_FRAME_TYPE) == null) {
                    latePolicy.setTimeFrameType(LatePolicy.TimeFrame.DAY);
                } else {
                    latePolicy.setTimeFrameType(LatePolicy.TimeFrame.valueOf((Integer) cursor.get(LATE_POLICY_TIME_FRAME_TYPE)));
                }
            } else {
                latePolicy.setTimeFrameType(LatePolicy.TimeFrame.CONSTANT);
            }
        }

        exactAssignment.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(ACCESS_DATE)).longValue()));
        exactAssignment.setDueDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(DUE_DATE)).longValue()));
        exactAssignment.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(CLOSE_DATE)).longValue()));
        exactAssignment.setReviewOpenDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(REVIEW_OPEN_DATE)).longValue()));

        if (exactAssignment.getDueDate().getMillisecond() > checkTime) {
            stateBuilder.setPastDue(true);
        }
    }

    /**
     * Updates data from an assignment.
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database where the assignment is being stored.
     * @param authId The id of the user that asking to insert the assignment.
     * @param assignmentId The id of the assignment that is being updated.
     * @param assignment The assignment that is being inserted.
     * @throws AuthenticationException The user does not have permission to update the assignment.
     * @throws DatabaseAccessException The assignment does not exist.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    static void mongoUpdateAssignment(final Authenticator authenticator, final MongoDatabase dbs, final String authId,
            final String assignmentId,
            final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final MongoCollection<Document> assignmentCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document cursor = assignmentCollection.find(convertStringToObjectId(assignmentId)).first();

        if (cursor == null) {
            throw new DatabaseAccessException("Assignment was not found with the following ID: " + assignmentId, true);
        }

        final Document updateQuery = new Document();

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.ASSIGNMENT, assignmentId, authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For assignment: " + assignmentId, AuthenticationException.INVALID_PERMISSION);
        }

        if (responder.hasModeratorPermission()) {
            if (assignment.hasName()) {
                updateQuery.append(NAME, assignment.getName());
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasDescription()) {
                updateQuery.append(DESCRIPTION, assignment.getDescription());
                update = true;
            }

            if (assignment.getLinksList() != null) {
                updateQuery.append(ASSIGNMENT_RESOURCES, assignment.getLinksList());
                update = true;
            }

            if (assignment.hasState() && assignment.getState().hasPublished() && assignment.getState().getPublished() && isPublishable(assignment)) {
                // Can not be set to false.
                updateQuery.append(STATE_PUBLISHED, true);
                update = true;
            }

            update |= setAssignmentTypeInformation(assignment, updateQuery, false);
            update |= setDateInformation(assignment, updateQuery, false);
            update |= setGradeInformation(assignment, updateQuery, false);
        }
        if (update) {
            assignmentCollection.updateOne(cursor, new Document(SET_COMMAND, updateQuery));
        }
    }

    /**
     * Checks for various values to see if the assignment is publishable.
     *
     * @param assignment The assignment we are checking to see if it is publishable.
     * @return True if it is publishable.
     */
    private static boolean isPublishable(final SrlAssignment assignment) {
        LOG.debug("{}", assignment);
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * With that being said this allows an assignment to be updated adding the
     * problemId to its list of items.
     *
     * @param dbs The database where the assignment is stored.
     * @param assignmentId The assignment that the problem is being added to.
     * @param problemId The id of the course problem that is being added to the assignment.
     * @throws AuthenticationException The user does not have permission to update the assignment.
     * @throws DatabaseAccessException The assignment does not exist.
     */
    static void mongoInsertProblemGroupIntoAssignment(final MongoDatabase dbs, final String assignmentId, final String problemId)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> assignmentCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.ASSIGNMENT));
        final Document cursor = assignmentCollection.find(convertStringToObjectId(assignmentId)).first();

        final Document updateObj = new Document(PROBLEM_LIST, problemId);
        assignmentCollection.updateOne(cursor, new Document("$addToSet", updateObj));
    }
}
