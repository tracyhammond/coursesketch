/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function Connection(uri, encrypted, attemptReconnect) {

    console.log("Creating connection");
	var connected = false;
	var onOpen = false;
	var onClose = false;
	var onRequest = false;
	var onLogin = false;
	var onRecognition = false;
	var onAnswerChecker = false;
	var onSubmission = false;
	var onSchoolData = false;
	var onError = false;

	var websocket;
	var wsUri = (encrypted?'wss://' : 'ws://') + uri;
	var timeoutVariable = false;
	var self = this;

	var totalTimeDifferance = dcodeIO.Long.fromInt(0);
    var timeDifferance = dcodeIO.Long.fromInt(0);
    var latency = dcodeIO.Long.fromInt(0);
    var SEND_TIME_TO_CLIENT_MSG = "1";
    var CLIENT_REQUEST_LATENCY_MSG = "2";
    var SEND_LATENCY_TO_CLIENT_MSG = "3";

	/**
	 * Creates a websocket and adds typical websocket methods to it.
	 */
	function createWebSocket() {
        try {
            console.log("Creating socket at " + wsUri);
            websocket = new WebSocket(wsUri);
            websocket.binaryType = "arraybuffer"; // We are talking binary
            websocket.onopen = function(evt) {
                connected = true;
                if (onOpen) onOpen(evt);
                if (timeoutVariable) {
                    clearTimeout(timeoutVariable);
                    timeoutVariable = false;
                }
            };

            websocket.onclose = function(evt) {
                connected = false;
                websocket.close();
                if (onClose) {
                    onClose(evt, attemptReconnect);
                } else {
                    alert("Connection to server closed");
                }
                if (attemptReconnect) {
                    console.log("going to attempt to reconnect in 3s");
                    timeoutVariable = setTimeout(function() {
                        console.log("attempting to reconnect now!");
                        self.reconnect();
                    }, 3000);
                }
            };
            websocket.onmessage = function(evt) {
                try {
                    var MessageType = CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType;
                    // Decode the Request
                    var msg = CourseSketch.PROTOBUF_UTIL.getRequestClass().decode(evt.data);
                    // console.log("request decoded succesfully ");
                    if (msg.requestType == MessageType.TIME) {
                        console.log("getting from time");
                        var rsp = onTime(evt, msg)
                        if (rsp != null) self.sendRequest(rsp);
                    } else if (msg.requestType == MessageType.LOGIN && onLogin) {
                        console.log("getting from login");
                        onLogin(evt, msg);
                    } else if (msg.requestType == MessageType.RECOGNITION && onRecognition) {
                        console.log("getting from recognition");
                        onRecognition(evt, msg);
                    } else if (msg.requestType == MessageType.SUBMISSION && onSubmission) {
                        console.log("getting from submission");
                        onSubmission(evt, msg);
                    } else if (msg.requestType == MessageType.FEEDBACK && onAnswerChecker) {
                        console.log("getting from answer checker");
                        onAnswerChecker(evt, msg);
                    } else if ((msg.requestType == MessageType.DATA_REQUEST || msg.requestType == MessageType.DATA_INSERT
                        || msg.requestType == MessageType.DATA_UPDATE || msg.requestType == MessageType.DATA_REMOVE) && onSchoolData) {
                        console.log("getting from school data");
                        console.log(msg);
                        onSchoolData(evt, msg);
                    } else if (msg.requestType == MessageType.ERROR) {
                        console.log(msg.getResponseText());
                        if (onError) {
                            onError(evt, msg.getResponseText());
                        }
                        alert("ERROR: " + msg.getResponseText());
                    } else if (onRequest) onRequest(evt, msg);
                } catch (err) {
                    console.error(err.stack);
                    if (onError) {
                        onError(evt, err);
                    }
                }
                // decode with protobuff and pass object to client
            };
            websocket.onerror = function(evt) {
                if (onError) {
                    onError(evt, null);
                }
            };
        } catch (error) {
            console.error(error);
            if (onError) {
                onError(null, error);
            }
        }
    }

	/**
	 * Attempts to restart the websocket so it can be reused.
	 */
	this.reconnect = function() {
	    if (!isUndefined(websocket)) {
	        websocket.close();
	    }
		createWebSocket();
	};

	/**
	 * Returns true if the websocket is connected correctly.
	 */
	this.isConnected = function() {
		return connected;
	};

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
		onAnswerChecker = listener;
	};

	this.setSubmissionListener = function(listener) {
		onSubmission = listener;
	};

	this.setSchoolDataListener = function(listener) {
		onSchoolData = listener;
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
			console.error(err);
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
	    setTimeout(function() {
            var event = {
                data : message.toArrayBuffer()
            };
            websocket.onmessage(event);
        }, 100);
    };
	/**
	 * Closes the websocket.
	 *
	 * Also performs other closing tasks.
	 */
	this.close = function() {
		websocket.close();
	};

	/**
	 * Gets the current time that is the same as the time the server sees.
	 */
	this.getCurrentTime = function() {
		var longVersion = dcodeIO.Long.fromString("" + (createTimeStamp() + totalTimeDifferance));
		return longVersion;
	};

	CourseSketch.getCurrentTime = this.getCurrentTime;

	function onTime(evt, msg) {
        if (msg.getResponseText() == SEND_TIME_TO_CLIENT_MSG) { // client
            return clientReciveTimeDiff(msg);
        } else if (msg.getResponseText() == SEND_LATENCY_TO_CLIENT_MSG) { // client
            return clientReciveLatency(msg);
        }
        return null;
    }

    function clientReciveTimeDiff(req) {
        var startCounter = self.getCurrentTime();
        timeDifferance = dcodeIO.Long.fromString("" + req.getMessageTime()).subtract(self.getCurrentTime());
        var rsp = CourseSketch.PROTOBUF_UTIL.Request();
        rsp.setRequestType(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.TIME);
        rsp.setMessageTime(dcodeIO.Long.fromString("" + req.getMessageTime()).add(self.getCurrentTime().subtract(startCounter)));
        rsp.setResponseText(CLIENT_REQUEST_LATENCY_MSG);

        return rsp;
    }

    function clientReciveLatency(req) {
        latency = dcodeIO.Long.fromString("" + req.getMessageTime());
        totalTimeDifferance = timeDifferance.add(latency);
        return null;
    }
}

makeValueReadOnly(Connection.prototype, "CONNECTION_LOST", 1006);
makeValueReadOnly(Connection.prototype, "INCORRECT_LOGIN", 4002);
makeValueReadOnly(Connection.prototype, "SERVER_FULL", 4001);
