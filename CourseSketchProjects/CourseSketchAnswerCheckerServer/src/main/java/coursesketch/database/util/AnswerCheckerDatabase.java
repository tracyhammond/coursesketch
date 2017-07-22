package coursesketch.database.util;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.interfaces.ServerInfo;
import org.bson.Document;
import org.bson.types.ObjectId;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;

public class AnswerCheckerDatabase extends AbstractCourseSketchDatabaseReader {
    private MongoDatabase database;
    private Authenticator auth;

    /**
     * Takes in a list of addressess where the database can be found and a name of the database.

     * @param serverInfo Information about the server.
     */
    public AnswerCheckerDatabase(ServerInfo serverInfo, Authenticator auth) {
        super(serverInfo);
        this.auth = auth;
    }

    @Override
    protected void setUpIndexes() {
    }

    @Override
    protected void onStartDatabase() throws DatabaseAccessException {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDatabase(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    public String generateKey(String authId, Submission.SrlSolution solution)
            throws DatabaseAccessException, AuthenticationException {

        MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.SOLUTION_COLLECTION);
        FindIterable<Document> documents = collection.find(new Document(SELF_ID, new ObjectId(solution.getProblemBankId())));
        if (documents.iterator().hasNext()) {
            // We can just quit early since the work has already been done.
            return null;
        }

        AuthenticationResponder authenticationResponder = auth.checkAuthentication(Util.ItemType.BANK_PROBLEM, solution.getProblemBankId(), authId,
                0,
                Authentication.AuthType.newBuilder().setCheckingOwner
                        (true).setCheckingAdmin(true).build());
        if (!authenticationResponder.hasTeacherPermission()) {
            throw new AuthenticationException("User does not have permission to create key", AuthenticationException.INVALID_PERMISSION);
        }

        ObjectId key = new ObjectId();

        collection.insertOne(new Document(SELF_ID, new ObjectId(solution.getProblemBankId())).append(REGISTRATION_KEY, key));

        return key.toString();
        // do stuff
    }

    public String getKey(String authId, Submission.SrlExperiment studentExperiment) throws AuthenticationException {

        // Checks user has permission to problem
        AuthenticationResponder courseProblemResponder = auth.checkAuthentication(Util.ItemType.COURSE_PROBLEM, studentExperiment.getProblemId(),
                authId, 0,
                Authentication.AuthType.newBuilder().setCheckingUser(true).build());
        if (!courseProblemResponder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permission to create key", AuthenticationException.INVALID_PERMISSION);
        }

        AuthenticationResponder bankProblemResponder = auth.checkAuthentication(Util.ItemType.BANK_PROBLEM, studentExperiment.getProblemId(),
                studentExperiment.getCourseId(), 0,
                Authentication.AuthType.newBuilder().setCheckingUser(true).build());
        if (!bankProblemResponder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permission to create key", AuthenticationException.INVALID_PERMISSION);
        }

        MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.SOLUTION_COLLECTION);
        Document first = collection.find(new Document(SELF_ID, new ObjectId(studentExperiment.getProblemBankId()))).first();

        return first.get(REGISTRATION_KEY).toString();
    }
}
