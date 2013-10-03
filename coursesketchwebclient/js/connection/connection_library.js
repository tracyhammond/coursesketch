/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function connection(uri, encrypted) {
	
	var onOpen;
	var onClose;
	var onMessage;
	var onError;
	var websocket;
	var wsUri = (encrypted?'wss://' : 'ws://') + uri;

	function createWebSocket() {
		try {
			websocket = new WebSocket(wsUri);
			websocket.binaryType = "arraybuffer"; // We are talking binary
			websocket.onopen = function(evt) {
				onOpen(evt);
			};
			websocket.onclose = function(evt) {
				onClose(evt);
			};
			websocket.onmessage = function(evt) {
				try {
			        // Decode the Message
			        var msg = Message.decode(evt.data);
					onMessage(evt, msg);
			    } catch (err) {
			    	onError(evt,err);
			    }
				// decode with protobuff and pass object to client
	
			};
			websocket.onerror = function(evt) {
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
		onMessage = message;
		onError = error;
	}

	/**
	 * Given a Message object (message defined in proto), send it over the wire.
	 *
	 * The message must be a protobuf object.
	 */
	this.sendMessage = function(message) {
		try {
		websocket.send(message.toArrayBuffer());
		} catch(err) {
			onError(null, err);
		}
	}

	/**
	 * Closes the websocket.
	 *
	 * Also performs other closing tasks.
	 */
	this.close = function() {
		websocket.close();
	}

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
				builder = ProtoBuf.protoFromFile("other/test.proto");
			}
			if (!Message) {
				Message = builder.build("Message");
			}
			postFunction();
		}
		
		load1();
	}

	if(!(filesLoaded && builder && ProtoBuf && Message)) {
		new protobufSetup(createWebSocket.bind(this));
	}
}

var filesLoaded = false;
var builder = false;
var ProtoBuf = false;
var Message = false; 