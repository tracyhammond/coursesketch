package coursesketch.server.authentication;

import coursesketch.database.auth.AuthenticationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.NoSuchAlgorithmException;

/**
 * Created by dtracers on 10/7/2015.
 */
@RunWith(PowerMockRunner.class)
public class HashManagerTest {

    static final String INVALID_ALG = "1234567";
    static final String CORRECT_PASSWORD = "my password sucks but is correct";
    static final String INCORRECT_PASSWORD = "my password sucks but is not correct";

    @Test(timeout=2000)
    public void hashValidatesCorrectly() throws Exception {
        String hash = HashManager.createHash(CORRECT_PASSWORD);
        System.out.println(hash);
        Assert.assertTrue(HashManager.validateHash(CORRECT_PASSWORD, hash));
    }

    @Test(timeout=2000)
    public void twoHashesDoNotHashToSameNumber() throws Exception {
        String hash = HashManager.createHash(CORRECT_PASSWORD);
        String hash2 = HashManager.createHash(CORRECT_PASSWORD);
        Assert.assertNotEquals(hash, hash2);
    }

    @Test(timeout=2000)
    public void twoHashesWithSameSaltToSameNumber() throws Exception {
        String salt = HashManager.generateSalt();
        String hash = HashManager.createHash(CORRECT_PASSWORD, salt);
        String hash2 = HashManager.createHash(CORRECT_PASSWORD, salt);
        Assert.assertEquals(hash, hash2);
    }

    @Test(timeout=2000)
    public void incorrectPasswordFailsToValidate() throws Exception {
        String hash = HashManager.createHash(CORRECT_PASSWORD);
        System.out.println(hash);
        Assert.assertFalse(HashManager.validateHash(INCORRECT_PASSWORD, hash));
    }

    @Test(timeout=2000, expected = AuthenticationException.class)
    public void incorrectPasswordFailsToValidateWithInvalidHashTooShort() throws Exception {
        String hash = "badhash";
        System.out.println(hash);
        Assert.assertFalse(HashManager.validateHash(INCORRECT_PASSWORD, hash));
    }

    @Test(timeout=2000)
    public void incorrectPasswordFailsToValidateWithInvalidHash() throws Exception {
        String hash = HashManager.CURRENT_HASH + HashManager.SPLIT_CHAR + "badhash";
        System.out.println(hash);
        Assert.assertFalse(HashManager.validateHash(INCORRECT_PASSWORD, hash));
    }

    @Test
    public void upgradingFromOldestPasswordWorks() throws Exception {
        String hash = PasswordHash.createHash(CORRECT_PASSWORD);
        Assert.assertFalse(HashManager.validateHash(CORRECT_PASSWORD, hash));

        String newHash = HashManager.upgradeHash(CORRECT_PASSWORD, hash);
        Assert.assertTrue(HashManager.validateHash(CORRECT_PASSWORD, newHash));
    }

    @Test
    public void upgradingFromInvalidPasswordReturnsNull() throws Exception {
        String hash = PasswordHash.createHash(CORRECT_PASSWORD);
        Assert.assertFalse(HashManager.validateHash(CORRECT_PASSWORD, hash));

        String newHash = HashManager.upgradeHash(INCORRECT_PASSWORD, hash);
        Assert.assertNull(newHash);
    }

    @Test(expected = NoSuchAlgorithmException.class)
    public void invalidAlgThrowsException() throws Exception {
        String hash = INVALID_ALG + HashManager.SPLIT_CHAR + "FAKEHASHFKAEHASK";
        HashManager.upgradeHash(CORRECT_PASSWORD, hash);
    }

    @Test(expected = AuthenticationException.class)
    public void invalidSaltThrowsException() throws Exception {
        HashManager.createHash("Pass", "badsalt");
    }

    @Test
    public void unsecureSaltIsGeneratedWithTinyValues() throws NoSuchAlgorithmException, AuthenticationException {
        String str = "1";
        String salt = HashManager.generateUnSecureSalt(str);
        HashManager.createHash("Pass", salt);
    }
}
