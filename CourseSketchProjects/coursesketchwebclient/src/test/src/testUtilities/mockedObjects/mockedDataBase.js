// assumes sinon is already included and mocked index and mocked connection and protobuf (protobuf probably won't be mocked though)

(function() {
    CourseSketch.makeNewDatabase = function(connection, dataListener) {
        return new SchoolDataManager(connection.userId, dataListener, connection,
            CourseSketch.PROTOBUF_UTIL.getRequestClass(), dcodeIO.ByteBuffer);
    };
    var db = CourseSketch.makeNewDatabase(CourseSketch.connection, CourseSketch.dataListener);
    var intervalVar = setInterval(function() {
        if (db.isDatabaseReady()) {
            clearInterval(intervalVar);
            CourseSketch.dataManager = sinon.stub(db);
            CourseSketch.dataManagerPure = db; // in case you need to replace the mock.
        }
    }, 100);
})();