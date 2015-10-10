package coursesketch.server.authentication;

import coursesketch.database.auth.AuthenticationException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by dtracers on 10/7/2015.
 */
public interface HashWrapper {
    String algorithmName();

    String hash(String string) throws AuthenticationException;

    String hash(String string, String salt) throws AuthenticationException;

    boolean validateHash(String candidate, String hashedValue) throws AuthenticationException;

    String generateSalt();
}
