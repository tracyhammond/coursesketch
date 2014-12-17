(function() {
    /**
     * Once everything is loaded we will ask to get all public courses.
     */
    $(document).ready(function() {
        var request = CourseSketch.PROTOBUF_UTIL.DataRequest();
        var item = CourseSketch.PROTOBUF_UTIL.ItemRequest();
        item.query = CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_SEARCH;
        request.items = [item];
        CourseSketch.connection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(request,
            CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST));
    });
    var localDoc = document;
    var courseList1 = new Array();
    var courseList2 = new Array();
    var courseRightSide = {};
    var courseProtoMap = {};

    var setTimeVar;

    CourseSketch.dataListener.setListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
            CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_SEARCH, function(evt, item) {
        var school = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(item.data, CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass());
        var courseList = school.courses;
        var idList = [];
        for (var i = 0; i < courseList.length; i++) {
            courseProtoMap[courseList[i].id] = courseList[i];
            if (i % 2 == 0) {
                courseList1.push(courseList[i]);
                courseRightSide[courseList[i].id] = false;
            } else {
                courseList2.push(courseList[i]);
                courseRightSide[courseList[i].id] = true;
            }
        }
        var schoolItemBuilder = new SchoolItemBuilder();
            schoolItemBuilder.setList(courseList1).build("class_list_column1");
        if (courseList2.length > 0) {
            schoolItemBuilder.setList(courseList2).build("class_list_column2");
        }
        schoolItemBuilder.showImage = false; // till we have images actually working!
        schoolItemBuilder.setBoxClickFunction(CourseSketch.classSearch.courseClickerFunction);
        localDoc.getElementById("loadingIcon").style.display="none";
    });

    CourseSketch.dataListener.setErrorListener(function(msg){
        localDoc.getElementById("loadingIcon").innerHTML = '<h1>error loading data</h1> <p>' + msg.getResponseText() + '</p>';
        clearTimeout(setTimeVar);
    });

    CourseSketch.dataListener.setListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
            CourseSketch.PROTOBUF_UTIL.ItemQuery.REGISTER, function(evt, item) {
        alert("User is already registered for this course");
        clearTimeout(setTimeVar);
    });

    CourseSketch.classSearch.courseClickerFunction = function(course) {
        var id = course.id;
        var element = localDoc.getElementById(id);
        var width = element.offsetWidth/2;
        var moveAmount = width + "px";
        //console.log("style "  + element.style.marginLeft);
        if (element.style.marginLeft == "" || element.style.marginLeft == "0px") {
            if (courseRightSide[id]) {
                moveAmount = "" + moveAmount;
            } else {
                moveAmount = "-" + moveAmount;
            }
            var button = localDoc.createElement('button');
            button.setAttribute("id", "button"+id);
            button.onclick = function() {
                CourseSketch.classSearch.registerClass(id);
                setTimeVar = setTimeout(function () { alert("Your have successfully registered") }, 3000);
            }
            button.textContent = "Register";
            button.style.position = "absolute";
            if (courseRightSide[id]) {
                button.style.left = element.offsetLeft + "px";
            } else {
                button.style.left = (element.offsetLeft + element.offsetWidth - width/2) + "px";
            }
            button.style.top = element.offsetHeight/2 + element.offsetTop + "px";
            //localDoc.appendChild(button);
            localDoc.getElementById("registerButton").appendChild(button);
            $("#" + id).animate({
                marginLeft: moveAmount,
                }, 300, function () {

            });
        } else {
            $("#" + id).animate({
                marginLeft: "0px",
                }, 300, function () {
                    localDoc.getElementById("registerButton").removeChild(localDoc.getElementById("button" + id));
            });
        }
    }

    /**
     * Allows a user to register for a class.
     */
    CourseSketch.classSearch.registerClass = function(id) {
        var request = new QueryBuilder.DataSend();
        var item = new QueryBuilder.ItemSend();
        item.query = QueryBuilder.ItemQuery.REGISTER;
        item.data = courseProtoMap[id].toArrayBuffer();
        request.items = [item];
        CourseSketch.connection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(request, Request.MessageType.DATA_INSERT));
        $("#" + id).animate({
            marginLeft: "0px",
            }, 300, function () {
                localDoc.getElementById("registerButton").removeChild(localDoc.getElementById("button" + id));
        });
    };
})();
