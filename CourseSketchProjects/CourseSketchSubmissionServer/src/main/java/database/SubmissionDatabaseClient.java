package database;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;
import util.MergeException;
import util.SubmissionMerger;
import utilities.LoggingConstants;

import java.net.UnknownHostException;

import static database.DatabaseStringConstants.ALLOWED_IN_PROBLEMBANK;
import static database.DatabaseStringConstants.ANSWER_CHOICE;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.DATABASE;
import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static database.DatabaseStringConstants.FIRST_STROKE_TIME;
import static database.DatabaseStringConstants.FIRST_SUBMISSION_TIME;
import static database.DatabaseStringConstants.IS_PRACTICE_PROBLEM;
import static database.DatabaseStringConstants.PROBLEM_BANK_ID;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SOLUTION_COLLECTION;
import static database.DatabaseStringConstants.SUBMISSION_TIME;
import static database.DatabaseStringConstants.TEXT_ANSWER;
import static database.DatabaseStringConstants.UPDATELIST;
import static database.DatabaseStringConstants.USER_ID;

/**
 * Manages the submissions in the database.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class SubmissionDatabaseClient extends AbstractCourseSketchDatabaseReader {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionDatabaseClient.class);

    /**
     * A private Database that stores all of the data used by mongo.
     */
    private DB database;

    /**
     * A private SubmissionDatabaseClient that accepts data for the database location.
     *
     * @param info Server information.
     *         The location that the server is taking place.
     */
    public SubmissionDatabaseClient(final ServerInfo info) {
        super(info);
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database.
     *
     * @param testOnly
     *         if true it uses the test database. Otherwise it uses the real
     *         name of the database.
     * @param fakeDB The fake database.
     */
    public SubmissionDatabaseClient(final boolean testOnly, final DB fakeDB) {
        this(null);
        if (testOnly && fakeDB != null) {
            database = fakeDB;
        } else {
            MongoClient mongoClient = null;
            try {
                mongoClient = new MongoClient("localhost");
            } catch (UnknownHostException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
            if (mongoClient == null) {
                return;
            }
            if (testOnly) {
                database = mongoClient.getDB("test");
            } else {
                database = mongoClient.getDB(DATABASE);
            }
        }
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     *
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     * It also ensures that the solution received is built on the previous solution as we do not permit the overwriting of history
     *
     * @param solution
     *         TODO change this to use new stuff
     * @return id if the element does not already exist.
     * @throws DatabaseAccessException
     *         problems
     */
    public String saveSolution(final SrlSolution solution) throws DatabaseAccessException {
        LOG.info("\n\n\nsaving the experiment!");
        final DBCollection solutions = database.getCollection(SOLUTION_COLLECTION);

        final BasicDBObject findQuery = new BasicDBObject(PROBLEM_BANK_ID, solution.getProblemBankId());
        final DBCursor multipleObjectCursor = solutions.find(findQuery);
        DBObject resultCursor = null;
        if (multipleObjectCursor.count() > 0) {
            resultCursor = multipleObjectCursor.next();
            multipleObjectCursor.close();
            final DBObject updateObj = new BasicDBObject(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray());
            solutions.update(resultCursor, new BasicDBObject("$set", updateObj));
        } else {
            LOG.info("No existing submissions found");

            final BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
                    .append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
                    .append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray())
                    .append(PROBLEM_BANK_ID, solution.getProblemBankId());

            solutions.insert(query);
            resultCursor = solutions.findOne(query);
            multipleObjectCursor.close();
        }
        return resultCursor.get(SELF_ID).toString();
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     *
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     *
     * @param experiment
     *         What the user is submitting.
     * @param submissionTime
     *         What time the server got the submission.
     * @return A string representing the id if it exists or is a new submission.
     * @throws DatabaseAccessException
     *         thrown if there are problems saving the experiment.
     */
    public String saveExperiment(final SrlExperiment experiment, final long submissionTime)
            throws DatabaseAccessException {
        LOG.info("saving the experiment!");
        verifyInput(experiment);

        final DBCollection experiments = database.getCollection(EXPERIMENT_COLLECTION);

        final BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, experiment.getProblemId())
                .append(USER_ID, experiment.getUserId());
        LOG.info("Searching for existing solutions {}", findQuery);
        final DBCursor multipleObjectCursor = experiments.find(findQuery).sort(new BasicDBObject(SUBMISSION_TIME, -1));
        LOG.info("Do we have the next cursor {}", multipleObjectCursor.hasNext());
        LOG.info("Number of solutions found {}", multipleObjectCursor.count());
        DBObject cursor = null;

        if (multipleObjectCursor.count() > 0) {
            LOG.info("UPDATING AN EXPERIMENT!!!!!!!!");
            cursor = multipleObjectCursor.next();

            // TODO figure out how to update a document with a single command

            final DBObject updateObj;
            try {
                updateObj = createSubmission(experiment.getSubmission(), cursor, false, submissionTime);
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission", e);
            }
            final DBObject updateObj2 = new BasicDBObject(SUBMISSION_TIME, submissionTime);
            final BasicDBObject updateQueryPart2 = new BasicDBObject("$set", updateObj);
            final BasicDBObject updateQuery2Part2 = new BasicDBObject("$set", updateObj2);
            experiments.update(cursor, updateQueryPart2);
            experiments.update(cursor, updateQuery2Part2);

            return cursor.get(SELF_ID).toString();
        } else {
            multipleObjectCursor.close();
            final DBObject submissionObject;
            try {
                submissionObject = createSubmission(experiment.getSubmission(), cursor, false, submissionTime);
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission", e);
            }
            final BasicDBObject query = new BasicDBObject(COURSE_ID, experiment.getCourseId())
                    .append(ASSIGNMENT_ID, experiment.getAssignmentId())
                    .append(COURSE_PROBLEM_ID, experiment.getProblemId())
                    .append(USER_ID, experiment.getUserId())
                            //        .append(ADMIN, experiment.getAccessPermissions().getAdminPermissionList())
                            //        .append(MOD, experiment.getAccessPermissions().getModeratorPermissionList())
                            //        .append(USERS, experiment.getAccessPermissions().getUserPermissionList())
                    .append(SUBMISSION_TIME, submissionTime);
            query.putAll(submissionObject);
            experiments.insert(query);
            cursor = experiments.findOne(query);
        }
        return cursor.get(SELF_ID).toString();
    }

    /**
     * Verifies that the input is valid.
     *
     * @param experiment
     *         The experiment that is trying to be stored in the database.
     * @throws DatabaseAccessException
     *         Thrown if a part of the experiment is invalid.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static void verifyInput(final SrlExperiment experiment) throws DatabaseAccessException {
        if (!experiment.hasProblemId() || Strings.isNullOrEmpty(experiment.getProblemId())) {
            throw new DatabaseAccessException("Problem id must be defined to make a submission");
        }

        if (!experiment.hasCourseId() || Strings.isNullOrEmpty(experiment.getCourseId())) {
            throw new DatabaseAccessException("Course id must be defined to make a submission");
        }

        if (!experiment.hasAssignmentId() || Strings.isNullOrEmpty(experiment.getAssignmentId())) {
            throw new DatabaseAccessException("Assignment id must be defined to make a submission");
        }
        if (!experiment.hasSubmission()) {
            throw new DatabaseAccessException("there is no submission data defined in this submission");
        }
        if (!experiment.hasUserId() || Strings.isNullOrEmpty(experiment.getUserId())) {
            throw new DatabaseAccessException("there is no user id data defined in this submission");
        }
    }

    /**
     * Gets the experiment by its id and sends all of the important information associated with it.
     *
     * @param itemId
     *         The id of the experiment we are trying to retrieve.
     * @param problemId
     *         This much match the problemId of the submission stored here otherwise it is considered an invalid retrieval.
     * @param permissions
     *          The permissions of the person attempting to get the experiment.
     * @return the experiment found in the database.
     * @throws DatabaseAccessException
     *         thrown if there are problems getting the item.
     * @throws AuthenticationException
     *         thrown if the problemId given does not match the problemId in the database.
     */
    public SrlExperiment getExperiment(final String itemId, final String problemId, final AuthenticationResponder permissions)
            throws DatabaseAccessException, AuthenticationException {
        if (Strings.isNullOrEmpty(problemId) || Strings.isNullOrEmpty(itemId)) {
            throw new DatabaseAccessException("Invalid arguments while getting experiment",
                    new IllegalArgumentException("itemId and problemId can not be null"));
        }

        LOG.info("Fetching experiment");
        final DBObject cursor = database.getCollection(EXPERIMENT_COLLECTION).findOne(new ObjectId(itemId));
        if (cursor == null) {
            throw new DatabaseAccessException("There is no experiment with id: " + itemId);
        }

        if (!problemId.equals(cursor.get(COURSE_PROBLEM_ID).toString())) {
            throw new AuthenticationException("Problem Id of the submission must match the submission being requested.",
                    AuthenticationException.INVALID_PERMISSION);
        }

        final SrlExperiment.Builder build = SrlExperiment.newBuilder();
        build.setAssignmentId(cursor.get(ASSIGNMENT_ID).toString());

        String submissionId = null;

        // only moderators and above are allowed to see the user id.
        if (permissions.hasModeratorPermission()) {
            build.setUserId(cursor.get(USER_ID).toString());
            submissionId = cursor.get(SELF_ID).toString();
        }
        build.setProblemId(cursor.get(COURSE_PROBLEM_ID).toString());
        build.setCourseId(cursor.get(COURSE_ID).toString());
        SrlSubmission sub = null;
        try {
            sub = getSubmission(cursor, submissionId);
        } catch (SubmissionException e) {
            throw new DatabaseAccessException("Error getting submission data", e);
        }

        build.setSubmission(sub);
        LOG.info("Experiment successfully fetched");
        return build.build();
    }

    /**
     * Retrieves the submission portion of the solution or experiment.
     *
     * @param submissionObject
     *         The database pointer to the data.
     * @param submissionId
     *         An optional id to add to the submission.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws SubmissionException
     *         Thrown if there are issues getting the submission.
     */
    private static SrlSubmission getSubmission(final DBObject submissionObject, final String submissionId) throws SubmissionException {
        final SrlSubmission.Builder subBuilder = SrlSubmission.newBuilder();
        if (!Strings.isNullOrEmpty(submissionId)) {
            subBuilder.setId(submissionId);
        }

        final SrlSubmission.SubmissionTypeCase submissionType = getExpectedType(submissionObject);

        switch (submissionType) {
            case UPDATELIST:
                final Object binary = submissionObject.get(UPDATELIST);
                if (binary == null) {
                    throw new SubmissionException("UpdateList did not contain any data", null);
                }
                try {
                    subBuilder.setUpdateList(SrlUpdateList.parseFrom(ByteString.copyFrom((byte[]) binary)));
                    return subBuilder.build();
                } catch (InvalidProtocolBufferException e) {
                    throw new SubmissionException("Error decoding update list", e);
                }
            case TEXTANSWER:
                final Object text = submissionObject.get(TEXT_ANSWER);
                if (text == null) {
                    throw new SubmissionException("Text answer did not contain any data", null);
                }
                subBuilder.setTextAnswer(text.toString());
                return subBuilder.build();
            case ANSWERCHOICE:
                final Object answerChoice = submissionObject.get(ANSWER_CHOICE);
                if (answerChoice == null) {
                    throw new SubmissionException("Text answer did not contain any data", null);
                }
                // prevents a possible class cast exception.
                subBuilder.setAnswerChoice(Integer.parseInt(answerChoice.toString()));
                return subBuilder.build();
            default:
                throw new SubmissionException("Submission data is not supported type or does not exist", null);

        }
    }

    /**
     * Creates a database object for the submission object.  Handles certain values in certain ways if they exist.
     *
     * @param submission
     *         The submission that is being inserted.
     * @param cursor
     *         The existing submission,  This should be null if an existing list does not exist.
     * @param isMod
     *         True if the user is acting as a moderator.
     * @param submissionTime
     *         The time that the server received the submission.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException
     *         thrown if there is a problem creating the database object.
     */
    private static BasicDBObject createSubmission(final SrlSubmission submission, final DBObject cursor,
            final boolean isMod, final long submissionTime) throws SubmissionException {
        if (submission.hasUpdateList()) {
            return createUpdateList(submission, cursor, isMod, submissionTime);
        } else if (submission.hasTextAnswer()) {
            return createTextSubmission(submission, cursor);
        } else if (submission.hasAnswerChoice()) {
            return createMultipleChoiceSolution(submission, cursor);
        } else {
            throw new SubmissionException("Tried to save as an invalid submission", null);
        }
    }

    /**
     * Creates a database object for the text submission.
     *
     * @param submission
     *         The submission that is being inserted.
     * @param cursor
     *         The existing submission,  This should be null if an existing list does not exist.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException
     *         thrown if there is a problem creating the database object.
     */
    private static BasicDBObject createTextSubmission(final SrlSubmission submission, final DBObject cursor) throws SubmissionException {
        if (cursor != null && getExpectedType(cursor) != SrlSubmission.SubmissionTypeCase.TEXTANSWER) {
            throw new SubmissionException("Can not switch to a text submission from a different type", null);
        }
        // don't store it as changes for right now.
        return new BasicDBObject(TEXT_ANSWER, submission.getTextAnswer());
    }

    /**
     * Creates a database object for the multiple choice submission.
     *
     * @param submission
     *         The submission that is being inserted.
     * @param cursor
     *         The existing submission,  This should be null if an existing list does not exist.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException
     *         thrown if there is a problem creating the database object.
     */
    private static BasicDBObject createMultipleChoiceSolution(final SrlSubmission submission, final DBObject cursor) throws SubmissionException {
        if (cursor != null && getExpectedType(cursor) != SrlSubmission.SubmissionTypeCase.ANSWERCHOICE) {
            throw new SubmissionException("Can not switch to a multiple choice submission from a different type", null);
        }
        // don't store it as changes for right now.
        return new BasicDBObject(ANSWER_CHOICE, submission.getAnswerChoice());
    }

    /**
    * Gets the time for the first stroke in a submission.
    * @param updateList The updateList in a submission.
    * @return the time for the first stroke recorded.
    *
    */
    private static long getFirstStrokeTime(final SrlUpdateList updateList) {
        for (int i = 0; i < updateList.getListList().size(); i++) {
            final Commands.SrlUpdate tmpUpdate = updateList.getListList().get(i);
            for (int j = 0; j < tmpUpdate.getCommandsCount(); j++) {
                final Commands.SrlCommand tmpCommand = tmpUpdate.getCommandsList().get(j);
                if (tmpCommand.getCommandType() == Commands.CommandType.ADD_STROKE) {
                    return tmpUpdate.getTime();
                }
            }
        }
        return -1;
    }

    /**
    *
    *  Gets the time for the last stroke in a submission.
    * @param updateList The updateList in a submission.
    * @return the time for the last stroke recorded.
    */
    static long getFirstSubmissionTime(final SrlUpdateList updateList) {
        try {
            for (int i = 0; i < updateList.getListList().size(); i++) {
                final Commands.SrlUpdate tmpUpdate = updateList.getListList().get(i);
                for (int j = 0; j < tmpUpdate.getCommandsCount(); j++) {
                    final Commands.SrlCommand tmpCommand = tmpUpdate.getCommandsList().get(j);

                    if (tmpCommand.getCommandType() == Commands.CommandType.MARKER) {
                        final Commands.Marker tmpMarker = Commands.Marker.parseFrom(tmpCommand.getCommandData());
                        if (tmpMarker.getType() == Commands.Marker.MarkerType.SUBMISSION) {
                            return tmpUpdate.getTime();
                        }
                    }
                } // End of the command loop.
            } // End of the update loop.
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * Creates a database object for the update list, merges the list if there is already a list in the database.
     *
     * @param submission
     *         The submission that is being inserted.
     * @param cursor
     *         The existing submission,  This should be null if an existing list does not exist.
     * @param isMod
     *         True if the user is acting as a moderator.
     * @param submissionTime
     *         The time that the server received the submission.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException
     *         thrown if there is a problem creating the database object.
     */
    static BasicDBObject createUpdateList(final SrlSubmission submission, final DBObject cursor,
            final boolean isMod, final long submissionTime)
            throws SubmissionException {
        if (cursor != null) {
            SrlUpdateList result = null;
            try {
                final Object binary = cursor.get(UPDATELIST);
                if (getExpectedType(cursor) != SrlSubmission.SubmissionTypeCase.UPDATELIST) {
                    throw new SubmissionException("Can not switch type of submission.", null);
                }
                if (binary == null) {
                    result = submission.getUpdateList();
                } else {
                    result = SrlUpdateList.parseFrom(ByteString.copyFrom((byte[]) binary));
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                result = submission.getUpdateList();
            }
            try {
                result = new SubmissionMerger(result, submission.getUpdateList()).setIsModerator(isMod).merge();
            } catch (MergeException e) {
                throw new SubmissionException("exception while merging the two lists.  Update rejected", e);
            }
            final long firstStrokeTime = getFirstStrokeTime(result);
            final long lastStrokeTime = getFirstSubmissionTime(result);
            result = setTime(result, submissionTime);
            return new BasicDBObject(UPDATELIST, result.toByteArray()).append(FIRST_STROKE_TIME, firstStrokeTime)
                    .append(FIRST_SUBMISSION_TIME, lastStrokeTime);
        } else {
            final long firstStrokeTime = getFirstStrokeTime(submission.getUpdateList());
            final long lastStrokeTime = getFirstSubmissionTime(submission.getUpdateList());
            return new BasicDBObject(UPDATELIST, setTime(submission.getUpdateList(), submissionTime).toByteArray())
                    .append(FIRST_STROKE_TIME, firstStrokeTime).append(FIRST_SUBMISSION_TIME, lastStrokeTime);
        }
    }

    /**
     * Sets the time of the last update to be the submission time.
     * If the last update is not a save marker or a submission marker then it creates a new save marker.
     *
     * @param updateList
     *         The update list that will have its last time set to the correct value.
     * @param submissionTime
     *         The time at which the server recieved the submission.
     * @return An update list with the correct submission time.
     */
    private static SrlUpdateList setTime(final SrlUpdateList updateList, final long submissionTime) {
        final SrlUpdateList.Builder builder = SrlUpdateList.newBuilder(updateList);
        final Commands.SrlUpdate finalUpdate = builder.getList(builder.getListCount() - 1);
        final Commands.SrlCommand com = finalUpdate.getCommands(0);
        boolean createNewMarker = false;
        if (com.getCommandType() != Commands.CommandType.MARKER) {
            // we need to create a new save marker
            createNewMarker = true;
        } else {
            try {
                final Commands.Marker marker = Commands.Marker.parseFrom(com.getCommandData());
                if (marker.getType() != Commands.Marker.MarkerType.SAVE && marker.getType() != Commands.Marker.MarkerType.SUBMISSION) {
                    createNewMarker = true;
                }
            } catch (InvalidProtocolBufferException e) {
                createNewMarker = true;
            }
        }

        if (createNewMarker) {
            final Commands.SrlUpdate.Builder saveUpdate = Commands.SrlUpdate.newBuilder();
            saveUpdate.setUpdateId(AbstractServerWebSocketHandler.Encoder.nextID().toString());
            saveUpdate.setTime(submissionTime);

            final Commands.Marker saveMarker = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
            final Commands.SrlCommand saveCommand = Commands.SrlCommand.newBuilder().setCommandType(Commands.CommandType.MARKER).setCommandId(
                    AbstractServerWebSocketHandler.Encoder.nextID().toString()).setCommandData(saveMarker.toByteString())
                    .setIsUserCreated(false).build();

            saveUpdate.addCommands(saveCommand);

            builder.addList(saveUpdate);
        } else {
            // just change the time on the last update.
            final Commands.SrlUpdate.Builder updatedTime = Commands.SrlUpdate.newBuilder(finalUpdate);
            updatedTime.setTime(submissionTime);
            builder.setList(builder.getListCount() - 1, updatedTime);
        }
        return builder.build();
    }

    /**
     * Returns the type of the submission.  Assumes this method is not called with null.
     *
     * @param cursor
     *         The object that we are trying to determine the type of.
     * @return The correct submission type given the cursor object.
     * @throws SubmissionException
     *         Thrown if there is no type found or if there are multiple types found.
     */
    private static SrlSubmission.SubmissionTypeCase getExpectedType(final DBObject cursor) throws SubmissionException {
        SrlSubmission.SubmissionTypeCase currentCase = null;
        if (cursor.containsField(UPDATELIST)) {
            currentCase = SrlSubmission.SubmissionTypeCase.UPDATELIST;
        }
        if (cursor.containsField(TEXT_ANSWER)) {
            if (currentCase != null) {
                throw new SubmissionException("Submission can not be multiple types at the same time.", null);
            }
            currentCase = SrlSubmission.SubmissionTypeCase.TEXTANSWER;
        }
        if (cursor.containsField(ANSWER_CHOICE)) {
            if (currentCase != null) {
                throw new SubmissionException("Submission can not be multiple types at the same time.", null);
            }
            currentCase = SrlSubmission.SubmissionTypeCase.ANSWERCHOICE;
        }
        if (currentCase == null) {
            throw new SubmissionException("Submission has no type specified", null);
        } else {
            return currentCase;
        }
    }

    /**
     * Sets up the index to allow for quicker access to the submission.
     */
    @Override
    public void setUpIndexes() {
        LOG.info("Setting up an index");
        LOG.info("Experiment Index command: {}", new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
        database.getCollection(EXPERIMENT_COLLECTION).createIndex(new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1).append("unique", true));
        database.getCollection(SOLUTION_COLLECTION).createIndex(new BasicDBObject(PROBLEM_BANK_ID, 1).append("unique", true));
    }

    /**
     * Called when startDatabase is called if the database has not already been started.
     *
     * This method should be synchronous.
     */
    @Override protected void onStartDatabase() {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }
}
