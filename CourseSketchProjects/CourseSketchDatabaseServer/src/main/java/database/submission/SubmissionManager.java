package database.submission;

import com.google.common.base.Strings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.submission.SubmissionManagerInterface;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.institution.mongo.MongoInstitution;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SOLUTION_COLLECTION;
import static database.DatabaseStringConstants.SOLUTION_ID;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Manages data that has to deal with submissions in the database server.
 *
 * This specifically is a link that links all of the institution data back to the submission data.
 * This does not actually store the submissions themselves.
 *
 * @author gigemjt
 */
public final class SubmissionManager {

    /**
     * Declaration and Definition.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionManager.class);

    /**
     * Private constructor.
     */
    private SubmissionManager() {
    }

    /**
     * Inserts a submission into the database.
     *
     * if {@code experiment} is true then {@code userId} is a userId otherwise
     * it is the bankProblem if {@code experiment} is true then {@code problem}
     * is a courseProblem otherwise it is the bankProblem.
     *
     * @param dbs The database that contains the information about the submission.
     * @param userId Generally the userId.  But it is used to uniquely identify each submission.
     * @param problemId The problem id.
     * @param submissionId The id associated with the submission on the submission server.
     * @param experiment True if the object being submitted is an experiment
     */
    @SuppressWarnings({ "PMD.NPathComplexity" })
    public static void mongoInsertSubmission(final MongoDatabase dbs, final String userId, final String problemId,
            final String submissionId,
            final boolean experiment) {
        LOG.info("Inserting an experiment {}", experiment);
        LOG.info("Database is {}", dbs);
        LOG.info("Problem id: {}", problemId);
        final Document myDbRef = new Document(SELF_ID, new ObjectId(problemId));
        final MongoCollection<Document> collection = dbs.getCollection(experiment ? EXPERIMENT_COLLECTION : SOLUTION_COLLECTION);
        final Document cursor = collection.find(myDbRef).first();

        LOG.info("cursor: {}", cursor);
        LOG.info("uniuq id: {}", userId);

        final Document queryObj = new Document(experiment ? userId : SOLUTION_ID, submissionId);
        if (cursor == null) {
            LOG.info("Creating a new instance to this old itemid");
            queryObj.append(SELF_ID, new ObjectId(problemId));
            collection.insertOne(queryObj);
            // we need to create a new cursor
        } else {
            LOG.info("adding a new submission to this old itemid");
            // insert the submissionId, if it is an experiment then we need to
            // use the userId to make it work.
            collection.updateOne(cursor, new Document("$set", queryObj));
        }
    }

    /**
     * Sends a request to the submission server to request an experiment as a user.
     *
     * @param authenticator The object being used to authenticate the user.
     * @param dbs The database that contains data about the experiment.
     * @param userId The user who has access to the experiment.
     * @param authId The id used to authenticate the users permissions to the submission.
     * @param courseId The id of the course the problem belongs to.
     * @param problemId The id of the problem associated with the sketch.
     * @param submissionManager The connections of the submission server
     * @return {@link protobuf.srl.submission.Submission.SrlExperiment} that had the specific submission id.
     * @throws DatabaseAccessException Thrown is there is data missing in the database.
     * @throws AuthenticationException Thrown if the user does not have the authentication
     */
    public static Submission.SrlExperiment mongoGetExperiment(final Authenticator authenticator, final MongoDatabase dbs, final String userId,
            final String authId, final String courseId, final String problemId,
            final SubmissionManagerInterface submissionManager) throws DatabaseAccessException, AuthenticationException {

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasStudentPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final Data.ItemResult.Builder send = Data.ItemResult.newBuilder();
        send.setQuery(ItemQuery.EXPERIMENT);
        final MongoCollection<Document> collection = dbs.getCollection(DatabaseStringConstants.EXPERIMENT_COLLECTION);
        final Document cursor = collection.find(convertStringToObjectId(problemId)).first();

        final String hashedUserId = MongoInstitution.hashUserId(userId, courseId);
        LOG.debug("Grabbing user with userId: {}", hashedUserId);
        if (cursor == null || !cursor.containsKey(hashedUserId)
                || Strings.isNullOrEmpty((String) cursor.get(hashedUserId))) {
            throw new DatabaseAccessException("The student has not submitted anything for this problem");
        }
        final String sketchId = cursor.get(hashedUserId).toString();
        LOG.info("SubmissionId: ", sketchId);

        final List<Submission.SrlExperiment> experimentList = submissionManager.getSubmission(authId, null, problemId, sketchId);
        if (experimentList.isEmpty()) {
            throw new DatabaseAccessException("No experiments were found");
        }
        return experimentList.get(0);
    }

