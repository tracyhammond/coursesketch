function LectureDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	 * Sets a lecture in local database.
	 *
	 * @param lecture lecture object to set
	 * @param lectureCallback function to be called after the lecture setting is done
	 */
	function setLectureLocal(lecture, lectureCallback) {
		database.putInLectures(lecture.id, lecture.toBase64(), function(e, request) {
			if(lectureCallback) {
				lectureCallback(e,request);
			}
		});
	}
	parent.setLectureLocal = setLectureLocal;
	
	/**
	 * Sets a lecture in server database.
	 *
	 * @param lecture lecture object to set
	 * @param lectureCallback function to be called after lecture setting is done
	 */
	function setLectureServer(lecture, lectureCallback) {
		lectureCallback();
		// TODO Function stub
	}
	
	/**
	 * Adds a new lecture to both local and server databases. Also updates the
	 * corresponding course given by the lecture's courseId.
	 *
	 * @param lecture lecture object to insert
	 * @param localCallback function to be called after local insert is done
	 * @param serverCallback function to be called after server insert is done
	 */
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
	
	/**
	 * Deletes a lecture from local database.
	 * 
	 * @param lectureId ID of the lecture to delete
	 * @param lectureCallback function to be called after the deletion is done
	 */
	function deleteLecture (lectureId, lectureCallback) {
		database.deleteFromLectures (lectureId, function (e, request) {
			if (lectureCallback) {
				lectureCallback (e, request);
			}
		});
	};
	parent.deleteLecture = deleteLecture;
	
	/**
	 * Gets a lecture from the local database.
	 *
	 * @param lectureId ID of the lecture to get
	 * @param lectureCallback function to be called after getting is complete,
	 * paramater is the lecture object
	 */
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

	parent.getLectureLocal = getLectureLocal;
	
	/**
	 * Gets a lecture from the local and server databases.
	 *
	 * @param lectureId ID of the lecture to get
	 * @param lectureCallback function to be called after getting is complete,
	 * paramater is the lecture object
	 */
	function getCourseLecture (lectureId, lectureCallback) {
		getCourseLectures([lectureId], function (lectureList) {
			lectureCallback (lectureList[0]);
		});
	};
	parent.getCourseLecture = getCourseLecture;
	
	/**
	 * Gets a list of lectures from the local and server databases.
	 *
	 * @param lectureIds IDs of the lectures to get
	 * @param lectureCallback function to be called after getting is complete,
	 * paramater is a list of lecture objects
	 */
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

	function setSlideLocal(slide, slideCallback) {
		database.putInSlides(slide.id, slide.toBase64(), function(e, request) {
			if(slideCallback) {
				slideCallback(e,request);
			}
		});
	}
	parent.setSlideLocal = setSlideLocal;
	
	function setSlideServer(slide, slideCallback) {
		slideCallback();
		// TODO Function stub
	}
	
	function insertSlide(slide, localCallback, serverCallback) {
		setSlideLocal(slide, localCallback);
		setSlideServer(slide, function() {
			parent.getCourseLecture(slide.lectureId, function(lecture) {
				var slideList = lecture.slides;
				slideList.push(slide.id);
				lecture.slideList = slideList;
				parent.setLecture(lecture, function() {
					if(!isUndefined(serverCallback)) {
						serverCallback(course);
					}
				});
			});
		});
	}
	parent.insertSlide = insertSlide;
	
	function deleteSlide (slideId, lectureCallback) {
		database.deleteFromSlides (slideId, function (e, request) {
			if (slideCallback) {
				slideCallback (e, request);
			}
		});
	};
	parent.deleteSlide = deleteSlide;
	
	function getSlideLocal (slideId, slideCallback) {
		database.getFromSlides(slideId, function (e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				slideCallback (undefined);
			} else if (result.data == nonExistantValue) {
				slideCallback (nonExistantValue);
			} else {
				var bytes = ByteBuffer.fromBase64(result.data);
				if(slideCallback)
					slideCallback(CourseSketch.PROTOBUF_UTIL.getSlideClass().decode(bytes));
			}
		});
	}
	parent.getSlideLocal = getSlideLocal;
	
	function getLectureSlide (slideId, slideCallback) {
		getLectureSlides([slideId], function (slideList) {
			slideCallback (slideList[0]);
		});
	};
	parent.getLectureSlide = getLectureSlide;
	
	function getLectureSlides (slideIds, slideCallback) {
		if (isUndefined (slideIds) || slideIds == null || slideIds.length == 0) {
			slideCallback (nonExistantValue);
		}
		var barrier = slideIds.length;
		var slidesFound = [];
		var slideIdsNotFound = [];
		for (var i = 0; i < slideIds.length; i++) {
			var currentSlideId = slideIds[i];
			function forLoopBlock (slideId) {
				getSlideLocal (slideId, function (lecture) {
					if (!isUndefined (slide)) {
						slidesFound.push(slide);
					} else {
						slideIdsNotFound.push(slideId);
					}
					barrier -= 1;
					if (barrier == 0) {
						if (slideIdsNotFound.length >= 1) {
							advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SLIDE, function (evt, item) {
								var school = CourseSketch.PROTOBUF_UTIL.getSrlLectureDataHolderClass().decode(item.data);
								var slide = school.slides[0];
								if (isUndefined (slide)) {
									lectureCallback (slide);
									advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SLIDE);
									return;
								}
								for (var i = 0; i < school.slides.length; i++) {
									localScope.setSlide(school.slides[i]);
									slidesFound.push(school.slides[i]);
								}
								slideCallback (slidesFound);
								advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.SLIDE);
							});
							sendData.sendDataRequest (CourseSketch.PROTOBUF_UTIL.ItemQuery.SLIDE, slideIdsNotFound);
						}
						if (slidesFound.length > 0) {
							slideCallback (slidesFound);
						}
					}
				});
			}
			forLoopBlock (currentSlideId);
		}
	};
	parent.getLectureSlides = getLectureSlides;
	/*
	function insertElement(element, localCallback, serverCallback){
		addElementLocal(element, localCallback);
		addElementServer(element, function() {
			parent.getLectureSlide(lectureElement.slideId, function(lectureSlide) {
				var elementList = lectureSlide.elements;
				elementList.push(element);
				lectureSlide.elements = elementList;
				parent.setLectureSlide(lectureSlide, function() {
					if(!isUndefined(serverCallback)) {
						serverCallback(lectureSlide);
					}
				});
			});
		});
	}
	parent.insertElement = insertElement;
	*/
}