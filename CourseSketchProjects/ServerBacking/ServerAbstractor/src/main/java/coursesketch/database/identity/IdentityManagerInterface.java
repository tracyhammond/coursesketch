package coursesketch.database.identity;

import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.utils.Util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by dtracers on 12/4/2015.
 */
public interface IdentityManagerInterface {

    /**
     * Gets the course roster.
     *
     * Only the users in the course roster are returned and the non users (moderators, peer teachers, teachers) are not returned by this function.
     *
     * @param authId
     *         The authentication id of the user wanting the item roster
     * @param itemId
     *         The item that the roster is being grabbed for (does not have to be a course)
     * @param itemType
     *         The itemtype that the roster is being grabbed for (does not have to be a course)
     * @param userIdsList
     *         a list of specific userIds to be grabbed.  Only the ids contained in this list are returned.
     *         This can be used to grab a single id as well
     * @param authChecker
     *         Used to check permissions in the database.
     * @return an {@code Map<String, String>} that maps a hashed userId (hashed by the courseId) to the username {@code Map<UserIdHash, UserName>}
     * If the user getting the course roster only as peer level permissions then the user name is not returned but the course roster still is.
     * Instead the map contains null values instead of a username {@code Map<UserIdHash, null>}.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission
     * @throws DatabaseAccessException
     *         Thrown if the item, group, or users do not exist.
     */
    Map<String, String> getItemRoster(String authId, String itemId, Util.ItemType itemType,
            Collection<String> userIdsList, Authenticator authChecker) throws AuthenticationException, DatabaseAccessException;

    /**
     * Creates a new user in the identity server.
     *
     * @param userName
     *         The username that is being added to the database.
     * @return A map that has the userId as the key and the password to access the userId as the value.
     * @throws AuthenticationException
     *         thrown if there is a problem creating the user hash.
     * @throws DatabaseAccessException
     *         thrown if there is a problem with the database.
     */
    Map<String, String> createNewUser(String userName) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets the username given the actual unhashed userId.
     *
     * @param userId
     *         The userId the username is being requested for
     * @param authId
     *         The permission the person who is asking for the username has
     * @param itemId
     *         Used for authentication purposes to ensure the person asking for the userId has permission to get the username
     * @param itemType
     *         Used for authentication purposes to ensure the person asking for the userId has permission to get the username
     * @param authChecker
     *         Used to check permissions in the database.
     * @return A map of the userId to the userName, {@code Map<UserId, UserName>}
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to get the user name
     * @throws DatabaseAccessException
     *         Thrown if the username does not exist.
     */
    Map<String, String> getUserName(String userId, String authId, String itemId, Util.ItemType itemType,
            Authenticator authChecker)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets the user identity.
     *
     * @param userName
     *         The username that is associated with the userId
     * @param authId
     *         The password to getting the user identity.
     * @return The userIdentity.
     * @throws AuthenticationException
     *         Thrown if the {@code authId} is invalid.
     * @throws DatabaseAccessException
     *         Thrown if the user is not found.
     */
    String getUserIdentity(String userName, String authId) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts a new item into the database.
     *
     * @param userId
     *         The user id of the user that is inserting the new item.
     * @param authId
     *         The AuthId of the user that is inserting the new item.
     * @param itemId
     *         The id of the item being inserted
     * @param itemType
     *         The type of item that is being inserted, EX: {@link protobuf.srl.school.Util.ItemType#COURSE}
     * @param parentId
     *         The id of the parent object EX: parent points to course if item is an Assignment.
     *         If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *         problem
     * @param authChecker
     *         Used to check that the user has access to perform the requested actions.
     * @throws DatabaseAccessException
     *         Thrown if the user does not have the correct permissions to perform the request actions.
     * @throws AuthenticationException
     *         Thrown if there is data that can not be found in the database.
     */
    void createNewItem(String userId, String authId, String itemId, Util.ItemType itemType,
            String parentId, Authenticator authChecker) throws DatabaseAccessException, AuthenticationException;

    /**
     * Registers a student with a course.
     *
     * The student must have a valid registration key.
     *
     * @param userId
     *         The user Id of the user that is being added.
     * @param authId
     *         The authentication Id of the user that is being added.
     * @param itemId
     *         The Id of the course or bank problem the user is being added to.
     * @param itemType
     *         The type of item the user is registering for (Only {@link protobuf.srl.school.Util.ItemType#COURSE}
     *         and (Only {@link protobuf.srl.school.Util.ItemType#BANK_PROBLEM} are valid types.
     * @param authChecker
     *         Used to check permissions in the database.
     * @throws AuthenticationException
     *         If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException
     *         Thrown if the item can not be found.
     */
    void registerUserInItem(String userId, String authId, String itemId, Util.ItemType itemType,
            Authenticator authChecker) throws AuthenticationException, DatabaseAccessException;
}
