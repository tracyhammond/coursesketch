package database.institution;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlAssignment.LatePolicy;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;

public class AssignmentManager 
{
	public static String mongoInsertAssignment(DB dbs, String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException
	{
		DBCollection new_user = dbs.getCollection("Assignments");
		AuthType auth = new AuthType();
		auth.checkAdminOrMod = true;
		if (!Authenticator.mognoIsAuthenticated(dbs, COURSE_COLLECTION, assignment.getCourseId(), userId, 0, auth))
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		BasicDBObject query = new BasicDBObject(COURSE_ID,assignment.getCourseId())
				.append(NAME,assignment.getName())
				.append(ASSIGNMENT_TYPE,assignment.getType().getNumber()) 
				.append(ASSIGNMENT_OTHER_TYPE,assignment.getOther())
				.append(DESCRIPTION,assignment.getDescription())
				.append(ASSIGNMENT_RESOURCES,assignment.getLinksList())
				.append(GRADE_WEIGHT,assignment.getGradeWeight())
				.append(ACCESS_DATE, assignment.getAccessDate().getMillisecond())
				.append(DUE_DATE, assignment.getDueDate().getMillisecond())
				.append(CLOSE_DATE,assignment.getCloseDate().getMillisecond())
				.append(IMAGE, assignment.getImageUrl())
				.append(ADMIN, assignment.getAccessPermission().getAdminPermissionList())
				.append(MOD,assignment.getAccessPermission().getModeratorPermissionList())
				.append(USERS, assignment.getAccessPermission().getUserPermissionList());
		if (assignment.hasLatePolicy()) {
			query.append(LATE_POLICY_FUNCTION_TYPE, assignment.getLatePolicy().getFunctionType())
					.append(LATE_POLICY_RATE, assignment.getLatePolicy().getRate())
					.append(LATE_POLICY_SUBTRACTION_TYPE, assignment.getLatePolicy().getSubtractionType());
			if (assignment.getLatePolicy().getFunctionType() == LatePolicy.FunctionType.WINDOW_FUNCTION) {
				query.append(LATE_POLICY_TIME_FRAME_TYPE, assignment.getLatePolicy().getTimeFrameType());
			}
		}
		if (assignment.getProblemListList() != null) {
			query.append(PROBLEM_LIST, assignment.getProblemListList());
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);

		// inserts the id into the previous the course
		CourseManager.mongoInsertIntoCourse(dbs, assignment.getCourseId(),corsor.get(SELF_ID).toString() );

		return corsor.get(SELF_ID).toString();
	}

	public static SrlAssignment mongoGetAssignment(DB dbs, String assignmentId, String userId, long checkTime) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Assignment was not found with the following ID " + assignmentId, true);
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

		// check to make sure the assignment is within the time period that the course is open and the user is in the course
		AuthType auth = new AuthType();
		auth.checkDate = true;
		auth.user = true;
		if (isUsers) {
			if (!Authenticator.mognoIsAuthenticated(dbs, COURSE_COLLECTION, (String)corsor.get(COURSE_ID), userId, checkTime, auth))
			{
				throw new AuthenticationException(AuthenticationException.INVALID_DATE);
			}
		}

		SrlAssignment.Builder exactAssignment = SrlAssignment.newBuilder();

		exactAssignment.setId(assignmentId);
		exactAssignment.setCourseId((String)corsor.get(COURSE_ID));
		exactAssignment.setName((String)corsor.get(NAME));
		exactAssignment.setType(SrlAssignment.AssignmentType.valueOf((Integer)corsor.get(ASSIGNMENT_TYPE)));
		exactAssignment.setOther((String)corsor.get(ASSIGNMENT_OTHER_TYPE));
		exactAssignment.setDescription((String)corsor.get(DESCRIPTION));
		exactAssignment.addAllLinks((List)corsor.get(ASSIGNMENT_RESOURCES));
		exactAssignment.setGradeWeight((String)corsor.get(GRADE_WEIGHT));

		if (isAdmin || isMod) { 
			try {
				LatePolicy.Builder latePolicy = LatePolicy.newBuilder();
				latePolicy.setFunctionType(SrlAssignment.LatePolicy.FunctionType.valueOf((Integer)corsor.get(LATE_POLICY_FUNCTION_TYPE)));
				latePolicy.setRate(Float.parseFloat(""+corsor.get(LATE_POLICY_RATE))); // safety cast to string then parse to float
				latePolicy.setSubtractionType((Boolean)corsor.get(LATE_POLICY_SUBTRACTION_TYPE));
				if (latePolicy.getFunctionType() == LatePolicy.FunctionType.WINDOW_FUNCTION) {
					latePolicy.setTimeFrameType(SrlAssignment.LatePolicy.TimeFrame.valueOf((Integer)corsor.get(LATE_POLICY_TIME_FRAME_TYPE)));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		try {
			exactAssignment.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(ACCESS_DATE)).longValue()));
			exactAssignment.setDueDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(DUE_DATE)).longValue()));
			exactAssignment.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(CLOSE_DATE)).longValue()));
		}catch(Exception e ){
			e.printStackTrace();
		}

		exactAssignment.setImageUrl((String)corsor.get(IMAGE));
		exactAssignment.addAllProblemList((List)corsor.get(PROBLEM_LIST));

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

	public static boolean mongoUpdateAssignment(DB dbs, String assignmentId,String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException
	{
		boolean update = false;
		DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection(ASSIGNMENT_COLLECTION);

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
				updateObj = new BasicDBObject(NAME, assignment.getName());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
				update = true;
			}
			if (assignment.hasType()) {
				updateObj = new BasicDBObject(ASSIGNMENT_TYPE, assignment.getType().getNumber());
				courses.update(corsor, new BasicDBObject ("$set",updateObj));
				update = true;
			}
			if (assignment.hasOther()) {
				updateObj = new BasicDBObject(ASSIGNMENT_OTHER_TYPE,assignment.getOther());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (assignment.hasDescription()) {
				updateObj = new BasicDBObject(DESCRIPTION, assignment.getDescription());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.getLinksList() != null) {
				updateObj = new BasicDBObject(ASSIGNMENT_RESOURCES, assignment.getLinksList());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.hasLatePolicy()) {
			//	updateObj = new BasicDBObject(LATE_POLICY, assignment.getLatePolicy().getNumber());
			//	courses.update(corsor, new BasicDBObject ("$set", updateObj));
			}
			if (assignment.hasGradeWeight()) {
				updateObj = new BasicDBObject(GRADE_WEIGHT, assignment.getGradeWeight());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.hasAccessDate()) {
				updateObj = new BasicDBObject(ACCESS_DATE, assignment.getAccessDate().getMillisecond());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}	
			if (assignment.hasDueDate()) {
				updateObj = new BasicDBObject(DUE_DATE, assignment.getDueDate().getMillisecond());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.hasCloseDate()) {
				updateObj = new BasicDBObject(CLOSE_DATE, assignment.getCloseDate().getMillisecond());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.hasImageUrl()) {
				updateObj = new BasicDBObject(IMAGE, assignment.getImageUrl());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}
			if (assignment.getProblemListCount() > 0) {
				updateObj = new BasicDBObject(PROBLEM_LIST, assignment.getProblemListList());
				courses.update(corsor, new BasicDBObject ("$set", updateObj));
				update = true;
			}

		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (assignment.hasAccessPermission()) {
				SrlPermission permissions = assignment.getAccessPermission();
				if (isAdmin)
				{
					// ONLY ADMIN CAN CHANGE ADMIN OR MOD
					if (permissions.getAdminPermissionCount() > 0) {
						updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
						updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
						courses.update(corsor, new BasicDBObject ("$set", updateObj));
					}
					if (permissions.getModeratorPermissionCount() > 0) {
						updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
						courses.update(corsor, new BasicDBObject ("$set", updateObj));
					}
				}
				if (permissions.getUserPermissionCount() > 0) 
				{
					updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
					courses.update(corsor, new BasicDBObject ("$set", updateObj));
				}
			}
		}
		if(update == true)
		{
			String[] users = (String[]) corsor.get(USERS);
			for(int i = 0;i < users.length;i++)
			{
				UserUpdateHandler.InsertUpdates(dbs, users[i], assignmentId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
			}
			
		}
		return true;
	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * With that being said this allows an assignment to be updated adding the problemId to its list of items.
	 */
	static boolean mongoInsert(DB dbs, String assignmentId, String problemId) {
		DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		DBCollection courses = dbs.getCollection(ASSIGNMENT_COLLECTION);
		DBObject updateObj = new BasicDBObject(PROBLEM_LIST, problemId);
		courses.update(corsor, new BasicDBObject ("$addToSet", updateObj));
		return true;
	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * This is used to copy permissions from the parent course into the current assignment.
	 */
	static void mongoInsertDefaultGroupId(DB dbs, String assignmentId, ArrayList<String>[] ids) {
		DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		DBCollection assignments = dbs.getCollection(ASSIGNMENT_COLLECTION);

		BasicDBObject updateQuery = null;
		BasicDBObject fieldQuery = null;
		for(int k = 0; k <ids.length; k++) {
			ArrayList<String> list = ids[k];
			String field = (k == 0) ? ADMIN : (k == 1 ? MOD : USERS); // k = 0 ADMIN, k = 1 MOD, k = 2 USERS
			if (k == 0 ) {
				fieldQuery = new BasicDBObject(field, new BasicDBObject("$each", list));
				updateQuery = new BasicDBObject("$addToSet", fieldQuery);
			} else {
				fieldQuery.append(field, new BasicDBObject("$each", list));
			}
		}
		System.out.println(updateQuery);
		assignments.update(corsor, updateQuery);
	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * Returns a list of Id for the default group for an assignment.
	 *
	 * the Ids are ordered as so: AdminGroup, ModGroup, UserGroup
	 */
	static ArrayList<String>[] mongoGetDefaultGroupId(DB dbs, String assignmentId) {
		DBRef myDbRef = new DBRef(dbs, ASSIGNMENT_COLLECTION, new ObjectId(assignmentId));
		DBObject corsor = myDbRef.fetch();
		ArrayList<String>[] returnValue = new ArrayList[3];
		returnValue[0] = (ArrayList)corsor.get(ADMIN);
		returnValue[1] = (ArrayList)corsor.get(MOD);
		returnValue[2] = (ArrayList)corsor.get(USERS);
		return returnValue;
	}
}
