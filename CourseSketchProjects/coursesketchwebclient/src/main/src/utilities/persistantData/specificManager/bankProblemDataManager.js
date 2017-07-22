/**
 * A manager for assignments that talks with the remote server.
 *
 * @param {SchoolDataManager} parent - The database that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener - A listener for the database.
 * @param {ProtoDatabase} parentDatabase -  The local database
 * @param {ByteBuffer} ByteBuffer - Used in the case of longs for javascript.
 * @constructor
 */
function BankProblemDataManager(parent, advanceDataListener, parentDatabase, ByteBuffer) {

    /**
     * Sets a bankProblem in local database.
     *
     * @param {SrlBankProblem} bankProblem - bankProblem object to set
     * @param {Function} bankProblemCallback - function to be called after the bankProblem setting is done
     */
    function setBankProblem(bankProblem, bankProblemCallback) {
        parentDatabase.putInBankProblems(bankProblem.id, bankProblem.toBase64(), function(e, request) {
            if (bankProblemCallback) {
                bankProblemCallback(e, request);
            }
        });
    }

    parent.setBankProblem = setBankProblem;

    /**
     * Deletes a bankProblem from local database.
     * This does not delete the id pointing to this item in the respective course.
     *
     * @param {String} bankProblemId
     *                ID of the lecture to delete
     * @param {Function} bankProblemCallback
     *                function to be called after the deletion is done
     */
    function deleteBankProblem(bankProblemId, bankProblemCallback) {
        parentDatabase.deleteFromBankProblems(bankProblemId, function(e, request) {
            if (bankProblemCallback) {
                bankProblemCallback(e, request);
            }
        });
    }

    parent.deleteBankProblem = deleteBankProblem;

    /**
     * Gets a bankProblem from the local database.
     *
     * @param {String} bankProblemId - ID of the bankProblem to get
     * @param {Function} bankProblemCallback - Function to be called after getting is complete, parameter
     *                is the bankProblem object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getBankProblemLocal(bankProblemId, bankProblemCallback) {
        if (isUndefined(bankProblemId) || bankProblemId === null) {
            bankProblemCallback(new DatabaseException('The given id is not assigned', 'getting BankProblem: ' + bankProblemId));
        }
        parentDatabase.getFromBankProblems(bankProblemId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                bankProblemCallback(new DatabaseException('The result is undefined', 'getting BankProblem: ' + bankProblemId));
            } else {
                // gets the data from the database and calls the callback
                var bytes = ByteBuffer.fromBase64(result.data);
                bankProblemCallback(CourseSketch.prutil.getSrlBankProblemClass().decode(bytes));
            }
        });
    }

    parent.getBankProblemLocal = getBankProblemLocal;


    /**
     * Adds a new bankProblem to the server databases.
     *
     * @param {SrlBankProblem} bankProblem
     *                bankProblem object to insert.
     * @param {Function} serverCallback
     *                function to be called after server insert is done.  Called with the new updateId of the bank problem.
     */
    function insertBankProblemServer(bankProblem, serverCallback) {
        if (isUndefined(bankProblem.id) || bankProblem.id === null) {
            bankProblem.id = generateUUID();
        }
        advanceDataListener.sendDataInsert(CourseSketch.prutil.ItemQuery.BANK_PROBLEM, bankProblem.toArrayBuffer(), function(evt, item) {
            if (isException(item)) {
                serverCallback(new DatabaseException('exception thrown while waiting for response from sever',
                    'inesrting bank problem ' + bankProblem, item));
                return;
            }

            var resultArray = item.getReturnText().split(':');
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getBankProblemLocal(oldId, function(bankProblem2) {
                deleteBankProblem(oldId);
                if (!isUndefined(bankProblem2) && !(bankProblem2 instanceof DatabaseException)) {
                    bankProblem2.id = newId;
                    setBankProblem(bankProblem2, function() {
                        serverCallback(bankProblem2);
                    });
                } else {
                    bankProblem.id = newId;
                    setBankProblem(bankProblem, function() {
                        serverCallback(bankProblem);
                    });
                }
            });
        });
    }

    /**
     * Adds a new bankProblem to both local and server databases. Also updates the
     * corresponding course given by the bankProblem's courseId.
     *
     * Inserts the bank problem in the case that the course problem does not exist.
     * This is detirmined if the bankProblem does not have a bankproblem id but does have the actual data for a bank problem.
     *
     * @param {SrlBankProblem} bankProblem
     *                bankProblem object to insert
     * @param {Function} [localCallback]
     *                function to be called after local insert is done
     * @param {Function} [serverCallback]
     *                function to be called after server insert is done
     */
    function insertBankProblem(bankProblem, localCallback, serverCallback) {
        if (isUndefined(bankProblem.id) || bankProblem.id === null) {
            bankProblem.id = generateUUID();
        }

        /**
         * This function is called after the bank problem is inserted if the course problem does not have a bank problem id.
         * Otherwise it is called immediately.
         */
        function insertingBankProblem() {
            setBankProblem(bankProblem, function() {
                console.log('inserted locally :' + bankProblem.id);
                if (!isUndefined(localCallback)) {
                    localCallback(bankProblem);
                }
                insertBankProblemServer(bankProblem, function(bankProblemUpdated) {
                    if (!isUndefined(serverCallback)) {
                        serverCallback(bankProblemUpdated);
                    }
                    // Finished with the course
                });
                // Finished with setting bankProblem
            });
        } // insertingBankProblem
        insertingBankProblem();

        // Finished with local bankProblem
    }

    parent.insertBankProblem = insertBankProblem;

    /**
     * Updates a bankProblem in both local and server databases.
     * Updates an existing bankProblem into the database. This bankProblem must already
     * exist.
     *
     * @param {SrlBankProblem} bankProblem - BankProblem object to set
     * @param {Function} localCallback - Function to be called after local bankProblem setting is done
     * @param {Function} serverCallback - Function to be called after server bankProblem setting is done
     */
    function updateBankProblem(bankProblem, localCallback, serverCallback) {
        setBankProblem(bankProblem, function() {
            if (!isUndefined(localCallback)) {
                localCallback(bankProblem);
            }
            advanceDataListener.sendDataUpdate(CourseSketch.prutil.ItemQuery.BANK_PROBLEM, bankProblem.toArrayBuffer(), function(evt, item) {
                if (isException(item) && !isUndefined(serverCallback)) {
                    serverCallback(new DatabaseException('exception thrown while waiting for response from sever',
                        'updating bank problem ' + bankProblem, item));
                    return;
                }
                // we do not need to make server changes we just need to make sure it was successful.
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
        });
    }

    parent.updateBankProblem = updateBankProblem;


    /**
     * Creates a request for asking about bank problems.
     *
     * @param {String} courseId - The id of the course the problem is being requested for.
     * @param {String} assignmentId - The id of the assignment the problem is being requested for.
     * @param {Integer} page - To make it easier we do not grab every single bank problem instead we grab them in batches
     *              (this process is called pagination)
     * @returns {ItemRequest} The request that is ready to be sent to the server.
     */
    function createAdvancedRequest(courseId, assignmentId, page) {
        var itemRequest = CourseSketch.prutil.ItemRequest();
        itemRequest.page = page;
        itemRequest.query = CourseSketch.prutil.ItemQuery.BANK_PROBLEM;
        itemRequest.itemId = [];
        itemRequest.itemId.push(courseId);
        itemRequest.itemId.push(assignmentId);

        return itemRequest;
    }

    /**
     * Returns a list of all of the bank problems from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull bank problems from the server!
     *
     * @param {String} courseId - The id of the course the problem is being requested for.
     * @param {String} assignmentId - The id of the assignment the problem is being requested for.
     * @param {Integer} page - To make it easier we do not grab every single bank problem instead we grab them in batches
     *              (this process is called pagination)
     * @param {Function} bankProblemCallbackComplete - called when the complete list of bank problems are
     *            grabbed.
     */
    function getAllBankProblems(courseId, assignmentId, page, bankProblemCallbackComplete) {
        var bankProblemRequest = createAdvancedRequest(courseId, assignmentId, page);
        var bankProblemList = [];
        advanceDataListener.sendDataRequest(bankProblemRequest, function(evt, item) {
            if (isException(item)) {
                bankProblemCallbackComplete(new DatabaseException('exception thrown while waiting for response from sever',
                    'getting bank problem course: [' + courseId + '] assignment: [' + assignmentId + ']', item));
                return;
            }
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                bankProblemCallbackComplete(new DatabaseException('The data sent back from the server does not exist: ' +
                    leftOverId));
                return;
            }

            for (var dataIndex = 0; dataIndex < item.data.length; dataIndex++) {
                var decodedBankProblem = CourseSketch.prutil.getSrlBankProblemClass().decode(item.data[dataIndex]);
                parent.setBankProblem(decodedBankProblem);
                bankProblemList.push(decodedBankProblem);
            }
            bankProblemCallbackComplete(bankProblemList);
        });
    }

    parent.getAllBankProblems = getAllBankProblems;

    /**
     * Returns a list of all of the bank problems from the local and server database for the given list
     * of Ids.
     *
     * This does attempt to pull bank problems from the server!
     *
     * @param {List<String>} bankProblemIdList
     *            list of IDs of the bankProblems to get
     * @param {Function} bankProblemCallbackPartial - called when bank problems are grabbed from the local
     *            database only. This list may not be complete. This may also
     *            not get called if there are no local bank problems.
     * @param {Function} bankProblemCallbackComplete - called when the complete list of bank problems are
     *            grabbed.
     */
    function getBankProblems(bankProblemIdList, bankProblemCallbackPartial, bankProblemCallbackComplete) {
        /*
         * So what happens here might be a bit confusing to some new people so let me explain it.
         * #1 there is a loop that goes through every item in the bankProblemIdList (which is a list of bankProblem ids)
         *
         * #2 there is a function declaration inside the loop the reason for this is so that the bankProblemId is not overwritten
         * when the callback is called.
         *
         * #3 we call getBankProblemLocal which then calls a callback about if it got an bankProblem or not if it didnt we add the id to a
         * list of Id we need to get from the server
         *
         * #4 after the entire list has been gone through (which terminates in the callback with barrier = 0)
         * if there are bank problems that need to be pulled from the server then that happens.
         *
         * #5 after talking to the server we get a response with a list of bankProblems,
         * these are combined with the local bankProblems then the original callback is called.
         *
         * #6 the function pattern terminates.
         */

        // standard preventative checking
        if (isUndefined(bankProblemIdList) || bankProblemIdList === null || bankProblemIdList.length === 0) {
            bankProblemCallbackPartial(new DatabaseException('The given id is not assigned', 'getting BankProblem: ' + bankProblemIdList));
            if (bankProblemCallbackComplete) {
                bankProblemCallbackComplete(new DatabaseException('The given id is not assigned', 'getting BankProblem: ' + bankProblemIdList));
            }
            return;
        }

        var barrier = bankProblemIdList.length;
        var bankProblemList = [];
        var leftOverId = [];

        // create local bankProblem list so everything appears really fast!
        for (var i = 0; i < bankProblemIdList.length; i++) {
            var bankProblemIdLoop = bankProblemIdList[i];

            /**
             * The purpose of this function is purely to scope the bankProblemId so that it changes.
             *
             * @param {String} bankProblemId - The id of a single bankProblem.
             */
            function loopContainer(bankProblemId) {
                getBankProblemLocal(bankProblemId, function(bankProblem) {
                    if (!isUndefined(bankProblem) && !(bankProblem instanceof DatabaseException)) {
                        bankProblemList.push(bankProblem);
                    } else {
                        leftOverId.push(bankProblemId);
                    }
                    barrier -= 1;
                    if (barrier === 0) {
                        // after the entire list has been gone through pull the leftovers from the server
                        if (leftOverId.length >= 1) {
                            var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.BANK_PROBLEM, leftOverId);
                            advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
                                if (isException(item)) {
                                    bankProblemCallbackComplete(new DatabaseException('exception thrown while waiting for response from sever',
                                        'getting bank problem ' + leftOverId, item));
                                    return;
                                }
                                if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                                    bankProblemCallbackComplete(new DatabaseException('The data sent back from the server does not exist: ' +
                                        leftOverId));
                                    return;
                                }

                                for (var dataIndex = 0; dataIndex < item.data.length; dataIndex++) {
                                    var decodedBankProblem = CourseSketch.prutil.getSrlBankProblemClass().decode(item.data[dataIndex]);
                                    parent.setBankProblem(decodedBankProblem);
                                    bankProblemList.push(decodedBankProblem);
                                }
                                bankProblemCallbackComplete(bankProblemList);
                            });
                        }

                        // this calls actually before the response from the server is received!
                        if (bankProblemList.length > 0) {
                            bankProblemCallbackPartial(bankProblemList);
                        } else {
                            bankProblemCallbackPartial(new DatabaseException('Nothing is in the the local database!',
                                'Grabbing bankProblem from server: ' + leftOverId));
                        }
                    } // end of if(barrier === 0)
                }); // end of getting local bankProblem
            } // end of loopContainer
            loopContainer(bankProblemIdLoop);
        } // end of loop
    }

    parent.getBankProblems = getBankProblems;

    /**
     * Returns a bankProblem with the given courseId; this function will ask the server if it does not exist locally.
     *
     * If the server is polled and the bankProblem still does not exist the function will call the callback with an exception.
     *
     * @param {String} bankProblemId - The id of the bankProblem we want to find.
     * @param {Function} bankProblemLocalCallback - called when bank problems are grabbed from the local
     *            util only. This list may not be complete. This may also
     *            not get called if there are no local bank problems.
     * @param {Function} bankProblemServerCallback - called when the complete list of bank problems are grabbed.
     */
    function getBankProblem(bankProblemId, bankProblemLocalCallback, bankProblemServerCallback) {
        getBankProblems([ bankProblemId ], function(bankProblemList) {
            if (!isUndefined(bankProblemLocalCallback) &&
                (bankProblemList instanceof CourseSketch.DatabaseException || isUndefined(bankProblemList[0]))) {
                bankProblemLocalCallback(new DatabaseException('Error with grabbing local bank problem', bankProblemList));
                return;
            }
            if (bankProblemLocalCallback) {
                bankProblemLocalCallback(bankProblemList[0]);
            }
        }, function(bankProblemList) {
            if (!isUndefined(bankProblemServerCallback) &&
                (bankProblemList instanceof CourseSketch.DatabaseException || isUndefined(bankProblemList[0]))) {
                bankProblemServerCallback(new DatabaseException('Error with grabbing remote bank problem', bankProblemList));
                return;
            }
            if (bankProblemServerCallback) {
                bankProblemServerCallback(bankProblemList[0]);
            }
        });
    }

    parent.getBankProblem = getBankProblem;
}
