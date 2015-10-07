package coursesketch.server.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by dtracers on 10/7/2015.
 */
public interface HashWrapper {
    String algorithmName();

    String hash(String string) throws InvalidKeySpecException, NoSuchAlgorithmException;

    boolean validateHash(String candidate, String hashedValue) throws InvalidKeySpecException, NoSuchAlgorithmException;
}
