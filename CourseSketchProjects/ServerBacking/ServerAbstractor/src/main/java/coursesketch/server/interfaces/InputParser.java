package coursesketch.server.interfaces;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses user input into the server.
 */
public class InputParser {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InputParser.class);

    /**
     * Command line options.
     */
    private Options options;

    /**
     * A Map of the arguments matched to their option.
     */
    private Map<Option, ArgumentListener> argumentListenerMap;

    /**
     * The command line parser.
     */
    private CommandLineParser parser;

    /**
     * Creates a new instance of the parser.
     */
    InputParser() {
        argumentListenerMap = new HashMap<>();
        parser = new DefaultParser();
        options = new Options();
    }

    /**
     * Adds a new option and a matching listener.
     *
     * @param option What the user inputs to activate the listener.
     * @param listener Called when the matching option is called.
     */
    public final void addParsingOption(Option option, ArgumentListener listener) {
        argumentListenerMap.put(option, listener);
        options.addOption(option);
    }

    /**
     * Creates an {@link Option} from some values.
     *
     * @param argumentName The command line name of the option
     * @param hasValues True if additional values need to be assigned to it.
     * @param description The human readable description of the options.
     * @return A built command line object.
     */
    public final Option createOption(String argumentName, boolean hasValues, String description) {
        return Option.builder().longOpt(argumentName).argName(argumentName).hasArg(hasValues).desc(description).build();
    }

    /**
     * Parses the string arguments.
     *
     * @param args The program arguments.
     * @throws ParseException Thrown if there is a problem parsing the input.
     */
    final void parse(final String[] args) throws ParseException {
        final CommandLine line = parser.parse(options, args);
        for (Option option : line.getOptions()) {
            final String argument = option.hasArg() ? option.getOpt() == null
                    ? option.getValue() : line.getOptionValue(option.getOpt())
                    : null;
            try {
                argumentListenerMap.get(option).run(argument);
            } catch (Exception e) {
                LOG.error("Error parsing input {}", e);
            }
        }
    }

    /**
     * Clears the options and listeners.
     */
    public final void clear() {
        options = new Options();
        argumentListenerMap = new HashMap<>();
    }

    /**
     * An interface for the argument listener.
     */
    public interface ArgumentListener {
        /**
         * Called for a matching option with the argument value.
         * @param argumentValue The argument value attached to the command line option
         * @throws Exception Thrown if there are internal exceptions
         */
        void run(String argumentValue) throws Exception;
    }
}
