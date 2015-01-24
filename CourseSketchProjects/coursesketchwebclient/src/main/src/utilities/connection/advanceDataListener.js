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

	var myScope = this;
	var defaultListener = defListener || false;
	var errorListener = false;

	this.setErrorListener = function(func) {
		errorListener = func;
	};

	/**
	 * Sets the listener to listen for database code.
	 */
	this.setListener = function(messageType, queryType, func) {
		var localMap = requestMap[messageType];
		//console.log("Adding listener");
		//console.log(messageType);
		//console.log(queryType);
		localMap[queryType] = func;
	};

	/**
	 * Removes the function associated with the listener
	 */
	this.removeListener = function(messageType, queryType) {
		var localMap = requestMap[messageType];
		//console.log("Adding listener");
		//console.log(messageType);
		//console.log(queryType);
		localMap[queryType] = undefined;
	};

	/**
	 * Gets the message type and the query type and finds the correct listener.
	 *
	 * if the correct type does not exist then the defaultListener is called instead.
	 */
	function decode(evt, msg, messageType) {
		var localMap = requestMap[messageType];
		try {
			try {
				msg.otherData.mark();
			} catch(exception) {
				console.log(exception);
				console.log(msg);
			}
			var dataList = CourseSketch.PROTOBUF_UTIL.getDataResultClass().decode(msg.otherData).results;
			for (var i = 0; i < dataList.length; i++) {
				//console.log("Decoding listener");
				var item = dataList[i];
				var func = localMap[item.query];
				if (!isUndefined(func)) {
					try {
						func(evt, item);
					} catch(exception) {
						console.error(exception);
						console.error(exception.stack);
						console.log(msg);
					}
				} else {
					defListener(evt, item);
				}
			}
		}catch(exception) {
			console.error(exception);
			console.error(exception.stack);
			console.log("decoding data failed: ", msg);
			if (errorListener) {
				errorListener(msg);
			}
		}
	}
	var localFunction = decode;
	connection.setSchoolDataListener(function(evt, msg) {
		localFunction(evt, msg, msg.requestType);
	});
}
