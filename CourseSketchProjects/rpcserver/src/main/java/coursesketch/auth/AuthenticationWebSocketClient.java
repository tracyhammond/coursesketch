package coursesketch.auth;

import com.google.protobuf.ServiceException;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import database.DatabaseAccessException;
import database.auth.AuthenticationChecker;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.net.URI;

/**
 * Created by gigemjt on 9/4/15.
 */
public class AuthenticationWebSocketClient extends ClientWebSocket implements AuthenticationChecker {

    private Authentication.AuthenticationService.BlockingInterface authService;
    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link AbstractClientWebSocket#connect()} or call
     * {@link AbstractClientWebSocket#send(ByteBuffer)}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer
     */
    protected AuthenticationWebSocketClient(final URI iDestination,
            final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
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
     * @param checkType
     *         The rules at that give a correct or false response.
     * @return True if all checked values are valid
     * @throws DatabaseAccessException
     *         thrown if there are issues grabbing data for the authenticator.
     */
    @Override public Authentication.AuthResponse isAuthenticated(final School.ItemType collectionType, final String itemId,
            final String userId, final Authentication.AuthType checkType) throws AuthenticationException {
        if (!Authenticator.validRequest(checkType)) {
            throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
        }
        if (authService == null) {
            authService = Authentication.AuthenticationService.newBlockingStub(getRpcChannel());
        }

        Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .setAuthParams(checkType)
                .build();
        Authentication.AuthResponse response = null;
        try {
            response = authService.authorizeUser(getnewRpcController(), request);
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
        return response;
    }
}
