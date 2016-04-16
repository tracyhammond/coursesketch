
/**
 * An exception that is used to represent problems with the advance data listener.
 *
 * @class DatabaseException
 * @extends BaseException
 */
function AdvanceListenerException(message, cause) {
    this.name = 'AdvanceListenerException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

AdvanceListenerException.prototype = new BaseException();
CourseSketch.AdvanceListenerException = AdvanceListenerException;


/**
 * Allows for more separation for the Data Result.
 *
 * What a listener receives:
 * <ul>
 * <li>an event that is specified by websocket event protocol</li>
 * <li>an ItemResult - this is specified by the protobuf file data.js</li>
 * </ul>
 *
 * @param {Request} Request - The protobuf Request Message.
 * @param {Function} defListener - The default listener that is called if a function is not assigned to a server response.
 */
function AdvanceDataListener(Request, defListener) {
    var requestMap = {};
    requestMap[Request.MessageType.DATA_REQUEST] = {};
    requestMap[Request.MessageType.DATA_INSERT] = {};
    requestMap[Request.MessageType.DATA_UPDATE] = {};
    var TIMEOUT_CONST = 'TIMED_OUT';

    var localScope = this;
    var defaultListener = defListener || false;
    var errorListener = false;

    /**
     * Sets a listener that is called when an error occurs.
     *
     * @param {Function} func - A function that is called when an error occurs.
     */
    this.setErrorListener = function(func) {
        errorListener = func;
    };

    /**
     * Adds a requestType to the datalistener
     * @param {Request.MessageType} requestType
     */
    this.addRequestType = function(requestType) {
        if (isUndefined(requestMap[requestType])) {
            requestMap[requestType] = {};
        }
    };

    /**
     * Sets the listener to listen for server response.
     *
     * @param {String} messageType - The message type of the request.
     * @param {String} requestId - The unique identifier for the request.
     * @param {Function} func - The function that is called as a result of listening.
     * @param {Number} [times] - the number of times you want the function to be called before it is removed.
     */
    function setListener(messageType, requestId, func, times) {
        console.log('Creating a listener for requestID ', requestId);
        var localMap = requestMap[messageType];

        localMap[requestId] = {
            func: func,
            times: (isUndefined(times)? 1 : times)
        };
    }

    /**
     * Sets the listener to listen for database code.
     *
     * And it also unwraps the DataResult type.
     *
     * @param {MessageType} messageType - The message type of the request.
     * @param {String} requestId - The unique identifier for the request.
     * @param {Function} func - The function that is called as a result of listening.
     * @param {Number} [times] - the number of times you want the function to be called before it is removed.
     * @param {ProtoBufMessage} returnType - This is the type of message that other data is decoded into.
     */
    this.setDataResultListener = function(messageType, requestId, func, times, returnType) {
        setListener(messageType, requestId, queryWrap(func, returnType), times);
    };

    /**
     * Removes the function associated with the listener.
     *
     * @param {MessageType} messageType - The message type of the request.
     * @param {String} requestId - The unique identifier for the request.
     */
    function removeListener(messageType, requestId) {
        var localMap = requestMap[messageType];
        localMap[requestId] = undefined;
    }
    this.removeListener = removeListener;

    /**
     * Returns a function that is wrapped to process data results.
     *
     * @param {Function} func - The function that is wrapper to process data results.
     * @returns {Function} A wrapped function that processes data results.
     * @param {ProtoBufMessage} returnType - This is the type of message that other data is decoded into.
     */
    function queryWrap(func, returnType) {
        return function(evt, msg, listener) {
            function manageTimeCallback(callbackData) {
                if (listener.times >= 0) {
                    listener.times -= 1;
                    if (!isUndefined(func)) {
                        try {
                            func(evt, callbackData);
                            if (listener.times === 0) {
                                removeListener(msg.requestType, msg.requestId);
                                return false;
                            }
                        } catch (exception) {
                            if (listener.times === 0) {
                                removeListener(msg.requestType, msg.requestId);
                                return false;
                            }
                            CourseSketch.clientException(exception);
                        }
                    } else {
                        defListener(evt, item);
                    } // if isUndefined func
                } else {
                    removeListener(msg.requestType, msg.requestId);
                    return false;
                }
                return true;
            }

            var result = undefined;
            if (msg.otherData === TIMEOUT_CONST) {
                removeListener(msg.requestType, msg.requestId);
                func(evt, new AdvanceListenerException('Connection to the database Timed Out'));
                return;
            }
            if (!isUndefined(messageType)) {
                result = CourseSketch.prutil.decodeProtobuf(msg.otherData, returnType);
                manageTimeCallback(result);
                return;
            }
            try {
                result = CourseSketch.prutil.decodeProtobuf(msg.otherData, CourseSketch.prutil.getDataResultClass());
            } catch (exception) {
                // removes listener on error to prevent data leakage.
                removeListener(msg.requestType, msg.requestId);
                CourseSketch.clientException(exception);
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
                if (!manageTimeCallback(item)) {
                    break;
                }
            } // for
            if (listener.times <= 0) {
                removeListener(msg.requestType, msg.requestId);
            }
        };
    }

    /**
     * Gets the message type and the query type and finds the correct listener.
     *
     * If the correct type does not exist then the defaultListener is called instead.
     *
     * @param {Event} evt - This is websocket event regarding the receive event of the message.
     * @param {Request} msg - This is the request object that was received.
     */
    function decode(evt, msg) {
        var messageType = msg.requestType;
        var localMap = requestMap[messageType];
        var listener = localMap[msg.requestId];
        console.log('decoding message request for message with id: ' + msg.requestId, listener);
        if (!isUndefined(listener)) {
            var func = listener.func;
            try {
                func(evt, msg, listener);
            } catch (exception) {
                removeListener(msg.requestType, msg.requestId);
                CourseSketch.clientException(exception);
            }
        } else {
            console.log('Listener for request id: ', msg.requestId, 'not found', msg);
            defListener(evt, msg);
        }
    }

    /**
     * Assigns a local function to the global connection object.
     */
    this.setupConnectionListeners = function() {
        CourseSketch.connection.setSchoolDataListener(function(evt, msg) {
            decode(evt, msg);
        });
    };

    this.setupConnectionListeners();

    /**
     * @returns {Function} A function that can be used as a listener.
     */
    this.getListenerHook = function() {
        return function (evt, msg) {
            decode(evt, msg);
        };
    };

    /**
     * Sends a request that will timeout after the server.
     *
     * @param {Request} request - The protobuf request being sent to the server.
     * @param {Function} callback - The function that is called as a result of listening.
     * @param {Number} [times] - The number of times you want the function to be called before it is removed.
     * @param {ProtoBufMessage} returnType - This is the type of message that other data is decoded into
     */
    this.sendRequestWithTimeout = function(request, callback, times, returnType) {
        var callbackCalled = false;
        var callbackTimedOut = false;
        var timeoutVariable = undefined;
        /**
         * A wrapped callback that handles the timeout.
         *
         * If the regular function is called the timeout is cleared.
         * If the timeout version is called then it will not call the regular version.
         * This is called for every item result.
         *
         * @param {Event} evt - WebsocketEvent
         * @param {Request|BaseException} msg - The protobuf request object sent from the server or an exception that occured along the way.
         * @param {Function} listener - The user function that is called as a result of listening.
         */
        var wrappedCallback = function(evt, msg, listener) {
            if ((isUndefined(msg) || msg.otherData === TIMEOUT_CONST || msg instanceof BaseException) && !callbackCalled) {
                callbackTimedOut = true;
                callback(evt, msg, listener);
                return;
            } else if (!callbackTimedOut) {
                callbackCalled = true;
                clearTimeout(timeoutVariable);
                callback(evt, msg, listener);
                return;
            }
            // it appears the server did eventually respond
            throw new AdvanceListenerException('The server responded but took too long to respond.');
        };
        // set listener
        this.setDataResultListener(request.requestType, request.requestId, wrappedCallback, times, returnType);

        if (!CourseSketch.connection.isConnected()) {
            console.log('The server is not connected all messages will be queued till reconnection is made.');
            CourseSketch.pushServerMessage(request, callback, times);
            CourseSketch.createReconnection();
            return;
        }
        // send request
        CourseSketch.connection.sendRequest(request);

        // set timeout
        timeoutVariable = setTimeout(function() {
            var clonedRequest = CourseSketch.prutil.cleanProtobuf(request, CourseSketch.prutil.getRequestClass());
            clonedRequest.otherData = TIMEOUT_CONST;
            decode(undefined, clonedRequest);
        }, 50000);
    };


    /**
     * Sends a request to retrieve data from the server.
     *
     * This sends a data request.
     * This will automatically time out after 5 seconds.
     *
     * @param {ItemRequest | List<ItemRequest>} itemRequest - This can be either a single item request or a list of itme requests.
     * @param {Function} callback - The function that is called as a result of listening.
     * @param {String} [requestId] - The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] - The number of times you want the function to be called before it is removed;
     */
    this.sendDataRequest = function sendDataRequest(itemRequest, callback, requestId, times) {
        if (isUndefined(callback)) {
            throw new AdvanceListenerException('Can not request data without a callback');
        }
        var dataRequest = CourseSketch.prutil.DataRequest();

        dataRequest.items = [].concat(itemRequest);

        var request = CourseSketch.prutil.createRequestFromData(dataRequest, Request.MessageType.DATA_REQUEST, requestId);

        this.sendRequestWithTimeout(request, callback, times);
    };

    /**
     * Inserts data into the server database.
     *
     * Only inserts a single one right now.
     *
     * @param {ItemQuery} queryType - The type of query it is.
     * @param {ByteArray} data - The protobuf bytes that are being sent to the server.
     * @param {Function} callback - The function that is called as a result of listening.
     * @param {String} [requestId] - The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] - The number of times you want the function to be called before it is removed;
     */
    this.sendDataInsert = function sendDataInsert(queryType, data, callback, requestId, times) {
        var dataSend = CourseSketch.prutil.DataSend();
        dataSend.items = [];
        var itemSend = CourseSketch.prutil.ItemSend();
        itemSend.setQuery(queryType);
        itemSend.setData(data);
        dataSend.items.push(itemSend);

        var request = CourseSketch.prutil.createRequestFromData(dataSend, Request.MessageType.DATA_INSERT, requestId);
        this.sendRequestWithTimeout(request, callback, times);
    };

    /**
     * Sends an update to the server for the data to be updated.
     *
     * @param {ItemQuery} queryType - The type of query it is.
     * @param {ByteArray} data - The protobuf bytes that are being sent to the server.
     * @param {Function} callback - The function that is called as a result of listening.
     * @param {String} [requestId] - The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] - The number of times you want the function to be called before it is removed;
     */
    this.sendDataUpdate = function sendDataUpdate(queryType, data, callback, requestId, times) {
        var dataSend = CourseSketch.prutil.DataSend();
        dataSend.items = [];
        var itemUpdate = CourseSketch.prutil.ItemSend();
        itemUpdate.setQuery(queryType);
        itemUpdate.setData(data);
        dataSend.items.push(itemUpdate);

        var request = CourseSketch.prutil.createRequestFromData(dataSend, Request.MessageType.DATA_UPDATE, requestId);
        this.sendRequestWithTimeout(request, callback, times);
    };

}
