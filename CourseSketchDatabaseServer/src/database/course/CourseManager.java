package database.course;

import java.util.ArrayList;
import java.util.Collections;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.PermissionBuilder;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseManager 
{
	
	public static String mongoInsertCourse(DB dbs, CourseBuilder course)
	{
		DBCollection new_user = dbs.getCollection("Courses");
		BasicDBObject query = new BasicDBObject("Description",course.description)
										 .append("Name",course.name)
										 .append("Access",course.access) 
										 .append("Semesester",course.semesester)
										 .append("OpenDate",course.openDate)
										 .append("CloseDate",course.closeDate)
										 .append("Image", course.image)
										 .append("Admin", course.permissions.admin)
										 .append("Mod",course.permissions.mod)
										 .append("Users", course.permissions.users);
		if (course.assignmentList != null) {
			query.append("AssignmentList",course.assignmentList);
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get("_id").toString();
	}
	
	public static CourseBuilder mongoGetCourse(DB dbs, String courseID,String userId, long checkTime) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Courses", new ObjectId(courseID));
		DBObject corsor = myDbRef.fetch();
		
		System.out.println(corsor.get("Admin").getClass().getCanonicalName());
		ArrayList adminList =  (ArrayList<Object>) corsor.get("Admin"); //convert to ArrayList<String>
		ArrayList modList =  (ArrayList<Object>) corsor.get("Mod"); //convert to ArrayList<String>
		ArrayList usersList =  (ArrayList<Object>) corsor.get("Users"); //convert to ArrayList<String>
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId,adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		CourseBuilder exactCourse = new CourseBuilder();
		
		exactCourse.setDescription((String)corsor.get("Description"));
		exactCourse.setName((String)corsor.get("Name"));
		exactCourse.setSemesester((String)corsor.get("Semesester"));
		exactCourse.setOpenDate((String)corsor.get("OpenDate"));
		exactCourse.setCloseDate((String)corsor.get("CloseDate"));
		exactCourse.setImage((String)corsor.get("Image"));
		// if you are a user the course must be open to view the assignments
		if (isAdmin || isMod || (isUsers && PermissionBuilder.isTimeValid(checkTime, exactCourse.openDate, exactCourse.closeDate))) {
			exactCourse.setAssignmentList((ArrayList)corsor.get("AssignmentList"));	
		}
		if (isAdmin) 
		{
			exactCourse.setAccess((String)corsor.get("Access")); // admin
			exactCourse.permissions.setAdmin((ArrayList)corsor.get("Admin")); // admin
			exactCourse.permissions.setMod((ArrayList)corsor.get("Mod"));	 // admin
			exactCourse.permissions.setUsers((ArrayList)corsor.get("Users")); //admin
		}
		return exactCourse;
		
	}
	
	
	public static boolean mongoUpdateCourse(DB dbs, String courseID,String userId,CourseBuilder course) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("Courses");
		DBRef myDbRef = new DBRef(dbs, "Courses", new ObjectId(courseID));
		DBObject corsor = myDbRef.fetch();
		
		ArrayList adminList = (ArrayList<Object>)corsor.get("Admin");
		ArrayList modList = (ArrayList<Object>)corsor.get("Mod");
		boolean isAdmin,isMod;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
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
		if (isAdmin || isMod) {
			if (course.assignmentList != null) {
				updated.append("$set", new BasicDBObject("AssignmentList", course.assignmentList));
			}
		}
		return true;
		
	}

}
