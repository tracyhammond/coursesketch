package database.submission;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;

import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;

import org.bson.types.ObjectId;

import protobuf.srl.query.Data.DataRequest;
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

import connection.ConnectionException;
import connection.SubmissionConnection;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class SubmissionManager {
    private static SubmissionManager instance;
    private SubmissionConnection solutionConnection;

    public SubmissionManager(final SubmissionConnection connection) {
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
     * if {@code experiment} is true then {@code uniqueId} is a userId otherwise
     * it is the bankProblem if {@code experiment} is true then {@code problem}
     * is a courseProblem otherwise it is the bankProblem
     *
     * @param dbs
     * @param uniqueId
     * @param problemId
     * @param submissionId
     * @param experiment
     */
    public static void mongoInsertSubmission(final DB dbs, final String problemId, final String uniqueId, final String submissionId, final boolean experiment) {
        System.out.println("Inserting an experiment " + experiment);
        System.out.println("database is " + dbs);
        final DBRef myDbRef = new DBRef(dbs, experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION, new ObjectId(problemId));
        final DBCollection collection = dbs.getCollection(experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION);
        final DBObject corsor = myDbRef.fetch();
        System.out.println(corsor);
        System.out.println("uniuq id " + uniqueId);
        final BasicDBObject queryObj = new BasicDBObject(experiment ? uniqueId : SOLUTION_ID, submissionId);
        if (corsor == null) {
            System.out.println("creating a new instance for this problemId");
            queryObj.append(SELF_ID, new ObjectId(problemId));
            collection.insert(queryObj);
            // we need to create a new corsor
        } else {
            System.out.println("adding a new submission to this old itemid");
            // insert the submissionId, if it is an experiment then we need to
            // use the uniqueId to make it work.
            collection.update(corsor, new BasicDBObject("$set", queryObj));
        }
    }

    /**
     * @param submissionId
     * @param userId
     * @param problemId
     * @return the submission id
     * @throws DatabaseAccessException
     */
    public static void mongoGetExperiment(final DB dbs, final String userId, final String problemId, final String sessionInfo, final MultiConnectionManager internalConnections)
            throws DatabaseAccessException {
        final Request.Builder r = Request.newBuilder();
        r.setSessionInfo(sessionInfo);
        r.setRequestType(MessageType.DATA_REQUEST);
        final ItemRequest.Builder build = ItemRequest.newBuilder();
        build.setQuery(ItemQuery.EXPERIMENT);
        final DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        final String sketchId = "" + corsor.get(userId);
        System.out.println("SketchId " + sketchId);
        if ("null".equals(sketchId)) {
            throw new DatabaseAccessException("The student has not submitted anything for this problem");
        }
        build.addItemId(sketchId);
        final DataRequest.Builder data = DataRequest.newBuilder();
        data.addItems(build);
        r.setOtherData(data.build().toByteString());
        System.out.println("Sending command " + r.build());
        try {
            internalConnections.send(r.build(), null, SubmissionConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a request to the server for every single sketch in a single
     * problem.
     *
     * @param submissionId
     * @param userId
     * @param problemId
     * @return the submission id
     * @throws DatabaseAccessException
     * @throws AuthenticationException
     */
    public static void mongoGetAllExperimentsAsInstructor(final DB dbs, final String userId, final String problemId, final String sessionInfo,
            final MultiConnectionManager internalConnections, final ByteString review) throws DatabaseAccessException, AuthenticationException {
        final DBObject problem = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId)).fetch();
        if (problem == null) {
            throw new DatabaseAccessException("Problem was not found with the following ID " + problemId);
        }
        final ArrayList adminList = (ArrayList<Object>) problem.get(ADMIN); // convert
                                                                      // to
                                                                      // ArrayList<String>
        final ArrayList modList = (ArrayList<Object>) problem.get(MOD); // convert to
                                                                  // ArrayList<String>
        boolean isAdmin = false, isMod = false;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);
        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final Request.Builder r = Request.newBuilder();
        r.setSessionInfo(sessionInfo);
        r.setRequestType(MessageType.DATA_REQUEST);
        final ItemRequest.Builder build = ItemRequest.newBuilder();
        build.setQuery(ItemQuery.EXPERIMENT);
        final DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        for (String key : corsor.keySet()) {
            final String sketchId = "" + corsor.get(key);
            System.out.println("SketchId " + sketchId);
            build.addItemId(sketchId);
        }
        build.setAdvanceQuery(review);
        final DataRequest.Builder data = DataRequest.newBuilder();
        data.addItems(build);
        r.setOtherData(data.build().toByteString());
        System.out.println("Sending command " + r.build());
        try {
            internalConnections.send(r.build(), null, SubmissionConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    // need to be able to get a single submission
    // be able to get all of the submissions
    // if you are trying to get your submission you just need your userId
    // if you are trying to get all submissions you need to authenticate with
    // the specific course problem.
}
