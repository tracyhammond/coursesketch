/**
 * Creates a new connection to the wsUri.
 *
 * With this connection you can send information which is encoded via protobufs.
 */
function Connection(uri, encrypted, attemptReconnect) {

    console.log('Creating connection');
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
    var localScope = this;

    var totalTimeDifferance = dcodeIO.Long.fromInt(0);
    var timeDifferance = dcodeIO.Long.fromInt(0);
    var latency = dcodeIO.Long.fromInt(0);
    var SEND_TIME_TO_CLIENT_MSG = '1';
    var CLIENT_REQUEST_LATENCY_MSG = '2';
    var SEND_LATENCY_TO_CLIENT_MSG = '3';

    /**
     * Creates a websocket and adds typical websocket methods to it.
     */
    function createWebSocket() {
        try {
            console.log('Creating socket at ' + wsUri);
            websocket = new WebSocket(wsUri);
            websocket.binaryType = 'arraybuffer'; // We are talking binary
            /**
             * Called when the websocekt opens.
             *
             * @param {Event} evt An event containing data about opening.
             */
            websocket.onopen = function(evt) {
                connected = true;
                if (onOpen) {
                    onOpen(evt);
                }
                if (timeoutVariable) {
                    clearTimeout(timeoutVariable);
                    timeoutVariable = false;
                }
            };

            /**
             * Called when the websocket closes.
             *
             * @param {Event} evt An event containing data about closing.
             */
            websocket.onclose = function(evt) {
                connected = false;
                websocket.close();
                if (onClose) {
                    onClose(evt, attemptReconnect);
                } else {
                    alert('Connection to server closed');
                }
                if (attemptReconnect) {
                    console.log('going to attempt to reconnect in 3s');
                    timeoutVariable = setTimeout(function() {
                        console.log('attempting to reconnect now!');
                        localScope.reconnect();
                    }, 3000);
                }
            };

            /**
             * Called when the websocket receives a message.
             *
             * @param {Event} evt An event containing data about receiving a message.
             */
            websocket.onmessage = function(evt) {
                /*jshint maxcomplexity:15 */
                try {
                    var MessageType = CourseSketch.prutil.getRequestClass().MessageType;
                    // Decode the Request
                    var msg = CourseSketch.prutil.getRequestClass().decode(evt.data);
                    // console.log('request decoded succesfully ');
                    if (msg.requestType === MessageType.TIME) {
                        console.log('getting from time');
                        var rsp = onTime(evt, msg);
                        if (rsp !== null && !isUndefined(rsp)) {
                            localScope.sendRequest(rsp);
                        }
                    } else if (msg.requestType === MessageType.LOGIN && onLogin) {
                        console.log('getting from login');
                        onLogin(evt, msg);
                    } else if (msg.requestType === MessageType.RECOGNITION && onRecognition) {
                        console.log('getting from recognition');
                        onRecognition(evt, msg);
                    } else if (msg.requestType === MessageType.SUBMISSION && onSubmission) {
                        console.log('getting from submission');
                        onSubmission(evt, msg);
                    } else if (msg.requestType === MessageType.FEEDBACK && onAnswerChecker) {
                        console.log('getting from answer checker');
                        onAnswerChecker(evt, msg);
                    } else if ((msg.requestType === MessageType.DATA_REQUEST || msg.requestType === MessageType.DATA_INSERT ||
                            msg.requestType === MessageType.DATA_UPDATE || msg.requestType === MessageType.DATA_REMOVE) && onSchoolData) {
                        console.log('getting from school data');
                        //console.log(msg);
                        onSchoolData(evt, msg);
                    } else if (msg.requestType === MessageType.ERROR) {
                        var exception = CourseSketch.prutil.decodeProtobuf(msg.getOtherData(),
                            CourseSketch.prutil.getProtoExceptionClass());

                        console.log('exception object', exception);
                        console.log(msg.getResponseText());

                        CourseSketch.showShallowException(exception);
                        if (onError) {
                            onError(evt, msg.getResponseText());
                        }
                    } else if (onRequest) {
                        onRequest(evt, msg);
                    }
                } catch (err) {
                    console.error(err.stack);
                    if (onError) {
                        onError(evt, err);
                    }
                }
                // Decode with protobuff and pass object to client
            };

            /**
             * Called when the websocket throws an error.
             *
             * @param {Event} evt An event containing data about the error.
             */
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

    /**
     * Sets a listener to listen to login events.
     *
     * @param {Function} listener the function that is called when the client receives a login message from the server
     */
    this.setLoginListener = function(listener) {
        onLogin = listener;
    };

    /**
     * Sets a listener to listen to recognition events.
     *
     * @param {Function} listener the function that is called when the client receives a recognition message from the server.
     */
    this.setRecognitionListener = function(listener) {
        onRecognition = listener;
    };

    /**
     * Sets a listener to listen to answer checker events.
     *
     * @param {Function} listener the function that is called when the client receives a answer checker message from the server.
     */
    this.setAnswerCheckingListener = function(listener) {
        onAnswerChecker = listener;
    };

    /**
     * Sets a listener to listen to submission events.
     *
     * @param {Function} listener the function that is called when the client receives a submission message from the server.
     */
    this.setSubmissionListener = function(listener) {
        onSubmission = listener;
    };

    /**
     * Sets a listener to listen to submission events.
     *
     * @param {Function} listener the function that is called when the client receives a submission message from the server.
     */
    this.setSchoolDataListener = function(listener) {
        onSchoolData = listener;
    };

    /**
     * Sets a listener to listen to a connection opening.
     *
     * @param {Function} listener the function that is called when the client opens a connection.
     */
    this.setOnOpenListener = function(listener) {
        onOpen = listener;
    };

    /**
     * Sets a listener to listen to a connection closing.
     *
     * @param {Function} listener the function that is called when the client closes a connection.
     */
    this.setOnCloseListener = function(listener) {
        onClose = listener;
    };

    /**
     * Sets a listener to listen to any message.
     *
     * @param {Function} listener the function that is called when the client receives any message.
     */
    this.setOnMessageListener = function(listener) {
        onRequest = listener;
    };

    /**
     * Sets a listener to listen to an error event from the server.
     *
     * @param {Function} listener the function that is called when the client receives an error from the server.
     */
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
        } catch (err) {
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
                data: message.toArrayBuffer()
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
        var longVersion = dcodeIO.Long.fromString('' + (createTimeStamp() + totalTimeDifferance));
        return longVersion;
    };

    CourseSketch.getCurrentTime = this.getCurrentTime;

    /**
     * Called to synchronize time events.
     *
     * @param {Event} evt An event about receiving a message
     * @param {Request} msg The message the contains timing data.
     * @returns {Request|Undefined} returns the time to send or null if no time is being sent
     */
    function onTime(evt, msg) {
        if (msg.getResponseText() === SEND_TIME_TO_CLIENT_MSG) { // client
            return clientReciveTimeDiff(msg);
        } else if (msg.getResponseText() === SEND_LATENCY_TO_CLIENT_MSG) { // client
            return clientReciveLatency(msg);
        }
        return undefined;
    }

    /**
     * Computes the time difference and returns a request for the latency.
     *
     * Called when the server returns the request for the time difference.
     * @param {Request} req the time request
     * @returns {Request} A request that specifies a request to the server to return latency.
     */
    function clientReciveTimeDiff(req) {
        var startCounter = localScope.getCurrentTime();
        timeDifferance = dcodeIO.Long.fromString('' + req.getMessageTime()).subtract(localScope.getCurrentTime());
        var rsp = CourseSketch.prutil.Request();
        rsp.setRequestType(CourseSketch.prutil.getRequestClass().MessageType.TIME);
        rsp.setMessageTime(dcodeIO.Long.fromString('' + req.getMessageTime()).add(localScope.getCurrentTime().subtract(startCounter)));
        rsp.setResponseText(CLIENT_REQUEST_LATENCY_MSG);
        rsp.setRequestId(generateUUID());
        return rsp;
    }

    /**
     * Saves the offset locally.
     *
     * @param {Request} req The time request.
     * @returns {Undefined} No more server actions are needed.  Return null to denote that.
     */
    function clientReciveLatency(req) {
        latency = dcodeIO.Long.fromString('' + req.getMessageTime());
        totalTimeDifferance = timeDifferance.add(latency);
        return undefined;
    }
}

makeValueReadOnly(Connection.prototype, 'CONNECTION_LOST', 1006);
makeValueReadOnly(Connection.prototype, 'INCORRECT_LOGIN', 4002);
makeValueReadOnly(Connection.prototype, 'SERVER_FULL', 4001);
