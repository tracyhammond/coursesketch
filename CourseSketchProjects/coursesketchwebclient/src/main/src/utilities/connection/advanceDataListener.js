
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
 */
function AdvanceDataListener(connection, Request, defListener) {
    var requestMap = {};
    requestMap[Request.MessageType.DATA_REQUEST] = {};
    requestMap[Request.MessageType.DATA_INSERT] = {};
    requestMap[Request.MessageType.DATA_UPDATE] = {};
    var TIMEOUT_CONST = "TIMED_OUT";

    var localScope = this;
    var defaultListener = defListener || false;
    var errorListener = false;

    /**
     * Sets a listener that is called when an error occurs.
     *
     * @param {Function} func A function that is called when an error occurs.
     */
    this.setErrorListener = function(func) {
        errorListener = func;
    };

    /**
     * Sets the listener to listen for database code.
     * @param {Number} [times] the number of times you want the function to be called before it is removed;
     */
    function setListener(messageType, requestId, func, times) {
        var localMap = requestMap[messageType];

        localMap[requestId] = {
            func: func,
            times: (isUndefined(times)? 1 : times)
        };
    };

    /**
     * Sets the listener to listen for database code.
     *
     * And it also unwraps the DataResult type.
     * @param {MessageType} messageType
     * @param {String} requestId
     * @param {Function} func The function that is called as a result of listening
     * @param {Number} [times] the number of times you want the function to be called before it is removed;
     */
    this.setDataResultListener = function(messageType, requestId, func, times) {
        setListener(messageType, requestId, queryWrap(func), times);
    };

    /**
     * Removes the function associated with the listener
     */
    function removeListener(messageType, requestId) {
        var localMap = requestMap[messageType];
        localMap[requestId] = undefined;
    }
    this.removeListener = removeListener;

    /**
     * Returns a function that is wrapped to process data results.
     * @param {Function} func The function that is wrapper to process data results.
     * @returns {Function}
     */
    function queryWrap(func) {
        return function(evt, msg, listener) {
            var result = undefined;
            if (msg.otherData === TIMEOUT_CONST) {
                removeListener(msg.requestType, msg.requestId);
                func(evt, new AdvanceListenerException('Connection to the database Timed Out'));
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
                if (listener.times >= 0) {
                    listener.times -= 1;
                    if (!isUndefined(func)) {
                        try {
                            func(evt, item);
                        } catch (exception) {
                            removeListener(msg.requestType, msg.requestId);
                            CourseSketch.clientException(exception);
                        }
                    } else {
                        defListener(evt, item);
                    } // if isUndefined func
                } else {
                    removeListener(msg.requestType, msg.requestId);
                    return; // no more callbacks
                } // if times >= 0
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
     * @param {Event} evt This is websocket event regarding the receive event of the message.
     * @param {Request} msg This is the request object that was received.
     */
    function decode(evt, msg) {
        var messageType = msg.requestType;
        var localMap = requestMap[messageType];
        var listener = localMap[msg.requestId];
        if (!isUndefined(listener)) {
            var func = listener.func;
            try {
                func(evt, msg, listener);
            } catch (exception) {
                removeListener(msg.requestType, msg.requestId);
                CourseSketch.clientException(exception);
            }
        } else {
            defListener(evt, msg);
        }
    }

    connection.setSchoolDataListener(function(evt, msg) {
        decode(evt, msg);
    });

    /**
     *
     * @param request
     * @param callback
     */
    this.sendRequestWithTimeout = function(request, callback, times) {
        var callbackCalled = false;
        var callbackTimedOut = false;
        var timeoutVariable = undefined;
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
            throw new AdvanceListenerException('We got into an odd state');
        };
        // set listener
        this.setDataResultListener(request.requestType, request.requestId, wrappedCallback, times);

        // send request
        connection.sendRequest(request);

        // set timeout
        timeoutVariable = setTimeout(function() {
            var clonedRequest = CourseSketch.prutil.cleanProtobuf(request, CourseSketch.prutil.getRequestClass());
            clonedRequest.otherData = TIMEOUT_CONST;
            decode(undefined, clonedRequest);
        }, 5000);
    };


    /**
     * Sends a request to retrieve data from the server.
     *
     * This sends a data request
     * (this will automatically time out after 5 seconds)
     * @param {ItemRequest | List<ItemRequest>} itemRequest This can be either a single item request or a list of itme requests.
     * @param {Function} callback The function that is called as a result of listening.
     * @param {String} [requestId] The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] The number of times you want the function to be called before it is removed;
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
     * (only inserts a single one right now)
     * @param {Function} callback The function that is called as a result of listening.
     * @param {String} [requestId] The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] The number of times you want the function to be called before it is removed;
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
     * @param {QueryType} queryType
     * @oaram {ByteArray} data
     * @param {Function} callback The function that is called as a result of listening.
     * @param {String} [requestId] The id that is unique to this request to identify what callback is from what request.
     * @param {Number} [times] The number of times you want the function to be called before it is removed;
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
