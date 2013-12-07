package database.assignment;

import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.course.CourseBuilder;
import database.course.CourseManager;

public class AssignmentManager 
{
	public static String mongoInsertAssignment(DB dbs, String userId, AssignmentBuilder assignment) throws AuthenticationException
	{
		DBCollection new_user = dbs.getCollection("Assignments");
		CourseBuilder course = CourseManager.mongoGetCourse(dbs,assignment.courseId,userId);
		boolean isAdmin = Authenticator.checkAuthentication(dbs, userId, course.permissions.admin);
		boolean isMod = Authenticator.checkAuthentication(dbs, userId, course.permissions.mod);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException();
		}
		BasicDBObject query = new BasicDBObject("CourseId",assignment.courseId)
										 .append("Name",assignment.name)
										 .append("Type",assignment.type) 
										 .append("Other",assignment.other)
										 .append("Description",assignment.description)
										 .append("Resources",assignment.resources)
										 .append("LatePolicy", assignment.latePolicy)
										 .append("GradeWeigh",assignment.gradeWeight)
										 .append("OpenDate", assignment.openDate)
										 .append("DueDate", assignment.dueDate)
										 .append("CloseDate",assignment.closeDate)
										 .append("ImageUrl", assignment.imageUrl)
										 
										 .append("Admin", assignment.permissions.admin)
										 .append("Mod",assignment.permissions.mod)
										 .append("Users", assignment.permissions.users);
		if (assignment.problemList != null) {
			query.append("ProblemList", assignment.problemList);
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);

		String[] addedAssignment = Arrays.copyOf(course.assignmentList, course.assignmentList.length+1);;

		addedAssignment[course.assignmentList.length] = (String) corsor.get("_id"); 
		CourseBuilder newCourse = new CourseBuilder();
		newCourse.setAssignmentList(addedAssignment);
		CourseManager.mongoUpdateCourse(dbs, assignment.courseId,userId,newCourse);
		return (String) corsor.get("_id");
	}

	public static AssignmentBuilder mongoGetAssignment(DB dbs, String courseID,String userId) throws AuthenticationException
	{
		DBCollection assignments = dbs.getCollection("Assignments");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = assignments.findOne(query);

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

		AssignmentBuilder exactAssignment = new AssignmentBuilder();

		exactAssignment.setCourseId((String)corsor.get("CourseId"));
		exactAssignment.setName((String)corsor.get("Name"));
		exactAssignment.setType((String)corsor.get("Type"));
		exactAssignment.setOther((String)corsor.get("Other"));
		exactAssignment.setDescription((String)corsor.get("Description"));
		exactAssignment.setResources((String)corsor.get("Resources"));
		exactAssignment.setLatePolicy((String)corsor.get("LatePolicy"));
		exactAssignment.setGradeWeigh((String)corsor.get("GradeWeigh"));
		exactAssignment.setOpenDate((String)corsor.get("OpenDate"));
		exactAssignment.setDueDate((String)corsor.get("DueDate"));
		exactAssignment.setCloseDate((String)corsor.get("CloseDate"));
		exactAssignment.setImageUrl((String)corsor.get("ImageUrl"));
		exactAssignment.setProblemList((String[])corsor.get("ProblemList"));

		if (isAdmin) 
		{
			exactAssignment.permissions.setAdmin((String[])corsor.get("Admin")); // admin
			exactAssignment.permissions.setMod((String[])corsor.get("Mod"));	 // admin
		}
		if (isAdmin || isMod) {
			exactAssignment.permissions.setUsers((String[])corsor.get("Users")); //admin
		}
		return exactAssignment;

	}

	public static boolean mongoUpdateAssignment(DB dbs, String courseID,String userId,AssignmentBuilder assignment) throws AuthenticationException
	{
		DBCollection assignments = dbs.getCollection("Assignments");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = assignments.findOne(query);

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
			if (assignment.name != null) {
				updated.append("$set", new BasicDBObject("Name", assignment.name));
			}
			if (assignment.type != null) {
				updated.append("$set", new BasicDBObject("Type", assignment.type));
			}
			if (assignment.other != null) {
				updated.append("$set", new BasicDBObject("Other", assignment.other));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (assignment.description != null) {
				updated.append("$set", new BasicDBObject("Description", assignment.description));
			}
			if (assignment.resources != null) {
				updated.append("$set", new BasicDBObject("Resources", assignment.resources));
			}
			if (assignment.latePolicy != null) {
				updated.append("$set", new BasicDBObject("LatePolicy", assignment.latePolicy));
			}
			if (assignment.gradeWeight != null) {
				updated.append("$set", new BasicDBObject("GradeWeight", assignment.gradeWeight));
			}
			if (assignment.openDate != null) {
				updated.append("$set", new BasicDBObject("OpenDate", assignment.openDate));
			}
			if (assignment.dueDate != null) {
				updated.append("$set", new BasicDBObject("DueDate", assignment.dueDate));
			}
			if (assignment.closeDate != null) {
				updated.append("$set", new BasicDBObject("CloseDate", assignment.closeDate));
			}
			if (assignment.imageUrl != null) {
				updated.append("$set", new BasicDBObject("ImageUrl", assignment.imageUrl));
			}
			if (assignment.problemList != null) {
				updated.append("$set", new BasicDBObject("ProblemList", assignment.problemList));
			}

		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (isAdmin) 
			{
				// ONLY ADMIN CAN CHANGE ADMIN OR MOD
				if (assignment.permissions.admin != null) {
					updated.append("$set", new BasicDBObject("Admin", assignment.permissions.admin));
				}
				if (assignment.permissions.mod != null) {
					updated.append("$set", new BasicDBObject("Mod", assignment.permissions.mod));
				}
			}
			if (assignment.permissions.users != null) 
			{
				updated.append("$set", new BasicDBObject("Users", assignment.permissions.users));
			}
		}
		return true;
	}

}
