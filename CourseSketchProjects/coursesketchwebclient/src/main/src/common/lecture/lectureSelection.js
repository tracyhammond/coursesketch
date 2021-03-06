validateFirstRun(document.currentScript);
/**
 * @namespace lectureSelection
 */

(function() {
    $(document).ready(function() {
        CourseSketch.lectureSelection.courseSelectionManager = new ClickSelectionManager();
        CourseSketch.lectureSelection.currentCourse = undefined;

        /**
         * Function to be called when a lecture has finished editing.
         *
         * @param {protoObjectAttribute} attributeChanged
         *            the name of the protobuf attribute that changed
         * @param {protoObjectAttributeValue} oldValue
         *            the attribute's old value
         * @param {protoObjectAttribute} newValue
         *            the attribute's new value
         * @param {protoObject} lectureObject
         *            protobuf element that has been edited
         * @memberof lectureSelection
         */
        CourseSketch.lectureSelection.lectureEndEdit = function(attributeChanged, oldValue, newValue, lectureObject) {
            element[attributeChanged] = newValue;
            CourseSketch.dataManager.updateLecture(lectureObject);
        };

        /**
         * Function that is called when a lecture is selected (clicked on).
         *
         * @param {protoObject} lecture
         *            protobuf object of the lecture that was selected
         * @memberof lectureSelection
         */
        CourseSketch.lectureSelection.lectureSelected = function(lecture) {
            CourseSketch.dataManager.addState('currentLecture', lecture);
            if (CourseSketch.connection.isInstructor) {
                CourseSketch.redirectContent('/src/instructor/lecture/lecturePage.html', 'Edit Lecture');
            } else {
                CourseSketch.redirectContent('/src/student/lecture/lecturePage.html', 'View Lecture');
            }
        };

        /**
         * Renders a list of lectures to the screen.
         *
         * @param {list} lectureList
         *                list of lectures to display
         * @memberof lectureSelection
         */
        CourseSketch.lectureSelection.displayLectures = function(lectureList) {
            if (lectureList[0] instanceof CourseSketch.DatabaseException) {
                throw lectureList[0];
            }
            var add = $('#add').clone();
            var schoolItemBuilder = new SchoolItemBuilder();
            schoolItemBuilder.setList(lectureList)
                .setShowDate(false)
                .setEditCallback(CourseSketch.lectureSelection.lectureEndEdit)
                .setInstructorCard(CourseSketch.connection.isInstructor)
                .setBoxClickFunction(CourseSketch.lectureSelection.lectureSelected)
                .build(document.querySelector('#col2>.content'));
            if (CourseSketch.connection.isInstructor) {
                $('#col2>.content').prepend(add);
                $('#add').bind('click', CourseSketch.lectureSelection.addLecture);
                $('#add').addClass('show');
            }
        };

        /**
         * Updates selection and gets lectures for the course.
         *
         * Called when a course is selected.
         *
         * @param {Object} course
         *                course object of the selected element
         * @memberof lectureSelection
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
            CourseSketch.dataManager.getCourse(courseid, function(foundCourse) {
                CourseSketch.dataManager.getCourseLectures(foundCourse.lectureList,
                    CourseSketch.lectureSelection.displayLectures,
                    CourseSketch.lectureSelection.displayLectures);
            });
        };

        /**
         * Adds a new lecture to the currently selected course.
         *
         * @param {event} evt
         *                event from click (or other) action
         * @param {Function} addLectureCallback - A function that is called after the lecture is added.
         * @memberof lectureSelection
         */
        CourseSketch.lectureSelection.addLecture = function(evt, addLectureCallback) {
            var lecture = CourseSketch.prutil.Lecture();
            lecture.courseId = currentCourse;
            lecture.name = 'Untitled Lecture';
            lecture.id = generateUUID();
            lecture.description = 'N/A';

            /**
             * Called after inserting a lecture into the database.
             *
             * @memberof lectureSelection
             */
            var insertCallback = function() {
                CourseSketch.dataManager.getCourse(currentCourse,
                    function(course) {
                        CourseSketch.dataManager.getCourseLectures(
                            course.lectureList,
                            CourseSketch.lectureSelection.displayLectures);
                        console.log('finished adding to course ' + currentCourse);
                        if (!isUndefined(addLectureCallback)) {
                            addLectureCallback(course);
                        }
                    });
            };
            CourseSketch.dataManager.insertLecture(lecture, insertCallback, insertCallback);
        };

        /**
         * Renders a list of courses to the screen.
         *
         * @param {List<SrlCourse>} courseList - List of courses to display.
         * @memberof lectureSelection
         */
        CourseSketch.lectureSelection.showCourses = function(courseList) {
            CourseSketch.lectureSelection.schoolItemBuilder = new SchoolItemBuilder();
            CourseSketch.lectureSelection.schoolItemBuilder
                .setList(courseList)
                .setShowDate(false)
                .setBoxClickFunction(this.courseSelected)
                .build(document.querySelector('#col1>.content'));
        };

        /**
         * Loads the courses from the database.
         *
         * @param {List<SrlCourse>} courseList - List of courses to display.
         * @memberof lectureSelection
         */
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
                    }); // Pollupdates
                }
            }, 100);
        }
    });
})();
