function LectureDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
	var localScope = parent;
	var ByteBuffer = buffer;
	
	function setLecture (lecture, lectureCallback) {
		database.putInLecture(lecture.id, lecture.toBase64(), function (e, request) {
			if (lectureCallback) {
				lectureCallback (e, request);
			}
		});
	};
	parent.setLecture = setLecture;
	
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
				lectureCallback(CourseSketch.PROTOBUF_UTIL.getSrlLectureClass().decode(bytes));
			}
		});
	}
	parent.getLecture = getLecture;
	
	function getCourseLecture (lectureId, lectureCallback) {
		getLectures([lectureId], function (lectureList) {
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
						lectureIdsNotFound.push(lecture);
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
							sendData.sendDataRequest (CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM, lectureIdsNotFound);
						}
						if (lectureList.length > 0) {
							lectureCallback (lectureList);
						}
					}
				});
			}
			forLoopBlock (currentLectureId);
		}
	};
	parent.getCourseLectures = getCourseLectures;
}