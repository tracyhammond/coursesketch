package database.auth;

import database.auth.Authenticator.AuthenticationData;

import java.util.List;

/**
 * An interface that implements where data for authentication actually comes from.
 * @author gigemjt
 *
 */
public interface AuthenticationDataCreator {

    /**
     * Returns the authentication group given a collection and an itemId.
     *
     * @param collection
     *            The table / collection where this data is store.
     * @param itemId
     *            The specific group id where this data is stored.
     * @return an {@link AuthenticationData}
     */
    AuthenticationData getAuthGroups(String collection, String itemId);

    /**
     * Grabs the user list using some method.
     *
     * @param groupId
     *            The group Id
     * @return A list that represents the group found at id.
     */
    List<String> getUserList(final String groupId);
}
