package coursesketch.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import connection.LoginServerWebSocketHandler;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.authentication.HashManager;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static database.DatabaseStringConstants.EMAIL;
import static database.DatabaseStringConstants.INSTRUCTOR_CLIENT_ID;
import static database.DatabaseStringConstants.INSTRUCTOR_ID;
import static database.DatabaseStringConstants.IS_DEFAULT_INSTRUCTOR;
import static database.DatabaseStringConstants.LOGIN_COLLECTION;
import static database.DatabaseStringConstants.LOGIN_DATABASE;
import static database.DatabaseStringConstants.PASSWORD;
import static database.DatabaseStringConstants.STUDENT_CLIENT_ID;
import static database.DatabaseStringConstants.STUDENT_ID;
import static database.DatabaseStringConstants.USER_NAME;

/**
 * A client for the login database.
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
     * The key for the client id in the returned value for logging in.
     */
    public static final String CLIENT_ID = "ClientId";

    /**
     * The key for the server id in the returned value for logging in.
     */
    public static final String SERVER_ID = "ServerId";

    /**
     * The key for if the user is logged in as an instructor in the returned value for logging in.
     */
    public static final String IS_INSTRUCTOR = "IsInstructor";

    /**
     * Manages the identity of the user.
     */
    private final IdentityManagerInterface identityManager;

    /**
     * a private database.
     */
    @SuppressWarnings("PMD.UnusedPrivateField")
    private DB database = null;

    /**
     * @param info Server information
     * @param identityWebSocketClient The interface for getting user identity information.
     */
    public DatabaseClient(final ServerInfo info, final IdentityManagerInterface identityWebSocketClient) {
        super(info);
        identityManager = identityWebSocketClient;
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test
     * instance that can only access a test database.
     *
     * @param testOnly
     *         if true it uses the test database. Otherwise it uses the real
     *         name of the database.
     * @param fakeDB
     *         uses a fake DB for its unit tests. This is typically used for
     *         unit test.
     * @param identityWebSocketClient The interface for getting user identity information.
     */
    public DatabaseClient(final boolean testOnly, final DB fakeDB, final IdentityManagerInterface identityWebSocketClient) {
        super(null);
        identityManager = identityWebSocketClient;
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
                database = mongoClient.getDB(LOGIN_DATABASE);
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
     * Creates a database if one does not already exist.
     */
    @Override protected void onStartDatabase() {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    /**
     * Logs in the user. Attempts to log in as the default account if that
     * option is specified otherwise it will login as the type that is
     * specified.
     *
     * @param user
     *         the user name that is attempting to login.
     * @param password
     *         the password of the user that is attempting to log in.
     * @param loginAsDefault
     *         true if the system will log in as the default account.
     * @param loginAsInstructor
     *         true if the system will log in as the instructor (not used if
     *         loginAsDefault is true).
     * @return A basic db object with a set of values:
     *          {
     *              CLIENT_ID: clientId,
     *              SERVER_ID: serverId,
     *              IS_INSTRUCTOR: boolean
     *          }
     * @throws LoginException
     *         thrown if there is a problem loggin in.
     */
    public BasicDBObject mongoIdentify(final String user, final String password, final boolean loginAsDefault, final boolean loginAsInstructor)
            throws LoginException {
        final DBCollection table = database.getCollection(LOGIN_COLLECTION);
        final BasicDBObject query = new BasicDBObject(USER_NAME, user);

        final DBObject cursor = table.findOne(query);

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
     * @param query
     *         The user that the password is being updated for.
     * @param newPassword
     *         The new password.
     * @throws AuthenticationException
     *         Thrown if an invalid key is set
     * @throws NoSuchAlgorithmException
     *         Thrown if the specified algorithm does not exist.
     */
    private void updatePassword(final DBCollection table, final DBObject query, final String newPassword)
            throws AuthenticationException, NoSuchAlgorithmException {
        final String newHash = HashManager.createHash(newPassword);
        table.update(query, new BasicDBObject(DatabaseStringConstants.SET_COMMAND, new BasicDBObject(DatabaseStringConstants.PASSWORD, newHash)));
    }

    /**
     * Gets the user information. This assumes that the user was able to log in
     * correctly.
     *
     * @param cursor
     *         a pointer to the database object.
     * @param loginAsDefault
     *         true if the system will log in as the default account.
     * @param loginAsInstructor
     *         true if the system will log in as the instructor (not used if
     *         loginAsDefault is true).
     * @return A {@link BasicDBObject} with a set of values:
     *          {
     *              CLIENT_ID: clientId,
     *              SERVER_ID: serverId,
     *              IS_INSTRUCTOR: boolean
     *          }
     * @throws LoginException
     *         Thrown if the user ids are not able to be grabbed.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    private BasicDBObject getUserInfo(final DBObject cursor, final boolean loginAsDefault, final boolean loginAsInstructor) throws LoginException {
        final BasicDBObject result =  new BasicDBObject();
        final boolean defaultAccountIsInstructor = (Boolean) cursor.get(IS_DEFAULT_INSTRUCTOR);
        result.append(DatabaseClient.IS_INSTRUCTOR,
                (loginAsDefault && defaultAccountIsInstructor) || (!loginAsDefault && loginAsInstructor));
        if ((loginAsDefault && defaultAccountIsInstructor) || (!loginAsDefault && loginAsInstructor)) {
            result.append(DatabaseClient.CLIENT_ID, cursor.get(INSTRUCTOR_CLIENT_ID));
            result.append(DatabaseClient.SERVER_ID, cursor.get(INSTRUCTOR_ID));
        } else if ((loginAsDefault && !defaultAccountIsInstructor) || (!loginAsDefault && !loginAsInstructor)) {
            result.append(DatabaseClient.CLIENT_ID, cursor.get(STUDENT_CLIENT_ID));
            result.append(DatabaseClient.SERVER_ID, cursor.get(STUDENT_ID));
        } else {
            throw new LoginException(LoginServerWebSocketHandler.PERMISSION_ERROR_MESSAGE);
        }
        final String userId = getUserId(cursor.get(DatabaseStringConstants.USER_NAME).toString(),
                cursor.get(DatabaseStringConstants.IDENTITY_AUTH).toString());
        result.append(DatabaseStringConstants.USER_ID, userId);
        // gets user id

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
     * Adds a new user to the database.
     *
     * @param user
     *         The user name to be added.
     * @param password
     *         the password of the user to be added to the DB.
     * @param email
     *         The email of the user.
     * @param isInstructor
     *         If the default account is an instructor
     * @throws AuthenticationException
     *         Thrown if an invalid key is set
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
        final DBCollection loginCollection = database.getCollection(LOGIN_COLLECTION);
        BasicDBObject query = new BasicDBObject(USER_NAME, user);
        final DBObject cursor = loginCollection.findOne(query);
        if (cursor == null) {
            final Map<String, String> result = identityManager.createNewUser(user);
            if (result.isEmpty()) {
                throw new RegistrationException("Unable to get the password from the new user");
            }
            final Map.Entry<String, String> userIdentity =  result.entrySet().iterator().next();
            query = new BasicDBObject(USER_NAME, user).append(PASSWORD, HashManager.createHash(password)).append(EMAIL, email)
                    .append(IS_DEFAULT_INSTRUCTOR, isInstructor).append(INSTRUCTOR_ID, FancyEncoder.fancyID())
                    .append(STUDENT_ID, FancyEncoder.fancyID()).append(STUDENT_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString())
                    .append(INSTRUCTOR_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString())
                    .append(DatabaseStringConstants.IDENTITY_AUTH, userIdentity.getValue());
            loginCollection.insert(query);
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
    public void userLoggedInSuccessfully(final String username, final String authId, final boolean isInstructor, final long... systemTime) {
        final DBCollection loginCollection = database.getCollection(LOGIN_COLLECTION);
        final BasicDBObject query = new BasicDBObject(USER_NAME, username).append(isInstructor ? INSTRUCTOR_ID : STUDENT_ID, authId);

        // FUTURE: remove this once https://github.com/fakemongo/fongo/issues/156 is resolved and use systemTime in mongo directly.
        final BasicDBList timeList = new BasicDBList();
        for (long time: systemTime) {
            timeList.add(time);
        }

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
        final BasicDBObject update = new BasicDBObject(DatabaseStringConstants.PUSH_COMMAND,
                new BasicDBObject(DatabaseStringConstants.LAST_LOGIN_TIMES,
                        new BasicDBObject(DatabaseStringConstants.EACH_COMMAND, timeList)
                                .append(DatabaseStringConstants.SORT_COMMAND, -1)
                                .append(DatabaseStringConstants.SLICE_COMMAND, MAX_LOGIN_TIME_LENGTH)))
                .append(DatabaseStringConstants.INCREMENT_COMMAND,
                        new BasicDBObject(DatabaseStringConstants.LOGIN_AMOUNT_FIELD, 1));

        loginCollection.update(query, update);
    }
}
