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

    /**
     * Gets the course roster.
     *
     * Only the users in the course roster are returned and the non users (moderators, peer teachers, teachers) are not returned by this function.
     *
     * @param authId
     *         The authentication id of the user wanting the item roster
     * @param itemId
     *         The item that the roster is being grabbed for (does not have to be a course)
     * @param itemType
     *         The itemtype that the roster is being grabbed for (does not have to be a course)
     * @param userIdsList
     *         a list of specific userIds to be grabbed.  Only the ids contained in this list are returned.
     *         This can be used to grab a single id as well
     * @param authChecker
     *         Used to check permissions in the database.
     * @return an {@code Map<String, String>} that maps a hashed userId (hashed by the courseId) to the username {@code Map<UserIdHash, UserName>}
     * If the user getting the course roster only as peer level permissions then the user name is not returned but the course roster still is.
     * Instead the map contains null values instead of a username {@code Map<UserIdHash, null>}.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission
     * @throws DatabaseAccessException
     *         Thrown if the item, group, or users do not exist.
     */
    @Override public Map<String, String> getItemRoster(final String authId, final String itemId, final School.ItemType itemType,
            final Collection<String> userIdsList, final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        return null;
    }

    /**
     * Creates a new user in the identity server.
     *
     * @param userName
     *         The username that is being added to the database.
     * @return A map that has the userId as the key and the password to access the userId as the value.
     * @throws AuthenticationException
     *         thrown if there is a problem creating the user hash.
     */
    @Override public Map<String, String> createNewUser(final String userName) throws AuthenticationException {
        return null;
    }

    /**
     * Gets the username given the actual unhashed userId.
     *
     * @param userId
     *         The userId the username is being requested for
     * @param authId
     *         The permission the person who is asking for the username has
     * @param itemId
     *         Used for authentication purposes to ensure the person asking for the userId has permission to get the username
     * @param itemType
     *         Used for authentication purposes to ensure the person asking for the userId has permission to get the username
     * @param authChecker
     *         Used to check permissions in the database.
     * @return A map of the userId to the userName, {@code Map<UserId, UserName>}
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to get the user name
     * @throws DatabaseAccessException
     *         Thrown if the username does not exist.
     */
    @Override public Map<String, String> getUserName(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {
        return null;
    }

    /**
     * Gets the user identity.
     *
     * @param userName
     *         The username that is associated with the userId
     * @param authId
     *         The password to getting the user identity.
     * @return The userIdentity.
     * @throws AuthenticationException
     *         Thrown if the {@code authId} is invalid.
     * @throws DatabaseAccessException
     *         Thrown if the user is not found.
     */
    @Override public String getUserIdentity(final String userName, final String authId) throws AuthenticationException, DatabaseAccessException {
        return null;
    }
}
