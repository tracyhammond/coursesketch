package handlers.subhandlers;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.institution.Institution;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.utilities.ExceptionUtilities;
import protobuf.srl.request.Message;
import protobuf.srl.services.grading.GradingServer;

public class AnswerCheckerGradingHandler extends GradingServer.GradingService implements CourseSketchRpcService {
    private Institution databaseReader;

    @Override
    public void insertRawGrade(RpcController controller, GradingServer.GradeRequest request,
            RpcCallback<protobuf.srl.request.Message.DefaultResponse> done) {
        Message.ProtoException protoException = null;
        try {
            databaseReader.addGrade(request.getAuthRequest(), request.getUserAuthId(), request.getGrade());
        } catch (AuthenticationException | DatabaseAccessException e) {
            protoException = ExceptionUtilities.createProtoException(e);
        }
        Message.DefaultResponse.Builder builder = Message.DefaultResponse.newBuilder();
        if (protoException != null) {
            builder.setException(protoException);
        } else {
            builder.setSuccessful(true);
        }
        done.run(builder.build());
    }

    @Override
    public AbstractCourseSketchDatabaseReader createDatabaseReader(ServerInfo serverInfo) {
        return null;
    }

    @Override
    public void onInitializeDatabases() {

    }

    @Override
    public void setDatabaseReader(AbstractCourseSketchDatabaseReader databaseReader) {
        this.databaseReader = (Institution) databaseReader;
    }

    @Override
    public void onInitialize() {

    }
}
