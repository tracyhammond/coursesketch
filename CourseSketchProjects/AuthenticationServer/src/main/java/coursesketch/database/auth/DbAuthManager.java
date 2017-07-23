package coursesketch.database.auth;

import com.google.common.base.Strings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.MongoUtilities;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.COURSE_ID;
import static coursesketch.database.util.DatabaseStringConstants.OWNER_ID;
import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.USER_LIST;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.DbSchoolUtility.getParentItemType;
import static coursesketch.database.util.MongoUtilities.getUserGroup;
import static coursesketch.utilities.AuthUtilities.generateAuthSalt;
import static coursesketch.utilities.AuthUtilities.generateHash;
import static coursesketch.utilities.AuthUtilities.largestAllowedLevel;

/**
 * Created by dtracers on 10/7/2015.
 */
public final class DbAuthManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DbAuthManager.class);

    /**
     * The database that the auth checker grabs data from.
     */
    private final MongoDatabase database;

    /**
     * Creates A DbAuthManager with a database.
     * @param database The database where all of the data is stored.
     */
    public DbAuthManager(final MongoDatabase database) {
        this.database = database;
    }

    /**
     * Inserts a new item into the database.
     *
     * @param authId The AuthId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted.
     * @param itemType The type of item that is being inserted. EX: {@link Util.ItemType#COURSE}
     * @param parentId The id of the parent object. EX: parent points to the course if item is an Assignment.
     *                 If the {@code itemType} is a bank problem, then this value can be a course that automatically gets permission to view the bank
     *                 problem.
     * @param registrationKey This key is needed for a user to grant themself access permission to a course.
     * @param authChecker Used to check if the user has permission to insert the item.
     * @throws DatabaseAccessException Thrown if the user does not have the correct permissions to insert the item.
     * @throws AuthenticationException Thrown if there is data that can not be found in the database.
     */
    public void insertNewItem(final String authId, final String itemId, final Util.ItemType itemType,
            final String parentId, final String registrationKey, final DbAuthChecker authChecker)
            throws DatabaseAccessException, AuthenticationException {
        final Util.ItemType parentType = getParentItemType(itemType);
        if (parentType == null) {
            throw new DatabaseAccessException("Invalid type for checking permissions");
        }
        if (!parentType.equals(itemType)) {
            final Authentication.AuthResponse response = authChecker.isAuthenticated(getParentItemType(itemType), parentId, authId,
                    Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
            final AuthenticationResponder responder = new AuthenticationResponder(response);
            if (!responder.hasModeratorPermission()) {
                throw new AuthenticationException("User does not have permission to insert new items for id: " + parentId,
                        AuthenticationException.INVALID_PERMISSION);
            }
        }

        final Document insertQuery = createItemInsertQuery(authId, itemId, itemType, registrationKey);
        if (!parentType.equals(itemType)) {
            copyParentDetails(insertQuery, itemId, itemType, parentId);
        }

        // if it is a course
        if (itemType.equals(Util.ItemType.COURSE)) {
            final String groupId = createNewGroup(authId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);
        }

        if (itemType.equals(Util.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(authId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);

            if (!Strings.isNullOrEmpty(parentId)) {
                LOG.warn("Inserting bank problem {} with no parent id", itemId);
                insertUserIntoGroup(parentId, groupId, Authentication.AuthResponse.PermissionLevel.STUDENT);
            }
        }

        // Collection is created by mongo if it did not exist before.
        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));
        collection.insertOne(insertQuery);

    }

    /**
     * Creates a basic query for inserting items into the database.
     *
     * @param authId The AuthId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted.
     * @param itemType The type of item that is being inserted. EX: {@link Util.ItemType#COURSE}
     * @param registrationKey This key is needed for a user to grant themself access permission to a course.
     * @return {@link Document} that contains the basic set up that every item has for its creation.
     */
    private Document createItemInsertQuery(final String authId, final String itemId, final Util.ItemType itemType,
            final String registrationKey) {
        final Document query = new Document(SELF_ID, new ObjectId(itemId));
        if (Util.ItemType.COURSE.equals(itemType)) {
            query.append(COURSE_ID, new ObjectId(itemId))
                    .append(OWNER_ID, authId);
        }
        if (Util.ItemType.BANK_PROBLEM.equals(itemType)) {
            query.append(DatabaseStringConstants.PROBLEM_BANK_ID, itemId)
                    .append(OWNER_ID, authId);
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
     * @param itemId The id of the item being inserted.
     * @param itemType The type of item that is being inserted. EX: {@link Util.ItemType#COURSE}
     * @param parentId The id of the parent object. EX: parent points to course if item is an Assignment.
     *                 If the {@code itemType} is a bank problem, then this value can be a course that automatically gets permission to view the bank
     *                 problem.
     * @throws DatabaseAccessException Thrown if the parent object can not be found.
     */
    private void copyParentDetails(final Document insertQuery, final String itemId, final Util.ItemType itemType, final String parentId)
            throws DatabaseAccessException {
        final Util.ItemType collectionType = getParentItemType(itemType);
        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(collectionType));
        final Document result = collection.find(MongoUtilities.convertStringToObjectId(parentId))
                .projection(Projections.include(USER_LIST, COURSE_ID, OWNER_ID)).first();
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " was not found in the database.");
        }

        // This prevents the existing insertQuery ObjectId from being overwritten by the result ObjectId.
        result.remove(SELF_ID);
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
    String createNewGroup(final String authId, final String courseId) throws AuthenticationException {
        final String salt = generateAuthSalt();
        final String hash = generateHash(authId, salt);
        final Document groupQuery = new Document(COURSE_ID, new ObjectId(courseId))
                .append(DatabaseStringConstants.SALT, salt)
                .append(hash, Authentication.AuthResponse.PermissionLevel.TEACHER.getNumber());

        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).insertOne(groupQuery);
        return groupQuery.get(SELF_ID).toString();
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

        if (Strings.isNullOrEmpty(authId)) {
            throw new DatabaseAccessException("Illegal argument when inserting user into group, userId can not be empty",
                    new IllegalArgumentException("UserId can not be null or empty"));
        }

        final MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        final Document group = collection.find(MongoUtilities.convertStringToObjectId(groupId))
                .projection(Projections.include(SELF_ID, DatabaseStringConstants.SALT)).first();
        if (group == null) {
            throw new DatabaseAccessException("Group with id " + groupId + " could not be found.");
        }
        final String salt = group.get(DatabaseStringConstants.SALT).toString();
        final String hash = generateHash(authId, salt);

        final Document update = new Document(hash, permissionLevel.getNumber());
        collection.updateOne(MongoUtilities.convertStringToObjectId(group.get(SELF_ID).toString()),
                new Document(DatabaseStringConstants.SET_COMMAND, update));
    }

    /**
     * Self-registers a student for a course via a registration key.
     *
     * The student must have a valid registration key, an instructor does not require a valid registration key in some instances.
     * @param authId The authentication Id of the user that is being added.
     * @param itemId The Id of the course or bank problem the user is being added to.
     * @param itemType The type of item the user is registering for (Only {@link Util.ItemType#COURSE}
     *                 and (Only {@link Util.ItemType#BANK_PROBLEM} are valid types.
     * @param registrationKey The key that is used to register for the course.
     * @throws AuthenticationException If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException Thrown if the item can not be found.
     */
    public void registerSelf(final String authId, final String itemId, final Util.ItemType itemType, final String registrationKey)
            throws AuthenticationException, DatabaseAccessException {
        if (!Util.ItemType.COURSE.equals(itemType) && !Util.ItemType.BANK_PROBLEM.equals(itemType)) {
            throw new AuthenticationException("Can only register users in a course or a bank problem.", AuthenticationException.OTHER);
        }

        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));
        final Document result = collection.find(MongoUtilities.convertStringToObjectId(itemId))
                .projection(Projections.include(USER_LIST, REGISTRATION_KEY)).first();

        if (result == null) {
            throw new DatabaseAccessException("The item with the id: " + itemId + " was not found in the database.");
        }
        if (Strings.isNullOrEmpty(registrationKey)) {
            throw new AuthenticationException("Registration key is required but none is given", AuthenticationException.INVALID_PERMISSION);
        }
        final String databaseRegistrationKey = (String) result.get(DatabaseStringConstants.REGISTRATION_KEY);
        if (!registrationKey.equals(databaseRegistrationKey)) {
            throw new AuthenticationException("Invalid Registration key [" + registrationKey + "]"
                    + " is not equal to stored registration key [" + databaseRegistrationKey + "] ", AuthenticationException.INVALID_PERMISSION);
        }
        final List<String> userGroups = getUserGroup(result);
        insertUserIntoGroup(authId, userGroups.get(0), Authentication.AuthResponse.PermissionLevel.STUDENT);
    }

    /**
     * Adds a user to a group for a certain type
     *
     * The person authorizing the addition must be the owner of the group.
     *
     * @param registrationKey The owner of the group that the user is being added to.
     * @param authId The authentication Id of the user that is being added.
     * @param itemId The Id of the course or bank problem the user is being added to.
     * @param itemType The type of item the user is registering for (Only {@link Util.ItemType#COURSE}
     *                 and (Only {@link Util.ItemType#BANK_PROBLEM} are valid types.
     * @param authChecker Used to check permissions in the database.
     * @param authParams Used to give the level of the user that should be added.
     * @throws AuthenticationException If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException Thrown if the item can not be found.
     */
    public void addUser(final String registrationKey, final String authId, final String itemId, final Util.ItemType itemType,
            final DbAuthChecker authChecker, final Authentication.AuthType authParams) throws DatabaseAccessException, AuthenticationException {

        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));
        final Document result = collection.find(MongoUtilities.convertStringToObjectId(itemId))
                .projection(Projections.include(USER_LIST, REGISTRATION_KEY)).first();

        if (result == null) {
            throw new DatabaseAccessException("The item with the id: " + itemId + " was not found in the database.");
        }
        if (Strings.isNullOrEmpty(registrationKey)) {
            throw new AuthenticationException("Registration key is required but none is given", AuthenticationException.INVALID_PERMISSION);
        }
        final Authentication.AuthResponse authenticated = authChecker.isAuthenticated(itemType, itemId, registrationKey, authParams);

        if (!new AuthenticationResponder(authenticated).isOwner()) {
            throw new AuthenticationException("Only owners can modify user lists", AuthenticationException.INVALID_PERMISSION);
        }

        final List<String> userGroups = getUserGroup(result);
        insertUserIntoGroup(authId, userGroups.get(0), largestAllowedLevel(authParams));
    }
}
