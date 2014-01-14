package database.institution;

import static database.StringConstants.DATABASE;
import static database.StringConstants.GROUP_PREFIX;
import static database.StringConstants.USER_GROUP_COLLECTION;
import static database.StringConstants.USER_LIST;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlGroup;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.submission.SubmissionManager;
import database.user.GroupManager;
import database.user.UserClient;

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
		this("goldberglinux.tamu.edu");
		//this("localhost");
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
	 * @throws DatabaseAccessException 
	 */
	public static ArrayList<SrlAssignment> mongoGetAssignment(List<String> assignementID,String userId) throws AuthenticationException, DatabaseAccessException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
		for (int assignments = assignementID.size() - 1; assignments >= 0; assignments--) {
			try {
				allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().db, assignementID.get(assignments),
						userId, currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
				throw e;
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

		ArrayList<String>[] ids = CourseManager.mongoGetDefaultGroupList(getInstance().db, assignment.getCourseId());
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

	/**
	 * Inserts the {@link SrlBankProblem} into the the database.
	 *
	 * Upon insertion a bank problem is created within the problem bank.
	 * @param userId The credentials used to authenticate the insertion
	 * @param problem The object being inserted 
	 * @return The Id of the object that was inserted
	 */
	public static String mongoInsertBankProblem(String userId, SrlBankProblem problem) throws AuthenticationException {
		return BankProblemManager.mongoInsertBankProblem(getInstance().db, problem);
	}

	/**
	 * Registers a user for a course
	 *
	 * Upon registration 3 steps happen:
	 * <ol>
	 * <li> The user is checked to make sure that they already are not enrolled in the course.
	 * <li> The user is added to the user permission list. </li>
	 * <li> The user now has the course in its list of courses.</li>
	 * </ol>
	 * @param userId The credentials used to authenticate the insertion
	 * @param problem The object being inserted 
	 * @return The Id of the object that was inserted
	 * @throws DatabaseAccessException only thrown if the user is already registered for the course
	 */
	public static final boolean putUserInCourse(String courseId, String userId) throws DatabaseAccessException {
		// this actually requires getting the data from the course itself
		String userGroupId = CourseManager.mongoGetDefaultGroupId(getInstance().db, courseId)[2]; // user group!
		
		// FIXME: when mongo version 2.5.5 java client comes out please change this!
		ArrayList<String> hack = new ArrayList<String>();
		hack.add(GROUP_PREFIX+userGroupId);
		if (Authenticator.checkAuthentication(getInstance().db, userId, hack)) {
			return false;
		}
		// DO NOT USE THIS CODE ANY WHERE ESLE
		DBRef myDbRef = new DBRef(getInstance().db, USER_GROUP_COLLECTION, new ObjectId(userGroupId));
		DBObject corsor = myDbRef.fetch();
		DBCollection courses = getInstance().db.getCollection(USER_GROUP_COLLECTION);
		BasicDBObject object = new BasicDBObject("$addToSet", new BasicDBObject(USER_LIST, userId));
		courses.update(corsor, object);

		UserClient.addCourseToUser(userId, courseId);
		return true;
	}

	public static final ArrayList<SrlCourse> getUserCourses(String userId) throws AuthenticationException {
		return Institution.mongoGetCourses(UserClient.getUserCourses(userId), userId);
	}

	/**
	 * A message sent from the submission server that allows the insertion of the message
	 * @param req
	 */
	public static void mongoInsertSubmission(Request req) {
		try {
			SrlExperiment exp = SrlExperiment.parseFrom(req.getOtherData());
			SubmissionManager.mongoInsertSubmission(getInstance().db, exp.getProblemId(), exp.getUserId(), exp.getSubmission().getId(), true);
			return;
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			// TODO: change how this process works for instructors!
			SrlSolution exp = SrlSolution.parseFrom(req.getOtherData());
			throw new Exception("Instructors need to be authenticated first!");
			//SubmissionManager.mongoInsertSubmission(getInstance().db, exp.getProblemBankId(), exp.getProblemBankId(), exp.getSubmission().getId(), false);
			//return;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
