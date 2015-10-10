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

    /**
     *
     * @param authId
     * @param itemId
     * @param itemType
     * @param parentId The id of the parent object EX: parent points to course if item is an Assignment.
     * @param registrationKey
     * @param authChecker
     * @throws DatabaseAccessException
     * @throws AuthenticationException
     */
    public void insertNewItem(final String authId, final String itemId, final School.ItemType itemType,
            final String parentId, final String registrationKey, final DbAuthChecker authChecker)
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

        final BasicDBObject insertQuery = createItemInsertQuery(itemId, itemType, authId, registrationKey);
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

        if (itemType.equals(School.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(itemId, itemType, authId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);

            if (parentId != null) {
                insertUserIntoGroup(parentId, groupId, Authentication.AuthResponse.PermissionLevel.STUDENT);
            }
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        collection.insert(insertQuery);

    }

    /**
     * Creates a basic query for inserting items into the database.
     * @param itemId
     * @param itemType
     * @param authId
     * @param registrationKey
     * @return
     */
    private BasicDBObject createItemInsertQuery(final String itemId, final School.ItemType itemType, final String authId,
            final String registrationKey) {
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId));
        if (School.ItemType.COURSE.equals(itemType)) {
            query.append(DatabaseStringConstants.COURSE_ID, new ObjectId(itemId))
                    .append(DatabaseStringConstants.OWNER_ID, authId);
        }
        if (School.ItemType.BANK_PROBLEM.equals(itemType)) {
            query.append(DatabaseStringConstants.PROBLEM_BANK_ID, itemId)
                    .append(DatabaseStringConstants.OWNER_ID, authId);
        }
        if (registrationKey != null) {
            query.append(DatabaseStringConstants.REGISTRATION_KEY, registrationKey);
        }
        return query;
    }

    /**
     * Copies the details of the parent item (mainly groups and CourseId) into the current item.
     * @param insertQuery
     * @param itemId
     * @param itemType
     * @param parentId
     * @throws DatabaseAccessException
     */
    private void copyParentDetails(final BasicDBObject insertQuery, final String itemId, final School.ItemType itemType, final String parentId)
            throws DatabaseAccessException {
        final School.ItemType collectionType = getParentItemType(itemType);
        final DBCollection collection = database.getCollection(getCollectionFromType(collectionType));
        final DBObject result = collection.findOne(new ObjectId(parentId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true)
                        .append(DatabaseStringConstants.COURSE_ID, true)
                        .append(DatabaseStringConstants.OWNER_ID, true));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        // This would overwrite existing id but there is already a valid id in here.
        result.removeField(DatabaseStringConstants.SELF_ID);
        insertQuery.putAll(result);
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
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
        final BasicDBObject groupQuery = new BasicDBObject(DatabaseStringConstants.COURSE_ID, new ObjectId(courseId))
                .append(DatabaseStringConstants.SALT, salt)
                .append(hash, Authentication.AuthResponse.PermissionLevel.TEACHER.getNumber());

        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        collection.insert(groupQuery);
        return groupQuery.get(DatabaseStringConstants.SELF_ID).toString();
    }

    /**
     * Allows the insertion of a user into a group with the designaed permission.
     * @param authId
     * @param groupId
     * @param permissionLevel
     * @throws AuthenticationException
     */
    private void insertUserIntoGroup(final String authId, final String groupId, final Authentication.AuthResponse.PermissionLevel permissionLevel)
            throws AuthenticationException {
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        final DBObject group = collection.findOne(new ObjectId(groupId));
        final String salt = group.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }

        if (hash == null) {
            throw new AuthenticationException("Unable to create authentication hash for group " + groupId, AuthenticationException.OTHER);
        }

        final BasicDBObject update = new BasicDBObject(hash, permissionLevel.getNumber());
        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).update(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(groupId)),
                new BasicDBObject(DatabaseStringConstants.SET_COMMAND, update));
    }

    public void registerSelf(final String authId, final String itemId, final School.ItemType itemType, final String registrationKey,
            final DbAuthChecker authChecker) throws AuthenticationException, DatabaseAccessException {
        if (!School.ItemType.COURSE.equals(itemType)) {
            throw new AuthenticationException("Can only register users in a course (not a bank problem)", AuthenticationException.OTHER);
        }
    }
}
