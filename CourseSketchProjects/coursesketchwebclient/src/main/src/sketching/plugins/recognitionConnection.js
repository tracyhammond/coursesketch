/**
 * A plugin used to send updates to the server.
 * @param {Connection} serverConnection a connection to the server.
 */
function recognitionConnection(serverConnection) {
    /**
     * Holds the list of updates that are waiting to be sent to the server.
     *
     * This list should almost always be near empty.
     */
    var queuedServerUpdates = [];

    /**
     * Called when the updatemanager adds an update.
     * @param {SrlUpdate} update the update to be sent to thee recognition server.
     * @param {Boolean} toRemote true if this update is destined to the remote server.
     */
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
                var request = CourseSketch.PROTOBUF_UTIL.createRequestFromUpdate(update,
                        CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.RECOGNITION);
                serverConnection.sendRequest(request);
                if (queuedServerUpdates.length > 0) {
                    this.emptyQueue(); // recursion! (kind of)
                }
            }
        }.bind(this), 10);
    };
}

// TODO: test and fill this out.
