package database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseManager 
{
	
	private static String mongoInsertCourse(DB dbs, CourseBuilder course)
	{
		DBCollection new_user = dbs.getCollection("Courses");
		BasicDBObject query = new BasicDBObject("Description",course.description)
										 .append("Name",course.name)
										 .append("Access",course.access) 
										 .append("Semesester",course.semesester)
										 .append("OpenDate",course.openDate)
										 .append("CloseDate",course.closeDate)
										 .append("Image", course.image)
										 .append("AssignmentList",course.assignmentList)
										 .append("Admin", course.permissions.admin)
										 .append("Mod",course.permissions.mod)
										 .append("Users", course.permissions.users);
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return (String) corsor.get("_id");
	}
	
	static CourseBuilder mongoGetCourse(DB dbs, String courseID,String userId) throws AuthenticationException
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
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);
		
		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException();
		}
		
		CourseBuilder exactCourse = new CourseBuilder();
		
		exactCourse.setDescription((String)corsor.get("Description"));
		exactCourse.setName((String)corsor.get("Name"));
		exactCourse.setSemesester((String)corsor.get("Semesester"));
		exactCourse.setOpenDate((String)corsor.get("OpenDate"));
		exactCourse.setCloseDate((String)corsor.get("CloseDate"));
		exactCourse.setImage((String)corsor.get("Image"));
		exactCourse.setAssignmentList((String[])corsor.get("AssignmentList"));	
		if (isAdmin) 
		{
			exactCourse.setAccess((String)corsor.get("Access")); // admin
			exactCourse.permissions.setAdmin((String[])corsor.get("Admin")); // admin
			exactCourse.permissions.setMod((String[])corsor.get("Mod"));	 // admin
			exactCourse.permissions.setUsers((String[])corsor.get("Users")); //admin
		}
		
		return exactCourse;
		
	}
	
	
	static boolean mongoUpdateCourse(DB dbs, String courseID,String userId,CourseBuilder course) throws AuthenticationException
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
			
			if (course.semesester != null) {
				updated.append("$set", new BasicDBObject("Semesester", course.semesester));
			}
			if (course.openDate != null) {
				updated.append("$set", new BasicDBObject("OpenDate", course.openDate));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.closeDate != null) {
				updated.append("$set", new BasicDBObject("CloseDate", course.closeDate));
			}
			if (course.image != null) {
				updated.append("$set", new BasicDBObject("Image", course.image));
			}
			if (course.assignmentList != null) {
				updated.append("$set", new BasicDBObject("AssignmentList", course.assignmentList));
			}
			if (course.description != null) {
				updated.append("$set", new BasicDBObject("Description", course.description));
			}
			if (course.name != null) {
				updated.append("$set", new BasicDBObject("Name", course.name));
			}
			if (course.access != null) {
				updated.append("$set", new BasicDBObject("Access", course.access));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.permissions.admin != null) {
				updated.append("$set", new BasicDBObject("Admin", course.permissions.admin));
			}
			if (course.permissions.mod != null) {
				updated.append("$set", new BasicDBObject("Mod", course.permissions.mod));
			}
			if (course.permissions.users != null) {
				updated.append("$set", new BasicDBObject("Users", course.permissions.users));
			}
		}
		return true;
		
	}

}
