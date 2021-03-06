package utilities;

import protobuf.srl.request.Message;

/**
 * Created by dtracers on 10/21/2015.
 */
public class CourseSketchException extends Exception {

    /**
     * An exception that may haven been sent from another server or passed up as a compiled proto object.
     */
    private Message.ProtoException protoException;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * @param protoException An existing chain of exceptions.  This could come from a different server even.
     */
    public CourseSketchException(final Message.ProtoException protoException) {
        this.protoException = protoException;
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public CourseSketchException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message
     *         the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param protoException An existing chain of exceptions.  This could come from a different server even.
     * @since 1.4
     */
    public CourseSketchException(final String message, final Message.ProtoException protoException) {
        super(message);
        this.protoException = protoException;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
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
    public CourseSketchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause
     *         the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since 1.4
     */
    public CourseSketchException(final Throwable cause) {
        super(cause);
    }

    /**
     * Sets a proto exception that occued.
     * @param protoException The protobuf exception.
     */
    public final void setProtoException(final Message.ProtoException protoException) {
        this.protoException = protoException;
    }

    /**
     * @return A {@link utilities.ProtobufUtilities.ProtobufException} if it exist or null if it does not.
     */
    public final Message.ProtoException getProtoException() {
        if (protoException == null) {
            return createSpecialProtoException();
        }
        return protoException;
    }

    /**
     * @return a special version of the proto exception for this specific exception.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Message.ProtoException createSpecialProtoException() {
        return null;
    }

    @Override
    public final String getMessage() {
        final String result = super.getMessage();
        if (getProtoException() != null) {
            return result + "\n\tCaused by: " + protoException.toString().replace("\n", "\n\t\t") + "\rEnding StackTrace";
        }
        return result;
    }
}
