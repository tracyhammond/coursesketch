/**
 * Allows for more separation for the Data Result.
 *
 * What a listener receives:
 * <ul>
 * <li>an event that is specified by websocket event protocol</li>
 * <li>an ItemResult - this is specified by the protobuf file data.js</li>
 * </ul>
 */
function AdvanceDataListener(connection, Request, defListener) {
    var requestMap = {};
    requestMap[Request.MessageType.DATA_REQUEST] = {};
    requestMap[Request.MessageType.DATA_INSERT] = {};
    requestMap[Request.MessageType.DATA_UPDATE] = {};

    var localScope = this;
    var defaultListener = defListener || false;
    var errorListener = false;
    this.setErrorListener = function(func) {
        errorListener = func;
    };
    /**
     * Sets the listener to listen for database code.
     */
    this.setListener = function(messageType, requestId, func) {
        var localMap = requestMap[messageType];
        //console.log('Adding listener');
        //console.log(messageType);
        //console.log(queryType);
        localMap[requestId] = func;
    };

    /**
     * Sets the listener to listen for database code.
     *
     * And it also unwraps the DataResult type.
     * @param {MessageType} messageType
     * @param {String} requestId
     * @param {Function} func
     */
    this.setDataResultListener = function(messageType, requestId, func) {
        this.setListener(messageType, requestId, queryWrap(func));
    };

    /**
     * Removes the function associated with the listener
     */
    this.removeListener = function(messageType, requestId) {
        var localMap = requestMap[messageType];
        //console.log('Adding listener');
        //console.log(messageType);
        //console.log(queryType);
        localMap[requestId] = undefined;
    };

    /**
     * Returns a function that is wrapped to process data results.
     * @param {Function} func The function that is wrapper to process data results.
     * @returns {Function}
     */
    function queryWrap(func) {
        return function(evt, msg) {
            var result = undefined;
            try {
                result = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(msg.otherData, CourseSketch.PROTOBUF_UTIL.getDataResultClass());
            } catch (exception) {
                // removes listener on error to prevent data leakage.
                removeListener(msg.requestType, msg.requestId);
                console.log(exception);
                // still call func though.
                func(evt, undefined);
                if (errorListener) {
                    errorListener(msg);
                }
            }
            var dataList = result.results;
            for (var i = 0; i < dataList.length; i++) {
                //console.log('Decoding listener');
                var item = dataList[i];
                if (!isUndefined(func)) {
                    try {
                        func(evt, item);
                    } catch (exception) {
                        removeListener(msg.requestType, msg.requestId);
                        console.error(exception);
                        console.error(exception.stack);
                        console.log(msg);
                    }
                } else {
                    defListener(evt, item);
                }
            }
        };
    }

    /**
     * Gets the message type and the query type and finds the correct listener.
     *
     * If the correct type does not exist then the defaultListener is called instead.
     * @param {Event} evt This is websocket event regarding the receive event of the message.
     * @param {Request} msg This is the request object that was received.
     * @param {MessageType} messageType if the message is a DataResult or a DataRequest or others.
     */
    function decode(evt, msg, messageType) {
        var localMap = requestMap[messageType];
        var func = localMap[msg.requestId];
        if (!isUndefined(func)) {
            try {
                func(evt, item);
            } catch (exception) {
                removeListener(msg.requestType, msg.requestId);
                console.error(exception);
                console.error(exception.stack);
                console.log(msg);
            }
        } else {
            defListener(evt, msg);
        }
    }
    var localFunction = decode;
    connection.setSchoolDataListener(function(evt, msg) {
        localFunction(evt, msg, msg.requestType);
    });

    /**
     *
     * @param request
     * @param callback
     */
    this.sendRequestWithTimeout = function(request, callback) {
        var callbackCalled = false;
        var wrappedCallback = function(evt, msg) {
            if (callbackCalled) {
                return;
            }
            callbackCalled = true;
            removeListener(msg.requestType, msg.requestId);
            callback(evt, msg);
        };

    };
}
