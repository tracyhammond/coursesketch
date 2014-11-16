function SlideDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	 * Sets a slide in the local database
	 * 
	 * @param slide is a slide object
	 * @param slideCallback function to be called after the slide setting is done
	 */
	function setSlideLocal(slide, slideCallback) {
		database.putInSlides(slide.id, slide.toBase64(), function(e, request) {
			if(slideCallback) {
				slideCallback(e,request);
			}
		});
	}
	parent.setSlideLocal = setSlideLocal;
	
	/**
	 * Sets a slide in the server database
	 *
	 * @param slide is a slide object
	 * @param slideCallback function to be called after the slide setting is done
	 */
	function setSlideServer(slide, slideCallback) {
		slideCallback();
		// TODO Function stub
	}
	
	/**
	 * Adds a new slide to both local and server databases. Also updates the
	 * corresponding slide given by the lecture's courseId.
	 *
	 * @param slide slide object to insert
	 * @param localCallback function to be called after local insert is done
	 * @param serverCallback function to be called after server insert is done
	 */
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
	
	/**
	 * Deletes a slide from local database.
	 * 
	 * @param slideId ID of the lecture to delete
	 * @param lectureCallback function to be called after the deletion is done
	 */
	function deleteSlide (slideId, slideCallback) {
		database.deleteFromSlides (slideId, function (e, request) {
			if (slideCallback) {
				slideCallback (e, request);
			}
		});
	};
	parent.deleteSlide = deleteSlide;
	
	/**
	 * Gets a slide from the local database.
	 *
	 * @param slideId ID of the slide to get
	 * @param slideCallback function to be called after getting is complete,
	 * paramater is the slide object
	 */
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
	
	/**
	 * Gets a slide from the local and server databases.
	 *
	 * @param slideId ID of the slide to get
	 * @param slideCallback function to be called after getting is complete,
	 * paramater is the slide object
	 */
	function getLectureSlide (slideId, slideCallback) {
		getLectureSlides([slideId], function (slideList) {
			slideCallback (slideList[0]);
		});
	};
	parent.getLectureSlide = getLectureSlide;
	
	/**
	 * Gets a list of slides from the local and server databases.
	 *
	 * @param slideIds IDs of the slides to get
	 * @param slideCallback function to be called after getting is complete,
	 * paramater is a list of slide objects
	 */
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