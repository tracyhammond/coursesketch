(function() {
    $(document).ready(function() {
        CourseSketch.lecturePage = [];
        CourseSketch.lecturePage.waitScreenManager = new WaitScreenManager();

        CourseSketch.lecturePage.doResize = function(event) {
            var target = event.target;

            // add the change in coords to the previous width of the target element
            var newWidth  = parseFloat($(target).width()) + event.dx;
            var newHeight = parseFloat($(target).height()) + event.dy;

            // update the element's style
            target.style.width  = newWidth + 'px';
            target.style.height = newHeight + 'px';

            target.textContent = newWidth + '×' + newHeight;
        }

        /**
         * Selects a specific lecture slide.
         *
         * @param slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         */
        CourseSketch.lecturePage.selectSlide = function(slideIndex) {
            var completionHandler = function() {
                $(".slide-thumb").each(function() {
                    $(this).removeClass("selected");
                })
                $("#" + slideIndex + ".slide-thumb").addClass("selected");
                CourseSketch.dataManager.getLectureSlide(CourseSketch.lecturePage
                    .lecture.idList[slideIndex].id, CourseSketch.lecturePage.renderSlide,
                    CourseSketch.lecturePage.renderSlide);
                CourseSketch.lecturePage.removeWaitOverlay();
            }
            if(!isUndefined(CourseSketch.lecturePage.currentSlide)) {
                CourseSketch.lecturePage.addWaitOverlay();

                // Need to do small delay here so the wait overlay actually shows up
                setTimeout(function() {
                    var elements = document.getElementById("slide-content").children;

                    // Need to remove all the old elements; they will be replaced by the new ones
                    CourseSketch.lecturePage.currentSlide.elements = [];

                    for(var i = 0; i < elements.length; ++i) {
                        elements[i].saveData();
                    }
                    CourseSketch.dataManager.updateSlide(CourseSketch.lecturePage.currentSlide, completionHandler, completionHandler);
                }, 10);

            } else {
                completionHandler();
            }
        }

        // Do setup
        if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
            CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState("currentLecture");
            CourseSketch.dataManager.clearStates();
            CourseSketch.lecturePage.displaySlides();
        } else {
            var intervalVar = setInterval(function() {
                if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
                    clearInterval(intervalVar);
                    CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState("currentLecture");
                    CourseSketch.dataManager.clearStates();
                    CourseSketch.lecturePage.displaySlides();
                }
            }, 100);
        }
        interact('.resize').resizable(true).on('resizemove', CourseSketch.lecturePage.doResize);
    });
})();
