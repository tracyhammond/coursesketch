package coursesketch.server.rpc;

import com.google.protobuf.Service;
import coursesketch.server.interfaces.ISocketInitializer;

/**
 * An Rpc Service that contains specific fields that all CourseSketch Rpc Services should have.
 *
 * Created by gigemjt on 9/3/15.
 */
public interface CourseSketchRpcService extends Service {
    /**
     * Sets the object that initializes this service.
     * @param socketInitializer The {@link ISocketInitializer} that contains useful data for any RpcService used by CourseSketch.
     */
    void setSocketInitializer(final ISocketInitializer socketInitializer);
}
