(function() {
	$(document).ready(function() {
		CourseSketch.lectureSelection.courseSelectionManager = undefined,
		CourseSketch.lectureSelection.currentCourse = undefined,

		/**
		 * Renders a list of lectures to the screen.
		 *
		 * @param lectureList list of lectures to display
		 */
		CourseSketch.lectureSelection.displayLectures = function(lectureList) {
			var add = $("#add").clone();
			var schoolItemBuilder = new SchoolItemBuilder();
			schoolItemBuilder.setList(lectureList).setShowDate(false).build(document.querySelector("#col2>.content"));
			$("#col2>.content").prepend(add);
			$("#add").bind("click", CourseSketch.lectureSelection.addLecture);
			$("#add").addClass("show");
		};

		/**
		 * Called when a course is selected. Updates selection and gets lectures for the course.
		 *
		 * @param course course object of the selected element
		 */
		CourseSketch.lectureSelection.courseSelected = function(course) {
			var courseid = course.id;
			this.currentCourse = course.id;
			CourseSketch.dataManager.getCourseLectures(course.lectureList, CourseSketch.lectureSelection.displayLectures);
			CourseSketch.lectureSelection.courseSelectionManager.clearAllSelectedItems();
			CourseSketch.lectureSelection.courseSelectionManager.addSelectedItem(document.getElementById(courseid));
		};

		/**
		 * Adds a new lecture to the currently selected course.
		 *
		 * @param evt event from click (or other) action
		 */
		CourseSketch.lectureSelection.addLecture = function(evt) {
			$("#col2>.content").append("<span class=\"lecture\"><div class=\"title\">TITLE</div><div class=\"summary\">Untitled Lecture</div></span>");
		    var lecture=CourseSketch.PROTOBUF_UTIL.Lecture();
		    lecture.courseId = currentCourse;
		    lecture.title = "Untitled Lecture";
		    lecture.id = generateUUID();
		    lecture.description = "N/A";
		    CourseSketch.dataManager.insertLecture(lecture );
		    console.log("finished adding to course "+ currentCourse);
		    
		};

		/**
		 * Renders a list of courses to the screen.
		 *
		 * @param courseList list of courses to display
		 */
		CourseSketch.lectureSelection.showCourses = function(courseList) {
			CourseSketch.lectureSelection.schoolItemBuilder = new SchoolItemBuilder();
			CourseSketch.lectureSelection.schoolItemBuilder.setList(courseList).setShowDate(false)
				.setBoxClickFunction(this.courseSelected).build(document.querySelector("#col1>.content"));
		};
	
		CourseSketch.lectureSelection.courseSelectionManager = new clickSelectionManager();
		var loadCourses = function(courseList) {
		    /* (waitingIcon.isRunning()) {
			waitingIcon.finishWaiting();
		    }*/
		    CourseSketch.lectureSelection.showCourses(courseList);
		}; // End loadCourses
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
    			    }); // End pollUpdates function
    			}
		    }, 100); // End interval function
		}
	});
})();
