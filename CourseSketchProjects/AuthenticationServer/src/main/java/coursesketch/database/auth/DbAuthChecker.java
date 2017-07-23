package coursesketch.database.auth;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.utilities.AuthUtilities;
import org.bson.Document;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.getUserGroup;
import static coursesketch.utilities.AuthUtilities.generateHash;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.OWNER;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.STUDENT;

/**
 * Checks the local database for access to certain items.
 *
 * Created by dtracers
 */
public final class DbAuthChecker implements AuthenticationChecker {

    /**
     * The database that the auth checker grabs data from.
     */
    private final MongoDatabase database;

    /**
     * Creates a DbAuthChecker that takes in the database.
     * @param database The database.
     */
    public DbAuthChecker(final MongoDatabase database) {
        this.database = database;
    }

    /**
     * Checks to make sure that the user is authenticated for all values in the preFixedCheckType that are true.
     *
     * @param collectionType
     *         The table / collection where this data is stored.
     * @param itemId
     *         The Id of the object we are checking against.
     * @param userId
     *         The user we are checking is valid.
     * @param preFixedCheckType
     *         The rules that we check against to determine if the user is authenticated or not.
     * @return True if all checked values are valid.
     * @throws DatabaseAccessException
     *         Thrown if there are issues grabbing data for the database.
     * @throws AuthenticationException
     *         Thrown if there are problems creating the auth response.
     */
    @Override
    public Authentication.AuthResponse isAuthenticated(final Util.ItemType collectionType, final String itemId, final String userId,
            final Authentication.AuthType preFixedCheckType) throws DatabaseAccessException, AuthenticationException {

        checkNotNull(collectionType, "collectionType");
        checkNotNull(itemId, "itemId");
        checkNotNull(userId, "userId");

        final Authentication.AuthType checkType = AuthUtilities.fixCheckType(preFixedCheckType);
        if (!Authenticator.validUserAccessRequest(checkType)) {
            throw new AuthenticationException("Invalid Authentication Request: No auth options set to check against.",
                    AuthenticationException.NO_AUTH_SENT);
        }

        final MongoCollection<Document> collection = this.database.getCollection(getCollectionFromType(collectionType));
        final Document result = collection.find(convertStringToObjectId(itemId)).first();
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " was not found in the database.");
        }

        final Authentication.AuthResponse.PermissionLevel permissionLevel = getPermissionLevel(preFixedCheckType, userId, result);

        if (permissionLevel == null) {
            return Authentication.AuthResponse.getDefaultInstance();
        }

        final Authentication.AuthResponse.Builder responseBuilder = Authentication.AuthResponse.newBuilder();
        if (permissionLevel.equals(OWNER) && checkType.getCheckingOwner()) {
            responseBuilder.setIsOwner(true);
        }

        if (checkType.getCheckAccess()) {
            responseBuilder.setHasAccess(permissionLevel.compareTo(STUDENT) >= 0);
        }
        final Authentication.AuthResponse.PermissionLevel largestAllowedLevel = AuthUtilities.largestAllowedLevel(checkType);
        // left - right
        if (permissionLevel.compareTo(largestAllowedLevel) >= 0) {
            responseBuilder.setPermissionLevel(largestAllowedLevel);
        } else {
            responseBuilder.setPermissionLevel(permissionLevel);
        }
        return responseBuilder.build();
    }

    /**
     * Gets the permission level from the document.
     * @param userId
     *         The user we are checking is valid.
     * @param preFixedCheckType
     *         The rules that we check against to determine if the user is authenticated or not.
     * @param authDatabaseObject
     *         The object that was grabbed from the database.
     * @return Permission level of the user.
     * @throws DatabaseAccessException
     *         Thrown if there are issues grabbing data for the database.
     * @throws AuthenticationException
     *         Thrown if there are problems creating the auth response.
     */
    private Authentication.AuthResponse.PermissionLevel getPermissionLevel(final Authentication.AuthType preFixedCheckType,
            final String userId, final Document authDatabaseObject) throws DatabaseAccessException, AuthenticationException {

        if (preFixedCheckType.getCheckingOwner()
                && authDatabaseObject.get(DatabaseStringConstants.OWNER_ID).toString().equals(userId)) {
            // This may need to be larger permission if needed.
            return Authentication.AuthResponse.PermissionLevel.OWNER;
        }

        final List<String> groupList = getUserGroup(authDatabaseObject);
        Authentication.AuthResponse.PermissionLevel permissionLevel = null;

        final MongoCollection<Document> groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

        // Checks the permission level for each group that is used by the item+
        for (String groupId : groupList) {
            final Authentication.AuthResponse.PermissionLevel permLevel = getUserPermissionLevel(groupCollection, groupId, userId);
            if (permLevel != null) {
                permissionLevel = permLevel;
                break;
            }
        }
        return permissionLevel;
    }

    /**
     * Checks the group permission, and returns a permission level.
     *
     * @param collection The collection that contains the group.
     * @param groupId The id of the group that is being checked.
     * @param userId The id that is being checked.
     * @return A permission level that represents what permission the user has.  This does not return null.
     * @throws DatabaseAccessException Thrown if the group does not exist.
     * @throws AuthenticationException Thrown if there are problems comparing the hashes.
     */
    private Authentication.AuthResponse.PermissionLevel getUserPermissionLevel(final MongoCollection<Document> collection, final String groupId,
            final String userId) throws DatabaseAccessException, AuthenticationException {
        final Document group = collection.find(convertStringToObjectId(groupId)).first();
        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        final String salt = group.get(DatabaseStringConstants.SALT).toString();
        final String hash = generateHash(userId, salt);

        final Object permissionLevel = group.get(hash);
        if (permissionLevel == null) {
            return Authentication.AuthResponse.PermissionLevel.NO_PERMISSION;
        }
        return Authentication.AuthResponse.PermissionLevel.valueOf((Integer) permissionLevel);
    }
}
