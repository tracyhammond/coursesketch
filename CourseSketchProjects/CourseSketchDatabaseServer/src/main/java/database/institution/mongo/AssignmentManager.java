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
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.LatePolicy;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.State;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.ASSIGNMENT_COLLECTION;
import static database.DatabaseStringConstants.ASSIGNMENT_OTHER_TYPE;
import static database.DatabaseStringConstants.ASSIGNMENT_RESOURCES;
import static database.DatabaseStringConstants.ASSIGNMENT_TYPE;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
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
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.USERS;

/**
 * Manages assignments for mongo.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.UselessParentheses" })
public final class AssignmentManager {

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
        final DBCollection newUser = dbs.getCollection(ASSIGNMENT_COLLECTION);
        final AuthType auth = new AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, assignment.getCourseId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject query = new BasicDBObject(COURSE_ID, assignment.getCourseId()).append(NAME, assignment.getName())
                .append(ASSIGNMENT_TYPE, assignment.getType().getNumber()).append(ASSIGNMENT_OTHER_TYPE, assignment.getOther())
                .append(DESCRIPTION, assignment.getDescription()).append(ASSIGNMENT_RESOURCES, assignment.getLinksList())
                .append(GRADE_WEIGHT, assignment.getGradeWeight()).append(ACCESS_DATE, assignment.getAccessDate().getMillisecond())
                .append(DUE_DATE, assignment.getDueDate().getMillisecond()).append(CLOSE_DATE, assignment.getCloseDate().getMillisecond())
                .append(IMAGE, assignment.getImageUrl()).append(ADMIN, assignment.getAccessPermission().getAdminPermissionList())
                .append(MOD, assignment.getAccessPermission().getModeratorPermissionList())
                .append(USERS, assignment.getAccessPermission().getUserPermissionList());
        if (assignment.hasLatePolicy()) {
            query.append(LATE_POLICY_FUNCTION_TYPE, assignment.getLatePolicy().getFunctionType().getNumber())
                    .append(LATE_POLICY_RATE, assignment.getLatePolicy().getRate())
                    .append(LATE_POLICY_SUBTRACTION_TYPE, assignment.getLatePolicy().getSubtractionType().getNumber());

            query.append(LATE_POLICY_TIME_FRAME_TYPE, assignment.getLatePolicy().getTimeFrameType().getNumber());
        }
        if (assignment.getProblemListList() != null) {
            query.append(PROBLEM_LIST, assignment.getProblemListList());
        }
        newUser.insert(query);
        final DBObject corsor = newUser.findOne(query);

        // inserts the id into the previous the course
        CourseManager.mongoInsertAssignmentIntoCourse(dbs, assignment.getCourseId(), corsor.get(SELF_ID).toString());

        return corsor.get(SELF_ID).toString();
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
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Assignment was not found with the following ID " + assignmentId, true);
        }

        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (List<String>) corsor.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (List<String>) corsor.get(MOD));
        isUsers = authenticator.checkAuthentication(userId, (List<String>) corsor.get(USERS));

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the assignment is within the time period that the
        // course is open and the user is in the course
        // FUTURE: maybe not make this necessarry if the insertion of assignments prevents this.
        final AuthType auth = new AuthType();
        auth.setCheckDate(true);
        auth.setUser(true);

        // Throws an exception if a user (only) is trying to get an assignment when the class is not in session.
        if (isUsers && !isAdmin && !isMod && !authenticator
                .isAuthenticated(COURSE_COLLECTION, (String) corsor.get(COURSE_ID), userId, checkTime, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_DATE);
        }

        final State.Builder stateBuilder = State.newBuilder();
        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
            if (published) {
                stateBuilder.setPublished(true);
            } else {
                if (!isAdmin || !isMod) {
                    throw new DatabaseAccessException("The specific assignment is not published yet", true);
                }
                stateBuilder.setPublished(false);
            }
        }

        // now all possible exceptions have already been thrown.
        final SrlAssignment.Builder exactAssignment = SrlAssignment.newBuilder();

        exactAssignment.setId(assignmentId);

        // sets the majority of the assignment data
        setAssignmentData(exactAssignment, corsor);

        setAssignmentStateAndDate(exactAssignment, stateBuilder, corsor, isAdmin, isMod, checkTime);

        if (corsor.get(IMAGE) != null) {
            exactAssignment.setImageUrl((String) corsor.get(IMAGE));
        }

        // if you are a user, the assignment must be open to view the problems
        if (isAdmin || isMod || (isUsers
                && Authenticator.isTimeValid(checkTime, exactAssignment.getAccessDate(), exactAssignment.getCloseDate()))) {
            if (corsor.get(PROBLEM_LIST) != null) {
                exactAssignment.addAllProblemList((List) corsor.get(PROBLEM_LIST));
            }

            stateBuilder.setAccessible(true);
        } else if (isUsers && !Authenticator.isTimeValid(checkTime, exactAssignment.getAccessDate(), exactAssignment.getCloseDate())) {
            stateBuilder.setAccessible(false);
            System.err.println("USER ASSIGNMENT TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            System.err.println(exactAssignment.getAccessDate().getMillisecond() + " < " + checkTime + " < "
                    + exactAssignment.getCloseDate().getMillisecond());
            stateBuilder.setAccessible(false);
        }

        exactAssignment.setState(stateBuilder);

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (isAdmin) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
        }
        if (isAdmin || isMod) {
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // mod
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
        exactAssignment.setType(SrlAssignment.AssignmentType.valueOf((Integer) corsor.get(ASSIGNMENT_TYPE)));
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
        final DBObject corsor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection assignmentCollection = dbs.getCollection(ASSIGNMENT_COLLECTION);

        final ArrayList adminList = (ArrayList<Object>) corsor.get("Admin");
        final ArrayList modList = (ArrayList<Object>) corsor.get("Mod");
        boolean isAdmin, isMod;
        isAdmin = authenticator.checkAuthentication(userId, adminList);
        isMod = authenticator.checkAuthentication(userId, modList);

        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin || isMod) {
            if (assignment.hasName()) {
                updateObj = new BasicDBObject(NAME, assignment.getName());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasType()) {
                updateObj = new BasicDBObject(ASSIGNMENT_TYPE, assignment.getType().getNumber());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasOther()) {
                updateObj = new BasicDBObject(ASSIGNMENT_OTHER_TYPE, assignment.getOther());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasDescription()) {
                updateObj = new BasicDBObject(DESCRIPTION, assignment.getDescription());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.getLinksList() != null) {
                updateObj = new BasicDBObject(ASSIGNMENT_RESOURCES, assignment.getLinksList());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasLatePolicy()) {
                throw new DatabaseAccessException("Late policy does not exist in the database!");
                /*
                 updateObj = new BasicDBObject(LATE_POLICY,
                 assignment.getLatePolicy().getNumber());
                 courses.update(corsor, new BasicDBObject (SET_COMMAND,
                 updateObj));
                 */
            }
            if (assignment.hasGradeWeight()) {
                updateObj = new BasicDBObject(GRADE_WEIGHT, assignment.getGradeWeight());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasAccessDate()) {
                updateObj = new BasicDBObject(ACCESS_DATE, assignment.getAccessDate().getMillisecond());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasDueDate()) {
                updateObj = new BasicDBObject(DUE_DATE, assignment.getDueDate().getMillisecond());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasCloseDate()) {
                updateObj = new BasicDBObject(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.hasImageUrl()) {
                updateObj = new BasicDBObject(IMAGE, assignment.getImageUrl());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }
            if (assignment.getProblemListCount() > 0) {
                updateObj = new BasicDBObject(PROBLEM_LIST, assignment.getProblemListList());
                assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasAccessPermission()) {
                final SrlPermission permissions = assignment.getAccessPermission();
                if (isAdmin) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updated.append(SET_COMMAND, new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
                        updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
                        assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
                        assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
                    assignmentCollection.update(corsor, new BasicDBObject(SET_COMMAND, updateObj));
                }
            }
        }
        if (update) {
            UserUpdateHandler.insertUpdates(dbs, ((List) corsor.get(USERS)), assignmentId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
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
     */
    static boolean mongoInsert(final DB dbs, final String assignmentId, final String problemId) {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));

        final DBObject corsor = myDbRef.fetch();

        final DBCollection courses = dbs.getCollection(ASSIGNMENT_COLLECTION);
        final DBObject updateObj = new BasicDBObject(PROBLEM_LIST, problemId);
        courses.update(corsor, new BasicDBObject("$addToSet", updateObj));

        UserUpdateHandler.insertUpdates(dbs, ((List) corsor.get(USERS)), assignmentId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
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

        final BasicDBObject updateQuery = MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        System.out.println(updateQuery);
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
