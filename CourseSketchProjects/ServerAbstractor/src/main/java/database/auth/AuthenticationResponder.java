package database.auth;

import protobuf.srl.services.authentication.Authentication;

/**
 * Created by gigemjt on 9/4/15.
 * This works on the assumption of accumulative permissions.
 * So as you go higher up the list you can not lose permissions.
 */
public class AuthenticationResponder {
    private final Authentication.AuthResponse response;

    public AuthenticationResponder(Authentication.AuthResponse response) {
        this.response = response;
    }

    /**
     * This returns true IFF registration is not required OR the user has a permission level greater than student
     * OR the auth checker detirmines the user has access
     * @return
     */
    public boolean hasAccess() {
        return response.getHasAccess();
    }

    /**
     * This is true if it is in the valid dates OR the instructor has said access is allowed even after those dates!
     * @return
     */
    public boolean isItemOpen() {
        return response.hasIsItemPublished() && response.getIsItemOpen();
    }

    public boolean isItemPublished() {
        return response.hasIsItemPublished() && response.getIsItemPublished();
    }

    public boolean isRegistrationRequired() {
        // if we do not have a value for registration we assume the more restrictive option.
        // this is the opposite of most values so this method is required
        // even though the default is true...
        return !response.hasIsRegistrationRequired() || response.getIsRegistrationRequired();
    }

    public boolean hasStudentPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE;
    }

    public boolean hasPeerTeacherPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.PEER_TEACHER_VALUE;
    }

    public boolean hasModeratorPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE;
    }

    public boolean hasTeacherPermission() {
        return response.hasPermissionLevel()
                && response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE;
    }
}
