package coursesketch.database.auth;

import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

/**
 * An interface that is required for any item wanting to update or modify Authentication data.
 *
 * Created by dtracers on 10/20/2015.
 */
public interface AuthenticationUpdater {
    /**
     * Creates a new Auth group for the given item information in the authentication server.
     *
     * This new item group will contain information.
     *
     * @param authId
     *         The user who is creating this item.  They do not have to be the owner of the topmost item.
     * @param itemId
     *         The id of the item the new group is being added into.
     * @param itemType
     *         The type of item it is: course, assignment.
     * @param parentId
     *         The id of the parent item.  This may not exist for certain {@code itemType}.
     * @param registrationKey
     *         A registration key that is required to register new users in this auth group.  It is currently only used by
     *         {@link Util.ItemType#COURSE} and {@link Util.ItemType#BANK_PROBLEM}
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to create this new item group.
     */
    void createNewItem(String authId, String itemId, Util.ItemType itemType, String parentId, String registrationKey)
            throws AuthenticationException;

    /**
     * Attempts to register the given authId in the items.
     *
     * This will only be used to give users an authentication level of user.
     * Different methods must be used to obtain larger authentication levels.
     *
     * @param authId
     *         The user that is being registered
     * @param itemId
     *         The id of the item that the user is requesting registration for.
     * @param itemType
     *         The type of item it is: course, assignment.
     * @param registrationKey
     *         This field is required and must contain the same value as the registration key in the database.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to register for this item.  (Typically that means the key is wrong)
     */
    void registerUser(String authId, String itemId, Util.ItemType itemType, String registrationKey) throws AuthenticationException;

    /**
     * Attempts to register the given authId in the items.
     *
     * This will only be used to give users an authentication level of user.
     * Different methods must be used to obtain larger authentication levels.
     * @param ownerId
     *         The id of the owner of this item.
     * @param authId
     *         The user that is being registered.
     * @param itemId
     *         The id of the item that the user is requesting registration for.
     * @param itemType
     *         The type of item it is: course, assignment.
     * @param permissionLevel
     *         The permission level the user should be added at.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to register for this item.  (Typically that means the owner id is wrong)
     */
    void addUser(String ownerId, String authId, String itemId, Util.ItemType itemType,
            Authentication.AuthResponse.PermissionLevel permissionLevel) throws AuthenticationException;
}
