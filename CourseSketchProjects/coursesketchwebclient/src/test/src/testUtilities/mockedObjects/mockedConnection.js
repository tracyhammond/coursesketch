(function() {
    var connection = new Connection("http://localHost:8080", false, false);
    connection.userId = "fakseUser1";
    CourseSketch.connection = sinon.stub(connection);
    CourseSketch.connectionPure = connection; // in case you need to replace the mock.

    CourseSketch.getCurrentTime = function() {
        var longVersion = dcodeIO.Long.fromString("" + (createTimeStamp()));
        return longVersion;
    };
})();