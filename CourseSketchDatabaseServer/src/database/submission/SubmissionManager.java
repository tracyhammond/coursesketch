package database.submission;

import static util.StringConstants.*;

import java.util.ArrayList;

import multiConnection.MultiConnectionManager;

import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ExperimentReview;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

import connection.SubmissionConnection;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.institution.Institution;


public class SubmissionManager 
{
	private static SubmissionManager instance;
	private SubmissionConnection solutionConnection;

	public SubmissionManager(SubmissionConnection connection) {
		if (instance == null) {
			this.solutionConnection = connection;
		}
	}

	private static SubmissionManager getInstance() {
		return instance;
	}

	/**
	 * Inserts a submission into the database.
	 *
	 * if {@code experiment} is true then {@code uniqueId} is a userId otherwise it is the bankProblem
	 * if {@code experiment} is true then {@code problem} is a courseProblem otherwise it is the bankProblem
	 * @param dbs
	 * @param uniqueId
	 * @param problemId
	 * @param submissionId
	 * @param experiment
	 */
	public static void mongoInsertSubmission(DB dbs, String problemId, String uniqueId, String submissionId, boolean experiment) {
		System.out.println("Inserting an experiment " + experiment);
		System.out.println("database is " + dbs);
		DBRef myDbRef = new DBRef(dbs, experiment?EXPERIMENT_COLLECTION:SOLUTION_COLLECTION, new ObjectId(problemId));
		DBCollection collection = dbs.getCollection(experiment?EXPERIMENT_COLLECTION:SOLUTION_COLLECTION);
		DBObject corsor = myDbRef.fetch();
		System.out.println(corsor);
		System.out.println("uniuq id " + uniqueId);
		BasicDBObject queryObj = new BasicDBObject( experiment ? uniqueId : SOLUTION_ID, submissionId);
		if (corsor == null) {
			System.out.println("creating a new instance for this problemId");
			queryObj.append(SELF_ID, new ObjectId(problemId));
			collection.insert(queryObj);
			// we need to create a new corsor
		} else {
			System.out.println("adding a new submission to this old itemid");
			// insert the submissionId, if it is an experiment then we need to use the uniqueId to make it work.
			collection.update(corsor, new BasicDBObject("$set", queryObj));
		}
	}

	/**
	 * @param submissionId
	 * @param userId
	 * @param problemId
	 * @return the submission id
	 */
	public static void mongoGetExperiment(DB dbs, String userId, String problemId, String sessionInfo, MultiConnectionManager internalConnections) {
		Request.Builder r = Request.newBuilder();
		r.setSessionInfo(sessionInfo);
		r.setRequestType(MessageType.DATA_REQUEST);
		ItemRequest.Builder build = ItemRequest.newBuilder();
		build.setQuery(ItemQuery.EXPERIMENT);
		DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
		DBObject corsor = myDbRef.fetch();
		String sketchId = "" + corsor.get(userId);
		System.out.println("SketchId " + sketchId);
		build.addItemId(sketchId);
		DataRequest.Builder data = DataRequest.newBuilder();
		data.addItems(build);
		r.setOtherData(data.build().toByteString());
		System.out.println("Sending command " + r.build());
		internalConnections.send(r.build(), null, SubmissionConnection.class);
	}

	/**
	 * Builds a request to the server for every single sketch in a single problem.
	 * @param submissionId
	 * @param userId
	 * @param problemId
	 * @return the submission id
	 * @throws DatabaseAccessException
	 * @throws AuthenticationException
	 */
	public static void mongoGetAllExperimentsAsInstructor(DB dbs, String userId, String problemId, String sessionInfo,
			MultiConnectionManager internalConnections, ByteString review) throws DatabaseAccessException, AuthenticationException {
		DBObject problem = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId)).fetch();
		if (problem == null) {
			throw new DatabaseAccessException("Problem was not found with the following ID " + problemId);
		}
		ArrayList adminList = (ArrayList<Object>) problem.get(ADMIN); // convert to ArrayList<String>
		ArrayList modList = (ArrayList<Object>) problem.get(MOD); // convert to ArrayList<String>
		boolean isAdmin = false, isMod = false;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isMod = Authenticator.checkAuthentication(dbs, userId, modList);
		if (!isAdmin && !isMod) {
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		Request.Builder r = Request.newBuilder();
		r.setSessionInfo(sessionInfo);
		r.setRequestType(MessageType.DATA_REQUEST);
		ItemRequest.Builder build = ItemRequest.newBuilder();
		build.setQuery(ItemQuery.EXPERIMENT);
		DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
		DBObject corsor = myDbRef.fetch();
		for (String key: corsor.keySet()) {
			String sketchId = "" + corsor.get(key);
			System.out.println("SketchId " + sketchId);
			build.addItemId(sketchId);
		}
		build.setAdvanceQuery(review);
		DataRequest.Builder data = DataRequest.newBuilder();
		data.addItems(build);
		r.setOtherData(data.build().toByteString());
		System.out.println("Sending command " + r.build());
		internalConnections.send(r.build(), null, SubmissionConnection.class);
	}

	//need to be able to get a single submission
	// be able to get all of the submissions
	// if you are trying to get your submission you just need your userId
	// if you are trying to get all submissions you need to authenticate with the specific course problem.
}
