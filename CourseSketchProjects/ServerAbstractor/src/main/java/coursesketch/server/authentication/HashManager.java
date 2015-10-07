package coursesketch.server.authentication;

import com.google.common.collect.ImmutableMap;
import org.mindrot.BCrypt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

/**
 * Created by dtracers on 10/7/2015.
 */
public class HashManager {
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

    public static String createHash(final String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(CURRENT_HASH));
        }
        return CURRENT_HASH + SPLIT_CHAR + function.hash(password);
    }

    public static boolean validateHash(final String candidate, final String hash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final HashWrapper function = HASH_FUNCTION_MAP.get(CURRENT_HASH); // latest hash function
        if (function == null) {
            throw new NoSuchAlgorithmException(getAlgorithmFromHash(hash));
        }
        return function.validateHash(candidate, unwrapHash(hash));
    }

    public static String upgradeHash(final String candidate, final String hash) throws NoSuchAlgorithmException, InvalidKeySpecException {
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

    private static String unwrapHash(final String hash) {
        if (hash.charAt(HASH_NAME_LENGTH) == SPLIT_CHAR) {
            return  hash.substring(HASH_NAME_LENGTH + 1);
        }
        return hash;
    }

    private static class BCryptWrapper implements HashWrapper {
        private static final int LOG_ROUNDS = 12;

        @Override public String algorithmName() {
            return "BYCRYPT";
        }

        @Override public String hash(final String password)throws InvalidKeySpecException, NoSuchAlgorithmException {
            return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
        }

        @Override public boolean validateHash(final String candidate, final String hashedValue) {
            try {
                return BCrypt.checkpw(candidate, hashedValue);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    @Deprecated
    private static class PasswordHashWrapper implements HashWrapper {

        @Override public String algorithmName() {
            return "PASHASH";
        }

        @Override public String hash(final String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
            return PasswordHash.createHash(password);
        }

        @Override public boolean validateHash(final String candidate, final String hashedValue)
                throws InvalidKeySpecException, NoSuchAlgorithmException {
            return PasswordHash.validatePassword(candidate, hashedValue);
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
