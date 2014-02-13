package database.institution;

import static database.StringConstants.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlPermission.Builder;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class CourseManager {
	static String mongoInsertCourse(DB dbs, SrlCourse course) {
		DBCollection new_user = dbs.getCollection(COURSE_COLLECTION);
		BasicDBObject query = new BasicDBObject(DESCRIPTION, course.getDescription()).append(NAME, course.getName())
				.append(COURSE_ACCESS, course.getAccess().getNumber()).append(COURSE_SEMESTER, course.getSemester())
				.append(ACCESS_DATE, course.getAccessDate().getMillisecond()).append(CLOSE_DATE, course.getCloseDate().getMillisecond())
				.append(IMAGE, course.getImageUrl()).append(ADMIN, course.getAccessPermission().getAdminPermissionList())
				.append(MOD, course.getAccessPermission().getModeratorPermissionList())
				.append(USERS, course.getAccessPermission().getUserPermissionList());
		if (course.getAssignmentListList() != null) {
			query.append(ASSIGNMENT_LIST, course.getAssignmentListList());
		}
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	static SrlCourse mongoGetCourse(DB dbs, String courseId, String userId, long checkTime) throws AuthenticationException,
			DatabaseAccessException {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
		}
		ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN); // convert to ArrayList<String>
		ArrayList modList = (ArrayList<Object>) corsor.get(MOD); // convert to ArrayList<String>
		ArrayList usersList = (ArrayList<Object>) corsor.get(USERS); // convert to ArrayList<String>
		boolean isAdmin, isMod, isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if (!isAdmin && !isMod && !isUsers) {
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		SrlCourse.Builder exactCourse = SrlCourse.newBuilder();
		exactCourse.setDescription((String) corsor.get(DESCRIPTION));
		exactCourse.setName((String) corsor.get(NAME));
		if (corsor.get(COURSE_SEMESTER) != null) {
			exactCourse.setSemester((String) corsor.get(COURSE_SEMESTER));
		}
		try {
			exactCourse.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(ACCESS_DATE)).longValue()));
			exactCourse.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(CLOSE_DATE)).longValue()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		exactCourse.setId(courseId);
		if (corsor.get(IMAGE) != null) {
			exactCourse.setImageUrl((String) corsor.get(IMAGE));
		}
		// if you are a user the course must be open to view the assignments
		if (isAdmin || isMod || (isUsers && Authenticator.isTimeValid(checkTime, exactCourse.getAccessDate(), exactCourse.getCloseDate()))) {
			if (corsor.get(ASSIGNMENT_LIST) != null) {
				exactCourse.addAllAssignmentList((List) corsor.get(ASSIGNMENT_LIST));
			}
		}
		if (isAdmin) {
			try {
				exactCourse.setAccess(SrlCourse.Accessibility.valueOf((Integer) corsor.get(COURSE_ACCESS))); // admin
			} catch (Exception e) {

			}
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
			permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
			permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // admin
			exactCourse.setAccessPermission(permissions.build());
		}
		return exactCourse.build();

	}

	public static boolean mongoUpdateCourse(DB dbs, String courseID, String userId, SrlCourse course) throws AuthenticationException {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseID));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection(COURSE_COLLECTION);

		ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
		ArrayList modList = (ArrayList<Object>) corsor.get(MOD);
		boolean isAdmin, isMod;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);

		if (!isAdmin && !isMod) {
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject updated = new BasicDBObject();
		if (isAdmin) {
			if (course.hasSemester()) {
				updateObj = new BasicDBObject(COURSE_SEMESTER, course.getSemester());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}
			if (course.hasAccessDate()) {

				updateObj = new BasicDBObject(ACCESS_DATE, course.getAccessDate().getMillisecond());
				courses.update(corsor, new BasicDBObject("$set", updateObj));

			}
			// Optimization: have something to do with pulling values of an
			// array and pushing values to an array
			if (course.hasCloseDate()) {
				updateObj = new BasicDBObject(CLOSE_DATE, course.getCloseDate().getMillisecond());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}

			if (course.hasImageUrl()) {
				updateObj = new BasicDBObject(IMAGE, course.getImageUrl());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}
			if (course.hasDescription()) {
				updateObj = new BasicDBObject(DESCRIPTION, course.getDescription());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}
			if (course.hasName()) {
				updateObj = new BasicDBObject(NAME, course.getName());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}
			if (course.hasAccess()) {
				updateObj = new BasicDBObject(COURSE_ACCESS, course.getAccess().getNumber());
				courses.update(corsor, new BasicDBObject("$set", updateObj));

			}
			// Optimization: have something to do with pulling values of an array and pushing values to an array
			if (course.hasAccessPermission()) {
				System.out.println("Updating permissions!");
				SrlPermission permissions = course.getAccessPermission();
				if (permissions.getAdminPermissionList() != null) {
					updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
					courses.update(corsor, new BasicDBObject("$set", updateObj));
				}
				if (permissions.getModeratorPermissionList() != null) {
					updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
					courses.update(corsor, new BasicDBObject("$set", updateObj));
				}
				if (permissions.getUserPermissionList() != null) {
					updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
					courses.update(corsor, new BasicDBObject("$set", updateObj));
				}
			}
		}
		if (isAdmin || isMod) {
			if (course.getAssignmentListList() != null) {
				updateObj = new BasicDBObject(ASSIGNMENT_LIST, course.getAssignmentListList());
				courses.update(corsor, new BasicDBObject("$set", updateObj));
			}
		}
		// courses.update(corsor, new BasicDBObject ("$set",updateObj));

		return true;

	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * With that being said this allows a course to be updated adding the assignmentId to its list of items.
	 */
	static boolean mongoInsertIntoCourse(DB dbs, String courseId, String assignmentId) {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
		updateObj = new BasicDBObject(ASSIGNMENT_LIST, assignmentId);
		courses.update(corsor, new BasicDBObject ("$addToSet",updateObj));
		return true;
	}

	public static ArrayList<SrlCourse> mongoGetAllPublicCourses(DB dbs) {
		DBCollection courseTable = dbs.getCollection(COURSE_COLLECTION);
		
		ArrayList<SrlCourse> resultList = new ArrayList<SrlCourse>();

		// checks for all public courses.
		DBObject publicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.PUBLIC.getNumber()); // the value for a public course
		DBCursor cursor = courseTable.find(publicCheck);
		while (cursor.hasNext()) {
			SrlCourse.Builder build = SrlCourse.newBuilder();
			DBObject foundCourse = cursor.next();
			build.setId(foundCourse.get(SELF_ID).toString());
			build.setDescription(foundCourse.get(DESCRIPTION).toString());
			build.setName(foundCourse.get(NAME).toString());
			build.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
			build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
			resultList.add(build.build());
		}

		// checks for all super public courses.
		DBObject superPublicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.SUPER_PUBLIC.getNumber()); // the value for a superpublic course
		cursor = courseTable.find(superPublicCheck);
		while (cursor.hasNext()) {
			SrlCourse.Builder build = SrlCourse.newBuilder();
			DBObject foundCourse = cursor.next();
			build.setId(foundCourse.get(SELF_ID).toString());
			build.setDescription(foundCourse.get(DESCRIPTION).toString());
			build.setName(foundCourse.get(NAME).toString());
			build.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
			build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
			resultList.add(build.build());
		}
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn;	
			conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/problembank?" +
			                               "user=srl&password=sketchrec");
			Statement stmt = conn.createStatement() ;
			String query = "select * from CourseInfo where id=3;" ;
			ResultSet rs = stmt.executeQuery(query) ;
			SrlCourse.Builder build = SrlCourse.newBuilder();
			build.setId("3");
			build.setDescription(rs.getString("Description"));
			build.setName(rs.getString("Name"));
			//build.setAccessDate(new DateTime());
			
				//	RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
		//build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
