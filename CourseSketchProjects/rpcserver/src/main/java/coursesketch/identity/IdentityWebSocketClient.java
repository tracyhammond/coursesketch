package coursesketch.identity;

import com.google.protobuf.ServiceException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import database.DatabaseAccessException;
import protobuf.srl.request.Message;
import protobuf.srl.school.School;
import protobuf.srl.services.identity.Identity;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * A Websocket that connects to the Identity server and abstracts the RPC method of sending request to the authentication server.
 *
 *
 * Created by gigemjt on 12/4/15.
 */
public final class IdentityWebSocketClient extends ClientWebSocket implements IdentityManagerInterface {

    /**
     * The default address for the identity server.
     */
    public static final String ADDRESS = "IDENTITY_IP_PROP";

    /**
     * The default port of the Identity Server.
     */
    public static final int PORT = 8891;

    /**
     * The blocker service that is used to communicate.
     */
    private Identity.IdentityService.BlockingInterface identityService;

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
    public IdentityWebSocketClient(final URI iDestination,
            final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    @Override public void createNewItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final String parentId, final Authenticator authChecker)
            throws AuthenticationException {

        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(itemType)
                .build();

        final Identity.IdentityCreationRequest.Builder creationRequestBuilder = Identity.IdentityCreationRequest.newBuilder()
                .setItemRequest(request);
        if (parentId == null && School.ItemType.COURSE != itemType && School.ItemType.BANK_PROBLEM != itemType) {
            throw new AuthenticationException("Parent Id can only be null when inserting a course", AuthenticationException.NO_AUTH_SENT);
        }
        if (parentId != null) {
            creationRequestBuilder.setParentItemId(parentId);
        }

        Message.DefaultResponse response = null;
        try {
            final Identity.IdentityCreationRequest creationRequest = creationRequestBuilder.build();
            response = identityService.createNewItem(getNewRpcController(), creationRequest);
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

    @Override public void registerUserInItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(itemType)
                .build();

        Message.DefaultResponse response = null;
        try {
            response = identityService.registerUser(getNewRpcController(), request);
            if (response.hasException()) {
                final AuthenticationException authExcep =
                        new AuthenticationException("Exception with authentication server", AuthenticationException.OTHER);
                authExcep.setProtoException(response.getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
    }

    @Override public Map<String, String> getItemRoster(final String authId, final String itemId, final School.ItemType itemType,
            final Collection<String> userIdsList, final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setItemId(itemId)
                .setItemType(itemType)
                .build();

        final Identity.IdentityCreationRequest.Builder creationRequestBuilder = Identity.IdentityCreationRequest.newBuilder()
                .setItemRequest(request);
        if (parentId == null && School.ItemType.COURSE != itemType && School.ItemType.BANK_PROBLEM != itemType) {
            throw new AuthenticationException("Parent Id can only be null when inserting a course", AuthenticationException.NO_AUTH_SENT);
        }
        if (parentId != null) {
            creationRequestBuilder.setParentItemId(parentId);
        }

        Message.DefaultResponse response = null;
        try {
            final Identity.IdentityCreationRequest creationRequest = creationRequestBuilder.build();
            response = identityService.requestCourseRoster(getNewRpcController(), creationRequest);
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

    @Override public Map<String, String> createNewUser(final String userName) throws AuthenticationException {
        return null;
    }

    @Override public Map<String, String> getUserName(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        return null;
    }

    @Override public String getUserIdentity(final String userName, final String authId) throws AuthenticationException, DatabaseAccessException {
        return null;
    }
}
