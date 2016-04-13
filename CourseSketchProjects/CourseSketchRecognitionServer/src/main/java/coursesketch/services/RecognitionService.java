package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;

/**
 * Created by David Windows on 4/13/2016.
 */
public class RecognitionService extends RecognitionServer.RecognitionService implements CourseSketchRpcService {
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        
    }

    @Override public void addUpdate(final RpcController controller, final RecognitionServer.AddUpdateRequest request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {

    }

    @Override public void createUpdateList(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {

    }

    @Override
    public void addTemplate(final RpcController controller, final RecognitionServer.RecognitionTemplate request,
            final RpcCallback<Message.DefaultResponse> done) {

    }
}
