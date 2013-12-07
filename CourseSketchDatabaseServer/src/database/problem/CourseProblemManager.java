package database.problem;

import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseProblemManager 
{
	private static String mongoInsertAssignment(DB dbs, String userId, CourseProblemBuilder problem) throws AuthenticationException
	{
		DBCollection new_user = dbs.getCollection("Courses");
		AssignmentBuilder assignment = AssignmentManager.mongoGetAssignment(dbs,problem.courseId,userId);
		boolean isAdmin = Authenticator.checkAuthentication(dbs, userId, assignment.permissions.admin);
		boolean isMod = Authenticator.checkAuthentication(dbs, userId, assignment.permissions.mod);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException();
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

		String[] addedProblem = Arrays.copyOf(assignment.problemList, assignment.problemList.length+1);

		addedProblem[assignment.problemList.length] = (String) corsor.get("_id"); 

		AssignmentBuilder newAssignment = new AssignmentBuilder();

		newAssignment.setProblemList(addedProblem);

		AssignmentManager.mongoUpdateAssignment(dbs, problem.courseId,userId,newAssignment);

		return (String) corsor.get("_id");
	}

	private static CourseProblemBuilder mongoGetProblem(DB dbs, String courseID,String userId) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Assignments");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);
		
		String[] adminList = (String[])corsor.get("Admin");
		String[] modList = (String[])corsor.get("Mod");	
		String[] usersList = (String[])corsor.get("Users");
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);
		
		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException();
		}
		
		CourseProblemBuilder exactProblem = new CourseProblemBuilder();
		
		exactProblem.setCourseId((String)corsor.get("CourseId"));
		exactProblem.setAssignmentId((String)corsor.get("AssignmentId"));
		exactProblem.setGradeWeight((String)corsor.get("GradeWeight"));
		

		// problem manager get problem from bank (as a user!)
		ProblemBuilder problemBank = ProblemManager.getProblem(dbs, (String)corsor.get("problemBankId"), exactProblem.courseId); // problem bank look up
		exactProblem.problemResource = problemBank;
		
		if (isAdmin) {
			exactProblem.permissions.setAdmin((String[])corsor.get("Admin")); // admin
			exactProblem.permissions.setMod((String[])corsor.get("Mod"));	 // admin
		}
		if (isAdmin || isMod) {
			exactProblem.setProblemBankId((String)corsor.get("ProblemBankId")); //admin or mod
			exactProblem.permissions.setUsers((String[])corsor.get("Users")); //admin or mod
		}
		return exactProblem;

	}


	private static boolean mongoUpdateAssignment(DB dbs, String courseID,String userId,CourseProblemBuilder problem) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Courses");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);

		String[] adminList = (String[])corsor.get("Admin");
		String[] modList = (String[])corsor.get("Mod");	
		String[] usersList = (String[])corsor.get("Users");
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException();
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






