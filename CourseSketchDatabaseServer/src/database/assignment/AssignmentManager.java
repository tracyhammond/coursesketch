package database.assignment;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlAssignment;
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
import database.course.CourseManager;

public class AssignmentManager 
{
	public static String mongoInsertAssignment(DB dbs, String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException
	{
		DBCollection new_user = dbs.getCollection("Assignments");
		SrlCourse course = CourseManager.mongoGetCourse(dbs, assignment.getCourseId(), userId, 0); // user can not insert anyways so we are good.
		if (course.getAccessPermission().getAdminPermissionList() == null) {
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		boolean isAdmin = Authenticator.checkAuthentication(dbs, userId, course.getAccessPermission().getAdminPermissionList());
		boolean isMod = Authenticator.checkAuthentication(dbs, userId, course.getAccessPermission().getModeratorPermissionList());

		if(!isAdmin && !isMod)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		BasicDBObject query = new BasicDBObject(COURSE_ID,assignment.getCourseId())
										 .append(NAME,assignment.getName())
										 .append(ASSIGNMENT_TYPE,assignment.getType().getNumber()) 
										 .append(ASSIGNMENT_OTHER_TYPE,assignment.getOther())
										 .append(DESCRIPTION,assignment.getDescription())
										 .append(ASSIGNMENT_RESOURCES,assignment.getLinksList())
										 .append(LATE_POLICY, assignment.getLatePolicy().getNumber())
										 .append(GRADE_WEIGHT,assignment.getGradeWeight())
										 .append(ACCESS_DATE, assignment.getAccessDate().getMillisecond())
										 .append(DUE_DATE, assignment.getDueDate().getMillisecond())
										 .append(CLOSE_DATE,assignment.getCloseDate().getMillisecond())
										 .append(IMAGE, assignment.getImageUrl())
										 .append(ADMIN, assignment.getAccessPermission().getAdminPermissionList())
										 .append(MOD,assignment.getAccessPermission().getModeratorPermissionList())
										 .append(USERS, assignment.getAccessPermission().getUserPermissionList());
		if (assignment.getProblemListList() != null) {
			query.append(PROBLEM_LIST, assignment.getProblemListList());
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);

		List<String> idList = new ArrayList<String>();
		if(course.getAssignmentListList() != null) {
			idList.addAll(course.getAssignmentListList());
		}
		idList.add((String) corsor.get(SELF_ID).toString());
		SrlCourse.Builder newCourse = SrlCourse.newBuilder();
		newCourse.addAllAssignmentList(idList);
		CourseManager.mongoUpdateCourse(dbs, assignment.getCourseId(), userId, newCourse.buildPartial());
		return (String) corsor.get(SELF_ID).toString();
	}

	public static SrlAssignment mongoGetAssignment(DB dbs, String assignmentId, String userId, long checkTime) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "Assignments", new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Assignment was not found with the following ID " + assignmentId);
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

		SrlAssignment.Builder exactAssignment = SrlAssignment.newBuilder();

		exactAssignment.setId(assignmentId);
		exactAssignment.setCourseId((String)corsor.get(COURSE_ID));
		exactAssignment.setName((String)corsor.get(NAME));
		exactAssignment.setType(SrlAssignment.AssignmentType.valueOf((Integer)corsor.get(ASSIGNMENT_TYPE)));
		exactAssignment.setOther((String)corsor.get(ASSIGNMENT_OTHER_TYPE));
		exactAssignment.setDescription((String)corsor.get(DESCRIPTION));
		exactAssignment.addAllLinks((List)corsor.get(ASSIGNMENT_RESOURCES));
		exactAssignment.setLatePolicy(SrlAssignment.LatePolicy.valueOf((Integer)corsor.get(LATE_POLICY)));
		exactAssignment.setGradeWeight((String)corsor.get(GRADE_WEIGHT));

		try {
			exactAssignment.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(ACCESS_DATE)).longValue()));
			exactAssignment.setDueDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(DUE_DATE)).longValue()));
			exactAssignment.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(CLOSE_DATE)).longValue()));
		}catch(Exception e ){
			e.printStackTrace();
		}

		exactAssignment.setImageUrl((String)corsor.get(IMAGE));
		exactAssignment.addAllProblemList((List)corsor.get(PROBLEM_LIST));

		if (isUsers) {
			SrlCourse course = CourseManager.mongoGetCourse(dbs, exactAssignment.getCourseId(), userId, checkTime);
			if (!PermissionBuilder.isTimeValid(checkTime, course.getAccessDate(), course.getCloseDate())) {
				throw new AuthenticationException(AuthenticationException.EARLY_ACCESS);
			}
		}
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		if (isAdmin) 
		{
			permissions.addAllAdminPermission((ArrayList)corsor.get(ADMIN)); // admin
			permissions.addAllModeratorPermission((ArrayList)corsor.get(MOD));	 // admin
		}
		if (isAdmin || isMod) {
			permissions.addAllUserPermission((ArrayList)corsor.get(USERS)); // mod
			exactAssignment.setAccessPermission(permissions.build());
		}
		return exactAssignment.build();

	}

	public static boolean mongoUpdateAssignment(DB dbs, String assignmentId,String userId, SrlAssignment assignment) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, "Assignments", new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();

		ArrayList adminList = (ArrayList<Object>)corsor.get("Admin");
		ArrayList modList = (ArrayList<Object>)corsor.get("Mod");
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
			if (assignment.hasName()) {
				updated.append("$set", new BasicDBObject(NAME, assignment.getName()));
			}
			if (assignment.hasType()) {
				updated.append("$set", new BasicDBObject(ASSIGNMENT_TYPE, assignment.getType()));
			}
			if (assignment.hasOther()) {
				updated.append("$set", new BasicDBObject(ASSIGNMENT_OTHER_TYPE, assignment.getOther()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (assignment.hasDescription()) {
				updated.append("$set", new BasicDBObject(DESCRIPTION, assignment.getDescription()));
			}
			if (assignment.getLinksList() != null) {
				updated.append("$set", new BasicDBObject(ASSIGNMENT_RESOURCES, assignment.getLinksList()));
			}
			if (assignment.hasLatePolicy()) {
				updated.append("$set", new BasicDBObject(LATE_POLICY, assignment.getLatePolicy().getNumber()));
			}
			if (assignment.hasGradeWeight()) {
				updated.append("$set", new BasicDBObject(GRADE_WEIGHT, assignment.getGradeWeight()));
			}
			if (assignment.hasAccessDate()) {
				updated.append("$set", new BasicDBObject(ACCESS_DATE, assignment.getAccessDate().getMillisecond()));
			}
			if (assignment.hasDueDate()) {
				updated.append("$set", new BasicDBObject(DUE_DATE, assignment.getDueDate().getMillisecond()));
			}
			if (assignment.hasCloseDate()) {
				updated.append("$set", new BasicDBObject(CLOSE_DATE, assignment.getCloseDate().getMillisecond()));
			}
			if (assignment.hasImageUrl()) {
				updated.append("$set", new BasicDBObject(IMAGE, assignment.getImageUrl()));
			}
			if (assignment.getProblemListCount() > 0) {
				updated.append("$set", new BasicDBObject(PROBLEM_LIST, assignment.getProblemListList()));
			}

		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (assignment.hasAccessPermission()) {
				SrlPermission permissions = assignment.getAccessPermission();
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
