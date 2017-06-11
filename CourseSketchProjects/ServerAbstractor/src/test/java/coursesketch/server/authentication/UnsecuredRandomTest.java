package coursesketch.server.authentication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.SecureRandom;

/**
 * Created by dtracers on 11/24/2015.
 */
@RunWith(PowerMockRunner.class)
public class UnsecuredRandomTest {
    @Test
    public void randomDoesNotCrashWithLargerNumberOfBytes() {
        String str = "1";
        SecureRandom r = new UnsecuredRandom(str);
        byte[] bytes = new byte[8];
        r.nextBytes(bytes);
        for (byte b: bytes) {
            Assert.assertEquals(str.charAt(0), b);
        }
    }

    @Test
    public void randomDoesNotCrashWithLargerNumberOfDifferentBytes() {
        String str = "12";
        SecureRandom r = new UnsecuredRandom(str);
        byte[] bytes = new byte[8];
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(str.charAt(i % str.length()), bytes[i]);
        }
    }

    @Test
    public void randomDoesNotCrashWithSmallerNumberOfBytes() {
        String str = "0123456789";
        SecureRandom r = new UnsecuredRandom(str);
        byte[] bytes = new byte[8];
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(str.charAt(i), bytes[i]);
        }
    }
}
