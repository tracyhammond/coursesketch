package coursesketch.server.interfaces;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class InputParserTest {
    private final static String OPTION_NAME = "optionName";
    private final static String OPTION_VALUE = "optionValue";

    @Test
    public void testParsingCallsCorrectArgument() throws Exception {
        InputParser parser = new InputParser();
        InputParser.ArgumentListener listener = Mockito.spy(new InputParser.ArgumentListener() {
            @Override
            public void run(String argumentValue) throws Exception {
                assertEquals(argumentValue, OPTION_VALUE);
            }
        });
        parser.addParsingOption(parser.createOption(OPTION_NAME, true, "hello"), listener);
        parser.parse(new String[]{ "-" + OPTION_NAME + "=" + OPTION_VALUE});
        verify(listener).run(eq(OPTION_VALUE));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParsingThrowsExceptionAfterClear() throws Exception {
        InputParser parser = new InputParser();
        InputParser.ArgumentListener listener = Mockito.spy(new InputParser.ArgumentListener() {
            @Override
            public void run(String argumentValue) throws Exception {
            }
        });
        parser.addParsingOption(parser.createOption(OPTION_NAME, true, "hello"), listener);
        parser.clear();
        parser.parse(new String[]{ "-" + OPTION_NAME + "=" + OPTION_VALUE});
        verify(listener, never()).run(eq(OPTION_VALUE));
    }
}
