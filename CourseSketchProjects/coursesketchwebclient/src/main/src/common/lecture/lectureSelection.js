CourseSketch.Lecture = {
	courseSelectionManager : undefined,
	currentCourse : undefined,

	/**
	 * Renders a list of lectures to the screen.
	 *
	 * @param lectureList list of lectures to display
	 */
	displayLectures : function(lectureList) {
		$("#placeholder").css({
			display: "none"
		});
		var add = $("#add").clone();
		var schoolItemBuilder = new SchoolItemBuilder();
		schoolItemBuilder.setList(lectureList).setShowDate(false).build(document.querySelector("#col2>.content"));
		$("#col2>.content").prepend(add);
		$("#add").bind("click", CourseSketch.Lecture.addLecture);
		$("#add").css({
			display: "inline-block"
		});
	},

	/**
	 * Called when a course is selected. Updates selection and gets lectures for the course.
	 *
	 * @param course course object of the selected element
	 */
	courseSelected : function(course) {
		var courseid = course.id;
		this.currentCourse = course.id;
		CourseSketch.dataManager.getCourseLectures(course.lectureList, CourseSketch.Lecture.displayLectures);
		CourseSketch.Lecture.courseSelectionManager.clearAllSelectedItems();
		CourseSketch.Lecture.courseSelectionManager.addSelectedItem(document.getElementById(courseid));
		CourseSketch.dataManager.getCourse(courseid, function(course) {
			CourseSketch.dataManager.getCourseLectures(course.lectureList, CourseSketch.Lecture.displayLectures);
		});
	},
	
	/**
	 * Adds a new lecture to the currently selected course.
	 *
	 * @param evt event from click (or other) action
	 */
	addLecture : function(evt) {
		$("#col2>.content").append("<span class=\"lecture\"><div class=\"title\">TITLE</div><div class=\"summary\">Untitled Lecture</div></span>");
	    var lecture=CourseSketch.PROTOBUF_UTIL.Lecture();
	    lecture.courseId = currentCourse;
	    lecture.title = "Untitled Lecture";
	    lecture.id = generateUUID();
	    lecture.description = "N/A";
	    CourseSketch.dataManager.insertLecture(lecture );
	    console.log("finished adding to course "+ currentCourse);
	    
	},

	/**
	 * Renders a list of courses to the screen.
	 *
	 * @param courseList list of courses to display
	 */
	showCourses : function(courseList) {
		var schoolItemBuilder = new SchoolItemBuilder();
		schoolItemBuilder.setList(courseList).setShowDate(false).setBoxClickFunction(this.courseSelected).build(document.querySelector("#col1>.content"));
	}
};

$(document).ready(function() {
	CourseSketch.Lecture.courseSelectionManager = new clickSelectionManager();
	var loadCourses = function(courseList) {
            /* (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting();
            }*/
            //localScope.showCourses(courseList);
	    CourseSketch.Lecture.showCourses(courseList);
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
});
