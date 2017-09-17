package coursesketch.connection;

import connection.DatabaseRunner;
import coursesketch.serverfront.ProxyRunner;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.System.setIn;

public class ProxyIntegrationTests {
    private ProxyRunner proxyRunner;
    private DatabaseRunner databaseRunner;

    @Before
    public void setup() throws InterruptedException {
        String[] arguments = new String[]{
                "isLocal=true",
              //  "databaseUrls=127.0.0.1:37017"
        };
        databaseRunner = new DatabaseRunner(arguments);
        databaseRunner.start();
        Thread.sleep(1000);
        proxyRunner = new ProxyRunner(arguments);
        proxyRunner.start();
        Thread.sleep(1000);
    }

    @Test
    public void testProxyMessageToServer() {
        setIn(new ByteArrayInputStream("Reconnect".getBytes(StandardCharsets.UTF_8)));
    }
}
