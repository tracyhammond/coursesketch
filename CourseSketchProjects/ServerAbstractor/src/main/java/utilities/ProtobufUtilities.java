package utilities;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import protobuf.srl.request.Message;

/**
 * Holds a standard set of utilties for protobuf use.
 *
 * Created by gigemjt on 5/24/15.
 */
public final class ProtobufUtilities {

    /**
     * An exception that is specific to protobuf errors.
     */
    public static class ProtobufException extends RuntimeException {

        /**
         * Constructs a new runtime exception with {@code null} as its
         * detail message.  The cause is not initialized, and may subsequently be
         * initialized by a call to {@link #initCause}.
         */
        public ProtobufException() {
            super();
        }

        /**
         * Constructs a new runtime exception with the specified cause and a
         * detail message of <tt>(cause==null ? null : cause.toString())</tt>
         * (which typically contains the class and detail message of
         * <tt>cause</tt>).  This constructor is useful for runtime exceptions
         * that are little more than wrappers for other throwables.
         *
         * @param cause
         *         the cause (which is saved for later retrieval by the
         *         {@link #getCause()} method).  (A <tt>null</tt> value is
         *         permitted, and indicates that the cause is nonexistent or
         *         unknown.)
         * @since 1.4
         */
        public ProtobufException(final Throwable cause) {
            super(cause);
        }

        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message
         *         the detail message. The detail message is saved for
         *         later retrieval by the {@link #getMessage()} method.
         */
        public ProtobufException(final String message) {
            super(message);
        }

        /**
         * Constructs a new runtime exception with the specified detail message and
         * cause.  <p>Note that the detail message associated with
         * {@code cause} is <i>not</i> automatically incorporated in
         * this runtime exception's detail message.
         *
         * @param message
         *         the detail message (which is saved for later retrieval
         *         by the {@link #getMessage()} method).
         * @param cause
         *         the cause (which is saved for later retrieval by the
         *         {@link #getCause()} method).  (A <tt>null</tt> value is
         *         permitted, and indicates that the cause is nonexistent or
         *         unknown.)
         * @since 1.4
         */
        public ProtobufException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Empty constructor.
     */
    private ProtobufUtilities() {
        // empty constructor.
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
     * If the input is null a blank request is returned.
     * If deep is true then all data is copied from the input request
     * @param req The request that contains the information that is being copied.
     * @param deep True if the entire request is copied. If false then only the minimum needed to be sent from the server is saved.
     * @return A copy of the Request in builder form.
     */
    public static Message.Request.Builder createBaseResponse(final Message.Request req, final boolean deep) {
        if (req == null) {
            return Message.Request.newBuilder();
        }
        if (deep) {
            return Message.Request.newBuilder(req);
        }

        final Message.Request.Builder response = Message.Request.newBuilder();
        response.setRequestId(req.getRequestId());
        response.setRequestType(req.getRequestType());
        response.setSessionInfo(req.getSessionInfo());
        return response;
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
     * If the input is null a blank request is returned.
     *
     * This called {@link #createBaseResponse(Message.Request, boolean)} with deep being false.
     * @param req The request that contains the information that is being copied.
     * @return A copy of the Request in builder form.
     */
    public static Message.Request.Builder createBaseResponse(final Message.Request req) {
        return createBaseResponse(req, false);
    }

    /**
     * Creates a base response from the request.
     *
     * This contains information that does not change when responding.
     * Right now this contains:
     * <ul>
     *     <li>requestType</li>
     *     <li>otherData</li>
     *     <li>sessionInfo</li>
     *     <li>requestId</li>
     * </ul>
     *
     * If the input is null a blank request is returned
     * @param type An exception is thrown if this is null.
     * @param data compiled to a byte array and set as otherData
     * @param sessionInfo information about where the request came from.
     * @param requestId sets on automatically if this null.
     * @return A built request containing all of this information
     */
    public static Message.Request.Builder createRequestFromData(final Message.Request.MessageType type, final GeneratedMessage data,
            final String sessionInfo, final String requestId) {
        if (type == null) {
            throw new ProtobufException("Request type can not be null");
        }

        final Message.Request.Builder response = Message.Request.newBuilder();
        response.setRequestType(type);
        if (data != null) {
            response.setOtherData(Any.pack(data));
        }
        if (requestId != null) {
            response.setRequestId(requestId);
        } else {
            response.setRequestId(AbstractServerWebSocketHandler.Encoder.nextID().toString());
        }

        if (sessionInfo != null) {
            response.setSessionInfo(sessionInfo);
        }
        return response;
    }

    /**
     * Creates a base response from the request.
     *
     * This contains information that does not change when responding.
     * Right now this contains:
     * <ul>
     *     <li>requestType</li>
     *     <li>requestId</li>
     * </ul>
     *
     * If the input is null a blank request is returned
     * @param type An exception is thrown if this is null.
     * @return A built request containing all of this information
     * @see #createRequestFromData(Message.Request.MessageType, GeneratedMessage, String, String)
     */
    public static Message.Request.Builder createRequestFromData(final Message.Request.MessageType type) {
        return createRequestFromData(type, null);
    }

    /**
     * Creates a base response from the request.
     *
     * This contains information that does not change when responding.
     * Right now this contains:
     * <ul>
     *     <li>requestType</li>
     *     <li>otherData</li>
     *     <li>requestId</li>
     * </ul>
     *
     * If the input is null a blank request is returned
     * @param type An exception is thrown if this is null.
     * @param data compiled to a byte array and set as otherData
     * @return A built request containing all of this information
     * @see #createRequestFromData(Message.Request.MessageType, GeneratedMessage, String, String)
     */
    public static Message.Request.Builder createRequestFromData(final Message.Request.MessageType type, final GeneratedMessage data) {
        return createRequestFromData(type, data, null);
    }

    /**
     * Creates a base response from the request.
     *
     * This contains information that does not change when responding.
     * Right now this contains:
     * <ul>
     *     <li>requestType</li>
     *     <li>otherData</li>
     *     <li>sessionInfo</li>
     *     <li>requestId</li>
     * </ul>
     *
     * If the input is null a blank request is returned
     * @param type An exception is thrown if this is null.
     * @param data compiled to a byte array and set as otherData
     * @param sessionInfo information about where the request came from.
     * @return A built request containing all of this information
     * @see #createRequestFromData(Message.Request.MessageType, GeneratedMessage, String, String)
     */
    public static Message.Request.Builder createRequestFromData(final Message.Request.MessageType type, final GeneratedMessage data,
            final String sessionInfo) {
        return createRequestFromData(type, data, sessionInfo, null);
    }
}
