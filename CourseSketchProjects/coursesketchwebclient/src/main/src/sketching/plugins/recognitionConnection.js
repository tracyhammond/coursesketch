function recognitionConnection(serverConnection) {
    /**
     * Holds the list of updates that are waiting to be sent to the server.
     * 
     * This list should almost always be near empty.
     */
    var queuedServerUpdates = new Array();

    this.addUpdate = function(update, toRemote) {
        if (!isUndefined(toRemote) && toRemote) {
            // we send to the remote server
            queuedServerUpdates.push(update);
            this.emptyQueue();
        }
    };

    /**
     * Slowly empties the queue for sending messages to the server.
     */
    this.emptyQueue = function() {
        setTimeout(function() {
            if (queuedServerUpdates.length > 0) {
                var update = queuedServerUpdates.removeObjectByIndex(0);
                var request = PROTOBUF_UTIL.createRequestFromUpdate(update, PROTOBUF_UTIL.getRequestClass().MessageType.RECOGNITION);
                serverConnection.sendRequest(request);
                if (queuedServerUpdates.length > 0) {
                    this.emptyQueue(); // recursion! (kind of)
                }
            }
        }.bind(this),10);
    };
}

// TODO: test and fill this out.