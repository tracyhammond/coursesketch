(function() {
    $(document).ready(function() {
        CourseSketch.lecturePage = [];

        /**
         * Adds a new text box to the currently selected lecture slide.
         */
        CourseSketch.lecturePage.newTextBox = function() {
            var textbox = document.createElement('text-box-creation');
            //CourseSketch.lecturePage.slide.elements.push(textbox);
            //CourseSketch.dataManager.updateSlide(CourseSketch.lecturePage.slide);
            document.querySelector("#slide-content").appendChild(textbox);
        }

        CourseSketch.lecturePage.newSketchContent = function() {
            var sketchSurface = document.createElement('sketch-surface');
            document.querySelector("#slide-content").appendChild(sketchSurface);
            //CourseSketch.lecturePage.slide.elements.push(sketchSurface);
            //CourseSketch.dataManager.updateSlide(CourseSketch.lecturePage.slide);
            setTimeout(function() {
                sketchSurface.resizeSurface();
            }, 500);
        }

        /**
         * Renders a slide to the DOM.
         *
         * NOTE: THIS FUNCTION IS NOT COMPLETE!
         *
         * @param slide
         *            protobuf slide element to be rendered
         */
        CourseSketch.lecturePage.renderSlide = function(slide) {
            CourseSketch.lecturePage.slide = slide;
            console.log(slide);
        }


        /**
         * Selects a specific lecture slide.
         *
         * @param slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         */
        CourseSketch.lecturePage.selectSlide = function(slideIndex) {
            $(".slide-thumb").each(function() {
                $(this).removeClass("selected");
            })
            $("#" + slideIndex + ".slide-thumb").addClass("selected");
            CourseSketch.dataManager.getLectureSlide(CourseSketch.lecturePage
                .lecture.slides[slideIndex], CourseSketch.lecturePage.renderSlide,
                CourseSketch.lecturePage.renderSlide);
        }

        /**
         * Adds a slide thumbnail to the DOM.
         *
         * @param slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         */
        CourseSketch.lecturePage.addSlideToDom = function(slideIndex) {
            var slideThumb = document.createElement("span");
            slideThumb.id = slideIndex;
            slideThumb.className = "slide-thumb";
            slideThumb.textContent = slideIndex + 1;
            slideThumb.onclick = function() {
                CourseSketch.lecturePage.selectSlide(slideIndex);
            };
            document.querySelector("#slides>.content").appendChild(slideThumb);
        }

        /**
         * Adds a new slide to the current lecture.
         */
        CourseSketch.lecturePage.newSlide = function() {
            var slide = CourseSketch.PROTOBUF_UTIL.LectureSlide();
            slide.id = generateUUID();
            slide.lectureId = CourseSketch.lecturePage.lecture.id;
            slide.unlocked = true;
            var finishGetCourse = function(lecture) {
                CourseSketch.lecturePage.lecture.slides.push(slide.id);
                CourseSketch.lecturePage.displaySlides();
            }
            var finishInsert = function(lecture) {
                CourseSketch.dataManager.getCourseLecture(CourseSketch.lecturePage
                    .lecture.id, finishGetCourse, finishGetCourse);
            }
            CourseSketch.dataManager.insertSlide(slide, finishInsert, finishInsert);
        }

        /**
         * Displays all of the slides for the current lecture.
         */
        CourseSketch.lecturePage.displaySlides = function() {
            $("#lecture-title").text(CourseSketch.lecturePage.lecture.name);
            $(".slide-thumb:not(\"#add\")").each(function() {
                $(this).remove();
            });
            for(var i = 0; i < CourseSketch.lecturePage.lecture.slides.length; ++i) {
                CourseSketch.lecturePage.addSlideToDom(i);
            }
        }

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
    });
})();
