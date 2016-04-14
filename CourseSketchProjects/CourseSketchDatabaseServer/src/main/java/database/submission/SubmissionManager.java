package database.submission;

import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;

import java.util.ArrayList;
import java.util.List;

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
     * @param uniqueId Generally the userId.  But it is used to uniquely identify each submission.
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
        final BasicDBObject myDbRef = new BasicDBObject(SELF_ID, new ObjectId(problemId));
        final DBCollection collection = dbs.getCollection(experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION);
        final DBObject cursor = collection.findOne(myDbRef);

        LOG.info("cursor: {}", cursor);
        LOG.info("uniuq id: {}", uniqueId);

        final BasicDBObject queryObj = new BasicDBObject(experiment ? uniqueId : SOLUTION_ID, submissionId);
        if (cursor == null) {
            LOG.info("Creating a new instance to this old itemid");
            queryObj.append(SELF_ID, new ObjectId(problemId));
            collection.insert(queryObj);
            // we need to create a new cursor
        } else {
            LOG.info("adding a new submission to this old itemid");
            // insert the submissionId, if it is an experiment then we need to
            // use the uniqueId to make it work.
            collection.update(cursor, new BasicDBObject("$set", queryObj));
        }
    }

    /**
     * Sends a request to the submission server to request an experiment as a user.
     *
     * @param dbs The database that contains data about the experiment.
     * @param userId The user who has access to the experiment.
     * @param problemId The id of the problem associated with the sketch.
     * @param submissionManager The connections of the submission server
     * @throws DatabaseAccessException Thrown is there is data missing in the database.
     * @throws AuthenticationException Thrown if the user does not have the authentication
     * @return {@link protobuf.srl.submission.Submission.SrlExperiment} that had the specific submission id.
     */
    public static Submission.SrlExperiment mongoGetExperiment(final DB dbs, final String userId, final String problemId,
            final SubmissionManagerInterface submissionManager) throws DatabaseAccessException, AuthenticationException {

        final Data.ItemResult.Builder send = Data.ItemResult.newBuilder();
        send.setQuery(ItemQuery.EXPERIMENT);
        final DBObject cursor = dbs.getCollection(DatabaseStringConstants.EXPERIMENT_COLLECTION).findOne(new ObjectId(problemId));
        if (cursor == null || !cursor.containsField(userId) || Strings.isNullOrEmpty((String) cursor.get(userId))) {
            throw new DatabaseAccessException("The student has not submitted anything for this problem");
        }
        final String sketchId = cursor.get(userId).toString();
        LOG.info("SketchId: ", sketchId);

        final List<Submission.SrlExperiment> experimentList = submissionManager.getSubmission(userId, null, problemId, sketchId);
        if (experimentList.isEmpty()) {
            throw new DatabaseAccessException("No experiments were found");
        }
        return experimentList.get(0);
    }

    /**
     * Builds a request to the server for all of the sketches in a single problem.
     *
     * @param authenticator The object being used to authenticate the server.
     * @param dbs The database where the data is stored.
     * @param userId The user that was requesting this information.
     * @param problemId The problem for which the sketch data is being requested.
     * @param submissionManager The connections of the submission server
     * @throws DatabaseAccessException Thrown if there are no problems data that exist.
     * @throws AuthenticationException Thrown if the user does not have the authentication.
     * @return {@link protobuf.srl.submission.Submission.SrlExperiment} that were found with the specific submission ids.
     */
    public static List<Submission.SrlExperiment> mongoGetAllExperimentsAsInstructor(final Authenticator authenticator, final DB dbs,
            final String userId, final String problemId,
            final SubmissionManagerInterface submissionManager)
            throws DatabaseAccessException, AuthenticationException {

        final DBObject problemExperimentMap = dbs.getCollection(DatabaseStringConstants.EXPERIMENT_COLLECTION).findOne(new ObjectId(problemId));
        if (problemExperimentMap == null) {
            throw new DatabaseAccessException("Students have not submitted any data for this problem: " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, problemId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final List<String> itemRequest = createSubmissionRequest(problemExperimentMap);
        final String[] submissionIds = itemRequest.toArray(new String[itemRequest.size()]);
        final List<Submission.SrlExperiment> experimentList = submissionManager
                .getSubmission(userId, authenticator, problemId, submissionIds);
        if (experimentList.isEmpty()) {
            throw new DatabaseAccessException("No experiments were found");
        }
        return experimentList;
    }

    /**
     * Creates a submission request for the submission server.
     *
     * @param experiments A {@link DBObject} that represents the experiments in the database.
     * @return {@link List<String>} of submission ids that is used to query the submission server.
     */
    private static List<String> createSubmissionRequest(final DBObject experiments) {
        final List<String> submissionIds = new ArrayList<>();
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
            submissionIds.add(sketchId);
        }
        return submissionIds;
    }

}
