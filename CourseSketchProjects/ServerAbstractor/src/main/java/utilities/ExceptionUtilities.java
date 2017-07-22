package utilities;

import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;

/**
 * Conversion from an Exception to a ProtoBuf Message that can be used to be sent from server to client.
 * Created by Raunak on 3/31/15.
 */
public final class ExceptionUtilities {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionUtilities.class);

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
        if (tException == null) {
            pException.setMssg("Passed in null exception");
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                pException.addStackTrace(element.toString());
            }
            return pException.build();
        }
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
        } else if (tException instanceof CourseSketchException) {
            final Message.ProtoException exception = ((CourseSketchException) tException).getProtoException();
            if (exception != null) {
                pException.setCause(exception);
            }
        }

        // gets the class name of the exception.
        pException.setExceptionType(tException.getClass().toString());
        return pException.build();
    }

    /**
     * Takes in an exception, and creates a response on it.
     * @param tException Is a Throwable Exception
     * @param successful true if the request was successful even with the exception
     * @return A response that contains the exception.
     */
    public static Message.DefaultResponse createExceptionResponse(final Throwable tException, final boolean successful) {
        return Message.DefaultResponse.newBuilder().setException(createProtoException(tException)).setSuccessful(true).build();
    }

    /**
     * Takes in an exception, and creates a response on it.
     * @param tException Is a Throwable Exception
     * @return A response that contains the exception
     */
    public static Message.DefaultResponse createExceptionResponse(final Throwable tException) {
        return createExceptionResponse(tException, false);
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
     * Holds an exception for multi-threaded applications that need to pass up exceptions.
     */
    public static final class ExceptionHolder {
        /**
         * The exception that is being passed up.
         */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Exception exception;
    }

    /**
     * @return A new instance of an {@link ExceptionHolder}.
     */
    public static ExceptionHolder getExceptionHolder() {
        return new ExceptionHolder();
    }

    /**
     * @param throwable The exception that is being compared.
     * @param exception The proto exception that is being checked.
     * @return true if the given Throwable is the same type as the protoException
     */
    public static boolean isSameType(final Throwable throwable, final Message.ProtoException exception) {
        return exception.getExceptionType().equals(throwable.getClass().toString());
    }

    /**
     * @param throwable The class representing the exception being compared.
     * @param exception The proto exception that is being checked.
     * @return true if the given Throwable is the same type as the protoException
     */
    public static boolean isSameType(final Class<? extends Throwable> throwable, final Message.ProtoException exception) {
        return exception.getExceptionType().equals(throwable.toString());
    }

    /**
     * Checks the type of exception that was thrown in the proto exception and throws the same type if it exists.
     * @param exception The exception that was found to be thrown.
     * @param message An optional message
     * @throws AuthenticationException Thrown if the proto exception was an AuthenticationException.
     * @throws DatabaseAccessException Thrown if the proto exception was an DatabaseAccessException.
     */
    public static void handleProtoException(final Message.ProtoException exception, final String message)
            throws AuthenticationException, DatabaseAccessException {
        if (ExceptionUtilities.isSameType(AuthenticationException.class, exception)) {
            final AuthenticationException exception1 = new AuthenticationException(message, AuthenticationException.OTHER);
            exception1.setProtoException(exception);
            throw exception1;
        } else if (ExceptionUtilities.isSameType(DatabaseAccessException.class, exception)) {
            final DatabaseAccessException exception1 = new DatabaseAccessException(message);
            exception1.setProtoException(exception);
            throw exception1;
        }
        LOG.error(message, exception);
    }
}
