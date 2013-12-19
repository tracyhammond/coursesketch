package database.institution;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlGroup;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.user.GroupManager;

public final class Institution {
	private static Institution instance;
	private DB db;

	private Institution(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
			db = mongoClient.getDB(DATABASE);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Institution() {
		//this("goldberglinux.tamu.edu");
		this("localhost");
	}

	private static Institution getInstance() {
		if(instance==null)
			instance = new Institution();
		return instance;
	}

	/**
	 * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
	 * @param testOnly
	 */
	public Institution(boolean testOnly) {
		try {
			MongoClient mongoClient = new MongoClient("localhost");
			db = mongoClient.getDB("test");
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	/**
	 * Returns a list of courses given a list of Ids for the courses
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlCourse> mongoGetCourses(List<String> courseIds,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlCourse> allCourses = new ArrayList<SrlCourse>();
		for(String courseId : courseIds) {
			try {
				allCourses.add(CourseManager.mongoGetCourse(getInstance().db, courseId, userId,currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			}
		}
		return allCourses;
	}

	/**
	 * Returns a list of problems given a list of Ids for the course problems.
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlProblem> mongoGetCourseProblem(List<String> problemID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlProblem> allCourses = new ArrayList<SrlProblem>();
		for(int index = 0; index < problemID.size(); index ++) {
			try {
				allCourses.add(CourseProblemManager.mongoGetCourseProblem(getInstance().db, problemID.get(index), userId, currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			} catch (AuthenticationException e) {
				if (e.getType() != AuthenticationException.INVALID_DATE) {
					throw e;
				}
			}
		}
		return allCourses;
	}

	/**
	 * Returns a list of problems given a list of Ids for the course problems.
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlAssignment> mongoGetAssignment(List<String> assignementID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
		for (int assignments = assignementID.size() - 1; assignments >= 0; assignments--) {
			try {
				allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().db, assignementID.get(assignments),
						userId, currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			} catch (AuthenticationException e) {
				if (e.getType() != AuthenticationException.INVALID_DATE) {
					throw e;
				}
			}
		}
		return allAssignments;
	}

	public static ArrayList<SrlBankProblem> mongoGetProblem(List<String> problemID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlBankProblem> allProblems = new ArrayList<SrlBankProblem>();
		for (int problem = problemID.size() - 1; problem >= 0; problem--) {
			allProblems.add(BankProblemManager.mongoGetBankProblem(getInstance().db, problemID.get(problem), userId));
		}
		return allProblems;
	}

	public static ArrayList<SrlCourse> getAllPublicCourses() {
		return CourseManager.mongoGetAllPublicCourses(getInstance().db);
	}

	/**
	 * Inserts a {@link SrlCourse} into the the database.
	 *
	 * Upon insertion 3 steps happen:
	 * <ol>
	 * <li> a default usergroup is created for this course for users, mods, and admins</li>
	 * <li> the course is created in the course collection</li>
	 * <li> the course contains a reference to the Id of the userGroup and has the groups in its access permission list </li>
	 * </ol>
	 * @param userId The credentials used to authenticate the insertion
	 * @param assignment The object being inserted 
	 * @return The Id of the object that was inserted
	 */
	public static String mongoInsertCourse(String userId, SrlCourse course) {
		
		// Creates the default permissions for the courses.
		SrlPermission permission = null;
		if (course.hasAccessPermission()) {
			permission = course.getAccessPermission();
		}

		SrlGroup.Builder courseGroup = SrlGroup.newBuilder();
		courseGroup.addAdmin(userId);
		courseGroup.setGroupName(course.getName() + "_User");
		courseGroup.clearUserId();
		if (permission != null && permission.getUserPermissionCount() > 0) {
			courseGroup.addAllUserId(permission.getUserPermissionList());
		}
		String userGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

		courseGroup.setGroupName(course.getName() + "_Mod");
		courseGroup.clearUserId();
		if (permission != null && permission.getModeratorPermissionCount() > 0) {
			courseGroup.addAllUserId(permission.getModeratorPermissionList());
		}
		String modGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

		courseGroup.setGroupName(course.getName() + "_Admin");
		courseGroup.clearUserId();
		if (permission != null && permission.getAdminPermissionCount() > 0) {
			courseGroup.addAllUserId(permission.getAdminPermissionList());
		}
		courseGroup.addUserId(userId); // an admin will always exist
		String adminGroupId = GroupManager.mongoInsertGroup(getInstance().db, courseGroup.buildPartial());

		// overwrites the existing permissions with the new user specific course permission
		SrlCourse.Builder builder = SrlCourse.newBuilder(course);
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		permissions.addAdminPermission(GROUP_PREFIX + adminGroupId);
		permissions.addModeratorPermission(GROUP_PREFIX + modGroupId);
		permissions.addUserPermission(GROUP_PREFIX + userGroupId);
		builder.setAccessPermission(permissions.build());
		String resultId = CourseManager.mongoInsertCourse(getInstance().db, builder.buildPartial());

		// links the course to the group!
		CourseManager.mongoInsertDefaultGroupId(getInstance().db, resultId, userGroupId, modGroupId, adminGroupId);
		return resultId;
	}

	/**
	 * Inserts the assignment into the the database.
	 *
	 * Upon insertion 3 steps happen:
	 * <ol>
	 * <li> the assignment is created in an assignment collection</li>
	 * <li> the course assignment list now contains the assignment Id </li>
	 * <li> the assignment has the same default permissions as the parent course </li>
	 * </ol>
	 * @param userId The credentials used to authenticate the insertion
	 * @param assignment The object being inserted 
	 * @return The Id of the object that was inserted
	 */
	public static String mongoInsertAssignment(String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException {
		String resultId = AssignmentManager.mongoInsertAssignment(getInstance().db, userId, assignment);

		ArrayList<String>[] ids = CourseManager.mongoGetDefaultGroupId(getInstance().db, assignment.getCourseId());
		AssignmentManager.mongoInsertDefaultGroupId(getInstance().db, resultId, ids);

		return resultId;
	}

	/**
	 * Inserts the assignment into the the database.
	 *
	 * Upon insertion 3 steps happen:
	 * <ol>
	 * <li> the assignment is created in a problem collection</li>
	 * <li> the assignment problems list now contains the problem Id </li>
	 * <li> the problem has the same default permissions as the parent assignment </li>
	 * </ol>
	 * @param userId The credentials used to authenticate the insertion
	 * @param problem The object being inserted 
	 * @return The Id of the object that was inserted
	 */
	public static String mongoInsertCourseProblem(String userId, SrlProblem problem) throws AuthenticationException, DatabaseAccessException {
		String resultId = CourseProblemManager.mongoInsertCourseProblem(getInstance().db, userId, problem);

		ArrayList<String>[] ids = AssignmentManager.mongoGetDefaultGroupId(getInstance().db, problem.getAssignmentId());
		CourseProblemManager.mongoInsertDefaultGroupId(getInstance().db, resultId, ids);
		return resultId;
	}
}
