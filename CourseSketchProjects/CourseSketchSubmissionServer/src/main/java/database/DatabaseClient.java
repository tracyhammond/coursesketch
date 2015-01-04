package database;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.bson.types.ObjectId;
import protobuf.srl.commands.Commands;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;
import util.MergeException;
import util.SubmissionMerger;

import java.net.UnknownHostException;

import static database.DatabaseStringConstants.ALLOWED_IN_PROBLEMBANK;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.EXPERIMENT_COLLECTION;
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
public class DatabaseClient {
    /**
     * A single instance of the mongo institution.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static volatile DatabaseClient instance;

    /**
     * A private Database that stores all of the data used by mongo.
     */
    private DB database;

    /**
     * A private institution that accepts a url for the database location.
     *
     * @param url
     *         The location that the server is taking place.
     */
    private DatabaseClient(final String url) {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(url);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (mongoClient == null) {
            return;
        }
        database = mongoClient.getDB("submissions");
        if (database == null) {
            System.out.println("Db is null!");
        } else {
            setUpIndexes();
        }
    }

    /**
     * A default constructor that creates an instance at a specific database
     * location.
     */
    private DatabaseClient() {
        this("goldberglinux.tamu.edu");
        //this("localhost");
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database.
     *
     * @param testOnly
     *         true if we only want to tests the database client.
     */
    public DatabaseClient(final boolean testOnly) {
        try {
            final MongoClient mongoClient = new MongoClient("localhost");
            if (testOnly) {
                database = mongoClient.getDB("test");
            } else {
                database = mongoClient.getDB("submissions");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        instance = this;
    }

    /**
     * @return An instance of the mongo client. Creates it if it does not exist.
     */
    @SuppressWarnings("checkstyle:innerassignment")
    public static DatabaseClient getInstance() {
        DatabaseClient result = instance;
        if (result == null) {
            synchronized (DatabaseClient.class) {
                if (result == null) {
                    result = instance;
                    instance = result = new DatabaseClient();
                    //result.auth = new Authenticator(new MongoAuthenticator(instance.database));
                }
            }
        }
        return result;
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     *
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     * It also ensures that the solution recieved is built on the previous solution as we do not permit the overwritting of history
     *
     * @param solution
     *         TODO change this to use new stuff
     * @param client
     *         The database that is being used to store the data.
     * @return id if the element does not already exist.
     * @throws DatabaseAccessException
     *         problems
     */
    public static String saveSolution(final SrlSolution solution, final DatabaseClient client) throws DatabaseAccessException {
        System.out.println("\n\n\nsaving the experiment!");
        final DBCollection solutions = client.getDb().getCollection(SOLUTION_COLLECTION);

        final BasicDBObject findQuery = new BasicDBObject(PROBLEM_BANK_ID, solution.getProblemBankId());
        final DBCursor multipleObjectCursor = solutions.find(findQuery);
        DBObject resultCursor = null;
        if (multipleObjectCursor.count() > 0) {
            resultCursor = multipleObjectCursor.next();
            multipleObjectCursor.close();
            final DBObject updateObj = new BasicDBObject(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray());
            solutions.update(resultCursor, new BasicDBObject("$set", updateObj));
        } else {
            System.out.println("No existing submissions found");

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
     * TODO SECURITY CHECKS!
     *
     * @param experiment
     *         What the user is submitting.
     * @param submissionTime
     *         What time the server got the submission.
     * @param client
     *         The database that is being used to store the data.
     * @return A string representing the id if it exists or is a new submission.
     * @throws DatabaseAccessException
     *         thrown if there are problems saving the experiment.
     */
    public static String saveExperiment(final SrlExperiment experiment, final long submissionTime,
            final DatabaseClient client) throws DatabaseAccessException {
        System.out.println("saving the experiment!");
        final DBCollection experiments = client.getDb().getCollection(EXPERIMENT_COLLECTION);

        final BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, experiment.getProblemId())
                .append(USER_ID, experiment.getUserId());
        System.out.println("Searching for existing solutions " + findQuery);
        final DBCursor multipleObjectCursor = experiments.find(findQuery).sort(new BasicDBObject(SUBMISSION_TIME, -1));
        System.out.println("Do we have the next cursos " + multipleObjectCursor.hasNext());
        System.out.println("Number of solutions found" + multipleObjectCursor.count());
        DBObject cursor = null;

        if (multipleObjectCursor.count() > 0) {
            System.out.println("UPDATING AN EXPERIMENT!!!!!!!!");
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

            // We do not need to return the id it already exist
            return null;
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
     * Gets the experiment by its id and sends all of the important information associated with it.
     * TODO run auth checks.
     *
     * @param itemId
     *         the id of the experiment we are trying to retrieve.
     * @param client
     *         The database that is being used to store the data.
     * @return the experiment found in the database.
     * @throws DatabaseAccessException
     *         thrown if there are problems getting the item
     */
    public static SrlExperiment getExperiment(final String itemId, final DatabaseClient client) throws DatabaseAccessException {
        System.out.println("Fetching experiment");
        final DBObject cursor = client.getDb().getCollection(EXPERIMENT_COLLECTION).findOne(new ObjectId(itemId));

        if (cursor == null) {
            throw new DatabaseAccessException("There is no experiment with id: " + itemId);
        }

        final SrlExperiment.Builder build = SrlExperiment.newBuilder();
        build.setAssignmentId(cursor.get(ASSIGNMENT_ID).toString());
        build.setUserId(cursor.get(USER_ID).toString());
        build.setProblemId(cursor.get(COURSE_PROBLEM_ID).toString());
        build.setCourseId(cursor.get(COURSE_ID).toString());
        SrlSubmission sub = null;
        try {
            sub = getSubmission(cursor);
        } catch (SubmissionException e) {
            throw new DatabaseAccessException("Error getting submission data", e);
        }
        build.setSubmission(sub);
        System.out.println("Experiment successfully fetched");
        return build.build();
    }

    /**
     * Retrieves the submission portion of the solution or experiment.
     *
     * @param cursor
     *         the database pointer to the data.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws SubmissionException
     *         Thrown if there are issues getting the submission.
     */
    private static SrlSubmission getSubmission(final DBObject cursor) throws SubmissionException {
        final SrlSubmission.Builder subBuilder = SrlSubmission.newBuilder();
        Object result = cursor.get(UPDATELIST);
        if (result != null) {
            try {
                subBuilder.setUpdateList(SrlUpdateList.parseFrom(ByteString.copyFrom((byte[]) result)));
                return subBuilder.build();
            } catch (InvalidProtocolBufferException e) {
                throw new SubmissionException("Error decoding update list", e);
            }
        }
        result = cursor.get(TEXT_ANSWER);
        if (result != null) {
            subBuilder.setTextAnswer(result.toString());
            return subBuilder.build();
        }
        throw new SubmissionException("Submission data is not supported type or does not exist", null);
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
            return createTextSubmission(submission);
        } else {
            throw new SubmissionException("Tried to save as an invalid submission", null);
        }
    }

    /**
     * Creates a database object for the text submission.
     *
     * @param submission
     *         The submission that is being inserted.
     * @return An object that represents how it would be stored in the database.
     */
    private static BasicDBObject createTextSubmission(final SrlSubmission submission) {
        // don't store it as changes for right now.
        return new BasicDBObject(TEXT_ANSWER, submission.getTextAnswer());
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
    private static BasicDBObject createUpdateList(final SrlSubmission submission, final DBObject cursor,
            final boolean isMod, final long submissionTime)
            throws SubmissionException {
        if (cursor != null) {
            SrlUpdateList result = null;
            try {
                result = SrlUpdateList.parseFrom(ByteString.copyFrom((byte[]) cursor.get(UPDATELIST)));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                result = submission.getUpdateList();
            }
            try {
                result = new SubmissionMerger(result, submission.getUpdateList()).setIsModerator(isMod).merge();
            } catch (MergeException e) {
                throw new SubmissionException("exception while merging the two lists.  Update rejected", e);
            }
            result = setTime(result, submissionTime);
            return new BasicDBObject(UPDATELIST, result.toByteArray());
        } else {
            return new BasicDBObject(UPDATELIST, setTime(submission.getUpdateList(), submissionTime).toByteArray());
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
     * Sets up the index to allow for quicker access to the submission.
     */
    public final void setUpIndexes() {
        System.out.println("Setting up an index");
        System.out.println("Experiment Index command: " + new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
        database.getCollection(EXPERIMENT_COLLECTION).createIndex(new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1).append("unique", true));
        database.getCollection(SOLUTION_COLLECTION).createIndex(new BasicDBObject(PROBLEM_BANK_ID, 1).append("unique", true));
    }

    /**
     * Returns null if a subclass is used.
     *
     * @return An instance of the current database (this method is protected).
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected DB getDb() {
        if (getClass().equals(DatabaseClient.class)) {
            return database;
        }
        return null;
    }
}
