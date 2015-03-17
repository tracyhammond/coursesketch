validateFirstRun(document.currentScript);

/**
 * @namespace "lecturePage/instructor"
 */
(function() {
    $(document).ready(function() {

        /**
         * Saves the textbox for viewing use later.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.saveTextBox = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.textBox = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        };

        /**
         * Saves the question for viewing use later.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.saveQuestion = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getSrlQuestionClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.question = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        };

        /**
         * Saves the image for viewing use later.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.saveImageBox = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getImageClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.image = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        };

        /**
         * Saves the embedded html for viewing use later.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.saveEmbeddedHtml = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getEmbeddedHtmlClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.embeddedHtml = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        };

        /**
         * Selects a specific lecture slide.
         *
         * @param {Integer} slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.selectSlide = function(slideIndex) {
            /**
             * Called when the specific slide is completely loaded.
             * @memberof "lecturePage/instructor"
             */
            var completionHandler = function() {
                $('.slide-thumb').each(function() {
                    $(this).removeClass('selected');
                });
                $('#' + slideIndex + '.slide-thumb').addClass('selected');
                CourseSketch.lecturePage.selectedSlideIndex = slideIndex;
                CourseSketch.dataManager.getLectureSlide(CourseSketch.lecturePage
                    .lecture.idList[slideIndex].id, CourseSketch.lecturePage.renderSlide,
                    CourseSketch.lecturePage.renderSlide);
                CourseSketch.lecturePage.removeWaitOverlay();
            };
            if (!isUndefined(CourseSketch.lecturePage.currentSlide)) {
                CourseSketch.lecturePage.addWaitOverlay();

                // Need to do small delay here so the wait overlay actually shows up
                setTimeout(function() {
                    var elements = document.getElementById('slide-content').children;

                    // Need to remove all the old elements; they will be replaced by the new ones
                    CourseSketch.lecturePage.currentSlide.elements = [];

                    for (var i = 0; i < elements.length; ++i) {
                        elements[i].saveData();
                    }
                    CourseSketch.dataManager.updateSlide(CourseSketch.lecturePage.currentSlide, completionHandler);
                }, 10);

            } else {
                completionHandler();
            }
        };

        /**
         * Adds a new slide to the current lecture.
         * @memberof "lecturePage/instructor"
         */
        CourseSketch.lecturePage.newSlide = function() {
            CourseSketch.lecturePage.addWaitOverlay();
            var slide = CourseSketch.PROTOBUF_UTIL.LectureSlide();
            slide.id = generateUUID();
            slide.lectureId = CourseSketch.lecturePage.lecture.id;
            slide.unlocked = true;

            /**
             * Called after the course lecture is grabbed.
             * @memberof "lecturePage/instructor"
             */
            var finishGetCourseLecture = function(lecture) {
                var id = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
                id.id = slide.id;
                id.isSlide = true;
                id.unlocked = true;
                CourseSketch.lecturePage.lecture = lecture;
                CourseSketch.lecturePage.displaySlides();
                CourseSketch.lecturePage.removeWaitOverlay();
            };

            /**
             * Called when the slide is sucesfully inserted into the database.
             * @memberof "lecturePage/instructor"
             */
            var finishInsert = function(slide) {
                CourseSketch.dataManager.getCourseLecture(slide.lectureId, finishGetCourseLecture, finishGetCourseLecture);
            };
            CourseSketch.dataManager.insertSlide(slide, finishInsert, finishInsert);
        };

        // Do setup
        if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
            CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState('currentLecture');
            CourseSketch.dataManager.clearStates();
            CourseSketch.lecturePage.displaySlides();
        } else {
            var intervalVar = setInterval(function() {
                if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
                    clearInterval(intervalVar);
                    CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState('currentLecture');
                    CourseSketch.dataManager.clearStates();
                    CourseSketch.lecturePage.displaySlides();
                }
            }, 100);
        }
        interact('.resize').resizable(true).on('resizemove', CourseSketch.lecturePage.doResize);
    });
})();
