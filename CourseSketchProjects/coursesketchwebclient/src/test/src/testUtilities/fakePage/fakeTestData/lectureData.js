(function() {
	CourseSketch.fakeLectures = [];
	
	var lecture1 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture2 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture3 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture4 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture5 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture6 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture7 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture8 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture9 = CourseSketch.PROTOBUF_UTIL.Lecture();
	var lecture10 = CourseSketch.PROTOBUF_UTIL.Lecture();

	lecture1.id = "1";
	lecture1.courseId = "1";
	lecture1.name = "Test lecture title!";
	lecture1.description = "I am a test lecture description!";
	CourseSketch.fakeLectures.push(lecture1);

	lecture2.id = "2";
	lecture2.courseId = "1";
	lecture2.name = "Another test lecture!";
	lecture2.description = "Awesome sauce!";
	CourseSketch.fakeLectures.push(lecture2);

	lecture3.id = "3";
	lecture3.courseId = "2";
	lecture3.name = "How to Use CourseSketch Lectures";
	lecture3.description = "Learn how to make a super awesome CourseSketch lecture!";
	CourseSketch.fakeLectures.push(lecture3);
	
	lecture4.id = "4";
	lecture4.courseId = "2";
	lecture4.name = "CourseSketch Tutorials";
	lecture4.description = "Learn how to make a super awesome CourseSketch tutorials!";
	CourseSketch.fakeLectures.push(lecture4);

	lecture5.id = "5";
	lecture5.courseId = "3";
	lecture5.name = "Quantum Entanglement Communicators";
	lecture5.description = "FTL communication is a neccesity. This class will teach you the basic workings of a quantum entanglement communicator.";
	CourseSketch.fakeLectures.push(lecture5);

	lecture6.id = "6";
	lecture6.courseId = "3";
	lecture6.name = "Rocket Physics";
	lecture6.description = "The hardest physics class you will ever take. Your brain will not be able to handle this.";
	CourseSketch.fakeLectures.push(lecture6);

	lecture7.id = "7";
	lecture7.courseId = "4";
	lecture7.name = "Introduction to Basket Weaving";
	lecture7.description = "Before you can make a basket underwater, you must first make a basket above water.";
	CourseSketch.fakeLectures.push(lecture7);

	lecture8.id = "8";
	lecture8.courseId = "4";
	lecture8.name = "Hydronics of Basket Weaving";
	lecture8.description = "This class will teach you one of the basic challenges behind weaving baskets underwater: hydronics.";
	CourseSketch.fakeLectures.push(lecture8);
	
	lecture9.id = "9";
	lecture9.courseId = "5";
	lecture9.name = "Using the Fake Page";
	lecture9.description = "Learn how to use fake pages!";
	CourseSketch.fakeLectures.push(lecture9);

	lecture10.id = "10";
	lecture10.courseId = "5";
	lecture10.name = "The Data Manager";
	lecture10.description = "Learn how to use the CourseSketch Data Manager!";
	CourseSketch.fakeLectures.push(lecture10);
})();