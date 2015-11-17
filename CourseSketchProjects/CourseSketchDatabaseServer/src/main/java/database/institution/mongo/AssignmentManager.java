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
import protobuf.srl.school.School;
import protobuf.srl.school.School.LatePolicy;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.State;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.SrlPermission;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.ASSIGNMENT_COLLECTION;
import static database.DatabaseStringConstants.ASSIGNMENT_OTHER_TYPE;
import static database.DatabaseStringConstants.ASSIGNMENT_RESOURCES;
import static database.DatabaseStringConstants.ASSIGNMENT_TYPE;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.DUE_DATE;
import static database.DatabaseStringConstants.GRADE_WEIGHT;
import static database.DatabaseStringConstants.IMAGE;
import static database.DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_RATE;
import static database.DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PERMISSION_LEVELS;
import static database.DatabaseStringConstants.PROBLEM_LIST;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.USERS;
import static database.utilities.MongoUtilities.createId;

/**
 * Manages assignments for mongo.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.UselessParentheses" })
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
     * @param authenticator
     *         The object that is performing authenticaton.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param userId
     *         The id of the user that asking to insert the assignment.
     * @param assignment
     *         The assignment that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException
     *         Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertAssignment(final Authenticator authenticator, final DB dbs, final String userId, final SrlAssignment assignment)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection assignmentCollection = dbs.getCollection(ASSIGNMENT_COLLECTION);
        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE, assignment.getCourseId().trim(), userId, 0, courseAuthType);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For course: " + assignment.getCourseId(), AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, assignment.getCourseId()).append(NAME, assignment.getName())
                .append(ASSIGNMENT_TYPE, assignment.getAssignmentType().getNumber()).append(ASSIGNMENT_OTHER_TYPE, assignment.getOther())
                .append(DESCRIPTION, assignment.getDescription()).append(ASSIGNMENT_RESOURCES, assignment.getLinksList())
                .append(GRADE_WEIGHT, assignment.getGradeWeight()).append(ACCESS_DATE, assignment.getAccessDate().getMillisecond())
                .append(DUE_DATE, assignment.getDueDate().getMillisecond())
                .append(IMAGE, assignment.getImageUrl()).append(ADMIN, assignment.getAccessPermission().getAdminPermissionList())
                .append(MOD, assignment.getAccessPermission().getModeratorPermissionList())
                .append(USERS, assignment.getAccessPermission().getUserPermissionList());

        // Sets a default date in the instance that a date was not given.
        if (!assignment.hasCloseDate()) {
            query.append(CLOSE_DATE, RequestConverter.getMaxTime());
        } else {
            query.append(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
        }
        if (assignment.hasLatePolicy()) {
            query.append(LATE_POLICY_FUNCTION_TYPE, assignment.getLatePolicy().getFunctionType().getNumber())
                    .append(LATE_POLICY_RATE, assignment.getLatePolicy().getRate())
                    .append(LATE_POLICY_SUBTRACTION_TYPE, assignment.getLatePolicy().getSubtractionType().getNumber());

            query.append(LATE_POLICY_TIME_FRAME_TYPE, assignment.getLatePolicy().getTimeFrameType().getNumber());
        }
        if (assignment.getProblemListList() != null) {
            query.append(PROBLEM_LIST, assignment.getProblemListList());
        }
        assignmentCollection.insert(query);
        final DBObject cursor = assignmentCollection.findOne(query);

        // inserts the id into the previous the course
        CourseManager.mongoInsertAssignmentIntoCourse(dbs, assignment.getCourseId(), cursor.get(SELF_ID).toString());

        return cursor.get(SELF_ID).toString();
    }

    /**
     * Grabs the assignment from mongo and performs checks making sure the user is valid before returning the assignment.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param assignmentId
     *         The id of the assignment that is being grabbed.
     * @param userId
     *         The id of the user that asking to insert the assignment.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the assignment is valid)
     * @return The assignment from the database.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the
     *         assignment.
     * @throws DatabaseAccessException
     *         Thrown if there are problems retrieving the assignment.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    public static SrlAssignment mongoGetAssignment(final Authenticator authenticator, final DB dbs, final String assignmentId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, createId(assignmentId));
        final DBObject cursor = myDbRef.fetch();
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
                .checkAuthentication(School.ItemType.ASSIGNMENT, assignmentId, userId, checkTime, authType);

        if (!responder.hasAccess()) {
            throw new AuthenticationException("For assignment: " + assignmentId, AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the assignment is within the time period that the
        // course is open and the user is in the course
        // FUTURE: maybe not make this necessarry if the insertion of assignments prevents this.

        final Authentication.AuthType courseAuthType = Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build();
        final AuthenticationResponder courseResponder = authenticator
                .checkAuthentication(School.ItemType.COURSE, (String) cursor.get(COURSE_ID), userId, checkTime, courseAuthType);

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
                exactAssignment.addAllProblemList((List) cursor.get(PROBLEM_LIST));
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

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (responder.hasTeacherPermission()) {
            permissions.addAllAdminPermission((ArrayList) cursor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) cursor.get(MOD)); // admin
        }
        if (responder.hasModeratorPermission()) {
            permissions.addAllUserPermission((ArrayList) cursor.get(USERS)); // mod
            exactAssignment.setAccessPermission(permissions.build());
        }
        return exactAssignment.build();
    }

    /**
     * Sets data of the assignment from the given cursor.
     *
     * @param exactAssignment
     *         The assignment that the data is being set to.
     * @param corsor
     *         The database cursor pointing to a specific assignment.
     */
    private static void setAssignmentData(final SrlAssignment.Builder exactAssignment, final DBObject corsor) {
        exactAssignment.setCourseId((String) corsor.get(COURSE_ID));
        exactAssignment.setName((String) corsor.get(NAME));
        exactAssignment.setAssignmentType(SrlAssignment.AssignmentType.valueOf((Integer) corsor.get(ASSIGNMENT_TYPE)));
        exactAssignment.setOther((String) corsor.get(ASSIGNMENT_OTHER_TYPE));
        exactAssignment.setDescription((String) corsor.get(DESCRIPTION));
        exactAssignment.addAllLinks((List) corsor.get(ASSIGNMENT_RESOURCES));
        exactAssignment.setGradeWeight((String) corsor.get(GRADE_WEIGHT));
    }

    /**
     * Sets data about the state of the assignment and its date.
     *
     * @param exactAssignment
     *         A protobuf assignment builder.
     * @param stateBuilder
     *         A protobuf state builder.
     * @param corsor
     *         The current database pointer for the assignment.
     * @param isAdmin
     *         True if the user is acting as an admin.
     * @param isMod
     *         True if the user is acting as a moderator.
     * @param checkTime
     *         The time that the check was performed.
     */
    private static void setAssignmentStateAndDate(final SrlAssignment.Builder exactAssignment, final State.Builder stateBuilder,
            final DBObject corsor, final boolean isAdmin, final boolean isMod, final long checkTime) {
        if (isAdmin || isMod) {
            final LatePolicy.Builder latePolicy = LatePolicy.newBuilder();
            if (corsor.get(LATE_POLICY_FUNCTION_TYPE) == null) {
                latePolicy.setFunctionType(LatePolicy.FunctionType.STEPPING_FUNCTION);
            } else {
                latePolicy.setFunctionType(LatePolicy.FunctionType.valueOf((Integer) corsor.get(LATE_POLICY_FUNCTION_TYPE)));
            }

            if (corsor.get(LATE_POLICY_RATE) == null) {
                latePolicy.setRate(1.0F);
            } else {
                latePolicy.setRate(Float.parseFloat("" + corsor.get(LATE_POLICY_RATE)));
            }

            if (corsor.get(LATE_POLICY_SUBTRACTION_TYPE) == null) {
                latePolicy.setSubtractionType(LatePolicy.SubtractionType.CAP);
            } else {
                try {
                    final Object subType = corsor.get(LATE_POLICY_SUBTRACTION_TYPE);
                    if (subType != null) {
                        final boolean subtractionType = (Boolean) subType; // true is cap score.
                        if (subtractionType) {
                            latePolicy.setSubtractionType(LatePolicy.SubtractionType.CAP);
                        } else {
                            latePolicy.setSubtractionType(LatePolicy.SubtractionType.PERCENT);
                        }
                    } else {
                        latePolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((Integer) corsor.get(LATE_POLICY_SUBTRACTION_TYPE)));
                    }
                } catch (ClassCastException e) {
                    latePolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((Integer) corsor.get(LATE_POLICY_SUBTRACTION_TYPE)));
                }
            }

            if (latePolicy.getFunctionType() != LatePolicy.FunctionType.EXPONENTIAL) {
                if (corsor.get(LATE_POLICY_TIME_FRAME_TYPE) == null) {
                    latePolicy.setTimeFrameType(LatePolicy.TimeFrame.DAY);
                } else {
                    latePolicy.setTimeFrameType(LatePolicy.TimeFrame.valueOf((Integer) corsor.get(LATE_POLICY_TIME_FRAME_TYPE)));
                }
            } else {
                latePolicy.setTimeFrameType(LatePolicy.TimeFrame.CONSTANT);
            }
        }

        exactAssignment.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(ACCESS_DATE)).longValue()));
        exactAssignment.setDueDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(DUE_DATE)).longValue()));
        exactAssignment.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(CLOSE_DATE)).longValue()));

        if (exactAssignment.getDueDate().getMillisecond() > checkTime) {
            stateBuilder.setPastDue(true);
        }
    }

    /**
     * Updates data from an assignment.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param assignmentId
     *         The id of the assignment that is being updated.
     * @param userId
     *         The id of the user that asking to insert the assignment.
     * @param assignment
     *         The assignment that is being inserted.
     * @return true if the assignment was updated successfully.
     * @throws AuthenticationException
     *         The user does not have permission to update the assignment.
     * @throws DatabaseAccessException
     *         The assignment does not exist.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static boolean mongoUpdateAssignment(final Authenticator authenticator, final DB dbs, final String assignmentId, final String userId,
            final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject cursor = myDbRef.fetch();

        if (cursor == null) {
            throw new DatabaseAccessException("Assignment was not found with the following ID: " + assignmentId, true);
        }

        final DBCollection assignmentCollection = dbs.getCollection(ASSIGNMENT_COLLECTION);

        final BasicDBObject updateObj = new BasicDBObject();

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, assignmentId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("For assignment: " + assignmentId, AuthenticationException.INVALID_PERMISSION);
        }

        if (responder.hasModeratorPermission()) {
            if (assignment.hasName()) {
                updateObj.append(NAME, assignment.getName());
                update = true;
            }

            if (assignment.hasAssignmentType()) {
                update = true;
                updateObj.append(ASSIGNMENT_TYPE, assignment.getAssignmentType().getNumber());
            }
            if (assignment.hasOther()) {
                updateObj.append(ASSIGNMENT_OTHER_TYPE, assignment.getOther());
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasDescription()) {
                updateObj.append(DESCRIPTION, assignment.getDescription());
                update = true;
            }
            if (assignment.getLinksList() != null) {
                updateObj.append(ASSIGNMENT_RESOURCES, assignment.getLinksList());
                update = true;
            }
            if (assignment.hasLatePolicy()) {
                throw new DatabaseAccessException("Late policy does not exist in the database!");
                /*
                 updateObj = new BasicDBObject(LATE_POLICY,
                 assignment.getLatePolicy().getNumber());
                 courses.update(cursor, new BasicDBObject (SET_COMMAND,
                 updateObj));
                 */
            }
            if (assignment.hasGradeWeight()) {
                updateObj.append(GRADE_WEIGHT, assignment.getGradeWeight());
                update = true;
            }
            if (assignment.hasAccessDate()) {
                updateObj.append(ACCESS_DATE, assignment.getAccessDate().getMillisecond());
                update = true;
            }
            if (assignment.hasDueDate()) {
                updateObj.append(DUE_DATE, assignment.getDueDate().getMillisecond());
                update = true;
            }
            if (assignment.hasCloseDate()) {
                updateObj.append(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
                update = true;
            }
            if (assignment.hasImageUrl()) {
                updateObj.append(IMAGE, assignment.getImageUrl());
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasAccessPermission()) {
                final SrlPermission permissions = assignment.getAccessPermission();
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
            assignmentCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
            UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), assignmentId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
        }
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * With that being said this allows an assignment to be updated adding the
     * problemId to its list of items.
     *
     * @param dbs
     *         The database where the assignment is stored.
     * @param assignmentId
     *         The assignment that the problem is being added to.
     * @param problemId
     *         The id of the course problem that is being added to the assignment.
     * @return True if it is successful.
     * @throws AuthenticationException The user does not have permission to update the assignment.
     * @throws DatabaseAccessException The assignment does not exist.
     */
    static boolean mongoInsert(final DB dbs, final String assignmentId, final String problemId)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, createId(assignmentId));

        final DBObject cursor = myDbRef.fetch();

        final DBCollection courses = dbs.getCollection(ASSIGNMENT_COLLECTION);
        final DBObject updateObj = new BasicDBObject(PROBLEM_LIST, problemId);
        courses.update(cursor, new BasicDBObject("$addToSet", updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) cursor.get(USERS)), assignmentId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * This is used to copy permissions from the parent course into the current
     * assignment.
     *
     * @param dbs
     *         The database where the data is stored.
     * @param assignmentId
     *         The id of the assignment that is getting permissions.
     * @param ids
     *         The list of list of permissions that is getting added.
     */
    // package-private
    static void mongoInsertDefaultGroupId(final DB dbs, final String assignmentId, final List<String>... ids) {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection assignments = dbs.getCollection(ASSIGNMENT_COLLECTION);

        final BasicDBObject updateQuery = new BasicDBObject(); // MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        LOG.info("Updated Query: ", updateQuery);
        assignments.update(corsor, updateQuery);
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * Returns a list of Id for the default group for an assignment.
     *
     * The Ids are ordered as so: AdminGroup, ModGroup, UserGroup.
     *
     * @param dbs
     *         The database where the ids are stored.
     * @param assignmentId
     *         The id of the assignment that contains the ids.
     * @return A list of id groups.
     */
    static List<String>[] mongoGetDefaultGroupId(final DB dbs, final String assignmentId) {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        final ArrayList<String>[] returnValue = new ArrayList[PERMISSION_LEVELS];
        returnValue[0] = (ArrayList) corsor.get(ADMIN);
        returnValue[1] = (ArrayList) corsor.get(MOD);
        returnValue[2] = (ArrayList) corsor.get(USERS);
        return returnValue;
    }
}
