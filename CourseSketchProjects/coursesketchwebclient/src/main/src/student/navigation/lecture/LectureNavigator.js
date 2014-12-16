function LectureNavigator(lectureId, preferredIndex) {
    var callbackList = [];
    var currentLecture;
    var currentLectureId = lectureId;
    var currentSlideIndex = preferredIndex;
    var lectureIdStack = new Array();
    var indicesStack = new Array();
    var currentSlide;
    var isDone = false;

    this.goToSlide = function goToSlide(index) {
        changeSlide(index);
    };

    /**
     * Goes to the next slide
     * If the next slide equals -1 and the stack is empty
     * then you are at the end of the lecture
     * If the next slide equals -1 and the stack is not empty
     * then you are at the end of a lecture within another lecture
     * If the next slide is not equal to -1 and the nav's nextLecture != null
     * then you are moving into a lecture
     * If the next slide is not equal to -1 and the nav's nextLecture == null
     * then you are moving to a slide within the same lecture
     */
    this.goToNextSlide = function goToNextSlide() {
        var idMessage = currentLecture.idList[currentSlideIndex];
        if (idMessage.nav.nextSlide === -1) {
            if(indicesStack.length == 0) {
                isDone = true;
                callCallback();
                return;
            } else {
                var nextSlideIndex = indicesStack.pop();
                var nextLectureId = indicesStack.pop();
                function hasLecture(lecture) {
                    currentLecture = lecture;
                    currentLectureId = lecture.id;
                    if (!(lecture.idList[nextSlideIndex].isSlide)) {
                        loadLecture(lecture.idList[nextSlideIndex].id, lecture.idList[nextSlideIndex].nav.nextSlide);
                    } else {
                        loadSlide(lecture.idList[nextSlideIndex].id, nextSlideIndex);
                    }
                }
            }
        } else {
            if (idMessage.nav.nextLectureId != null && !isUndefined(idMessage.nav.nextLectureId)) {
                loadLecture(idMessage.nav.nextLectureId, idMessage.nav.nextSlide);
            } else {
                loadSlide(currentLecture.idList[idMessage.nav.nextSlide].id, idMessage.nav.nextSlide);
            }
        }
    }

    /**
     * asynchronously calls the callbacks
     */
    function callCallback() {
        for (var i = 0; i < callbackList.length; i++) {
            callBacker(i);
        }
    }

    /**
     * Scopes the index for the callbackList.
     * this way the browser is not locked up by callbacks.
     */
    function callBacker(scopedIndex) {
        var navScope = this;
        setTimeout(function() {callbackList[scopedIndex](navScope);},20);
    }

    function changeSlide(index) {
        if (index < 0 || index >= currentLecture.idList.length) {
            return;
        }
        currentSlideIndex = index;
        currentSlide = currentLecture.idList[index];
        callCallback()
    }

    /**
     * Loads a slide with an id and the index at which this slide exist.
     * @param index The new index that will become the current index.
     * @param nextSlideId the id of the next slide that will become the current slide.
     */
    function loadSlide(nextSlideId, index) {
        function hasSlide(slide) {
            currentSlide = slide;
            currentSlideIndex = index;
            callCallback();
        }
        CourseSketch.dataManager.getLectureSlide(nextSlideId, hasSlide, hasSlide);
    }

    /**
     * loads a lecture from the given lecture id
     * @param nextLectureId
     *         id of next lecture
     * @param index
     *         the index of the slide in the lecture we want
     */
    function loadLecture(nextLectureId, index) {
        lectureIdStack.push(currentLectureId);
        indicesStack.push(index);
        function hasLecture(lecture) {
            currentLecture = lecture;
            currentLectureId = lecture.id;
            if (!(lecture.idList[0].isSlide)) {
                loadLecture(lecture.idList[0].id, lecture.idList[0].nav.nextSlide);
            } else {
                loadSlide(lecture.idList[0].id, 0);
            }
        }
        CourseSketch.dataManager.getCourseLecture(nextLectureId, hasLecture, hasLecture);
    }

    this.addCallback = function(callback) {
        callbackList.push(callback);
    };

    /**
     * Does not apply to the entire scope just the current lecture scope
     */
    this.hasPrevious = function() {
        return currentSlideIndex > 0;
    };

    /**
     * Does not apply to the entire scope just the current lecture scope
     */
    this.hasNext = function() {
        return currentSlideIndex < currentLecture.idList.length;
    }

    this.getCurrentNumber = function() {
        return currentSlideIndex + 1;
    }

    this.getLength = function() {
        return currentLecture.idList.length;
    }

    this.getCurrentSlideId = function() {
        return currentSlide.id;
    }

    this.getCurrentSlide = function() {
        return currentSlide;
    }

    this.getCurrentLectureId = function() {
        return currentLectureId;
    }

    /**
     * removes a callback from the callback list
     */
    this.removeCallback = function(callback) {
        var index = callbackList.indexOf(callback);
        if (index >= 0 ) {
            callbackList.splice(index, 1);
        }
    };

    /**
     * refresh reloads the current slide and lecture
     */
    this.refresh = function() {
        function hasLecture(lecture) {
            currentLecture = lecture;
            var id = currentLecture.idList[currentSlideIndex].id;
            loadSlide(id, currentSlideIndex);
        }
        CourseSketch.dataManager.getCourseLecture(currentLectureId, hasLecture, hasLecture);
    }
}
