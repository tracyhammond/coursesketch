package database;

import java.util.UUID;

import interfaces.AbstractServerWebSocketHandler;

/**
 * Does a longer version of the normal UUID to make it more unique.
 */
public final class FancyEncoder {

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
        final long random = (long) (Math.random() * ((double) Integer.MAX_VALUE));
        final long time = System.currentTimeMillis() | random;
        return nextId.toString() + "-" + Long.toHexString(Math.abs(time + random * time));
    }
}
