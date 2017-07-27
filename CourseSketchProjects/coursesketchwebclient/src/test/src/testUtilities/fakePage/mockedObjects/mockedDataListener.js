CourseSketch.Testing = CourseSketch.Testing || {};

// assumes fake connection
(function() {
    CourseSketch.makeNewDataListener = function() {
        return new AdvanceDataListener(CourseSketch.prutil.getRequestClass(), function(evt, item) {
            console.log("default listener",evt,item);
        });
    };
    var dataListener = CourseSketch.makeNewDataListener();
    CourseSketch.dataListener = dataListener;
    //CourseSketch.dataListenerPure = dataListener; // in case you need to replace the mock.

    var fakeInsertFunctions = {};
    var fakeDataRequestFunctions = {};
    var fakeRequestFunctions = {};

    CourseSketch.Testing.addFakeInsertFunction = function addFakeInsertFunction(queryType, func) {
        fakeInsertFunctions[queryType] = func;
    };

    CourseSketch.Testing.addFakeDataRequestFunction = function addFakeInsertFunction(queryType, func) {
        fakeDataRequestFunctions[queryType] = func;
    };

    CourseSketch.Testing.addFakeRequestFunction = function addFakeInsertFunction(requestType, func) {
        fakeRequestFunctions[requestType] = func;
    };

    CourseSketch.Testing.createFakeResult = function (id) {
        return [undefined, {
            getReturnText: function() { return id + ':' + id; }
        }];
    };

    CourseSketch.Testing.dataListenerConfig = {
        callRealDataUpdate: true,
        callRealDataInsert: true,
        callRealRequest: true
    };

    dataListener.realDataUpdate = dataListener.sendDataUpdate;
    dataListener.sendDataUpdate = function (queryType, data, callback, requestId, times) {
        if (CourseSketch.Testing.dataListenerConfig.callRealDataUpdate) {
            dataListener.realDataUpdate(queryType, data, callback, requestId, times);
            return;
        }
        try {
            fakeDataRequestFunctions[queryType](data, requestId, times);
        } catch (exception) {
            console.log(exception);
        }
        callback();
    };

    dataListener.realDataInsert = dataListener.sendDataInsert;
    dataListener.sendDataInsert = function (queryType, data, callback, requestId, times) {
        if (CourseSketch.Testing.dataListenerConfig.callRealDataInsert) {
            dataListener.realDataInsert(queryType, data, callback, requestId, times);
            return;
        }
        var result = fakeInsertFunctions[queryType](data, requestId, times);
        callback(result[0], result[1]);
    };

    dataListener.realRequestWithTimeout = dataListener.sendRequestWithTimeout;
    dataListener.sendRequestWithTimeout = function (request, callback) {
        if (CourseSketch.Testing.dataListenerConfig.callRealRequest) {
            dataListener.realRequestWithTimeout(request, callback);
            return;
        }
        callback(event, fakeRequestFunctions[request.requestType](request));
    };
})();
