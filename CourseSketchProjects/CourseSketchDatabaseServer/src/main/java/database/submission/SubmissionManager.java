package database.submission;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.*;
import connection.SubmissionClientWebSocket;
import coursesketch.server.interfaces.MultiConnectionManager;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import protobuf.srl.commands.Commands;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.tutorial.TutorialOuterClass;
import utilities.ConnectionException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

import static database.DatabaseStringConstants.*;

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
    public static void mongoGetExperiment(final DB dbs, final String userId, final String problemId, final String sessionInfo,
            final MultiConnectionManager internalConnections) throws DatabaseAccessException {
        final Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setSessionInfo(sessionInfo);
        requestBuilder.setRequestType(MessageType.DATA_REQUEST);
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
            final String problemId, final String sessionInfo, final MultiConnectionManager internalConnections, final ByteString review)
            throws DatabaseAccessException, AuthenticationException {
        final DBObject problem = new DBRef(dbs, COURSE_PROBLEM_COLLECTION, new ObjectId(problemId)).fetch();
        if (problem == null) {
            throw new DatabaseAccessException("Problem was not found with the following ID " + problemId);
        }
        final ArrayList adminList = (ArrayList<Object>) problem.get(ADMIN); // convert
        // to
        // ArrayList<String>
        final ArrayList modList = (ArrayList<Object>) problem.get(MOD); // convert
                                                                        // to
        // ArrayList<String>
        boolean isAdmin = false, isMod = false;
        isAdmin = authenticator.checkAuthentication(userId, adminList);
        isMod = authenticator.checkAuthentication(userId, modList);
        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setSessionInfo(sessionInfo);
        requestBuilder.setRequestType(MessageType.DATA_REQUEST);
        final ItemRequest.Builder build = ItemRequest.newBuilder();
        build.setQuery(ItemQuery.EXPERIMENT);
        final DBRef myDbRef = new DBRef(dbs, EXPERIMENT_COLLECTION, new ObjectId(problemId));
        final DBObject corsor = myDbRef.fetch();
        for (String key : corsor.keySet()) {
            if (SELF_ID.equals(key)) {
                continue;
            }
            final Object experimentId = corsor.get(key);
            if (experimentId == null || experimentId instanceof ObjectId) {
                continue;
            }
            final String sketchId = corsor.get(key).toString();
            LOG.info("SketchId: {}", sketchId);
            build.addItemId(sketchId);
        }
        build.setAdvanceQuery(review);
        final DataRequest.Builder data = DataRequest.newBuilder();
        data.addItems(build);
        requestBuilder.setOtherData(data.build().toByteString());
        LOG.info("Sending command: {}", requestBuilder.build());
        try {
            internalConnections.send(requestBuilder.build(), null, SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Builds a request to the server for all of the sketches in a single
     * problem.
     * @param authenticator The object being used to authenticate the server.
     * @param dbs The database where the data is stored.
     * @param userId The user that was requesting this information
     */
    public static String mongoInsertTutorial(final Authenticator authenticator, final DB dbs, final String userId,
            final TutorialOuterClass.Tutorial tutorialObject) throws DatabaseAccessException, AuthenticationException {
        final DBCollection tutorialCollection = dbs.getCollection(TUTORIAL_COLLECTION);

        final BasicDBObject query = new BasicDBObject(DESCRIPTION, tutorialObject.getDescription()).append(NAME, tutorialObject.getName())
                .append(URL, tutorialObject.getUrl()).append(URL_HASH, tutorialObject.getUrl().hashCode())
                .append(UPDATELIST, tutorialObject.getSteps().toByteArray());

        tutorialCollection.insert(query);
        final DBObject cursor = tutorialCollection.findOne(query);
        return cursor.get(SELF_ID).toString();
    }

    public static TutorialOuterClass.Tutorial mongoGetTutorial(final Authenticator authenticator, final DB dbs, final String userId,
            final String tutorialId) throws DatabaseAccessException, AuthenticationException {
        final DBCollection tutorialCollection = dbs.getCollection(TUTORIAL_COLLECTION);

        final DBObject cursor = tutorialCollection.findOne(new BasicDBObject(SELF_ID, new ObjectId(tutorialId)));
        if (cursor == null) {
            throw new DatabaseAccessException("Tutorial was not found with the following ID " + tutorialId);
        }

        return extractTutorial(cursor);
    }

    public static List<TutorialOuterClass.Tutorial> mongoGetTutorialList(final Authenticator authenticator, final DB dbs, final String userId,
                                                                         final String tutorialUrl) throws DatabaseAccessException, AuthenticationException {
        final DBCollection tutorialCollection = dbs.getCollection(TUTORIAL_COLLECTION);

        final DBCursor cursor = tutorialCollection.find(new BasicDBObject(URL_HASH, tutorialUrl.hashCode()));
        final List<TutorialOuterClass.Tutorial> tutorialList = new ArrayList<>();
        if (cursor == null) {
            throw new DatabaseAccessException("No tutorials were found with the following URL: " + tutorialUrl);
        }

        while(cursor.hasNext()) {
            tutorialList.add(extractTutorial(cursor.next()));
        }

        return tutorialList;
    }

    private static TutorialOuterClass.Tutorial extractTutorial(DBObject dbTutorial) throws DatabaseAccessException {
        final TutorialOuterClass.Tutorial.Builder tutorial = TutorialOuterClass.Tutorial.newBuilder();
        tutorial.setId(dbTutorial.get(SELF_ID).toString());
        tutorial.setName(dbTutorial.get(NAME).toString());
        tutorial.setDescription(dbTutorial.get(DESCRIPTION).toString());
        tutorial.setUrl(dbTutorial.get(URL).toString());
        try {
            tutorial.setSteps((Commands.SrlUpdateList.parseFrom((byte[]) dbTutorial.get(UPDATELIST))).toByteString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new DatabaseAccessException("unable to decode steps", e);
        }
        return tutorial.build();
    }
    // need to be able to get a single submission
    // be able to get all of the submissions
    // if you are trying to get your submission you just need your userId
    // if you are trying to get all submissions you need to authenticate with
    // the specific course problem.
}
