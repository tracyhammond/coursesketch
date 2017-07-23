package coursesketch.server.authentication;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * This is an unsecure random.  Only use this on code that does not need to be super secure.
 *
 * DO NOT USE THIS ON PASSWORDS!
 * This is for providing custom not random salts
 * Created by dtracers on 11/23/2015.
 */
public class UnsecuredRandom extends SecureRandom {
    /**
     * The local salt that is being stored.
     */
    private final String salt;

    /**
     * Create an instance of the random with the given salt.
     * @param salt The base salt that all results are based off of.
     */
    public UnsecuredRandom(final String salt) {
        this.salt = salt;
    }

    @Override
    @SuppressWarnings("PMD.UseVarargs")
    public final void nextBytes(final byte[] bytes) {
        final byte[] saltedBytes = salt.getBytes(StandardCharsets.UTF_8);
        if (saltedBytes.length < bytes.length) {
            System.arraycopy(saltedBytes, 0, bytes, 0, saltedBytes.length);
            int lengthDifference = bytes.length - saltedBytes.length;
            while (lengthDifference > 0) {
                final int offset = bytes.length - lengthDifference;
                final int length = Math.min(lengthDifference, saltedBytes.length);
                System.arraycopy(saltedBytes, 0, bytes, offset, length);
                lengthDifference -= saltedBytes.length;
            }
        } else {
            System.arraycopy(saltedBytes, 0, bytes, 0, bytes.length);
        }

    }
}
