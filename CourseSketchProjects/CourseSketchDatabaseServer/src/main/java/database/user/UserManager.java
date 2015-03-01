package database.user;

import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.COURSE_LIST;
import static database.DatabaseStringConstants.CREDENTIALS;
import static database.DatabaseStringConstants.EMAIL;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.USER_COLLECTION;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.SrlUser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.PasswordHash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * Manages different user infomation.
 *
 * @author gigemjt
 *
 */
public final class UserManager {
    /**
     * Logger declaration/definition.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);
    /**
     * Private constructor.
     *
     */
    private UserManager() {
    }

    // get grades
    // update grades
    // insert grades
    // get real world identity (need second password)
    // isert real world identity
    // update real world identity (need second password)
    //

    /**
     * Returns the list of courses that a users has registered for.
     *
     * @param userId The user that is requesting for courses.
     * @param dbs where the user id is searched for.
     * @return A list of course id.
     * @throws DatabaseAccessException Thrown if the user id does not exist.
     */
    public static List<String> getUserCourses(final DB dbs, final String userId) throws DatabaseAccessException {
        final DBCollection users = dbs.getCollection(USER_COLLECTION);
        final BasicDBObject query = new BasicDBObject(SELF_ID, userId);
        final DBObject cursor = users.findOne(query);
        if (cursor == null) {
            throw new DatabaseAccessException("Can not find a user with that id", false);
        }
        return (ArrayList) cursor.get(COURSE_LIST);
    }

    /**
     * Creates a new user in the database.
     * @param dbs The database where the user is being created.
     * @param user Information about the user.
     * @param userId The user id that is associated with the user.
     * @throws DatabaseAccessException Thrown if there was an error when creating a password hash of the id.
     */
    public static void createUser(final DB dbs, final SrlUser user, final String userId) throws DatabaseAccessException {
        final DBCollection users = dbs.getCollection(USER_COLLECTION);
        // NOSHIP: userId must be hashed using the userName as a salt?
        BasicDBObject query = null;
        LOG.info("userId: {}", userId);
        try {
            query = new BasicDBObject(SELF_ID, userId).append(COURSE_LIST, new ArrayList<String>())
                    .append(CREDENTIALS, PasswordHash.createHash(user.getEmail())).append(EMAIL, user.getEmail())
                    .append(ADMIN, PasswordHash.createHash(userId));
        } catch (NoSuchAlgorithmException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        } catch (InvalidKeySpecException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
        if (query == null) {
            throw new DatabaseAccessException("An error occured creating password hash");
        }
        users.insert(query);
    }

    /**
     * On first insert we also take in email as credentials. All other ones we
     * take in a password!
     *
     * @param dbs the database where the user exist.
     * @param userName the name that the user gave itself.
     * @param userId The id of the user.
     * @param userData extra user data.
     * @param email the email of the user.
     */
    public static void createUserData(final DB dbs, final String userName, final String userId, final String email, final String userData) {
        final DBCollection users = dbs.getCollection(USER_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_LIST, new ArrayList<String>());
        users.insert(query);
    }

    /**
     * After this method is called a user now has a course added to their account.
     * @param database The database where the user exist.
     * @param userId The id of the user.
     * @param courseId The id of the course that is being added.
     */
    static void addCourseToUser(final DB database, final String userId, final String courseId) {
        LOG.debug("The users Id {}", userId);
        final DBCollection users = database.getCollection(USER_COLLECTION);
        final BasicDBObject query = new BasicDBObject("$addToSet", new BasicDBObject(COURSE_LIST, courseId));
        final DBRef myDbRef = new DBRef(database, USER_COLLECTION, userId);
        final DBObject corsor = myDbRef.fetch();
        LOG.info("query {}", query);
        LOG.info("courseId {}", courseId);
        users.update(corsor, query);
    }

    /*
     * public void registerUserForCourse(DB dbs, String userId, String CourseId)
     * { DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new
     * ObjectId(userId)); DBObject corsor = myDbRef.fetch(); DBObject updateObj
     * = null; DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
     * updateObj = new BasicDBObject(ASSIGNMENT_LIST, assignmentId);
     * courses.update(corsor, new BasicDBObject ("$addToSet",updateObj)); }
     */
}
