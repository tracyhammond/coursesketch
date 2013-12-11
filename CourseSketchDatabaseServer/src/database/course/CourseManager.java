package database.course;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.PermissionBuilder;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
public class CourseManager 
{
	
	public static String mongoInsertCourse(DB dbs, SrlCourse course)
	{
		DBCollection new_user = dbs.getCollection("Courses");
		BasicDBObject query = new BasicDBObject(DESCRIPTION,course.getDescription())
										 .append(NAME,course.getName())
										 .append(COURSE_ACCESS,course.getAccess().getNumber()) 
										 .append(COURSE_SEMESTER,course.getSemester())
										 .append(ACCESS_DATE, course.getAccessDate().getMillisecond())
										 .append(CLOSE_DATE, course.getCloseDate().getMillisecond())
										 .append(IMAGE, course.getImageUrl())
										 .append(ADMIN, course.getAccessPermission().getAdminPermissionList())
										 .append(MOD,course.getAccessPermission().getModeratorPermissionList())
										 .append(USERS, course.getAccessPermission().getUserPermissionList());
		if (course.getAssignmentListList() != null) {
			query.append(ASSIGNMENT_LIST,course.getAssignmentListList());
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}
	
	public static SrlCourse mongoGetCourse(DB dbs, String courseId,String userId, long checkTime) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "Courses", new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
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

		SrlCourse.Builder exactCourse = SrlCourse.newBuilder();

		exactCourse.setDescription((String)corsor.get(DESCRIPTION));
		exactCourse.setName((String)corsor.get(NAME));
		exactCourse.setSemester((String)corsor.get(COURSE_SEMESTER));
		try {
			exactCourse.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(ACCESS_DATE)).longValue()));
			exactCourse.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(CLOSE_DATE)).longValue()));
		}catch(Exception e ){
			e.printStackTrace();
		}
		exactCourse.setId(courseId); 
		if (corsor.get("Image") != null) {
			exactCourse.setImageUrl((String)corsor.get(IMAGE));
		}
		// if you are a user the course must be open to view the assignments
		if (isAdmin || isMod || (isUsers && PermissionBuilder.isTimeValid(checkTime, exactCourse.getAccessDate(), exactCourse.getCloseDate()))) {
			if (corsor.get(ASSIGNMENT_LIST) != null) {
				exactCourse.addAllAssignmentList((List)corsor.get(ASSIGNMENT_LIST));
			}
		}
		if (isAdmin) 
		{
			try {
				exactCourse.setAccess(SrlCourse.Accessibility.valueOf((Integer) corsor.get(COURSE_ACCESS))); // admin
			} catch(Exception e) {
				
			}
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAllAdminPermission((ArrayList)corsor.get(ADMIN)); // admin
			permissions.addAllModeratorPermission((ArrayList)corsor.get(MOD));	 // admin
			permissions.addAllUserPermission((ArrayList)corsor.get(USERS)); //admin
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
		
		ArrayList adminList = (ArrayList<Object>)corsor.get(ADMIN);
		ArrayList modList = (ArrayList<Object>)corsor.get(MOD);
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
				updateObj = new BasicDBObject(COURSE_SEMESTER, course.getSemester());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
			if (course.hasAccessDate()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject(ACCESS_DATE, course.getAccessDate().getMillisecond()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.hasCloseDate()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject(CLOSE_DATE, course.getCloseDate().getMillisecond()));
			}
			if (course.hasImageUrl()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject(IMAGE, course.getImageUrl()));
			}
			if (course.hasDescription()) {
				updateObj = new BasicDBObject(DESCRIPTION, course.getDescription());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
			if (course.hasName()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject(NAME, course.getName()));
			}
			if (course.hasAccess()) {
				((BasicDBObject) updateObj).append("$set", new BasicDBObject(COURSE_ACCESS, course.getAccess().getNumber()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.getAccessPermission() != null) {
				SrlPermission permissions = course.getAccessPermission();
				if (permissions.getAdminPermissionList() != null) {
					updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
				}
				if (permissions.getModeratorPermissionList() != null) {
					updated.append("$set", new BasicDBObject(MOD, permissions.getModeratorPermissionList()));
				}
				if (permissions.getUserPermissionList() != null) {
					updated.append("$set", new BasicDBObject(USERS, permissions.getUserPermissionList()));
				}
			}
			
			
		}
		if (isAdmin || isMod) {
			if (course.getAssignmentListList() != null) {
				updated.append("$set", new BasicDBObject(ASSIGNMENT_LIST, course.getAssignmentListList()));
			}
		}
		//courses.update(corsor, new BasicDBObject ("$set",updateObj));
		
		return true;
		
	}

}
