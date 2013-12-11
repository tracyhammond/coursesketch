package database.course;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.PermissionBuilder;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseManager 
{
	
	public static String mongoInsertCourse(DB dbs, SrlCourse course)
	{
		DBCollection new_user = dbs.getCollection("Courses");
		BasicDBObject query = new BasicDBObject("Description",course.getDescription())
										 .append("Name",course.getName())
										 .append("Access",course.getAccess().getNumber()) 
										 .append("Semesester",course.getSemester())
										 .append("OpenDate", course.getAccessDate().getMillisecond())
										 .append("CloseDate", course.getCloseDate().getMillisecond())
										 .append("Image", course.getImageUrl())
										 .append("Admin", course.getAccessPermission().getAdminPermissionList())
										 .append("Mod",course.getAccessPermission().getModeratorPermissionList())
										 .append("Users", course.getAccessPermission().getUserPermissionList());
		if (course.getAssignmentListList() != null) {
			query.append("AssignmentList",course.getAssignmentListList());
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get("_id").toString();
	}
	
	public static SrlCourse mongoGetCourse(DB dbs, String courseId,String userId, long checkTime) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Courses", new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		System.out.println("corosor is nulll???? " + corsor);
		System.out.println("courses ID: " + courseId);
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

		SrlCourse.Builder exactCourse = SrlCourse.newBuilder();
		
		exactCourse.setDescription((String)corsor.get("Description"));
		exactCourse.setName((String)corsor.get("Name"));
		exactCourse.setSemester((String)corsor.get("Semesester"));
		try {
			exactCourse.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get("OpenDate")).longValue()));
			exactCourse.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get("OpenDate")).longValue()));
		}catch(Exception e ){
			
		}
		exactCourse.setId(courseId); 
		if (corsor.get("Image") != null) {
			exactCourse.setImageUrl((String)corsor.get("Image"));
		}
		// if you are a user the course must be open to view the assignments
		if (isAdmin || isMod || (isUsers && PermissionBuilder.isTimeValid(checkTime, exactCourse.getAccessDate(), exactCourse.getCloseDate()))) {
			if (corsor.get("AssignmentList") != null) {
				exactCourse.addAllAssignmentList((ArrayList)corsor.get("AssignmentList"));
			}
		}
		if (isAdmin) 
		{
			try {
				exactCourse.setAccess(SrlCourse.Accessibility.valueOf((Integer) corsor.get("Access"))); // admin
			} catch(Exception e) {
				
			}
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAllAdminPermission((ArrayList)corsor.get("Admin")); // admin
			permissions.addAllModeratorPermission((ArrayList)corsor.get("Mod"));	 // admin
			permissions.addAllUserPermission((ArrayList)corsor.get("Users")); //admin
			exactCourse.setAccessPermission(permissions.build());
		}
		return exactCourse.build();
		
	}
	
	
	public static boolean mongoUpdateCourse(DB dbs, String courseID, String userId, SrlCourse course) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Courses", new ObjectId(courseID));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection("Courses");
		
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
			if (course.hasSemester()) {
				updateObj = new BasicDBObject("Semesester", course.getSemester());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
			if (course.hasAccessDate()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject("OpenDate", course.getAccessDate().getMillisecond()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.hasCloseDate()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject("CloseDate", course.getCloseDate().getMillisecond()));
			}
			if (course.hasImageUrl()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject("Image", course.getImageUrl()));
			}
			if (course.hasDescription()) {
				updateObj = new BasicDBObject("Description", course.getDescription());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
			if (course.hasName()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject("Name", course.getName()));
			}
			if (course.hasAccess()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject("Access", course.getAccess().getNumber()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.getAccessPermission() != null) {
				SrlPermission permissions = course.getAccessPermission();
				if (permissions.getAdminPermissionList() != null) {
					updated.append("$set", new BasicDBObject("Admin", permissions.getAdminPermissionList()));
				}
				if (permissions.getModeratorPermissionList() != null) {
					updated.append("$set", new BasicDBObject("Mod", permissions.getModeratorPermissionList()));
				}
				if (permissions.getUserPermissionList() != null) {
					updated.append("$set", new BasicDBObject("Users", permissions.getUserPermissionList()));
				}
			}
			
			
		}
		if (isAdmin || isMod) {
			if (course.getAssignmentListList() != null) {
				updated.append("$set", new BasicDBObject("AssignmentList", course.getAssignmentListList()));
			}
		}
		//courses.update(corsor, new BasicDBObject ("$set",updateObj));
		
		return true;
		
	}

}
