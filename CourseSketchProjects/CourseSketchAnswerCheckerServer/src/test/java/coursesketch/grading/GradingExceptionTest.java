package coursesketch.grading;

import org.junit.Assert;
import org.junit.Test;
import protobuf.srl.request.Message;
import protobuf.srl.submission.Feedback;
import utilities.CourseSketchException;

import static org.junit.Assert.assertEquals;

public class GradingExceptionTest {
    @Test
    public void testConstructor() {
        String message = "message";
        CourseSketchException con = new GradingException(message);
        assertEquals(con.getMessage(), message);
    }

    @Test
    public void testConstructorWithCause() {
        String message = "message";
        Exception cause = new Exception();
        CourseSketchException con = new GradingException(message, cause);
        assertEquals(con.getMessage(), message);
        assertEquals(con.getCause(), cause);
        assertEquals(con.getProtoException(), null);
    }

    @Test
    public void testProtobufExceptions() {
        String message = "message";
        CourseSketchException con = new GradingException(message);
        final Message.ProtoException defaultInstance = Message.ProtoException.getDefaultInstance();
        con.setProtoException(defaultInstance);
        assertEquals(con.getProtoException(), defaultInstance);
    }

    @Test
    public void testFeedback() {
        String message = "message";
        GradingException con = new GradingException(message);
        Assert.assertEquals(false, con.hasFeedbackData());
        Feedback.FeedbackData data = Feedback.FeedbackData.getDefaultInstance();
        con.setFeedbackData(data);
        Assert.assertEquals(true, con.hasFeedbackData());
        assertEquals(con.getFeedbackData(), data);
    }
}

