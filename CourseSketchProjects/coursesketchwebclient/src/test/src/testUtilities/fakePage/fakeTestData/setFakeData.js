/**
 * loads all of the test data.
 */
(function() {
    var barrier = new CallbackBarrier();
    console.log(barrier);
    var lectureBarrier = barrier.getCallback();
    var slideBarrier = barrier.getCallback();
    var courseBarrier = barrier.getCallback();
    var assignmentBarrier = barrier.getCallback();
    var problemBarrier = barrier.getCallback();

    // barriers have been tested and work as expected with these function.
    var loadLectures = function() {
        var localBarrier = new CallbackBarrier();
        var lectureLoadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeLectures.length);
        localBarrier.finalize(lectureBarrier);
        for (var i = 0; i < CourseSketch.fakeLectures.length; ++i) {
            console.log(CourseSketch.fakeLectures[i]);
            CourseSketch.dataManager.setAssignment(CourseSketch.fakeLectures[i], lectureLoadedCallback, lectureLoadedCallback);
        }
    };

    var loadSlides = function() {
        var localBarrier = new CallbackBarrier();
        var slidesLoadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeSlides.length);
        localBarrier.finalize(slideBarrier);
        for (var i = 0; i < CourseSketch.fakeSlides.length; ++i) {
            CourseSketch.dataManager.setSlide(CourseSketch.fakeSlides[i], slidesLoadedCallback, slidesLoadedCallback);
        }
    };

    var loadAssignments = function() {
        var localBarrier = new CallbackBarrier();
        var assignmentLoadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeAssignments.length);
        localBarrier.finalize(assignmentBarrier);
        for (var i = 0; i < CourseSketch.fakeAssignments.length; ++i) {
            CourseSketch.dataManager.setAssignment(CourseSketch.fakeAssignments[i], assignmentLoadedCallback);
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
        var localBarrier = new CallbackBarrier();
        var loadedCallback = localBarrier.getCallbackAmount(CourseSketch.fakeProblems.length);
        localBarrier.finalize(problemBarrier);
        for (var i = 0; i < CourseSketch.fakeProblems.length; ++i) {
            CourseSketch.dataManager.setCourseProblem(CourseSketch.fakeProblems[i], loadedCallback);
        }
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
            for (var i = 0; i<CourseSketch.fakeSketches.length; ++i){
                if (CourseSketch.fakeExperiments[i].problemId == problemId){
                    results.push(CourseSketch.fakeExperiments[i]);
                }
            }
            callback(results);
        };

        barrier.finalize(function() {
            console.log("DATABASE HAS ITS DATA LOADED");
            CourseSketch.dataManager.testDataLoaded = true;
        });
        loadCourses();
        loadSlides();
        loadProblems();
        loadAssignments();
        loadLectures();
    }

    // waits till the database is ready to set up our loading process
    if (!isUndefined(CourseSketch.dataManager.realDatabaseReady) && CourseSketch.dataManager.realDatabaseReady()) {
        databaseIsReadForLoading();
    } else {
        var intervalVar = setInterval(function() {
            if (!isUndefined(CourseSketch.dataManager.realDatabaseReady) && CourseSketch.dataManager.realDatabaseReady()) {
                clearInterval(intervalVar);
                databaseIsReadForLoading();
            }
        }, 50);
    }

    /**
     * Returns a request given an input request from the server for some certain data.
     */
    CourseSketch.serverResponseForBankProblems = function(req) {
        var dataRequest = CourseSketch.prutil.getDataRequestClass().decode(req.otherData);
        var itemRequest = dataRequest.items[0];
        var totalLength = CourseSketch.fakeBankProblems.length;
        var school = CourseSketch.prutil.SrlSchool();
        school.bankProblems = [];

        // maybe use slice in the future! but not today.
        for (var i = itemRequest.page * 10; (i < totalLength && i < (itemRequest.page + 1) * 10); i++) {
            school.bankProblems.push(CourseSketch.fakeBankProblems[i]);
        }

        var result = CourseSketch.prutil.ItemResult();
        if (school.bankProblems.length > 0) {
            result.data = school.toArrayBuffer();
        } else {
            result.noData = true;
        }
        result.query = itemRequest.query;

        var dataResults = CourseSketch.prutil.DataResult();
        dataResults.results = [result];
        var resultingRequest = CourseSketch.prutil.createRequestFromData(dataResults,
                req.requestType);
        return resultingRequest;
    };
})();
