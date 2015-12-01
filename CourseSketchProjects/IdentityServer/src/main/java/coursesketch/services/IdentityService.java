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

/**
 * Created by gigemjt on 9/3/15.
 */
public final class IdentityService extends Identity.IdentityService implements CourseSketchRpcService {

    /**
     * Manages authentication storage for this service.
     */
    private final IdentityManager identityManager;

    /**
     * Used for checking if users have permissions to access certain values.
     */
    private final Authenticator authChecker;

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityService.class);

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
    @Override public void requestCourseRoster(final RpcController controller, final Identity.RequestRoster request,
            final RpcCallback<Identity.UserNameResponse> done) {
        // does nothing yet
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new user in the identity server.
     */
    @Override public void createNewUser(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        final String userId = identityManager.createNewUser(request.getUserId());
        final Identity.UserNameResponse response = Identity.UserNameResponse.newBuilder()
                .addUserNames(Identity.UserNameResponse.MapFieldEntry
                        .newBuilder()
                        .setKey(request.getUserId())
                        .setValue(userId)
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
            final RpcCallback<Message.DefaultResponse> done) {
        // does nothing yet
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
            identityManager.insertNewItem(identityRequest.getUserId(), identityRequest.getAuthId(), identityRequest.getItemId(),
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
            identityManager.registerSelf(identityRequest.getUserId(), identityRequest.getAuthId(), identityRequest.getItemId(),
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
}
