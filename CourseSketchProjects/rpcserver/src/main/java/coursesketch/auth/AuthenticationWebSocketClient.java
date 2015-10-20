package coursesketch.auth;

import com.google.protobuf.ServiceException;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.net.URI;

/**
 * A Websocket that connects to the Authentication server and abstracts the RPC method of sending request to the authentication server.
 *
 * This is also a valid {@link AuthenticationChecker} that can be passed to an authentication system.
 *
 * Created by gigemjt on 9/4/15.
 */
public class AuthenticationWebSocketClient extends ClientWebSocket implements AuthenticationChecker {

    /**
     * The default address for the authentication server.
     */
    public static final String ADDRESS = "AUTH_IP_PROP";

    /**
     * The default port of the Auth Server.
     */
    public static final int PORT = 8890;

    /**
     * The blocker service that is used to communicate.
     */
    private Authentication.AuthenticationService.BlockingInterface authService;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer The server that created the websocket.
     */
    public AuthenticationWebSocketClient(final URI iDestination,
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
     *         thrown if there are issues grabbing data for the authenticator.
     * @throws AuthenticationException Thrown if there is a problem creating the auth response.
     */
    @Override public final Authentication.AuthResponse isAuthenticated(final School.ItemType collectionType, final String itemId,
            final String userId, final Authentication.AuthType checkType) throws AuthenticationException {
        if (!Authenticator.validRequest(checkType)) {
            throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
        }
        if (authService == null) {
            authService = Authentication.AuthenticationService.newBlockingStub(getRpcChannel());
        }

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .setAuthParams(checkType)
                .build();
        Authentication.AuthResponse response = null;
        try {
            response = authService.authorizeUser(getNewRpcController(), request);
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
        return response;
    }
}
