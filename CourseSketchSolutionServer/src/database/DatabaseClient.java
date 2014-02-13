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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

public class DatabaseClient {
	private static DatabaseClient instance;
	private DB db;

	private DatabaseClient(String url) throws Exception {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(url);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (mongoClient == null) {
			throw new Exception("An error occured while making mongoClient");
		}
		db = mongoClient.getDB("submissions");
		if (db == null) {
			System.out.println("Db is null!");
		} else {
			setUpIndexes();
		}
	}

	private DatabaseClient() throws Exception{
		this("goldberglinux.tamu.edu");
		//this("localhost");
	}

	public static DatabaseClient getInstance() {
		if (instance==null) {
			try {
				instance = new DatabaseClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public void setUpIndexes() {
		System.out.println("Setting up an index");
		System.out.println("Experiment Index command: " + new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
		db.getCollection(EXPERIMENT_COLLECTION).ensureIndex(new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
		db.getCollection(SOLUTION_COLLECTION).ensureIndex(new BasicDBObject(PROBLEM_BANK_ID, 1).append("unique", true));
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

	/**
	 * Saves the experiment trying to make sure that there are no duplicates.
	 *
	 * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
	 * @param experiment
	 * @return
	 */
	public static final String saveSolution(SrlSolution solution) {
		System.out.println("saving the experiment!");
		DBCollection solutions = getInstance().db.getCollection(SOLUTION_COLLECTION);

		BasicDBObject findQuery = new BasicDBObject(PROBLEM_BANK_ID, solution.getProblemBankId());
		DBCursor c = solutions.find(findQuery);
		DBObject corsor = null;
		if (c.count() > 0) {
			corsor = c.next();
			c.close();
			DBCollection courses = getInstance().db.getCollection(SOLUTION_COLLECTION);
			DBObject updateObj = new BasicDBObject(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray());
			courses.update(corsor, new BasicDBObject ("$set", updateObj));
		} else {
			System.out.println("No existing submissions found");
			DBCollection new_user = getInstance().db.getCollection(SOLUTION_COLLECTION);

			BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
			.append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
			.append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray())
			.append(PROBLEM_BANK_ID, solution.getProblemBankId());

			new_user.insert(query);
			corsor = new_user.findOne(query);
			c.close();
		}
		return corsor.get(SELF_ID).toString();
	}

	/**
	 * Saves the experiment trying to make sure that there are no duplicates.
	 * 
	 * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
	 * @param experiment
	 * @return
	 */
	public static final String saveExperiment(SrlExperiment experiment) {
		System.out.println("saving the experiment!");
		DBCollection experiments = getInstance().db.getCollection(EXPERIMENT_COLLECTION);

		BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, experiment.getProblemId())
		.append(USER_ID, experiment.getUserId());
		System.out.println("Searching for existing solutions " + findQuery);
		DBCursor c = experiments.find(findQuery).sort(new BasicDBObject(SUBMISSION_TIME, -1));
		System.out.println("Do we have the next cursos " + c.hasNext());
		System.out.println("Number of solutions found" + c.count());
		DBObject corsor = null;
		if (c.count() > 0) {
			corsor = c.next();
			c.close();
			DBCollection courses = getInstance().db.getCollection(EXPERIMENT_COLLECTION);
			DBObject updateObj = new BasicDBObject(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray());
			DBObject updateObj2 = new BasicDBObject(SUBMISSION_TIME, experiment.getSubmission().getSubmissionTime());
			courses.update(corsor, new BasicDBObject ("$set", updateObj).append("$set", updateObj2));
		} else {
			c.close();
			BasicDBObject query = new BasicDBObject(COURSE_ID, experiment.getCourseId())
			.append(ASSIGNMENT_ID, experiment.getAssignmentId())
			.append(COURSE_PROBLEM_ID, experiment.getProblemId())
			.append(USER_ID, experiment.getUserId())
			//.append(ADMIN, experiment.getAccessPermissions().getAdminPermissionList())
			//.append(MOD, experiment.getAccessPermissions().getModeratorPermissionList())
			//.append(USERS, experiment.getAccessPermissions().getUserPermissionList())
			.append(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray())
			.append(SUBMISSION_TIME, experiment.getSubmission().getSubmissionTime());
			experiments.insert(query);
			corsor = experiments.findOne(query);
		}
		return corsor.get(SELF_ID).toString();
	}

	public static SrlExperiment getExperiment(String itemId) {
		System.out.println("Fetching experiment");
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
		System.out.println("RESULT: " + build.build());
		return build.build();
	}

}