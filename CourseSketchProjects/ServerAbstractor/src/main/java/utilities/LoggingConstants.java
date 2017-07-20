package utilities;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Holds constants used for logging.
 */
public final class LoggingConstants {

    /**
     * A constant for the exception messages.
     */
    public static final String EXCEPTION_MESSAGE = "Exception: {}";

    /**
     * Stuff.
     */
    private LoggingConstants() {
    }

    /**
     * Pretty Prints json for logging.
     *
     * @param json Takes in a json string and outputs the result.
     * @return A json string in a nice human readable format.
     */
    public static String prettyPrintJson(final String json) {
        return JsonWriter.formatJson(json);
    }
}
