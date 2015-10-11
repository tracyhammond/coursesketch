package coursesketch.server.authentication;

import com.google.common.collect.ImmutableMap;
import coursesketch.database.auth.AuthenticationException;
import org.mindrot.BCrypt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

/**
 * Created by dtracers on 10/7/2015.
 */
@SuppressWarnings({"PMD.SingularField", "PMD.TooManyMethods" })
public class HashManager {

    /**
     * The minimum salt length we deam to be secure enough.  (Honestly this is problem too short)
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
     */
    public static final int HASH_NAME_LENGTH = 7;

    /**
     * The length that all real hashes must be longer than.
     */
    private static final int MIN_HASH_LENGTH = HASH_NAME_LENGTH + 1;

    /**
     * Creates a hash for the given input.
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
     * Creates a hash for the given input.  Uses the salt
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
     * Validates that the canidate and the hash are the same value.
     *
     * @param candidate The value we are checking to see if it matches the hash.
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
     * Returns a new hash only if the candidate can be validated by an old password, otherwise it returns null.
     * @param candidate The value we are checking to see if it matches the hash.
     * @param hash An already hashed value.
     * @return true if there is a match, false otherwise.
     * @throws NoSuchAlgorithmException Thrown if there are no hashing algorithm available.
     * @throws AuthenticationException Thrown if there is a problem creating a secure hash.
     */
    public static String upgradeHash(final String candidate, final String hash) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(getAlgorithmFromHash(hash)); // latest hash function
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
     * @param hash The hash that we want to get the algorithm of.
     * @return A string representing the algorithm that was used.
     */
    public static String getAlgorithmFromHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return  hash.substring(0, HASH_NAME_LENGTH);
        }
        return PRE_HASH_STORAGE_HASH;
    }

    /**
     * @return a valid secure salt that can be used in hashing.
     * @throws NoSuchAlgorithmException Thrown if there are no salting algorithm's available.
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
     * @param bytes The byte array to be converted into a hexadecimal string
     * @return A string that only contains hex characters
     */
    public static String toHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts a hex string into a binary array.
     * @param s A hex string that is to be converted into bytes.
     * @return A byte that is made from the hex characters
     */
    public static byte[] fromHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Returns a hash that does not contain the leading algorithm name.
     * @param hash A hash that starts with an algorithm name.
     * @return a hash that does not contain the leading algorithm name.
     */
    private static String unwrapHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return  hash.substring(HASH_NAME_LENGTH + 1);
        }
        return hash;
    }

    private static class BCryptWrapper implements HashWrapper {
        private static final int LOG_ROUNDS = 12;
        private static final int SALT_VERSION_CHAR0 = 36;
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

    private static Map<String, HashWrapper> createHashes() {
        final ImmutableMap.Builder mapBuilder = ImmutableMap.builder();

        addHashToMap(mapBuilder, new BCryptWrapper());
        addHashToMap(mapBuilder, new PasswordHashWrapper());

        return mapBuilder.build();
    }

    private static void addHashToMap(final ImmutableMap.Builder<String, HashWrapper> mapBuilder, final HashWrapper wrapper) {
        if (wrapper.algorithmName().length() != HASH_NAME_LENGTH) {
            throw new AssertionError("All Hash keys must be the same length [" + HASH_NAME_LENGTH + "]");
        }
        mapBuilder.put(wrapper.algorithmName(), wrapper);
    }
}
