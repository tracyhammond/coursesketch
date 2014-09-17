package connection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.joda.time.DateTime;

import protobuf.srl.request.Message.Request;

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
     * A message representing the state to send to the client.
     *
     * Specifically this one is about sending a time message to the client.
     */
    private static final String SEND_TIME_TO_CLIENT_MSG = "1";

    /**
     * A message representing the state to send to the client.
     *
     * Specifically this one is about the client requesting the latency.
     */
    private static final String CLIENT_REQUEST_LATENCY_MSG = "2";

    /**
     * A message representing the state to send to the client.
     *
     * Specifically this one is about sending the latency to the client.
     */
    private static final String SEND_LATENCY_TO_CLIENT_MSG = "3";


    /**
     * The initial time difference received from the server.
     *
     * This is a transient value and is not thread safe.
     */
    private static long timeDifference = 0;

    /**
     * The difference between the master server and the client server.
     */
    private static long totalTimeDifference = 0;

    /**
     * A listener that is called every time that latency is received and finished calculating.
     */
    private static ActionListener listen;

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

        final Request.Builder req = Request.newBuilder();
        req.setRequestType(Request.MessageType.TIME);
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
        System.out.println("Server Recieved Time");
        timeDifference = req.getMessageTime() - getSystemTime();
        System.out.println("server time:" + milltoDate(req.getMessageTime()));
        System.out.println("proxy time:" + milltoDate(DateTime.now().getMillis()));
        final Request.Builder rsp = Request.newBuilder();
        rsp.setRequestType(Request.MessageType.TIME);
        rsp.setMessageTime(req.getMessageTime() + (getSystemTime() - startCounter));
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

        final Request.Builder rsp = Request.newBuilder();

        rsp.setRequestType(Request.MessageType.TIME);
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
        final long latency = req.getMessageTime();
        totalTimeDifference = timeDifference + latency;
        if (listen != null) {
            listen.actionPerformed(new ActionEvent(req, 0, null));
        }
        System.out.println("Proxy Recived Time\nTotal Time Diff:" + totalTimeDifference);
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
}
