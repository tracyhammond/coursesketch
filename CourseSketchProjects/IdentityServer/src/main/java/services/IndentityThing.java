package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.identity.Identity;

/**
 * Created by gigemjt on 9/3/15.
 */
public class IndentityThing extends Identity.IdentityService {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IndentityThing.class);

    /**
     * <code>rpc requestUserNames(.protobuf.srl.services.identity.UserNameRequest) returns (.protobuf.srl.services.identity.UserNameResponse);</code>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void requestUserNames(final RpcController controller, final Identity.UserNameRequest request,
            final RpcCallback<Identity.UserNameResponse> done) {
        LOG.info("REQUESTING IDENTITY {}", request);
    }
}
