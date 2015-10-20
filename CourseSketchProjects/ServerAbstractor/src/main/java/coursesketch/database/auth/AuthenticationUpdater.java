package coursesketch.database.auth;

import protobuf.srl.school.School;

/**
 * An interface that is required for any item wanting to update or modify Authentication data.
 *
 * Created by dtracers on 10/20/2015.
 */
public interface AuthenticationUpdater {
    void createNewItem(School.ItemType itemType, String itemId, String parentId, String userId, String registrationKey)
            throws AuthenticationException;
    void registerUser(School.ItemType itemType, String itemId, String userId, String registrationKey) throws AuthenticationException;
}
