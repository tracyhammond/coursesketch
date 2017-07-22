package coursesketch.database.util.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.util.DatabaseAccessException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School.SrlUser;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.COURSE_LIST;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.USER_COLLECTION;


/**
 * Manages different user infomation.
 *
 * @author gigemjt
 */
public final class UserManager {

    /**
     * Logger declaration/definition.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);

    /**
     * Private constructor.
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
    public static List<String> getUserCourses(final MongoDatabase dbs, final String userId) throws DatabaseAccessException {
        final MongoCollection<Document> users = dbs.getCollection(USER_COLLECTION);
        final Document query = new Document(SELF_ID, userId);
        final Document cursor = users.find(query).first();
        if (cursor == null) {
            throw new DatabaseAccessException("Can not find a user with that id", false);
        }
        return (List<String>) cursor.get(COURSE_LIST);
    }

    /**
     * Creates a new user in the database.
     *
     * @param dbs The database where the user is being created.
     * @param user Information about the user.
     * @param userId The user id that is associated with the user.
     */
    public static void createUser(final MongoDatabase dbs, final SrlUser user, final String userId) {
        final MongoCollection<Document> users = dbs.getCollection(USER_COLLECTION);
        LOG.debug("userId: {}", userId);
        LOG.debug(user.getEmail());
        final Document query = new Document(SELF_ID, userId).append(COURSE_LIST, new ArrayList<String>());
        users.insertOne(query);
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
    public static void createUserData(final MongoDatabase dbs, final String userName, final String userId,
            final String email, final String userData) {
        final MongoCollection<Document> users = dbs.getCollection(USER_COLLECTION);
        final Document query = new Document(COURSE_LIST, new ArrayList<String>());
        users.insertOne(query);
    }

    /**
     * After this method is called a user now has a course added to their account.
     *
     * @param database The database where the user exist.
     * @param userId The id of the user.
     * @param courseId The id of the course that is being added.
     */
    static void addCourseToUser(final MongoDatabase database, final String userId, final String courseId) {
        LOG.debug("The users Id {}", userId);
        final MongoCollection<Document> users = database.getCollection(USER_COLLECTION);

        final Document cursor = users.find(new Document(SELF_ID, userId)).first();

        if (cursor != null) {
            final Document query = new Document("$addToSet", new Document(COURSE_LIST, courseId));
            LOG.info("query {}", query);
            LOG.info("courseId {}", courseId);
            users.updateOne(cursor, query);
        } else {
            // FUTURE: add a counter so it does not loop for infinity
            createUser(database, SrlUser.getDefaultInstance(), userId);
            addCourseToUser(database, userId, courseId);
        }
    }
}
