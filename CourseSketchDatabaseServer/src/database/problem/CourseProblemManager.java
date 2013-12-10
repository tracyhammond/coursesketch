package database.problem;

import java.util.ArrayList;
import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.PermissionBuilder;
import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.course.CourseBuilder;
import database.course.CourseManager;

public class CourseProblemManager 
{
	private static String mongoInsertAssignment(DB dbs, String userId, CourseProblemBuilder problem) throws AuthenticationException
	{
		DBCollection new_user = dbs.getCollection("Problems");
		AssignmentBuilder assignment = AssignmentManager.mongoGetAssignment(dbs,problem.courseId,userId, 0);
		boolean isAdmin = Authenticator.checkAuthentication(dbs, userId, assignment.permissions.admin);
		boolean isMod = Authenticator.checkAuthentication(dbs, userId, assignment.permissions.mod);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject query = new BasicDBObject("CourseId",problem.courseId)
										 .append("AssignmentId",problem.assignmentId)
										 .append("ProblemBankId",problem.problemBankId)
										 .append("GradeWeight",problem.gradeWeight)
										 .append("Admin", problem.permissions.admin)
										 .append("Mod",problem.permissions.mod)
										 .append("Users", problem.permissions.users);
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);

		assignment.problemList.add((String) corsor.get("_id"));
		AssignmentBuilder newAssignment = new AssignmentBuilder();
		newAssignment.setProblemList(assignment.problemList);
		AssignmentManager.mongoUpdateAssignment(dbs, assignment.courseId,userId,newAssignment);

		return (String) corsor.get("_id");
	}

	private static CourseProblemBuilder mongoGetProblem(DB dbs, String courseID,String userId, long checkTime) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Problems");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);

		ArrayList adminList = (ArrayList<Object>)corsor.get("Admin");
		ArrayList modList = (ArrayList<Object>)corsor.get("Mod");	
		ArrayList usersList = (ArrayList<Object>)corsor.get("Users");
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		CourseProblemBuilder exactProblem = new CourseProblemBuilder();

		exactProblem.setCourseId((String)corsor.get("CourseId"));
		exactProblem.setAssignmentId((String)corsor.get("AssignmentId"));
		exactProblem.setGradeWeight((String)corsor.get("GradeWeight"));

		if (isUsers) {
			AssignmentBuilder assignment = AssignmentManager.mongoGetAssignment(dbs, exactProblem.assignmentId, userId, checkTime);
			if(!PermissionBuilder.isTimeValid(checkTime, assignment.openDate, assignment.closeDate)) {
				throw new AuthenticationException(AuthenticationException.EARLY_ACCESS);
			}
		}

		// problem manager get problem from bank (as a user!)
		ProblemBankBuilder problemBank = ProblemManager.mongoGetProblem(dbs, (String)corsor.get("problemBankId"), (String)exactProblem.courseId); // problem bank look up
		exactProblem.problemResource = problemBank;

		if (isAdmin) {
			exactProblem.permissions.setAdmin((ArrayList)corsor.get("Admin")); // admin
			exactProblem.permissions.setMod((ArrayList)corsor.get("Mod"));	 // admin
		}
		if (isAdmin || isMod) {
			exactProblem.setProblemBankId((String)corsor.get("ProblemBankId")); //admin or mod
			exactProblem.permissions.setUsers((ArrayList)corsor.get("Users")); //admin or mod
		}
		return exactProblem;

	}

	private static boolean mongoUpdateAssignment(DB dbs, String courseID,String userId,CourseProblemBuilder problem) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Problems");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);

		ArrayList adminList = (ArrayList<Object>)corsor.get("Admin");
		ArrayList modList = (ArrayList<Object>)corsor.get("Mod");	
		ArrayList usersList = (ArrayList<Object>)corsor.get("Users");
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
			if (problem.gradeWeight != null) {
				updated.append("$set", new BasicDBObject("Name", problem.gradeWeight));
			}
			if (problem.problemBankId != null) {
				updated.append("$set", new BasicDBObject("Type", problem.problemBankId));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (isAdmin) 
			{
				// ONLY ADMIN CAN CHANGE ADMIN OR MOD
				if (problem.permissions.admin != null) {
					updated.append("$set", new BasicDBObject("Admin", problem.permissions.admin));
				}
				if (problem.permissions.mod != null) {
					updated.append("$set", new BasicDBObject("Mod", problem.permissions.mod));
				}
			}
			if (problem.permissions.users != null) 
			{
				updated.append("$set", new BasicDBObject("Users", problem.permissions.users));
			}
		}
		return true;
	}

}
