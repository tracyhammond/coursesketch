package coursesketch.server.authentication;

/**
 * Created by dtracers on 12/24/2015.
 */

import coursesketch.database.auth.AuthenticationException;
import org.mindrot.BCrypt;

/**
 * Interfaces with the BCrypt library.
 */
class BCryptWrapper implements HashWrapper {
    /**
     * Number of rounds used when generating salt.
     */
    private static final int LOG_ROUNDS = 12;

    /**
     * The character that represents the bycrypt version.
     */
    private static final int SALT_VERSION_CHAR0 = 36;

    /**
     * The character that represents the bycrypt version.
     */
    private static final int SALT_VERSION_CHAR1 = 50;

    @Override public String algorithmName() {
        return "BYCRYPT";
    }

    @Override public String hash(final String password)throws AuthenticationException {
        return BCrypt.hashpw(password, generateSalt());
    }

    @Override public String hash(final String string, final String salt) throws AuthenticationException {
        if (salt.length() < HashManager.MIN_SALT_LENGTH || salt.charAt(0) != SALT_VERSION_CHAR0 || salt.charAt(1) != SALT_VERSION_CHAR1) {
            throw new AuthenticationException("Invalid Salt Format", AuthenticationException.INSUFFICIENT_HASH);
        }
        return BCrypt.hashpw(string, salt);
    }

    @Override public boolean validateHash(final String candidate, final String hashedValue) {
        try {
            return BCrypt.checkpw(candidate, hashedValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override public String generateSalt() {
        return BCrypt.gensalt(LOG_ROUNDS);
    }

    /**
     * @param originalSalt
     *         The salt the generated one is derived from.  It is completely deterministic based on the original salt.
     * @return a valid secure salt that can be used in hashing.
     * This salt is not secure and should not be used in passwords.
     */
    @Override public String generateUnsecuredSalt(final String originalSalt) {
        return BCrypt.gensalt(LOG_ROUNDS, new UnsecuredRandom(originalSalt));
    }
}
