package utilities;

import protobuf.srl.request.Message;

/**
 * Conversion from an Exception to a ProtoBuf Message that can be used to be sent from server to client.
 * Created by Raunak on 3/31/15.
 */
public final class ExceptionUtilities {

    /**
     *
     */
    private ExceptionUtilities() { }
    /**
     *
     * @param tException Is a Throwable Exception.
     * @return ProtoException, pException, that inherits the properties of e.
     */
    public static Message.ProtoException createProtoException(final Throwable tException) {
        final Message.ProtoException.Builder pException = Message.ProtoException.newBuilder();
        pException.setMssg(tException.getMessage());
        for (StackTraceElement element : tException.getStackTrace()) {
            pException.addStackTrace(element.toString());
        }
        if (tException.getCause() != null) {
            pException.setCause(createProtoException(tException.getCause()));
        }
        return pException.build();
    }
    /**
     * Creates a request that represents the exception that was caused.
     *
     * @param exception
     *         the exception to be sent back to the client.
     * @param inputRequest
     *         The request that was sent to this server.
     * @return A request that warps around the exception.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    public static Message.Request createExceptionRequest(final Message.ProtoException exception, final Message.Request inputRequest) {
        final Message.Request.Builder builder = Message.Request.newBuilder(inputRequest);
        builder.setRequestType(Message.Request.MessageType.ERROR);
        builder.clearOtherData();
        builder.clearMessageTime();
        builder.setOtherData(exception.toByteString());
        return builder.build();
    }
}
