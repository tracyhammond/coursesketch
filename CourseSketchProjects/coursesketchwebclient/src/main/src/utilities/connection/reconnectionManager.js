// This only contains functions that act upon the CourseSketch object.
// Used when reconnecting or refreshing the page.
$(document).ready(function() {

    var queuedMessages = [];

    /**
     * Pusheds a server message that does not complete until the user is logged in again.
     *
     * @param {Request} request - The request being sent to the server.
     * @param {Function} callback - What is called when the server responds.
     * @param {Number | undefined} times - The number of time the request is expected.
     */
    CourseSketch.pushServerMessage = function(request, callback, times) {
        queuedMessages.push({
            'request':  request,
            'callback': callback,
            'times':    times
        });
    };

    /**
     * Called when the server is successfully reconnected.
     *
     * It Attempts to send all queuedMessages.
     */
    CourseSketch.onSuccessfulReconnection = function() {
        console.log('User has logged in querying any existing messages sent to the server.');
        while (queuedMessages.length > 0) {
            var queuedObject = removeObjectByIndex(queuedMessages, 0);
            CourseSketch.dataListener.sendRequestWithTimeout(queuedObject.request, queuedObject.callback, queuedObject.times);
        }
    };
});
