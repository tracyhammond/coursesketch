/**
 * loads all of the test data.
 */
$(document).ready(function() {
    var barrier = new CallbackBarrier();
    console.log(barrier);
    var lectureBarrier = barrier.getCallback();
    var courseBarrier = barrier.getCallback();
    var assignmentBarrier = barrier.getCallback();
    var problemBarrier = barrier.getCallback();

    // barriers have been tested and work as expected with this function.
    var loadLectures = function() {
        var localBarrier = new CallbackBarrier();
        var lectureLoadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeLectures.length);
        localBarrier.finalize(lectureBarrier);
        for (var i = 0; i < CourseSketch.fakeLectures.length; ++i) {
            CourseSketch.dataManager.setLecture(CourseSketch.fakeLectures[i], lectureLoadedCallback, lectureLoadedCallback);
        }
    };

    var loadAssignments = function() {
        var localBarrier = new CallbackBarrier();
        var assignmentLoadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeAssignments.length);
        localBarrier.finalize(assignmentBarrier);
        for (var i = 0; i < CourseSketch.fakeAssignments.length; ++i) {
            CourseSketch.dataManager.setAssignmentLocal(CourseSketch.fakeAssignments[i], assignmentLoadedCallback);
        }
    };

    var loadCourses = function() {
        var localBarrier = new CallbackBarrier();
        var loadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeCourses.length);
        localBarrier.finalize(courseBarrier);
        for (var i = 0; i < CourseSketch.fakeCourses.length; ++i) {
            CourseSketch.dataManager.setCourse(CourseSketch.fakeCourses[i], loadedCallback);
        }
    };

    var loadProblems = function() {
        /*
        for (var i = 0; i < CourseSketch.fakeLectures.length; ++i) {
            CourseSketch.dataManager.setLectureLocal(CourseSketch.fakeLectures[i]);
        }
        */
        problemBarrier();
    };

    /**
     * Called when we can load our fake data into the database.
     */
    function databaseIsReadForLoading() {
        /**
         * Replaces the functionality of get all courses with this one.
         * This one does not talk to the server. Instead it instantiates the state of all of the courses.
         */
        CourseSketch.dataManager.getAllCourses = function(coursesCallback) {
            var resultList = [];
            var localBarrier = new CallbackBarrier();
            var loadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeCourses.length);
            localBarrier.finalize(function() {
                coursesCallback(resultList);
            });
            for (var i = 0; i < CourseSketch.fakeCourses.length; ++i) {
                CourseSketch.dataManager.getCourse(CourseSketch.fakeCourses[i].id, function(course) {
                    resultList.push(course);
                    loadedCallback();
                });
            }
        };

        CourseSketch.dataManager.getAllExperiments = function(problemId, callback) {
            var results = [];
            for(var i = 0; i<CourseSketch.fakeSketches.length; ++i){
                if(CourseSketch.fakExperiments[i].problemId == problemId){
                    results.push(CourseSketch.fakeExperiments[i]);
                }   
            }
            callback(results);
        }

        barrier.finalize(function() {
            console.log("DATABASE HAS ITS DATA LOADED");
            CourseSketch.dataManager.testDataLoaded = true;
        });
        loadCourses();
        loadLectures();
        loadAssignments();
        loadProblems();
    }

    // waits till the database is ready to set up our loading process
    if (CourseSketch.dataManager.realDatabaseReady()) {
        databaseIsReadForLoading();
    } else {
        var intervalVar = setInterval(function() {
            if (CourseSketch.dataManager.realDatabaseReady()) {
            clearInterval(intervalVar);
            databaseIsReadForLoading();
        }
    }, 100);
    }
});
