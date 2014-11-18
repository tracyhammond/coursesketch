// assumes sinon is already included and mocked index and mocked connection and protobuf (protobuf probably won't be mocked though)

(function() {
    CourseSketch.makeNewDatabase = function(connection, dataListener) {
        return new SchoolDataManager(connection.userId, dataListener, connection,
            CourseSketch.PROTOBUF_UTIL.getRequestClass(), dcodeIO.ByteBuffer);
    };
    var db = CourseSketch.makeNewDatabase(CourseSketch.connection, CourseSketch.dataListener);
    CourseSketch.dataManager = db;
    CourseSketch.dataManager.testDataLoaded = false;
    CourseSketch.dataManager.realDatabaseReady = CourseSketch.dataManager.isDatabaseReady;
    CourseSketch.dataManager.isDatabaseReady = function() {
        return CourseSketch.dataManager.realDatabaseReady() && CourseSketch.dataManager.testDataLoaded;
    };
})();
