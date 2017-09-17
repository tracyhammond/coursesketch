package coursesketch.database.identity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.server.authentication.HashManager;
import coursesketch.server.interfaces.ServerInfo;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;
import utilities.Encoder;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static coursesketch.database.util.DatabaseStringConstants.COURSE_ID;
import static coursesketch.database.util.DatabaseStringConstants.OWNER_ID;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.USER_LIST;
import static coursesketch.database.util.DatabaseStringConstants.USER_NAME;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.DbSchoolUtility.getParentItemType;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.getUserGroup;

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
    private MongoDatabase database;

    /**
     * Creates An IdentityManager with a database.
     *
     * @param database
     *         The database where all of the data is stored.
     */
    public IdentityManager(final MongoDatabase database) {
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
    @Override
    protected void setUpIndexes() {
        final MongoCollection<Document> collection = this.database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        collection.createIndex(new Document(DatabaseStringConstants.USER_NAME, 1));
    }

    /**
     * {@inheritDoc}
     *
     * @throws DatabaseAccessException
     *         thrown if the database already exist when the database is created.
     */
    @Override
    protected void onStartDatabase() throws DatabaseAccessException {
        if (this.database != null) {
            throw new DatabaseAccessException("Mongo instance already exists!");
        }
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        this.database = mongoClient.getDatabase(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewItem(final String userId, final String authId, final String itemId, final Util.ItemType itemType,
            final String parentId, final Authenticator authChecker)
            throws DatabaseAccessException, AuthenticationException {
        final Util.ItemType parentType = getParentItemType(itemType);
        if (parentType == null) {
            throw new DatabaseAccessException("Invalid parent type");
        }
        if (!parentType.equals(itemType)) {
            final AuthenticationResponder responder = authChecker.checkAuthentication(getParentItemType(itemType), parentId, authId, 0,
                    Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
            if (!responder.hasModeratorPermission()) {
                throw new AuthenticationException("User does not have permission to insert new items for id: " + parentId,
                        AuthenticationException.INVALID_PERMISSION);
            }
        }

        final Document insertQuery = createItemInsertQuery(userId, itemId, itemType);
        if (!parentType.equals(itemType)) {
            copyParentDetails(insertQuery, itemId, itemType, parentId);
        }

        // if it is a course
        if (itemType.equals(Util.ItemType.COURSE)) {
            final String groupId = createNewGroup(userId, itemId);
            final List<String> groupList = new ArrayList<>();
            groupList.add(groupId);
            insertQuery.append(USER_LIST, groupList);
        } else if (itemType.equals(Util.ItemType.BANK_PROBLEM)) {
            final String groupId = createNewGroup(userId, itemId);
            final List<String> groupList = Lists.newArrayList(groupId);
            insertQuery.append(USER_LIST, groupList);

            if (!Strings.isNullOrEmpty(parentId)) {
                LOG.warn("Inserting bank problem {} with no parent id", itemId);
                insertUserIntoGroup(parentId, groupId, true);
            }
        }

        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));
        collection.insertOne(insertQuery);
    }

    /**
     * Creates a basic query for inserting items into the database.
     *
     * @param userId
     *         The userId of the user that is inserting the new item.
     * @param itemId
     *         The id of the item being inserted
     * @param itemType
     *         The type of item that is being inserted, EX: {@link Util.ItemType#COURSE}
     * @return {@link Document} that contains the basic set up that every item has for its creation.
     */
    private Document createItemInsertQuery(final String userId, final String itemId, final Util.ItemType itemType) {
        final Document query = new Document(DatabaseStringConstants.SELF_ID, new ObjectId(itemId));
        if (Util.ItemType.COURSE.equals(itemType)) {
            query.append(DatabaseStringConstants.COURSE_ID, new ObjectId(itemId))
                    .append(DatabaseStringConstants.OWNER_ID, userId);
        } else if (Util.ItemType.BANK_PROBLEM.equals(itemType)) {
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
     *         The type of item that is being inserted, EX: {@link Util.ItemType#COURSE}
     * @param parentId
     *         The id of the parent object EX: parent points to course if item is an Assignment.
     *         If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *         problem
     * @throws DatabaseAccessException
     *         Thrown if the parent object can not be found.
     */
    private void copyParentDetails(final Document insertQuery, final String itemId, final Util.ItemType itemType, final String parentId)
            throws DatabaseAccessException {
        final Util.ItemType collectionType = getParentItemType(itemType);
        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(collectionType));
        final Document result = collection.find(convertStringToObjectId(parentId))
                .projection(Projections.include(USER_LIST, COURSE_ID, OWNER_ID)).first();
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        // This would overwrite existing id but there is already a valid id in here.
        result.remove(DatabaseStringConstants.SELF_ID);
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
    String createNewGroup(final String userId, final String courseId) throws AuthenticationException {
        final String hash;
        try {
            final String unsecuredSalt = HashManager.generateUnSecureSalt(courseId);
            hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Invalid algorithm when creating a new group", e);
        }
        final Document nonStudent = new Document();
        nonStudent.append(userId, hash);
        final Document groupQuery = new Document(DatabaseStringConstants.COURSE_ID, new ObjectId(courseId))
                .append(USER_LIST, new Document())
                .append(DatabaseStringConstants.NON_USER_LIST, nonStudent);

        final MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        collection.insertOne(groupQuery);
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

        final MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
        final Document group = collection.find(convertStringToObjectId(groupId))
                .projection(Projections.include(SELF_ID, COURSE_ID)).first();
        if (group == null) {
            throw new DatabaseAccessException("group could not be found");
        }
        final String hash;
        try {
            final String unsecuredSalt = HashManager.generateUnSecureSalt(group.get(DatabaseStringConstants.COURSE_ID).toString());
            hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                    .getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Invalid algorithm when inserting a user into group", e);
        }

        // final Document newIdentity = new Document(userId, hash);
        final String list = isUser ? USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).updateOne(
                group,
                new Document(DatabaseStringConstants.SET_COMMAND,
                        new Document(list + DatabaseStringConstants.SUBFIELD_COMMAND + userId, hash)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerUserInItem(final String userId, final String authId, final String itemId, final Util.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        if (!Util.ItemType.COURSE.equals(itemType) && !Util.ItemType.BANK_PROBLEM.equals(itemType)) {
            throw new AuthenticationException("Can only register users in a course or a bank problem", AuthenticationException.OTHER);
        }

        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));
        final Document result = collection.find(convertStringToObjectId(itemId))
                .projection(Projections.include(USER_LIST)).first();

        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final AuthenticationResponder responder = authChecker.checkAuthentication(itemType, itemId, authId, 0,
                Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
        if (!responder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permission to insert new items for id: " + itemId,
                    AuthenticationException.INVALID_PERMISSION);
        }

        final List<String> userGroups = getUserGroup(result);
        insertUserIntoGroup(userId, userGroups.get(0), !responder.hasPeerTeacherPermission());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> createNewUser(final String userName) throws AuthenticationException, DatabaseAccessException {
        final ObjectId userId = new ObjectId();
        final String userPassword = Encoder.nextID().toString();
        final String hashPassword;
        try {
            hashPassword = HashManager.createHash(userPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Invalid algorithm when creating a new user", e);
        }
        final MongoCollection<Document> userCollection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final Document query = new Document(DatabaseStringConstants.USER_NAME, userName);
        final Document cursor = userCollection.find(query).first();
        if (cursor == null) {
            userCollection.insertOne(new Document(DatabaseStringConstants.SELF_ID, userId)
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
        final MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final Document userInfo = collection.find(new Document(DatabaseStringConstants.USER_NAME, userName)).first();

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
    public Map<String, String> getItemRoster(final String authId, final String itemId, final Util.ItemType itemType,
            final Collection<String> userIdsList, final Authenticator authChecker)
            throws AuthenticationException, DatabaseAccessException {
        final AuthenticationResponder responder = authChecker.checkAuthentication(itemType, itemId, authId, 0,
                Authentication.AuthType.newBuilder().setCheckingAdmin(true).build());
        if (!responder.hasPeerTeacherPermission()) {
            throw new AuthenticationException("Need to be at least a peer teacher", AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> collection = database.getCollection(getCollectionFromType(itemType));

        final Document item = collection.find(convertStringToObjectId(itemId))
                .projection(Projections.include(USER_LIST)).first();

        if (item == null) {
            throw new DatabaseAccessException(getCollectionFromType(itemType) + " can not be found with the given itemId: " + itemId);
        }

        final Map<String, String> courseRoster = new HashMap<>();

        final List<String> groupList = getUserGroup(item);

        final MongoCollection<Document> groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

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
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    private Map<String, String> getGroupRoster(final MongoCollection<Document> collection, final String groupId)
            throws DatabaseAccessException {
        final Document group = collection.find(convertStringToObjectId(groupId))
                .projection(Projections.include(USER_LIST)).first();

        return (Map<String, String>) group.get(USER_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getUserName(final String userId, final String authId, final String itemId, final Util.ItemType itemType,
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
        final MongoCollection<Document> collection = database.getCollection(DatabaseStringConstants.USER_COLLECTION);
        final MongoCursor<Document> cursor = collection.find(
                new Document(DatabaseStringConstants.SELF_ID, new Document(DatabaseStringConstants.IN_COMMAND, identityList)))
                .projection(Projections.include(SELF_ID, USER_NAME)).iterator();

        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("No users were found with the given userIds");
        }

        final Map<String, String> userNameMap = new HashMap<>();
        while (cursor.hasNext()) {
            final Document userName = cursor.next();
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
    boolean isUserInItem(final String userId, final boolean isUser, final String itemId, final Util.ItemType itemType)
            throws DatabaseAccessException {
        final MongoCollection<Document> collection = this.database.getCollection(getCollectionFromType(itemType));
        final Document result = collection.find(convertStringToObjectId(itemId)).first();
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final List<String> groupList = getUserGroup(result);

        final MongoCollection<Document> groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

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
    private boolean isUserInGroup(final MongoCollection<Document> collection, final String groupId,
            final String userId, final boolean isUser) throws DatabaseAccessException {
        final String listType = isUser ? USER_LIST : DatabaseStringConstants.NON_USER_LIST;
        final Document group = collection.find(convertStringToObjectId(groupId))
                .projection(Projections.include(listType)).first();
        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        final Document list = (Document) group.get(listType);
        return list.containsKey(userId);
    }
}
