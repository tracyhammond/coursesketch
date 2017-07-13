// assumes fake connection
(function() {
    CourseSketch.makeNewDataListener = function() {
        return new AdvanceDataListener(CourseSketch.prutil.getRequestClass(), function(evt, item) {
            console.log("default listener",evt,item);
        });
    };
    var dataListener = CourseSketch.makeNewDataListener();
    CourseSketch.dataListener = dataListener;
    //CourseSketch.dataListenerPure = dataListener; // in case you need to replace the mock.
})();
