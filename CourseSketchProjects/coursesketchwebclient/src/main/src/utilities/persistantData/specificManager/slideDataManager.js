function SlideDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
    var dataListener = advanceDataListener;
    var database = parentDatabase;
    var Request = request
    var localScope = parent;
    var ByteBuffer = buffer;

    /**
     * Sets a slide in the local database
     *
     * @param {SrlSlide} slide is a slide object
     *
     * @param {Function} slideCallback function to be called after the slide setting is done
     */
    function setSlide(slide, slideCallback) {
        database.putInSlides(slide.id, slide.toBase64(), function(e, request) {
            if (!isUndefined(slideCallback)) {
                slideCallback(e, request);
            }
        });
    }
    parent.setSlide = setSlide;

    /**
     * Sets a slide in the server database
     *
     * @param {SrlSlide} slide is a slide object
     * @param {Function} slideCallback function to be called after the slide setting is done
     */
    function insertSlideServer(slide, slideCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, function(evt, item) {
            console.log("RESPONSE PLEASE!!!!");
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
            var resultArray = item.getReturnText().split(":");
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            getSlideLocal(oldId, function(slide2) {
                deleteSlide(oldId);
                if (!isUndefined(slide2) && !(slide2 instanceof DatabaseException)) {
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
     * @param {SrlLecture} slide
     *                lecture object to set
     * @param {Function} localCallback
     *                function to be called local lecture setting is done
     * @param {Function} serverCallback
     *                function to be called after server lecture setting is done
     */
    function updateSlide(slide, localCallback, serverCallback) {
        setSlide(slide, localCallback);
        advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.LECTURESLIDE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
            serverCallback(item);
        });
        sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, slide.toArrayBuffer());
    }
    parent.updateSlide = updateSlide;

    /**
     * Adds a new slide to both local and server databases. Also updates the
     * corresponding slide given by the lecture's courseId.
     *
     * @param {SrlSlide} slide slide object to insert
     * @param {Function} localCallback function to be called after local insert is done
     * @param {Function} serverCallback function to be called after server insert is done
     */
    function insertSlide(slide, localCallback, serverCallback) {
        setSlide(slide, function(e, request) {
            console.log("inserted locally :" + slide.id)
            if (!isUndefined(localCallback)) {
                try {
                    localCallback(e, request);
                } catch (exception) {
                    // ignore callback problems we want to succeed
                }
            }
            insertSlideServer(slide, function(slideUpdated) {
                console.log("SLIDE IS UPADTED FROM SERVER! " + slideUpdated.id);
                parent.getCourseLecture(slide.lectureId, function(lecture) {
                    var idsInLectureList = lecture.idList;
                    var idInLecture = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
                    idInLecture.id = slideUpdated.id;
                    idInLecture.isSlide = true;
                    console.log("SLIDE IS STUFF! " + idInLecture);
                    idsInLectureList.push(idInLecture);
                    parent.setLecture(lecture, function() {
                        if (!isUndefined(serverCallback)) {
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
    parent.insertSlide = insertSlide;

    /**
     * Deletes a slide from local database.
     *
     * @param {String} slideId ID of the lecture to delete
     * @param {Function} slideCallback function to be called after the deletion is done
     */
    function deleteSlide (slideId, slideCallback) {
        database.deleteFromSlides(slideId, function(e, request) {
            if (!isUndefined(slideCallback)) {
                slideCallback(e, request);
            }
        });
    }
    parent.deleteSlide = deleteSlide;

    /**
     * Gets a slide from the local database.
     *
     * @param {String} slideId ID of the slide to get
     *
     * @param {Function} slideCallback function to be called after getting is complete,
     *                paramater is the slide object
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
     * @param {String} slideId ID of the slide to get.
     * @param {Function} localCallback function to be called after getting is complete,
     *                paramater is the slide object.
     * @param {Function} serverCallback function to be called after looking in the server for the slide.
     */
    function getLectureSlide(slideId, localCallback, serverCallback) {
        getLectureSlides([ slideId ], isUndefined(localCallback) ? undefined : function(slideList) {
            localCallback (slideList[0]);
        }, isUndefined(serverCallback) ? undefined : function(slideList) {
            serverCallback(slideList[0]);
        });
    };
    parent.getLectureSlide = getLectureSlide;

    /**
     * Gets a list of slides from the local and server databases.
     *
     * @param {String} slideIds IDs of the slides to get
     * @param {Function} localCallback function to be called after getting is complete,
     *              paramater is a list of slide objects.
     * @param {Function} serverCallback function to be called after looking in the server for the slide.
     */
    function getLectureSlides (slideIds, localCallback, serverCallback) {
        if (isUndefined (slideIds) || slideIds == null || slideIds.length == 0) {
            if (!isUndefined(localCallback)) {
                localCallback(new DatabaseException("Result is undefined!", "Grabbing slide from server: " + slideIds));
            } else {
                serverCallback(new DatabaseException("Nothing is in the server database!", "Grabbing slide from server: " + slideIds));
            }
        }
        var barrier = slideIds.length;
        var slidesFound = [];
        var slideIdsNotFound = [];
        for (var i = 0; i < slideIds.length; i++) {
            var currentSlideId = slideIds[i];
            (function(slideId) {
                getSlideLocal(slideId, function(slide) {
                    if (!isUndefined(slide) && !(slide instanceof DatabaseException)) {
                        slidesFound.push(slide);
                    } else {
                        slideIdsNotFound.push(slideId);
                    }
                    barrier -= 1;
                    if (barrier == 0) {
                        if (slideIdsNotFound.length >= 1) {
                            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST,
                                    CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, function (evt, item) {
                                var school = CourseSketch.PROTOBUF_UTIL.getSrlLectureDataHolderClass().decode(item.data);
                                var slide = school.slides[0];
                                if (isUndefined(slide) || slide instanceof DatabaseException) {
                                    if (!isUndefined(serverCallback)) {
                                        serverCallback(slide);
                                    }
                                    advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                                            CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
                                    return;
                                }  // end if
                                for (var i = 0; i < school.slides.length; i++) {
                                    localScope.setSlide(school.slides[i]);
                                    slidesFound.push(school.slides[i]);
                                } // end for
                                if (!isUndefined(serverCallback)) {
                                    serverCallback(slidesFound);
                                } // end if serverCallback
                                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                                        CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE);
                            }); // setListener
                            sendData.sendDataRequest (CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURESLIDE, slideIdsNotFound);
                        } // end if lectureIdsNotFound
                        if (slidesFound.length > 0 && !isUndefined(localCallback)) {
                            localCallback (slidesFound);
                        } // end if
                    } // end if barrier
                });// end of getSlideLocal
            })(currentSlideId);
        } // end for slideIds
    };
    parent.getLectureSlides = getLectureSlides;
}
