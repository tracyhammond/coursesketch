package coursesketch.grading;

import protobuf.srl.request.Message;
import protobuf.srl.submission.Feedback;
import utilities.CourseSketchException;

/**
 * Thrown while Grading.
 */
public class GradingException extends CourseSketchException {

    /**
     * Feedback that was created before the exception.
     */
    private Feedback.FeedbackData feedbackData;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * @param protoException An existing chain of exceptions.  This could come from a different server even.
     */
    public GradingException(final Message.ProtoException protoException) {
        super(protoException);
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
    GradingException(final String message) {
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
    public GradingException(final String message, final Message.ProtoException protoException) {
        super(message, protoException);
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
    public GradingException(final String message, final Throwable cause) {
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
    public GradingException(final Throwable cause) {
        super(cause);
    }

    @Override
    protected final Message.ProtoException createSpecialProtoException() {
        // Do some stuff but add in this feedback data too
        return null;
    }

    /**
     * @return Feedback data set in this exception.  Can be null.
     */
    final Feedback.FeedbackData getFeedbackData() {
        return feedbackData;
    }

    /**
     * Sets feedback data.
     *
     * @param feedbackData The feedback up until the exception occurred.
     */
    public final void setFeedbackData(Feedback.FeedbackData feedbackData) {
        this.feedbackData = feedbackData;
    }

    /**
     * @return True if feedback data exists.
     */
    final boolean hasFeedbackData() {
        return feedbackData != null;
    }
}
