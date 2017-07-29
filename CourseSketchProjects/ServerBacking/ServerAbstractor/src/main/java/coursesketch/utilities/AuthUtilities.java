package coursesketch.utilities;

import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.authentication.HashManager;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Contains utilities for {@link protobuf.srl.services.authentication.Authentication.AuthType}.
 *
 * Created by dtracers on 9/16/2015.
 */
@SuppressWarnings({ "checkstyle:magicnumber", "PMD.TooManyMethods" })
public final class AuthUtilities {

    /**
     * Empty constructor.
     */
    private AuthUtilities() {
    }

    /**
     * Converts the checktype to make it cumulative.
     *
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
        builder.setCheckingOwner(preFixedCheckType.getCheckingOwner());
        return builder.build();
    }

    /**
     * Converts the checktype to make it a cumulative permission check.
     *
     * Only returns the most restrictive permission level allowed.
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
     * Creates an Authtype to make it a cumulative permission check.
     *
     * Only returns the most restrictive permission level allowed.
     * @param permissionLevel an AuthType that may not be cumulative.
     * @return The largest level that is asked for by the given check type.
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public static Authentication.AuthType.Builder createAuthTypeCheckFromLevel(final Authentication.AuthResponse.PermissionLevel permissionLevel) {
        final Authentication.AuthType.Builder builder = Authentication.AuthType.newBuilder();
        switch (permissionLevel) {
            case TEACHER:
                builder.setCheckingAdmin(true);
            case MODERATOR:
                builder.setCheckingMod(true);
            case PEER_TEACHER:
                builder.setCheckingPeerTeacher(true);
            case STUDENT:
                builder.setCheckingUser(true);
                break;
            default:
                // do nothing
        }
        return builder;
    }

    /**
     * Returns a long that contains all of the values in the auth check combined.
     *
     * @param preFixedCheckType An AuthType that may not be cumulative.
     * @return A long that represents the combined value of all permissions that were set by the checktype.
     */
    private static long mergeCheckTypes(final Authentication.AuthType preFixedCheckType) {
        return createAccessValue(preFixedCheckType.getCheckAccess())
                | createUserValue(preFixedCheckType.getCheckingUser())
                | createPeerTeacherValue(preFixedCheckType.getCheckingPeerTeacher())
                | createModValue(preFixedCheckType.getCheckingMod())
                | createAdminValue(preFixedCheckType.getCheckingAdmin());
    }

    /**
     * Creates the long representation of a true check for access.
     *
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00001} if given true.
     */
    private static long createAccessValue(final boolean value) {
        return convertAndShift(value, 0);
    }

    /**
     * Creates the long representation of a true check for a user level of permission.
     *
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00010} if given true.
     */
    private static long createUserValue(final boolean value) {
        return convertAndShift(value, 1);
    }

    /**
     * Creates the long representation of a true check for a peerteacher level of permission.
     *
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b00100} if given true.
     */
    private static long createPeerTeacherValue(final boolean value) {
        return convertAndShift(value, 2);
    }

    /**
     * Creates the long representation of a true check for a moderator level of permission.
     *
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b01000} if given true.
     */
    private static long createModValue(final boolean value) {
        return convertAndShift(value, 3);
    }

    /**
     * Creates the long representation of a true check for an admin (teacher) level of permission.
     *
     * @param value true if this should return a bitshifted 1
     * @return {@code 0b10000} if given true.
     */
    private static long createAdminValue(final boolean value) {
        return convertAndShift(value, 4);
    }

    /**
     * Converts the boolean to a number then shifts it.
     *
     * @param bool If false, zero is always returned.
     * @param shiftAmount the amount to shift the result by.
     * @return A number that is based on the input.
     */
    static long convertAndShift(final boolean bool, final int shiftAmount) {
        return (bool ? 1 : 0) << shiftAmount;
    }

    /**
     * Generates a salt and throws only AuthenticationException.
     *
     * @return A string that can be used for salting.
     * @throws AuthenticationException Thrown for multiple reasons.
     */
    public static String generateAuthSalt() throws AuthenticationException {
        try {
            return HashManager.generateSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Generates a hash and throws only AuthenticationException.
     *
     * @param authId The id being hashed.
     * @param salt The salt of the hash.
     * @return A hashed string.
     * @throws AuthenticationException Thrown for multiple reasons.
     */
    public static String generateHash(final String authId, final String salt) throws AuthenticationException {
        try {
            return HashManager.toHex(HashManager.createHash(authId, salt).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Creates an empty authentication checker that only throws exceptions when the methods are called.
     * @return Creates an instance of the {@link AuthenticationOptionChecker} that throws an exception when any of its methods are called.
     */
    public static AuthenticationOptionChecker createThrowingAuthenticationOptionChecker() {
        return new AuthenticationOptionChecker() {
            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime)
                    throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public AuthenticationDataCreator createDataGrabber(final Util.ItemType collectionType, final String itemId)
                    throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }
        };
    }
}
