/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function Connection(uri, encrypted) {

	var onOpen;
	var onClose;
	var onRequest = false;
	var onLogin = false;
	var onRecognition = false;
	var onAnswerChecker = false;
	var onSchoolData = false;
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
				else {
					alert("Connection to server closed");
				}
			};
			websocket.onmessage = function(evt) {
				try {
			        // Decode the Request
			        var msg = Request.decode(evt.data);
			        console.log("request decoded succesfully");
			        if (msg.requestType == Request.MessageType.LOGIN && onLogin) {
			        	onLogin(evt, msg);
			        } else if (msg.requestType == Request.MessageType.RECOGNITION && onRecognition) {
			        	onRecognition(evt, msg);
			        } else if (msg.requestType == Request.MessageType.ANSWER_CHECKING && onAnswerChecker) {
			        	onAnswerChecker(evt, msg);
			        } else if (msg.requestType == Request.MessageType.DATA_REQUEST && onSchoolData) {
			        	onSchoolData(evt, msg);
			        }else if (onRequest)
			        	onRequest(evt, msg);
			    } catch (err) {
			    	console.error(err);
			    	onError(evt,err);
			    }
				// decode with protobuff and pass object to client
			};
			websocket.onerror = function(evt) {
				if (onError)
					onError(evt,null);
			};
		} catch(error) {
			console.error(error);
			if (onError) {
				onError(null,error);
			}
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

	this.setLoginListener = function(listener) {
		onLogin = listener;
	};

	this.setRecognitionListener = function(listener) {
		onRecognition = listener;
	};

	this.setAnswerCheckingListener = function(listener) {
		ononAnswerChecker = listener;
	};
	
	this.setSchoolDataListener = function(listener) {
		onSchoolData = listener;
	}

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
			//loader.loadFile("js/connection/libraries/Long.min.js",'js',loadBarrier.bind(this));
			//loader.loadFile("js/connection/libraries/ByteBuffer.min.js",'js',loadBarrier.bind(this));
			loadBarrier();
			loadBarrier();
		}

		function load2() {
			//loader.loadFile("js/connection/libraries/Protobuf.min.js",'js',loadBarrier.bind(this));
			loadBarrier();
		}

		function loadBarrier() {
			barrierCount++;
			if (barrierCount == 2) {
				load2();
			}else if (barrierCount == 3) {
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
				builder = ProtoBuf.protoFromFile(protobufDirectory + "message.proto");
			}
			if (!Request) {
				var requestPackage = builder.build("protobuf").srl.request;
				Request = requestPackage.Request;
				LoginInformation = requestPackage.LoginInformation;
			}
			buildSchool();
			//buildSketch();
			postFunction();
			buildSketch();
			buildUpdateList();
			if (!Long) {
				Long = dcodeIO.Long;
			}
			
		}

		function buildSchool() {
			var builder = ProtoBuf.protoFromFile(protobufDirectory + "school.proto");
			var schoolBuilder = builder.build("protobuf").srl.school;
			if (!SrlCourse)
				SrlCourse = schoolBuilder.SrlCourse;
			if (!SrlAssignment)
				SrlAssignment = schoolBuilder.SrlAssignment;
			if (!SrlProblem)
				SrlProblem = schoolBuilder.SrlProblem;
		}

		function buildSketch() {
			if (!sketchBuilder) {
				var builder = ProtoBuf.protoFromFile(protobufDirectory + "sketch.proto");
				sketchBuilder = builder.build("protobuf").srl.sketch;
			}	

			if (!ProtoSrlSketch)
				ProtoSrlSketch = sketchBuilder.SrlSketch;
			if (!ProtoSrlObject)
				ProtoSrlObject = sketchBuilder.SrlObject;
			if (!ProtoSrlShape)
				ProtoSrlShape = sketchBuilder.SrlShape;
			if (!ProtoSrlStroke)
				ProtoSrlStroke = sketchBuilder.SrlStroke;
			if (!ProtoSrlPoint)
				ProtoSrlPoint = sketchBuilder.SrlPoint;
		}

		function buildUpdateList() {
			if (!ProtoUpdateCommand) {
				var builder = ProtoBuf.protoFromFile(protobufDirectory + "commands.proto");
				ProtoUpdateCommand = builder.build("protobuf").srl.commands;
			}

			if (!ProtoSrlUpdate)
				ProtoSrlUpdate = ProtoUpdateCommand.Update;
			if (!ProtoSrlCommand)
				ProtoSrlCommand = ProtoUpdateCommand.Command;
			if (!ProtoSrlCommandType)
				ProtoSrlCommandType = ProtoUpdateCommand.CommandType;
		}
		/*
		function testRepeated() {
			console.log("WORKING");
	        var builder = ProtoBuf.protoFromFile(protobufDirectory+"/test.proto");
	        var root = builder.build("protobuf");
	        Outer = root.Outer;
	        Inner = root.Inner;
	        var inners = new Array();

	        // Array of repeated messages
	        inners.push(new Inner("a"), new Inner("b"), new Inner("c"));
	        var outer = new Outer();
	        outer.setInners(inners);

	        // Array of repeated message objects
	        inners = new Array();
	        inners.push({ str: 'a' }, { str: 'b' }, { str: 'c' });
	        console.log("WORKING");
	        outer.setInners(inners); // Converts
	        console.log("FINISHED WORKING");
		}
		*/
		load1();
	}

	if(!(filesLoaded && builder && ProtoBuf && Request)) {
		new protobufSetup(createWebSocket.bind(this));
	}

	/**
	 * Given a protobuf command object a request is created.
	 */
	this.createRequestFromCommand = function(command, requestType) {
		var request = new Request();
		request.requestType = requestType;
		var update = new ProtoSrlUpdate();
		var array = new Array();
		array.push(command);
		update.setCommands(array);
		var longVersion = Long.fromString("" + createTimeStamp());
		update.setTime(longVersion);
		update.setUpdateId(generateUUID());
		var buffer = update.toArrayBuffer();
		request.setOtherData(buffer);
		return request;
	}
}
var Long = false;

