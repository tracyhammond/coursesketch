/**
 * Manages the data for the tutorial.
 *
 * Talks with the remote server to simplify getting data from the database.
 * Created by gigemjt on 5/12/15.
 *
 * @param {CourseSketchDatabase} parent the parent server
 * @param {AdvanceDataListener} dataListener makes listening to the server easier
 * @param {IndexedDB} database (Not used in this manager)
 * @param {Function} sendData A function that makes sending data much easier
 * @param {SrlRequest} Request A shortcut to a request
 * @param {ByteBuffer} ByteBuffer Used in the case of longs for javascript.
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
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);
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
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, tutorial.toArrayBuffer());
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
            advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);
                // we do not need to make server changes we just need to make sure it was successful.
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
            sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, tutorial.toArrayBuffer());
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
     * get tutorial list.
     *
     * @param {String} url The url of the webpage.
     * @param {Function} tutorialCallback called when the tutorial list has been recieved.
     * @param {Number} page the page of the tutorial list that is wanted.
     */
    function getTutorialList(url, tutorialCallback, page) {
        advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);
            tutorialList = [];
            for (var i = 0; i < item.data.length; i++) {
                var tutorial = CourseSketch.PROTOBUF_UTIL.getTutorialClass().decode(item.data[i]);
                tutorialList.append(tutorial);
            }
            tutorialCallback(tutorialList);
        });
        var itemRequest = CourseSketch.PROTOBUF_UTIL.ItemRequest();
        itemRequest.setQuery(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);
        itemRequest.itemId = [ url ];
        if (isUndefined(page)) {
            itemRequest.page = 0;
        } else {
            itemRequest.page = page;
        }
        sendData.sendDataRequest(itemRequest);
    }

    parent.getTutorialList = getTutorialList;

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

        function callOnce(tutorial) {
            if (!called) {
                called = true;
                tutorialCallback(tutorial[0]);
            }
        }

        getTutorialLocal(tutorialId, function(localTutorial) {
            if (!isUndefined(localTutorial) && !(localTutorial instanceof DatabaseException)) {
                callOnce(localTutorial);
                return;
            }
            advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                    CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);

                // after listener is removed
                if (isUndefined(item.data) || item.data === null) {
                    tutorialCallbackComplete(new DatabaseException('The data sent back from the server does not exist.'));
                    return;
                }
                var tutorial = CourseSketch.PROTOBUF_UTIL.getTutorial().decode(item.data[0]);
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
            sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, leftOverId);

        }); // end of getting local tutorial
        getTutorials([ tutorialId ], callOnce, callOnce);
    }

    parent.getTutorial = getTutorial;
}
