var courseSelectionManager;

var displayLectures = function(lectureList) {
	console.log(lectureList);
	$("#placeholder").css({
		display: "none"
	});
	var add = $("#add").clone();
	var schoolItemBuilder = new SchoolItemBuilder();
	schoolItemBuilder.setList(lectureList).setShowDate(false).build(document.querySelector("#col2>.content"));
	$("#col2>.content").prepend(add);
	$("#add").bind("click", addLecture);
	$("#add").css({
		display: "inline-block"
	});
}

var courseSelected = function(course) {
	var courseid = course.id;
	CourseSketch.dataManager.getCourseLectures(course.lectureList, displayLectures);
	courseSelectionManager.clearAllSelectedItems();
        courseSelectionManager.addSelectedItem(document.getElementById(courseid));
	CourseSketch.dataManager.getCourse(courseid, function(course) {
		CourseSketch.dataManager.getCourseLectures(course.lectureList, displayLectures);
	});
}

var addLecture = function(evt) {
	$("#col2>.content").append("<span class=\"lecture\"><div class=\"title\">TITLE</div><div class=\"summary\">HELLO WORLD</div></span>");
}

var showCourses = function(courseList) {
	var schoolItemBuilder = new SchoolItemBuilder();
	schoolItemBuilder.setList(courseList).setShowDate(false).setBoxClickFunction(courseSelected).build(document.querySelector("#col1>.content"));
}

$(document).ready(function() {
	courseSelectionManager = new clickSelectionManager();
	var loadCourses = function(courseList) {
            /* (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting();
            }*/
            //localScope.showCourses(courseList);
	    showCourses(courseList);
        };
        if (CourseSketch.dataManager.isDatabaseReady()) {
            CourseSketch.dataManager.pollUpdates(function() {
                CourseSketch.dataManager.getAllCourses(loadCourses);
            });
        } else {
            var intervalVar = setInterval(function() {
                if (CourseSketch.dataManager.isDatabaseReady()) {
                    clearInterval(intervalVar);
                    CourseSketch.dataManager.pollUpdates(function() {
                        CourseSketch.dataManager.getAllCourses(loadCourses);
                    });
                }
            }, 100);
        }
})
