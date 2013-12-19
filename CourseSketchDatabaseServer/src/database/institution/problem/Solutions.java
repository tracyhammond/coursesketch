package database.institution.problem;

import static database.institution.StringConstants.*;

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

import database.institution.DatabaseAccessException;
import database.institution.auth.AuthenticationException;
import database.institution.auth.Authenticator;

public class Solutions 
{
	public static String mongoInsertCourse(DB dbs, SrlSolution solution)
	{
		DBCollection new_user = dbs.getCollection("Solutions");
		BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK,solution.getAllowedInProblemBank())
										 .append(IS_PRACTICE_PROBLEM,solution.getIsPracticeProblem()) 
										 .append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray()) // byte blob
										 .append(ADMIN, solution.getAccessPermissions().getAdminPermissionList())
										 .append(MOD,solution.getAccessPermissions().getModeratorPermissionList())
										 .append(USERS, solution.getAccessPermissions().getUserPermissionList());
										 
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get("SELF_ID").toString();
	}

	public static SrlSolution mongoGetSolutions(DB dbs, String solutionId,String userId) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "Solutions", new ObjectId(solutionId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Solution was not found with the following ID " + solutionId);
		}
		ArrayList adminList =  (ArrayList<Object>) corsor.get(ADMIN); //convert to ArrayList<String>
		ArrayList modList =  (ArrayList<Object>) corsor.get(MOD); //convert to ArrayList<String>
		ArrayList usersList =  (ArrayList<Object>) corsor.get(USERS); //convert to ArrayList<String>
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId,adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		//need to figure out how to add the SrlSolutin which is similar to SrlCourse
		SrlSolution.Builder exactSolution = SrlSolution.newBuilder();
		exactSolution.setAllowedInProblemBank((Boolean)corsor.get(ALLOWED_IN_PROBLEMBANK));
		exactSolution.setIsPracticeProblem((Boolean)corsor.get(IS_PRACTICE_PROBLEM));
		SrlSubmission.Builder sub = SrlSubmission.newBuilder();
		sub.setId((String)corsor.get(SELF_ID));
		System.out.println(corsor.get(UPDATELIST).getClass());
		try {
			sub.setUpdateList(SrlUpdateList.parseFrom(((byte[])corsor.get(UPDATELIST))));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		exactSolution.setSubmission(sub.build());
		return exactSolution.build();
		
	}
}
