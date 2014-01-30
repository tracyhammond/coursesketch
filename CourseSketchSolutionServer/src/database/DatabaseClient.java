package database;

import static database.StringConstants.*;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;

import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
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
		this("goldberglinux.tamu.edu");
		//this("localhost");
	}

	public static DatabaseClient getInstance() {
		if(instance==null)
			instance = new DatabaseClient();
		return instance;
	}


	/**
	 * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
	 * @param testOnly
	 */
	public DatabaseClient(boolean testOnly) {
		try {
			MongoClient mongoClient = new MongoClient("localhost");
			if (testOnly) {
				db = mongoClient.getDB("test");
			} else {
				db = mongoClient.getDB("submissions");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	
	public static final String saveSolution(SrlSolution solution) {
		DBCollection new_user = getInstance().db.getCollection(SOLUTION_COLLECTION);
		BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
		.append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
		//.append(ADMIN, solution.getAccessPermissions().getAdminPermissionList())
		//.append(MOD, solution.getAccessPermissions().getModeratorPermissionList())
		//.append(USERS, solution.getAccessPermissions().getUserPermissionList())
		.append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray())
		.append(SUBMISSION_TIME, solution.getSubmission().getSubmissionTime());
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static final String saveExperiment(SrlExperiment experiment) {
		System.out.println("saving the experiment!");
		DBCollection new_user = getInstance().db.getCollection(EXPERIMENT_COLLECTION);
		BasicDBObject query = new BasicDBObject(COURSE_ID, experiment.getCourseId())
		.append(ASSIGNMENT_ID, experiment.getAssignmentId())
		.append(COURSE_PROBLEM_ID, experiment.getProblemId())
		.append(USER_ID, experiment.getUserId())
		//.append(ADMIN, experiment.getAccessPermissions().getAdminPermissionList())
		//.append(MOD, experiment.getAccessPermissions().getModeratorPermissionList())
		//.append(USERS, experiment.getAccessPermissions().getUserPermissionList())
		.append(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray())
		.append(SUBMISSION_TIME, experiment.getSubmission().getSubmissionTime());
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static void updateSubmission(String resultantId, ByteString updateList) throws Exception {
		throw new Exception("Not supported yet!");
	}

	public static SrlExperiment getExperiment(String itemId) {
		DBRef myDbRef = new DBRef(getInstance().db, EXPERIMENT_COLLECTION, new ObjectId(itemId));
		DBObject corsor = myDbRef.fetch();
		SrlExperiment.Builder build = SrlExperiment.newBuilder();
		build.setAssignmentId(corsor.get(ASSIGNMENT_ID).toString());
		build.setUserId(corsor.get(USER_ID).toString());
		build.setProblemId(corsor.get(COURSE_PROBLEM_ID).toString());
		build.setCourseId(corsor.get(COURSE_ID).toString());
		SrlSubmission.Builder sub = SrlSubmission.newBuilder();
		sub.setUpdateList(ByteString.copyFrom((byte[])corsor.get(UPDATELIST)));
		build.setSubmission(sub.build());
		return build.build();
	}
}