package handlers;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlExperimentList;

import com.google.protobuf.InvalidProtocolBufferException;

import database.DatabaseClient;

public class DataRequestHandler {
	public static Request handleRequest(Request req) {
		System.out.println("Parsing data request!");
		DataRequest dataReq;
		try {
			dataReq = DataRequest.parseFrom(req.getOtherData());
			Request.Builder resultReq = Request.newBuilder(req);
			resultReq.clearOtherData();
			DataResult.Builder builder = DataResult.newBuilder();
			try {
				for (ItemRequest itemReq: dataReq.getItemsList()) {
					if (itemReq.getQuery() == ItemQuery.EXPERIMENT) {
						if (!itemReq.hasAdvanceQuery()) {
							builder.addResults(handleSingleExperiment(itemReq));
						} else {
							builder.addResults(getExperimentsForInstructor(itemReq));
						}
						//conn.send(resultReq.build().toByteArray());
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
				Request.Builder build = Request.newBuilder();
				build.setRequestType(Request.MessageType.ERROR);
				build.setResponseText(e.getMessage());
				build.setSessionInfo(req.getSessionInfo());
				return build.build();
			}
			resultReq.setOtherData(builder.build().toByteString());
			resultReq.setRequestType(MessageType.DATA_REQUEST);
			return resultReq.build();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			Request.Builder build = Request.newBuilder();
			build.setRequestType(Request.MessageType.ERROR);
			build.setResponseText(e.getMessage());
			build.setSessionInfo(req.getSessionInfo());
			return build.build();
		}
	}

	private static ItemResult handleSingleExperiment(ItemRequest itemReq) {
		System.out.println("attempting to get an experiment!");
		SrlExperiment experiment = null;
		String errorMessage = "";
		try {
			experiment = DatabaseClient.getExperiment(itemReq.getItemId(0));
		} catch (Exception e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
		}

		ItemResult.Builder send = ItemResult.newBuilder();
		send.setQuery(ItemQuery.EXPERIMENT);

		if (experiment != null) {
			send.setData(experiment.toByteString());
		} else {
			send.setNoData(true);
			send.setErrorMessage(errorMessage);
			//error stuff
		}
		return send.build();
	}

	private static ItemResult getExperimentsForInstructor(ItemRequest itemReq) {
		System.out.println("attempting to get an experiment!");
		SrlExperimentList.Builder experiments = SrlExperimentList.newBuilder();
		String errorMessage = "";
		for (String item : itemReq.getItemIdList()) {
			try {
				experiments.addExperiments(DatabaseClient.getExperiment(item));
			} catch (Exception e) {
				errorMessage += e.getMessage();
				e.printStackTrace();
			}
		}

		ItemResult.Builder send = ItemResult.newBuilder();
		send.setQuery(ItemQuery.EXPERIMENT);
		send.setData(experiments.build().toByteString());
		send.setErrorMessage(errorMessage);
		send.setAdvanceQuery(itemReq.getAdvanceQuery());
		return send.build();
	}
}
