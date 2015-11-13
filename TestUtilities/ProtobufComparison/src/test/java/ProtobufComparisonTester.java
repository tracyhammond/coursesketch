import com.coursesketch.test.utilities.ProtobufComparison;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import org.junit.Test;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparisonTester {
    @Test
    public void testComparisonOfSameProtoObjects() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.getDefaultInstance(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfSameProtoObjectsdiffvalues() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.newBuilder().setRequestId("6").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfSameProtoObjectsdiffvaluesButItIsIgnored() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().ignoreField(Message.Request.getDescriptor().findFieldByName("requestId")).build();
        comp.equals(Message.Request.newBuilder().setRequestId("6").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfDefaultValuesAreIgnore() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().ignoreField(Message.Request.getDescriptor().findFieldByName("requestId")).build();
        comp.equals(Message.Request.newBuilder().setRequestId("").buildPartial(), Message.Request.getDefaultInstance());
    }

    @Test
    public void testComparisonOfSameP2rotoObjects() {
        ProtobufComparison comp = new ProtobufComparisonBuilder().build();
        comp.equals(Message.Request.getDefaultInstance(), Message.Request.getDefaultInstance());
    }
}
