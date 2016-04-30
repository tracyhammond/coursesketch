package utilities;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Created by Raunak on 2/26/15.
 */
public class LoggingConstants {
    /**
     * A constant for the exception messages.
     */
    public static final String EXCEPTION_MESSAGE = "Exception: {}";

    /**
     * Pretty Prints json for logging.
     *
     * @param json Takes in a json string and outputs the result.
     * @return
     */
    public static final String prettyPrintJson(final String json) {
        return JsonWriter.formatJson(json);
    }
}
