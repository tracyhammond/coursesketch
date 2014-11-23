(function() {
    CourseSketch.lecturePage = [];

    /**
     * Adds a new text box to the current lecture slide.
     */
    CourseSketch.lecturePage.newTextBox = function() {
        var textbox = document.createElement('text-box-creation');
        document.querySelector("#slide-content").appendChild(textbox);
    }

    CourseSketch.lecturePage.selectSlide = function(slideIndex) {
        $(".slide-thumb").each(function() {
            $(this).removeClass("selected");
        })
        $("#" + slideIndex + ".slide-thumb").addClass("selected");
    }
    
    CourseSketch.lecturePage.addSlideToDom = function(slideIndex) {
        var cssWidth = "calc(10vw + "
            + (CourseSketch.lecturePage.lecture.slides.length * 10.84)
            + "vw + 100px)"
        $("#slides>.content").css({
            width: cssWidth
        });
        $("#slides>.content").append("<span id=\""
            + slideIndex + "\" class=\"slide-thumb\" onclick=\"CourseSketch.lecturePage.selectSlide("
            + slideIndex + ")\">" + slideIndex + "</span>");
    }

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
            CourseSketch.dataManager.getCourseLecture(CourseSketch.lecturePage.lecture.id, finishGetCourse, finishGetCourse);
        }
        CourseSketch.dataManager.insertSlide(slide, finishInsert, finishInsert);
    }

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
})();