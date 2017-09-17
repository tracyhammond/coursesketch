package coursesketch.auth;

import com.google.protobuf.ServiceException;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationUpdater;
import coursesketch.database.auth.Authenticator;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import protobuf.srl.request.Message;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.net.URI;

import static coursesketch.utilities.AuthUtilities.createAuthTypeCheckFromLevel;

/**
 * A Websocket that connects to the Authentication server and abstracts the RPC method of sending request to the authentication server.
 *
 * This is also a valid {@link AuthenticationChecker} that can be passed to an authentication system.
 *
 * Created by gigemjt on 9/4/15.
 */
public class AuthenticationWebSocketClient extends ClientWebSocket implements AuthenticationChecker, AuthenticationUpdater {

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
     *
     * Note that this does not actually try and connect the wrapper. You have to
     * explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()}.
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
     * Checks to make sure the user is authenticated for all values that are true.
     *
     * @param collectionType
     *         The table / collection where this data is stored.
     * @param itemId
     *         The Id of the object we are checking against.
     * @param userId
     *         The user we are checking is valid.
     * @param checkType
     *         The rules at that give a correct or false response.
     * @return True if all checked values are valid.
     *
     * @throws AuthenticationException Thrown if there is a problem creating the auth response.
     */
    @Override
    public final Authentication.AuthResponse isAuthenticated(final Util.ItemType collectionType, final String itemId,
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
        final Authentication.AuthResponse response;
        try {
            response = authService.authorizeUser(getNewRpcController(), request);
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
        if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
            final AuthenticationException authExcep =
                    new AuthenticationException(AuthenticationException.OTHER);
            authExcep.setProtoException(response.getDefaultResponse().getException());
            throw authExcep;
        }
        return response;
    }

    @Override
    public final void createNewItem(final String authId, final String itemId, final Util.ItemType collectionType,
            final String parentId,
            final String registrationKey) throws AuthenticationException {

        if (authService == null) {
            authService = Authentication.AuthenticationService.newBlockingStub(getRpcChannel());
        }

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(authId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .build();

        final Authentication.AuthCreationRequest.Builder creationRequestBuilder = Authentication.AuthCreationRequest.newBuilder()
                .setItemRequest(request);
        if (parentId == null && Util.ItemType.COURSE != collectionType && Util.ItemType.BANK_PROBLEM != collectionType) {
            throw new AuthenticationException("Parent Id can only be null when inserting a course", AuthenticationException.NO_AUTH_SENT);
        }
        if (parentId != null) {
            creationRequestBuilder.setParentItemId(parentId);
        }
        if (registrationKey != null) {
            creationRequestBuilder.setRegistrationKey(registrationKey);
        }

        final Message.DefaultResponse response;
        try {
            final Authentication.AuthCreationRequest creationRequest = creationRequestBuilder.build();
            response = authService.createNewItem(getNewRpcController(), creationRequest);
            if (response.hasException()) {
                final AuthenticationException authExcep =
                        new AuthenticationException("Exception with authentication server", AuthenticationException.OTHER);
                authExcep.setProtoException(response.getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public final void registerUser(final String authId, final String itemId, final Util.ItemType collectionType,
            final String registrationKey)
            throws AuthenticationException {
        if (authService == null) {
            authService = Authentication.AuthenticationService.newBlockingStub(getRpcChannel());
        }

        final Authentication.UserRegistration registrationRequest =
                createRegistrationRequest(registrationKey, authId, itemId, collectionType, Authentication.AuthType.getDefaultInstance());

        final Message.DefaultResponse response;
        try {
            response = authService.registerUser(getNewRpcController(), registrationRequest);
            if (response.hasException()) {
                final AuthenticationException authExcep =
                        new AuthenticationException("Exception with authentication server", AuthenticationException.OTHER);
                authExcep.setProtoException(response.getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public final void addUser(final String ownerId, final String authId, final String itemId, final Util.ItemType collectionType,
            final Authentication.AuthResponse.PermissionLevel permissionLevel)
            throws AuthenticationException {
        if (authService == null) {
            authService = Authentication.AuthenticationService.newBlockingStub(getRpcChannel());
        }

        final Authentication.UserRegistration registrationRequest = createRegistrationRequest(ownerId, authId, itemId, collectionType,
                createAuthTypeCheckFromLevel(permissionLevel).setCheckingOwner(true).build());

        final Message.DefaultResponse response;
        try {
            response = authService.addUser(getNewRpcController(), registrationRequest);
            if (response.hasException()) {
                final AuthenticationException authExcep =
                        new AuthenticationException("Exception with authentication server", AuthenticationException.OTHER);
                authExcep.setProtoException(response.getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Creates a request for registration/adding a user.
     *
     * @param registrationKey Used to authenticate the request
     * @param authId The id being registered/added
     * @param itemId The item it belongs to
     * @param collectionType The type of collection
     * @param authParams optional parameters
     * @return A registration proto.
     */
    private Authentication.UserRegistration createRegistrationRequest(final String registrationKey, final String authId, final String itemId,
            final Util.ItemType collectionType, final Authentication.AuthType authParams) {

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(authId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .setAuthParams(authParams)
                .build();

        final Authentication.UserRegistration.Builder creationRequest = Authentication.UserRegistration.newBuilder()
                .setItemRequest(request);

        if (registrationKey != null) {
            creationRequest.setRegistrationKey(registrationKey);
        }
        return creationRequest.build();
    }
}
