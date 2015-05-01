function AssignmentDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
    var dataListener = advanceDataListener;
    var database = parentDatabase;
    var Request = request;
    var localScope = parent;
    var ByteBuffer = buffer;

    /**
     * Looks at the assignment and gives it some state if the state values do
     * not exist.
     */
    function stateCallback(assignment, assignmentCallback) {
        /*jshint maxcomplexity:13 */
        var state = assignment.getState();
        var updateAssignment = false;
        if (isUndefined(state) || state === null) {
            state = CourseSketch.PROTOBUF_UTIL.State();
            updateAssignment = true;
        }
        try {
            // do state stuff
            var access = assignment.getAccessDate().getMillisecond();
            var close = assignment.getCloseDate().getMillisecond();
            var due = assignment.getDueDate().getMillisecond();
            var current = parent.getCurrentTime();
            if (isUndefined(state.accessible) || state.accessible === null) {
                if (current.lessThan(access) || current.greaterThan(close)) {
                    state.accessible = false;
                } else {
                    state.accessible = true;
                }
                updateAssignment = true;
            }

            if (isUndefined(state.pastDue) || state.pastDue === null) {
                if (current.greaterThan(due)) {
                    state.pastDue = true;
                } else {
                    state.pastDue = false;
                }
                updateAssignment = true;
            }
        } catch (exception) {
            // console.error(exception);
        }

        // so we do not have to perform this again!
        if (updateAssignment) {
            assignment.state = state;
            setAssignment(assignment);
        }

        if (assignmentCallback && isFunction(assignmentCallback)) {
            assignmentCallback(assignment);
        }
    }

    /**
     * Calls that stateCallback with all of the assignments in the list
     * modifying their states appropiately.
     */
    function stateCallbackList(assignmentList, assignmentCallback) {
        for (var i = 0; i < assignmentList.length; i++) {
            stateCallback(assignmentList[i]);
        }
        if (assignmentCallback) {
            assignmentCallback(assignmentList);
        }
    }

    /**
     * Sets the assignment locally into the local database.
     */
    function setAssignment(assignment, assignmentCallback) {
        database.putInAssignments(assignment.id, assignment.toBase64(), function(e, request) {
            if (assignmentCallback) {
                assignmentCallback(e, request);
            }
        });
    }
    parent.setAssignment = setAssignment;

    /**
     * Deletes a assignment from local database.
     * This does not delete the id pointing to this item in the respective course.
     *
     * @param {String} assignmentId
     *                ID of the assignment to delete
     * @param {Function} assignmentCallback
     *                function to be called after the deletion is done
     */
    function deleteAssignment(assignmentId, assignmentCallback) {
        database.deleteFromAssignments(assignmentId, function(e, request) {
            if (assignmentCallback) {
                assignmentCallback(e, request);
            }
        });
    }
    parent.deleteAssignment = deleteAssignment;

    /**
     * Sets a assignment in server database.
     *
     * @param {SrlAssignment} assignment
     *                assignment object to set
     * @param {Function} assignmentCallback
     *                function to be called after assignment setting is done
     */
    function insertAssignmentServer(assignment, assignmentCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);
            var resultArray = item.getReturnText().split(':');
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getAssignmentLocal(oldId, function(assignment2) {
                deleteAssignment(oldId);
                if (!isUndefined(assignment2) && !(assignment2 instanceof DatabaseException)) {
                    assignment2.id = newId;
                    setAssignment(assignment2, function() {
                        assignmentCallback(assignment2);
                    });
                } else {
                    assignment.id = newId;
                    setAssignment(assignment, function(e, request) {
                        assignmentCallback(assignment);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, assignment.toArrayBuffer());
    }


    /**
     * Adds a new assignment to both local and server databases. Also updates the
     * corresponding course given by the assignment's courseId.
     *
     * @param {String} assignment
     *                assignment object to insert
     * @param {Function} localCallback
     *                function to be called after local insert is done
     * @param {Function} serverCallback
     *                function to be called after server insert is done
     */
    function insertAssignment(assignment, localCallback, serverCallback) {
        if (isUndefined(assignment.id) || assignment.id === null) {
            assignment.id = generateUUID();
        }
        setAssignment(assignment, function(e, request) {
            console.log('inserted locally :' + assignment.id);
            if (!isUndefined(localCallback)) {
                localCallback(assignment);
            }
            insertAssignmentServer(assignment, function(assignmentUpdated) {
                parent.getCourse(assignment.courseId, function(course) {
                    var assignmentList = course.assignmentList;

                    // remove old Id (if it exists)
                    if (assignmentList.indexOf(assignment.id) >= 0) {
                        removeObjectFromArray(assignmentList, assignment.id);
                    }
                    assignmentList.push(assignmentUpdated.id);
                    parent.setCourse(course, function() {
                        if (!isUndefined(serverCallback)) {
                            serverCallback(assignmentUpdated);
                        }
                    });
                    // Course is set with its new assignment
                });
                // Finished with the course
            });
            // Finished with setting assignment
        });
        // Finished with local assignment
    }
    parent.insertAssignment = insertAssignment;

    /**
     * Sets a assignment in both local and server databases.
     * Updates an existing assignment into the database. This assignment must already
     * exist.
     *
     * @param {SrlAssignment} assignment
     *                assignment object to set
     * @param {Function} localCallback
     *                function to be called after local assignment setting is done
     * @param {Function} serverCallback
     *                function to be called after server assignment setting is done
     */
    function updateAssignment(assignment, localCallback, serverCallback) {
        setAssignment(assignment, function() {
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
            sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, assignment.toArrayBuffer());
        });
    }
    parent.updateAssignment = updateAssignment;

    /**
     * Gets an Assignment from the local database.
     *
     * @param {String} assignmentId
     *                ID of the assignment to get
     * @param {Function} assignmentCallback
     *                function to be called after getting is complete, parameter
     *                is the assignment object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getAssignmentLocal(assignmentId, assignmentCallback) {
        if (isUndefined(assignmentId) || assignmentId === null) {
            assignmentCallback(new DatabaseException('The given id is not assigned', 'getting Assignment: ' + assignmentId));
        }
        database.getFromAssignments(assignmentId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                assignmentCallback(new DatabaseException('The result is undefined', 'getting Assignment: ' + assignmentId));
            } else {
                // gets the data from the database and calls the callback
                try {
                    var bytes = ByteBuffer.fromBase64(result.data);
                    stateCallback(CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().decode(bytes), assignmentCallback);
                } catch (exception) {
                    console.error(exception);
                    assignmentCallback(new DatabaseException('The result is undefined', 'getting Assignment: ' + assignmentId));
                }
            }
        });
    }
    parent.getAssignmentLocal = getAssignmentLocal;

    /**
     * Returns a list of all of the assignments from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull assignments from the server!
     *
     * @param {List<String>} assignmentIdList
     *            list of IDs of the assignments to get
     * @param {Function} assignmentCallbackPartial
     *            {Function} called when assignments are grabbed from the local
     *            database only. This list may not be complete. This may also
     *            not get called if there are no local assignments.
     * @param {Function} assignmentCallbackComplete
     *            {Function} called when the complete list of assignments are
     *            grabbed.
     */
    function getAssignments(assignmentIdList, assignmentCallbackPartial, assignmentCallbackComplete) {
        /*
         * So what happens here might be a bit confusing to some new people so
         * let me explain it. #1 there is a loop that goes through every item in
         * the assignmentIdList (which is a list of assignment ids)
         *
         * #2 there is a function declaration inside the loop the reason for
         * this is so that the assignmentId is not overwritten when the callback
         * is called.
         *
         * #3 we call getAssignmentLocal which then calls a callback about if it
         * got an assignment or not if it didn't we add the id to a list of Id
         * we need to get from the server
         *
         * #4 after the entire list has been gone through (which terminates in
         * the callback with barrier = 0) if there are any that need to be
         * pulled from the server then that happens
         *
         * #5 after talking to the server we get a response with a list of
         * assignments, these are combined with the local assignments then the
         * original callback is called.
         *
         * #6 the function pattern terminates.
         */

        // standard preventative checking
        if (isUndefined(assignmentIdList) || assignmentIdList === null || assignmentIdList.length === 0) {
            assignmentCallbackPartial(new DatabaseException('The given id is not assigned', 'getting Assignment: ' + assignmentIdList));
            if (assignmentCallbackComplete) {
                assignmentCallbackComplete(new DatabaseException('The given id is not assigned', 'getting Assignment: ' + assignmentIdList));
            }
        }

        var barrier = assignmentIdList.length;
        var assignmentList = [];
        var leftOverId = [];

        // create local assignment list so everything appears really fast!
        for (var i = 0; i < assignmentIdList.length; i++) {
            var assignmentIdLoop = assignmentIdList[i];
            // the purpose of this function is purely to scope the assignmentId
            // so that it changes
            (function(assignmentId) {
                getAssignmentLocal(assignmentId, function(assignment) {
                    if (!isUndefined(assignment) && !(assignment instanceof DatabaseException)) {
                        assignmentList.push(assignment);
                    } else {
                        leftOverId.push(assignmentId);
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
                                        assignmentCallbackComplete(new DatabaseException('The data sent back from the server does not exist.'));
                                        return;
                                    }

                                    var assignment = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(item.data[0],
                                            CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass());
                                    if (isUndefined(assignment) || assignment instanceof DatabaseException) {
                                        var result = assignment;
                                        if (isUndefined(result)) {
                                            result = new DatabaseException('Nothing is in the server database!',
                                                'Grabbing assignment from server: ' + assignmentIdList);
                                        }
                                        if (!isUndefined(assignmentCallbackComplete)) {
                                            assignmentCallbackComplete(result);
                                        }
                                        return;
                                    }
                                    for (var i = 0; i < school.assignments.length; i++) {
                                        localScope.setAssignment(school.assignments[i]);
                                        assignmentList.push(school.assignments[i]);
                                    }
                                    stateCallbackList(assignmentList, assignmentCallbackComplete);
                                    assignmentIdList = null;
                                });
                            // creates a request that is then sent to the server
                            sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT, leftOverId);
                        } else {
                            stateCallbackList(assignmentList, assignmentCallbackComplete);
                        }

                        // this calls actually before the response from the
                        // server is received!
                        if (assignmentList.length > 0) {
                            stateCallbackList(assignmentList, assignmentCallbackPartial);
                        }
                    } // end of if(barrier === 0)
                }); // end of getting local assignment
            })(assignmentIdLoop); // end of loopContainer
        } // end of loop
    }
    parent.getAssignments = getAssignments;

    /**
     * Returns a assignment with the given assignmentId will ask the server if it
     * does not exist locally
     *
     * @param {String} assignmentId
     *            The id of the assignment we want to find.
     * @param {Function} assignmentCallback
     *            The method to call when the assignment has been found. (this
     *            is asynchronous)
     */
    function getAssignment(assignmentId, assignmentCallback) {
        if (isUndefined(assignmentCallback)) {
            throw new DatabaseException('Calling get Assignment with an undefined callback');
        }
        var called = false;
        function callOnce(assignmentList) {
            if (!called) {
                called = true;
                assignmentCallback(assignmentList[0]);
            }
        }
        getAssignments([ assignmentId ], callOnce, callOnce);
    }
    parent.getAssignment = getAssignment;
}
