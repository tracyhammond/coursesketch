/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function Connection(uri, encrypted, attemptReconnect) {

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
	var timeoutVariable = false;
	var self = this;

	function createWebSocket() {
		try {
			websocket = new WebSocket(wsUri);
			websocket.binaryType = "arraybuffer"; // We are talking binary
			websocket.onopen = function(evt) {
				if (onOpen)
					onOpen(evt);
				if (timeoutVariable) {
					clearTimeout(timeoutVariable);
					timeoutVariable = false;
				}
			};

			websocket.onclose = function(evt) {
				websocket.close();
				if (onClose) {
					onClose(evt, attemptReconnect);
				} else {
					alert("Connection to server closed");
				}
				if (attemptReconnect) {
					console.log("going to attempt to reconnect in 3s");
					timeoutVariable = setTimeout(function() {console.log("attempting to reconnect now!");self.reconnect();}, 3000); 
				}
			};

			websocket.onmessage = function(evt) {
				try {
			        // Decode the Request
			        var msg = Request.decode(evt.data);
			        console.log("request decoded succesfully ");
			        if (msg.requestType == Request.MessageType.LOGIN && onLogin) {
			        	console.log("getting from login");
			        	onLogin(evt, msg);
			        } else if (msg.requestType == Request.MessageType.RECOGNITION && onRecognition) {
			        	console.log("getting from recognition");
			        	onRecognition(evt, msg);
			        } else if (msg.requestType == Request.MessageType.ANSWER_CHECKING && onAnswerChecker) {
			        	console.log("getting from answer checker");
			        	onAnswerChecker(evt, msg);
			        } else if (msg.requestType == Request.MessageType.DATA_REQUEST && onSchoolData) {
			        	console.log("getting from school data");
			        	onSchoolData(evt, msg);
			        } else if (onRequest)
			        	onRequest(evt, msg);
			    } catch (err) {
			    	console.error(err.stack);
			    	if (onError) {
			    		onError(evt,err);
			    	}
			    }
				// decode with protobuff and pass object to client
			};
			websocket.onerror = function(evt) {
				if (onError) {
					onError(evt,null);
				}
			};
		} catch(error) {
			console.error(error);
			if (onError) {
				onError(null,error);
			}
		}

	}

	this.reconnect = function() {
		console.log(websocket);
		websocket.close();

		createWebSocket();
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
			if (onError) {
				onError(null, err);
			}
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
			postFunction();
			buildSketch();
			buildUpdateList();
			buildDataQuery();
			if (!Long) {
				Long = dcodeIO.Long;
			}
			
		}

		function buildDataQuery() {
			var builder = ProtoBuf.protoFromFile(protobufDirectory + "data.proto");
			QueryBuilder = builder.build("protobuf").srl.query;
			if (!DataRequest)
				DataRequest = QueryBuilder.DataRequest;
			if (!ItemRequest)
				ItemRequest = QueryBuilder.ItemRequest;
		}
		
		function buildSchool() {
			var builder = ProtoBuf.protoFromFile(protobufDirectory + "school.proto");
			SchoolBuilder = builder.build("protobuf").srl.school;
			if (!SrlCourse)
				SrlCourse = SchoolBuilder.SrlCourse;
			if (!SrlAssignment)
				SrlAssignment = SchoolBuilder.SrlAssignment;
			if (!SrlProblem)
				SrlProblem = SchoolBuilder.SrlProblem;
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
			if (!ProtoSrlInterpretation)
				ProtoSrlInterpretation = sketchBuilder.Interpretation;
		}

		function buildUpdateList() {
			if (!ProtoUpdateCommand) {
				var builder = ProtoBuf.protoFromFile(protobufDirectory + "commands.proto");
				ProtoUpdateCommand = builder.build("protobuf").srl.commands;
			}

			if (!ProtoSrlUpdate)
				ProtoSrlUpdate = ProtoUpdateCommand.SrlUpdate;
			if (!ProtoSrlCommand)
				ProtoSrlCommand = ProtoUpdateCommand.SrlCommand;
			if (!ProtoSrlCommandType)
				ProtoSrlCommandType = ProtoUpdateCommand.CommandType;
			if (!IdChain)
				IdChain = ProtoUpdateCommand.IdChain;
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
	 * Given a protobuf Command object a Request is created.
	 */
	this.createRequestFromCommands = function(commands, requestType) {
		return this.createRequestFromUpdate(this.createUpdateFromCommands(commands), requestType);
	}

	/**
	 * Given a protobuf Command array an Update is created.
	 */
	this.createUpdateFromCommands = function(commands) {
		if (!isArray(commands)) {
			console.error(commands);
			throw 'Invalid Type Error: Input is not an Array';
		}
		var update = new ProtoSrlUpdate();
		update.setCommands(commands);
		var longVersion = Long.fromString("" + createTimeStamp());
		update.setTime(longVersion);
		update.setUpdateId(generateUUID());
		return update;
	}

	/**
	 * Given a protobuf object compile it to other data and return a request
	 */
	this.createRequestFromData = function(data, requestType) {
		var request = new Request();
		request.requestType = requestType;
		var buffer = data.toArrayBuffer();
		request.setOtherData(buffer);
		return request;
	}

	/**
	 * Given an Update a Request is created.
	 */
	this.createRequestFromUpdate = function(update, requestType) {
		var request = new Request();
		request.requestType = requestType;
		var buffer = update.toArrayBuffer();
		request.setOtherData(buffer);
		return request;
	}

	this.createBaseCommand = function(commandType, userCreated) {
    	var command = new ProtoSrlCommand();
    	var n = createTimeStamp();
    	var longVersion = Long.fromString("" + n);
		command.time = longVersion;
		command.commandType = commandType;
		command.isUserCreated = userCreated;
		command.commandId = generateUUID(); // unique ID
		return command;
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
var SchoolBuilder = false;

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
var ProtoSrlInterpretation = false;

/**
 * Update related protobufs.
 *
 * (capitol P because they classes)
 */
var ProtoUpdateCommand = false;
var ProtoSrlUpdate = false;
var ProtoSrlCommand = false;
var ProtoSrlCommandType = false;
var IdChain = false;

var QueryBuilder = false; // TODO: ADD THESE INTO PARENT COPY
var DataRequest = false;
var ItemRequest = false

const CONNECTION_LOST = 1006;
const INCORRECT_LOGIN = 4002;
const SERVER_FULL = 4001;
const protobufDirectory = "other/protobuf/";

/**
 * copy global parameters
 */
function copyParentProtos(scope) {
	copyParentValues(scope,'Long');

	copyParentValues(scope,'filesLoaded');
	copyParentValues(scope,'builder');
	copyParentValues(scope,'ProtoBuf');
	copyParentValues(scope,'Request');
	copyParentValues(scope,'LoginInformation');

	copyParentValues(scope,'SrlCourse');
	copyParentValues(scope,'SrlAssignment');
	copyParentValues(scope,'SrlProblem');
	copyParentValues(scope,'SchoolBuilder');

	copyParentValues(scope,'ProtoSrlSketch');
	copyParentValues(scope,'ProtoSrlObject');
	copyParentValues(scope,'ProtoSrlShape');
	copyParentValues(scope,'ProtoSrlStroke');
	copyParentValues(scope,'ProtoSrlPoint');
	copyParentValues(scope,'ProtoSrlInterpretation');

	copyParentValues(scope,'ProtoUpdateCommand');
	copyParentValues(scope,'ProtoSrlUpdate');
	copyParentValues(scope,'ProtoSrlCommand');
	copyParentValues(scope,'ProtoSrlCommandType');
	copyParentValues(scope,'IdChain');

	// so this can happen forever!
	copyParentValues(scope,'copyParentProtos');
}
