import com.coursesketch.test.utilities.ProtobufComparison;
import org.junit.Test;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparisonTester {
    @Test
    public void testComparisonOfSameProtoObjects() {
        ProtobufComparison comp = new ProtobufComparison(null, null);
        comp.equals(Message.Request.getDefaultInstance(), Message.Request.getDefaultInstance());
    }
}
