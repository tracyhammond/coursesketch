package database.submission;

import static database.StringConstants.*;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;


public class SubmissionManager 
{
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
		DBRef myDbRef = new DBRef(dbs, experiment?EXPERIMENT_COLLECTION:SOLUTION_COLLECTION, new ObjectId(problemId));
		DBCollection collection = dbs.getCollection(experiment?EXPERIMENT_COLLECTION:SOLUTION_COLLECTION);
		DBObject corsor = myDbRef.fetch();
		BasicDBObject queryObj = new BasicDBObject( experiment ? uniqueId : SOLUTION_ID, submissionId);
		if (corsor == null) {
			queryObj.append(SELF_ID, new ObjectId(problemId));
			collection.insert(queryObj);
			// we need to create a new corsor
		} else {
			// insert the submissionId, if it is an experiment then we need to use the uniqueId to make it work.
			collection.update(corsor, new BasicDBObject("$set", queryObj));
		}
	}

	/**
	 * 
	 * @param submissionId
	 * @param userId
	 * @param problemId
	 */
	public static void mongoGetSubmission(DB dbs, String userId, String problemId, String sessionId) {

	}
	
	//need to be able to get a single submission
	// be able to get all of the submissions
	// if you are trying to get your submission you just need your userId
	// if you are trying to get all submissions you need to authenticate with the specific course problem.
}
