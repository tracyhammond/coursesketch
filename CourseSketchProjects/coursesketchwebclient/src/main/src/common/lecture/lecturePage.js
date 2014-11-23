(function() {
        CourseSketch.lecturePage = [];
        if(isUndefined(CourseSketch.lecturePage.lecture)) {
		CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState("currentLecture");
		CourseSketch.dataManager.clearStates();
	}

        /**
         * Adds a new text box to the current lecture slide.
         */
        CourseSketch.lecturePage.newTextBox = function() {
            var textbox = document.createElement('text-box-creation');
	    document.querySelector("#slide-content").appendChild(textbox);
        }

        CourseSketch.lecturePage.addSlideToDom = function(slideIndex) {
            $("#slides>.content").append("<span class=\"slide-thumb\">" + slideIndex + "</span>");
        }

	CourseSketch.lecturePage.newSlide = function() {
            CourseSketch.lecturePage.lecture.slides.push([]);
            CourseSketch.lecturePage.addSlideToDom(CourseSketch.lecturePage.lecture.slides.length);
        }

        CourseSketch.lecturePage.displaySlides = function() {
            for(var i = 0; i < CourseSketch.lecturePage.lecture.slides.length; ++i) {
                CourseSketch.lecturePage.addSlideToDom(i);
            }
        }

        //CourseSketch.lecturePage.displaySlides();
})();