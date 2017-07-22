package coursesketch.database.auth;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;
import utilities.AuthUtilities;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static database.DbSchoolUtility.getCollectionFromType;
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
    private final DB database;

    /**
     * Creates a DbAuthChecker that takes in the database.
     * @param database The database.
     */
    public DbAuthChecker(final DB database) {
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
    @Override public Authentication.AuthResponse isAuthenticated(final Util.ItemType collectionType, final String itemId, final String userId,
            final Authentication.AuthType preFixedCheckType) throws DatabaseAccessException, AuthenticationException {

        checkNotNull(collectionType, "collectionType");
        checkNotNull(itemId, "itemId");
        checkNotNull(userId, "userId");

        final Authentication.AuthType checkType = AuthUtilities.fixCheckType(preFixedCheckType);
        if (!Authenticator.validUserAccessRequest(checkType)) {
            throw new AuthenticationException("Invalid Authentication Request: No auth options set to check against.",
                    AuthenticationException.NO_AUTH_SENT);
        }

        final DBCollection collection = this.database.getCollection(getCollectionFromType(collectionType));
        final DBObject result = collection.findOne(new ObjectId(itemId));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " was not found in the database.");
        }

        final List<String> groupList = (List<String>) result.get(DatabaseStringConstants.USER_LIST);
        Authentication.AuthResponse.PermissionLevel permissionLevel = null;

        if (preFixedCheckType.getCheckingOwner()) {
            if (result.get(DatabaseStringConstants.OWNER_ID).equals(userId)) {
                // This may need to be larger permission if needed.
                permissionLevel = Authentication.AuthResponse.PermissionLevel.TEACHER;
            }
        }

        if (permissionLevel == null) {
            final DBCollection groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

            // Checks the permission level for each group that is used by the item+
            for (String groupId : groupList) {
                final Authentication.AuthResponse.PermissionLevel permLevel = getUserPermissionLevel(groupCollection, groupId, userId);
                if (permLevel != null) {
                    permissionLevel = permLevel;
                    break;
                }
            }
        }

        if (permissionLevel == null) {
            return Authentication.AuthResponse.getDefaultInstance();
        }

        final Authentication.AuthResponse.Builder responseBuilder = Authentication.AuthResponse.newBuilder();
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
     * Checks the group permission, and returns a permission level.
     *
     * @param collection The collection that contains the group.
     * @param groupId The id of the group that is being checked.
     * @param userId The id that is being checked.
     * @return A permission level that represents what permission the user has.  This does not return null.
     * @throws DatabaseAccessException Thrown if the group does not exist.
     * @throws AuthenticationException Thrown if there are problems comparing the hashes.
     */
    private Authentication.AuthResponse.PermissionLevel getUserPermissionLevel(final DBCollection collection, final String groupId,
            final String userId) throws DatabaseAccessException, AuthenticationException {
        final DBObject group = collection.findOne(new ObjectId(groupId));
        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        String hash = null;
        final String salt = group.get(DatabaseStringConstants.SALT).toString();
        try {
            hash = HashManager.toHex(HashManager.createHash(userId, salt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
        if (hash == null) {
            throw new AuthenticationException("Unable to create authentication hash for group " + groupId, AuthenticationException.OTHER);
        }
        final Object permissionLevel = group.get(hash);
        if (permissionLevel == null) {
            return Authentication.AuthResponse.PermissionLevel.NO_PERMISSION;
        }
        return Authentication.AuthResponse.PermissionLevel.valueOf((Integer) permissionLevel);
    }
}
