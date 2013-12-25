package internalConnection;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.drafts.Draft;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class SolutionConnection extends WrapperConnection {

	public SolutionConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = MultiInternalConnectionServer.Decoder.parseRequest(buffer); // this contains the solution
		String[] sessionInfo = req.getSessionInfo().split("+");
		AnswerConnectionState state = (AnswerConnectionState) getStateFromId(sessionInfo[0]);
		if (req.getRequestType() == MessageType.DATA_REQUEST) {
			SrlExperiment expr = state.getExperiment(sessionInfo[1]);
			SrlSolution sol = null;
			try {
				sol = SrlSolution.parseFrom(req.getOtherData());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			// FIXME: implement comparison.
			// this could take a very very long time!

			// we need to this at least
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			getConnectionFromState(state).send(builder.build().toByteArray());
		} else if (req.getRequestType() == MessageType.SUBMISSION) {
			// pass up the Id to the client
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			getConnectionFromState(state).send(builder.build().toByteArray());
		}
	}
}