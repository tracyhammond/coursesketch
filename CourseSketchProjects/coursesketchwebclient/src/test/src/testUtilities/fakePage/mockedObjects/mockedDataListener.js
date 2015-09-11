// assumes fake connection
(function() {
    CourseSketch.makeNewDataListener = function(connection) {
        return new AdvanceDataListener(connection, CourseSketch.prutil.getRequestClass(), function(evt, item) {
            console.log("default listener");
        });
    };
    var dataListener = CourseSketch.makeNewDataListener(CourseSketch.connection);
    CourseSketch.dataListener = dataListener;
    //CourseSketch.dataListenerPure = dataListener; // in case you need to replace the mock.
})();
