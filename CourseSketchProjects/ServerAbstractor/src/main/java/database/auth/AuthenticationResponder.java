package database.auth;

import protobuf.srl.services.authentication.Authentication;

/**
 * Created by gigemjt on 9/4/15.
 */
public class AuthenticationResponder {
    private final Authentication.AuthResponse response;

    public AuthenticationResponder(Authentication.AuthResponse response) {
        this.response = response;
    }

    /**
     * This is mainly if the person has access to the item itself in any way.
     * This does NOT mean access to everything.
     * @return
     */
    public boolean hasAccess() {
        return response.getHasAccess();
    }

    public boolean hasStudentPermission() {
        return response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE;
    }

    public boolean isItemOpen() {
        return response.getIsItemOpen();
    }

    public boolean hasPeerTeacherPermission() {
        return response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.PEER_TEACHER_VALUE;
    }

    public boolean hasModeratorPermission() {
        return response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE;
    }

    public boolean hasTeacherPermission() {
        return response.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE;
    }
}
