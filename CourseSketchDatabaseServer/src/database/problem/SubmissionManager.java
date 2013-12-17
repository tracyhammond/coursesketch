package database.problem;

import static database.StringConstants.*;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class SubmissionManager 
{
	/*
	public static String mongoInsertCourse(DB dbs, SrlSubmission submission)
	{
		DBCollection new_user = dbs.getCollection("Solutions");
		BasicDBObject query = new BasicDBObject(SCHOOL_ID,submission.getschoolId())
										 .append(UPDATE_LIST,submission.getupdateList())
										 .append(SKETCH, submission.getsketch())
										 .append(EXTRA_DATA, submission.getextraData())
										 .append(VIEW_PERMISSION, submission.getviewPermissions());
										 
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get("SELF_ID").toString();
	}
	
	public static SrlCourse mongoGetSolutions(DB dbs, String submissionId,String schoolId) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "Solutions", new ObjectId(submissionId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Course was not found with the following ID " + submissionId);
		}
		ArrayList adminList =  (ArrayList<Object>) corsor.get(ADMIN); //convert to ArrayList<String>
		ArrayList modList =  (ArrayList<Object>) corsor.get(MOD); //convert to ArrayList<String>
		ArrayList usersList =  (ArrayList<Object>) corsor.get(USERS); //convert to ArrayList<String>
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, schoolId,adminList);
		isMod = Authenticator.checkAuthentication(dbs, schoolId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, schoolId, usersList);

		if(!isAdmin && !isMod && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		//need to figure out how to add the SrlSolutin which is similar to SrlCourse
		SrlSolution.Builder exactCourse = SrlSolution.newBuilder();
		exactCourse.setSemester((String)corsor.get(SCHOOLID));
		exactCourse.setSemester((String)corsor.get(UPDATE_LIST));
		exactCourse.setSemester((String)corsor.get(SKETCH));
		exactCourse.setSemester((String)corsor.get(EXTRA_DATA));
		exactCourse.setSemester((String)corsor.get(VIEW_PERMISSION));
		
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
	
	
	public static boolean mongoUpdateSolutions(DB dbs, String submissionId, String userId, SrlCourse course) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Solutions", new ObjectId(submissionId));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection("Solutions");
		
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
			if (course.hasSemester()) 
			{
				updateObj = new BasicDBObject(SCHOOLID, course.getSchoolId());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
			if (course.hasAccessDate()) 
			{
				
				updateObj = new BasicDBObject(UPDATE_LIST, course.getUpdateList);
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.hasCloseDate()) 
			{
				updateObj = new BasicDBObject(SKETCH, course.getSketch);
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
			}
			
			if (course.hasImageUrl()) {
				updateObj = new BasicDBObject(EXTRA_DATA, course.getExtraData);
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
			}
			if (course.hasDescription()) {
				updateObj = new BasicDBObject(VIEW_PERMISSION, course.getViewPermission());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			
			
			
		}
		//courses.update(corsor, new BasicDBObject ("$set",updateObj));
		
		return true;
		
	}
*/
}
