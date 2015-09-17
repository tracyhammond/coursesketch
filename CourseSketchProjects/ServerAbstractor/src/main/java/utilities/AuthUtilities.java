package utilities;

import protobuf.srl.services.authentication.Authentication;

/**
 * Created by dtracers on 9/16/2015.
 */
@SuppressWarnings("checkstyle:magicnumber")
public final class AuthUtilities {
    /**
     * Converts the checktype to make it cumulative.
     * <p/>
     * So if a checktype with only admin set is passed in the output would contain all of the permission below it.
     * @param preFixedCheckType
     * @return
     */
    public static Authentication.AuthType fixCheckType(final Authentication.AuthType preFixedCheckType) {
        final Authentication.AuthType.Builder builder = Authentication.AuthType.newBuilder();
        final long mergedCheckValues = mergeCheckTypes(preFixedCheckType);
        builder.setCheckAccess(mergedCheckValues >= createAccessValue(true));
        builder.setCheckingUser(mergedCheckValues >= createUserValue(true));
        builder.setCheckingPeerTeacher(mergedCheckValues >= createPeerTeacherValue(true));
        builder.setCheckingMod(mergedCheckValues >= createModValue(true));
        builder.setCheckingAdmin(mergedCheckValues >= createAdminValue(true));
        return builder.build();
    }

    /**
     * Converts the checktype to make it a cumulative permission check.
     * <p/>
     * only returns the most restrictive permission level allowed
     * @param cumulativeCheckType
     * @return
     */
    public static Authentication.AuthResponse.PermissionLevel largestAllowedLevel(final Authentication.AuthType checkType) {
        if (cumulativeCheckType.getCheckingAdmin()) {
            return Authentication.AuthResponse.PermissionLevel.TEACHER;
        }
        if (cumulativeCheckType.getCheckingMod()) {
            return Authentication.AuthResponse.PermissionLevel.MODERATOR;
        }
        if (cumulativeCheckType.getCheckingPeerTeacher()) {
            return Authentication.AuthResponse.PermissionLevel.PEER_TEACHER;
        }
        if (cumulativeCheckType.getCheckingUser()) {
            return Authentication.AuthResponse.PermissionLevel.STUDENT;
        }
        return Authentication.AuthResponse.PermissionLevel.NO_PERMISSION;
    }

    private static long mergeCheckTypes(final Authentication.AuthType preFixedCheckType) {
        return createAccessValue(preFixedCheckType.getCheckAccess())
                | createUserValue(preFixedCheckType.getCheckingUser())
                | createPeerTeacherValue(preFixedCheckType.getCheckingPeerTeacher())
                | createModValue(preFixedCheckType.getCheckingMod())
                | createAdminValue(preFixedCheckType.getCheckingAdmin());
    }

    private static long createAccessValue(final boolean value) {
        return convertAndShift(value, 0);
    }

    private static long createUserValue(final boolean value) {
        return convertAndShift(value, 1);
    }

    private static long createPeerTeacherValue(final boolean value) {
        return convertAndShift(value, 2);
    }

    private static long createModValue(final boolean value) {
        return convertAndShift(value, 3);
    }

    private static long createAdminValue(final boolean value) {
        return convertAndShift(value, 4);
    }

    public static long convertAndShift(final boolean bool, int shiftAmount) {
        return (bool ? 1 : 0) << shiftAmount;
    }
}
