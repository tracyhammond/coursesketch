package coursesketch.database.auth;

import protobuf.srl.services.authentication.Authentication;

import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.MODERATOR;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.PEER_TEACHER;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.STUDENT;
import static protobuf.srl.services.authentication.Authentication.AuthResponse.PermissionLevel.TEACHER;

/**
 * Created by gigemjt on 9/4/15.
 * This works on the assumption of accumulative permissions.
 * So as you go higher up the list you can not lose permissions.
 */
public class AuthenticationResponder {
    /**
     * The protobuf object backing the responder.
     */
    private final Authentication.AuthResponse response;

    /**
     * Creates an {@code AuthenticationResponder} with the given protobuf object.
     * @param response The response that was passed in.
     */
    public AuthenticationResponder(final Authentication.AuthResponse response) {
        this.response = response;
    }

    /**
     * @return  true IFF registration is not required OR the user has a permission level greater than student
     * OR the auth checker determines the user has access.  By default this returns false.
     */
    public final boolean hasAccess() {
        return response.getHasAccess();
    }

    /**
     * @return true iff if the item is within the open dates.  By default this returns false.
     */
    public final boolean isItemOpen() {
        return response.getIsItemOpen();
    }

    /**
     * @return True if the item is published.  By default this returns false.
     */
    public final boolean isItemPublished() {
        return response.hasIsItemPublished() && response.getIsItemPublished();
    }

    /**
     * @return true if registration is required for the item.  By default this returns true.
     */
    public final boolean isRegistrationRequired() {
        // if we do not have a value for registration we assume the more restrictive option.
        // this is the opposite of most values so this method is required
        // even though the default is true...
        return !response.hasIsRegistrationRequired() || response.getIsRegistrationRequired();
    }

    /**
     * @return True if the permission level is at least the level of a {@link Authentication.AuthResponse.PermissionLevel#STUDENT}.
     * By default this returns false.
     */
    public final boolean hasStudentPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().compareTo(STUDENT) >= 0;
    }

    /**
     * @return True if the permission level is at least the level of a {@link Authentication.AuthResponse.PermissionLevel#PEER_TEACHER}.
     *         By default this returns false.
     */
    public final boolean hasPeerTeacherPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().compareTo(PEER_TEACHER) >= 0;
    }

    /**
     * @return True if the permission level is at least the level of a {@link Authentication.AuthResponse.PermissionLevel#MODERATOR}.
     * By default this returns false.
     */
    public final boolean hasModeratorPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().compareTo(MODERATOR) >= 0;
    }

    /**
     * @return True if the permission level is at least the level of a {@link Authentication.AuthResponse.PermissionLevel#TEACHER}.
     * By default this returns false.
     */
    public final boolean hasTeacherPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().compareTo(TEACHER) >= 0;
    }
}
