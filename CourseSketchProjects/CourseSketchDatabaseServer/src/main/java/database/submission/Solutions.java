package database.submission;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class Solutions {
    public static String mongoInsertCourse(final DB dbs, final SrlSolution solution) {
        final DBCollection new_user = dbs.getCollection("Solutions");
        final BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank()).append(IS_PRACTICE_PROBLEM,
                solution.getIsPracticeProblem()).append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray()); // byte
                                                                                                                             // blob
        // .append(ADMIN,
        // solution.getAccessPermissions().getAdminPermissionList())
        // .append(MOD,solution.getAccessPermissions().getModeratorPermissionList())
        // .append(USERS,
        // solution.getAccessPermissions().getUserPermissionList());

        new_user.insert(query);
        final DBObject corsor = new_user.findOne(query);
        return corsor.get("SELF_ID").toString();
    }

    public static SrlSolution mongoGetSolutions(final DB dbs, final String solutionId, final String userId) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, "Solutions", new ObjectId(solutionId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Solution was not found with the following ID " + solutionId);
        }
        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN); // convert
                                                                     // to
                                                                     // ArrayList<String>
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD); // convert to
                                                                 // ArrayList<String>
        final ArrayList usersList = (ArrayList<Object>) corsor.get(USERS); // convert
                                                                     // to
                                                                     // ArrayList<String>
        boolean isAdmin, isMod, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);
        isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        // need to figure out how to add the SrlSolutin which is similar to
        // SrlCourse
        final SrlSolution.Builder exactSolution = SrlSolution.newBuilder();
        exactSolution.setAllowedInProblemBank((Boolean) corsor.get(ALLOWED_IN_PROBLEMBANK));
        exactSolution.setIsPracticeProblem((Boolean) corsor.get(IS_PRACTICE_PROBLEM));
        final SrlSubmission.Builder sub = SrlSubmission.newBuilder();
        sub.setId((String) corsor.get(SELF_ID));
        System.out.println(corsor.get(UPDATELIST).getClass());
        /*
         * try {
         * sub.setUpdateList(SrlUpdateList.parseFrom(((byte[])corsor.get(UPDATELIST
         * )))); } catch (InvalidProtocolBufferException e) {
         * e.printStackTrace(); }
         */
        exactSolution.setSubmission(sub.build());
        return exactSolution.build();

    }
}
