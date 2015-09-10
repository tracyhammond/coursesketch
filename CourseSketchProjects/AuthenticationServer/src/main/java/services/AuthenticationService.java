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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.authentication.Authentication;

import java.util.List;

import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.GROUP_PREFIX_LENGTH;

/**
 * Created by gigemjt on 9/3/15.
 */
public final class AuthenticationService extends Authentication.AuthenticationService implements CourseSketchRpcService {

    private ISocketInitializer socketInitializer;

    private AuthenticationDataCreator dataGrabber;

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

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
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void authorizeUser(final RpcController controller, final Authentication.AuthRequest request,
            final RpcCallback<Authentication.AuthResponse> done) {
        try {
            done.run(checkAuthentication(request));
        } catch (AuthenticationException e) {
            controller.setFailed(e.toString());
        }
    }

    private Authentication.AuthResponse checkAuthentication(final Authentication.AuthRequest request) throws AuthenticationException {
        if (!Authenticator.validRequest(request.getAuthParams())) {
            throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
        }
        Authentication.AuthType checkType = request.getAuthParams();

        final AuthenticationData result;
        try {
            result = dataGrabber.getAuthGroups(request.getItemType(), request.getItemId());
        } catch (DatabaseAccessException e) {
            throw new AuthenticationException(e);
        }
        Authentication.AuthResponse.Builder response = Authentication.AuthResponse.newBuilder();

        boolean validAccess = authenticateUser(request.getAuthId(), result, request.getAuthParams());

        final boolean validUser = checkType.getCheckingUser() && validAccess;
        response.setIsUser(validUser);

        final boolean validMod = authenticateModerator(request.getAuthId(), result, checkType);
        // checking for access before we change
        validAccess |= validMod;

        // only return true if we are checking it is a mod.
        response.setIsMod(validMod && (checkType.getCheckingMod() || checkType.getCheckAdminOrMod()));

        final boolean validAdmin = authenticateAdmin(request.getAuthId(), result, checkType);
        validAccess |= validAdmin;

        response.setIsAdmin(validAdmin && (checkType.getCheckingAdmin() || checkType.getCheckAdminOrMod()));

        // access is granted to everyone if registration is not required.
        validAccess |= !result.isRegistrationRequired();
        response.setHasAccess(validAccess && checkType.getCheckAccess());

        return response.build();
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
