package utilities;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Manages possible differences in time between the servers.
 *
 * The time is calculates in 6 steps:
 * <ol>
 * <li>The client asks the server for the true time</li>
 * <li>The server responds and the client makes an adjustment</li>
 * <li>The client then uses that adjustment to ask for latency</li>
 * <li>The server then checks the time sent by the client compares it to its
 * time and assumes the round trip took the same amount of time. This value is
 * then sent back to the client</li>
 * <li>The client receives the latency and uses that plus the original time
 * difference to calculate the total time difference.</li>
 * </ol>
 * @author gigemjt
 *
 */
public final class TimeManager {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TimeManager.class);

    /**
     * A message representing the state to send to the client.
     *
     * Specifically this one is about sending a time message to the client.
     */
    public static final String SEND_TIME_TO_CLIENT_MSG = "1";

    /**
     * A message representing the state to send to the client.
     *
     * Specifically this one is about the client requesting the latency.
     */
    public static final String CLIENT_REQUEST_LATENCY_MSG = "2";

    /**
     * A message representing the state to send to the client.
     *
     * Specifically this one is about sending the latency to the client.
     */
    public static final String SEND_LATENCY_TO_CLIENT_MSG = "3";


    /**
     * The initial time difference received from the server.
     *
     * This is a transient value and is not thread safe.
     */
    private static long timeDifference;

    /**
     * The latency between the client and the server.
     */
    private static long latencyDifference;

    /**
     * The difference between the master server and the client server.
     */
    private static long totalTimeDifference;

    /**
     * A listener that is called every time that latency is received and finished calculating.
     */
    private static ActionListener listen;

    /**
     * Private constructor.
     */
    private TimeManager() {
    }

    /**
     * Sets a listener that is called when the final time is established.
     * @param list The listener is called with an empty ActionEvent
     */
    public static void setTimeEstablishedListener(final ActionListener list) {
        listen = list;
    }

    /**
     * @return the adjusted system time based off of data from the master server.
     */
    public static long getSystemTime() {
        return DateTime.now().getMillis() + totalTimeDifference;
    }

    /**
     * @return Creates a request that sends the current time to the client. Request contains 'true' time.
     */
    public static Request serverSendTimeToClient() {

        final Request.Builder req = ProtobufUtilities.createRequestFromData(Request.MessageType.TIME);
        req.setMessageTime(getSystemTime());
        // Server sending client  'true' time
        req.setResponseText(SEND_TIME_TO_CLIENT_MSG);
        return req.build();
    }

    /**
     * Receives the time of the server and calculates the time difference.
     *
     * Then creates a response asking the server to calculate the latency of the response.
     * @param req Contains the master server time.
     * @return A {@link Request} asking for the latency of the message itself.
     */
    private static Request clientReceiveTimeDiff(final Request req) {
        final long startCounter = getSystemTime();
        LOG.info("Server Recieved Time");
        timeDifference = req.getMessageTime() - getSystemTime();
        LOG.info("server time: {}", milltoDate(req.getMessageTime()));
        LOG.info("my (client) time: {}", milltoDate(DateTime.now().getMillis()));
        LOG.info("time difference: {}", timeDifference);
        final Request.Builder rsp = ProtobufUtilities.createBaseResponse(req);
        // message time plus adjusted time minus time it took to compute adjusted time.
        rsp.setMessageTime(req.getMessageTime() + getSystemTime() - startCounter);
        rsp.setResponseText(CLIENT_REQUEST_LATENCY_MSG);

        return rsp.build();
    }

    /**
     * Receives a request and handles it correctly based on the sent in values.
     * @param req The input request that is given.
     * @return A request if there is one to be sent or null if there is no response.
     */
    public static Request decodeRequest(final Request req) {
        // client
        if (req.getResponseText().equals(SEND_TIME_TO_CLIENT_MSG)) {
            return clientReceiveTimeDiff(req);
        // server
        } else if (req.getResponseText().equals(CLIENT_REQUEST_LATENCY_MSG)) {
            return serverSendLatencyToClient(req);
        // client
        } else if (req.getResponseText().equals(SEND_LATENCY_TO_CLIENT_MSG)) {
            return clientReceiveLatency(req);
        }
        return null;
    }

    /**
     * @param req Contains the slightly adjusted time sent from the client.
     * @return Creates a request that contains the computed latency
     */
    private static Request serverSendLatencyToClient(final Request req) {
        final long latency = getSystemTime() - req.getMessageTime();
        LOG.info("latency: {}", latency);

        final Request.Builder rsp = ProtobufUtilities.createBaseResponse(req);

        rsp.setMessageTime(latency / 2);
        rsp.setResponseText(SEND_LATENCY_TO_CLIENT_MSG);
        return rsp.build();
    }

    /**
     * Computes the final time difference given the latency.
     * @param req Contains the latency sent from the server.
     * @return null
     */
    private static Request clientReceiveLatency(final Request req) {
        latencyDifference = req.getMessageTime();
        totalTimeDifference = timeDifference + latencyDifference;
        if (listen != null) {
            listen.actionPerformed(new ActionEvent(req, 0, null));
        }
        LOG.info("Proxy Recived Time\nTotal Time Diff: {}", totalTimeDifference);
        return null;
    }

    /**
     * Given milliseconds return a {@link DateTime}.
     * @param mils milliseconds as a number
     * @return {@link DateTime}
     */
    private static DateTime milltoDate(final long mils) {
        return new DateTime(mils);
    }

    /**
     * Resets the TimeManager so that it is fresh. The results of this method
     * will be that all state will be equal to the TimeManager first being
     * created.
     */
    public static void reset() {
        timeDifference = 0;
        totalTimeDifference = 0;
        listen = null;
    }

    /**
     * @return the time difference between the server an the client but this
     *         assumes there is zero lag.
     */
    public static long getPartialTimeDifference() {
        return timeDifference;
    }

    /**
     * @return the time difference between the server an the client and this
     *         accounts for lag. But it assumes lag is the same between server
     *         and client.
     */
    public static long getTotalTimeDifference() {
        return totalTimeDifference;
    }

    /**
     * @return the latency difference between the server and the client.
     */
    public static long getLatencyDifference() {
        return latencyDifference;
    }
}