//ADD THIS!				resultList.add(build.build());
		} catch (Exception e){}
		return resultList;
	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * With that being said this allows the default ids to be inserted
	 */
	static void mongoInsertDefaultGroupId(DB dbs, String courseId, String userGroupId, String modGroupId, String adminGroupId) {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
		BasicDBObject listQueries = new BasicDBObject(ADMIN_GROUP_ID, adminGroupId).append(MOD_GROUP_ID, modGroupId).append(USER_GROUP_ID, userGroupId);
		DBObject courseQuery = new BasicDBObject("$set", listQueries);
		courses.update(corsor, courseQuery);
	}

	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * Returns a list of Id for the default group for an assignment.
	 *
	 * The list are ordered as so: AdminGroup, ModGroup, UserGroup
	 */
	static ArrayList<String>[] mongoGetDefaultGroupList(DB dbs, String courseId) {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		ArrayList<String>[] returnValue = new ArrayList[3];
		returnValue[0] = (ArrayList)corsor.get(ADMIN);
		returnValue[1] = (ArrayList)corsor.get(MOD);
		returnValue[2] = (ArrayList)corsor.get(USERS);
		return returnValue;
	}
	
	/**
	 * NOTE: This is meant for internal use do not make this method public
	 *
	 * Returns a list of Id for the default group for an assignment.
	 *
	 * The Ids are ordered as so: AdminGroup, ModGroup, UserGroup
	 */
	static String[] mongoGetDefaultGroupId(DB dbs, String courseId) {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
		DBObject corsor = myDbRef.fetch();
		String[] returnValue = new String[3];
		returnValue[0] = corsor.get(ADMIN_GROUP_ID).toString();
		returnValue[1] = corsor.get(MOD_GROUP_ID).toString();
		returnValue[2] = corsor.get(USER_GROUP_ID).toString();
		return returnValue;
	}
}
