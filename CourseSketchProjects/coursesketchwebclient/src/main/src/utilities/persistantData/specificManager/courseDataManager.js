/**
 * A manager for courses that talks with the remote server.
 *
 * @param {SchoolDataManager} parent - The database that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener - A listener and sender for the database
 * @param {ProtoDatabase} database - The local database
 * @param {ByteBuffer} ByteBuffer - Used in the case of longs for javascript.
 * @constructor
 */
function CourseDataManager(parent, advanceDataListener, database, ByteBuffer) {
    var COURSE_LIST = 'COURSE_LIST';
    var userCourseId = [];
    var userHasCourses = true;

    /**
     * Looks at the course and gives it some state if the state values do not exist.
     *
     * @param {SrlCourse} course - A course to be modified.
     * @param {Function} courseCallback - Called after the state is done updating.
     */
    function stateCallback(course, courseCallback) {
        /*jshint maxcomplexity:13 */
        var state = course.getState();
        var updateCourse = false;
        if (isUndefined(state) || state === null) {
            state = CourseSketch.prutil.State();
            updateCourse = true;
        }
        try {
            // do state stuff
            var access = course.getAccessDate().getMillisecond();
            var close = course.getCloseDate().getMillisecond();
            var current = CourseSketch.getCurrentTime();
            if (isUndefined(state.accessible) || state.accessible === null) {
                if (current.lessThan(access) || current.greaterThan(close)) {
                    state.accessible = false;
                } else {
                    state.accessible = true;
                }
                updateCourse = true;
            }

            if (isUndefined(state.pastDue) || state.pastDue === null) {
                if (current.greaterThan(close)) {
                    state.pastDue = true;
                } else {
                    state.pastDue = false;
                }
                updateCourse = true;
            }
        } catch (exception) {
            //console.log(exception);
        }

        // so we do not have to perform this again!
        if (updateCourse) {
            course.state = state;
            setCourse(course);
        }

        if (courseCallback) {
            courseCallback(course);
        }
    }

    /**
     * Gets an Course from the local database.
     *
     * @param {String} courseId - ID of the course to get
     * @param {Function} courseCallback - function to be called after getting is complete, parameter
     *                is the course object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getCourseLocal(courseId, courseCallback) {
        if (isUndefined(courseId) || courseId === null) {
            courseCallback(new DatabaseException('The given id is not assigned', 'getting Course: ' + courseId));
        }
        // quick and dirty failed fallback to local db
        database.getFromCourses(courseId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                courseCallback(new DatabaseException('The result is undefined', 'getting Course: ' + courseId));
            } else {
                // gets the data from the database and calls the callback
                try {
                    var bytes = ByteBuffer.fromBase64(result.data);
                    stateCallback(CourseSketch.prutil.getSrlCourseClass().decode(bytes), courseCallback);
                } catch (exception) {
                    console.error(exception);
                    courseCallback(new DatabaseException('The result is undefined', 'getting Course: ' + courseId));
                }
            }
        });
    }

    parent.getCourseLocal = getCourseLocal;

    /**
     * Returns a course with the given couresId will ask the server if it does
     * not exist locally.
     *
     *
     * @param {String} courseId - The id of the course we want to find.
     * @param {Function} courseCallback - The method to call when the course has been found. (this is asynchronous)
     */
    function getCourse(courseId, courseCallback) {
        if (isUndefined(courseCallback)) {
            throw new DatabaseException('Calling getCourse with an undefined callback');
        }

        if (isUndefined(courseId) || courseId === null) {
            throw new DatabaseException('The given id is not assigned', 'getting Course: ' + courseId);
        }

        getCourseLocal(courseId, function(localCourse) {
            if (isUndefined(localCourse) || localCourse instanceof DatabaseException) {
                var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.COURSE, [ courseId ]);
                advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
                    if (isException(item)) {
                        courseCallback(new DatabaseException('exception thrown while waiting for response from sever',
                            'getting course ' + courseId, item));
                        return;
                    }
                    if (isUndefined(item.data) || item.data === null) {
                        courseCallback(new DatabaseException('The data sent back from the server for course: ' + courseId + ' does not exist.'));
                        return;
                    }
                    var course = CourseSketch.prutil.decodeProtobuf(item.data[0], CourseSketch.prutil.getSrlCourseClass());
                    if (isUndefined(course)) {
                        courseCallback(new DatabaseException('Course does not exist in the remote database.'));
                        return;
                    }
                    setCourse(course, function() {
                        stateCallback(course, courseCallback);
                    });
                });
            } else {
                // get course local calls state callback so it is not needed here if it exists.
                courseCallback(localCourse);
            }
        });
    }

    parent.getCourse = getCourse;

    /**
     * Sets a course in local database.
     *
     * @param {SrlCourse} course
     *                course object to set
     * @param {Function} courseCallback
     *                function to be called after the course setting is done
     */
    function setCourse(course, courseCallback) {
        database.putInCourses(course.id, course.toBase64(), function(e, request) {
            if (userCourseId.indexOf(course.id) === -1) {
                userCourseId.push(course.id);
                setCourseIdList(userCourseId);
            }
            if (courseCallback) {
                courseCallback(e, request);
            }
        });
    }

    parent.setCourse = setCourse;

    /**
     * Sets a course in both local and server databases.
     * Updates an existing course into the database. This course must already
     * exist.
     *
     * @param {SrlCourse} course - course object to set
     * @param {Function} localCallback - function to be called after local course setting is done
     * @param {Function} serverCallback - function to be called after server course setting is done
     */
    function updateCourse(course, localCallback, serverCallback) {
        if (isUndefined(course)) {
            throw new DatabaseException('Can not update an undefined course');
        }
        setCourse(course, function() {
            if (!isUndefined(localCallback)) {
                localCallback();
            }
            advanceDataListener.sendDataUpdate(CourseSketch.prutil.ItemQuery.COURSE, course.toArrayBuffer(), function(evt, item) {
                // we do not need to make server changes we just need to make sure it was successful.
                if (isException(item)) {
                    var exception = new DatabaseException('exception thrown while waiting for response from sever',
                        'updating course ' + course, item);
                    if (!isUndefined(serverCallback)) {
                        serverCallback(exception);
                    } else {
                        console.error(exception);
                    }
                    return;
                }
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
        });
    }

    parent.updateCourse = updateCourse;

    /**
     * Deletes a course from local database.
     * This does not delete the id pointing to this item in the respective course.
     *
     * @param {String} courseId - ID of the course to delete
     * @param {Function} courseCallback
     *                function to be called after the deletion is done
     */
    function deleteCourse(courseId, courseCallback) {
        database.deleteFromCourses(courseId, function(e, request) {
            // remove course
            if (userCourseId.indexOf(courseId) >= 0) {
                removeObjectFromArray(userCourseId, courseId);
                setCourseIdList(userCourseId);
            }
            if (!isUndefined(courseCallback)) {
                courseCallback(e, request);
            }
        });
    }

    parent.deleteCourse = deleteCourse;

    /**
     * Stores the course ids locally in the database.
     *
     * @param {List<String>} idList - the list of ids the user currently have in their courses.
     */
    function setCourseIdList(idList) {
        database.putInCourses(COURSE_LIST, idList); // no call back needed!
    }

    /**
     * Returns a list of all of the courses in database.
     *
     * This does attempt to pull courses from the server!
     *
     * @param {Function} courseCallback - called when the courses are loaded (this may be called more than once)
     * @param {Boolean} onlyLocal - true if we do not want to ask the server, false otherwise (choose this because it defaults to asking the server).
     */
    function getAllCourses(courseCallback, onlyLocal) {
        // there are no courses loaded onto this client!
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.SCHOOL, [ '' ]);
        /**
         * Called when the server responds.
         *
         * @param {Event} evt - websocket event
         * @param {ItemResult | BaseException} item - The result from the server.
         */
        var callback = function(evt, item) {
            if (isException(item)) {
                courseCallback(new DatabaseException('exception thrown while waiting for response from sever',
                    'Getting all courses for user ' + parent.getCurrentId(), item));
                return;
            }
            // there was an error getting the user classes.
            if (!isUndefined(item.returnText) && item.returnText !== '' && item.returnText !== 'null' && item.returnText !== null) {
                userHasCourses = false;
                console.log(item.returnText);
                courseCallback(new DatabaseException(item.returnText, 'Getting all courses for user ' + parent.getCurrentId()));
                return;
            }
            var courseList = [];
            for (var i = 0; i < item.data.length; i++) {
                courseList.push(CourseSketch.prutil.decodeProtobuf(item.data[i],
                    CourseSketch.prutil.getSrlCourseClass()));
            }

            var setCourseCallback = createBarrier(courseList.length, function() {
                courseCallback(courseList);
            });
            for (var dataIndex = 0; dataIndex < courseList.length; dataIndex++) {
                var course = courseList[dataIndex];
                setCourse(course, setCourseCallback); // no callback is needed
            }
        };
        if (userCourseId.length === 0 && !onlyLocal) {
            advanceDataListener.sendDataRequest(itemRequest, callback);
        } else {
            // This calls the server for updates then creates a list from the
            // local data to appear fast
            // then updates list after server polling and comparing the two
            // list.
            var courseList = [];

            // ask server for course list
            if (!onlyLocal && false) {
                // TODO: this should maybe only ask after a certain amount of time since last updated?
                advanceDataListener.sendDataRequest(itemRequest, callback);
            }

            var localCourseCallback = createBarrier(userCourseId.length, function() {
                if (courseList.length > 0) {
                    courseCallback(courseList);
                } else {
                    courseCallback(new DatabaseException('No Valid Courses exist locally for this user'));
                }
            });
            // create local course list so everything appears really fast!
            for (var i = 0; i < userCourseId.length; i++) {
                this.getCourseLocal(userCourseId[i], function(course) {
                    if (!isUndefined(course) && !(course instanceof DatabaseException)) {
                        courseList.push(course);
                    }
                    localCourseCallback();
                });
            }

            if (userCourseId.length === 0 && onlyLocal) {
                courseCallback(new DatabaseException('No Courses exist locally for this user'));
            }
        }
    }

    parent.getAllCourses = getAllCourses;

    /**
     * Inserts a course into the database. This course must not exist.
     *
     * If there is a problem courseCallback is called with an exception.
     *
     * @param {SrlCourse} course - The course that is being inserted.
     * @param {Function} courseCallback - is called after the insertion of course into the local
     *            database. (this can be used for instant refresh)
     * @param {Function} serverCallback - serverCallback is called after the insertion of course into
     *            the server and the return of the server with the correct
     *            courseId
     */
    function insertCourse(course, courseCallback, serverCallback) {
        if (isUndefined(course.id) || course.id === null) {
            var courseId = generateUUID();
            course.id = courseId;
        }
        // sets the course into the local database;
        setCourse(course, function() {
            if (courseCallback) {
                courseCallback(course);
            }

            advanceDataListener.sendDataInsert(CourseSketch.prutil.ItemQuery.COURSE, course.toArrayBuffer(), function(evt, item) {
                if (isException(item)) {
                    serverCallback(new DatabaseException('An exception was thrown from the server while inserting a course',
                        '' + course, item));
                    return;
                }
                var resultArray = item.getReturnText().split(':');
                var oldId = resultArray[1].trim();
                var newId = resultArray[0].trim();
                // we want to get the current course in the local database in case
                // it has changed while the server was processing.
                getCourseLocal(oldId, function(course2) {
                    deleteCourse(oldId);
                    if (!isUndefined(course2) && !(course2 instanceof DatabaseException)) {
                        course2.id = newId;
                        setCourse(course2, function() {
                            serverCallback(course2);
                        });
                    } else {
                        course.id = newId;
                        setCourse(course, function(e, request) {
                            serverCallback(course);
                        });
                    }
                });
            });
        });
    }

    parent.insertCourse = insertCourse;

    /**
     * Gets the course roster then calls the callback with the map of userId: username.
     *
     * The callback is mandatory at the moment because callbacks to the server are asynchronous.
     * If this method simply returned the roster as a map, it would likely return after a subsequent server call is sent.
     * The subsequent server call would then be passing a null map as the roster instead of the actual roster.
     *
     * @param {String} courseId - The id of the course to retrieve the course roster for.
     * @param {Function} callback - A callback is called with a list of userIds
     */
    function getCourseRoster(courseId, callback) {
        if (isUndefined(callback)) {
            throw new DatabaseException('Calling getGrade with an undefined callback');
        }

        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_ROSTER, [ courseId ]);

        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
            if (isException(item)) {
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                    courseId, item));
                return;
            }
            // after listener is removed
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                // not calling the state callback because this should skip that step.
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                    courseId));
                return;
            }


            var decodedRoster = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(item.data[0], CourseSketch.prutil.getUserNameResponseClass());
            var userMap = new Map();
            for (var i = 0; i < decodedRoster.userNames.length; i++) {
                // Since proto doesn't officially have maps, we must create the map this way
                userMap.set(decodedRoster.userNames[i].key, decodedRoster.userNames[i].value);
            }
            callback(userMap);
        });

    }

    parent.getCourseRoster = getCourseRoster;

    /**
     * Gets the id's of all of the courses in the user's local client.
     */
    database.getFromCourses(COURSE_LIST, function(e, request, result) { // eslint-disable-line require-jsdoc
        if (isUndefined(result) || isUndefined(result.data)) {
            return;
        }
        userCourseId = result.data;
    });

    /**
     * @returns {Array} A list that represents all of the ids of courses in the database.
     */
    parent.getAllCourseIds = function() {
        return JSON.parse(JSON.stringify(userCourseId));
    };

    /**
     * Attempts to clear the database of courses.
     *
     * @param {Function} clearCallback - called after all of the courses were cleared.
     */
    parent.clearCourses = function(clearCallback) {
        var barrier = userCourseId.length;
        var list = parent.getAllCourseIds();
        for (var i = 0; i < list.length; i++) {
            deleteCourse(list[i], function() {
                barrier -= 1;
                if (barrier <= 0 && !isUndefined(clearCallback)) {
                    clearCallback();
                }
            });
        }
        if (list.length === 0) {
            clearCallback();
        }
    };

    /**
     * Searches the course list.
     *
     * @param {Function} callback - called with a list of all courses meeting the search requirements.
     */
    parent.searchCourses = function(callback) {
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.COURSE_SEARCH);
        /**
         * Listens for the search result and displays the result given to it.
         */
        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {// eslint-disable-line require-jsdoc
            if (isException(item)) {
                callback(new DatabaseException('There was an exception when getting the data back from the server while searching courses', item));
                return;
            }

            // there was an error getting the user classes.
            if (isUndefined(item.data) || item.data === null) {
                callback(new DatabaseException('There was no data sent back from the server for searching courses'));
                return;
            }
            var courseList = [];
            for (var i = 0; i < item.data.length; i++) {
                courseList.push(CourseSketch.prutil.decodeProtobuf(item.data[i],
                    CourseSketch.prutil.getSrlCourseClass()));
            }
            callback(courseList);
        });
    };
}
