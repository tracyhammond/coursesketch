function LectureDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	
	*/
	function setLectureLocal(lecture, lectureCallback) {
		database.putInLectures(lecture.id, lecture.toBase64(), function(e, request) {
			if(lectureCallback) {
				lectureCallback(e,request);
			}
		});
	}
	
	function setLectureServer(lecture, lectureCallback) {
		lectureCallback();
		// TODO Function stub
	}
	
	function insertLecture(lecture, localCallback, serverCallback) {
		setLectureLocal(lecture, localCallback);
		setLectureServer(lecture, function() {
						 var course = parent.getCourse(lecture.courseId);
						 var lectureList = course.lectureList;
						 lectureList.push(lecture.id);
						 course.lectureList = lectureList;
						 parent.setCourse(course);
		});
	}
	parent.insertLecture = insertLecture;
}