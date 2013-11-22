(function(localScope) {
var localLock; //do something with this? not sure what.
/**
 * Submits the updateList to the given connection.
 *
 * calls an updateMethod that should be very simple and quick and should take less than 5 milliseconds.  (like showing a percentage bar)
 * @param RequestType the type that it is being submitted to (Recognition, Submission, Saving)
 * @param startMethod called when the OPEN_SYNC command is sent.
 * @param updateMethod called after every command is sent (not including openSync and closeSync) updateMethod is called.
 * @param finishMethod called when the CLOSE_SYNC command is sent.
 */
function submitUpdateList(connection, updateList, CommandType, RequestType, startMethod, updateMethod, finishMethod) {
	if (updateList.length == 0) {
		startMethod();
		updateMethod(0,0);
		finishMethod();
	}

	var submitIndex = 0;
	var interval = 0;
	function submitOnce(update, index) {
		update.commandNumber = index;
		request = connection.createRequestFromUpdate(update, RequestType);
		connection.sendRequest(request);
	}

	function openSync() {
		var command = connection.createBaseCommand(CommandType.OPEN_SYNC, false);
		var array = new Array();
		array.push(command);
		var update = connection.createUpdateFromCommands(array);
		update.commandNumber = -1;
		request = connection.createRequestFromUpdate(update, RequestType);
		connection.sendRequest(request);
		if (startMethod) {
			startMethod();
		}
	}

	function closeSync() {
		var command = connection.createBaseCommand(CommandType.CLOSE_SYNC, false);
		var array = new Array();
		array.push(command);
		var update = connection.createUpdateFromCommands(array);
		update.commandNumber = updateList.length+1;
		request = connection.createRequestFromUpdate(update, RequestType);
		connection.sendRequest(request);
		if (finishMethod) {
			finishMethod();
		}
	}

	function submitList() {
		interval = setInterval(function() {
				try {
					submitOnce(updateList[submitIndex],submitIndex);
					submitIndex++;
					if (updateMethod) {
						updateMethod(submitIndex, updateList.length);
					}
					if (submitIndex == updateList.length) {
						clearInterval(interval);
						closeSync();
					}
				} catch(Exception) {
					clearInterval(interval);
					throw Exception;
				}
			},20);
	}
	
	openSync();
	// wait for server to send back ready!
	setTimeout(submitList(),200); // so that the server has time to capture them all correctly!
}
localScope.submitUpdateList = submitUpdateList;
})(this);
