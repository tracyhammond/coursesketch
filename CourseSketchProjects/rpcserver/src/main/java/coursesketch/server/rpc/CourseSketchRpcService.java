package coursesketch.server.rpc;

import com.google.protobuf.Service;
import coursesketch.server.interfaces.ISocketInitializer;

/**
 * Created by gigemjt on 9/3/15.
 */
public interface CourseSketchRpcService extends Service {
    /**
     * Sets the object that initializes this service.
     * @param socketIntializer
     */
    public void setSocketInitializer(ISocketInitializer socketIntializer);
}
