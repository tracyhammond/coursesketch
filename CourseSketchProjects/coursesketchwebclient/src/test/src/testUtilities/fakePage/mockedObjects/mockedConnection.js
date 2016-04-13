(function() {
    var connection = new Connection("localhost:8888", false, false);
    connection.userId = "fakseUser1";
    connection.setOnCloseListener(function(){});
    connection.reconnect();
    CourseSketch.connection = connection;
    CourseSketch.connection.isConnected = function () {
        return true;
    };
    CourseSketch.connectionPure = connection; // in case you need to replace the mock.

    CourseSketch.getCurrentTime = function() {
        var longVersion = dcodeIO.Long.fromString("" + (createTimeStamp()));
        return longVersion;
    };
})();
