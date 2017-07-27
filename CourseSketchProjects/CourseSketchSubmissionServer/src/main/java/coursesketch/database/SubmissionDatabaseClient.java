package coursesketch.database;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.MongoQuestionDataBuilder;
import coursesketch.database.util.MongoUtilities;
import coursesketch.database.util.SubmissionException;
import coursesketch.server.interfaces.ServerInfo;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.question.QuestionDataOuterClass.SketchArea;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;
import util.MergeException;
import util.SubmissionMerger;
import utilities.Encoder;
import utilities.LoggingConstants;
import utilities.TimeManager;

import static coursesketch.database.util.DatabaseStringConstants.ALLOWED_IN_PROBLEMBANK;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_ID;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.DATABASE;
import static coursesketch.database.util.DatabaseStringConstants.DOMAIN_ID;
import static coursesketch.database.util.DatabaseStringConstants.EXPERIMENT_COLLECTION;
import static coursesketch.database.util.DatabaseStringConstants.FIRST_STROKE_TIME;
import static coursesketch.database.util.DatabaseStringConstants.FIRST_SUBMISSION_TIME;
import static coursesketch.database.util.DatabaseStringConstants.IS_PRACTICE_PROBLEM;
import static coursesketch.database.util.DatabaseStringConstants.ITEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.PROBLEM_BANK_ID;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.SOLUTION_COLLECTION;
import static coursesketch.database.util.DatabaseStringConstants.SUBMISSION_TIME;
import static coursesketch.database.util.DatabaseStringConstants.UPDATELIST;
import static coursesketch.database.util.DatabaseStringConstants.USER_ID;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.createDomainIdFromProto;
import static coursesketch.database.util.MongoUtilities.getQuestionType;

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
    private MongoDatabase database;

    /**
     * A private SubmissionDatabaseClient that accepts data for the database location.
     *
     * @param info Server information.
     *             The location that the server is taking place.
     */
    public SubmissionDatabaseClient(final ServerInfo info) {
        super(info);
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database.
     *
     * @param testOnly if true it uses the test database. Otherwise it uses the real
     *                 name of the database.
     * @param fakeDB   The fake database.
     */
    public SubmissionDatabaseClient(final boolean testOnly, final MongoDatabase fakeDB) {
        this(null);
        if (testOnly && fakeDB != null) {
            database = fakeDB;
        } else {
            final MongoClient mongoClient = new MongoClient("localhost");
            if (testOnly) {
                database = mongoClient.getDatabase("test");
            } else {
                database = mongoClient.getDatabase(DATABASE);
            }
        }
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     * <p>
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     * <p>
     * It also ensures that the solution received is built on the previous solution as we do not permit the overwriting of history
     *
     * @param solution The element being saved.
     * @return id if the element does not already exist.
     * @throws DatabaseAccessException problems
     */
    public String saveSolution(final SrlSolution solution) throws DatabaseAccessException {
        LOG.info("\n\n\nsaving the experiment!");
        final MongoCollection<Document> solutions = database.getCollection(SOLUTION_COLLECTION);

        final Document findQuery = new Document(PROBLEM_BANK_ID, solution.getProblemBankId());
        final MongoCursor<Document> multipleObjectCursor = solutions.find(findQuery).iterator();
        Document existingSolution;
        if (multipleObjectCursor.hasNext()) {
            existingSolution = multipleObjectCursor.next();
            multipleObjectCursor.close();
            final Document updateObj;
            try {
                updateObj = createSubmission(solution.getSubmission(), existingSolution, true, TimeManager.getSystemTime());
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission from existing submission", e);
            }
            updateObj.append(DOMAIN_ID, createDomainIdFromProto(solution.getProblemDomain()));
            solutions.updateOne(existingSolution, new Document("$set", updateObj));
        } else {
            LOG.info("No existing submissions found");

            final Document submissionObject;
            try {
                submissionObject = createSubmission(solution.getSubmission(), null, true, TimeManager.getSystemTime());
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission", e);
            }

            final Document query = new Document(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
                    .append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
                    .append(PROBLEM_BANK_ID, solution.getProblemBankId());

            query.putAll(submissionObject);

            solutions.insertOne(query);
            existingSolution = solutions.find(query).first();
            multipleObjectCursor.close();
        }
        return existingSolution.get(SELF_ID).toString();
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     * <p>
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     * @param experiment     What the user is submitting.
     * @param submissionTime What time the server got the submission.
     * @return A string representing the id if it exists or is a new submission.
     * @throws DatabaseAccessException thrown if there are problems saving the experiment.
     */
    public String saveExperiment(final SrlExperiment experiment, final long submissionTime)
            throws DatabaseAccessException {
        LOG.info("saving the experiment!");
        verifyInput(experiment);

        final MongoCollection<Document> experiments = database.getCollection(EXPERIMENT_COLLECTION);

        final Document findQuery = new Document(COURSE_PROBLEM_ID, experiment.getProblemId())
                .append(USER_ID, experiment.getUserId());

        if (experiment.hasPartId()) {
            findQuery.append(ITEM_ID, experiment.getPartId());
        }
        LOG.info("Searching for existing solutions {}", findQuery);
        final MongoCursor<Document> multipleObjectCursor = experiments.find(findQuery).sort(new Document(SUBMISSION_TIME, -1)).iterator();
        LOG.info("Do we have the next cursor {}", multipleObjectCursor.hasNext());
        if (LOG.isDebugEnabled()) {
            LOG.info("Number of solutions found {}", experiments.count(findQuery));
        }
        Document existingSubmission;

        if (multipleObjectCursor.hasNext()) {
            LOG.info("UPDATING AN EXPERIMENT!!!!!!!!");
            existingSubmission = multipleObjectCursor.next();

            isSameQuestionType(existingSubmission, experiment.getSubmission());

            // TODO figure out how to update a document with a single command

            final Document updateObj;
            try {
                updateObj = createSubmission(experiment.getSubmission(), existingSubmission, false, submissionTime);
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission from existing submission", e);
            }
            final Document updateObj2 = new Document(SUBMISSION_TIME, submissionTime);
            final Document updateQueryPart2 = new Document("$set", updateObj);
            final Document updateQuery2Part2 = new Document("$set", updateObj2);
            experiments.updateOne(existingSubmission, updateQueryPart2);
            experiments.updateOne(existingSubmission, updateQuery2Part2);

            return existingSubmission.get(SELF_ID).toString();
        } else {
            multipleObjectCursor.close();
            final Document submissionObject;
            try {
                submissionObject = createSubmission(experiment.getSubmission(), null, false, submissionTime);
            } catch (SubmissionException e) {
                throw new DatabaseAccessException("Exception while creating submission", e);
            }
            final Document query = new Document(COURSE_ID, experiment.getCourseId())
                    .append(ASSIGNMENT_ID, experiment.getAssignmentId())
                    .append(COURSE_PROBLEM_ID, experiment.getProblemId())
                    .append(ITEM_ID, experiment.getPartId())
                    .append(USER_ID, experiment.getUserId())
                    .append(SUBMISSION_TIME, submissionTime);
            query.putAll(submissionObject);
            experiments.insertOne(query);
            existingSubmission = experiments.find(query).first();
        }
        return existingSubmission.get(SELF_ID).toString();
    }

    /**
     * Verifies that the input is valid.
     *
     * @param experiment The experiment that is trying to be stored in the database.
     * @throws DatabaseAccessException Thrown if a part of the experiment is invalid.
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
     * @param itemId      The id of the experiment we are trying to retrieve.
     * @param problemId   This much match the problemId of the submission stored here otherwise it is considered an invalid retrieval.
     * @param permissions The permissions of the person attempting to get the experiment.
     * @return the experiment found in the database.
     * @throws DatabaseAccessException thrown if there are problems getting the item.
     * @throws AuthenticationException thrown if the problemId given does not match the problemId in the database.
     */
    public SrlExperiment getExperiment(final String itemId, final String problemId, final AuthenticationResponder permissions)
            throws DatabaseAccessException, AuthenticationException {
        if (Strings.isNullOrEmpty(problemId) || Strings.isNullOrEmpty(itemId)) {
            throw new DatabaseAccessException("Invalid arguments while getting experiment",
                    new IllegalArgumentException("itemId and problemId can not be null"));
        }

        LOG.info("Fetching experiment");
        final Document cursor = database.getCollection(EXPERIMENT_COLLECTION).find(convertStringToObjectId(itemId)).first();
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
            submissionId = cursor.get(SELF_ID).toString();
        }
        build.setProblemId(cursor.get(COURSE_PROBLEM_ID).toString());
        build.setCourseId(cursor.get(COURSE_ID).toString());
        build.setPartId(cursor.get(ITEM_ID).toString());
        SrlSubmission sub;
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
     * Gets the experiment by its id and sends all of the important information associated with it.
     *
     * @param itemId        The id of the experiment we are trying to retrieve.
     * @param bankProblemId This much match the problemId of the submission stored here otherwise it is considered an invalid retrieval.
     * @param permissions   The permissions of the person attempting to get the experiment.
     * @return the experiment found in the database.
     * @throws DatabaseAccessException thrown if there are problems getting the item.
     * @throws AuthenticationException thrown if the problemId given does not match the problemId in the database.
     */
    public SrlSolution getSolution(final String itemId, final String bankProblemId, final AuthenticationResponder permissions)
            throws DatabaseAccessException, AuthenticationException {
        if (Strings.isNullOrEmpty(bankProblemId) || Strings.isNullOrEmpty(itemId)) {
            throw new DatabaseAccessException("Invalid arguments while getting solution",
                    new IllegalArgumentException("itemId and bankProblemId can not be null"));
        }

        LOG.info("Fetching solution");
        final Document document = database.getCollection(SOLUTION_COLLECTION).find(convertStringToObjectId(itemId)).first();
        if (document == null) {
            throw new DatabaseAccessException("There is no experiment with id: " + itemId);
        }

        if (!bankProblemId.equals(document.get(PROBLEM_BANK_ID).toString())) {
            throw new AuthenticationException("Bank Problem Id of the submission must match the submission being requested.",
                    AuthenticationException.INVALID_PERMISSION);
        }

        final SrlSolution.Builder build = SrlSolution.newBuilder();

        String submissionId = null;

        // only moderators and above are allowed to see the user id.
        if (permissions.hasModeratorPermission()) {
            submissionId = document.get(SELF_ID).toString();
        }
        build.setProblemBankId(document.get(PROBLEM_BANK_ID).toString());
        build.setIsPracticeProblem((Boolean) document.get(IS_PRACTICE_PROBLEM));
        build.setAllowedInProblemBank((Boolean) document.get(ALLOWED_IN_PROBLEMBANK));
        SrlSubmission sub;
        try {
            sub = getSubmission(document, submissionId);
        } catch (SubmissionException e) {
            throw new DatabaseAccessException("Error getting submission data", e);
        }
        build.setProblemDomain(MongoUtilities.createDomainIdFromDocument(document.get(DOMAIN_ID, new Document())));

        build.setSubmission(sub);
        LOG.info("Experiment successfully fetched");
        return build.build();
    }

    /**
     * Retrieves the submission portion of the solution or experiment.
     *
     * @param submissionObject The database pointer to the data.
     * @param submissionId     An optional id to add to the submission.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws SubmissionException Thrown if there are issues getting the submission.
     */
    private static SrlSubmission getSubmission(final Document submissionObject, final String submissionId) throws SubmissionException {
        final SrlSubmission.Builder subBuilder = SrlSubmission.newBuilder();
        if (!Strings.isNullOrEmpty(submissionId)) {
            subBuilder.setId(submissionId);
        }

        final QuestionData questionData;
        try {
            questionData = new MongoQuestionDataBuilder().buildQuestionDataProto(submissionObject);
        } catch (DatabaseAccessException e) {
            throw new SubmissionException("Failed to build question data", e);
        }
        return subBuilder.setSubmissionData(questionData).build();
    }

    /**
     * Creates a database object for the submission object.  Handles certain values in certain ways if they exist.
     *
     * @param submission     The submission that is being inserted.
     * @param cursor         The existing submission,  This should be null if an existing list does not exist.
     * @param isMod          True if the user is acting as a moderator.
     * @param submissionTime The time that the server received the submission.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException thrown if there is a problem creating the database object.
     */
    @SuppressWarnings({ "PMD.ExceptionAsFlowControl", "PMD.UselessParentheses" })
    private static Document createSubmission(final SrlSubmission submission, final Document cursor,
                                             final boolean isMod, final long submissionTime) throws SubmissionException {
        if (!submission.hasSubmissionData()) {
            throw new SubmissionException("Tried to save as an invalid submission", null);
        }
        try {
            return (new MongoQuestionDataBuilder() {
                @Override
                protected Document createUpdateList(SketchArea sketchArea) throws DatabaseAccessException {
                    try {
                        return SubmissionDatabaseClient.createUpdateList(sketchArea, cursor, isMod, submissionTime);
                    } catch (SubmissionException e) {
                        throw new DatabaseAccessException("Failed to create update list", e);
                    }
                }
            }).createSubmission(submission.getSubmissionData());
        } catch (DatabaseAccessException e) {
            throw new SubmissionException("Failed to create question data document", e);
        }
    }

    /**
     * Checks to make sure that the question type is still the same as the old time.
     *
     * @param document The document being checked.
     * @param submissionData The new submission data.
     * @throws DatabaseAccessException Thrown if the data does not match.
     */
    private void isSameQuestionType(Document document, SrlSubmission submissionData) throws DatabaseAccessException {
        if (document != null && submissionData != null && submissionData.hasSubmissionData()
                && getQuestionType(document) != submissionData.getSubmissionData().getElementTypeCase()) {
            throw new DatabaseAccessException("Can not change the saved type of the question", null);
        }
    }

    /**
     * Gets the time for the first stroke in a submission.
     *
     * @param updateList The updateList in a submission.
     * @return the time for the first stroke recorded.
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
     * Gets the time for the last stroke in a submission.
     *
     * @param updateList The updateList in a submission.
     * @return the time for the last stroke recorded.
     */
    private static long getFirstSubmissionTime(final SrlUpdateList updateList) {
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
     * @param submission     The submission that is being inserted.
     * @param cursor         The existing submission,  This should be null if an existing list does not exist.
     * @param isMod          True if the user is acting as a moderator.
     * @param submissionTime The time that the server received the submission.
     * @return An object that represents how it would be stored in the database.
     * @throws SubmissionException thrown if there is a problem creating the database object.
     */
    static Document createUpdateList(final SketchArea submission, final Document cursor,
                                     final boolean isMod, final long submissionTime)
            throws SubmissionException {
        if (cursor != null) {
            SrlUpdateList result;
            try {
                final Object binary = cursor.get(UPDATELIST);
                if (getQuestionType(cursor) != QuestionData.ElementTypeCase.SKETCHAREA) {
                    throw new SubmissionException("Can not switch type of submission.", null);
                }
                if (binary == null) {
                    result = submission.getRecordedSketch();
                } else {
                    result = SrlUpdateList.parseFrom(ByteString.copyFrom(((Binary) binary).getData()));
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                result = submission.getRecordedSketch();
            }
            try {
                result = new SubmissionMerger(result, submission.getRecordedSketch()).setIsModerator(isMod).merge();
            } catch (MergeException e) {
                throw new SubmissionException("exception while merging the two lists.  Update rejected", e);
            }
            final long firstStrokeTime = getFirstStrokeTime(result);
            final long lastStrokeTime = getFirstSubmissionTime(result);
            result = setTime(result, submissionTime);
            return new Document(UPDATELIST, new Binary(result.toByteArray())).append(FIRST_STROKE_TIME, firstStrokeTime)
                    .append(FIRST_SUBMISSION_TIME, lastStrokeTime);
        } else {
            final long firstStrokeTime = getFirstStrokeTime(submission.getRecordedSketch());
            final long lastStrokeTime = getFirstSubmissionTime(submission.getRecordedSketch());
            return new Document(UPDATELIST, new Binary(setTime(submission.getRecordedSketch(), submissionTime).toByteArray()))
                    .append(FIRST_STROKE_TIME, firstStrokeTime).append(FIRST_SUBMISSION_TIME, lastStrokeTime);
        }
    }

    /**
     * Sets the time of the last update to be the submission time.
     * If the last update is not a save marker or a submission marker then it creates a new save marker.
     *
     * @param updateList     The update list that will have its last time set to the correct value.
     * @param submissionTime The time at which the server recieved the submission.
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
            saveUpdate.setUpdateId(Encoder.nextID().toString());
            saveUpdate.setTime(submissionTime);

            final Commands.Marker saveMarker = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
            final Commands.SrlCommand saveCommand = Commands.SrlCommand.newBuilder().setCommandType(Commands.CommandType.MARKER).setCommandId(
                    Encoder.nextID().toString()).setCommandData(saveMarker.toByteString())
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
     * Sets up the index to allow for quicker access to the submission.
     */
    @Override
    public void setUpIndexes() {
        LOG.info("Setting up an index");
        LOG.info("Experiment Index command: {}", new Document(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
        database.getCollection(EXPERIMENT_COLLECTION).createIndex(new Document(COURSE_PROBLEM_ID, 1)
                .append(ITEM_ID, 1)
                .append(USER_ID, 1), new IndexOptions().unique(true));
        database.getCollection(SOLUTION_COLLECTION).createIndex(new Document(PROBLEM_BANK_ID, 1),
                new IndexOptions().unique(true));
    }

    /**
     * Called when startDatabase is called if the database has not already been started.
     * <p>
     * This method should be synchronous.
     */
    @Override
    protected void onStartDatabase() {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDatabase(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }
}
