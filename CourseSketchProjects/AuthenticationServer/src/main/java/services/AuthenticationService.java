package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import database.DatabaseAccessException;
import database.auth.AuthenticationData;
import database.auth.AuthenticationDataCreator;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.DbAuthChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.services.authentication.Authentication;

import java.util.List;

import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.GROUP_PREFIX_LENGTH;

/**
 * Created by gigemjt on 9/3/15.
 */
public final class AuthenticationService extends Authentication.AuthenticationService implements CourseSketchRpcService {

    private ISocketInitializer socketInitializer;


    private final DbAuthChecker authChecker;

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(final DbAuthChecker authChecker) {
        this.authChecker = authChecker;
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializer
     */
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        if (socketInitializer != null) {
            this.socketInitializer = socketInitializer;
        }
    }

    /**
     * <code>rpc authorizeUser(.protobuf.srl.services.authentication.AuthRequest) returns (.protobuf.srl.services.authentication.AuthResponse);</code>
     * Authorizes the user to have access to the data.
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void authorizeUser(final RpcController controller, final Authentication.AuthRequest request,
            final RpcCallback<Authentication.AuthResponse> done) {
        try {
            done.run(authChecker.isAuthenticated(request.getItemType(), request.getItemId(), request.getAuthId(), request.getAuthParams()));
        } catch (DatabaseAccessException e) {
            controller.setFailed(e.toString());
            LOG.error("Failed to authenticate", e);
        } catch (AuthenticationException e) {
            controller.setFailed(e.toString());
            LOG.error("Failed to authenticate", e);
        }
    }

    /**
     * <code>rpc createNewItem(.protobuf.srl.services.authentication.AuthCreationRequest) returns (.protobuf.srl.request.DefaultResponse);</code>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void createNewItem(final RpcController controller, final Authentication.AuthCreationRequest request,
            final RpcCallback<Message.DefaultResponse> done) {

    }
}
