
if (typeof window.CourseSketch == "undefined" || typeof CourseSketch == "undefined") {
    var CourseSketch = {}; // notice that this is not read only!
}
if (typeof parent == "undefined" || parent == null) {
    parent = {};
}

parent.CourseSketch = CourseSketch;

CourseSketch.connection = false;
CourseSketch.redirector = {};

CourseSketch.reloadContent = function() {
    throw "This function: reloadContent must be mocked to be called";
};

CourseSketch.redirectContent = function() {
    throw "This function: redirectContent must be mocked to be called";
};

CourseSketch.connection = {};

CourseSketch.dataListener = {};
CourseSketch.dataManager = {};
