function SlideDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
    var dataListener = advanceDataListener;
    var database = parentDatabase;
    var Request = request
    var localScope = parent;
    var ByteBuffer = buffer;

    /**
     * Sets a slide in the local database
     *
     * @param slide is a slide object
     *
     * @param slideCallback function to be called after the slide setting is done
     */
    function setSlide(slide, slideCallback) {
        database.putInSlides(slide.id, slide.toBase64(), function(e, request) {
            if (!isUndefined(slideCallback)) {
                slideCallback(e,request);
            }
        });
    }
    parent.setSlide = setSlide;

    /**
     * Sets a slide in the server database
     *
     * @param slide is a slide object
     * @param slideCallback function to be called after the slide setting is done
     */
    function insertSlideServer(slide, slideCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
            var resultArray = item.getReturnText().split(":");
            var oldId = resultArray[1];
            var newId = resultArray[0];
            getSlideLocal(oldId, function(slide2) {
                deleteSlide(oldId);
                if(!isUndefined(slide2)) {
                    slide2.id = newId;
                    setSlide(slide2, function() {
                        slideCallback(slide2);
                    });
                } else {
                    slide.id = newId;
                    setSlide(slide, function() {
                        slideCallback(slide);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, slide.toArrayBuffer());
    }

    /**
     * Sets a lecture in both local and server databases.
     *
     * @param lecture
     *                lecture object to set
     * @param localCallback
     *                function to be called local lecture setting is done
     * @param serverCallback
     *                function to be called after server lecture setting is done
     */
    function updateSlide(slide, localCallback, serverCallback) {
        setSlide(slide, localCallback);
        sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, slide.toArrayBuffer());
        advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.LECTURESLIDE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
            serverCallback(item);
        });
    }
    parent.updateSlide = updateSlide;

    /**
     * Adds a new slide to both local and server databases. Also updates the
     * corresponding slide given by the lecture's courseId.
     *
     * @param slide slide object to insert
     * @param localCallback function to be called after local insert is done
     * @param serverCallback function to be called after server insert is done
     */
    function insertSlide(slide, localCallback, serverCallback) {
        setSlide(slide, function(e, request) {
            console.log("inserted locally :" + slide.id)
            if (!isUndefined(localCallback)) {
                try {
                    localCallback(e, request);
                } catch(exception) {
                    // ignore callback problems we want to succeed
                }
            }
            insertSlideServer(slide, function(slideUpdated) {
                parent.getCourseLecture(slide.lectureId, function(lecture) {
                    var idsInLectureList = lecture.idList;
                    var idInLecture = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
                    idInLecture.id = slideUpdated.id;
                    idInLecture.isSlide = true;
                    idsInLectureList.push(idInLecture);
                    parent.setLecture(lecture, function() {
                        if(!isUndefined(serverCallback)) {
                            serverCallback(slideUpdated);
                        }
                    });// end of setLecture
                    // Lecture is set with its new slide
                });
                // Finished with the slide
            });
            // Finished with setting slide
        });
        // Finished with local slide
    }
    parent.insertLecture = insertLecture;

    /**
     * Deletes a slide from local database.
     *
     * @param slideId ID of the lecture to delete
     * @param lectureCallback function to be called after the deletion is done
     */
    function deleteSlide (slideId, slideCallback) {
        database.deleteFromSlides (slideId, function (e, request) {
            if (!isUndefined(slideCallback)) {
                slideCallback (e, request);
            }
        });
    }
    parent.deleteSlide = deleteSlide;

    /**
     * Gets a slide from the local database.
     *
     * @param slideId ID of the slide to get
     *
     * @param slideCallback function to be called after getting is complete,
     * paramater is the slide object
     */
    function getSlideLocal (slideId, slideCallback) {
        database.getFromSlides(slideId, function (e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                slideCallback (undefined);
            } else if (result.data == nonExistantValue) {
                slideCallback (nonExistantValue);
            } else {
                var bytes = ByteBuffer.fromBase64(result.data);
                if (!isUndefined(slideCallback)) {
                    slideCallback(CourseSketch.PROTOBUF_UTIL.getLectureSlideClass().decode(bytes));
                }
            }// end else
        });
    }
    parent.getSlideLocal = getSlideLocal;

    /**
     * Gets a slide from the local and server databases.
     *
     * @param slideId ID of the slide to get
     * @param slideCallback function to be called after getting is complete,
     * paramater is the slide object
     */
    function getLectureSlide (slideId, localCallback, serverCallback) {
        getLectureSlides([ slideId ], isUndefined(localCallback) ? undefined : function (slideList) {
            localCallback (slideList[0]);
        }, isUndefined(serverCallback) ? undefined : function(slideList) {
            serverCallback(slideList[0]);
        });
    };
    parent.getLectureSlide = getLectureSlide;

    /**
     * Gets a list of slides from the local and server databases.
     *
     * @param slideIds IDs of the slides to get
     * @param slideCallback function to be called after getting is complete,
     * paramater is a list of slide objects
     */
    function getLectureSlides (slideIds, localCallback, serverCallback) {
        if (isUndefined (slideIds) || slideIds == null || slideIds.length == 0) {
            if (!isUndefined(localCallback)) {
                 localCallback (nonExistantValue);
            } else {
                serverCallback (nonExistantValue);
            }
        }
        var barrier = slideIds.length;
        var slidesFound = [];
        var slideIdsNotFound = [];
        for (var i = 0; i < slideIds.length; i++) {
            var currentSlideId = slideIds[i];
            function forLoopBlock (slideId) {
                getSlideLocal (slideId, function (slide) {
                    if (!isUndefined (slide)) {
                        slidesFound.push(slide);
                    } else {
                        slideIdsNotFound.push(slideId);
                    }
                    barrier -= 1;
                    if (barrier == 0) {
                        if (slideIdsNotFound.length >= 1) {
                            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, function (evt, item) {
                                var school = CourseSketch.PROTOBUF_UTIL.getSrlLectureDataHolderClass().decode(item.data);
                                var slide = school.slides[0];
                                if (isUndefined (slide)) {
                                    if (!isUndefined(serverCallback)) {
                                        serverCallback(slide);
                                    }
                                    lectureCallback (slide);
                                    advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
                                    return;
                                }
                                for (var i = 0; i < school.slides.length; i++) {
                                    localScope.setSlide(school.slides[i]);
                                    slidesFound.push(school.slides[i]);
                                }
                                if (!isUndefined(serverCallback)) {
                                    serverCallback(slidesFound);
                                }
                                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
                            });
                            sendData.sendDataRequest (CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, slideIdsNotFound);
                        }
                        if (slidesFound.length > 0 && !isUndefined(localCallback)) {
                            localCallback (slidesFound);
                        }
                    }
                });// end of getSlideLocal
            }
            forLoopBlock (currentSlideId);
        }
    };
    parent.getLectureSlides = getLectureSlides;
}
