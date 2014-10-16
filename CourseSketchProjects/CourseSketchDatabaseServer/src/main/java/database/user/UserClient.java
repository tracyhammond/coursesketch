package database.user;

import static database.DatabaseStringConstants.DATABASE;

import java.util.ArrayList;

import protobuf.srl.school.School.SrlSchool;
import protobuf.srl.school.School.SrlUser;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;

/**
 * A client for all user data.  This has its own database and instance.
 * @author gigemjt
 *
 */
public final class UserClient {

    /**
     * A specific instance used for client actions.
     */
    private static UserClient instance;

    /**
     * A database specific to each instance.
     */
    private DB db;

    /**
     * A private constructor that creates a client at a specific Url.
     * @param url The url is the location of the server.
     */
    private UserClient(final String url) {
        final MongoClient mongoClient = new MongoClient(url);
        db = mongoClient.getDB(DATABASE);
    }

    /**
     * A private constructor that creates a client.
     */
    private UserClient() {
        this("goldberglinux.tamu.edu");
    }

    /**
     * Gets the instance for local use. Creates it if it does not exist.
     * @return an instance of the user client.
     */
    private static UserClient getInstance() {
        if (instance == null) {
            instance = new UserClient();
        }
        return instance;
    }

    /**
     * Used only for the purpose of testing overwrite the instance with a test.
     * instance that can only access a test database.
     *
     * @param testOnly denotes that his is only being used for testing.
     */
    public UserClient(final boolean testOnly) {
        try {
            final MongoClient mongoClient = new MongoClient("localhost");
            if (testOnly) {
                db = mongoClient.getDB("test");
            } else {
                db = mongoClient.getDB(DATABASE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = this;
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user
     *            {@link SrlUser} data for the new user to be inserted.
     * @param userId
     *            The userId associated with the user.
     * @return True if it is successful otherwise it will throw an exception.
     * @throws DatabaseAccessException
     *             Thrown if there are problems inserting the user.
     */
    public static boolean insertUser(final SrlUser user, final String userId) throws DatabaseAccessException {
        UserManager.createUser(getInstance().db, user, userId);
        return true;
    }

    /**
     * Adds the course to the user list of courses.
     *
     * @param userId
     *            The user that the course is being added to.
     * @param courseId
     *            The course that is being added to the user.
     */
    public static void addCourseToUser(final String userId, final String courseId) {
        UserManager.addCourseToUser(getInstance().db, userId, courseId);
    }

    /**
     * Gets all of the course ids for a specific user.
     *
     * @param userId
     *            The user that the courses are being grabbed for.
     * @return A list that contains all of the Id's for a specific user.
     * @throws DatabaseAccessException
     *             Thrown if the user does not exist.
     */
    public static ArrayList<String> getUserCourses(final String userId) throws DatabaseAccessException {
        return UserManager.getUserCourses(getInstance().db, userId);
    }

    /**
     * Gets an update for the user given a userId and the time of the last
     * update.
     *
     * @param userId
     *            Gets all updates after a certain time.
     * @param time
     *            The time the last update was given.
     * @return An SrlSchool that contains data about all of the updates.
     * @throws AuthenticationException
     *             Thrown if the user does not have access to any updates.
     * @throws DatabaseAccessException
     *             Thrown if no dates exist.
     */
    public static SrlSchool mongoGetReleventUpdates(final String userId, final long time) throws AuthenticationException, DatabaseAccessException {
        return UserUpdateHandler.mongoGetAllRelevantUpdates(getInstance().db, userId, time);
    }
}
