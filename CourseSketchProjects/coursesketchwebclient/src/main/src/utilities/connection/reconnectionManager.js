// This only contains functions that act upon the course sketch object.
// used when reconnecting or refreshing the page.
$(document).ready(function() {

    var queuedMessages = [];

    /**
     * Pusheds a server message that does not complete until the user is logged in again.
     * @param request
     * @param callback
     * @param times
     */
    CourseSketch.pushServerMessage = function (request, callback, times) {
        queuedMessages.push({
            "request" : request,
            "callback": callback,
            "times": times
        });
    };

    CourseSketch.onSuccessfulReconnection = function() {
        console.log("User has logged querying any existing messages sent to the server.");
        while (queuedMessages.length > 0) {
            var queuedObject = removeObjectByIndex(queuedMessages, 0);
            CourseSketch.dataListener.sendRequestWithTimeout(queuedObject.request, queuedObject.callback, queuedObject.times);
        }
    };
});
