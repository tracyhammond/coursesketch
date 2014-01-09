package database;

import static database.StringConstants.ADMIN;
import static database.StringConstants.ALLOWED_IN_PROBLEMBANK;
import static database.StringConstants.ASSIGNMENT_ID;
import static database.StringConstants.COURSE_ID;
import static database.StringConstants.COURSE_PROBLEM_ID;
import static database.StringConstants.IS_PRACTICE_PROBLEM;
import static database.StringConstants.MOD;
import static database.StringConstants.SELF_ID;
import static database.StringConstants.SOLUTION_COLLECTION;
import static database.StringConstants.UPDATELIST;
import static database.StringConstants.USERS;
import static database.StringConstants.USER_ID;

import java.net.UnknownHostException;

import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DatabaseClient {
	private static DatabaseClient instance;
	private DB db;

	private DatabaseClient(String url) {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(url);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		db = mongoClient.getDB("submissions");
		if (db == null) {
			System.out.println("Db is null!");
		}
	}

	private DatabaseClient(){
		//this("goldberglinux.tamu.edu");
		this("localhost");
	}

	public static void main(String[] args) throws Exception {

	}

	public static DatabaseClient getInstance() {
		if(instance==null)
			instance = new DatabaseClient();
		return instance;
	}

	public static final String saveSolution(SrlSolution solution) {
		DBCollection new_user = getInstance().db.getCollection(SOLUTION_COLLECTION);
		BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
		.append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
		.append(ADMIN, solution.getAccessPermissions().getAdminPermissionList())
		.append(MOD, solution.getAccessPermissions().getModeratorPermissionList())
		.append(USERS, solution.getAccessPermissions().getUserPermissionList())
		.append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray());
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static final String saveExperiment(SrlExperiment experiment) {
		DBCollection new_user = getInstance().db.getCollection("Solutions");
		BasicDBObject query = new BasicDBObject(COURSE_ID, experiment.getCourseId())
		.append(ASSIGNMENT_ID, experiment.getAssignmentId())
		.append(COURSE_PROBLEM_ID, experiment.getProblemId())
		.append(USER_ID, experiment.getUserId())
		.append(ADMIN, experiment.getAccessPermissions().getAdminPermissionList())
		.append(MOD, experiment.getAccessPermissions().getModeratorPermissionList())
		.append(USERS, experiment.getAccessPermissions().getUserPermissionList())
		.append(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray());
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}
}