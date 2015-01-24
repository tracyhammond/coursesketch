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

	var slideId1 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId2 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId3 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId4 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId5 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId6 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId7 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId8 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId9 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId10 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId11 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId12 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId13 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId14 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId15 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId16 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId17 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId18 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId19 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
	var slideId20 = CourseSketch.PROTOBUF_UTIL.IdsInLecture();

	slideId1.id = "1";
	slideId1.isSlide = true;

	slideId2.id = "2";
	slideId2.isSlide = true;

	slideId3.id = "3";
	slideId3.isSlide = true;

	slideId4.id = "4";
	slideId4.isSlide = true;

	slideId5.id = "5";
	slideId5.isSlide = true;

	slideId6.id = "6";
	slideId6.isSlide = true;

	slideId7.id = "7";
	slideId7.isSlide = true;

	slideId8.id = "8";
	slideId8.isSlide = true;

	slideId9.id = "9";
	slideId9.isSlide = true;

	slideId10.id = "10";
	slideId10.isSlide = true;

	slideId11.id = "11";
	slideId11.isSlide = true;

	slideId12.id = "12";
	slideId12.isSlide = true;

	slideId13.id = "13";
	slideId13.isSlide = true;

	slideId14.id = "14";
	slideId14.isSlide = true;

	slideId15.id = "15";
	slideId15.isSlide = true;

	slideId16.id = "16";
	slideId16.isSlide = true;

	slideId17.id = "17";
	slideId17.isSlide = true;

	slideId18.id = "18";
	slideId18.isSlide = true;

	slideId19.id = "19";
	slideId19.isSlide = true;

	slideId20.id = "20";
	slideId20.isSlide = true;

	var idList1 = [];
	idList1[0] = slideId1;
	idList1[1] = slideId2;

	var idList2 = [];
	idList2[0] = slideId3;
	idList2[1] = slideId4;

	var idList3 = [];
	idList3[0] = slideId5;
	idList3[1] = slideId6;

	var idList4 = [];
	idList4[0] = slideId7;
	idList4[1] = slideId8;

	var idList5 = [];
	idList5[0] = slideId9;
	idList5[1] = slideId10;

	var idList6 = [];
	idList6[0] = slideId11;
	idList6[1] = slideId12;

	var idList7 = [];
	idList7[0] = slideId13;
	idList7[1] = slideId14;

	var idList8 = [];
	idList8[0] = slideId15;
	idList8[1] = slideId16;

	var idList9 = [];
	idList9[0] = slideId17;
	idList9[1] = slideId18;

	var idList10 = [];
	idList10[0] = slideId19;
	idList10[1] = slideId20;

	lecture1.id = "1";
	lecture1.courseId = "1";
	lecture1.name = "Test lecture title!";
	lecture1.description = "I am a test lecture description!";
	lecture1.idList = idList1;
	CourseSketch.fakeLectures.push(lecture1);

	lecture2.id = "2";
	lecture2.courseId = "1";
	lecture2.name = "Another test lecture!";
	lecture2.description = "Awesome sauce!";
	lecture2.idList = idList2;
	CourseSketch.fakeLectures.push(lecture2);

	lecture3.id = "3";
	lecture3.courseId = "2";
	lecture3.name = "How to Use CourseSketch Lectures";
	lecture3.description = "Learn how to make a super awesome CourseSketch lecture!";
	lecture3.idList = idList3;
	CourseSketch.fakeLectures.push(lecture3);
	
	lecture4.id = "4";
	lecture4.courseId = "2";
	lecture4.name = "CourseSketch Tutorials";
	lecture4.description = "Learn how to make a super awesome CourseSketch tutorials!";
	lecture4.idList = idList4;
	CourseSketch.fakeLectures.push(lecture4);

	lecture5.id = "5";
	lecture5.courseId = "3";
	lecture5.name = "Quantum Entanglement Communicators";
	lecture5.description = "FTL communication is a neccesity. This class will teach you the basic workings of a quantum entanglement communicator.";
	lecture5.idList = idList5;
	CourseSketch.fakeLectures.push(lecture5);

	lecture6.id = "6";
	lecture6.courseId = "3";
	lecture6.name = "Rocket Physics";
	lecture6.description = "The hardest physics class you will ever take. Your brain will not be able to handle this.";
	lecture6.idList = idList6;
	CourseSketch.fakeLectures.push(lecture6);

	lecture7.id = "7";
	lecture7.courseId = "4";
	lecture7.name = "Introduction to Basket Weaving";
	lecture7.description = "Before you can make a basket underwater, you must first make a basket above water.";
	lecture7.idList = idList7;
	CourseSketch.fakeLectures.push(lecture7);

	lecture8.id = "8";
	lecture8.courseId = "4";
	lecture8.name = "Hydronics of Basket Weaving";
	lecture8.description = "This class will teach you one of the basic challenges behind weaving baskets underwater: hydronics.";
	lecture8.idList = idList8;
	CourseSketch.fakeLectures.push(lecture8);
	
	lecture9.id = "9";
	lecture9.courseId = "5";
	lecture9.name = "Using the Fake Page";
	lecture9.description = "Learn how to use fake pages!";
	lecture9.idList = idList9;
	CourseSketch.fakeLectures.push(lecture9);

	lecture10.id = "10";
	lecture10.courseId = "5";
	lecture10.name = "The Data Manager";
	lecture10.description = "Learn how to use the CourseSketch Data Manager!";
	lecture10.idList = idList10;
	CourseSketch.fakeLectures.push(lecture10);
})();
