package coursesketch.server.authentication;

import com.google.common.collect.ImmutableMap;
import coursesketch.database.auth.AuthenticationException;
import org.mindrot.BCrypt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

/**
 * Manages the hashing and validation of hashes.
 *
 * This abstracts the use of any one algorithm so the user of hashing does not need to care about what algorithm is used.
 * It also has utility methods for upgrading hashes and converting hashes to Hexadecimal.
 *
 * Created by dtracers on 10/7/2015.
 */
@SuppressWarnings({"PMD.SingularField", "PMD.TooManyMethods" })
public final class HashManager {

    /**
     * The minimum salt length we decide to be secure enough.  (Honestly this is probably too short)
     */
    private static final int MIN_SALT_LENGTH = 10;

    /**
     * Used when converting hashes to hex and back.
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * The current hash algorithm that is being used by course sketch.
     */
    public static final String CURRENT_HASH = new BCryptWrapper().algorithmName();

    /**
     * This was the old hash algorithm that was used before we started storing which hash algorithm we were using.
     */
    public static final String PRE_HASH_STORAGE_HASH = new PasswordHashWrapper().algorithmName();

    /**
     * A map containing all of the hash algorithms we have ever used.
     */
    private static final Map<String, HashWrapper> HASH_FUNCTION_MAP = createHashes();

    /**
     * A character that splits the Hash Algorithm name and the hash.
     */
    public static final char SPLIT_CHAR = '@';

    /**
     * The length that all hash names must adhere to.
     *
     * Do NOT change this value without making sure all new hash names are this length.
     */
    public static final int HASH_NAME_LENGTH = 7;

    /**
     * The length that all real hashes must be longer than.
     */
    private static final int MIN_HASH_LENGTH = HASH_NAME_LENGTH + 1;

    /**
     * Empty constructor.
     */
    private HashManager() {
    }

    /**
     * Creates a hash for the given input.
     *
     * Generates a random salt for use in the creation of the hash.
     * @param password The input that is being hashed. The hash is supposed to be secure enough to be used for passwords.
     * @return A hashed value.
     * @throws NoSuchAlgorithmException Thrown if there are no hashing algorithm available.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    public static String createHash(final String password) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return CURRENT_HASH + SPLIT_CHAR + function.hash(password);
    }

    /**
     * Creates a hash for the given input.
     *
     * Uses the salt in the creation of the hash.
     * @param password The input that is being hashed. The hash is supposed to be secure enough to be used for passwords.
     * @param salt Used if you want to maintain hashing consistancy.  The same salt and candidate should always give the same hash.
     * @return A hashed value.
     * @throws NoSuchAlgorithmException Thrown if there are no hashing algorithm available.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    public static String createHash(final String password, final String salt) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return CURRENT_HASH + SPLIT_CHAR + function.hash(password, salt);
    }

    /**
     * Validates that the {@code candidate} and the {@code hash} are the same value.
     *
     * The {@code candidate} is not hashed.
     *
     * @param candidate The value we are comparing to the hash.
     * @param hash An already hashed value.
     * @return true if there is a match, false otherwise.
     * @throws NoSuchAlgorithmException Thrown if there are no hashing algorithm available.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    public static boolean validateHash(final String candidate, final String hash) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(hash));
        }
        if (hash.length() <= MIN_HASH_LENGTH) {
            throw new AuthenticationException("Candidate Hash is not a valid length", AuthenticationException.INSUFFICIENT_HASH);
        }
        return function.validateHash(candidate, unwrapHash(hash));
    }

    /**
     * Upgrades the password from an old hashing algorithm to a new one.
     *
     * Returns a new hash only if the candidate can be validated by an old hash, otherwise it returns null.
     * {@code candidate} needs to be a correct password.
     * @param candidate The value we are comparing to the hash.
     * @param hash An already hashed value.
     * @return true if there is a match, false otherwise.
     * @throws NoSuchAlgorithmException Thrown if there are no hashing algorithm available.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    public static String upgradeHash(final String candidate, final String hash) throws NoSuchAlgorithmException, AuthenticationException {
        // hash algorithm of hash parameter
        final HashWrapper function = HASH_FUNCTION_MAP.get(getAlgorithmFromHash(hash));
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(hash));
        }
        if (function.validateHash(candidate, unwrapHash(hash))) {
            return createHash(candidate);
        }
        return null;
    }

    /**
     * Returns a string representing the algorithm from the hash.
     *
     * @param hash The hash that we want to get the algorithm of.
     * @return A string representing the algorithm that was used.
     */
    public static String getAlgorithmFromHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return hash.substring(0, HASH_NAME_LENGTH);
        }
        // If the SPLIT_CHAR was not found at the expected location,
        // then the hash uses the algorithm from before algorithms were stored.
        return PRE_HASH_STORAGE_HASH;
    }

    /**
     * Generates a random salt from the current hash algorithm.
     *
     * @return a valid secure salt that can be used in hashing.
     * @throws NoSuchAlgorithmException Thrown if there are no salting algorithms are available.
     */
    public static String generateSalt() throws NoSuchAlgorithmException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return function.generateSalt();
    }

    /**
     * Converts A byte array into a hexadecimal string.
     *
     * @param bytes The byte array to be converted into a hexadecimal string.
     * @return A hexadecimal string.
     */
    @SuppressWarnings({ "checkstyle:magicnumber", "PMD.UseVarargs" })
    public static String toHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int rawByte = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[rawByte >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[rawByte & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts a hex string into a byte array.
     *
     * @param hexString A hexadecimal string that is to be converted into bytes.
     * @return A byte array that is made from the hex characters.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static byte[] fromHex(final String hexString) {
        final int len = hexString.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Returns a hash that does not contain the leading algorithm name.
     *
     * @param hash A hash that starts with an algorithm name.
     * @return a hash that does not contain the leading algorithm name.
     */
    private static String unwrapHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return  hash.substring(HASH_NAME_LENGTH + 1);
        }
        return hash;
    }

    /**
     * Interfaces with the BCrypt library.
     */
    private static class BCryptWrapper implements HashWrapper {
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
            if (salt.length() < MIN_SALT_LENGTH || salt.charAt(0) != SALT_VERSION_CHAR0 || salt.charAt(1) != SALT_VERSION_CHAR1) {
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
    }

    /**
     * Implements Java's Sha1 algorithm using pbdkf2.
     */
    @Deprecated
    private static class PasswordHashWrapper implements HashWrapper {

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
            if (salt.length() < MIN_SALT_LENGTH) {
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

    /**
     * Creates and returns A map of all the hash algorithms that CourseSketch has used.
     *
     * @return A map of all the hash algorithms that CourseSketch has used.
     */
    private static Map<String, HashWrapper> createHashes() {
        final ImmutableMap.Builder mapBuilder = ImmutableMap.builder();

        addHashToMap(mapBuilder, new BCryptWrapper());
        addHashToMap(mapBuilder, new PasswordHashWrapper());

        return mapBuilder.build();
    }

    /**
     * A helper method that adds a hash algorithm to the map.
     *
     * @param mapBuilder Contains all of the hashing algorithms.
     * @param wrapper A specific hash algorithm.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static void addHashToMap(final ImmutableMap.Builder<String, HashWrapper> mapBuilder, final HashWrapper wrapper) {
        if (wrapper.algorithmName().length() != HASH_NAME_LENGTH) {
            throw new AssertionError("All Hash keys must be the same length [" + HASH_NAME_LENGTH + "]");
        }
        mapBuilder.put(wrapper.algorithmName(), wrapper);
    }
}
