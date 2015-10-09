package coursesketch.database.auth;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static database.DbSchoolUtility.getCollectionFromType;
import static database.DbSchoolUtility.getParentItemType;

/**
 * Created by dtracers on 10/7/2015.
 */
public class DbAuthManager {

    /**
     * The database that the auth checker grabs data from.
     */
    private final DB database;

    public DbAuthManager(final DB database) {
        this.database = database;
    }

    public void insertNewItem(final String authId, final String itemId, final School.ItemType itemType,
            final String parentId, final DbAuthChecker authChecker)
            throws DatabaseAccessException, AuthenticationException {
        final School.ItemType parentType = getParentItemType(itemType);
        if (!parentType.equals(itemType)) {
            final Authentication.AuthResponse response = authChecker.isAuthenticated(getParentItemType(itemType), parentId, authId,
                    Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
            final AuthenticationResponder responder = new AuthenticationResponder(response);
            if (!responder.hasModeratorPermission()) {
                throw new AuthenticationException("User does not have permission to insert new items for id: " + parentId,
                        AuthenticationException.INVALID_PERMISSION);
            }
        }

        final BasicDBObject insertQuery = createInsertQuery(itemId, itemType, authId);
        if (!parentType.equals(itemType)) {
            copyParentDetails(insertQuery, itemId, itemType, parentId);
        }

        // if it is a course
        if (itemType.equals(School.ItemType.COURSE)) {
            final String groupId = createNewGroup(itemId, itemType, authId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);
        }
        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        collection.insert(insertQuery);

    }

    /**
     * Creates a new group in the database.
     *
     * @param courseId The course that the group belongs to
     * @param itemType
     * @param authId
     * @return
     * @throws AuthenticationException
     */
    public String createNewGroup(final String courseId, final School.ItemType itemType, final String authId) throws AuthenticationException {
        String hash;
        String salt;
        try {
            salt = HashManager.generateSalt();
            hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException(e);
        }
        final BasicDBObject groupQuery = new BasicDBObject(DatabaseStringConstants.COURSE_ID, new ObjectId(courseId))
                .append(DatabaseStringConstants.SALT, salt)
                .append(hash, Authentication.AuthResponse.PermissionLevel.TEACHER.getNumber());

        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        collection.insert(groupQuery);
        return groupQuery.get(DatabaseStringConstants.SELF_ID).toString();
    }

    private void copyParentDetails(final BasicDBObject insertQuery, final String itemId, final School.ItemType itemType, final String parentId)
            throws DatabaseAccessException {
        final School.ItemType collectionType = getParentItemType(itemType);
        final DBCollection collection = database.getCollection(getCollectionFromType(collectionType));
        final DBObject result = collection.findOne(new ObjectId(itemId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true)
                        .append(DatabaseStringConstants.COURSE_ID, true)
                        .append(DatabaseStringConstants.OWNER_ID, true));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }
        insertQuery.putAll(result);
    }

    private BasicDBObject createInsertQuery(final String itemId, final School.ItemType itemType, final String authId) {
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId));
        if (School.ItemType.COURSE.equals(itemType)) {
            query.append(DatabaseStringConstants.COURSE_ID, itemId)
                    .append(DatabaseStringConstants.OWNER_ID, authId);
        }
        if (School.ItemType.BANK_PROBLEM.equals(itemType)) {
            query.append(DatabaseStringConstants.PROBLEM_BANK_ID, itemId)
                    .append(DatabaseStringConstants.OWNER_ID, authId);
        }
        return query;
    }
}
