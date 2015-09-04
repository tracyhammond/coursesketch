package utilities;

import protobuf.srl.request.Message;

/**
 * Conversion from an Exception to a ProtoBuf Message that can be used to be sent from server to client.
 * Created by Raunak on 3/31/15.
 */
public final class ExceptionUtilities {

    /**
     * Private constructor.
     */
    private ExceptionUtilities() { }

    /**
     * Takes in an exception, and creates a ProtoException object which inherit the
     * qualities of the exception such as the stack trace and the message.
     * @param tException Is a Throwable Exception.
     * @return ProtoException, pException, that inherits the properties of e.
     */
    public static Message.ProtoException createProtoException(final Throwable tException) {
        final Message.ProtoException.Builder pException = Message.ProtoException.newBuilder();
        if (tException.getMessage() != null) {
            pException.setMssg(tException.getMessage());
        } else {
            pException.setMssg("No message was found");
        }
        for (StackTraceElement element : tException.getStackTrace()) {
            pException.addStackTrace(element.toString());
        }
        if (tException.getCause() != null) {
            pException.setCause(createProtoException(tException.getCause()));
        }

        // gets the class name of the exception.
        pException.setExceptionType(tException.getClass().getSimpleName());
        return pException.build();
    }

    /**
     * Creates a request that represents the exception that was caused.
     *
     * @param inputRequest
     *         The request that was sent to this server.
     * @param exception
     *         the exception to be sent back to the client.
     * @param responseText
     *          sets the Response Text to the string and not the exception's message.
     * @return A request that warps around the exception.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    public static Message.Request createExceptionRequest(final Message.Request inputRequest, final Message.ProtoException exception,
            final String responseText) {
        final Message.Request.Builder builder = ProtobufUtilities.createBaseResponse(inputRequest);
        builder.setRequestType(Message.Request.MessageType.ERROR);
        builder.setOtherData(exception.toByteString());
        if (responseText != null) {
            builder.setResponseText(responseText);
        } else {
            builder.setResponseText(exception.getMssg());
        }
        return builder.build();
    }

    /**
     * Creates a request that represents the exception that was caused.
     *
     * @param inputRequest
     *          takes the Request type and session info from the inputRequest.
     * @param exception
     *          sets to the requests' other data.

     * @return A request that warps around the exception and message you pass in.
     */
    public static Message.Request createExceptionRequest(final Message.Request inputRequest, final Message.ProtoException exception) {
        return createExceptionRequest(inputRequest, exception, null);
    }

    /**
     * Holds an exception (in the case that one is needed
     */
    public static final class ExceptionHolder {
        public Exception exception;
    }

    public static ExceptionHolder getExceptionHolder() {
        return new ExceptionHolder();
    }
}
