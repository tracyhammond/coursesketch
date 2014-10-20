package database.institution.mongo;

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
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.USERS;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlAssignment.LatePolicy;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.State;

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

/**
 * Manages assignments for mongo.
 * @author gigemjt
 */
public final class AssignmentManager {

    /**
     * Private constructor.
     */
    private AssignmentManager() {
    }

    /**
     * Inserts an assignment into the mongo database.
     * @param authenticator the object that is performing authenticaton.
     * @param dbs The database where the assignment is being stored.
     * @param userId The id of the user that asking to insert the assignment.
     * @param assignment The assignment that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws AuthenticationException Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException Thrown if there are problems inserting the assignment.
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
            query.append(LATE_POLICY_FUNCTION_TYPE, assignment.getLatePolicy().getFunctionType())
                    .append(LATE_POLICY_RATE, assignment.getLatePolicy().getRate())
                    .append(LATE_POLICY_SUBTRACTION_TYPE, assignment.getLatePolicy().getSubtractionType());
            if (assignment.getLatePolicy().getFunctionType() == LatePolicy.FunctionType.WINDOW_FUNCTION) {
                query.append(LATE_POLICY_TIME_FRAME_TYPE, assignment.getLatePolicy().getTimeFrameType());
            }
        }
        if (assignment.getProblemListList() != null) {
            query.append(PROBLEM_LIST, assignment.getProblemListList());
        }
        newUser.insert(query);
        final DBObject corsor = newUser.findOne(query);

        // inserts the id into the previous the course
        CourseManager.mongoInsertIntoCourse(dbs, assignment.getCourseId(), corsor.get(SELF_ID).toString());

        return corsor.get(SELF_ID).toString();
    }

    /**
     * @param authenticator the object that is performing authentication.
     * @param dbs The database where the assignment is being stored.
     * @param assignmentId the id of the assignment that is being grabbed.
     * @param userId The id of the user that asking to insert the assignment.
     * @param checkTime The time that the assignment was asked to be grabbed. (used to check if the assignment is valid)
     * @return The assignment from the database.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the assignment.
     * @throws DatabaseAccessException Thrown if there are problems retrieving the assignment.
     */
    public static SrlAssignment mongoGetAssignment(final Authenticator authenticator, final DB dbs, final String assignmentId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Assignment was not found with the following ID " + assignmentId, true);
        }

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD);
        final ArrayList usersList = (ArrayList<Object>) corsor.get(USERS);
        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, adminList);
        isMod = authenticator.checkAuthentication(userId, modList);
        isUsers = authenticator.checkAuthentication(userId, usersList);

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the assignment is within the time period that the
        // course is open and the user is in the course
        final AuthType auth = new AuthType();
        auth.setCheckDate(true);
        auth.setUser(true);
        if (isUsers && !authenticator.isAuthenticated(COURSE_COLLECTION, (String) corsor.get(COURSE_ID), userId, checkTime, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_DATE);
        }

        final SrlAssignment.Builder exactAssignment = SrlAssignment.newBuilder();

        exactAssignment.setId(assignmentId);
        exactAssignment.setCourseId((String) corsor.get(COURSE_ID));
        exactAssignment.setName((String) corsor.get(NAME));
        exactAssignment.setType(SrlAssignment.AssignmentType.valueOf((Integer) corsor.get(ASSIGNMENT_TYPE)));
        exactAssignment.setOther((String) corsor.get(ASSIGNMENT_OTHER_TYPE));
        exactAssignment.setDescription((String) corsor.get(DESCRIPTION));
        exactAssignment.addAllLinks((List) corsor.get(ASSIGNMENT_RESOURCES));
        exactAssignment.setGradeWeight((String) corsor.get(GRADE_WEIGHT));

        if (isAdmin || isMod) {
            try {
                final LatePolicy.Builder latePolicy = LatePolicy.newBuilder();
                latePolicy.setFunctionType(SrlAssignment.LatePolicy.FunctionType.valueOf((Integer) corsor.get(LATE_POLICY_FUNCTION_TYPE)));
                // safety case to string then parse to float
                latePolicy.setRate(Float.parseFloat("" + corsor.get(LATE_POLICY_RATE)));
                latePolicy.setSubtractionType((Boolean) corsor.get(LATE_POLICY_SUBTRACTION_TYPE));
                if (latePolicy.getFunctionType() == LatePolicy.FunctionType.WINDOW_FUNCTION) {
                    latePolicy.setTimeFrameType(SrlAssignment.LatePolicy.TimeFrame.valueOf((Integer) corsor.get(LATE_POLICY_TIME_FRAME_TYPE)));
                }
            } catch (Exception e) {
                 e.printStackTrace();
            }
        }

        exactAssignment.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(ACCESS_DATE)).longValue()));
        exactAssignment.setDueDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(DUE_DATE)).longValue()));
        exactAssignment.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(CLOSE_DATE)).longValue()));

        // states
        final State.Builder stateBuilder = State.newBuilder();
        if (exactAssignment.getDueDate().getMillisecond() > checkTime) {
            stateBuilder.setPastDue(true);
        }

        // FUTURE: add this to all fields!
        // A course is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            try {
                final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
                if (published) {
                    stateBuilder.setPublished(true);
                } else {
                    if (!isAdmin || !isMod) {
                        throw new DatabaseAccessException("The specific assignment is not published yet", true);
                    }
                    stateBuilder.setPublished(false);
                }
            } catch (DatabaseAccessException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (corsor.get(IMAGE) != null) {
            exactAssignment.setImageUrl((String) corsor.get(IMAGE));
        }

        // if you are a user, the course must be open to view the assignments
        if (isAdmin || isMod || (isUsers
                && Authenticator.isTimeValid(checkTime, exactAssignment.getAccessDate(), exactAssignment.getCloseDate()))) {
            if (corsor.get(PROBLEM_LIST) != null) {
                exactAssignment.addAllProblemList((List) corsor.get(PROBLEM_LIST));
            }

            if (isAdmin || isMod) {
                System.out.println("User is an admin or mod for this course and is acting like one " + userId);
            } else {
                System.out.println("User is weakling for this course and is acting like one " + userId);
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
     * updates data from an assignment.
     * @param authenticator the object that is performing authentication.
     * @param dbs The database where the assignment is being stored.
     * @param assignmentId the id of the assignment that is being updated.
     * @param userId The id of the user that asking to insert the assignment.
     * @param assignment The assignment that is being inserted.
     * @return true if the assignment was updated successfully.
     * @throws AuthenticationException The user does not have permission to update the assignment.
     * @throws DatabaseAccessException The assignment does not exist.
     */
    public static boolean mongoUpdateAssignment(final Authenticator authenticator, final DB dbs, final String assignmentId, final String userId,
            final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection courses = dbs.getCollection(ASSIGNMENT_COLLECTION);

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
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasType()) {
                updateObj = new BasicDBObject(ASSIGNMENT_TYPE, assignment.getType().getNumber());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasOther()) {
                updateObj = new BasicDBObject(ASSIGNMENT_OTHER_TYPE, assignment.getOther());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasDescription()) {
                updateObj = new BasicDBObject(DESCRIPTION, assignment.getDescription());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.getLinksList() != null) {
                updateObj = new BasicDBObject(ASSIGNMENT_RESOURCES, assignment.getLinksList());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasLatePolicy()) {
                throw new DatabaseAccessException("Late policy does not exist in the database!");
                /*
                 updateObj = new BasicDBObject(LATE_POLICY,
                 assignment.getLatePolicy().getNumber());
                 courses.update(corsor, new BasicDBObject ("$set",
                 updateObj));
                 */
            }
            if (assignment.hasGradeWeight()) {
                updateObj = new BasicDBObject(GRADE_WEIGHT, assignment.getGradeWeight());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasAccessDate()) {
                updateObj = new BasicDBObject(ACCESS_DATE, assignment.getAccessDate().getMillisecond());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasDueDate()) {
                updateObj = new BasicDBObject(DUE_DATE, assignment.getDueDate().getMillisecond());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasCloseDate()) {
                updateObj = new BasicDBObject(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.hasImageUrl()) {
                updateObj = new BasicDBObject(IMAGE, assignment.getImageUrl());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (assignment.getProblemListCount() > 0) {
                updateObj = new BasicDBObject(PROBLEM_LIST, assignment.getProblemListList());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }

            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (assignment.hasAccessPermission()) {
                final SrlPermission permissions = assignment.getAccessPermission();
                if (isAdmin) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
                        updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
                        courses.update(corsor, new BasicDBObject("$set", updateObj));
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
                        courses.update(corsor, new BasicDBObject("$set", updateObj));
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
                    courses.update(corsor, new BasicDBObject("$set", updateObj));
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
     * @param dbs the database where the assignment is stored.
     * @param assignmentId the assignment that the problem is being added to.
     * @param problemId the id of the course problem that is being added to the assignment.
     * @return true if it is successful.
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
     * @param dbs the database where the data is stored.
     * @param assignmentId the id of the assignment that is getting permissions.
     * @param ids the list of list of permissions that is getting added.
     */
    /*package-private*/ static void mongoInsertDefaultGroupId(final DB dbs, final String assignmentId, final ArrayList<String>[] ids) {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection assignments = dbs.getCollection(ASSIGNMENT_COLLECTION);

        BasicDBObject updateQuery = null;
        BasicDBObject fieldQuery = null;
        for (int k = 0; k < ids.length; k++) {
            final ArrayList<String> list = ids[k];
            // k = 0 ADMIN, k = 1, MOD, k >= 2 USERS
            final String field = k == 0 ? ADMIN : (k == 1 ? MOD : USERS);
            if (k == 0) {
                fieldQuery = new BasicDBObject(field, new BasicDBObject("$each", list));
                updateQuery = new BasicDBObject("$addToSet", fieldQuery);
            } else {
                fieldQuery.append(field, new BasicDBObject("$each", list));
            }
        }
        System.out.println(updateQuery);
        assignments.update(corsor, updateQuery);
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * Returns a list of Id for the default group for an assignment.
     *
     * the Ids are ordered as so: AdminGroup, ModGroup, UserGroup.
     * @param dbs the database where the ids are stored.
     * @param assignmentId the id of the assignment that contains the ids.
     * @return a list of id groups.
     */
    static ArrayList<String>[] mongoGetDefaultGroupId(final DB dbs, final String assignmentId) {
        final DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
        final DBObject corsor = myDbRef.fetch();
        final ArrayList<String>[] returnValue = new ArrayList[PERMISSION_LEVELS];
        returnValue[0] = (ArrayList) corsor.get(ADMIN);
        returnValue[1] = (ArrayList) corsor.get(MOD);
        returnValue[2] = (ArrayList) corsor.get(USERS);
        return returnValue;
    }
}