    /**
     * Builds a request to the server for all of the sketches in a single problem.
     *
     * @param authenticator The object being used to authenticate the user.
     * @param dbs The database where the data is stored.
     * @param authId The user that was requesting this information.
     * @param problemId The problem for which the sketch data is being requested.
     * @param submissionManager The connections of the submission server
     * @param identityManager The connection to the identity server.
     * @return {@link protobuf.srl.submission.Submission.SrlExperiment} that were found with the specific submission ids.
     * @throws DatabaseAccessException Thrown if there are no problems data that exist.
     * @throws AuthenticationException Thrown if the user does not have the authentication.
     */
    public static List<Submission.SrlExperiment> mongoGetAllExperimentsAsInstructor(final Authenticator authenticator, final MongoDatabase dbs,
            final String authId, final String problemId,
            final SubmissionManagerInterface submissionManager,
            final IdentityManagerInterface identityManager)
            throws DatabaseAccessException, AuthenticationException {

        final Document problemExperimentMap = dbs.getCollection(DatabaseStringConstants.EXPERIMENT_COLLECTION)
                .find(convertStringToObjectId(problemId)).first();
        if (problemExperimentMap == null) {
            throw new DatabaseAccessException("Students have not submitted any data for this problem: " + problemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, problemId, authId, 0, authType);

        if (!responder.hasPeerTeacherPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final Map<String, String> itemRoster = identityManager.getItemRoster(authId, problemId, Util.ItemType.COURSE_PROBLEM, null, authenticator);
        LOG.debug("User Roster for problem: {}, Roster: {}", problemId, itemRoster);

        final List<String> itemRequest = createSubmissionRequest(problemExperimentMap, itemRoster);
        final String[] submissionIds = itemRequest.toArray(new String[itemRequest.size()]);
        final List<Submission.SrlExperiment> experimentList = submissionManager
                .getSubmission(authId, authenticator, problemId, submissionIds);

        if (experimentList.isEmpty()) {
            throw new DatabaseAccessException("No experiments were found");
        }

        final Map<String, String> userIdToSubmissionId = createSubmissionIdToUserIdMap(problemExperimentMap);
        LOG.debug("Submission id to user Id map: {}", userIdToSubmissionId);
        return mapExperimentToUserNames(itemRoster, userIdToSubmissionId, experimentList);
    }

    /**
     * Creates a list of experiments with user ids.
     *
     * @param userIdToUsername A map of userIds to usernames.
     * @param submissionIdToUserId A map of submissionIds to userIds.
     * @param experiments A list of experiments that do not contain valid usernames.
     * @return A list of {@link protobuf.srl.submission.Submission.SrlExperiment} that contain usernames.
     */
    private static List<Submission.SrlExperiment> mapExperimentToUserNames(final Map<String, String> userIdToUsername,
            final Map<String, String> submissionIdToUserId, final List<Submission.SrlExperiment> experiments) {
        final List<Submission.SrlExperiment> experimentListWithUserIds = new ArrayList<>();

        for (Submission.SrlExperiment experiment : experiments) {
            final String userId = experiment.getUserId();
            String userName = null;

            final String hashedUserId = submissionIdToUserId.get(experiment.getSubmission().getId());
            LOG.debug("unhahsed userId: {} Hashed userid: {} for experiment: {}", userId, hashedUserId, experiment.getSubmission().getId());
            userName = userIdToUsername.get(hashedUserId);

            if (userName == null) {
                LOG.debug("Userid does not exist in the course roster: {}", userId);
                userName = "" + ((Math.random() + 2.0) * (2 >> 2));
            }

            LOG.debug("UserName: {} for experiment: {}", userName, experiment.getSubmission().getId());

            experimentListWithUserIds.add(Submission.SrlExperiment.newBuilder(experiment).setUserId(userName).build());
            // experiment.
        }
        return experimentListWithUserIds;
    }

    /**
     * Creates a submission request for the submission server.
     *
     * @param experiments A {@link Document} that represents the experiments in the database.
     * @param itemRoster The list of users that are able to be viewed by the user wanting to view sketches.
     * @return {@link List<String>} of submission ids that is used to query the submission server.
     */
    private static List<String> createSubmissionRequest(final Document experiments, final Map<String, String> itemRoster) {
        final List<String> submissionIds = new ArrayList<>();
        for (String key : itemRoster.keySet()) {
            final String sketchId = experiments.get(key).toString();
            LOG.info("SketchId: {}", sketchId);
            submissionIds.add(sketchId);
        }
        return submissionIds;
    }

    /**
     * Creates a submission request for the submission server.
     *
     * @param experiments A {@link Document} that represents the experiments in the database.
     * @return {@link List<String>} of submission ids that is used to query the submission server.
     */
    private static Map<String, String> createSubmissionIdToUserIdMap(final Document experiments) {
        final Map<String, String> submissionIds = new HashMap<>();
        for (Map.Entry<String, Object> experimentEntry : experiments.entrySet()) {
            if (SELF_ID.equals(experimentEntry.getKey())) {
                continue;
            }
            final Object experimentId = experimentEntry.getValue();
            if (experimentId == null || experimentId instanceof ObjectId) {
                continue;
            }
            final String sketchId = experimentEntry.getValue().toString();
            LOG.info("SketchId: {}", sketchId);
            submissionIds.put(sketchId, experimentEntry.getKey());
        }
        return submissionIds;
    }

}
