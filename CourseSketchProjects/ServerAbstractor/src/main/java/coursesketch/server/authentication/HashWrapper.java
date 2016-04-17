package coursesketch.server.authentication;

import coursesketch.database.auth.AuthenticationException;


/**
 * Wraps around hash algorithms so they all present a similar interface.
 *
 * Created by dtracers on 10/7/2015.
 */
public interface HashWrapper {
    /**
     * @return The name of the algorithm.  This must be 7 characters long.
     */
    String algorithmName();

    /**
     * Creates a hash for the given input.
     *
     * Does not use a salt or creates its own salt.
     * @param string String we want to hash
     * @return A hashed value of the given string.
     * @throws AuthenticationException Thrown if there is a problem creating the hash.
     */
    String hash(String string) throws AuthenticationException;

    /**
     * Creates a hash for the given input.
     *
     * Uses the salt in the creation of the hash.
     * @param string The input that is being hashed. The hash is supposed to be secure enough to be used for passwords.
     * @param salt Used if you want to maintain hashing consistancy.  The same salt and candidate should always give the same hash.
     * @return A hashed value.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    String hash(String string, String salt) throws AuthenticationException;



    /**
     * Validates that the {@code candidate} and the {@code hashedValue} are the same value.
     *
     * The {@code candidate} is not hashed.
     *
     * @param candidate The value we are comparing to the hash.
     * @param hashedValue An already hashed value.
     * @return true if there is a match, false otherwise.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    boolean validateHash(String candidate, String hashedValue) throws AuthenticationException;

    /**
     * Creates and returns a salt that can be used in hashing.
     *
     * @return a valid secure salt that can be used in hashing.
     */
    String generateSalt();

    /**
     * @return a valid secure salt that can be used in hashing.
     * This salt is not secure and should not be used in passwords.
     * @param originalSalt The salt the generated one is derived from.  It is completely deterministic based on the original salt
     */
    String generateUnsecuredSalt(String originalSalt);
}
