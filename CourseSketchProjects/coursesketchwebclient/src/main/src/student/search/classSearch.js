validateFirstRun(document.currentScript);

(function() {

    var localDoc = document;
    var courseList1 = [];
    var courseList2 = [];
    var courseRightSide = {};
    var courseProtoMap = {};

    var setTimeVar;

    /**
     * Listens for the search result and displays the result given to it.
     *
     * @param {List<SrlCourse>} item - The search results.
     */
    var searchCallback =  function(item) {
        var courseList = [];
        if (CourseSketch.isException(item)) {
            CourseSketch.clientException(item);
            localDoc.getElementById('loadingIcon').style.display = 'none';
            return;
        } else {
            courseList = item;
        }

        var idList = [];
        for (var i = 0; i < courseList.length; i++) {
            courseProtoMap[courseList[i].id] = courseList[i];
            if (i % 2 === 0) {
                courseList1.push(courseList[i]);
                courseRightSide[courseList[i].id] = false;
            } else {
                courseList2.push(courseList[i]);
                courseRightSide[courseList[i].id] = true;
            }
        }
        var schoolItemBuilder = new SchoolItemBuilder();

        schoolItemBuilder.showImage = false; // till we have images actually working!
        schoolItemBuilder.setBoxClickFunction(CourseSketch.classSearch.courseClickerFunction);

        if (CourseSketch.isException(item)) {
            schoolItemBuilder.setEmptyListMessage(item.getMessage());
        } else {
            schoolItemBuilder.setEmptyListMessage('No Courses were found');
        }

        if (courseList1.length > 0) {
            schoolItemBuilder.setList(courseList1).build('class_list_column1');
        }
        if (courseList2.length > 0) {
            schoolItemBuilder.setList(courseList2).build('class_list_column2');
        }

        if (courseList1.length <= 0 && courseList2.length <= 0) {
            schoolItemBuilder.setList(courseList1).build('class_list_column1');
        }

        localDoc.getElementById('loadingIcon').style.display = 'none';
    };

    CourseSketch.dataListener.setErrorListener(function(msg) {
        localDoc.getElementById('loadingIcon').innerHTML = '<h1>error loading data</h1> <p>' + msg.getResponseText() + '</p>';
        clearTimeout(setTimeVar);
    });

/*
    CourseSketch.dataListener.setListener(CourseSketch.prutil.getRequestClass().MessageType.DATA_REQUEST,
            CourseSketch.prutil.ItemQuery.REGISTER, function(evt, item) {
        alert('User is already registered for this course');
        clearTimeout(setTimeVar);
    });
    */

    /**
     * Moves the course element over so that the registration button is visible.
     * Also sets up the registration button.
     */
    CourseSketch.classSearch.courseClickerFunction = function(course) {
        var id = course.id;
        var element = localDoc.getElementById(id);
        var width = element.offsetWidth / 2;
        var moveAmount = width + 'px';
        //console.log('style '  + element.style.marginLeft);
        if (element.style.marginLeft === '' || element.style.marginLeft === '0px') {
            if (courseRightSide[id]) {
                moveAmount = '' + moveAmount;
            } else {
                moveAmount = '-' + moveAmount;
            }
            var button = localDoc.createElement('button');
            button.setAttribute('id', 'button' + id);
            /**
             * Called to reigster the student.
             */
            button.onclick = function() {
                CourseSketch.classSearch.registerClass(id);
            };
            button.textContent = 'Register';
            button.style.position = 'absolute';
            if (courseRightSide[id]) {
                button.style.left = element.offsetLeft + 'px';
            } else {
                button.style.left = (element.offsetLeft + element.offsetWidth - width / 2) + 'px';
            }
            button.style.top = element.offsetHeight / 2 + element.offsetTop + 'px';
            //localDoc.appendChild(button);
            localDoc.getElementById('registerButton').appendChild(button);
            $('#' + id).animate({
                marginLeft: moveAmount
                }, 300, function() {
                });
        } else {
            $('#' + id).animate({
                marginLeft: '0px'
            }, 300, function() {
                localDoc.getElementById('registerButton').removeChild(localDoc.getElementById('button' + id));
            });
        }
    };

    /**
     * Allows a user to register for a class.
     */
    CourseSketch.classSearch.registerClass = function(id) {

        CourseSketch.dataListener.sendDataInsert(CourseSketch.prutil.ItemQuery.REGISTER, courseProtoMap[id].toArrayBuffer(), function(evt, item) {
            if (isException(item)) {
                var exception = new DatabaseException('registration failed for course parent.getCurrentId()', '', item);
                CourseSketch.clientException(exception);
            } else {
                alert('Registration successful');
            }
            $('#' + id).animate({
                marginLeft: '0px'
            }, 300, function() {
                localDoc.getElementById('registerButton').removeChild(localDoc.getElementById('button' + id));
            });
        });
    };

    /**
     * Once everything is loaded we will ask to get all public courses.
     */
    $(document).ready(function() {
        CourseSketch.dataManager.searchCourses(searchCallback);
    });
})();
