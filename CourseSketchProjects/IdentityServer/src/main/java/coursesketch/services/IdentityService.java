package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManager;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.services.identity.Identity;
import utilities.ExceptionUtilities;

import java.util.Map;

/**
 * Created by gigemjt on 9/3/15.
 */
public final class IdentityService extends Identity.IdentityService implements CourseSketchRpcService {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityService.class);

    /**
     * Manages authentication storage for this service.
     */
    private final IdentityManager identityManager;

    /**
     * Used for checking if users have permissions to access certain values.
     */
    private final Authenticator authChecker;

    /**
     * Creates an authentication service with an DbAuthManager and a DbAuthChecker.
     * @param authChecker {@link #authChecker}
     * @param identityManager {@link #identityManager}
     */
    public IdentityService(final Authenticator authChecker, final IdentityManager identityManager) {
        this.authChecker = authChecker;
        this.identityManager = identityManager;
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializer The object used to initialize the sockets.
     */
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        // Does not set any values.
    }

    /**
     * {@inheritDoc}
     *
     * Grabs the course roster.
     */
    @Override public void getItemRoster(final RpcController controller, final Identity.RequestRoster request,
            final RpcCallback<Identity.UserNameResponse> done) {
        final Identity.IdentityRequest requestData = request.getRequestData();
        Map<String, String> userIds;
        try {
            userIds = identityManager.getItemRoster(requestData.getAuthId(), requestData.getItemId(), requestData.getItemType(),
                    request.getUserIdsList(), authChecker);
        } catch (AuthenticationException e) {
            LOG.error("Authentication failed when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Failed to find user when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        }

        final Identity.UserNameResponse.Builder response = Identity.UserNameResponse.newBuilder();
        for (Map.Entry<String, String> userIdToUserName : userIds.entrySet()) {
            response.addUserNames(Identity.UserNameResponse.MapFieldEntry
                    .newBuilder()
                    .setKey(userIdToUserName.getKey())
                    .setValue(userIdToUserName.getValue())
                    .build());
        }

        done.run(response.build());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the username from the userId.
     */
    @Override public void getUserName(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        Map<String, String> userIdToUserName;
        try {
            userIdToUserName = identityManager
                    .getUserName(request.getUserId(), request.getAuthId(), request.getItemId(), request.getItemType(), authChecker);
        } catch (AuthenticationException e) {
            LOG.error("Authentication failed when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Failed to find user when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        }
        final Identity.UserNameResponse response = Identity.UserNameResponse.newBuilder()
                .addUserNames(Identity.UserNameResponse.MapFieldEntry
                        .newBuilder()
                        .setKey(request.getUserId())
                        .setValue(userIdToUserName.get(request.getUserId()))
                        .build())
                .build();
        done.run(response);
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new user in the identity server.
     */
    @Override public void createNewUser(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        final Map<String, String> userResult;
        try {
            userResult = identityManager.createNewUser(request.getUserId());
        } catch (AuthenticationException | DatabaseAccessException e) {
            LOG.error("Failed to create a new user", e);
            done.run(createErredUserNameResponse(e));
            return;
        }
        final Map.Entry<String, String> idPassEntry = userResult.entrySet().iterator().next();
        final Identity.UserNameResponse response = Identity.UserNameResponse.newBuilder()
                .addUserNames(Identity.UserNameResponse.MapFieldEntry
                        .newBuilder()
                        .setKey(idPassEntry.getKey())
                        .setValue(idPassEntry.getValue())
                        .build())
                .build();
        done.run(response);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the identity of the users based on their participation in the course.
     */
    @Override public void getUserIdentity(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        String identity = null;
        try {
            identity = identityManager.getUserIdentity(request.getUserId(), request.getAuthId());
        } catch (AuthenticationException e) {
            LOG.error("Authentication failed when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Failed to find user when getting user identity", e);
            done.run(createErredUserNameResponse(e));
            return;
        }
        final Identity.UserNameResponse response = Identity.UserNameResponse.newBuilder()
                .addUserNames(Identity.UserNameResponse.MapFieldEntry
                        .newBuilder()
                        .setKey(request.getUserId())
                        .setValue(identity)
                        .build())
                .build();
        done.run(response);
    }

    /**
     * {@inheritDoc}
     *
     * Used to create new items.
     * An item can be a course or an assignment or other parts of a course.
     */
    @Override public void createNewItem(final RpcController controller, final Identity.IdentityCreationRequest request,
            final RpcCallback<Message.DefaultResponse> done) {
        final Identity.IdentityRequest identityRequest = request.getItemRequest();
        try {
            identityManager.createNewItem(identityRequest.getUserId(), identityRequest.getAuthId(), identityRequest.getItemId(),
                    identityRequest.getItemType(), request.getParentItemId(), authChecker);
            done.run(Message.DefaultResponse.getDefaultInstance());
        } catch (DatabaseAccessException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("Failed to access data while inserting new auth data", e);
        } catch (AuthenticationException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("Failed to authenticate user while inserting new auth data", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Registers user for a course or a bank problem.
     */
    @Override public void registerUser(final RpcController controller, final Identity.IdentityRequest identityRequest,
            final RpcCallback<Message.DefaultResponse> done) {
        try {
            identityManager.registerUserInItem(identityRequest.getUserId(), identityRequest.getAuthId(), identityRequest.getItemId(),
                    identityRequest.getItemType(), authChecker);
            done.run(Message.DefaultResponse.getDefaultInstance());
        } catch (DatabaseAccessException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("Failed to access data while registering user.", e);
        } catch (AuthenticationException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("User may not have permission to register for this class.", e);
        }
    }

    /**
     * Creates a username response based on the exception.
     * @param tException The exception thrown
     * @return A username response that has had an exception thrown.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Identity.UserNameResponse createErredUserNameResponse(final Throwable tException) {
        return Identity.UserNameResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(tException)).build();
    }
}
