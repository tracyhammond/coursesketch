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
public class HashManager {

    private static final int MIN_SALT_LENGTH = 10;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * The current hash algorithm that is being used by course sketch.
     */
    public static final String CURRENT_HASH = new BCryptWrapper().algorithmName();
    public static final String PRE_HASH_STORAGE_HASH = new PasswordHashWrapper().algorithmName();
    private static final Map<String, HashWrapper> HASH_FUNCTION_MAP = createHashes();
    public static final char SPLIT_CHAR = '@';
    /**
     * The length that all hash names must adhere to
     */
    public static final int HASH_NAME_LENGTH = 7;

    /**
     * The length that all real hashes must be longer than.
     */
    private static final int MIN_HASH_LENGTH = HASH_NAME_LENGTH + 1;

    public static String createHash(final String password) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return CURRENT_HASH + SPLIT_CHAR + function.hash(password);
    }

    public static String createHash(final String password, final String salt) throws NoSuchAlgorithmException, AuthenticationException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return CURRENT_HASH + SPLIT_CHAR + function.hash(password, salt);
    }

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

    public static String getAlgorithmFromHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return  hash.substring(0, HASH_NAME_LENGTH);
        }
        return PRE_HASH_STORAGE_HASH;
    }

    public static String generateSalt() throws NoSuchAlgorithmException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return function.generateSalt();
    }

    /**
     * Converts A byte array into a hexadecimal string.
     * @param bytes
     * @return
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
     * @param s
     * @return
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
