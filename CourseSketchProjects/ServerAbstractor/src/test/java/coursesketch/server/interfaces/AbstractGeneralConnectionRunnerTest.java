package coursesketch.server.interfaces;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by gigemjt on 10/21/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractGeneralConnectionRunner.class)
public class AbstractGeneralConnectionRunnerTest {

    @Test(timeout=1000)
    public void testSSLIsConfigureIsCalledWhenSecureIsTrue() {
        //createPartialMockAndInvokeDefaultConstructor
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        doCallRealMethod().when(run).start();
        doNothing().when(run).loadConfigurations();
        Whitebox.setInternalState(run, "secure", true);
        run.start();
        verify(run, times(1)).configureSSL(anyString(), anyString());
    }

    @Test(timeout=1000)
    public void testSSLIsConfigureIsNOTCalledWhenSecureIsFalse() {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        doCallRealMethod().when(run).start();
        doNothing().when(run).loadConfigurations();
        Whitebox.setInternalState(run, "secure", false);
        run.start();
        verify(run, times(0)).configureSSL(anyString(), anyString());
    }

    @Test(timeout=1000)
    public void executeLocalEnviromentIsCalledWhenLocalIsTrue() {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        doCallRealMethod().when(run).start();
        doNothing().when(run).loadConfigurations();
        Whitebox.setInternalState(run, "local", true);
        run.start();
        verify(run, times(1)).executeLocalEnvironment();
    }

    @Test(timeout=1000)
    public void executeRemoveEnviromentIsCalledWhenLocalIsFalse() {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        doCallRealMethod().when(run).start();
        doNothing().when(run).loadConfigurations();
        Whitebox.setInternalState(run, "local", false);
        run.start();
        verify(run, times(1)).executeRemoveEnvironment();
    }

    @Test(timeout=1000)
    public void testParseCommandReturnsTrueGivenNoCommand() throws IOException, InterruptedException {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();
        assertTrue(run.parseCommand(null, null));
    }

    @Test(timeout=1000)
    public void testExitCommandCalledWithExitValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        run.parseCommand("exit", reader);

        PowerMockito.verifyPrivate(run).invoke("exitCommand", reader);
    }

    @Test(timeout=1000)
    public void testRestartCommandCalledWithRestartValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        run.parseCommand("restart", reader);

        PowerMockito.verifyPrivate(run).invoke("restartCommand", reader);
    }

    @Test(timeout=1000)
    public void testReconnectCalledWithReconnectValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        run.parseCommand("reconnect", reader);

        verify(run, times(1)).reconnect();
    }

    @Test(timeout=1000)
    public void testStopCommandCalledWithStopValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine()).thenThrow(Exception.class);
        run.parseCommand("stop", reader);

        PowerMockito.verifyPrivate(run).invoke("stopCommand", reader);
    }

    @Test(timeout=1000)
    public void testStartCommandCalledWithStartValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        run.parseCommand("start", reader);

        PowerMockito.verifyPrivate(run).invoke("startCommand");
    }

    @Test(timeout=1000)
    public void testToggleLogginCalledWithToggleValue() throws Exception {
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();

        BufferedReader reader = mock(BufferedReader.class);
        run.parseCommand("toggle logging", reader);

        PowerMockito.verifyPrivate(run).invoke("toggleLoggingCommand", reader);
    }

    @Test(timeout=1000)
    public void testParseUtilityCommandCalledWithUnknownValue() throws Exception {
        String command = "NOKNOWNVALUE";
        AbstractGeneralConnectionRunner run = PowerMockito.mock(AbstractGeneralConnectionRunner.class);
        when(run.parseCommand(anyString(), any(BufferedReader.class))).thenCallRealMethod();
        when(run.parseUtilityCommand(anyString(), any(BufferedReader.class))).thenReturn(false);

        BufferedReader reader = mock(BufferedReader.class);
        assertFalse(run.parseCommand(command, reader));

        verify(run, times(1)).parseUtilityCommand(command, reader);
    }
}
