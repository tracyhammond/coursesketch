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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static database.DbSchoolUtility.getCollectionFromType;
import static database.DbSchoolUtility.getParentItemType;

/**
 * Created by dtracers on 10/7/2015.
 */
public final class DbAuthManager {

    /**
     * The database that the auth checker grabs data from.
     */
    private final DB database;

    /**
     * Creates A DbAuthManager with a database.
     * @param database The database where all of the data is stored.
     */
    public DbAuthManager(final DB database) {
        this.database = database;
    }

    /**
     * Inserts a new item into the database.
     *
     * @param authId The AuthId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted.
     * @param itemType The type of item that is being inserted. EX: {@link protobuf.srl.school.School.ItemType#COURSE}
     * @param parentId The id of the parent object. EX: parent points to the course if item is an Assignment.
     *                 If the {@code itemType} is a bank problem, then this value can be a course that automatically gets permission to view the bank
     *                 problem.
     * @param registrationKey This key is needed for a user to grant themself access permission to a course.
     * @param authChecker Used to check if the user has permission to insert the item.
     * @throws DatabaseAccessException Thrown if the user does not have the correct permissions to insert the item.
     * @throws AuthenticationException Thrown if there is data that can not be found in the database.
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

        final BasicDBObject insertQuery = createItemInsertQuery(authId, itemId, itemType, registrationKey);
        if (!parentType.equals(itemType)) {
            copyParentDetails(insertQuery, itemId, itemType, parentId);
        }

        // if it is a course
        if (itemType.equals(School.ItemType.COURSE)) {
            final String groupId = createNewGroup(authId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);
        }

        if (itemType.equals(School.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(authId, itemId);
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
     *
     * @param authId The AuthId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted
     * @param itemType The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @param registrationKey The key is needed to allow users to added themselves having permissions to access the course.
     * @return {@link BasicDBObject} that contains the basic set up that every item has for its creation.
     */
    private BasicDBObject createItemInsertQuery(final String authId, final String itemId, final School.ItemType itemType,
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
     *
     * @param insertQuery An existing query for an item that is going to be inserted into the database.
     * @param itemId The id of the item being inserted
     * @param itemType The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @param parentId The id of the parent object EX: parent points to course if item is an Assignment.
     *                 If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *                 problem
     * @throws DatabaseAccessException Thrown if the parent object can not be found.
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
     * @param authId The id of the owner of the new group.
     * @param courseId The course that the group belongs to.
     * @return A {@link String} that is the mongo id of the new group.
     * @throws AuthenticationException Thrown if there are problems creating the hash data.
     */
    public String createNewGroup(final String authId, final String courseId) throws AuthenticationException {
        String hash;
        String salt;
        try {
            salt = HashManager.generateSalt();
            hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes(StandardCharsets.UTF_8));
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
     * Allows the insertion of a user into a group with the designated permission.
     *
     * @param authId The authentication Id of the user that is being added.
     * @param groupId The group this specific user is being added to.
     * @param permissionLevel The level of permissions this user will have.
     * @throws AuthenticationException Thrown if a valid hash can not be created for this user.
     * @throws DatabaseAccessException Thrown if the group can not be found.
     */
    private void insertUserIntoGroup(final String authId, final String groupId, final Authentication.AuthResponse.PermissionLevel permissionLevel)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        final DBObject group = collection.findOne(new ObjectId(groupId),
                new BasicDBObject(DatabaseStringConstants.SALT, true));
        if (group == null) {
            throw new DatabaseAccessException("group could not be found");
        }
        final String salt = group.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }

        if (hash == null) {
            throw new AuthenticationException("Unable to create authentication hash for group " + groupId, AuthenticationException.OTHER);
        }

        final BasicDBObject update = new BasicDBObject(hash, permissionLevel.getNumber());
        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).update(
                group,
                new BasicDBObject(DatabaseStringConstants.SET_COMMAND, update));
    }

    /**
     * Registers a student with a course.
     *
     * The student must have a valid registration key.
     * @param authId The authentication Id of the user that is being added.
     * @param itemId The Id of the course or bank problem the user is being added to.
     * @param itemType The type of item the user is registering for (Only {@link protobuf.srl.school.School.ItemType#COURSE}
     *                 and (Only {@link protobuf.srl.school.School.ItemType#BANK_PROBLEM} are valid types.
     * @param registrationKey The key that is used to register to the course.
     * @param authChecker Used to check permissions in the database.
     * @throws AuthenticationException If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException Thrown if the item can not be found.
     */
    public void registerSelf(final String authId, final String itemId, final School.ItemType itemType, final String registrationKey,
            final DbAuthChecker authChecker) throws AuthenticationException, DatabaseAccessException {
        if (!School.ItemType.COURSE.equals(itemType) && !School.ItemType.BANK_PROBLEM.equals(itemType)) {
            throw new AuthenticationException("Can only register users in a course or a bank problem", AuthenticationException.OTHER);
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        final DBObject result = collection.findOne(new ObjectId(itemId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true)
                        .append(DatabaseStringConstants.REGISTRATION_KEY, true));

        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }
        if (!result.get(DatabaseStringConstants.REGISTRATION_KEY).equals(registrationKey)) {
            throw new AuthenticationException("Invalid Registration key", AuthenticationException.INVALID_PERMISSION);
        }
        final List<String> userGroups = (List<String>) result.get(DatabaseStringConstants.USER_LIST);
        insertUserIntoGroup(authId, userGroups.get(0), Authentication.AuthResponse.PermissionLevel.STUDENT);
    }
}
