package database.auth;

import database.DatabaseAccessException;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

/**
 * Created by gigemjt on 9/3/15.
 */
public interface AuthenticationChecker {

    /**
     * Checks to make sure that the user is authenticated for all values that
     * are true.
     *
     * @param collectionType The table / collection where this data is store.
     * @param itemId
     *            The Id of the object we are checking against.
     * @param userId
     *            The user we are checking is valid
     * @param checkType The rules at that give a correct or false response.
     * @return True if all checked values are valid
     * @throws DatabaseAccessException thrown if there are issues grabbing data for the authenticator.
     * @throws AuthenticationException thrown if there are problems creating the auth response.
     */
    Authentication.AuthResponse isAuthenticated(final School.ItemType collectionType, final String itemId,
            final String userId, final Authentication.AuthType checkType)
            throws DatabaseAccessException, AuthenticationException;
}
