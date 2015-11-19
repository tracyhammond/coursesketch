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
     * <code>rpc requestCourseRoster(.protobuf.srl.services.identity.RequestRoster) returns (.protobuf.srl.services.identity.UserNameResponse);</code>
     *
     * <pre>
     * *
     * Requests the roster.  Depending on permissions the usernames may not be returned
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void requestCourseRoster(final RpcController controller, final Identity.RequestRoster request,
            final RpcCallback<Identity.UserNameResponse> done) {

    }

    /**
     * <code>rpc createNewUser(.protobuf.srl.services.identity.IdentityRequest) returns (.protobuf.srl.request.DefaultResponse);</code>
     *
     * <pre>
     * *
     * Creates a new user then returns if the creation was successful or not.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void createNewUser(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Message.DefaultResponse> done) {

    }

    /**
     * <code>rpc getUserIdentity(.protobuf.srl.services.identity.IdentityRequest) returns (.protobuf.srl.request.DefaultResponse);</code>
     *
     * <pre>
     * *
     * Creates a new user then returns if the creation was successful or not.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void getUserIdentity(final RpcController controller, final Identity.IdentityRequest request,
            final RpcCallback<Message.DefaultResponse> done) {

    }

    /**
     * <code>rpc createNewItem(.protobuf.srl.services.identity.IdentityCreationRequest) returns (.protobuf.srl.request.DefaultResponse);</code>
     *
     * <pre>
     *
     * Creates a new set of item permissions.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
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
     * <code>rpc registerUser(.protobuf.srl.services.identity.IdentityRequest) returns (.protobuf.srl.request.DefaultResponse);</code>
     *
     * <pre>
     * *
     * Registers the user in the item.
     * </pre>
     *
     * @param controller
     * @param identityRequest
     * @param done
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
