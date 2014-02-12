package database.submission;

import static database.StringConstants.*;

import org.bson.types.ObjectId;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

import connection.SolutionConnection;
import database.institution.Institution;


public class SubmissionManager 
{
	private static SubmissionManager instance;
	private SolutionConnection solutionConnection;

	public SubmissionManager(SolutionConnection connection) {
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
	public static void mongoGetExperiment(DB dbs, String userId, String problemId, String sessionInfo) {
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
		
	}

	//need to be able to get a single submission
	// be able to get all of the submissions
	// if you are trying to get your submission you just need your userId
	// if you are trying to get all submissions you need to authenticate with the specific course problem.
}
