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
import protobuf.srl.services.authentication.Authentication;
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
public class IdentityWebSocketClient extends ClientWebSocket implements IdentityManagerInterface {

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

    @Override public final void createNewItem(final School.ItemType collectionType, final String itemId, final String parentId, final String userId,
            final String registrationKey) throws AuthenticationException {

        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .build();

        final Authentication.AuthCreationRequest.Builder creationRequestBuilder = Authentication.AuthCreationRequest.newBuilder()
                .setItemRequest(request);
        if (parentId == null && School.ItemType.COURSE != collectionType && School.ItemType.BANK_PROBLEM != collectionType) {
            throw new AuthenticationException("Parent Id can only be null when inserting a course", AuthenticationException.NO_AUTH_SENT);
        }
        if (parentId != null) {
            creationRequestBuilder.setParentItemId(parentId);
        }
        if (registrationKey != null) {
            creationRequestBuilder.setRegistrationKey(registrationKey);
        }

        Message.DefaultResponse response = null;
        try {
            final Authentication.AuthCreationRequest creationRequest = creationRequestBuilder.build();
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

    @Override public final void registerUser(final School.ItemType collectionType, final String itemId, final String userId,
            final String registrationKey)
            throws AuthenticationException {
        if (identityService == null) {
            identityService = Identity.IdentityService.newBlockingStub(getRpcChannel());
        }

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setAuthId(userId)
                .setItemId(itemId)
                .setItemType(collectionType)
                .build();

        final Authentication.UserRegistration.Builder creationRequest = Authentication.UserRegistration.newBuilder()
                .setItemRequest(request);

        if (registrationKey != null) {
            creationRequest.setRegistrationKey(registrationKey);
        }

        Message.DefaultResponse response = null;
        try {
            response = identityService.registerUser(getNewRpcController(), creationRequest.build());
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

    /**
     * Inserts a new item into the database.
     *
     * @param userId
     *         The user id of the user that is inserting the new item.
     * @param authId
     *         The AuthId of the user that is inserting the new item.
     * @param itemId
     *         The id of the item being inserted
     * @param itemType
     *         The type of item that is being inserted, EX: {@link School.ItemType#COURSE}
     * @param parentId
     *         The id of the parent object EX: parent points to course if item is an Assignment.
     *         If the {@code itemType} is a bank problem the this value can be a course that automatically gets permission to view the bank
     *         problem
     * @param authChecker
     *         Used to check that the user has access to perform the requested actions.
     * @throws DatabaseAccessException
     *         Thrown if the user does not have the correct permissions to perform the request actions.
     * @throws AuthenticationException
     *         Thrown if there is data that can not be found in the database.
     */
    @Override public void createNewItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final String parentId, final Authenticator authChecker) throws DatabaseAccessException, AuthenticationException {

    }

    /**
     * Registers a student with a course.
     *
     * The student must have a valid registration key.
     *
     * @param userId
     *         The user Id of the user that is being added.
     * @param authId
     *         The authentication Id of the user that is being added.
     * @param itemId
     *         The Id of the course or bank problem the user is being added to.
     * @param itemType
     *         The type of item the user is registering for (Only {@link School.ItemType#COURSE}
     *         and (Only {@link School.ItemType#BANK_PROBLEM} are valid types.
     * @param authChecker
     *         Used to check permissions in the database.
     * @throws AuthenticationException
     *         If the user does not have access or an invalid {@code registrationKey}.
     * @throws DatabaseAccessException
     *         Thrown if the item can not be found.
     */
    @Override public void registerUserInItem(final String userId, final String authId, final String itemId, final School.ItemType itemType,
            final Authenticator authChecker) throws AuthenticationException, DatabaseAccessException {

    }
}
