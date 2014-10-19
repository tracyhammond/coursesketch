package interfaces;

import org.eclipse.jetty.server.Server;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by gigemjt on 10/19/14.
 */
public interface IGeneralConnectionRunner {
    void loadConfigurations();

    void executeLocalEnviroment();

    void executeRemoveEnviroment();

    void createServer();

    void startServer();

    boolean parseCommand(String command, BufferedReader sysin) throws IOException, InterruptedException;

    @SuppressWarnings("checkstyle:designforextension")
    boolean parseUtilityCommand(String command, BufferedReader sysin) throws IOException;

    void startInput();

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    void stop();

    String[] getArgs();

    Server getServer();

    int getPort();

    long getTimeoutTime();

    boolean isAcceptingCommandInput();

    void setAcceptingCommandInput(boolean acceptInputToSet);

    boolean isProduction();

    boolean isLocal();

    boolean isLogging();
}
