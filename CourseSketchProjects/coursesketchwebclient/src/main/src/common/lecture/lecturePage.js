(function() {
    CourseSketch.lecturePage = [];

    /**
     * Adds a new text box to the current lecture slide.
     */
    CourseSketch.lecturePage.newTextBox = function() {
        var textbox = document.createElement('text-box-creation');
    document.querySelector("#slide-content").appendChild(textbox);
    }

    CourseSketch.lecturePage.addSlideToDom = function(slideIndex) {
        var cssWidth = "calc(10vw + "
            + (CourseSketch.lecturePage.lecture.slides.length * 10.8)
            + "vw + 200px)"
        $("#slides>.content").css({
            width: cssWidth
        });
        $("#slides>.content").append("<span class=\"slide-thumb\">" + slideIndex + "</span>");
    }

    CourseSketch.lecturePage.newSlide = function() {
        var slide = CourseSketch.PROTOBUF_UTIL.LectureSlide();
        slide.id = generateUUID();
        slide.lectureId = CourseSketch.lecturePage.lecture.id;
        slide.unlocked = true;
        var finishGetCourse = function(lecture) {
            
            // TODO: This needs to be changed once insertSlide works!!!
            //CourseSketch.lecturePage.lecture = lecture;
            CourseSketch.lecturePage.lecture.slides.push(slide);
            
            CourseSketch.lecturePage.addSlideToDom(CourseSketch.lecturePage.lecture.slides.length);
        }
        var finishInsert = function(lecture) {
            CourseSketch.dataManager.getCourseLecture(CourseSketch.lecturePage.lecture.id, finishGetCourse, finishGetCourse);
        }
        CourseSketch.dataManager.insertSlide(slide, finishInsert, finishInsert);
    }

    CourseSketch.lecturePage.displaySlides = function() {
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
})();