/* eslint-disable require-jsdoc */
/**
 * A manager for slides that talks with the remote server.
 *
 * @param {SchoolDataManager} parent - The database that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener - A listener for the database.
 * @param {ProtoDatabase} database - The local database
 * @param {ByteBuffer} ByteBuffer - Used in the case of longs for javascript.
 * @constructor
 */
function SlideDataManager(parent, advanceDataListener, database, ByteBuffer) {
    var parentScope = parent;

    /**
     * Sets a slide in the local database
     *
     * @param {SrlSlide} slide - is a slide object
     *
     * @param {Function} slideCallback - function to be called after the slide setting is done
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
     * @param {SrlSlide} slide - is a slide object
     * @param {Function} slideCallback - function to be called after the slide setting is done
     */
    function insertSlideServer(slide, slideCallback) {
        advanceDataListener.sendDataInsert(CourseSketch.prutil.ItemQuery.LECTURESLIDE, slide.toArrayBuffer(), function(evt, item) {
            console.log('RESPONSE PLEASE!!!!');
            advanceDataListener.removeListener(CourseSketch.prutil.Request.MessageType.DATA_INSERT, CourseSketch.prutil.ItemQuery.LECTURESLIDE);
            var resultArray = item.getReturnText().split(':');
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
        advanceDataListener.sendDataUpdate(CourseSketch.prutil.ItemQuery.LECTURESLIDE, slide.toArrayBuffer(), function(evt, item) {
            advanceDataListener.removeListener(CourseSketch.prutil.Request.MessageType.DATA_UPDATE, CourseSketch.prutil.ItemQuery.LECTURESLIDE);
            serverCallback(item);
        });
    }

    parent.updateSlide = updateSlide;

    /**
     * Adds a new slide to both local and server databases. Also updates the
     * corresponding slide given by the lecture's courseId.
     *
     * @param {SrlSlide} slide - slide object to insert
     * @param {Function} localCallback - function to be called after local insert is done
     * @param {Function} serverCallback - function to be called after server insert is done
     */
    function insertSlide(slide, localCallback, serverCallback) {
        setSlide(slide, function(e, request) {
            console.log('inserted locally :' + slide.id);
            if (!isUndefined(localCallback)) {
                try {
                    localCallback(e, request);
                } catch (exception) {
                    // ignore callback problems we want to succeed
                }
            }
            insertSlideServer(slide, function(slideUpdated) {
                console.log('SLIDE IS UPADTED FROM SERVER! ' + slideUpdated.id);
                parent.getCourseLecture(slide.lectureId, function(lecture) {
                    var idsInLectureList = lecture.idList;
                    var idInLecture = CourseSketch.prutil.IdsInLecture();
                    idInLecture.id = slideUpdated.id;
                    idInLecture.isSlide = true;
                    console.log('SLIDE IS STUFF! ' + idInLecture);
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
     * @param {String} slideId - ID of the lecture to delete
     * @param {Function} slideCallback - function to be called after the deletion is done
     */
    function deleteSlide(slideId, slideCallback) {
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
     * @param {String} slideId - ID of the slide to get
     *
     * @param {Function} slideCallback - function to be called after getting is complete,
     *                paramater is the slide object
     */
    function getSlideLocal(slideId, slideCallback) {
        database.getFromSlides(slideId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                slideCallback(undefined);
            } else {
                var bytes = ByteBuffer.fromBase64(result.data);
                if (!isUndefined(slideCallback)) {
                    slideCallback(CourseSketch.prutil.getLectureSlideClass().decode(bytes));
                }
            }// end else
        });
    }

    parent.getSlideLocal = getSlideLocal;

    /**
     * Gets a slide from the local and server databases.
     *
     * @param {String} slideId - ID of the slide to get.
     * @param {Function} localCallback - function to be called after getting is complete,
     *                paramater is the slide object.
     * @param {Function} serverCallback - function to be called after looking in the server for the slide.
     */
    function getLectureSlide(slideId, localCallback, serverCallback) {
        getLectureSlides([ slideId ], isUndefined(localCallback) ? undefined : function(slideList) {
            localCallback(slideList[0]);
        }, isUndefined(serverCallback) ? undefined : function(slideList) {
            serverCallback(slideList[0]);
        });
    }

    parent.getLectureSlide = getLectureSlide;

    /**
     * Gets a list of slides from the local and server databases.
     *
     * @param {String} slideIds - IDs of the slides to get
     * @param {Function} localCallback - function to be called after getting is complete,
     *              paramater is a list of slide objects.
     * @param {Function} serverCallback - function to be called after looking in the server for the slide.
     */
    function getLectureSlides(slideIds, localCallback, serverCallback) {
        if (isUndefined(slideIds) || slideIds === null || slideIds.length === 0) {
            if (!isUndefined(localCallback)) {
                localCallback(new DatabaseException('Result is undefined!', 'Grabbing slide from server: ' + slideIds));
            } else {
                serverCallback(new DatabaseException('Nothing is in the server database!', 'Grabbing slide from server: ' + slideIds));
            }
        }
        var barrier = slideIds.length;
        var slidesFound = [];
        var slideIdsNotFound = [];
        for (var i = 0; i < slideIds.length; i++) {
            var currentSlideId = slideIds[i];
            (function(slideId) {
                getSlideLocal(slideId, function(localSlide) {
                    if (!isUndefined(localSlide) && !(localSlide instanceof DatabaseException)) {
                        slidesFound.push(localSlide);
                    } else {
                        slideIdsNotFound.push(slideId);
                    }
                    barrier -= 1;
                    if (barrier === 0) {
                        if (slideIdsNotFound.length >= 1) {
                            var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.LECTURESLIDE, slideIdsNotFound);
                            advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
                                var school = CourseSketch.prutil.getSrlLectureDataHolderClass().decode(item.data);
                                var slide = school.slides[0];
                                if (isUndefined(slide) || slide instanceof DatabaseException) {
                                    if (!isUndefined(serverCallback)) {
                                        serverCallback(slide);
                                    }
                                    advanceDataListener.removeListener(CourseSketch.prutil.Request.MessageType.DATA_REQUEST,
                                        CourseSketch.prutil.ItemQuery.LECTURESLIDE);
                                    return;
                                }  // end if
                                for (var slideIndex = 0; slideIndex < school.slides.length; slideIndex++) {
                                    parentScope.setSlide(school.slides[slideIndex]);
                                    slidesFound.push(school.slides[slideIndex]);
                                } // end for
                                if (!isUndefined(serverCallback)) {
                                    serverCallback(slidesFound);
                                } // end if serverCallback
                                advanceDataListener.removeListener(CourseSketch.prutil.Request.MessageType.DATA_REQUEST,
                                    CourseSketch.prutil.ItemQuery.LECTURESLIDE);
                            }); // setListener
                        } // end if lectureIdsNotFound
                        if (slidesFound.length > 0 && !isUndefined(localCallback)) {
                            localCallback(slidesFound);
                        } // end if
                    } // end if barrier
                });// end of getSlideLocal
            })(currentSlideId);
        } // end for slideIds
    }

    parent.getLectureSlides = getLectureSlides;
}
