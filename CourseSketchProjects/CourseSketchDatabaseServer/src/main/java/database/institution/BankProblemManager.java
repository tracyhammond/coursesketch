package database.institution;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class BankProblemManager {
    public static String mongoInsertBankProblem(final DB dbs, final SrlBankProblem problem) throws AuthenticationException {
        final DBCollection new_user = dbs.getCollection(PROBLEM_BANK_COLLECTION);
        final BasicDBObject query = new BasicDBObject(QUESTION_TEXT, problem.getQuestionText()).append(IMAGE, problem.getImage())
                .append(SOLUTION_ID, problem.getSolutionId()).append(COURSE_TOPIC, problem.getCourseTopic()).append(SUB_TOPIC, problem.getSubTopic())
                .append(SOURCE, problem.getSource()).append(QUESTION_TYPE, problem.getQuestionType().getNumber())
                .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
                .append(USERS, problem.getAccessPermission().getUserPermissionList()).append(KEYWORDS, problem.getOtherKeywordsList());

        new_user.insert(query);
        final DBObject corsor = new_user.findOne(query);
        return corsor.get(SELF_ID).toString();
    }

    public static SrlBankProblem mongoGetBankProblem(final DB dbs, final String problemBankID, final String userId) throws AuthenticationException {
        final DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankID));
        final DBObject corsor = myDbRef.fetch();

        final ArrayList adminList = (ArrayList) corsor.get(ADMIN);
        final ArrayList usersList = (ArrayList) corsor.get(USERS);
        boolean isAdmin, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

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
        return exactProblem.build();

    }

    public static boolean mongoUpdateBankProblem(final DB dbs, final String problemBankId, final String userId, final SrlBankProblem problem)
            throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
        final DBObject corsor = myDbRef.fetch();

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
        final ArrayList<Object> usersList = (ArrayList<Object>) corsor.get(USERS);
        boolean isAdmin, isMod, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);

        if (!isAdmin) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin) {
            if (problem.hasQuestionText()) {
                updated.append("$set", new BasicDBObject(QUESTION_TEXT, problem.getQuestionText()));
                update = true;
            }
            if (problem.hasImage()) {
                updated.append("$set", new BasicDBObject(IMAGE, problem.getImage()));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (problem.hasSolutionId()) {
                updated.append("$set", new BasicDBObject(SOLUTION_ID, problem.getSolutionId()));
                update = true;
            }
            if (problem.hasCourseTopic()) {
                updated.append("$set", new BasicDBObject(COURSE_TOPIC, problem.getCourseTopic()));
                update = true;
            }
            if (problem.hasSubTopic()) {
                updated.append("$set", new BasicDBObject(SUB_TOPIC, problem.getSubTopic()));
                update = true;
            }
            if (problem.hasSource()) {
                updated.append("$set", new BasicDBObject(SOURCE, problem.getSource()));
                update = true;
            }
            if (problem.hasQuestionType()) {
                updated.append("$set", new BasicDBObject(QUESTION_TYPE, problem.getQuestionType().getNumber()));
                update = true;
            }
            if (problem.getOtherKeywordsCount() > 0) {
                updated.append("$set", new BasicDBObject(KEYWORDS, problem.getOtherKeywordsList()));
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
                    if (permissions.getUserPermissionCount() > 0) {
                        updated.append("$set", new BasicDBObject(USERS, permissions.getUserPermissionList()));
                    }
                }
            }
        }

        if (update) {
            final String[] users = (String[]) corsor.get(USERS);
            for (int i = 0; i < users.length; i++) {
                UserUpdateHandler.InsertUpdate(dbs, users[i], problemBankId, "PROBLEM");
            }

        }

        return true;

    }
}
