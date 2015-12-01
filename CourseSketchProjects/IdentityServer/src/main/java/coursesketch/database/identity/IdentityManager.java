package coursesketch.database.identity;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.authentication.HashManager;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DbSchoolUtility.getCollectionFromType;
import static database.DbSchoolUtility.getParentItemType;

/**
 * Created by dtracers on 10/7/2015.
 */
public final class IdentityManager extends AbstractCourseSketchDatabaseReader {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityManager.class);

    /**
     * The database that the auth checker grabs data from.
     */
    private DB database;

    /**
     * Creates An IdentityManager with a database.
     * @param database The database where all of the data is stored.
     */
    public IdentityManager(final DB database) {
        super(null);
        this.database = database;
    }

    /**
     * Creates An Identity Manager with a server information.
     * @param serverInfo The information about the database location.
     */
    public IdentityManager(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DatabaseAccessException thrown if the database already exist when the database is created.
     */
    @Override protected void onStartDatabase() throws DatabaseAccessException {
        if (this.database != null) {
            throw new DatabaseAccessException("Mongo instance already exists!");
        }
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        this.database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        final DBCollection collection = this.database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        collection.createIndex(new BasicDBObject(DatabaseStringConstants.USER_NAME, 1));
        super.setDatabaseStarted();
    }

    /**
     * Inserts a new item into the database.
     *
     * @param userId The user id of the user that is inserting the new item.
     * @param authId The AuthId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted
     * @param itemType The type of item that is being inserted, EX: {@link protobuf.srl.school.School.ItemType#COURSE}
     * @param parentId The id of the parent object EX: parent points to course if item is an Assignment.
     *                 If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *                 problem
     * @param authChecker Used to check that the user has access to perform the requested actions.
     * @throws DatabaseAccessException Thrown if the user does not have the correct permissions to perform the request actions.
     * @throws AuthenticationException Thrown if there is data that can not be found in the database.
     */
    public void insertNewItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final String parentId, final Authenticator authChecker)
            throws DatabaseAccessException, AuthenticationException {
        final School.ItemType parentType = getParentItemType(itemType);
        if (!parentType.equals(itemType)) {
            final AuthenticationResponder responder = authChecker.checkAuthentication(getParentItemType(itemType), parentId, authId, 0,
                    Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
            if (!responder.hasModeratorPermission()) {
                throw new AuthenticationException("User does not have permission to insert new items for id: " + parentId,
                        AuthenticationException.INVALID_PERMISSION);
            }
        }

        final BasicDBObject insertQuery = createItemInsertQuery(userId, itemId, itemType);
        if (!parentType.equals(itemType)) {
            copyParentDetails(insertQuery, itemId, itemType, parentId);
        }

        // if it is a course
        if (itemType.equals(School.ItemType.COURSE)) {
            final String groupId = createNewGroup(userId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);
        }

        if (itemType.equals(School.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(userId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);

            if (parentId != null) {
                insertUserIntoGroup(parentId, groupId, true);
            }
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        collection.insert(insertQuery);
    }

    /**
     * Creates a basic query for inserting items into the database.
     *
     * @param userId The userId of the user that is inserting the new item.
     * @param itemId The id of the item being inserted
     * @param itemType The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @return {@link BasicDBObject} that contains the basic set up that every item has for its creation.
     */
    private BasicDBObject createItemInsertQuery(final String userId, final String itemId, final School.ItemType itemType) {
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId));
        if (School.ItemType.COURSE.equals(itemType)) {
            query.append(DatabaseStringConstants.COURSE_ID, new ObjectId(itemId))
                    .append(DatabaseStringConstants.OWNER_ID, userId);
        }
        if (School.ItemType.BANK_PROBLEM.equals(itemType)) {
            query.append(DatabaseStringConstants.PROBLEM_BANK_ID, itemId)
                    .append(DatabaseStringConstants.OWNER_ID, userId);
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
     * @param userId The id of the owner of the new group.
     * @param courseId The course that the group belongs to.
     * @return A {@link String} that is the mongo id of the new group.
     * @throws AuthenticationException Thrown if there are problems creating the hash data.
     */
    public String createNewGroup(final String userId, final String courseId) throws AuthenticationException {
        String hash;
        try {
            final String unsecuredSalt = HashManager.generateUnSecureSalt(courseId);
            hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
        final BasicDBObject nonStudent = new BasicDBObject();
        nonStudent.append(userId, hash);
        final BasicDBObject groupQuery = new BasicDBObject(DatabaseStringConstants.COURSE_ID, new ObjectId(courseId))
                .append(DatabaseStringConstants.USER_LIST, new BasicDBObject())
                .append(DatabaseStringConstants.NON_USER_LIST, nonStudent);

        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        collection.insert(groupQuery);
        return groupQuery.get(DatabaseStringConstants.SELF_ID).toString();
    }

    /**
     * Allows the insertion of a user into a group with the designated permission.
     *
     * @param userId The authentication Id of the user that is being added.
     * @param groupId The group this specific user is being added to.
     * @param isUser The id of the course the user is being added to.
     * @throws AuthenticationException Thrown if a valid hash can not be created for this user.
     * @throws DatabaseAccessException Thrown if the group can not be found.
     */
    private void insertUserIntoGroup(final String userId, final String groupId, final boolean isUser)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        final DBObject group = collection.findOne(new ObjectId(groupId),
                new BasicDBObject(DatabaseStringConstants.SELF_ID, true)
                        .append(DatabaseStringConstants.COURSE_ID, true));
        if (group == null) {
            throw new DatabaseAccessException("group could not be found");
        }
        String hash = null;
        try {
            final String unsecuredSalt = HashManager.generateUnSecureSalt(group.get(DatabaseStringConstants.COURSE_ID).toString());
            hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                    .getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }

        if (hash == null) {
            throw new AuthenticationException("Unable to create authentication hash for group " + groupId, AuthenticationException.OTHER);
        }

        // final BasicDBObject newIdentity = new BasicDBObject(userId, hash);
        final String list = isUser ? DatabaseStringConstants.USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).update(
                group,
                new BasicDBObject(DatabaseStringConstants.SET_COMMAND, new BasicDBObject(list + "." + userId, hash)));
    }

    /**
     * Registers a student with a course.
     *
     * The student must have a valid registration key.
     * @param userId The user Id of the user that is being added.
     * @param authId The authentication Id of the user that is being added.
     * @param itemId The Id of the course or bank problem the user is being added to.
     * @param itemType The type of item the user is registering for (Only {@link protobuf.srl.school.School.ItemType#COURSE}
     *                 and (Only {@link protobuf.srl.school.School.ItemType#BANK_PROBLEM} are valid types.
     * @param authChecker Used to check permissions in the database.
     * @throws AuthenticationException If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException Thrown if the item can not be found.
     */
    public void registerSelf(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        if (!School.ItemType.COURSE.equals(itemType) && !School.ItemType.BANK_PROBLEM.equals(itemType)) {
            throw new AuthenticationException("Can only register users in a course or a bank problem", AuthenticationException.OTHER);
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        final DBObject result = collection.findOne(new ObjectId(itemId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true));

        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final AuthenticationResponder responder = authChecker.checkAuthentication(itemType, itemId, authId, 0,
                Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
        if (!responder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permission to insert new items for id: " + itemId,
                    AuthenticationException.INVALID_PERMISSION);
        }

        final List<String> userGroups = (List<String>) result.get(DatabaseStringConstants.USER_LIST);
        insertUserIntoGroup(userId, userGroups.get(0), !responder.hasPeerTeacherPermission());
    }

    /**
     * Creates a new user in the identity server.
     * @param userName The username that is being added to the database.
     * @return A map that has the userId as the key and the password to access the userId as the value.
     * @throws AuthenticationException thrown if there is a problem creating the user hash.
     */
    public Map<String, String> createNewUser(final String userName) throws AuthenticationException {
        final ObjectId userId = new ObjectId();
        final String userPassword = AbstractServerWebSocketHandler.Encoder.nextID().toString();
        String hashPassword;
        try {
            hashPassword = HashManager.createHash(userPassword);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Algorithm could not be found", e);
            throw new AuthenticationException(e);
        }
        database.getCollection(DatabaseStringConstants.USER_COLLECTION)
                .insert(new BasicDBObject(DatabaseStringConstants.SELF_ID, userId)
                        .append(DatabaseStringConstants.USER_NAME, userName)
                        .append(DatabaseStringConstants.PASSWORD, hashPassword));

        final Map<String, String> result = new HashMap<>();
        result.put(userId.toString(), userPassword);
        return result;
    }

    /**
     * Gets the user identity.
     *
     * @param userName The username that is associated with the userId
     * @param authId The password to getting the user identity.
     * @return The userIdentity.
     * @throws AuthenticationException Thrown if the {@code authId} is invalid.
     * @throws DatabaseAccessException Thrown if the user is not found.
     */
    public String getUserIdentity(final String userName, final String authId) throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final DBObject userInfo = collection.findOne(new BasicDBObject(DatabaseStringConstants.USER_NAME, userName));

        if (userInfo == null) {
            throw new DatabaseAccessException("User not found with username [" + userName + "]");
        }
        try {
            if (!HashManager.validateHash(authId, userInfo.get(DatabaseStringConstants.PASSWORD).toString())) {
                throw new AuthenticationException("Auth Identification is invalid", AuthenticationException.INVALID_PERMISSION);
            }
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            LOG.error("Problem validating user hash");
            throw new AuthenticationException(e);
        }
        return userInfo.get(DatabaseStringConstants.SELF_ID).toString();
    }

    public Map<String, String> getUserName(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker)
            throws AuthenticationException, DatabaseAccessException {
        final AuthenticationResponder responder = authChecker.checkAuthentication(itemType, itemId, authId, 0,
                Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("Need to be a mod", AuthenticationException.INVALID_PERMISSION);
        }

        if (!isUserInItem(userId, true, itemId, itemType)) {
            throw new AuthenticationException("User needs to be in the item that the requester is a mod of",
                    AuthenticationException.OTHER);
        }

        return getUserNames(userId);
    }

    /**
     * Gets the user names given the identity.
     *
     * @param identity A list of userIds
     * @return A map representing the userId to userName
     * @throws DatabaseAccessException Thrown if no users are found.
     */
    private Map<String, String> getUserNames(final String... identity) throws DatabaseAccessException {
        final List<String> identityList = Arrays.asList(identity);
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final DBCursor cursor = collection.find(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new BasicDBObject(DatabaseStringConstants.IN_COMMAND, identityList)),
                new BasicDBObject(DatabaseStringConstants.SELF_ID, 1).append(DatabaseStringConstants.USER_NAME, 1));

        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("No users were found with the given userIds");
        }

        final Map<String, String> userNameMap = new HashMap<>();
        while (cursor.hasNext()) {
            final DBObject userName = cursor.next();
            userNameMap.put(userName.get(DatabaseStringConstants.SELF_ID).toString(),
                    userName.get(DatabaseStringConstants.USER_NAME).toString());
        }

        if (identity.length != userNameMap.size() && LOG.isWarnEnabled()) {
            for (String userId : identity) {
                if (!userNameMap.containsKey(userId)) {
                    LOG.warn("User id {} not found in the database", userId);
                }
            }
        }
        return userNameMap;
    }

    private boolean isUserInItem(final String userId, final boolean isUser, final String itemId, final School.ItemType collectionType)
            throws DatabaseAccessException {
        final DBCollection collection = this.database.getCollection(getCollectionFromType(collectionType));
        final DBObject result = collection.findOne(new ObjectId(itemId));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final List<String> groupList = (List<String>) result.get(DatabaseStringConstants.USER_LIST);
        Authentication.AuthResponse.PermissionLevel permissionLevel = null;

        final DBCollection groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

        for (String groupId : groupList) {
            if (isUserInGroup(groupCollection, groupId, userId, isUser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the group permission, and returns a permission level.
     *
     * @param collection The collection that contains the group.
     * @param groupId The id of the group that is being checked.
     * @param userId The id that is being checked
     * @return A permission level that represents what permission the user has.  This does not return null.
     * @throws DatabaseAccessException Thrown if the group does not exist.
     * @throws AuthenticationException Thrown if there are problems comparing the hashes.
     */
    private boolean isUserInGroup(final DBCollection collection, final String groupId,
            final String userId, final boolean isUser) throws DatabaseAccessException {
        final String list = isUser ? DatabaseStringConstants.USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        final DBObject group = collection.findOne(new ObjectId(groupId), new BasicDBObject(list, 1));

        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        return group.containsField(userId);
    }
}
