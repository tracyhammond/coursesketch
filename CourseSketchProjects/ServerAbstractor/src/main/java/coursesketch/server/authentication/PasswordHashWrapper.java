package coursesketch.server.authentication;

import coursesketch.database.auth.AuthenticationException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Implements Java's Sha1 algorithm using pbdkf2.
 */
@Deprecated
class PasswordHashWrapper implements HashWrapper {

    @Override public String algorithmName() {
        return "PASHASH";
    }

    @Override public String hash(final String password) throws AuthenticationException {
        try {
            return PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override public String hash(final String string, final String salt) throws AuthenticationException {
        if (salt.length() < HashManager.MIN_SALT_LENGTH) {
            throw new AuthenticationException("Invalid Salt Format", AuthenticationException.INSUFFICIENT_HASH);
        }
        try {
            return PasswordHash.createHash(string + salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override public boolean validateHash(final String candidate, final String hashedValue)
            throws AuthenticationException {
        try {
            return PasswordHash.validatePassword(candidate, hashedValue);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override public String generateSalt() {
        return PasswordHash.createSalt();
    }
}