var filesLoaded = false;
var builder = false;
var ProtoBuf = false;
var Request = false;
var LoginInformation = false;

/**
 * school related protobufs.
 */
var SrlCourse = false;
var SrlAssignment = false;
var SrlProblem = false;

/**
 * Sketch related protobufs.
 *
 * (capitol P because they classes)
 */
var sketchBuilder = false;
var ProtoSrlSketch = false;
var ProtoSrlObject = false;
var ProtoSrlShape = false;
var ProtoSrlStroke = false;
var ProtoSrlPoint = false;

/**
 * Update related protobufs.
 *
 * (capitol P because they classes)
 */
var ProtoUpdateCommand = false;
var ProtoSrlUpdate = false;
var ProtoSrlCommand = false;
var ProtoSrlCommandType = false;

const CONNECTION_LOST = 1006;
const INCORRECT_LOGIN = 4002;
const SERVER_FULL = 4001;
const protobufDirectory = "other/protobuf/";

/**
 * copy global parameters
 */
function copyProtosFromParentProtos() {
	Long = parent.Long;
	
	filesLoaded = parent.filesLoaded;
	builder = parent.builder;
	ProtoBuf = parent.ProtoBuf;
	Request = parent.Request;
	LoginInformation = parent.LoginInformation;

	SrlCourse = parent.SrlCourse;
	SrlAssignment = parent.SrlAssignment;
	SrlProblem = parent.SrlProblem;

	ProtoSrlSketch = parent.ProtoSrlSketch;
	ProtoSrlObject = parent.ProtoSrlObject;
	ProtoSrlShape = parent.ProtoSrlShape;
	ProtoSrlStroke = parent.ProtoSrlStroke;
	ProtoSrlPoint = parent.ProtoSrlPoint;

	ProtoUpdateCommand = parent.ProtoUpdateCommand;
	ProtoSrlUpdate = parent.ProtoSrlUpdate;
	ProtoSrlCommand = parent.ProtoSrlCommand;
	ProtoSrlCommandType = parent.ProtoSrlCommandType;
}

 /**
  * Generates an rfc4122 version 4 compliant solution.
  *
  * found at http://stackoverflow.com/a/2117523/2187510
  * and further improved at
  * http://stackoverflow.com/a/8809472/2187510
  */
function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
    });
    return uuid;
};

// Creates a time stamp every time this method is called.
function createTimeStamp() {
	return new Date().getTime();
}
