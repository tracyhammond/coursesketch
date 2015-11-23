package coursesketch.server.authentication;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This is an unsecure random.  Only use this on code that does not need to be super secure.
 *
 * DO NOT USE THIS ON PASSWORDS!
 * This is for providing custom not random salts
 * Created by dtracers on 11/23/2015.
 */
public class UnSecureRandom extends SecureRandom {
    private final String salt;

    public UnSecureRandom(final String salt) {
        this.salt = salt;
    }

    @Override
    public final void nextBytes(final byte[] bytes) {
        final byte[] saltedBytes = salt.getBytes();
        if (saltedBytes.length < bytes.length) {
            System.arraycopy(saltedBytes, 0, bytes, 0, saltedBytes.length);
            int lengthDifference = bytes.length - saltedBytes.length;
            while (lengthDifference > 0) {
                System.arraycopy(saltedBytes, 0, bytes, bytes.length - lengthDifference, lengthDifference % saltedBytes.length);
                lengthDifference -= saltedBytes.length;
            }
        } else {
            System.arraycopy(saltedBytes, 0, bytes, 0, bytes.length);
        }

    }
}
