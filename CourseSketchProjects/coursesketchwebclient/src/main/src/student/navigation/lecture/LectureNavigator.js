// jscs:disable

/**
 * A navigator for navigating lectures.
 *
 * Takes in the perferredIndex and a lecture Id.
 * @param {String} lectureId - the id to start the lecture at
 * @param {Number} preferredIndex - the number slide to start at.
 * @constructor
 */
function LectureNavigator(lectureId, preferredIndex) {
    var callbackList = [];
    var currentLecture;
    var currentLectureId = lectureId;
    var currentSlideIndex = preferredIndex;
    var lectureIdStack = [];
    var indicesStack = [];
    var currentSlide;
    var isDone = false;

    /**
     * Goes to a slide at the specific index in the current lecture.
     *
     * @param {Number} index - The index to navigate the lecture to.
     */
    this.goToSlide = function goToSlide(index) {
        changeSlide(index);
    };

    /**
     * Navigate from an embedded lecture to the one that contains it.
     */
    function popUp() {
        var nextSlideIndex = indicesStack.pop();
        var nextLectureId = lectureIdStack.pop();
        while (nextSlideIndex === -1 && indicesStack.length > 0) {
            nextSlideIndex = indicesStack.pop();
            nextLectureId = lectureIdStack.pop();
        }

        /**
         * Called when the lecture is loaded.
         * @param {SrlLecture} lecture - a proto lecture object.
         */
        function hasLecture(lecture) {
            if (nextSlideIndex === -1) {
                isDone = true;
                callCallback();
                return;
            }
            currentLecture = lecture;
            currentLectureId = lecture.id;
            var idMessage = currentLecture.idList[nextSlideIndex];

            if (idMessage.nav.nextLectureId !== null && !isUndefined(idMessage.nav.nextLectureId)) {
                loadLecture(idMessage.nav.nextLectureId, idMessage.nav.nextSlide);
            } else {
                loadSlide(idMessage.id, nextSlideIndex);
            }
        }
        CourseSketch.dataManager.getCourseLecture(nextLectureId, hasLecture, hasLecture);
    }

    /**
     * Goes to the next slide
     * If the next slide equals -1 and the stack is empty
     * then you are at the end of the lecture
     * If the next slide equals -1 and the stack is not empty
     * then you are at the end of a lecture within another lecture
     * If the next slide is not equal to -1 and the nav's nextLecture !== null
     * then you are moving into a lecture
     * If the next slide is not equal to -1 and the nav's nextLecture === null
     * then you are moving to a slide within the same lecture
     */
    this.goToNextSlide = function goToNextSlide() {
        var idMessage = currentLecture.idList[currentSlideIndex];
        if (idMessage.nav.nextSlide === -1 && (idMessage.nav.nextLectureId === null || isUndefined(idMessage.nav.nextLectureId))) {
            if (indicesStack.length === 0) {
                isDone = true;
                callCallback();
                return;
            } else {
                popUp();
            }
        } else {
            if (idMessage.nav.nextLectureId !== null && !isUndefined(idMessage.nav.nextLectureId)) {
                loadLecture(idMessage.nav.nextLectureId, idMessage.nav.nextSlide);
            } else {
                loadSlide(currentLecture.idList[idMessage.nav.nextSlide].id, idMessage.nav.nextSlide);
            }
        }
    };

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
        var localScope = this;
        setTimeout(function() {
            callbackList[scopedIndex](localScope);
        }, 20);
    }

    /**
     * Changes the slide to the given index.
     *
     * @param {Integer} index
     */
    function changeSlide(index) {
        if (index < 0 || index >= currentLecture.idList.length) {
            // TODO: change this to a lecture exception.
            throw new CourseSketch.BaseException('Index is not valid: ' + index);
        }
        currentSlideIndex = index;
        currentSlide = currentLecture.idList[index];
        callCallback();
    }

    /**
     * Loads a slide with an id and the index at which this slide exist.
     * @param {String} nextSlideId - the id of the next slide that will become the current slide.
     * @param {Integer} index - The new index that will become the current index.
     */
    function loadSlide(nextSlideId, index) {
        /**
         * Called when the slide has been loaded.
         * @param {SrlSlide} slide - the protobuf slide object.
         */
        function hasSlide(slide) {
            currentSlide = slide;
            currentSlideIndex = index;
            callCallback();
        }
        CourseSketch.dataManager.getLectureSlide(nextSlideId, hasSlide, hasSlide);
    }

    /**
     * loads a lecture from the given lecture id
     * @param {String} nextLectureId
     *         id of next lecture
     * @param {Integer} index
     *         the index of the slide in the lecture we want
     */
    function loadLecture(nextLectureId, index) {
        lectureIdStack.push(currentLectureId);
        indicesStack.push(index);
        /**
         * Called when the lecture has loaded from the server.
         * @param {SrlLecture} lecture - the lecture protobuf object from the server.
         */
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

    /**
     * Callback that is called after a slide has been loaded.
     * @param {Function} callback - called after the function has been loaded.
     */
    this.addCallback = function(callback) {
        callbackList.push(callback);
    };

    /**
     * Does not apply to the entire scope just the current lecture scope.
     */
    this.hasPrevious = function() {
        return currentSlideIndex > 0;
    };

    /**
     * Does not apply to the entire scope just the current lecture scope.
     */
    this.hasNext = function() {
        return currentSlideIndex < currentLecture.idList.length;
    };

    /**
     * Returns the current slide number in a human readable format.
     *
     * @returns {Number} slide index + 1
     */
    this.getCurrentNumber = function() {
        return currentSlideIndex + 1;
    };

    /**
     * @returns {Number} Number of slides in the current lecture.
     */
    this.getLength = function() {
        return currentLecture.idList.length;
    };

    /**
     * @returns {String} The id of the current slide.
     */
    this.getCurrentSlideId = function() {
        return currentSlide.id;
    };

    /**
     * @returns {Slide} A protobuf object that represents the current slide.
     */
    this.getCurrentSlide = function() {
        return currentSlide;
    };

    /**
     * @returns {String} The id of the current lecture.
     */
    this.getCurrentLectureId = function() {
        return currentLectureId;
    };

    /**
     * @returns {Boolean} True if the lecture is done false otherwise.
     */
    this.getIsDone = function() {
        return isDone;
    };

    /**
     * Removes a callback from the callback list.
     */
    this.removeCallback = function(callback) {
        var index = callbackList.indexOf(callback);
        if (index >= 0) {
            callbackList.splice(index, 1);
        }
    };

    /**
     * Refresh reloads the current slide and lecture.
     */
    this.refresh = function() {
        /**
         * Called when the lecture has loaded from the server.
         *
         * @param {SrlLecture} lecture - the lecture protobuf object from the server.
         */
        function hasLecture(lecture) {
            currentLecture = lecture;
            var id = currentLecture.idList[currentSlideIndex].id;
            loadSlide(id, currentSlideIndex);
        }
        CourseSketch.dataManager.getCourseLecture(currentLectureId, hasLecture, hasLecture);
    };
}
