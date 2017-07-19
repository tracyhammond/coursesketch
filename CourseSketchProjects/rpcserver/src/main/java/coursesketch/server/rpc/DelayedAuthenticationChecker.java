package coursesketch.server.rpc;

import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationException;
import database.DatabaseAccessException;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A wrapper for the authentication checker that handles the case of when the authentication websocket will be available.
 *
 * This is needed because when the authenticator needs to be created before the auth websocket is ready
 *
 * Created by dtracers on 11/19/2015.
 */
class DelayedAuthenticationChecker implements AuthenticationChecker {

    /**
     * An authentication checker that is the real instance of the authenticator.
     */
    private AuthenticationChecker realAuthenticationChecker;

    /**
     * {@inheritDoc}
     * @throws AuthenticationException
     *         thrown if there are problems creating the auth response or if the direct implementation is null;.
     */
    @Override public Authentication.AuthResponse isAuthenticated(final Util.ItemType collectionType, final String itemId, final String userId,
            final Authentication.AuthType checkType) throws DatabaseAccessException, AuthenticationException {
        if (realAuthenticationChecker != null) {
            return realAuthenticationChecker.isAuthenticated(collectionType, itemId, userId, checkType);
        } else {
            throw new AuthenticationException("Authentication Instance has not been initialized yet", AuthenticationException.NO_AUTH_SENT);
        }
    }

    /**
     * Sets the authentication checker once the socket has been created.
     * @param realAuthenticationChecker An authentication checker that is the real instance of the authenticator.
     */
    /* package-private */ void setRealAuthenticationChecker(final AuthenticationChecker realAuthenticationChecker) {
        this.realAuthenticationChecker = checkNotNull(realAuthenticationChecker);
    }
}
