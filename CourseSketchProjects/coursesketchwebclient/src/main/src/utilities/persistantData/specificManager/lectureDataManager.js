function LectureDataManager(parent, advanceDataListener, parentDatabase,
        sendData, request, buffer) {
    var dataListener = advanceDataListener;
    var database = parentDatabase;
    var Request = request
    var localScope = parent;
    var ByteBuffer = buffer;

    /**
     * Sets a lecture in local database.
     *
     * @param lecture
     *                lecture object to set
     * @param lectureCallback
     *                function to be called after the lecture setting is done
     */
    function setLecture(lecture, lectureCallback) {
        if (isUndefined(lecture)){
            lectureCallback(new DatabaseException("can't set undefined lecture", "Settting lecture locally: "));
            return;
        }
        if (lecture instanceof DatabaseException) {
            lectureCallback(lecture);
            return;
        }
        database.putInLectures(lecture.id, lecture.toBase64(), function(e, request) {
            if (!isUndefined(lectureCallback)) {
                lectureCallback(e, request);
            }
        });
    }
    parent.setLecture = setLecture;

    /**
     * Sets a lecture in server database.
     *
     * @param lecture
     *                lecture object to set
     * @param lectureCallback
     *                function to be called after lecture setting is done
     */
    function insertLectureServer(lecture, lectureCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
            var resultArray = item.getReturnText().split(":");
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getLectureLocal(oldId, function(lecture2) {
                deleteLecture(oldId);
                if (!isUndefined(lecture2) && !(lecture2 instanceof DatabaseException)) {
                    lecture2.id = newId;
                    setLecture(lecture2, function() {
                        lectureCallback(lecture2);
                    });
                } else {
                    lecture.id = newId;
                    setLecture(lecture, function(e, request) {
                        lectureCallback(lecture);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lecture.toArrayBuffer());
    }

    /**
     * Sets a lecture in both local and server databases.
     *
     * @param lecture
     *                lecture object to set
     * @param localCallback
     *                function to be called after local lecture setting is done
     * @param serverCallback
     *                function to be called after server lecture setting is done
     */
    function updateLecture(lecture, localCallback, serverCallback) {
        setLecture(lecture, function() {
            if (!isUndefined(localCallback)) {
                localCallback();
            }
            advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
                // we do not need to make server changes we
                // just need to make sure it was successful.
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
            sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lecture.toArrayBuffer());
        });
    }
    parent.updateLecture = updateLecture;

    /**
     * Adds a new lecture to both local and server databases. Also updates the
     * corresponding course given by the lecture's courseId.
     *
     * @param lecture
     *                lecture object to insert
     * @param localCallback
     *                function to be called after local insert is done
     * @param serverCallback
     *                function to be called after server insert is done
     */
    function insertLecture(lecture, localCallback, serverCallback) {
        setLecture(lecture, function(e, request) {
            console.log("inserted locally :" + lecture.id)
            if (!isUndefined(localCallback)) {
                localCallback(e, request);
            }
            insertLectureServer(lecture, function(lectureUpdated) {
                parent.getCourse(lecture.courseId, function(course) {
                    var lectureList = course.lectureList;
                    lectureList.push(lectureUpdated.id);
                    course.lectureList = lectureList;
                    parent.setCourse(course, function() {
                        if (!isUndefined(serverCallback)) {
                            serverCallback(course);
                        }
                    });
                    // Course is set with its new lecture
                });
                // Finished with the course
            });
            // Finished with setting lecture
        });
        // Finished with local lecture
    }
    parent.insertLecture = insertLecture;

    /**
     * Deletes a lecture from local database.
     *
     * @param lectureId
     *                ID of the lecture to delete
     * @param lectureCallback
     *                function to be called after the deletion is done
     */
    function deleteLecture(lectureId, lectureCallback) {
        database.deleteFromLectures(lectureId, function(e, request) {
            if (!isUndefined(lectureCallback)) {
                lectureCallback(e, request);
            }
        });
    }
    parent.deleteLecture = deleteLecture;

    /**
     * Gets a lecture from the local database.
     *
     * @param lectureId
     *                ID of the lecture to get
     * @param lectureCallback
     *                function to be called after getting is complete, parameter
     *                is the lecture object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getLectureLocal(lectureId, lectureCallback) {
        if (isUndefined(lectureId) || lectureId == null) {
            lectureCallback(new DatabaseException("The given id is not assigned", "getting Lecture: " + lectureId));
        }
        database.getFromLectures(lectureId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                lectureCallback(new DatabaseException("Result is undefined!", "Grabbing lecture from server: " + lectureId));
            } else if (result.data == nonExistantValue) {
                lectureCallback(new DatabaseException("Nothing is in the server database!", "Grabbing lecture from server: " + lectureId));
            } else {
                var bytes = ByteBuffer.fromBase64(result.data);
                if (!isUndefined(lectureCallback)) {
                    lectureCallback(CourseSketch.PROTOBUF_UTIL
                            .getLectureClass().decode(bytes));
                } // endif
            } // end else
        }); // end getFromLectures
    }

    parent.getLectureLocal = getLectureLocal;

    /**
     * Gets a lecture from the local and server databases.
     *
     * @param lectureId
     *                ID of the lecture to get
     * @param lectureCallback
     *                function to be called after getting is complete, paramater
     *                is the lecture object
     */
    function getCourseLecture(lectureId, localCallback, serverCallback) {
        getCourseLectures([ lectureId ], isUndefined(localCallback) ? undefined : function(lectureList) {
            localCallback(lectureList[0]);
        }, isUndefined(serverCallback) ? undefined : function(lectureList) {
            serverCallback(lectureList[0]);
        });
    }
    parent.getCourseLecture = getCourseLecture;

    /**
     * Returns a list of all of the lectures from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull lectures from the server!
     *
     * @param lectureIds
     *            list of IDs of the lectures to get
     * @param localCallback
     *            {Function} called when lectures are grabbed from the local
     *            database only. This list may not be complete. This may also
     *            not get called if there are no local lectures.
     * @param serverCallback
     *            {Function} called when the complete list of lectures are
     *            grabbed.
     */
    function getCourseLectures(lectureIds, localCallback, serverCallback) {
        if (isUndefined(lectureIds) || lectureIds == null || lectureIds.length == 0) {
            if (!isUndefined(localCallback)) {
                localCallback(new DatabaseException("Result is undefined!", "Grabbing lecture from server: " + lectureIds));
            } else {
                serverCallback(new DatabaseException("Nothing is in the server database!", "Grabbing lecture from server: " + lectureIds));
            }
        }
        var barrier = lectureIds.length;
        var lecturesFound = [];
        var lectureIdsNotFound = [];
        for (var i = 0; i < lectureIds.length; i++) {
            var currentLectureId = lectureIds[i];
            (function (lectureId) {
                getLectureLocal(lectureId, function(lecture) {
                    if (!isUndefined(lecture) && !(lecture instanceof DatabaseException)) {
                        lecturesFound.push(lecture);
                    } else {
                        lectureIdsNotFound.push(lectureId);
                    }
                    barrier -= 1;
                    if (barrier == 0) {
                        if (lectureIdsNotFound.length >= 1) {
                            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST,
                                    CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function(evt, item) {
                                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
                                // after listener is removed
                                if (isUndefined(item.data) || item.data == null) {
                                    serverCallback(new DatabaseException("The data sent back from the server does not exist."));
                                    return;
                                }
                                var school = CourseSketch.PROTOBUF_UTIL.getSrlLectureDataHolderClass().decode(item.data);
                                var lecture = school.lectures[0];
                                if (isUndefined(lecture) || lecture instanceof DatabaseException) {
                                    var result = lecture;
                                    if (isUndefined(result)) {
                                        result = new DatabaseException("Nothing is in the server database!",
                                            "Grabbing lecture from server: " + lectureIds);
                                    }
                                    if (!isUndefined(serverCallback)) {
                                        serverCallback(result);
                                    }
                                    return;
                                } // end error check
                                for (var i = 0; i < school.lectures.length; i++) {
                                    localScope.setLecture(school.lectures[i]);
                                    lecturesFound.push(school.lectures[i]);
                                } // end for
                                if (!isUndefined(serverCallback)){
                                    serverCallback(lecturesFound);
                                } // end if serverCallback

                            }); // setListener
                            sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lectureIdsNotFound);
                        } // end if lectureIdsNotFound
                        if (lecturesFound.length > 0 && !isUndefined(localCallback)) {
                            localCallback(lecturesFound);
                        } // end if
                    } // end if barrier
                });// end getLectureLocal
            })(currentLectureId); // end of auto function
        } // end for lectureIds
    }
    parent.getCourseLectures = getCourseLectures;
}
