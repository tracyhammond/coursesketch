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

    /**
     * Checks to see if a user is allowed within the current groupList.
     *
     * looks up the groupId if there are groupId associated with this groupList
     *
     * @param userId
     *            the userId within the program that is trying to be
     *            authenticated
     * @param groups The list of names to check if the user exist in.
     * @return True if the userId is in the list of the group.
     */
    private boolean checkAuthentication(final String userId, final List<String> groups) {
        if (groups == null) {
            return false;
        }
        for (String group : groups) {
            if (group.startsWith(GROUP_PREFIX)) {
                final List<String> list = dataGrabber.getUserList(group.substring(GROUP_PREFIX_LENGTH));
                if (checkAuthentication(userId, list)) {
                    return true;
                }
            } else {
                if (group.equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Authenticates just the user list.
     * @param userId the user being authenticated.
     * @param result contains the user list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the user is not authenticated.
     */
    private boolean authenticateUser(final String userId, final AuthenticationData result, final Authentication.AuthType checkType) {
        boolean validUser = false;
        if (checkType.getCheckingUser() || checkType.getCheckAccess()) {
            final List usersList = result.getUserList();
            validUser = this.checkAuthentication(userId, usersList);
        }
        return validUser;
    }

    /**
     * Authenticates just the moderator list.
     * @param userId the user being authenticated.
     * @param result contains the moderator list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the moderator is not authenticated.
     */
    private boolean authenticateModerator(final String userId, final AuthenticationData result, final Authentication.AuthType checkType) {
        boolean validMod = false;
        if (checkType.getCheckingMod() || checkType.getCheckAdminOrMod() || checkType.getCheckAccess()) {
            final List modList = result.getModeratorList();
            validMod = this.checkAuthentication(userId, modList);
        }
        return validMod;
    }

    /**
     * Authenticates just the admin list.
     * @param userId the user being authenticated.
     * @param result contains the admin list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the admin is not authenticated.
     */
    private boolean authenticateAdmin(final String userId, final AuthenticationData result, final Authentication.AuthType checkType) {
        boolean validAdmin = false;
        if (checkType.getCheckingAdmin() || checkType.getCheckAdminOrMod() || checkType.getCheckAccess()) {
            final List adminList = result.getAdminList();
            validAdmin = this.checkAuthentication(userId, adminList);
        }
        return validAdmin;
    }

    public void setDataGrabber(AuthenticationDataCreator dataGrabber) {
        if (dataGrabber == null) {
            this.dataGrabber = dataGrabber;
        }
    }
}
