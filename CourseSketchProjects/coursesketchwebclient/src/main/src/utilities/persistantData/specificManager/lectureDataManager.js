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
    function setLectureLocal(lecture, lectureCallback) {
        database.putInLectures(lecture.id, lecture.toBase64(), function(e, request) {
            if (!isUndefined(lectureCallback)) {
                lectureCallback(e, request);
            }
        });
    }
    parent.setLectureLocal = setLectureLocal;

    /**
     * Sets a lecture in server database.
     * 
     * @param lecture
     *                lecture object to set
     * @param lectureCallback
     *                function to be called after lecture setting is done
     */
    function setLectureServer(lecture, lectureCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
            var resultArray = item.getReturnText().split(":");
            var oldId = resultArray[1];
            var newId = resultArray[0];
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getLectureLocal(oldId, function(lecture2) {
                deleteLecture(oldId);
                if (!isUndefined(lecture2)) {
                    lecture2.id = newId;
                    setLectureLocal(lecture2, function() {
                        lectureCallback(lecture2);
                    });
                } else {
                    lecture.id = newId;
                    setLectureLocal(lecture, function() {
                        lectureCallback(lecture);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lecture.toArrayBuffer());
    }

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
        setLectureLocal(lecture, function(e, request) {
            if (!isUndefined(localCallback)) {
                localCallback(e, request);
            }
            setLectureServer(lecture, function(lecture2) {
                parent.getCourse(lecture.courseId, function(course) {
                    var lectureList = course.lectureList;
                    lectureList.push(lecture2.id);
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
     *                function to be called after getting is complete, paramater
     *                is the lecture object
     */
    function getLectureLocal(lectureId, lectureCallback) {
        database.getFromLectures(lectureId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                lectureCallback(undefined);
            } else if (result.data == nonExistantValue) {
                lectureCallback(nonExistantValue);
            } else {
                var bytes = ByteBuffer.fromBase64(result.data);
                if (!isUndefined(lectureCallback)) {
                    lectureCallback(CourseSketch.PROTOBUF_UTIL
                            .getLectureClass().decode(bytes));
                }// endif
            }// end else
        });// end getFromLectures 
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
    ;
    parent.getCourseLecture = getCourseLecture;

    /**
     * Gets a list of lectures from the local and server databases.
     * 
     * @param lectureIds
     *                IDs of the lectures to get
     * @param lectureCallback
     *                function to be called after getting is complete, paramater
     *                is a list of lecture objects
     */
    function getCourseLectures(lectureIds, localCallback, serverCallback) {
        if (isUndefined(lectureIds) || lectureIds == null || lectureIds.length == 0) {
            if(!isUndefined(localCallback)){
                localCallback(nonExistantValue);
            }else{
                serverCallback(nonExistantValue);
            }
        }
        var barrier = lectureIds.length;
        var lecturesFound = [];
        var lectureIdsNotFound = [];
        for (var i = 0; i < lectureIds.length; i++) {
            var currentLectureId = lectureIds[i];
            (function (lectureId) {
                getLectureLocal(lectureId, function(lecture) {
                    if (!isUndefined(lecture)) {
                        lecturesFound.push(lecture);
                    } else {
                        lectureIdsNotFound.push(lectureId);
                    }
                    barrier -= 1;
                    if (barrier == 0) {
                        if (lectureIdsNotFound.length >= 1) {
                            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function(evt, item) {
                                var school = CourseSketch.PROTOBUF_UTIL.getSrlLectureDataHolderClass().decode(item.data);
                                var lecture = school.lectures[0];
                                if (isUndefined(lecture)) {
                                    if (!isUndefined(serverCallback)){
                                        serverCallback(lecture);
                                    }// end if serverCallback
                                    advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
                                    return;
                                }// end if
                                for (var i = 0; i < school.lectures.length; i++) {
                                    localScope.setLectureLocal(school.lectures[i]);
                                    lecturesFound.push(school.lectures[i]);
                                }// end for
                                if (!isUndefined(serverCallback)){
                                    serverCallback(lecturesFound);
                                }// end if serverCallback
                                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
                            });// setListener
                            sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lectureIdsNotFound);
                        }// end if lectureIdsNotFound
                        if (lecturesFound.length > 0 && !isUndefined(localCallback)) {
                            localCallback(lecturesFound);
                        }// end if
                    }// end if barrier
                });// end getLectureLocal
            })(currentLectureId);// end of auto function
        }// end for lectureIds
    }
    ;
    parent.getCourseLectures = getCourseLectures;
}