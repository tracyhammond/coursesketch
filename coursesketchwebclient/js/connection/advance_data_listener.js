/**
 * Allows for more separation for the Data Result.
 *
 * What a listener receives:
 * <ul>
 * <li>an event that is specified by websocket event protocol</li>
 * <li>an ItemResult - this is specified by the protobuf file data.js</li>
 * </ul>
 */
function AdvanceDataListener(connection, Request, query, defListener) {
	var requestMap = {};
	requestMap[Request.MessageType.DATA_REQUEST] = {};
	requestMap[Request.MessageType.DATA_SENDING] = {};
	requestMap[Request.MessageType.DATA_UPDATE] = {};

	var myScope = this;
	var defaultListener = defListener;
	var QueryBuilder = query;
	
	/**
	 * Sets
	 */
	this.setListener = function(messageType, queryType, func) {
		var localMap = requestMap[messageType];
		console.log("Adding listener");
		console.log(messageType);
		console.log(queryType);
		localMap[queryType] = func;
	}

	/**
	 * Gets the message type and the query type and finds the correct listener.
	 *
	 * if the correct type does not exist then the defaultListener is called instead.
	 */
	function decode(evt, msg, messageType) {
		var localMap = requestMap[messageType];
		var dataList = QueryBuilder.DataResult.decode(msg.otherData).results;
		for(var i = 0; i < dataList.length; i++) {
			console.log("Decoding listener");
			var item = dataList[i];
			var func = localMap[item.query];
			console.log(messageType);
			console.log(item.query);
			console.log(func);
			if (!isUndefined(func)) {
				func(evt, item);
			} else {
				defListener(evt, item);
			}
		}
	}
	var localFunction = decode;
	connection.setSchoolDataListener(function(evt, msg) {
		localFunction(evt, msg, msg.requestType);
	});
}