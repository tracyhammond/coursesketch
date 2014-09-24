package database.institution;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.State;

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

public class CourseProblemManager {
    public static String mongoInsertCourseProblem(final DB dbs, final String userId, final SrlProblem problem) throws AuthenticationException,
            DatabaseAccessException {
        final DBCollection new_user = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        // make sure person is mod or admin for the assignment
        final AuthType auth = new AuthType();
        auth.checkAdminOrMod = true;
        if (!Authenticator.mognoIsAuthenticated(dbs, ASSIGNMENT_COLLECTION, problem.getAssignmentId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, problem.getCourseId()).append(ASSIGNMENT_ID, problem.getAssignmentId())
                .append(PROBLEM_BANK_ID, problem.getProblemBankId()).append(GRADE_WEIGHT, problem.getGradeWeight())
                .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
                .append(MOD, problem.getAccessPermission().getModeratorPermissionList())
                .append(USERS, problem.getAccessPermission().getUserPermissionList()).append(NAME, problem.getName())
                .append(DESCRIPTION, problem.getDescription()).append(PROBLEM_NUMBER, problem.getProblemNumber());
        new_user.insert(query);
        final DBObject corsor = new_user.findOne(query);

        // inserts the id into the previous the course
        AssignmentManager.mongoInsert(dbs, problem.getAssignmentId(), corsor.get(SELF_ID).toString());

        return corsor.get(SELF_ID).toString();
    }

    /**
     * Returns an SrlProblem given the problemId
     *
     * If a problem is not within a valid date an exception is thrown.
     *
     * @param dbs
     * @param problemId
     * @param userId
     * @param checkTime
     * @return
     * @throws AuthenticationException
     * @throws DatabaseAccessException
     */
    public static SrlProblem mongoGetCourseProblem(final DB dbs, final String problemId, final String userId, final long checkTime)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID " + problemId);
        }

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD);
        final ArrayList usersList = (ArrayList<Object>) corsor.get(USERS);
        boolean isAdmin, isMod, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);
        isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final SrlProblem.Builder exactProblem = SrlProblem.newBuilder();

        exactProblem.setId(problemId);
        exactProblem.setCourseId((String) corsor.get(COURSE_ID));
        exactProblem.setAssignmentId((String) corsor.get(ASSIGNMENT_ID));
        exactProblem.setGradeWeight((String) corsor.get(GRADE_WEIGHT));
        exactProblem.setName((String) corsor.get(NAME));
        exactProblem.setDescription((String) corsor.get(DESCRIPTION));

        // check to make sure the problem is within the time period that the
        // assignment is open and the user is in the assignment
        final AuthType auth = new AuthType();
        auth.checkDate = true;
        auth.user = true;
        if (isUsers) {
            if (!Authenticator.mognoIsAuthenticated(dbs, COURSE_COLLECTION, (String) corsor.get(COURSE_ID), userId, checkTime, auth)) {
                throw new AuthenticationException(AuthenticationException.INVALID_DATE);
            }
        }

        // states
        final State.Builder stateBuilder = State.newBuilder();

        // TODO: add this to all fields!
        // A course is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            try {
                final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
                if (published) {
                    stateBuilder.setPublished(true);
                } else {
                    if (!isAdmin || !isMod) {
                        throw new DatabaseAccessException("The specific course is not published yet", true);
                    } else {
                        stateBuilder.setPublished(false);
                    }
                }
            } catch (Exception e) {

            }
        }

        /*
         * if (corsor.get(IMAGE) != null) { exactProblem.setImageUrl((String)
         * corsor.get(IMAGE)); }
         */

        // problem manager get problem from bank (as a user!)
        final SrlBankProblem problemBank = BankProblemManager.mongoGetBankProblem(dbs, (String) corsor.get(PROBLEM_BANK_ID),
                (String) exactProblem.getCourseId()); // problem bank look up
        if (problemBank != null) {
            exactProblem.setProblemInfo(problemBank);
        }

        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (isAdmin) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
        }
        if (isAdmin || isMod) {
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // mod
            exactProblem.setAccessPermission(permissions.build());
        }
        return exactProblem.build();

    }

    public static boolean mongoUpdateCourseProblem(final DB dbs, final String problemId, final String userId, final SrlProblem problem)
            throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD);
        final ArrayList usersList = (ArrayList<Object>) corsor.get(USERS);
        boolean isAdmin, isMod, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);

        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin || isMod) {
            if (problem.hasGradeWeight()) {
                updated.append("$set", new BasicDBObject(GRADE_WEIGHT, problem.getGradeWeight()));
                update = true;
            }
            if (problem.hasProblemBankId()) {
                updated.append("$set", new BasicDBObject(PROBLEM_BANK_ID, problem.getProblemBankId()));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (problem.hasAccessPermission()) {
                final SrlPermission permissions = problem.getAccessPermission();
                if (isAdmin) {
                    // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                    if (permissions.getAdminPermissionCount() > 0) {
                        updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
                    }
                    if (permissions.getModeratorPermissionCount() > 0) {
                        updated.append("$set", new BasicDBObject(MOD, permissions.getModeratorPermissionList()));
                    }
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updated.append("$set", new BasicDBObject(USERS, permissions.getUserPermissionList()));
                }
            }
        }
        if (update) {
            UserUpdateHandler.InsertUpdates(dbs, ((List) corsor.get(USERS)), problemId, UserUpdateHandler.COURSE_PROBLEM_CLASSIFICATION);
        }
        return true;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * This is used to copy permissions from the parent assignment into the
     * current problem.
     */
    static void mongoInsertDefaultGroupId(final DB dbs, final String courseProblemId, final ArrayList<String>[] ids) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(courseProblemId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection problems = dbs.getCollection(COURSE_PROBLEM_COLLECTION);

        BasicDBObject updateQuery = null;
        BasicDBObject fieldQuery = null;
        for (int k = 0; k < ids.length; k++) {
            final ArrayList<String> list = ids[k];
            final String field = (k == 0) ? ADMIN : (k == 1 ? MOD : USERS); // k = 0
                                                                      // ADMIN,
                                                                      // k = 1
                                                                      // MOD, k
                                                                      // = 2
                                                                      // USERS
            if (k == 0) {
                fieldQuery = new BasicDBObject(field, new BasicDBObject("$each", list));
                updateQuery = new BasicDBObject("$addToSet", fieldQuery);
            } else {
                fieldQuery.append(field, new BasicDBObject("$each", list));
            }
        }
        System.out.println(updateQuery);
        problems.update(corsor, updateQuery);
    }
}
