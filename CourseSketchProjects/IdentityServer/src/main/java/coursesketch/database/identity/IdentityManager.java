package coursesketch.database.identity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static database.DbSchoolUtility.getCollectionFromType;
import static database.DbSchoolUtility.getParentItemType;

/**
 * Created by dtracers on 10/7/2015.
 */
public final class IdentityManager extends AbstractCourseSketchDatabaseReader implements IdentityManagerInterface {

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
     *
     * @param database
     *         The database where all of the data is stored.
     */
    public IdentityManager(final DB database) {
        super(null);
        this.database = database;
    }

    /**
     * Creates An Identity Manager with a server information.
     *
     * @param serverInfo
     *         The information about the database location.
     */
    public IdentityManager(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * Sets up any indexes that need to be set up or have not yet been set up.
     */
    @Override protected void setUpIndexes() {
        final DBCollection collection = this.database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        collection.createIndex(new BasicDBObject(DatabaseStringConstants.USER_NAME, 1));
    }

    /**
     * {@inheritDoc}
     *
     * @throws DatabaseAccessException
     *         thrown if the database already exist when the database is created.
     */
    @Override protected void onStartDatabase() throws DatabaseAccessException {
        if (this.database != null) {
            throw new DatabaseAccessException("Mongo instance already exists!");
        }
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        this.database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
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
        } else if (itemType.equals(School.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(userId, itemId);
            final List<String> groupList = Lists.newArrayList(groupId);
            insertQuery.append(DatabaseStringConstants.USER_LIST, groupList);

            if (!Strings.isNullOrEmpty(parentId)) {
                LOG.warn("Inserting bank problem {} with no parent id", itemId);
                insertUserIntoGroup(parentId, groupId, true);
            }
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));
        collection.insert(insertQuery);
    }

