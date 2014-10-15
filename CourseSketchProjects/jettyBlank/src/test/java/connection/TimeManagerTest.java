package connection;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protobuf.srl.request.Message.Request;

public class TimeManagerTest {

    static final long FIXED_TIME_SERVER = 50; // just a random time
    static final long FIXED_TIME_CLIENT = 100; // the client is off by 50
    static final long LAG = 25; // lag is 25

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.setCurrentMillisSystem();
        TimeManager.reset();
    }

    @Test
    public void testDefaultTimeHasNoTimeDifference() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
    }

    @Test
    public void testSendClientTimeReturnsCorrectValue() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request req = TimeManager.serverSendTimeToClient();
        assertEquals(TimeManager.SEND_TIME_TO_CLIENT_MSG, req.getResponseText());
        assertEquals(FIXED_TIME_SERVER, req.getMessageTime());
    }

    @Test
    public void testClientReceiveTimeDiffCorrectlyPartiallyNoLag() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();
        // the client now has a different time it should be off by 50
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);
        final Request clientResponse = TimeManager.decodeRequest(server);
        // we are assuming no lag for this test.
        assertEquals(FIXED_TIME_SERVER - FIXED_TIME_CLIENT, TimeManager.getPartialTimeDifference());
        assertEquals(FIXED_TIME_SERVER, clientResponse.getMessageTime());
        assertEquals(TimeManager.CLIENT_REQUEST_LATENCY_MSG, clientResponse.getResponseText());
    }

    @Test
    public void testClientReceiveTimeDiffCorrectlyPartialOnlyLag() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER + LAG);
        final Request clientResponse = TimeManager.decodeRequest(server);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER + LAG * 2);
        final Request lagResponse = TimeManager.decodeRequest(clientResponse);

        assertEquals(LAG, lagResponse.getMessageTime());
        assertEquals(TimeManager.SEND_LATENCY_TO_CLIENT_MSG, lagResponse.getResponseText());
    }

    @Test
    public void testClientReceiveTimeDiffCorrectlyCompleteOnlyLag() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER + LAG);
        final Request clientResponse = TimeManager.decodeRequest(server);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER + LAG * 2);
        final Request lagResponse = TimeManager.decodeRequest(clientResponse);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER + LAG * 3);
        final Request finalResponse = TimeManager.decodeRequest(lagResponse);

        // set time back to server time
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);

        assertEquals(null, finalResponse);
        assertEquals(TimeManager.getPartialTimeDifference(), -TimeManager.getLatencyDifference());
        assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
    }

    @Test
    public void testClientReceiveTimeDiffCorrectlyCompletelyNoLag() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);
        final Request clientResponse = TimeManager.decodeRequest(server);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request lagResponse = TimeManager.decodeRequest(clientResponse);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);
        final Request finalResponse = TimeManager.decodeRequest(lagResponse);

        // set time back to server time
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);

        assertEquals(null, finalResponse);
        assertEquals(0, TimeManager.getLatencyDifference());
        assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
    }

    @Test
    public void testClientReceiveTimeDiffCorrectlyCompletely() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT + LAG);
        final Request clientResponse = TimeManager.decodeRequest(server);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER  + LAG * 2);
        final Request lagResponse = TimeManager.decodeRequest(clientResponse);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT  + LAG * 3);
        final Request finalResponse = TimeManager.decodeRequest(lagResponse);

        // set time back to server time
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);

        assertEquals(null, finalResponse);
        assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
    }

    @Test
    public void decodeReturnsNullAndIgnoresInvalidRequest() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        final Request.Builder fakeRequest = Request.newBuilder(server);
        fakeRequest.setResponseText("NOT VALID REQUEST TIME");
        final Request clientResponse = TimeManager.decodeRequest(fakeRequest.build());

        assertEquals(null, clientResponse);
    }

    /**
     * The callback should change the server time so if the callback is not called this test will fail.
     */
    @Test
    public void testClientReceiveTimeDiffCorrectlyListener() {
        TimeManager.setTimeEstablishedListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT);
                assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
                TimeManager.getTotalTimeDifference();
            }

        });

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER);
        final Request server = TimeManager.serverSendTimeToClient();

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT + LAG);
        final Request clientResponse = TimeManager.decodeRequest(server);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_SERVER  + LAG * 2);
        final Request lagResponse = TimeManager.decodeRequest(clientResponse);

        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_CLIENT  + LAG * 3);
        final Request finalResponse = TimeManager.decodeRequest(lagResponse);

        assertEquals(null, finalResponse);
        assertEquals(FIXED_TIME_SERVER, TimeManager.getSystemTime());
    }
}
