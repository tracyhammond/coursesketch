package coursesketch.server.interfaces;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InputParser {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InputParser.class);

    private Options options;
    private Map<Option, ArgumentListener> argumentListenerMap;
    private CommandLineParser parser;

    InputParser() {
        argumentListenerMap = new HashMap<>();
        parser = new DefaultParser();
        options = new Options();
    }

    public void addParsingOption(Option option, ArgumentListener listener) {
        argumentListenerMap.put(option, listener);
        options.addOption(option);
    }

    public Option createOption(String argumentName, boolean hasValues, String description) {
        return Option.builder().longOpt(argumentName).argName(argumentName).hasArg(hasValues).desc(description).build();
    }

    void parse(String[] args) throws ParseException {
        CommandLine line = parser.parse(options, args);
        for (Option option : line.getOptions()) {
            String argument = option.hasArg() ? line.getOptionValue(option.getOpt()) : null;
            try {
                argumentListenerMap.get(option).run(argument);
            } catch (Exception e) {
                LOG.error("Error parsing input {}", e);
            }
        }
    }

    public void clear() {
        options = new Options();
        argumentListenerMap = new HashMap<>();
    }

    public interface ArgumentListener {
        void run(String argumentValue) throws IOException, InterruptedException;
    }
}
