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
        var state = assignment.getState();
        var updateAssignment = false;
        if (isUndefined(state) || state == null) {
            state = CourseSketch.PROTOBUF_UTIL.State();
            updateAssignment = true;
        }
        try {
            // do state stuff
            var access = assignment.getAccessDate().getMillisecond();
            var close = assignment.getCloseDate().getMillisecond();
            var due = assignment.getDueDate().getMillisecond();
            var current = parent.getCurrentTime();
            if (isUndefined(state.accessible) || state.accessible == null) {
                if (current.lessThan(access) || current.greaterThan(close)) {
                    state.accessible = false;
                } else {
                    state.accessible = true;
                }
                updateAssignment = true;
            }

            if (isUndefined(state.pastDue) || state.pastDue == null) {
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

    function setAssignment(assignment, assignmentCallback) {
        database.putInAssignments(assignment.id, assignment.toBase64(), function(e, request) {
            if (assignmentCallback) {
                assignmentCallback(e, request);
            }
        });
    }
    parent.setAssignment = setAssignment;
    parent.setAssignmentLocal = setAssignment;

    function deleteAssignment(assignmentId, couresCallback) {
        database.deleteFromAssignments(assignmentId, function(e, request) {
            if (assignmentCallback) {
                assignmentCallback(e, request);
            }
        });
    }
    parent.deleteAssignment = deleteAssignment;

    /**
     * Gets an Assignment from the local database.
     *
     * @param assignmentId
     *                ID of the assignment to get
     * @param assignmentCallback
     *                function to be called after getting is complete, parameter
     *                is the assignment object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getAssignmentLocal(assignmentId, assignmentCallback) {
        if (isUndefined(assignmentId) || assignmentId == null) {
            assignmentCallback(new DatabaseException("The given id is not assigned", "getting Assignment: " + assignmentId));
        }
        database.getFromAssignments(assignmentId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                assignmentCallback(new DatabaseException("The result is undefined", "getting Assignment: " + assignmentId));
            } else if (result.data == nonExistantValue) {
                // the server holds this special value then it means the server
                // does not have the value
                assignmentCallback(new DatabaseException("The database does not hold this value", "getting Assignment: " + assignmentId));
            } else {
                // gets the data from the database and calls the callback
                try {
                    var bytes = ByteBuffer.fromBase64(result.data);
                    stateCallback(CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().decode(bytes), assignmentCallback);
                } catch (exception) {
                    console.error(exception);
                    assignmentCallback(new DatabaseException("The result is undefined", "getting Assignment: " + assignmentId));
                }
            }
        });
    }

    /**
     * Returns a list of all of the assignments from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull assignments from the server!
     *
     * @param assignmentIdList
     *            list of IDs of the assignments to get
     * @param assignmentCallbackPartial
     *            {Function} called when assignments are grabbed from the local
     *            database only. This list may not be complete. This may also
     *            not get called if there are no local assignments.
     * @param assignmentCallbackComplete
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
        if (isUndefined(assignmentIdList) || assignmentIdList == null || assignmentIdList.length == 0) {
            assignmentCallbackPartial(new DatabaseException("The given id is not assigned", "getting Assignment: " + assignmentIdList));
            if (assignmentCallbackComplete) {
                assignmentCallbackComplete(new DatabaseException("The given id is not assigned", "getting Assignment: " + assignmentIdList));
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
                                        var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
                                        var assignment = school.assignments[0];
                                        if (isUndefined(assignment) || assignment instanceof DatabaseException) {
                                            advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                                                    CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);
                                            var result = assignment;
                                            if (isUndefined(result)) {
                                                result = new DatabaseException("Nothing is in the server database!",
                                                    "Grabbing assignment from server: " + assignmentIdList);
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
                                        advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
                                                CourseSketch.PROTOBUF_UTIL.ItemQuery.ASSIGNMENT);
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
                    } // end of if(barrier == 0)
                }); // end of getting local assignment
            })(assignmentIdLoop); // end of loopContainer
        } // end of loop
    }
    parent.getAssignments = getAssignments;

    /**
     * Returns a assignment with the given assignmentId will ask the server if it
     * does not exist locally
     *
     * If the server is pulled and the assignment still does not exist the Id is
     * set with nonExistantValue and the database is never polled for this item
     * for the life of the program again.
     *
     * @param assignmentId
     *            The id of the assignment we want to find.
     * @param assignmentCallback
     *            The method to call when the assignment has been found. (this
     *            is asynchronous)
     */
    function getAssignment(assignmentId, assignmentCallback) {
        getAssignments([ assignmentId ], function(assignmentList) {
            assignmentCallback(assignmentList[0]);
        });
    }
    parent.getAssignment = getAssignment;
}
