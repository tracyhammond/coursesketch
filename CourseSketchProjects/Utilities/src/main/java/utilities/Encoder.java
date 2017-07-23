package utilities;


import protobuf.srl.request.Message;

import java.util.UUID;

/**
 * Encodes messages with important information.
 * @author gigemjt
 */
public final class Encoder {
    /**
     * Empty constructor.
     */
    private Encoder() {
    }

    /**
     * Returns a {@link Message.Request} that contains the sessionInfo and the time
     * that the message was sent.
     *
     * @param req The message that is being rebuilt.
     *
     * @param sessionInfo The information about the session.
     *
     * @return itself if the sessionInfo is null.
     */
    public static Message.Request requestIDBuilder(final Message.Request req, final String sessionInfo) {
        if (sessionInfo == null) {
            return req;
        }

        // why do the work if they are the same?
        if (sessionInfo.equals(req.getSessionInfo())) {
            return req;
        }

        final Message.Request.Builder sessionInfoReplacement = ProtobufUtilities.createBaseResponse(req, true);
        sessionInfoReplacement.setSessionInfo(sessionInfo);
        if (!sessionInfoReplacement.hasMessageTime()) {
            sessionInfoReplacement.setMessageTime(TimeManager.getSystemTime());
        }
        return sessionInfoReplacement.build();
    }

    /**
     * @return The next UUID that is generated.
     */
    public static UUID nextID() {
        return UUID.randomUUID();
    }
}
