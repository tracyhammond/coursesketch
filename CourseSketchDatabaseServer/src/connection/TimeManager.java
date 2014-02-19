package connection;

import org.joda.time.DateTime;

import protobuf.srl.request.Message.Request;

public class TimeManager {
	
	private final static String SEND_TIME_TO_CLIENT_MSG = "1";
	private final static String CLIENT_REQUEST_LATENCY_MSG = "2";
	private final static String SEND_LATENCY_TO_CLIENT_MSG = "3";
	
	private static long latency = 0;
	private static long timeDifferance = 0;
	private static long totalTimeDifferance = 0;
	
	public static long getSystemTime() {
		return DateTime.now().getMillis() + totalTimeDifferance;
	}
	
	public static Request serverSendTimeToClient() {
		Request.Builder req = Request.newBuilder();
		req.setRequestType(Request.MessageType.TIME);
		req.setMessageTime(getSystemTime());
		req.setResponseText(SEND_TIME_TO_CLIENT_MSG); // Server sending client 'true' time
		return req.build();
	}
	
	public static Request clientReciveTimeDiff(Request req) {
		long startCounter = getSystemTime();
		
		timeDifferance = req.getMessageTime() - getSystemTime();
		
		Request.Builder rsp = Request.newBuilder();
		rsp.setRequestType(Request.MessageType.TIME);
		rsp.setMessageTime(req.getMessageTime()+(getSystemTime()-startCounter));
		rsp.setResponseText(CLIENT_REQUEST_LATENCY_MSG);
		
		return rsp.build();
	}
	
	public static Request decodeRequest(Request req) {
		if (req.getResponseText().equals(SEND_TIME_TO_CLIENT_MSG)){ //client
			return clientReciveTimeDiff(req);
		} else if (req.getResponseText().equals(CLIENT_REQUEST_LATENCY_MSG)) { //server
			return serverSendLatencyToClient(req);
		} else if (req.getResponseText().equals(SEND_LATENCY_TO_CLIENT_MSG)) { //client
			return clientReciveLatency(req);
		}
		return null;
	}
	
	public static Request serverSendLatencyToClient(Request req) {
		long latency = getSystemTime()-req.getMessageTime();
		
		Request.Builder rsp = Request.newBuilder();
		
		rsp.setRequestType(Request.MessageType.TIME);
		rsp.setMessageTime(latency/2);
		rsp.setResponseText(SEND_LATENCY_TO_CLIENT_MSG); 
		return rsp.build();
	}
	
	public static Request clientReciveLatency(Request req) {
		latency = req.getMessageTime();
		totalTimeDifferance=timeDifferance+latency;
		
		return null;
	}
}
