/**
 * Manages the data for the tutorial.
 *
 * Talks with the remote server to simplify getting data from the database.
 * Created by gigemjt on 5/12/15.
 *
 * @param {CourseSketchDatabase} parent the parent server
 * @param {AdvanceDataListener} advanceDataListener makes listening to the server easier
 * @param {IndexedDB} database (Not used in this manager)
 * @param {Function} sendData A function that makes sending data much easier
 * @param {SrlRequest} Request A shortcut to a request
 * @param {ByteBuffer} ByteBuffer Used in the case of longs for javascript.
 * @constructor
 */
function TutorialDataManager(parent, advanceDataListener, database, sendData, Request, ByteBuffer) {
    var localScope = parent;

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
    function insertTutorial(tutorial, tutorialCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL);
            console.log('got response from server!');
            if (tutorialCallback) {
                tutorialCallback();
            }
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, tutorial.toArrayBuffer());
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
            tutorialCallbackComplete(tutorialList);
            tutorialIdList = null;
        });
        // creates a request that is then sent to the server
        sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.TUTORIAL, leftOverId);
    }

    parent.getTutorial = getTutorial;
}
