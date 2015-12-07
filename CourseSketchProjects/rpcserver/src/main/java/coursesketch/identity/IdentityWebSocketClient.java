package coursesketch.identity;

import com.google.protobuf.ServiceException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.school.School;
import protobuf.srl.services.identity.Identity;
import utilities.ExceptionUtilities;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Websocket that connects to the Identity server and abstracts the RPC method of sending request to the authentication server.
 *
 *
 * Created by gigemjt on 12/4/15.
 */
public final class IdentityWebSocketClient extends ClientWebSocket implements IdentityManagerInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityWebSocketClient.class);

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
                .setAuthId(authId)
                .build();

        final Identity.RequestRoster.Builder creationRequestBuilder = Identity.RequestRoster.newBuilder()
                .setRequestData(request)
                .addAllUserIds(userIdsList);

        Identity.UserNameResponse response = null;
        try {
            final Identity.RequestRoster creationRequest = creationRequestBuilder.build();
            response = identityService.getItemRoster(getNewRpcController(), creationRequest);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                handleProtoException(response.getDefaultResponse().getException(), "Exception thrown while getting the item roster");
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException(e, false);
        }
        return createMapFromProto(response);
    }

    @Override public Map<String, String> createNewUser(final String userName) throws AuthenticationException, DatabaseAccessException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(userName)
                .build();

        Identity.UserNameResponse response = null;
        try {
            response = identityService.createNewUser(getNewRpcController(), request);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                handleProtoException(response.getDefaultResponse().getException(), "Exception thrown while creating a new user");
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException(e, false);
        }
        return createMapFromProto(response);
    }

    @Override public Map<String, String> getUserName(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(userId)
                .setItemId(itemId)
                .setItemType(itemType)
                .setAuthId(authId)
                .build();

        Identity.UserNameResponse response = null;
        try {
            response = identityService.getUserName(getNewRpcController(), request);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                handleProtoException(response.getDefaultResponse().getException(), "Exception thrown while getting the user name");
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException(e, false);
        }
        return createMapFromProto(response);
    }

    @Override public String getUserIdentity(final String userName, final String authId) throws AuthenticationException, DatabaseAccessException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(userName)
                .setAuthId(authId)
                .build();

        Identity.UserNameResponse response = null;
        try {
            response = identityService.getUserIdentity(getNewRpcController(), request);
            LOG.debug("Response ", response);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                handleProtoException(response.getDefaultResponse().getException(), "Exception thrown while getting the user identity");
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException(e, false);
        }
        final Map<String, String> result = createMapFromProto(response);
        if (result.isEmpty()) {
            throw new DatabaseAccessException("Unable to find the user identity");
        }
        return result.entrySet().iterator().next().getValue();
    }

    /**
     * Creates a map from the UserNameResponse.
     * @param response The response that contains the map that is being reconstructed.
     * @return A map of one string to the other that was in the user name response.
     */
    private Map<String, String> createMapFromProto(final Identity.UserNameResponse response) {
        final Map<String, String> result = new HashMap<>();
        for (Identity.UserNameResponse.MapFieldEntry mapFieldEntry : response.getUserNamesList()) {
            result.put(mapFieldEntry.getKey(), mapFieldEntry.getValue());
        }
        return result;
    }

    /**
     * Checks the type of exception that was thrown in the proto exception and throws the same type if it exists.
     * @param exception The exception that was found to be thrown.
     * @param message An optional message
     * @throws AuthenticationException Thrown if the proto exception was an AuthenticationException.
     * @throws DatabaseAccessException Thrown if the proto exception was an DatabaseAccessException.
     */
    private void handleProtoException(final Message.ProtoException exception, final String message)
            throws AuthenticationException, DatabaseAccessException {
        if (ExceptionUtilities.isSameType(AuthenticationException.class, exception)) {
            final AuthenticationException exception1 = new AuthenticationException(message, AuthenticationException.OTHER);
            exception1.setProtoException(exception);
            throw exception1;
        } else if (ExceptionUtilities.isSameType(DatabaseAccessException.class, exception)) {
            final DatabaseAccessException exception1 = new DatabaseAccessException(message);
            exception1.setProtoException(exception);
            throw exception1;
        }
        LOG.error(message, exception);
    }
}
