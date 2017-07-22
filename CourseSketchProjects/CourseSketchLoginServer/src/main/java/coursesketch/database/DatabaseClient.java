package coursesketch.database;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connection.LoginServerWebSocketHandler;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.authentication.HashManager;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static coursesketch.database.util.DatabaseStringConstants.EMAIL;
import static coursesketch.database.util.DatabaseStringConstants.INSTRUCTOR_CLIENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.INSTRUCTOR_ID;
import static coursesketch.database.util.DatabaseStringConstants.IS_DEFAULT_INSTRUCTOR;
import static coursesketch.database.util.DatabaseStringConstants.LOGIN_COLLECTION;
import static coursesketch.database.util.DatabaseStringConstants.LOGIN_DATABASE;
import static coursesketch.database.util.DatabaseStringConstants.PASSWORD;
import static coursesketch.database.util.DatabaseStringConstants.STUDENT_CLIENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.STUDENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.USER_NAME;

/**
 * A client for the login coursesketch.util.util.
 */
public final class DatabaseClient extends AbstractCourseSketchDatabaseReader {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseClient.class);

    /**
     * Max number of login times we store.
     */
    private static final int MAX_LOGIN_TIME_LENGTH = 10;

    /**
     * The key for the client id in the value returned for logging in.
     */
    public static final String CLIENT_ID = "ClientId";

    /**
     * The key for the server id in the value returned by logging in.
     */
    public static final String SERVER_ID = "ServerId";

    /**
     * The key for if the user is logged in as an instructor in the value returned for logging in.
     */
    public static final String IS_INSTRUCTOR = "IsInstructor";

    /**
     * Manages the identity of the user.
     */
    private final IdentityManagerInterface identityManager;

    /**
     * A private coursesketch.util.util.
     */
    @SuppressWarnings("PMD.UnusedPrivateField")
    private MongoDatabase database = null;

    /**
     * Constructor for the coursesketch.util.util client.
     *
     * @param info Server information
     * @param identityWebSocketClient The interface for getting user identity information.
     */
    public DatabaseClient(final ServerInfo info, final IdentityManagerInterface identityWebSocketClient) {
        super(info);
        identityManager = identityWebSocketClient;
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test coursesketch.util.util.
     *
     * @param testOnly
     *         If true it uses the test coursesketch.util.util. Otherwise it uses the real name of the coursesketch.util.util.
     * @param fakeDB
     *         Uses a fake MongoDatabase for its unit tests. This is typically used for unit testing.
     * @param identityWebSocketClient The interface for getting user identity information.
     */
    public DatabaseClient(final boolean testOnly, final MongoDatabase fakeDB, final IdentityManagerInterface identityWebSocketClient) {
        super(null);
        identityManager = identityWebSocketClient;
        if (testOnly && fakeDB != null) {
            database = fakeDB;
        } else {
            final MongoClient mongoClient = new MongoClient("localhost");
            if (testOnly) {
                database = mongoClient.getDatabase("test");
            } else {
                database = mongoClient.getDatabase(LOGIN_DATABASE);
            }
        }
    }

    /**
     * Sets up any indexes that need to be set up or have not yet been set up.
     */
    @Override protected void setUpIndexes() {
        //
    }

    /**
     * {@inheritDoc}
     *
     * Creates a coursesketch.util.util if one does not already exist.
     */
    @Override protected void onStartDatabase() {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDatabase(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    /**
     * Logs in the user.
     *
     * Attempts to log in as the default account if that option is specified otherwise it will login as the type that is specified.
     *
     * @param user
     *         The user name that is attempting to login.
     * @param password
     *         The password of the user that is attempting to log in.
     * @param loginAsDefault
     *         True if the system will log in as the default account.
     * @param loginAsInstructor
     *         True if the system will log in as the instructor (not used if loginAsDefault is true).
     * @return A basic db object with a set of values:
     *          {
     *              CLIENT_ID: clientId,
     *              SERVER_ID: serverId,
     *              IS_INSTRUCTOR: boolean
     *          }
     * @throws LoginException
     *         Thrown if there is a problem loggin in.
     */
    public Document mongoIdentify(final String user, final String password, final boolean loginAsDefault, final boolean loginAsInstructor)
            throws LoginException {
        final MongoCollection<Document> table = database.getCollection(LOGIN_COLLECTION);
        final Document query = new Document(USER_NAME, user);

        final Document cursor = table.find(query).first();

        if (cursor == null) {
            throw new LoginException(LoginServerWebSocketHandler.INCORRECT_LOGIN_MESSAGE);
        }
        try {
            final String hash = cursor.get(PASSWORD).toString();
            if (HashManager.validateHash(password, hash)) {
                return getUserInfo(cursor, loginAsDefault, loginAsInstructor);
            } else {
                if (!HashManager.CURRENT_HASH.equals(HashManager.getAlgorithmFromHash(hash))) {
                    final String newHash = HashManager.upgradeHash(password, hash);
                    if (newHash != null) {
                        updatePassword(table, cursor, password);
                        return getUserInfo(cursor, loginAsDefault, loginAsInstructor);
                    } else {
                        throw new LoginException(LoginServerWebSocketHandler.INCORRECT_LOGIN_MESSAGE);
                    }
                } else {
                    throw new LoginException(LoginServerWebSocketHandler.INCORRECT_LOGIN_MESSAGE);
                }
            }
        } catch (GeneralSecurityException | AuthenticationException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            throw new LoginException("An error occurred while comparing passwords", e);
        }
    }

    /**
     * Updates the password to the new value using the hash manager.
     *
     * This method is private on purpose please leave it that way.
     *
     * @param table
     *         The collection that the password is being updated in.
     * @param userDatabaseObject
     *         The user that the password is being updated for.
     * @param newPassword
     *         The new password.
     * @throws AuthenticationException
     *         Thrown if an invalid key is set.
     * @throws NoSuchAlgorithmException
     *         Thrown if the specified algorithm does not exist.
     */
    private void updatePassword(final MongoCollection<Document> table, final Document userDatabaseObject, final String newPassword)
            throws AuthenticationException, NoSuchAlgorithmException {
        final String newHash = HashManager.createHash(newPassword);
        table.updateOne(userDatabaseObject,
                new Document(DatabaseStringConstants.SET_COMMAND, new Document(DatabaseStringConstants.PASSWORD, newHash)));
    }

    /**
     * Gets the user information. This assumes that the user was able to log in correctly.
     *
     * @param cursor
     *         A pointer to the coursesketch.util.util object.
     * @param loginAsDefault
     *         True if the system will log in as the default account.
     * @param loginAsInstructor
     *         True if the system will log in as the instructor (not used if loginAsDefault is true).
     * @return A {@link Document} with a set of values:
     *          {
     *              CLIENT_ID: clientId,
     *              SERVER_ID: serverId,
     *              IS_INSTRUCTOR: boolean
     *          }
     * @throws LoginException
     *         Thrown if the user ids are not able to be grabbed.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    private Document getUserInfo(final Document cursor, final boolean loginAsDefault, final boolean loginAsInstructor) throws LoginException {
        final Document result =  new Document();
        final boolean defaultAccountIsInstructor = (Boolean) cursor.get(IS_DEFAULT_INSTRUCTOR);

        final boolean isDefaultInstructor = loginAsDefault && defaultAccountIsInstructor;
        final boolean isNonDefaultInstructor = !loginAsDefault && loginAsInstructor;

        final boolean isDefaultStudent = loginAsDefault && !defaultAccountIsInstructor;
        final boolean isNonDefaultStudent = !loginAsDefault && !loginAsInstructor;

        result.append(DatabaseClient.IS_INSTRUCTOR, isDefaultInstructor || isNonDefaultInstructor);
        if (isDefaultInstructor || isNonDefaultInstructor) {
            result.append(DatabaseClient.CLIENT_ID, cursor.get(INSTRUCTOR_CLIENT_ID));
            result.append(DatabaseClient.SERVER_ID, cursor.get(INSTRUCTOR_ID));
        } else if (isDefaultStudent || isNonDefaultStudent) {
            result.append(DatabaseClient.CLIENT_ID, cursor.get(STUDENT_CLIENT_ID));
            result.append(DatabaseClient.SERVER_ID, cursor.get(STUDENT_ID));
        } else {
            throw new LoginException(LoginServerWebSocketHandler.PERMISSION_ERROR_MESSAGE);
        }
        // Gets user id and add it to the result.
        final String userId = getUserId(cursor.get(DatabaseStringConstants.USER_NAME).toString(),
                cursor.get(DatabaseStringConstants.IDENTITY_AUTH).toString());
        result.append(DatabaseStringConstants.USER_ID, userId);

        return result;
    }

    /**
     * Gets the user identity for the server.
     * @param userName The username of the user
     * @param idAuth The authentication needed to get the id.
     * @return The user id
     * @throws LoginException Thrown if there are problems getting the user id.
     */
    private String getUserId(final String userName, final String idAuth) throws LoginException {
        try {
            return identityManager.getUserIdentity(userName, idAuth);
        } catch (AuthenticationException | DatabaseAccessException e) {
            throw new LoginException("Error getting the user identity", e);
        }
    }

    /**
     * Adds a new user to the coursesketch.util.util.
     *
     * @param user
     *         The user name to be added.
     * @param password
     *         The password of the user to be added to the MongoDatabase.
     * @param email
     *         The email of the user.
     * @param isInstructor
     *         If the default account is an instructor.
     * @throws AuthenticationException
     *         Thrown if an invalid key is set.
     * @throws NoSuchAlgorithmException
     *         Thrown if the specified algorithm does not exist.
     * @throws RegistrationException
     *         Thrown if the user already exist in the system.
     * @throws DatabaseAccessException
     *         Thrown if there are problems with the IdentityServer
     * @return The user identity grabbed from the identity server.
     */
    public String createUser(final String user, final String password, final String email, final boolean isInstructor)
            throws AuthenticationException, NoSuchAlgorithmException, RegistrationException, DatabaseAccessException {
        final MongoCollection<Document> loginCollection = database.getCollection(LOGIN_COLLECTION);
        Document query = new Document(USER_NAME, user);
        final Document cursor = loginCollection.find(query).first();
        if (cursor == null) {
            final Map<String, String> result = identityManager.createNewUser(user);
            if (result.isEmpty()) {
                throw new RegistrationException("Unable to get the password from the new user");
            }
            final Map.Entry<String, String> userIdentity =  result.entrySet().iterator().next();
            query = new Document(USER_NAME, user).append(PASSWORD, HashManager.createHash(password)).append(EMAIL, email)
                    .append(IS_DEFAULT_INSTRUCTOR, isInstructor).append(INSTRUCTOR_ID, FancyEncoder.fancyID())
                    .append(STUDENT_ID, FancyEncoder.fancyID()).append(STUDENT_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString())
                    .append(INSTRUCTOR_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString())
                    .append(DatabaseStringConstants.IDENTITY_AUTH, userIdentity.getValue());
            loginCollection.insertOne(query);
            return userIdentity.getKey();
        } else {
            throw new RegistrationException(LoginServerWebSocketHandler.REGISTRATION_ERROR_MESSAGE);
        }
    }

    /**
     * Adds The last login time for the user.
     *
     * This is limited to the last {@code MAX_LOGIN_TIME_LENGTH} number of times.
     * Searches for the user first.
     *
     * @param username The username of the person logging in.
     * @param authId The authentication of the person logging in.  (To ensure that they have actually logged in.)
     * @param isInstructor True if the user is logging in as an instructor.
     * @param systemTime
     *         A list of system times.
     *         This should almost always be a single time but is in a vararg format to make it easier for inserting a list.
     */
    public void userLoggedInSuccessfully(final String username, final String authId, final boolean isInstructor, final List<Long> systemTime) {
        final MongoCollection<Document> loginCollection = database.getCollection(LOGIN_COLLECTION);
        final Document query = new Document(USER_NAME, username).append(isInstructor ? INSTRUCTOR_ID : STUDENT_ID, authId);

        /*
            $push: {
                LAST_LOGIN_TIMES: {
                    $each: [ systemTime ],
                    $sort: -1,
                    $slice: MAX_LOGIN_TIME_LENGTH
                }
            },
            $inc: {
                LOGIN_AMOUNT_FIELD: 1
            }
         */
        final Document update = new Document(DatabaseStringConstants.PUSH_COMMAND,
                new Document(DatabaseStringConstants.LAST_LOGIN_TIMES,
                        new Document(DatabaseStringConstants.EACH_COMMAND, systemTime)
                                .append(DatabaseStringConstants.SORT_COMMAND, -1)
                                .append(DatabaseStringConstants.SLICE_COMMAND, MAX_LOGIN_TIME_LENGTH)))
                .append(DatabaseStringConstants.INCREMENT_COMMAND,
                        new Document(DatabaseStringConstants.LOGIN_AMOUNT_FIELD, 1));

        loginCollection.updateOne(query, update);
    }
}
