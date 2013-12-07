package database.problem;

import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class ProblemManager 
{
	private static String mongoInsertProblem(DB dbs, ProblemBankBuilder problem) throws AuthenticationException
	{
	
		DBCollection new_user = dbs.getCollection("ProblemBank");
		BasicDBObject query = new BasicDBObject("QuestionText",problem.questionText)
										 .append("QestionImageName",problem.qestionImageName)
										 .append("QuestionAnswerId",problem.questionAnswerId) 
										 .append("CourseTopic",problem.courseTopic)
										 .append("SubTopic",problem.subTopic)
										 .append("Source",problem.source)
										 .append("QuestionType", problem.questionType)
										 .append("Access", problem.access)
										 .append("OtherKeywords", problem.otherKeywords);
										 
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return (String) corsor.get("_id");
	}
	
	private static ProblemBankBuilder mongoGetAssignment(DB dbs, String problemBankID,String userId) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Assignments");
		BasicDBObject query = new BasicDBObject("_id",problemBankID);
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
		
		ProblemBankBuilder exactProblem = new ProblemBankBuilder();
		
		exactProblem.setQuestionText((String)corsor.get("QuestionText"));
		exactProblem.setQestionImageName((String)corsor.get("QestionImageName"));
		exactProblem.setQuestionAnswerId((String)corsor.get("QuestionAnswerId"));
		exactProblem.setCourseTopic((String)corsor.get("CourseTopic"));
		exactProblem.setSubTopic((String)corsor.get("SubTopic"));
		exactProblem.setSource((String)corsor.get("Source"));
		exactProblem.setQuestionType((String)corsor.get("QuestionType"));
		exactProblem.setAccess((String[])corsor.get("Access"));
		exactProblem.setOtherKeywords((String[])corsor.get("OtherKeywords"));
		
		
		if (isAdmin) 
		{
			exactProblem.permissions.setAdmin((String[])corsor.get("Admin")); // admin
			exactProblem.permissions.setMod((String[])corsor.get("Mod"));	 // admin
		}
		if (isAdmin || isMod) {
			exactProblem.permissions.setUsers((String[])corsor.get("Users")); //admin
		}
		return exactProblem;

	}


	private static boolean mongoUpdateAssignment(DB dbs, String courseID,String userId,ProblemBankBuilder problem) throws AuthenticationException
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
		if (isAdmin) 
		{
			
			if (problem.semesester != null) {
				updated.append("$set", new BasicDBObject("Semesester", problem.semesester));
			}
			if (problem.openDate != null) {
				updated.append("$set", new BasicDBObject("OpenDate", problem.openDate));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (problem.closeDate != null) {
				updated.append("$set", new BasicDBObject("CloseDate", problem.closeDate));
			}
			if (problem.image != null) {
				updated.append("$set", new BasicDBObject("Image", problem.image));
			}
			if (problem.assignmentList != null) {
				updated.append("$set", new BasicDBObject("AssignmentList", problem.assignmentList));
			}
			if (problem.description != null) {
				updated.append("$set", new BasicDBObject("Description", problem.description));
			}
			if (problem.name != null) {
				updated.append("$set", new BasicDBObject("Name", problem.name));
			}
			if (problem.access != null) {
				updated.append("$set", new BasicDBObject("Access", problem.access));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (problem.permissions.admin != null) {
				updated.append("$set", new BasicDBObject("Admin", problem.permissions.admin));
			}
			if (problem.permissions.mod != null) {
				updated.append("$set", new BasicDBObject("Mod", problem.permissions.mod));
			}
			if (problem.permissions.users != null) {
				updated.append("$set", new BasicDBObject("Users", problem.permissions.users));
			}
		}
		return true;
		
	}
}
