package database;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.submission.Submission.SrlChecksum;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;
import util.Checksum;

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
import static database.DatabaseStringConstants.UPDATELIST;
import static database.DatabaseStringConstants.USER_ID;

public class DatabaseClient {
    private static DatabaseClient instance;
    private DB db;

    private DatabaseClient(final String url) throws Exception {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(url);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (mongoClient == null) {
            throw new Exception("An error occured while making mongoClient");
        }
        db = (mongoClient.getDB("submissions"));
        if (db == null) {
            System.out.println("Db is null!");
        } else {
            setUpIndexes();
        }
    }

    private DatabaseClient() throws Exception {
        this("goldberglinux.tamu.edu");
        //this("localhost");
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
     *
     * @param testOnly
     */
    public DatabaseClient(final boolean testOnly) {
        try {
            final MongoClient mongoClient = new MongoClient("localhost");
            if (testOnly) {
                db = (mongoClient.getDB("test"));
            } else {
                db = (mongoClient.getDB("submissions"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = this;
    }

    public static DatabaseClient getInstance() {
        if (instance == null) {
            try {
                instance = new DatabaseClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     *
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     * It also ensures that the solution recieved is built on the previous solution as we do not permit the overwritting of history
     *
     * @param experiment
     * @return
     */
    public static String saveSolution(final SrlSolution solution, final DatabaseClient databaseClient) throws DatabaseException {
        System.out.println("\n\n\nsaving the experiment!");
        DBCollection solutions = databaseClient.getDB().getCollection(SOLUTION_COLLECTION);

        BasicDBObject findQuery = new BasicDBObject(PROBLEM_BANK_ID, solution.getProblemBankId());
        DBCursor c = solutions.find(findQuery);
        DBObject corsor = null;
        if (c.count() > 0) {
            corsor = c.next();
            c.close();
            DBCollection courses = databaseClient.getDB().getCollection(SOLUTION_COLLECTION);
            DBObject updateObj = new BasicDBObject(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray());
            courses.update(corsor, new BasicDBObject("$set", updateObj));
        } else {
            System.out.println("No existing submissions found");
            DBCollection new_user = databaseClient.getDB().getCollection(SOLUTION_COLLECTION);

            BasicDBObject query = new BasicDBObject(ALLOWED_IN_PROBLEMBANK, solution.getAllowedInProblemBank())
                    .append(IS_PRACTICE_PROBLEM, solution.getIsPracticeProblem())
                    .append(UPDATELIST, solution.getSubmission().getUpdateList().toByteArray())
                    .append(PROBLEM_BANK_ID, solution.getProblemBankId());

            new_user.insert(query);
            corsor = new_user.findOne(query);
            c.close();
        }
        return corsor.get(SELF_ID).toString();
    }

    /**
     * Saves the experiment trying to make sure that there are no duplicates.
     *
     * First it searches to see if any experiments exist.  If they do then we sort them by submission time and overwrite them!
     *
     * TODO SECURITY CHECKS!
     *
     * @param experiment
     * @return
     * @throws DatabaseException
     */
    public static String saveExperiment(final SrlExperiment experiment, final DatabaseClient databaseClient) throws DatabaseException {
        System.out.println("saving the experiment!");
        final DBCollection experiments = databaseClient.getDB().getCollection(EXPERIMENT_COLLECTION);

        final BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, experiment.getProblemId())
                .append(USER_ID, experiment.getUserId());
        System.out.println("Searching for existing solutions " + findQuery);
        DBCursor c = experiments.find(findQuery).sort(new BasicDBObject(SUBMISSION_TIME, -1));
        System.out.println("Do we have the next cursos " + c.hasNext());
        System.out.println("Number of solutions found" + c.count());
        DBObject corsor = null;
        if (c.count() > 0) {
            System.out.println("UPDATING AN EXPERIMENT!!!!!!!!");
            corsor = c.next();

            try {
                SrlUpdateList databaseList = SrlUpdateList.parseFrom((byte[]) corsor.get(UPDATELIST));
                SrlUpdateList inputList = SrlUpdateList.parseFrom(experiment.getSubmission().getUpdateList());

                SrlUpdateList result;
                result = completeListCheck(databaseList, inputList);
                // This is a safe comparison as the method ensures the same object is returned and is not aliased.
                if (result == databaseList) {
                    // We do a NO-OP and return success
                    return corsor.get(SELF_ID).toString();
                }
            } catch (InvalidProtocolBufferException e) {
                // TODO: figure out how to handle this event...
                e.printStackTrace();
            } finally {
                c.close();
            }

            final DBCollection courses = databaseClient.getDB().getCollection(EXPERIMENT_COLLECTION);

            // TODO: figure out how to update a document with a single command

            final DBObject updateObj = new BasicDBObject(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray());
            final DBObject updateObj2 = new BasicDBObject(SUBMISSION_TIME, experiment.getSubmission().getSubmissionTime());
            final BasicDBObject updateQueryPart2 = new BasicDBObject("$set", updateObj);
            final BasicDBObject updateQuery2Part2 = new BasicDBObject("$set", updateObj2);
            courses.update(corsor, updateQueryPart2);
            courses.update(corsor, updateQuery2Part2);
        } else {
            c.close();
            BasicDBObject query = new BasicDBObject(COURSE_ID, experiment.getCourseId())
                    .append(ASSIGNMENT_ID, experiment.getAssignmentId())
                    .append(COURSE_PROBLEM_ID, experiment.getProblemId())
                    .append(USER_ID, experiment.getUserId())
                            //.append(ADMIN, experiment.getAccessPermissions().getAdminPermissionList())
                            //.append(MOD, experiment.getAccessPermissions().getModeratorPermissionList())
                            //.append(USERS, experiment.getAccessPermissions().getUserPermissionList())
                    .append(UPDATELIST, experiment.getSubmission().getUpdateList().toByteArray())
                    .append(SUBMISSION_TIME, experiment.getSubmission().getSubmissionTime());
            experiments.insert(query);
            corsor = experiments.findOne(query);
        }
        return corsor.get(SELF_ID).toString();
    }

    /**
     * Gets the experiment by its id and sends all of the important information associated with it
     *
     * @param itemId
     * @return
     */
    public static SrlExperiment getExperiment(final String itemId, final DatabaseClient databaseClient) {
        System.out.println("Fetching experiment");
        final DBRef myDbRef = new DBRef(databaseClient.getDB(), EXPERIMENT_COLLECTION, new ObjectId(itemId));
        final DBObject corsor = myDbRef.fetch();

        if (corsor == null) {
            return null;
        }

        SrlExperiment.Builder build = SrlExperiment.newBuilder();
        build.setAssignmentId(corsor.get(ASSIGNMENT_ID).toString());
        build.setUserId(corsor.get(USER_ID).toString());
        build.setProblemId(corsor.get(COURSE_PROBLEM_ID).toString());
        build.setCourseId(corsor.get(COURSE_ID).toString());
        SrlSubmission.Builder sub = SrlSubmission.newBuilder();
        sub.setUpdateList(ByteString.copyFrom((byte[]) corsor.get(UPDATELIST)));
        build.setSubmission(sub.build());
        System.out.println("Experiment succesfully fetched");
        return build.build();
    }

    /**
     * Returns database object unmodified if the list are the same
     * If they are not the same but are compatibable a new object of the combined version is made
     *
     * @throws DatabaseException
     */
    public static SrlUpdateList completeListCheck(final SrlUpdateList database, final SrlUpdateList input) throws DatabaseException {
        final SrlChecksum databaseSum = Checksum.computeChecksum(database.getListList());
        final SrlChecksum inputSum = Checksum.computeChecksum(input.getListList());
        if (databaseSum.equals(inputSum)) {
            return database;
        }

        final int newIndex = Checksum.checksumIndex(input.getListList(), databaseSum);
        if (newIndex < 0) {
            throw new DatabaseException("Input list is not compatible with previous data");
        }

        final SrlUpdateList.Builder combinedList = SrlUpdateList.newBuilder(database);
        // The combined list should be safe as we can ensure that history is not changed as we use it to build the new list.
        // and the checksum of the new combined data should match the complete list of the input data
        // this is to safegaurd against a possible attack where they managed to figure out the checksum process
        // and come up with a way to ensure equality with it not being equal (so changing history will still not be possible)
        combinedList.addAllList(input.getListList().subList(newIndex + 1, input.getListCount()));
        return combinedList.build();
    }

    public final void setUpIndexes() {
        System.out.println("Setting up an index");
        System.out.println("Experiment Index command: " + new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1));
        db.getCollection(EXPERIMENT_COLLECTION).ensureIndex(new BasicDBObject(COURSE_PROBLEM_ID, 1).append(USER_ID, 1).append("unique", true));
        db.getCollection(SOLUTION_COLLECTION).ensureIndex(new BasicDBObject(PROBLEM_BANK_ID, 1).append("unique", true));
    }

    /**
     * Returns null if a subclass is used.
     *
     * @return An instance of the current database (this method is protected).
     */
    protected DB getDB() {
        if (getClass().equals(DatabaseClient.class)) {
            return db;
        }
        return null;
    }
}
