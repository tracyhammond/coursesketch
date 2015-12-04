package utilities;

import org.junit.Assert;
import org.junit.Test;
import protobuf.srl.request.Message;

/**
 * Created by dtracers on 12/4/2015.
 */
public class ExceptionUtilitiesTest {
    @Test
    public void nothingBadHappensWhenPassedNullProto() {
        Message.ProtoException protoException = ExceptionUtilities.createProtoException(null);
        Assert.assertNotNull(protoException);

        System.out.println(protoException);
    }

    @Test
    public void nothingBadHappensWhenPassedNullResponse() {
        Object exceptionResponse = ExceptionUtilities.createExceptionResponse(null);
        Assert.assertNotNull(exceptionResponse);

        System.out.println(exceptionResponse);
    }

    @Test
    public void nothingBadHappensWhenPassedNullResponse2() {
        Object exceptionResponse = ExceptionUtilities.createExceptionResponse(null, false);
        Assert.assertNotNull(exceptionResponse);

        System.out.println(exceptionResponse);
    }

    @Test
    public void stackTraceIsProcessedCorrectly() {
        String message = "MY MESSAGE";
        Exception exception = new Exception(message);
        Message.ProtoException exceptionResponse = ExceptionUtilities.createProtoException(exception);

        StackTraceElement[] elements = exception.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            Assert.assertEquals(elements[i].toString(), exceptionResponse.getStackTrace(i));
        }
        Assert.assertEquals(message, exceptionResponse.getMssg());
        Assert.assertEquals(Exception.class.toString(), exceptionResponse.getExceptionType());
        Assert.assertNotNull(exceptionResponse);

        System.out.println(exceptionResponse);
    }
}
