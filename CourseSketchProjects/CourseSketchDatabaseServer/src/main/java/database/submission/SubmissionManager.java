package database.submission;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import connection.SubmissionClientWebSocket;
import coursesketch.server.interfaces.MultiConnectionManager;
import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import utilities.ConnectionException;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;

import static database.DatabaseStringConstants.COURSE_PROBLEM_COLLECTION;
import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SOLUTION_COLLECTION;
import static database.DatabaseStringConstants.SOLUTION_ID;

/**
 * Manages data that has to deal with submissions in the database server.
 *
 * This specifically is a link that links all of the institution data back to the submission data.
 * This does not actually store the submissions themselves.
 * @author gigemjt
 *
 */
public final class SubmissionManager {

    /**
     * Declaration and Definition.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionManager.class);

    /**
     * Private constructor.
     *
     */
    private SubmissionManager() {
    }

    /**
     * Inserts a submission into the database.
     *
     * if {@code experiment} is true then {@code uniqueId} is a userId otherwise
     * it is the bankProblem if {@code experiment} is true then {@code problem}
     * is a courseProblem otherwise it is the bankProblem
     *  @param dbs The database that contains the information about the submission.
     * @param uniqueId Generally the userId.
     * @param problemId The problem id.
     * @param submissionId The id associated with the submission on the submission server.
     * @param experiment True if the object being submitted is an experiment
     */
    @SuppressWarnings({ "PMD.NPathComplexity" })
    public static void mongoInsertSubmission(final DB dbs, final String uniqueId, final String problemId,
            final String submissionId,
            final boolean experiment) {
        LOG.info("Inserting an experiment {}", experiment);
        LOG.info("Database is {}", dbs);
        LOG.info("Problem id: {}", problemId);
        final DBRef myDbRef = new DBRef(dbs, experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION, new ObjectId(problemId));
        final DBCollection collection = dbs.getCollection(experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION);
        final DBObject corsor = myDbRef.fetch();

        LOG.info("corsor: {}", corsor);
        LOG.info("uniuq id: {}", uniqueId);

        final BasicDBObject queryObj = new BasicDBObject(experiment ? uniqueId : SOLUTION_ID, submissionId);
        if (corsor == null) {
            LOG.info("Creating a new instance to this old itemid");
            queryObj.append(SELF_ID, new ObjectId(problemId));
            collection.insert(queryObj);
            // we need to create a new corsor
        } else {
            LOG.info("adding a new submission to this old itemid");
            // insert the submissionId, if it is an experiment then we need to
            // use the uniqueId to make it work.
            collection.update(corsor, new BasicDBObject("$set", queryObj));
        }
    }

    /**
     * Sends a request to the submission server to request an experiment as a user.
     * @param dbs The database that contains data about the experiment.
     * @param userId The user who has access to the experiment.
     * @param problemId The id of the problem associated with the sketch.
     * @param sessionInfo The session information that is sent to the submission server.
     * @param internalConnections A manager of connections to another database.
     * @throws DatabaseAccessException Thrown is there is data missing in the database.
     */
    public static void mongoGetExperiment(final DB dbs, final String userId, final String problemId, final Request sessionInfo,
            final MultiConnectionManager internalConnections) throws DatabaseAccessException {

        final ItemRequest.Builder build = ItemRequest.newBuilder();
        build.setQuery(ItemQuery.EXPERIMENT);
        final DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("The student has not submitted anything for this problem");
        }
        final String sketchId = "" + corsor.get(userId);
        LOG.info("SketchId: ", sketchId);
        if ("null".equals(sketchId)) {
            throw new DatabaseAccessException("The student has not submitted anything for this problem");
        }
        build.addItemId(sketchId);
        final DataRequest.Builder data = DataRequest.newBuilder();
        data.addItems(build);

        final Request.Builder requestBuilder = ProtobufUtilities.createBaseResponse(sessionInfo);
        requestBuilder.setOtherData(data.build().toByteString());
        try {
            internalConnections.send(requestBuilder.build(), null, SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            throw new DatabaseAccessException("Failed to send request to submission server for experiment", e);
        }
    }

    /**
     * Builds a request to the server for all of the sketches in a single
     * problem.
     * @param authenticator The object being used to authenticate the server.
     * @param dbs The database where the data is stored.
     * @param userId The user that was requesting this information.
     * @param problemId The problem for which the sketch data is being requested.
     * @param sessionInfo The session information of the current server.
     * @param internalConnections The connections of other servers.
     * @param review A list of data about reviewing the sketches.
     * @throws DatabaseAccessException Thrown if there are no problems data that exist.
     * @throws AuthenticationException Thrown if the user does not have the authentication
     */
    public static void mongoGetAllExperimentsAsInstructor(final Authenticator authenticator, final DB dbs, final String userId,
            final String problemId, final Request sessionInfo, final MultiConnectionManager internalConnections, final ByteString review)
            throws DatabaseAccessException, AuthenticationException {
        final DBObject problem = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId)).fetch();
        if (problem == null) {
            throw new DatabaseAccessException("Problem was not found with the following ID " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
        final DBObject dbObject = myDbRef.fetch();

        if (dbObject == null) {
            throw new DatabaseAccessException("Students have not submitted any data for this problem: " + problemId);
        }

        final ItemRequest itemRequest = createSubmissionRequest(dbObject, review);
        final DataRequest.Builder data = DataRequest.newBuilder();
        data.addItems(itemRequest);
        final Request.Builder requestBuilder = ProtobufUtilities.createBaseResponse(sessionInfo);
        requestBuilder.setOtherData(data.build().toByteString());
        try {
            internalConnections.send(requestBuilder.build(), null, SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Creates a submission request for the submission server.
     * @param experiments A {@link DBObject} that represents the experiments in the database.
     * @param review An advance query used for reviewing students submissions.
     * @return {@link ItemRequest} That is used to query the submission server.
     */
    private static ItemRequest createSubmissionRequest(final DBObject experiments, final ByteString review) {
        final ItemRequest.Builder itemRequest = ItemRequest.newBuilder();
        itemRequest.setQuery(ItemQuery.EXPERIMENT);
        for (String key : experiments.keySet()) {
            if (SELF_ID.equals(key)) {
                continue;
            }
            final Object experimentId = experiments.get(key);
            if (experimentId == null || experimentId instanceof ObjectId) {
                continue;
            }
            final String sketchId = experiments.get(key).toString();
            LOG.info("SketchId: {}", sketchId);
            itemRequest.addItemId(sketchId);
        }
        itemRequest.setAdvanceQuery(review);
        return itemRequest.build();
    }

    // need to be able to get a single submission
    // be able to get all of the submissions
    // if you are trying to get your submission you just need your userId
    // if you are trying to get all submissions you need to authenticate with
    // the specific course problem.
}
