function CourseDataManager(parent, advanceDataListener, parentDatabase, sendData, Request, ByteBuffer) {
    const
    COURSE_LIST = "COURSE_LIST";
    var userCourses = {};
    var userCourseId = [];
    var userHasCourses = true;
    var dataListener = advanceDataListener;
    var database = parentDatabase;
    var sendDataRequest = sendData.sendDataRequest;

    /**
     * Looks at the course and gives it some state if the state values do not
     * exist.
     */
    function stateCallback(course, courseCallback) {
        var state = course.getState();
        var updateCourse = false;
        if (isUndefined(state) || state == null) {
            state = CourseSketch.PROTOBUF_UTIL.State();
            updateCourse = true;
        }
        try {
            // do state stuff
            var access = course.getAccessDate().getMillisecond();
            var close = course.getCloseDate().getMillisecond();
            var current = CourseSketch.getCurrentTime();
            if (isUndefined(state.accessible) || state.accessible == null) {
                if (current.lessThan(access) || current.greaterThan(close)) {
                    state.accessible = false;
                } else {
                    state.accessible = true;
                }
                updateCourse = true;
            }

            if (isUndefined(state.pastDue) || state.pastDue == null) {
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
     * @param courseId
     *                ID of the course to get
     * @param courseCallback
     *                function to be called after getting is complete, parameter
     *                is the course object, can be called with {@link DatabaseException} if an exception occurred getting the data.
     */
    function getCourseLocal(courseId, courseCallback) {
        if (isUndefined(courseId) || courseId == null) {
            courseCallback(new DatabaseException("The given id is not assigned", "getting Course: " + courseId));
        }

        // quick and dirty this is in ram && synchronous (not in local memory)
        if (!isUndefined(userCourses[courseId])) {
            if (userCourses[courseId] == nonExistantValue || isUndefined(userCourses[courseId]) || userCourses[courseId] == null) {
                courseCallback(new DatabaseException("Course does not exist", "Getting courses with id " + courseId));
                return;
            }
            var bytes = ByteBuffer.fromBase64(userCourses[courseId]);
            stateCallback(CourseSketch.PROTOBUF_UTIL.getSrlCourseClass().decode(bytes), courseCallback);
            return;
        }

        // quick and dirty failed fallback to local db
        database.getFromCourses(courseId, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                courseCallback(new DatabaseException("The result is undefined", "getting Course: " + courseId));
            } else if (result.data == nonExistantValue) {
                // the server holds this special value then it means the server
                // does not have the value
                courseCallback(new DatabaseException("The database does not hold this value", "getting Course: " + courseId));
            } else {
                // gets the data from the database and calls the callback
                try {
                    var bytes = ByteBuffer.fromBase64(result.data);
                    stateCallback(CourseSketch.PROTOBUF_UTIL.getSrlCourseClass().decode(bytes), courseCallback);
                } catch (exception) {
                    console.error(exception);
                    courseCallback(new DatabaseException("The result is undefined", "getting Course: " + courseId));
                }
            }
        });
    }
    parent.getCourseLocal = getCourseLocal;

    /**
     * Returns a course with the given couresId will ask the server if it does
     * not exist locally.
     *
     * If the server is pulled and the course still does not exist the Id is set
     * with nonExistantValue and the database is never polled for this item for
     * the life of the program again.
     *
     * @param courseId
     *            The id of the course we want to find.
     * @param courseCallback
     *            The method to call when the course has been found. (this is
     *            asynchronous)
     */
    function getCourse(courseId, courseCallback) {
        if (isUndefined(courseId) || courseId == null) {
            courseCallback(new DatabaseException("The given id is not assigned", "getting Course: " + courseId));
        }

        getCourseLocal(courseId, function(course) {
            if (isUndefined(course) || course instanceof DatabaseException) {
                advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
                    advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE);
                    var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
                    var course = school.courses[0];
                    if (isUndefined(course)) {
                        userCourses[courseId] = nonExistantValue;
                        courseCallback(nonExistantValue);
                        advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE);
                        return;
                    }
                    setCourse(course, function() {
                        stateCallback(course, courseCallback);
                    });
                });
                // creates a request that is then sent to the server
                sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, [ courseId ]);
            } else {
                // get course local calls state callback so it is not needed here if it exists.
                courseCallback(course);
            }
        });
    }

    parent.getCourse = getCourse;

    /**
     * Sets a course in local database.
     *
     * @param course
     *                course object to set
     * @param courseCallback
     *                function to be called after the course setting is done
     */
    function setCourse(course, courseCallback) {
        database.putInCourses(course.id, course.toBase64(), function(e, request) {
            if (courseCallback) {
                courseCallback(e, request);
            }
        });
        userCourses[course.id] = course.toBase64(); // stored in memory
    }
    parent.setCourse = setCourse;

    /**
     * Sets a course in both local and server databases.
     * Updates an existing course into the database. This course must already
     * exist.
     *
     * @param course
     *                course object to set
     * @param localCallback
     *                function to be called after local course setting is done
     * @param serverCallback
     *                function to be called after server course setting is done
     */
    function updateCourse(course, localCallback, serverCallback) {
        setCourse(course, function() {
            if (!isUndefined(localCallback)) {
                localCallback();
            }
            advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_UPDATE, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE);
                 // we do not need to make server changes we
                                        // just need to make sure it was successful.
                if (!isUndefined(serverCallback)) {
                    serverCallback(item);
                }
            });
            sendData.sendDataUpdate(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, course.toArrayBuffer());
        });
    }
    parent.updateCourse = updateCourse;

    function deleteCourse(courseId, courseCallback) {
        database.deleteFromCourses(courseId, function(e, request) {
            delete userCourses[courseId]; // removing it from the local map
            var index = userCourseId.indexOf(courseId);
            if (index > -1) {
                userCourseId.splice(index, 1);
                setCourseIdList(userCourseId);
            }
            if (!isUndefined(courseCallback)) {
                courseCallback(e, request);
            }
        });
    }
    parent.deleteCourse = deleteCourse;

    function setCourseIdList(idList) {
        userCourseId = idList;
        database.putInCourses(COURSE_LIST, idList); // no call back needed!
    }

    /**
     * Returns a list of all of the courses in database.
     *
     * This does attempt to pull courses from the server!
     * @param courseCallback called when the courses are loaded (this may be called more than once)
     * @param onlyLocal {Boolean} true if we do not want to ask the server, false otherwise (choose this because it defaults to asking the server).
     */
    function getAllCourses(courseCallback, onlyLocal) {
        var localFunction = setCourseIdList;
        // there are no courses loaded onto this client!
        advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SCHOOL, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SCHOOL);
            // there was an error getting the user classes.
            if (!isUndefined(item.returnText) && item.returnText != "" && item.returnText != "null" && item.returnText != null) {
                userHasCourses = false;
                console.log(item.returnText);
                advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SCHOOL);
                courseCallback(new DatabaseException(item.returnText, "Getting all courses for user " + parent.getCurrentId()));
                return;
            }
            var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
            var courseList = school.courses;
            var idList = [];
            for (var i = 0; i < courseList.length; i++) {
                var course = courseList[i];
                setCourse(course); // no callback is needed
                idList.push(course.id);
            }
            courseCallback(courseList);
            setCourseIdList(idList);
        });
        if (userCourseId.length == 0 && userHasCourses) {
            sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.SCHOOL, [ "" ]);
            // console.log("course list from server polled!");
        } else {
            // This calls the server for updates then creates a list from the
            // local data to appear fast
            // then updates list after server polling and comparing the two
            // list.
            // console.log("course list from local place polled!");
            var barrier = userCourseId.length;
            var courseList = [];

            // ask server for course list
            if (!onlyLocal) {
                sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.SCHOOL, [ "" ]);
            }

            // create local course list so everything appears really fast!
            for (var i = 0; i < userCourseId.length; i++) {
                this.getCourse(userCourseId[i], function(course) {
                    courseList.push(course);
                    barrier -= 1;
                    if (barrier == 0) {
                        courseCallback(courseList);
                        return;
                    }
                });
            }

            if (onlyLocal) {
                courseCallback(new DatabaseException("No Courses exist locally for this user"));
            }
            // we ask the program for the list of courses by id then we compare
            // and update!
        }
    }
    parent.getAllCourses = getAllCourses;

    /**
     * Inserts a course into the database. This course must not exist.
     *
     * If there is a problem courseCallback is called with an error code. TODO:
     * create error code and call courseCallback.
     *
     * @param course
     * @param courseCallback
     *            is called after the insertion of course into the local
     *            database. (this can be used for instant refresh)
     * @param serverCallback
     *            serverCallback is called after the insertion of course into
     *            the server and the return of the server with the correct
     *            courseId
     */
    function insertCourse(course, courseCallback, serverCallback) {
        if (isUndefined(course.id) || course.id == null) {
            var courseId = generateUUID();
            course.id = courseId;
        }
        // sets the course into the local database;
        setCourse(course, function() {
            if (courseCallback) {
                courseCallback(course);
            } // temp for now!

            advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
                advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE);
                var resultArray = item.getReturnText().split(":");
                var oldId = resultArray[1];
                var newId = resultArray[0];
                // we want to get the current course in the local database in case
                // it has changed while the server was processing.
                getCourseLocal(oldId, function(course2) {
                    deleteCourse(oldId);
                    course2.id = newId;
                    setCourse(course2, function() {
                        userCourseId.push(newId);
                        setCourseIdList(userCourseId);

                        // now that everything is set we can return
                        if (!isUndefined(serverCallback)) {
                            serverCallback(course2);
                        }
                    });
                });
            });
            sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE, course.toArrayBuffer());
        });
    }
    parent.insertCourse = insertCourse;

    /*
     * gets the id's of all of the courses in the user's local client.
     */
    database.getFromCourses(COURSE_LIST, function(e, request, result) {
        if (isUndefined(result) || isUndefined(result.data)) {
            return;
        }
        userCourseId = result.data;
    });

    /**
     * @return {Array} A list that represents all of the ids of courses in the database.
     */
    parent.getAllCourseIds = function() {
        return JSON.parse(JSON.stringify(userCourseId));
    }

    /**
     * Attempts to clear the database of courses.
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
        if (list.length == 0) {
            clearCallback();
        }
    }
}
