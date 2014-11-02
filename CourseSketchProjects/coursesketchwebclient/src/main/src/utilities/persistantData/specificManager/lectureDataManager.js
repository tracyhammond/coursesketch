function LectureDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
	var localScope = parent;
	var ByteBuffer = buffer;

	function setLectureLocal(lecture, lectureCallback) {
		database.putInLectures(lecture.id, lecture.toBase64(), function(e, request) {
			if(lectureCallback) {
				lectureCallback(e,request);
			}
		});
	}
	parent.setLectureLocal = setLectureLocal;
	
	function setLectureServer(lecture, lectureCallback) {
		lectureCallback();
		// TODO Function stub
	}
	
	function insertLecture(lecture, localCallback, serverCallback) {
		setLectureLocal(lecture, localCallback);
		setLectureServer(lecture, function() {
			parent.getCourse(lecture.courseId, function(course) {
				var lectureList = course.lectureList;
				lectureList.push(lecture.id);
				course.lectureList = lectureList;
				parent.setCourse(course, function() {
					if(!isUndefined(serverCallback)) {
						serverCallback(course);
					}
				});
			});
		});
	}
	parent.insertLecture = insertLecture;
	
	function deleteLecture (lectureId, lectureCallback) {
		database.deleteFromLectures (lectureId, function (e, request) {
			if (lectureCallback) {
				lectureCallback (e, request);
			}
		});
	};
	parent.deleteLecture = deleteLecture;
	
	function getLectureLocal (lectureId, lectureCallback) {
		database.getFromLectures(lectureId, function (e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				lectureCallback (undefined);
			} else if (result.data == nonExistantValue) {
				lectureCallback (nonExistantValue);
			} else {
				var bytes = ByteBuffer.fromBase64(result.data);
				if(lectureCallback)
					lectureCallback(CourseSketch.PROTOBUF_UTIL.getLectureClass().decode(bytes));
			}
		});
	}
	// parent.getLecture = getLecture;
	
	function getCourseLecture (lectureId, lectureCallback) {
		getCourseLectures([lectureId], function (lectureList) {
			lectureCallback (lectureList[0]);
		});
	};
	parent.getCourseLecture = getCourseLecture;
	
	function getCourseLectures (lectureIds, lectureCallback) {
		if (isUndefined (lectureIds) || lectureIds == null || lectureIds.length == 0) {
			lectureCallback (nonExistantValue);
		}
		var barrier = lectureIds.length;
		var lecturesFound = [];
		var lectureIdsNotFound = [];
		for (var i = 0; i < lectureIds.length; i++) {
			var currentLectureId = lectureIds[i];
			function forLoopBlock (lectureId) {
				getLectureLocal (lectureId, function (lecture) {
					if (!isUndefined (lecture)) {
						lecturesFound.push(lecture);
					} else {
						lectureIdsNotFound.push(lectureId);
					}
					barrier -= 1;
					if (barrier == 0) {
						if (lectureIdsNotFound.length >= 1) {
							advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, function (evt, item) {
								var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
								var lecture = school.lectures[0];
								if (isUndefined (lecture)) {
									lectureCallback (lecture);
									advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
									return;
								}
								for (var i = 0; i < school.lectures.length; i++) {
									localScope.setLecture(school.lectures[i]);
									lecturesFound.push(school.lectures[i]);
								}
								lectureCallback (lecturesFound);
								advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE);
							});
							sendData.sendDataRequest (CourseSketch.PROTOBUF_UTIL.ItemQuery.LECTURE, lectureIdsNotFound);
						}
						if (lecturesFound.length > 0) {
							lectureCallback (lecturesFound);
						}
					}
				});
			}
			forLoopBlock (currentLectureId);
		}
	};
	parent.getCourseLectures = getCourseLectures;
}