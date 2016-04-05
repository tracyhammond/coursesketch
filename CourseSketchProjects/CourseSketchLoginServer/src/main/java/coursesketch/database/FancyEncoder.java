package coursesketch.database;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Does a longer version of the normal UUID to make it more unique.
 */
public final class FancyEncoder {

    /**
     * Uses secure random instead of normal Math.Random because that is not as secure.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Empty constructor.
     */
    private FancyEncoder() {
    }

    /**
     * @return a UUID+ by taking a UUID then adding more random to the end of
     *         the UUID.
     */
    public static String fancyID() {

        final UUID nextId = AbstractServerWebSocketHandler.Encoder.nextID();
        final long random = SECURE_RANDOM.nextLong();
        final long time = System.currentTimeMillis() | random;
        return nextId.toString() + "-" + Long.toHexString(Math.abs(time + random * time));
    }
}
