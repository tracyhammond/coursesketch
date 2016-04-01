validateFirstRun(document.currentScript);


/**
 * @namespace "lecturePage/student"
 */

(function() {
    $(document).ready(function() {
        /**
         * Selects a specific lecture slide.
         *
         * @param {Integer} slideIndex - Index of the slide in the current lecture's protobuf object.
         * @memberof "lecturePage/student"
         */
        CourseSketch.lecturePage.selectSlide = function(slideIndex) {
            /**
             * Called when a user is changing slide.
             */
            var completionHandler = function() {
                $('.slide-thumb').each(function() {
                    $(this).removeClass('selected');
                });
                $('#' + slideIndex + '.slide-thumb').addClass('selected');
                CourseSketch.lecturePage.selectedSlideIndex = slideIndex;
                CourseSketch.lecturePage.removeWaitOverlay();
            };
            if (!isUndefined(CourseSketch.lecturePage.currentSlide)) {
                CourseSketch.lecturePage.addWaitOverlay();

                // Need to do small delay here so the wait overlay actually shows up
                setTimeout(function() {
                    completionHandler();
                }, 10);

            } else {
                completionHandler();
            }
        };

        $(document).keydown(function(e) {
            switch (e.which) {
                case 37: { // left
                    CourseSketch.lecturePage.navigation.gotoPrevious();
                }
                break;

                case 38: // up
                break;

                case 39: { // right
                    CourseSketch.lecturePage.navigation.gotoNext();
                }
                break;

                case 40: // down
                break;

                case 27: // escape!
                break;

                default:
                    return; // exit this handler for other keys
            }
            e.preventDefault(); // prevent the default action (scroll / move caret)
        });

        /**
         * Sets up the lecture for registering and displaying the slides.
         */
        function setup() {
            CourseSketch.lecturePage.lectureId = CourseSketch.dataManager.getState('currentLecture');
            CourseSketch.lecturePage.navigation.resetNavigation(CourseSketch.lecturePage.lectureId);
            CourseSketch.dataManager.clearStates();
        }

        // Do setup
        if (CourseSketch.dataManager.isDatabaseReady() && !isUndefined(CourseSketch.lecturePage.lectureId)) {
            setup();
        } else {
            var intervalVar = setInterval(function() {
                if (CourseSketch.dataManager.isDatabaseReady() && !isUndefined(CourseSketch.lecturePage.lectureId)) {
                    clearInterval(intervalVar);
                    setup();
                }
            }, 100);
        }
        interact('.resize').resizable(true).on('resizemove', CourseSketch.lecturePage.doResize);
    });
})();
