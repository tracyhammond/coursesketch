(function(localScope) {
var localLock; //do something with this? not sure what.
/**
 * Submits the updateList to the given connection.
 *
 * calls an updateMethod that should be very simple and quick and should take less than 5 milliseconds.  (like showing a percentage bar)
 * @param builders contains the protobuf parts needed to submit a problem.  The first part is CommandBuilder then the SubmissionBuilder.
 * 
 * @param firstPiece is the first part of a submission. It contains relevant details like if it is a student or instructor.
 * It will also contain all the data needed to store for later retrieval.<br>
 * Finally firstPiece is either an instanceof SrlExperiment or SrlSolution.
 *
 * @param RequestType the type that it is being submitted to (Recognition, Submission, Saving)
 * @param startMethod called when the OPEN_SYNC command is sent.
 * @param updateMethod called after every command is sent (not including openSync and closeSync) updateMethod is called.
 * @param finishMethod called when the CLOSE_SYNC command is sent.
 */
function submitUpdateList(connection, updateList, builders, firstPiece, RequestType, startMethod, updateMethod, finishMethod) {
	var CommandBuilder = builders[0];
	var SubmissionBuilder = builders[1];
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
		if (firstPiece instanceof SubmissionBuilder.SrlExperiment) {
			request.responseText = "student";
		}
		connection.sendRequest(request);
	}

	function openSync() {

		request = connection.createRequestFromData(firstPiece, RequestType);
		if (firstPiece instanceof SubmissionBuilder.SrlExperiment) {
			request.responseText = "student";
		}

		connection.sendRequest(request);

		setTimeout(function() {
			var command = connection.createBaseCommand(CommandBuilder.CommandType.OPEN_SYNC, false);
			var array = new Array();
			array.push(command);
			var update = connection.createUpdateFromCommands(array);
			submitOnce(update, -1);
			setTimeout(submitList, 100);
		}, 200);
		
		if (startMethod) {
			startMethod();
		}
		// wait for server to send back ready!
		// // so that the server has time to capture them all correctly!
	}

	function closeSync() {
		var command = connection.createBaseCommand(CommandBuilder.CommandType.CLOSE_SYNC, false);
		var array = new Array();
		array.push(command);
		var update = connection.createUpdateFromCommands(array);
		update.commandNumber = updateList.length+1;
		request = connection.createRequestFromUpdate(update, RequestType);
		if (firstPiece instanceof SubmissionBuilder.SrlExperiment) {
			request.responseText = "student";
		}
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
						setTimeout(closeSync, 100); // to make sure all the other updates made it in first
					}
				} catch(Exception) {
					clearInterval(interval);
					throw Exception;
				}
			},20);
	}

	openSync();
}
localScope.submitUpdateList = submitUpdateList;
})(this);
