function LectureNavigator(lectureId, preferredIndex){
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
            if (idMessage.nav.nextLecture != null && !isUndefined(idMessage.nav.nextLecture)) {
                loadLecture(nextLectureId, idMessage.nav.nextSlide);
            } else {
                loadSlide(nextSlideId, nextSlideIndex);
            }
        }
    }

    function callCallback() {
        for (var i = 0; i < callbackList.length; i++) {
            callBacker(i);
        }
    }

    function callBack(scopedIndex) {
        setTimeout(function () {
            callbackList[scopedIndex](navScope);
        }, 20);
    }

    function changeSlide(index) {
        if (index < 0 || index >= currentLecture.idList.length) {
            return;
        }
        currentSlideIndex = index;
        currentSlide = currentLecture.idList[index];
        callCallback()
    }

    function loadSlide(nextSlideId, index) {
        function hasSlide(slide) {
            currentSlide = slide;
            callCallback()
            currentSlideIndex = index;
        }
        CourseSketch.dataManager.getLectureSlide(nextSlideId, hasSlide, hasSlide);
    }

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

    this.hasPrevious = function() {
        return currentSlideIndex > 0;
    };

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
}