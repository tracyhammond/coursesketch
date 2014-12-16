(function() {
    $(document).ready(function() {

        CourseSketch.lecturePage.doResize = function(event) {
            var target = event.target;

            // add the change in coords to the previous width of the target element
            var newWidth  = parseFloat($(target).width()) + event.dx;
            var newHeight = parseFloat($(target).height()) + event.dy;

            // update the element's style
            target.style.width  = newWidth + 'px';
            target.style.height = newHeight + 'px';

            target.textContent = newWidth + '×' + newHeight;
        };

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
                CourseSketch.lecturePage.selectedSlideIndex = slideIndex;
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
                    completionHandler();
                }, 10);

            } else {
                completionHandler();
            }
        }

        $(document).keydown(function(e) {
            switch(e.which) {
                case 37: // left
                    if (CourseSketch.lecturePage.selectedSlideIndex > 0) {
                        CourseSketch.lecturePage.selectSlide(CourseSketch.lecturePage.selectedSlideIndex - 1);
                    }
                break;

                case 38: // up
                break;

                case 39: // right
                    if (CourseSketch.lecturePage.selectedSlideIndex < CourseSketch.lecturePage.lecture.idList.length - 1) {
                        CourseSketch.lecturePage.selectSlide(CourseSketch.lecturePage.selectedSlideIndex + 1);
                    }

                break;

                case 40: // down
                break;

                case 27: // escape!

                break;
                default: return; // exit this handler for other keys
            }
            e.preventDefault(); // prevent the default action (scroll / move caret)
        });

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
