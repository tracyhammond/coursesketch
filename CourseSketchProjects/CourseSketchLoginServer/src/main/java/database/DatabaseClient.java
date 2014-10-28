package database;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

<<<<<<< HEAD
import connection.PasswordHash;


public class DatabaseClient {
	private static DatabaseClient instance;
	private DB db;

	private DatabaseClient(String url) {
		System.out.println("creating new database instance");
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(url);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		db = mongoClient.getDB("login");
		if (db == null) {
			System.out.println("Db is null!");
		}
	}

	private DatabaseClient() {
		this("goldberglinux.tamu.edu");
		//this("localhost");
	}

	/**
	 * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
	 * @param testOnly
	 */
	public DatabaseClient(boolean testOnly) {
		try {
			MongoClient mongoClient = new MongoClient("localhost");
			if (testOnly) {
				db = mongoClient.getDB("test");
			} else {
				db = mongoClient.getDB("login");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	public static void main(String[] args) throws Exception {

	}

	public static DatabaseClient getInstance() {
		if(instance==null)
			instance = new DatabaseClient();
		return instance;
	}

	public static final String mongoIdentify(String u, String p) throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException {

		//boolean auth = getInstance().db.authenticate("headlogin","login".toCharArray());
		DBCollection table = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",u);

		DBObject corsor = table.findOne(query);

		if (corsor==null)
			return null;
		if (PasswordHash.validatePassword(p.toCharArray(),corsor.get("Password").toString())) {
			String result = corsor.get("ServerId") + ":" + corsor.get("ClientId");
			return result;
		} else {
			return null;
		}
		//return corsor.hasNext();
	}

	public static final boolean MongoAddUser(String user, String password,String email, boolean isInstructor) throws GeneralSecurityException, InvalidKeySpecException {
		DBCollection new_user = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",user);
		DBObject corsor = new_user.findOne(query);
		if(corsor == null)
		{
			query = new BasicDBObject("UserName",user)
				.append("Password",PasswordHash.createHash(password))
				.append("Email", email)
				.append("IsInstructor",isInstructor)
				.append("ServerId", Encoder.fancyID())
				.append("ClientId", Encoder.nextID().toString());
			new_user.insert(query);
			return true;
		}
		return false;
	}

	public static final boolean mongoIsInstructor(String user) {
		boolean auth = getInstance().db.authenticate("headlogin","login".toCharArray());
		DBCollection table = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",user);

		DBObject cursor = table.findOne(query);
		if (cursor == null) {
			System.out.println("Unable to find user!");
			return false;
		}
		
		String instructor = "" + cursor.get("IsInstructor");
		System.out.println("Instructor value " + instructor);
		if (cursor.get("IsInstructor") == null || instructor.equals("null")) {
			System.out.println("no value for instructor");
			return false;
		}
		return instructor.equals("true");
	}
=======
import static database.DatabaseStringConstants.PASSWORD;
import static database.DatabaseStringConstants.USER_NAME;
import static database.DatabaseStringConstants.LOGIN_COLLECTION;
import static database.DatabaseStringConstants.STUDENT_ID;
import static database.DatabaseStringConstants.STUDENT_CLIENT_ID;
import static database.DatabaseStringConstants.INSTRUCTOR_CLIENT_ID;
import static database.DatabaseStringConstants.INSTRUCTOR_ID;
import static database.DatabaseStringConstants.IS_DEFAULT_INSTRUCTOR;
import static database.DatabaseStringConstants.EMAIL;
import static database.DatabaseStringConstants.LOGIN_DATABASE;

import connection.LoginServerWebSocketHandler;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;

/**
 * A client for the login database.
 */
public class DatabaseClient {

    /**
     * A single instance of the database client.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static volatile DatabaseClient instance;

    /**
     * a private database.
     */
    @SuppressWarnings("PMD.UnusedPrivateField")
    private DB database = null;

    /**
     * @param url
     *            the location at which the database is created.
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
        database = mongoClient.getDB(LOGIN_DATABASE);
    }

    /**
     * Creates the database at a specific url.
     */
    private DatabaseClient() {
        this("goldberglinux.tamu.edu");
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test
     * instance that can only access a test database.
     *
     * @param testOnly
     *            if true it uses the test database. Otherwise it uses the real
     *            name of the database.
     * @param fakeDB
     *            uses a fake DB for its unit tests. This is typically used for
     *            unit test.
     */
    public DatabaseClient(final boolean testOnly, final DB fakeDB) {
        if (testOnly && fakeDB != null) {
            database = fakeDB;
        } else {
            MongoClient mongoClient = null;
            try {
                mongoClient = new MongoClient("localhost");
            } catch (UnknownHostException e) {
                e.printStackTrace();
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
                }
            }
        }
        return result;
    }

    /**
     * Logs in the user. Attempts to log in as the default account if that
     * option is specified otherwise it will login as the type that is
     * specified.
     *
     * @param user
     *            the user name that is attempting to login.
     * @param password
     *            the password of the user that is attempting to log in.
     * @param loginAsDefault
     *            true if the system will log in as the default account.
     * @param loginAsInstructor
     *            true if the system will log in as the instructor (not used if
     *            loginAsDefault is true).
     * @return The server side userid : the client side user id.
     * @throws LoginException
     *             thrown if there is a problem loggin in.
     */
    public static final String mongoIdentify(final String user, final String password, final boolean loginAsDefault, final boolean loginAsInstructor)
            throws LoginException {
        final DBCollection table = getInstance().database.getCollection(LOGIN_COLLECTION);
        final BasicDBObject query = new BasicDBObject(USER_NAME, user);

        final DBObject cursor = table.findOne(query);

        if (cursor == null) {
            throw new LoginException(LoginServerWebSocketHandler.INCORRECT_LOGIN_MESSAGE);
        }
        try {
            if (PasswordHash.validatePassword(password.toCharArray(), cursor.get(PASSWORD).toString())) {
                return getUserInfo(cursor, loginAsDefault, loginAsInstructor);
            } else {
                throw new LoginException(LoginServerWebSocketHandler.INCORRECT_LOGIN_MESSAGE);
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new LoginException("An error occured while comparing passwords", e);
        }
    }

    /**
     * Gets the user information. This assumes that the user was able to log in
     * correctly.
     *
     * @param cursor
     *            a pointer to the database object.
     * @param loginAsDefault
     *            true if the system will log in as the default account.
     * @param loginAsInstructor
     *            true if the system will log in as the instructor (not used if
     *            loginAsDefault is true).
     * @return A string representing the user id.
     * @throws LoginException
     *             Thrown if the user ids are not able to be grabbed.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    private static String getUserInfo(final DBObject cursor, final boolean loginAsDefault, final boolean loginAsInstructor) throws LoginException {
        String result;
        final boolean defaultAccountIsInstructor = (Boolean) cursor.get(IS_DEFAULT_INSTRUCTOR);
        if ((loginAsDefault && defaultAccountIsInstructor) || (!loginAsDefault && loginAsInstructor)) {
            result = cursor.get(INSTRUCTOR_ID) + ":" + cursor.get(INSTRUCTOR_CLIENT_ID);
        } else if ((loginAsDefault && !defaultAccountIsInstructor) || (!loginAsDefault && !loginAsInstructor)) {
            result = cursor.get(STUDENT_ID) + ":" + cursor.get(STUDENT_CLIENT_ID);
        } else {
            throw new LoginException(LoginServerWebSocketHandler.PERMISSION_ERROR_MESSAGE);
        }
        return result;
    }

    /**
     * Adds a new user to the database.
     *
     * @param user
     *            The user name to be added.
     * @param password
     *            the password of the user to be added to the DB.
     * @param email
     *            The email of the user.
     * @param isInstructor
     *            If the default account is an instructor
     * @throws GeneralSecurityException
     *             Thrown if there are problems creating the hash for the
     *             password.
     * @throws RegistrationException
     *             Thrown if the user already exist in the system.
     */
    public static final void createUser(final String user, final String password, final String email, final boolean isInstructor)
            throws GeneralSecurityException, RegistrationException {
        final DBCollection loginCollection = getInstance().database.getCollection(LOGIN_COLLECTION);
        BasicDBObject query = new BasicDBObject(USER_NAME, user);
        final DBObject cursor = loginCollection.findOne(query);
        if (cursor == null) {
            query = new BasicDBObject(USER_NAME, user).append(PASSWORD, PasswordHash.createHash(password)).append(EMAIL, email)
                    .append(IS_DEFAULT_INSTRUCTOR, isInstructor).append(INSTRUCTOR_ID, FancyEncoder.fancyID())
                    .append(STUDENT_ID, FancyEncoder.fancyID()).append(STUDENT_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString())
                    .append(INSTRUCTOR_CLIENT_ID, AbstractServerWebSocketHandler.Encoder.nextID().toString());
            loginCollection.insert(query);
        } else {
            throw new RegistrationException(LoginServerWebSocketHandler.REGISTRATION_ERROR_MESSAGE);
        }
    }

    /**
     * @param user
     *            the username of the account that is being checked.
     * @return true if the default account for the user is an instructor
     *         account.
     */
    public static final boolean defaultIsInstructor(final String user) {
        final DBCollection table = getInstance().database.getCollection(LOGIN_COLLECTION);
        final BasicDBObject query = new BasicDBObject(USER_NAME, user);

        final DBObject cursor = table.findOne(query);
        if (cursor == null) {
            System.out.println("Unable to find user!");
            return false;
        }
        return (Boolean) cursor.get(IS_DEFAULT_INSTRUCTOR);
    }
>>>>>>> origin/master

}
