package utilities;

import protobuf.srl.request.Message;

/**
 * Holds a standard set of utilties for protobuf use.
 *
 * Created by gigemjt on 5/24/15.
 */
public class ProtobufUtilities {
    /**
     * Empty constructor.
     */
    private ProtobufUtilities() {

    }

    /**
     * Creates a base response from the request.
     *
     * This contains information that does not change when responding.
     * Right now this contains:
     * <ul>
     *     <li>requestId</li>
     *     <li>requestType</li>
     *     <li>sessionInfo</li>
     * </ul>
     *
     * If the input is null a blank request is returned
     * @param req
     * @return
     */
    public static Message.Request.Builder createBaseResponse(Message.Request req) {
        if (req == null) {
            return Message.Request.newBuilder();
        }
        final Message.Request.Builder response = Message.Request.newBuilder();
        response.setRequestId(req.getRequestId());
        response.setRequestType(req.getRequestType());
        response.setSessionInfo(req.getSessionInfo());
        return response;
    }

}
