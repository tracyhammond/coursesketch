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
     * @param preFixedCheckType an AuthType that may not be cumulative.
     * @return An {@link protobuf.srl.services.authentication.Authentication.AuthType} that is cumulative.
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
     * @param checkType an AuthType that may not be cumulative.
     * @return The largest level that is asked for by the given check type.
     */
    public static Authentication.AuthResponse.PermissionLevel largestAllowedLevel(final Authentication.AuthType checkType) {
        if (checkType.getCheckingAdmin()) {
            return Authentication.AuthResponse.PermissionLevel.TEACHER;
        }
        if (checkType.getCheckingMod()) {
            return Authentication.AuthResponse.PermissionLevel.MODERATOR;
        }
        if (checkType.getCheckingPeerTeacher()) {
            return Authentication.AuthResponse.PermissionLevel.PEER_TEACHER;
        }
        if (checkType.getCheckingUser()) {
            return Authentication.AuthResponse.PermissionLevel.STUDENT;
        }
        return Authentication.AuthResponse.PermissionLevel.NO_PERMISSION;
    }

    /**
     * returns a long that contains all of the values in the auth check combined.
     * @param preFixedCheckType an AuthType that may not be cumulative.
     * @return a long that represents the combined value of all permissions that were set by the checktype.
     */
    private static long mergeCheckTypes(final Authentication.AuthType preFixedCheckType) {
        return createAccessValue(preFixedCheckType.getCheckAccess())
                | createUserValue(preFixedCheckType.getCheckingUser())
                | createPeerTeacherValue(preFixedCheckType.getCheckingPeerTeacher())
                | createModValue(preFixedCheckType.getCheckingMod())
                | createAdminValue(preFixedCheckType.getCheckingAdmin());
    }

    /**
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00001} if given true.
     */
    private static long createAccessValue(final boolean value) {
        return convertAndShift(value, 0);
    }

    /**
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00010} if given true.
     */
    private static long createUserValue(final boolean value) {
        return convertAndShift(value, 1);
    }

    /**
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00100} if given true.
     */
    private static long createPeerTeacherValue(final boolean value) {
        return convertAndShift(value, 2);
    }

    /**
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b01000} if given true.
     */
    private static long createModValue(final boolean value) {
        return convertAndShift(value, 3);
    }

    /**
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b10000} if given true.
     */
    private static long createAdminValue(final boolean value) {
        return convertAndShift(value, 4);
    }

    /**
     * Converts the boolean to a number then shifts it.
     *
     * @param bool if falze zero is always returned.
     * @param shiftAmount the amount to shift the result by.
     * @return A number that is based on the input.
     */
    public static long convertAndShift(final boolean bool, final int shiftAmount) {
        return (bool ? 1 : 0) << shiftAmount;
    }
}
