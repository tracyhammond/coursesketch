package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.identity.Identity;

/**
 * Created by gigemjt on 9/3/15.
 */
public final class IdentityThing extends Identity.IdentityService implements CourseSketchRpcService {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityThing.class);

    /**
     * socket initializer.
     */
    private ISocketInitializer socketInitializer;

    /**
     * <code>rpc requestUserNames(.protobuf.srl.services.identity.UserNameRequest) returns (.protobuf.srl.services.identity.UserNameResponse);</code>.
     *
     * @param controller asdf
     * @param request asf
     * @param done asdf
     */
    @Override public void requestUserNames(final RpcController controller, final Identity.UserNameRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        LOG.info("REQUESTING IDENTITY {}", request);
        LOG.debug("SOCKET STARTER {}", socketInitializer);
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializerIn The socket that created this server.
     */
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializerIn) {
        if (socketInitializerIn != null) {
            this.socketInitializer = socketInitializerIn;
        }
    }
}
