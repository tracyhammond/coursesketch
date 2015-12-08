package coursesketch.database.auth;

import protobuf.srl.school.School;

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
     * @param itemType
     *         The type of item it is: course, assignment.
     * @param itemId
     *         The id of the item the new group is being added into.
     * @param parentId
     *         The id of the parent item.  This may not exist for certain {@code itemType}.
     * @param userId
     *         The user who is creating this item.  They do not have to be the owner of the topmost item.
     * @param registrationKey
     *         A registration key that is required to register new users in this auth group.  It is currently only used by
     *         {@link protobuf.srl.school.School.ItemType#COURSE} and {@link protobuf.srl.school.School.ItemType#BANK_PROBLEM}
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to create this new item group.
     */
    void createNewItem(School.ItemType itemType, String itemId, String parentId, String userId, String registrationKey)
            throws AuthenticationException;

    /**
     * Attempts to register the given userId in the items.
     *
     * This will only be used to give users an authentication level of user.
     * Different methods must be used to obtain larger authentication levels.
     *
     * @param itemType
     *         The type of item it is: course, assignment.
     * @param itemId
     *         The id of the item that the user is requesting registration for.
     * @param userId
     *         The user that is being registered
     * @param registrationKey
     *         This field is required and must contain the same value as the registration key in the database.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to register for this item.  (Typically that means the key is wrong)
     */
    void registerUser(School.ItemType itemType, String itemId, String userId, String registrationKey) throws AuthenticationException;
}
