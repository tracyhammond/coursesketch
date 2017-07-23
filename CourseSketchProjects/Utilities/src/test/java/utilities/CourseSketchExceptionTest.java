package utilities;

import org.junit.Test;
import protobuf.srl.request.Message;

import static org.junit.Assert.assertEquals;

public class CourseSketchExceptionTest {
    @Test
    public void testConstructor() {
        String message = "message";
        CourseSketchException con = new CourseSketchException(message);
        assertEquals(con.getMessage(), message);
    }

    @Test
    public void testConstructorWithCause() {
        String message = "message";
        Exception cause = new Exception();
        CourseSketchException con = new CourseSketchException(message, cause);
        assertEquals(con.getMessage(), message);
        assertEquals(con.getCause(), cause);
        assertEquals(con.getProtoException(), null);
    }

    @Test
    public void testProtobufExceptions() {
        String message = "message";
        CourseSketchException con = new CourseSketchException(message);
        final Message.ProtoException defaultInstance = Message.ProtoException.getDefaultInstance();
        con.setProtoException(defaultInstance);
        assertEquals(con.getProtoException(), defaultInstance);
    }
}
