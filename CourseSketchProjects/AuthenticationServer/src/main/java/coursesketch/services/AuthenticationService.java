package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.DbAuthChecker;
import coursesketch.database.auth.DbAuthManager;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.utilities.ExceptionUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.services.authentication.Authentication;

/**
 * Manages RPC messages for authentication purposes.
 *
 * Created by gigemjt on 9/3/15.
 */
public final class AuthenticationService extends Authentication.AuthenticationService implements CourseSketchRpcService {

    /**
     * Manages authentication storage for this service.
     */
    private final DbAuthManager authManager;

    /**
     * Used for checking if users have permissions to access certain values.
     */
    private final DbAuthChecker authChecker;

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Creates an authentication service with a DbAuthManager and a DbAuthChecker.
     *
     * @param authChecker {@link #authChecker}
     * @param authManager {@link #authManager}
     */
    public AuthenticationService(final DbAuthChecker authChecker, final DbAuthManager authManager) {
        this.authChecker = authChecker;
        this.authManager = authManager;
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializer The object used to initialize the sockets.
     */
    @Override
    public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        // Defined by other implementations.
    }

    /**
     * {@inheritDoc}
     *
     * Used to check if a user has access to a certain item.
     *
     * An item can be a course or an assignment or other parts of a course.
     */
    @Override
    public void authorizeUser(final RpcController controller, final Authentication.AuthRequest request,
            final RpcCallback<Authentication.AuthResponse> done) {
        try {
            done.run(authChecker.isAuthenticated(request.getItemType(), request.getItemId(), request.getAuthId(), request.getAuthParams()));
        } catch (DatabaseAccessException | AuthenticationException e) {
            done.run(Authentication.AuthResponse.newBuilder().setPermissionLevel(Authentication.AuthResponse.PermissionLevel.NO_PERMISSION)
                    .setIsRegistrationRequired(true)
                    .setDefaultResponse(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e))).build());
            controller.setFailed(e.toString());
            LOG.error("Failed to authenticate", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Used to create new items.
     * An item can be a course or an assignment or other parts of a course.
     */
    @Override
    public void createNewItem(final RpcController controller, final Authentication.AuthCreationRequest request,
            final RpcCallback<Message.DefaultResponse> done) {
        final Authentication.AuthRequest authRequest = request.getItemRequest();
        try {
            authManager.insertNewItem(authRequest.getAuthId(), authRequest.getItemId(), authRequest.getItemType(), request.getParentItemId(),
                    request.getRegistrationKey(), authChecker);
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
    @Override
    public void registerUser(final RpcController controller, final Authentication.UserRegistration request,
            final RpcCallback<Message.DefaultResponse> done) {
        final Authentication.AuthRequest authRequest = request.getItemRequest();
        try {
            authManager.registerSelf(authRequest.getAuthId(), authRequest.getItemId(), authRequest.getItemType(),
                    request.getRegistrationKey());
            done.run(Message.DefaultResponse.getDefaultInstance());
        } catch (DatabaseAccessException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("Failed to access data while registering user.", e);
        } catch (AuthenticationException e) {
            done.run(Message.DefaultResponse.newBuilder().setException(ExceptionUtilities.createProtoException(e)).build());
            LOG.error("User may not have permission to register for this class.", e);
        }
    }

    @Override
    public void addUser(final RpcController controller, final Authentication.UserRegistration request,
            final RpcCallback<Message.DefaultResponse> done) {
        final Authentication.AuthRequest authRequest = request.getItemRequest();
        try {
            authManager.addUser(request.getRegistrationKey(), authRequest.getAuthId(), authRequest.getItemId(),
                    authRequest.getItemType(), authChecker, request.getItemRequest().getAuthParams());
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
