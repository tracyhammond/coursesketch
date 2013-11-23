package internalConnections;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import protobuf.srl.request.Message.Request;
import proxyServer.ConnectionState;
import proxyServer.DataClient;
import proxyServer.Encoder;
import proxyServer.ProxyServer;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ManagerConnection{

	private boolean connectionType = CONNECT_LOCALLY;
	public static final boolean CONNECT_LOCALLY = true;
	public static final boolean CONNECT_REMOTE = false;
	RecognitionConnection recognition = null;
	LoginConnection logindata = null;

	public ConnectionState connection = null;
	ProxyServer parent;
	public ManagerConnection(ProxyServer parent) {
		this.parent = parent;
	}
	
	
	public static RecognitionConnection connectRecognition(ProxyServer serv, boolean local) {
		RecognitionConnection c=null;
		String location = local ? "ws://localhost:8888" : "ws://goldberglinux02.tamu.edu:8888";
		try {
			c = new RecognitionConnection( new URI( location ), new Draft_10() , serv);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		return c;
	}
	
	public static DataClient connectData(ProxyServer serv, boolean local) {
		DataClient c=null;
		String location = local ? "ws://localhost:8885" : "ws://goldberglinux.tamu.edu:8885";
		try {
			c = new DataClient( new URI( location ), new Draft_10() , serv);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		return c;
	}
	
	public static LoginConnection connectLogin(ProxyServer serv, boolean local) {
		LoginConnection c=null;
		String location = local ? "ws://localhost:8886" : "ws://goldberglinux.tamu.edu:37771";
		try {
			c = new LoginConnection( new URI( location ), new Draft_10() , serv);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		return c;
	}
	
	public void send(Request req, String userID){
		Request packagedRequest = Encoder.requestIDBuilder(req, userID);		//Attach the existing request with the UserID
		getRecognitionClient().send(packagedRequest.toByteArray());
	}

	public void connectServers(ProxyServer serv) {
		System.out.println("Open Recognition...");
		recognition = connectRecognition(serv, connectionType);
		System.out.println("Open Data/Login...");
		logindata = connectLogin(serv, connectionType);
	}
	
	public RecognitionConnection getRecognitionClient(){
		return recognition;
	}
	
	public LoginConnection getLoginClient(){
		return logindata;
	}

}