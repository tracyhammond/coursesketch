/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function Connection(uri, encrypted) {
	
	var onOpen;
	var onClose;
	var onRequest;
	var onError;
	var websocket;
	var wsUri = (encrypted?'wss://' : 'ws://') + uri;

	function createWebSocket() {
		try {
			websocket = new WebSocket(wsUri);
			websocket.binaryType = "arraybuffer"; // We are talking binary
			websocket.onopen = function(evt) {
				if (onOpen)
					onOpen(evt);
			};
			websocket.onclose = function(evt) {
				if (onClose)
					onClose(evt);
			};
			websocket.onmessage = function(evt) {
				try {
			        // Decode the Request
			        var msg = Request.decode(evt.data);
			        if (onRequest)
			        	onRequest(evt, msg);
			    } catch (err) {
			    	onError(evt,err);
			    }
				// decode with protobuff and pass object to client
	
			};
			websocket.onerror = function(evt) {
				if (onError)
					onError(evt,error);
			};
		} catch(error) {
			onError(null,error);
		}
		
	}

	/**
	 * Sets the listeners for the different functions:
	 * 
	 * On Open - called when the connection is open. Recieves event object.  (called after everything is set up too)
	 * On Close - called when the connection is closed. Recieves event object.
	 * On Recieve - called the client recieves a message. Recieves event object and message Object.
	 * On Error - called when an error is thrown. Recieves event object.  It may be passed an error object.
	 */
	this.setListeners = function(open, close, message, error) {
		onOpen = open;
		onClose = close;
		onRequest = message;
		onError = error;
	};

	this.setOnOpenListener = function(listener) {
		onOpen = listener;
	};

	this.setOnCloseListener = function(listener) {
		onClose = listener;
	};

	this.setOnMessageListener = function(listener) {
		onRequest = listener;
	};

	this.setOnErrorListener = function(listener) {
		onError = listener;
	};
	
	/**
	 * Given a Request object (message defined in proto), send it over the wire.
	 *
	 * The message must be a protobuf object.
	 */
	this.sendRequest = function(message) {
		try {
		websocket.send(message.toArrayBuffer());
		} catch(err) {
			onError(null, err);
		}
	};

	/**
	 * This is a test function that allows you to spoof messages to yourself.
	 *
	 * Only the data is the same right now.
	 * The message is delayed but the function returns immediately.
	 * TODO: complete the entirety of the event that can be spoofed.
	 */
	this.sendSelf = function(message) {
		var event =  { data : message.toArrayBuffer()};
		setTimeout(function() {websocket.onmessage(event);},500);
	};
	
	/**
	 * Closes the websocket.
	 *
	 * Also performs other closing tasks.
	 */
	this.close = function() {
		websocket.close();
	};

	function protobufSetup(postLoadedFunction) {		
		var barrierCount = 0;
		var postFunction = postLoadedFunction;
		function load1() {
			loader.loadFile("js/connection/libraries/Long.min.js",'js',loadBarrier.bind(this));
			loader.loadFile("js/connection/libraries/ByteBuffer.min.js",'js',load2.bind(this));
		}

		function load2() {
			barrierCount++;
			loader.loadFile("js/connection/libraries/ProtoBuf.min.js",'js',loadBarrier.bind(this));
		}

		function loadBarrier() {
			barrierCount++;
			if(barrierCount == 3) {
				//Next Function.
				initializeBuf();
				filesLoaded = true;
			}
		}

		function initializeBuf() {
			if (!ProtoBuf) {
				ProtoBuf = dcodeIO.ProtoBuf;
			}
			if (!builder) {
				builder = ProtoBuf.protoFromFile("other/message.proto");
			}
			if (!Request) {
				var requestPackage = builder.build("protobuf").srl.request;
				Request = requestPackage.Request;
				LoginInformation = requestPackage.LoginInformation;
			}
			buildSchool();
			buildSketch();
			postFunction();
		}

		function buildSchool() {
			var schoolBuilder = ProtoBuf.protoFromFile("other/school.proto");
			if(!SRL_Course)
				SRL_Course = schoolBuilder.build('SRL_Course');
			if(!SRL_Assignment)
				SRL_Assignment = schoolBuilder.build('SRL_Assignment');
			if(!SRL_Problem)
				SRL_Problem = schoolBuilder.build('SRL_Problem');
		}

		function buildSketch() {
			var schoolBuilder = ProtoBuf.protoFromFile("other/sketch.proto");
			if(!Proto_SRL_Sketch)
				Proto_SRL_Sketch = schoolBuilder.build('SRL_Sketch');
			if(!Proto_SRL_Object)
				Proto_SRL_Object = schoolBuilder.build('SRL_Object');
			if(!Proto_SRL_Point)
				Proto_SRL_Point = schoolBuilder.build('SRL_Point');
		}
		load1();
	}

	if(!(filesLoaded && builder && ProtoBuf && Request)) {
		new protobufSetup(createWebSocket.bind(this));
	}
}

var filesLoaded = false;
var builder = false;
var ProtoBuf = false;
var Request = false;
var LoginInformation = false;
var SRL_Course = false;
var SRL_Assignment = false;
var SRL_Problem = false;
var Proto_SRL_Sketch = false;
var Proto_SRL_Object = false;
var Proto_SRL_Stroke = false;
var Proto_SRL_Point = false;
const CONNECTION_LOST = 1006;
const INCORRECT_LOGIN = 4002;
const SERVER_FULL = 4001;
function copyProtosFromParentProtos() {
	filesLoaded = parent.filesLoaded;
	builder = parent.builder;
	ProtoBuf = parent.ProtoBuf;
	Request = parent.Request;
	LoginInformation = parent.LoginInformation;
	SRL_Course = parent.SRL_Course;
	SRL_Assignment = parent.SRL_Assignment;
	SRL_Problem = parent.SRL_Problem;
	Proto_SRL_Sketch = parent.Proto_SRL_Sketch;
	Proto_SRL_Object = parent.Proto_SRL_Object;
	Proto_SRL_Point = parent.Proto_SRL_Point;
}
