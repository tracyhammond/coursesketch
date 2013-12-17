package database.problem;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseProblemManager 
{
	public static String mongoInsertCourseProblem(DB dbs, String userId, SrlProblem problem) throws AuthenticationException, DatabaseAccessException
	{
		DBCollection new_user = dbs.getCollection("Problems");
		SrlAssignment assignment = AssignmentManager.mongoGetAssignment(dbs,problem.getAssignmentId(),userId, 0);
		if (assignment.getAccessPermission().getAdminPermissionCount() <= 0) {
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		boolean isAdmin = Authenticator.checkAuthentication(dbs, userId, assignment.getAccessPermission().getAdminPermissionList());
		boolean isMod = Authenticator.checkAuthentication(dbs, userId, assignment.getAccessPermission().getModeratorPermissionList());

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject query = new BasicDBObject(COURSE_ID,problem.getCourseId())
										 .append(ASSIGNMENT_ID,problem.getAssignmentId())
										 .append(PROBLEM_BANK_ID,problem.getProblemBankId())
										 .append(GRADE_WEIGHT,problem.getGradeWeight())
										 .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
										 .append(MOD, problem.getAccessPermission().getModeratorPermissionList())
										 .append(USERS, problem.getAccessPermission().getUserPermissionList());
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);

		List<String> idList = new ArrayList<String>();
		if (assignment.getProblemListCount() > 0) {
			idList.addAll(assignment.getProblemListList());
		}
		idList.add((String) corsor.get(SELF_ID).toString());
		SrlAssignment.Builder newAssignment = SrlAssignment.newBuilder();
		newAssignment.addAllProblemList(idList);

		AssignmentManager.mongoUpdateAssignment(dbs, problem.getAssignmentId(), userId, newAssignment.buildPartial());

		return corsor.get(SELF_ID).toString();
	}

	public static SrlProblem mongoGetCourseProblem(DB dbs, String problemId,String userId, long checkTime) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "Problems", new ObjectId(problemId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Course was not found with the following ID " + problemId);
		}

		ArrayList adminList = (ArrayList<Object>)corsor.get(ADMIN);
		ArrayList modList = (ArrayList<Object>)corsor.get(MOD);	
		ArrayList usersList = (ArrayList<Object>)corsor.get(USERS);
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		SrlProblem.Builder exactProblem = SrlProblem.newBuilder();

		exactProblem.setId(problemId);
		exactProblem.setCourseId((String)corsor.get(COURSE_ID));
		exactProblem.setAssignmentId((String)corsor.get(ASSIGNMENT_ID));
		exactProblem.setGradeWeight((String)corsor.get(GRADE_WEIGHT));

		if (isUsers) {
			SrlAssignment assignment = AssignmentManager.mongoGetAssignment(dbs, exactProblem.getAssignmentId(), userId, checkTime);
			if (!Authenticator.isTimeValid(checkTime, assignment.getAccessDate(), assignment.getCloseDate())) {
				throw new AuthenticationException(AuthenticationException.EARLY_ACCESS);
			}
		}

		// problem manager get problem from bank (as a user!)
		SrlBankProblem problemBank = BankProblemManager.mongoGetBankProblem(dbs, (String)corsor.get(PROBLEM_BANK_ID), (String)exactProblem.getCourseId()); // problem bank look up
		if (problemBank != null) {
			exactProblem.setProblemInfo(problemBank);
		}

		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		if (isAdmin) 
		{
			permissions.addAllAdminPermission((ArrayList)corsor.get(ADMIN)); // admin
			permissions.addAllModeratorPermission((ArrayList)corsor.get(MOD));	 // admin
		}
		if (isAdmin || isMod) {
			permissions.addAllUserPermission((ArrayList)corsor.get(USERS)); // mod
			exactProblem.setAccessPermission(permissions.build());
		}
		return exactProblem.build();

	}

	public static boolean mongoUpdateCourseProblem(DB dbs, String problemId,String userId,SrlProblem problem) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Problems", new ObjectId(problemId));
		DBObject corsor = myDbRef.fetch();

		ArrayList adminList = (ArrayList<Object>)corsor.get(ADMIN);
		ArrayList modList = (ArrayList<Object>)corsor.get(MOD);	
		ArrayList usersList = (ArrayList<Object>)corsor.get(USERS);
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject updated = new BasicDBObject();
		if (isAdmin || isMod) 
		{
			if (problem.hasGradeWeight()) {
				updated.append("$set", new BasicDBObject(GRADE_WEIGHT, problem.getGradeWeight()));
			}
			if (problem.hasProblemBankId()) {
				updated.append("$set", new BasicDBObject(PROBLEM_BANK_ID, problem.getProblemBankId()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (problem.hasAccessPermission()) {
				SrlPermission permissions = problem.getAccessPermission();
				if (isAdmin)
				{
					// ONLY ADMIN CAN CHANGE ADMIN OR MOD
					if (permissions.getAdminPermissionCount() > 0) {
						updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
					}
					if (permissions.getModeratorPermissionCount() > 0) {
						updated.append("$set", new BasicDBObject(MOD, permissions.getModeratorPermissionList()));
					}
				}
				if (permissions.getUserPermissionCount() > 0) 
				{
					updated.append("$set", new BasicDBObject(USERS, permissions.getUserPermissionList()));
				}
			}
		}
		return true;
	}

}
