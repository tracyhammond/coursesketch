package coursesketch.database.auth;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import utilities.AuthUtilities;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static database.DbSchoolUtility.getCollectionFromType;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.STUDENT;

/**
 * Checks The local database for access to certain items.
 *
 * Created by dtracers
 */
public final class DbAuthChecker implements AuthenticationChecker {

    /**
     * The database that the auth checker grabs data from.
     */
    private final DB database;

    /**
     *
     * @param database
     */
    public DbAuthChecker(final DB database) {
        this.database = database;
    }

    /**
     * Checks to make sure that the user is authenticated for all values that
     * are true.
     *
     * @param collectionType
     *         The table / collection where this data is store.
     * @param itemId
     *         The Id of the object we are checking against.
     * @param userId
     *         The user we are checking is valid
     * @param preFixedCheckType
     *         The rules at that give a correct or false response.
     * @return True if all checked values are valid
     * @throws DatabaseAccessException
     *         thrown if there are issues grabbing data for the authenticator.
     * @throws AuthenticationException
     *         thrown if there are problems creating the auth response.
     */
    @Override public Authentication.AuthResponse isAuthenticated(final School.ItemType collectionType, final String itemId, final String userId,
            final Authentication.AuthType preFixedCheckType) throws DatabaseAccessException, AuthenticationException {

        checkNotNull(collectionType, "collectionType");
        checkNotNull(itemId, "itemId");
        checkNotNull(userId, "userId");

        final Authentication.AuthType checkType = AuthUtilities.fixCheckType(preFixedCheckType);
        if (!Authenticator.validUserAccessRequest(checkType)) {
            throw new AuthenticationException("Invalid Authentication Request: No options to check were filled out",
                    AuthenticationException.NO_AUTH_SENT);
        }

        final DBCollection collection = this.database.getCollection(getCollectionFromType(collectionType));
        final DBObject result = collection.findOne(new ObjectId(itemId));
        if (result == null) {
            throw new DatabaseAccessException("The item with the id " + itemId + " Was not found in the database");
        }

        final List<String> groupList = (List<String>) result.get(DatabaseStringConstants.USER_LIST);
        Authentication.AuthResponse.PermissionLevel permissionLevel = null;

        final DBCollection groupCollection = this.database.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);

        for (String groupId : groupList) {
            final Authentication.AuthResponse.PermissionLevel permLevel = checkGroupPermission(groupCollection, groupId, userId);
            if (permLevel != null) {
                permissionLevel = permLevel;
                break;
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

    private Authentication.AuthResponse.PermissionLevel checkGroupPermission(final DBCollection collection, final String groupId,
            final String userId) throws DatabaseAccessException, AuthenticationException {
        final DBObject group = collection.findOne(new ObjectId(groupId));
        if (group == null) {
            throw new DatabaseAccessException("Can not find group with id: " + groupId);
        }

        String hash = null;
        try {
            hash = HashManager.createHash(group.get(DatabaseStringConstants.COURSE_ID) + userId);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException(e);
        }
        final Object permissionLevel = group.get(hash);
        if (permissionLevel == null) {
            return Authentication.AuthResponse.PermissionLevel.NO_PERMISSION;
        }
        return Authentication.AuthResponse.PermissionLevel.valueOf((Integer) permissionLevel);
    }
}