    /**
     * Creates a basic query for inserting items into the database.
     *
     * @param userId
     *         The userId of the user that is inserting the new item.
     * @param itemId
     *         The id of the item being inserted
     * @param itemType
     *         The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @return {@link BasicDBObject} that contains the basic set up that every item has for its creation.
     */
    private BasicDBObject createItemInsertQuery(final String userId, final String itemId, final School.ItemType itemType) {
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId));
        if (School.ItemType.COURSE.equals(itemType)) {
            query.append(DatabaseStringConstants.COURSE_ID, new ObjectId(itemId))
                    .append(DatabaseStringConstants.OWNER_ID, userId);
        } else if (School.ItemType.BANK_PROBLEM.equals(itemType)) {
            query.append(DatabaseStringConstants.PROBLEM_BANK_ID, new ObjectId(itemId))
                    .append(DatabaseStringConstants.OWNER_ID, userId);
        }
        return query;
    }

    /**
     * Copies the details of the parent item (mainly groups and CourseId) into the current item.
     *
     * @param insertQuery
     *         An existing query for an item that is going to be inserted into the database.
     * @param itemId
     *         The id of the item being inserted
     * @param itemType
     *         The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @param parentId
     *         The id of the parent object EX: parent points to course if item is an Assignment.
     *         If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *         problem
     * @throws DatabaseAccessException
     *         Thrown if the parent object can not be found.
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
     * @param userId
     *         The id of the owner of the new group.
     * @param courseId
     *         The course that the group belongs to.
     * @return A {@link String} that is the mongo id of the new group.
     * @throws AuthenticationException
     *         Thrown if there are problems creating the hash data.
     */
    public String createNewGroup(final String userId, final String courseId) throws AuthenticationException {
        String hash;
        try {
            final String unsecuredSalt = HashManager.generateUnSecureSalt(courseId);
            hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Invalid algorithm when creating a new group", e);
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
     * @param userId
     *         The authentication Id of the user that is being added.
     * @param groupId
     *         The group this specific user is being added to.
     * @param isUser
     *         The id of the course the user is being added to.
     * @throws AuthenticationException
     *         Thrown if a valid hash can not be created for this user.
     * @throws DatabaseAccessException
     *         Thrown if the group can not be found.
     */
    private void insertUserIntoGroup(final String userId, final String groupId, final boolean isUser)
            throws AuthenticationException, DatabaseAccessException {
        if (Strings.isNullOrEmpty(userId)) {
            throw new DatabaseAccessException("Illegal argument when inserting user into group, userId can not be empty",
                    new IllegalArgumentException("UserId can not be null or empty"));
        }

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
            throw new AuthenticationException("Invalid algorithm when inserting a user into group", e);
        }

        if (hash == null) {
            throw new AuthenticationException("Unable to create authentication hash for group " + groupId, AuthenticationException.OTHER);
        }

        // final BasicDBObject newIdentity = new BasicDBObject(userId, hash);
        final String list = isUser ? DatabaseStringConstants.USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).update(
                group,
                new BasicDBObject(DatabaseStringConstants.SET_COMMAND,
                        new BasicDBObject(list + DatabaseStringConstants.SUBFIELD_COMMAND + userId, hash)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerUserInItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
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
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> createNewUser(final String userName) throws AuthenticationException, DatabaseAccessException {
        final ObjectId userId = new ObjectId();
        final String userPassword = AbstractServerWebSocketHandler.Encoder.nextID().toString();
        String hashPassword;
        try {
            hashPassword = HashManager.createHash(userPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Invalid algorithm when creating a new user", e);
        }
        final DBCollection userCollection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.USER_NAME, userName);
        final DBObject cursor = userCollection.findOne(query);
        if (cursor == null) {
            userCollection.insert(new BasicDBObject(DatabaseStringConstants.SELF_ID, userId)
                    .append(DatabaseStringConstants.USER_NAME, userName)
                    .append(DatabaseStringConstants.PASSWORD, hashPassword));
        } else {
            throw new DatabaseAccessException("User [" + userName + "] already exists in the database");
        }

        final Map<String, String> result = new HashMap<>();
        result.put(userId.toString(), userPassword);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getItemRoster(final String authId, final String itemId, final School.ItemType itemType,
            final Collection<String> userIdsList, final Authenticator authChecker)
            throws AuthenticationException, DatabaseAccessException {
        final AuthenticationResponder responder = authChecker.checkAuthentication(itemType, itemId, authId, 0,
                Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
        if (!responder.hasPeerTeacherPermission()) {
            throw new AuthenticationException("Need to be at least a peer teacher", AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection collection = database.getCollection(getCollectionFromType(itemType));

        final DBObject item = collection.findOne(new ObjectId(itemId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true));

        if (item == null) {
            throw new DatabaseAccessException(getCollectionFromType(itemType) + " can not be found with the given itemId: " + itemId);
        }

        final Map<String, String> courseRoster = new HashMap<>();

        final List<String> groupList = (List<String>) item.get(DatabaseStringConstants.USER_LIST);

        final DBCollection groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

        for (String groupId : groupList) {
            courseRoster.putAll(getGroupRoster(groupCollection, groupId));
        }

        final Map<String, String> userIdToUserNames = new HashMap<>();

        if (!responder.hasModeratorPermission()) {
            for (String hashedId : courseRoster.values()) {
                userIdToUserNames.put(hashedId, null);
            }
            return userIdToUserNames;
        }

        final Set<String> userIds = courseRoster.keySet();
        if (userIdsList != null && !userIdsList.isEmpty()) {
            // This does modify the courseRoster map also removing elements from that are not in the set
            // This is okay because we do not need the extra values in it anyways.
            userIds.retainAll(userIdsList);
        }

        final Map<String, String> unHashedUserIdsToUserNames = getUserNames(userIds);

        for (Map.Entry<String, String> userId : unHashedUserIdsToUserNames.entrySet()) {
            userIdToUserNames.put(courseRoster.get(userId.getKey()), userId.getValue());
        }
        return userIdToUserNames;
    }

    /**
     * Gets the roster for the specific group.
     *
     * @param collection
     *         The group collection.
     * @param groupId
     *         The id where the group is located
     * @return A {@code Map<String, String>} which is a {@code Map<UnhashedUserId, HashedUserId>}.
     */
    private Map<String, String> getGroupRoster(final DBCollection collection, final String groupId) {
        final DBObject group = collection.findOne(new ObjectId(groupId),
                new BasicDBObject(DatabaseStringConstants.USER_LIST, true));

        return (Map<String, String>) group.get(DatabaseStringConstants.USER_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * @param identity
     *         A list of userIds
     * @return A map representing the userId to userName
     * @throws DatabaseAccessException
     *         Thrown if no users are found.
     */
    private Map<String, String> getUserNames(final String... identity) throws DatabaseAccessException {
        return getUserNames(Arrays.asList(identity));
    }

    /**
     * Gets the user names given the identity.
     *
     * @param identity
     *         A list of userIds
     * @return A map representing the userId to userName
     * @throws DatabaseAccessException
     *         Thrown if no users are found.
     */
    @SuppressWarnings("unused")
    private Map<String, String> getUserNames(final Collection<String> identity) throws DatabaseAccessException {
        final List<ObjectId> identityList = new ArrayList<>();
        for (String userId : identity) {
            identityList.add(new ObjectId(userId));
        }
        final DBCollection collection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final DBCursor cursor = collection.find(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new BasicDBObject(DatabaseStringConstants.IN_COMMAND, identityList)),
                new BasicDBObject(DatabaseStringConstants.SELF_ID, 1).append(DatabaseStringConstants.USER_NAME, 1));

        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("No users were found with the given userIds");
        }

        final Map<String, String> userNameMap = new HashMap<>();
        for (DBObject userName: cursor) {
            userNameMap.put(userName.get(DatabaseStringConstants.SELF_ID).toString(),
                    userName.get(DatabaseStringConstants.USER_NAME).toString());
        }

        if (identity.size() != userNameMap.size() && LOG.isWarnEnabled()) {
            for (String userId : identity) {
                if (!userNameMap.containsKey(userId)) {
                    LOG.warn("User id {} not found in the database", userId);
                }
            }
        }
        return userNameMap;
    }

    /**
     * Returns true if the user is in the item.
     *
     * @param userId
     *         The id that is being checked
     * @param isUser
     *         True if checking the user list instead of the non user list
     * @param itemId
     *         The item that the user is being checked in
     * @param itemType
     *         The type of item that is being checked
     * @return Returns true if the user is in the group.
     * @throws DatabaseAccessException
     *         Thrown if the group does not exist.
     */
    boolean isUserInItem(final String userId, final boolean isUser, final String itemId, final School.ItemType itemType)
            throws DatabaseAccessException {
        final DBCollection collection = this.database.getCollection(getCollectionFromType(itemType));
        final DBObject result = collection.findOne(new ObjectId(itemId));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final List<String> groupList = (List<String>) result.get(DatabaseStringConstants.USER_LIST);

        final DBCollection groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

        for (String groupId : groupList) {
            if (isUserInGroup(groupCollection, groupId, userId, isUser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the user is in the group.
     *
     * @param collection
     *         The collection that contains the group.
     * @param groupId
     *         The id of the group that is being checked.
     * @param userId
     *         The id that is being checked
     * @param isUser
     *         True if checking the user list instead of the non user list
     * @return Returns true if the user is in the group.
     * @throws DatabaseAccessException
     *         Thrown if the group does not exist.
     */
    private boolean isUserInGroup(final DBCollection collection, final String groupId,
            final String userId, final boolean isUser) throws DatabaseAccessException {
        final String listType = isUser ? DatabaseStringConstants.USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        final DBObject group = collection.findOne(new ObjectId(groupId), new BasicDBObject(listType, 1));
        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        final DBObject list = (DBObject) group.get(listType);
        return list.containsField(userId);
    }
}
