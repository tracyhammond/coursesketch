/**
 * Manages the data for the tutorial.
 *
 * Talks with the remote server to simplify getting data from the database
 * @param parent
 * @param advanceDataListener
 * @param parentDatabase
 * @param sendData
 * @param request
 * @param buffer
 * @constructor
 */
function TutorialDataManager(parent, dataListener, database, sendData, Request, ByteBuffer) {
    var localScope = parent;

    /**
     * Sets the tutorial locally into the local database.
     */
    function setTutorial(tutorial, tutorialCallback) {
        database.putInTutorials(tutorial.id, tutorial.toBase64(), function(e, request) {
            if (tutorialCallback) {
                tutorialCallback(e, request);
            }
        });
    }

    parent.setTutorial = setTutorial;

    /**
     * Deletes a tutorial from local database.
     * This does not delete the id pointing to this item in the respective course.
     *
     * @param {String} tutorialId
     *                ID of the tutorial to delete
     * @param {Function} tutorialCallback
     *                function to be called after the deletion is done
     */
    function deleteTutorial(tutorialId, tutorialCallback) {
        database.deleteFromTutorials(tutorialId, function(e, request) {
            if (tutorialCallback) {
                tutorialCallback(e, request);
            }
        });
    }

    parent.deleteTutorial = deleteTutorial;

    /**
     * Sets a tutorial in server database.
     *
     * @param {SrlTutorial} tutorial
     *                tutorial object to set
     * @param {Function} tutorialCallback
     *                function to be called after tutorial setting is done
     */
    function insertTutorialServer(tutorial, tutorialCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);
            var resultArray = item.getReturnText().split(':');
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getTutorialLocal(oldId, function(tutorial2) {
                deleteTutorial(oldId);
                if (!isUndefined(tutorial2) && !(tutorial2 instanceof DatabaseException)) {
                    tutorial2.id = newId;
                    setTutorial(tutorial2, function() {
                        tutorialCallback(tutorial2);
                    });
                } else {
                    tutorial.id = newId;
                    setTutorial(tutorial, function(e, request) {
                        tutorialCallback(tutorial);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, tutorial.toArrayBuffer());
    }


    /**
     * Adds a new tutorial to both local and server databases. Also updates the
     * corresponding course given by the tutorial's courseId.
     *
     * @param {String} tutorial
     *                tutorial object to insert
     * @param {Function} localCallback
     *                function to be called after local insert is done
     * @param {Function} serverCallback
     *                function to be called after server insert is done
     */
    function insertTutorial(tutorial, localCallback, serverCallback) {
        if (isUndefined(tutorial.id) || tutorial.id === null) {
            tutorial.id = generateUUID();
        }
        setTutorial(tutorial, function(e, request) {
            console.log('inserted locally :' + tutorial.id);
            if (!isUndefined(localCallback)) {
                localCallback(tutorial);
            }
            insertTutorialServer(tutorial, function(tutorialUpdated) {
                parent.getCourse(tutorial.courseId, function(course) {
                    var tutorialList = course.tutorialList;

                    // remove old Id (if it exists)
                    if (tutorialList.indexOf(tutorial.id) >= 0) {
                        removeObjectFromArray(tutorialList, tutorial.id);
                    }
                    tutorialList.push(tutorialUpdated.id);
                    parent.setCourse(course, function() {
                        if (!isUndefined(serverCallback)) {
                            serverCallback(tutorialUpdated);
                        }
                    });
                    // Course is set with its new tutorial
                });
                // Finished with the course
            });
            // Finished with setting tutorial
        });
        // Finished with local tutorial
    }

    parent.insertTutorial = insertTutorial;

    /**
     * Sets a tutorial in both local and server databases.
     * Updates an existing tutorial into the database. This tutorial must already
     * exist.
     *
     * @param {SrlTutorial} tutorial
     *                tutorial object to set
     * @param {Function} localCallback
     *                function to be called after local tutorial setting is done
     * @param {Function} serverCallback
     *                function to be called after server tutorial setting is done
     */
    function updateTutorial(tutorial, localCallback, serverCallback) {
        setTutorial(tutorial, function() {
            if (!isUndefined(localCallback)) {
                localCallback();
            }
            advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);
                // we do not need to make server changes we just need to make sure it was successful.
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
            sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, tutorial.toArrayBuffer());
        });
    }

    parent.updateTutorial = updateTutorial;

    /**
     * Gets an Tutorial from the local database.
     *
     * @param {String} tutorialId
     *                ID of the tutorial to get
     * @param {Function} tutorialCallback
     *                function to be called after getting is complete, parameter
     *                is the tutorial object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getTutorialLocal(tutorialId, tutorialCallback) {
        if (isUndefined(tutorialId) || tutorialId === null) {
            tutorialCallback(new DatabaseException('The given id is not assigned', 'getting Tutorial: ' + tutorialId));
        }
        database.getFromTutorials(tutorialId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                tutorialCallback(new DatabaseException('The result is undefined', 'getting Tutorial: ' + tutorialId));
            } else {
                // gets the data from the database and calls the callback
                try {
                    var bytes = ByteBuffer.fromBase64(result.data);
                    stateCallback(CourseSketch.PROTOBUF_UTIL.getTutorialClass().decode(bytes), tutorialCallback);
                } catch (exception) {
                    console.error(exception);
                    tutorialCallback(new DatabaseException('The result is undefined', 'getting Tutorial: ' + tutorialId));
                }
            }
        });
    }

    parent.getTutorialLocal = getTutorialLocal;

    /**
     * Returns a list of all of the tutorials from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull tutorials from the server!
     *
     * @param {List<String>} tutorialIdList
     *            list of IDs of the tutorials to get
     * @param {Function} tutorialCallbackPartial
     *            {Function} called when tutorials are grabbed from the local
     *            database only. This list may not be complete. This may also
     *            not get called if there are no local tutorials.
     * @param {Function} tutorialCallbackComplete
     *            {Function} called when the complete list of tutorials are
     *            grabbed.
     */
    function getTutorials(tutorialIdList, tutorialCallbackPartial, tutorialCallbackComplete) {
        /*
         * So what happens here might be a bit confusing to some new people so
         * let me explain it. #1 there is a loop that goes through every item in
         * the tutorialIdList (which is a list of tutorial ids)
         *
         * #2 there is a function declaration inside the loop the reason for
         * this is so that the tutorialId is not overwritten when the callback
         * is called.
         *
         * #3 we call getTutorialLocal which then calls a callback about if it
         * got an tutorial or not if it didn't we add the id to a list of Id
         * we need to get from the server
         *
         * #4 after the entire list has been gone through (which terminates in
         * the callback with barrier = 0) if there are any that need to be
         * pulled from the server then that happens
         *
         * #5 after talking to the server we get a response with a list of
         * tutorials, these are combined with the local tutorials then the
         * original callback is called.
         *
         * #6 the function pattern terminates.
         */

        // standard preventative checking
        if (isUndefined(tutorialIdList) || tutorialIdList === null || tutorialIdList.length === 0) {
            tutorialCallbackPartial(new DatabaseException('The given id is not assigned', 'getting Tutorial: ' + tutorialIdList));
            if (tutorialCallbackComplete) {
                tutorialCallbackComplete(new DatabaseException('The given id is not assigned', 'getting Tutorial: ' + tutorialIdList));
            }
        }

        var barrier = tutorialIdList.length;
        var tutorialList = [];
        var leftOverId = [];

        // create local tutorial list so everything appears really fast!
        for (var i = 0; i < tutorialIdList.length; i++) {
            var tutorialIdLoop = tutorialIdList[i];
            // the purpose of this function is purely to scope the tutorialId
            // so that it changes
            (function(tutorialId) {
                getTutorialLocal(tutorialId, function(tutorial) {
                    if (!isUndefined(tutorial) && !(tutorial instanceof DatabaseException)) {
                        tutorialList.push(tutorial);
                    } else {
                        leftOverId.push(tutorialId);
                    }
                    barrier -= 1;
                    if (barrier <= 0) {
                        // after the entire list has been gone through pull the
                        // leftovers from the server
                        if (leftOverId.length >= 1) {
                            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT,
                                function(evt, item) {
                                    advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                                        CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);

                                    // after listener is removed
                                    if (isUndefined(item.data) || item.data === null) {
                                        tutorialCallbackComplete(new DatabaseException('The data sent back from the server does not exist.'));
                                        return;
                                    }
                                    var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
                                    var tutorial = school.tutorials[0];
                                    if (isUndefined(tutorial) || tutorial instanceof DatabaseException) {
                                        var result = tutorial;
                                        if (isUndefined(result)) {
                                            result = new DatabaseException('Nothing is in the server database!',
                                                'Grabbing tutorial from server: ' + tutorialIdList);
                                        }
                                        if (!isUndefined(tutorialCallbackComplete)) {
                                            tutorialCallbackComplete(result);
                                        }
                                        return;
                                    }
                                    for (var i = 0; i < school.tutorials.length; i++) {
                                        localScope.setTutorial(school.tutorials[i]);
                                        tutorialList.push(school.tutorials[i]);
                                    }
                                    stateCallbackList(tutorialList, tutorialCallbackComplete);
                                    tutorialIdList = null;
                                });
                            // creates a request that is then sent to the server
                            sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, leftOverId);
                        } else {
                            stateCallbackList(tutorialList, tutorialCallbackComplete);
                        }

                        // this calls actually before the response from the
                        // server is received!
                        if (tutorialList.length > 0) {
                            stateCallbackList(tutorialList, tutorialCallbackPartial);
                        }
                    } // end of if(barrier === 0)
                }); // end of getting local tutorial
            })(tutorialIdLoop); // end of loopContainer
        } // end of loop
    }

    parent.getTutorials = getTutorials;

    /**
     * Returns a tutorial with the given tutorialId will ask the server if it
     * does not exist locally
     *
     * @param {String} tutorialId
     *            The id of the tutorial we want to find.
     * @param {Function} tutorialCallback
     *            The method to call when the tutorial has been found. (this
     *            is asynchronous)
     */
    function getTutorial(tutorialId, tutorialCallback) {
        if (isUndefined(tutorialCallback)) {
            throw new DatabaseException('Calling get Tutorial with an undefined callback');
        }
        var called = false;

        function callOnce(tutorialList) {
            if (!called) {
                called = true;
                tutorialCallback(tutorialList[0]);
            }
        }

        getTutorials([ tutorialId ], callOnce, callOnce);
    }

    parent.getTutorial = getTutorial;
}
