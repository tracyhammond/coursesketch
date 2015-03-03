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
import org.bson.types.ObjectId;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;
import sun.font.Script;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.*;

/**
 * Interfaces with the mongo database to manage bank problems.
 * @author gigemjt
 *
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
public final class BankProblemManager {

    /**
     * Private constructor.
     *
     */
    private BankProblemManager() {
    }

    /**
     * Inserts a problem bank into the mongo database.
     *
     * @param dbs the database into which the bank is being inserted.
     * @param problem the problem data that is being inserted.
     * @return The mongo id of the problem bank.
     * @throws AuthenticationException Not currently thrown but may be thrown in the future.
     */
    public static String mongoInsertBankProblem(final DB dbs, final SrlBankProblem problem) throws AuthenticationException {
        final DBCollection problemBankCollection = dbs.getCollection(PROBLEM_BANK_COLLECTION);
        final BasicDBObject query = new BasicDBObject(QUESTION_TEXT, problem.getQuestionText()).append(IMAGE, problem.getImage())
                .append(SOLUTION_ID, problem.getSolutionId()).append(COURSE_TOPIC, problem.getCourseTopic()).append(SUB_TOPIC, problem.getSubTopic())
                .append(SOURCE, problem.getSource()).append(QUESTION_TYPE, problem.getQuestionType().getNumber())
                .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
                .append(USERS, problem.getAccessPermission().getUserPermissionList()).append(KEYWORDS, problem.getOtherKeywordsList())
                .append(SCRIPT, problem.getScript());

        problemBankCollection.insert(query);
        final DBObject corsor = problemBankCollection.findOne(query);
        return corsor.get(SELF_ID).toString();
    }

    /**
     * gets a mongo bank problem (this is usually grabbed through a course id instead of a specific user unless the user is the admin).
     * @param authenticator The object that is authenticating the user.
     * @param dbs the database where the problem is stored.
     * @param problemBankID the id of the problem that is being grabbed.
     * @param userId the id of the user (typically a course unless they are an admin)
     * @return the SrlBank problem data if it past all tests.
     * @throws AuthenticationException thrown if the user does not have access to the permissions.
     */
    public static SrlBankProblem mongoGetBankProblem(final Authenticator authenticator, final DB dbs, final String problemBankID, final String userId)
            throws AuthenticationException {
        final DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankID));
        final DBObject corsor = myDbRef.fetch();

        boolean isAdmin, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (ArrayList) corsor.get(ADMIN));
        isUsers = authenticator.checkAuthentication(userId, (ArrayList) corsor.get(USERS));

        if (!isAdmin && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final SrlBankProblem.Builder exactProblem = SrlBankProblem.newBuilder();

        exactProblem.setId(problemBankID);
        exactProblem.setQuestionText((String) corsor.get(QUESTION_TEXT));
        exactProblem.setImage((String) corsor.get(IMAGE));
        if (isAdmin) {
            exactProblem.setSolutionId((String) corsor.get(SOLUTION_ID));
        }
        exactProblem.setCourseTopic((String) corsor.get(COURSE_TOPIC));
        exactProblem.setSubTopic((String) corsor.get(SUB_TOPIC));
        exactProblem.setSource((String) corsor.get(SOURCE));
        exactProblem.setQuestionType(SrlBankProblem.QuestionType.valueOf((Integer) corsor.get(QUESTION_TYPE)));
        exactProblem.addAllOtherKeywords((ArrayList) corsor.get(KEYWORDS)); // change
                                                                            // arraylist
        final SrlPermission.Builder permissions = SrlPermission.newBuilder();
        if (isAdmin) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // admin
            exactProblem.setAccessPermission(permissions.build());
        }
        exactProblem.setScript((String) corsor.get(SCRIPT));
        return exactProblem.build();

    }

    /**
     * Updates a bank problem.
     * @param authenticator
     *            the object that is performing authentication.
     * @param dbs
     *            The database where the assignment is being stored.
     * @param problemBankId the id of the problem getting updated.
     * @param userId the user updating the course problem.
     * @param problem the bank problem data that is being updated.
     * @return true if the update is successful
     * @throws AuthenticationException Thrown if the user does not have permission to update the bank problem.
     * @throws DatabaseAccessException Thrown if there is an issue updating the problem.
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
        "PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts" })
    public static boolean mongoUpdateBankProblem(final Authenticator authenticator, final DB dbs, final String problemBankId, final String userId,
            final SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject corsor = myDbRef.fetch();

        boolean isAdmin;
        isAdmin = authenticator.checkAuthentication(userId, (ArrayList) corsor.get(ADMIN));
        final DBCollection problemCollection = dbs.getCollection(PROBLEM_BANK_COLLECTION);

        if (!isAdmin) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (problem.hasQuestionText()) {
            problemCollection.update(corsor, new BasicDBObject(SET_COMMAND, new BasicDBObject(QUESTION_TEXT, problem.getQuestionText())));
            updated.append(SET_COMMAND, new BasicDBObject(QUESTION_TEXT, problem.getQuestionText()));
            update = true;
        }
        if (problem.hasImage()) {
            updated.append(SET_COMMAND, new BasicDBObject(IMAGE, problem.getImage()));
            update = true;
        }
        // Optimization: have something to do with pulling values of an
        // array and pushing values to an array
        if (problem.hasSolutionId()) {
            updated.append(SET_COMMAND, new BasicDBObject(SOLUTION_ID, problem.getSolutionId()));
            update = true;
        }
        if (problem.hasCourseTopic()) {
            updated.append(SET_COMMAND, new BasicDBObject(COURSE_TOPIC, problem.getCourseTopic()));
            update = true;
        }
        if (problem.hasSubTopic()) {
            updated.append(SET_COMMAND, new BasicDBObject(SUB_TOPIC, problem.getSubTopic()));
            update = true;
        }
        if (problem.hasSource()) {
            updated.append(SET_COMMAND, new BasicDBObject(SOURCE, problem.getSource()));
            update = true;
        }
        if (problem.hasQuestionType()) {
            updated.append(SET_COMMAND, new BasicDBObject(QUESTION_TYPE, problem.getQuestionType().getNumber()));
            update = true;
        }
        if (problem.getOtherKeywordsCount() > 0) {
            updated.append(SET_COMMAND, new BasicDBObject(KEYWORDS, problem.getOtherKeywordsList()));
            update = true;
        }
        if (problem.getScript()) {
            updated.append(SCRIPT, new BasicDBObject(SCRIPT, problem.getScript()));
            update = true;
        }
        // Optimization: have something to do with pulling values of an
        // array and pushing values to an array
        if (problem.hasAccessPermission()) {
            final SrlPermission permissions = problem.getAccessPermission();
            if (isAdmin) {
                // ONLY ADMIN CAN CHANGE ADMIN OR MOD
                if (permissions.getAdminPermissionCount() > 0) {
                    updated.append(SET_COMMAND, new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
                }
                if (permissions.getUserPermissionCount() > 0) {
                    updated.append(SET_COMMAND, new BasicDBObject(USERS, permissions.getUserPermissionList()));
                }
            }
        }

        if (update) {
            problemCollection.update(corsor, updated);
            final List<String> users = (List) corsor.get(USERS);
            for (int i = 0; i < users.size(); i++) {
                UserUpdateHandler.insertUpdate(dbs, users.get(i), problemBankId, "PROBLEM");
            }
        }

        return true;

    }
}
