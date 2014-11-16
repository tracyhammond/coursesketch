(function() {
    $(document).ready(function() {
        CourseSketch.lectureSelection.courseSelectionManager = new clickSelectionManager();
        CourseSketch.lectureSelection.currentCourse = undefined;
    
        /**
         * Renders a list of lectures to the screen.
         * 
         * @param lectureList
         *                list of lectures to display
         */
        CourseSketch.lectureSelection.displayLectures = function(lectureList) {
            var add = $("#add").clone();
            var schoolItemBuilder = new SchoolItemBuilder();
            schoolItemBuilder.setList(lectureList)
                .setShowDate(false)
                .build(document.querySelector("#col2>.content"));
            $("#col2>.content").prepend(add);
            $("#add").bind("click", CourseSketch.lectureSelection.addLecture);
            $("#add").addClass("show");
        };
    
        /**
         * Called when a course is selected. Updates selection
         * and gets lectures for the course.
         * 
         * @param course
         *                course object of the selected element
         */
        CourseSketch.lectureSelection.courseSelected = function(course) {
            var courseid = course.id;
            this.currentCourse = course.id;
            CourseSketch.dataManager.getCourseLectures(course.lectureList,
                CourseSketch.lectureSelection.displayLectures);
            CourseSketch.lectureSelection.courseSelectionManager
                .clearAllSelectedItems();
            CourseSketch.lectureSelection.courseSelectionManager
                .addSelectedItem(document.getElementById(courseid));
            CourseSketch.dataManager.getCourse(courseid, function(course) {
                CourseSketch.dataManager.getCourseLectures(course.lectureList,
                    CourseSketch.lectureSelection.displayLectures);
            });
        };
    
        /**
         * Adds a new lecture to the currently selected course.
         * 
         * @param evt
         *                event from click (or other) action
         */
        CourseSketch.lectureSelection.addLecture = function(addLectureCallback) {
            var lecture = CourseSketch.PROTOBUF_UTIL.Lecture();
            lecture.courseId = currentCourse;
            lecture.name = "Untitled Lecture";
            lecture.id = generateUUID();
            lecture.description = "N/A";
            var insertCallback = function() {
                CourseSketch.dataManager.getCourse(currentCourse, 
                    function(course) {
                        CourseSketch.dataManager.getCourseLectures(
                            course.lectureList,
                            CourseSketch.lectureSelection.displayLectures);
                            console.log("finished adding to course "
                                + currentCourse);
                            addLectureCallback(course);
                    });
            };
            CourseSketch.dataManager.insertLecture(lecture, insertCallback, insertCallback);
        };
    
        /**
         * Renders a list of courses to the screen.
         *
         * @param courseList list of courses to display
         */
        CourseSketch.lectureSelection.showCourses = function(courseList) {
            CourseSketch.lectureSelection.schoolItemBuilder = new SchoolItemBuilder();
            CourseSketch.lectureSelection.schoolItemBuilder
                .setList(courseList)
                .setShowDate(false)
                .setBoxClickFunction(this.courseSelected)
                .build(document.querySelector("#col1>.content"));
        };
        
        var loadCourses = function(courseList) {
            /* (waitingIcon.isRunning()) {
            waitingIcon.finishWaiting();
            }*/
            CourseSketch.lectureSelection.showCourses(courseList);
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
})();
