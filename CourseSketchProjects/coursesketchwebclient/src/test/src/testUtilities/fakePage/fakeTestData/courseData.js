(function(){
	CourseSketch.fakeCourses = [];

	var course1 = CourseSketch.PROTOBUF_UTIL.SrlCourse();
	var course2 = CourseSketch.PROTOBUF_UTIL.SrlCourse();
	var course3 = CourseSketch.PROTOBUF_UTIL.SrlCourse();
	var course4 = CourseSketch.PROTOBUF_UTIL.SrlCourse();
	var course5 = CourseSketch.PROTOBUF_UTIL.SrlCourse();

	course1.id = "1";
	course1.name = "This is a test item!";
	course1.description = "This is a description. It is not quite as big as the title.";
	course1.lectureList = ["1", "2"];
	CourseSketch.fakeCourses.push(course1);

	course2.id = "2";
	course2.name = "CourseSketch 101";
	course2.description = "Take this class to learn more about CourseSketch! We will guide you through the basics to get you up and running quickly.";
	course2.lectureList = ["3", "4"];
	CourseSketch.fakeCourses.push(course2);

	course3.id = "3";
	course3.name = "Quantum Rocket Science 1337";
	course3.description = "This is the hardest class that is ever taught anywhere. Unless you are at least as smart as Albert Einstein, you will definitely fail. Topics include: time travel, hyperdrive design, and quantum entanglement communication.";
	course3.lectureList = ["5", "6"];
	CourseSketch.fakeCourses.push(course3);
	
	course4.id = "4";
	course4.name = "Underwater Basket Weaving 220";
	course4.description = "Learn how to weave a basket underwater!";
	course4.lectureList = ["7", "8"];
	CourseSketch.fakeCourses.push(course4);

	course5.id = "5";
	course5.name = "ENGR 491";
	course5.description = "The super-awesome CourseSketch research course!";
	course5.lectureList = ["9", "10"];
	CourseSketch.fakeCourses.push(course5);
}
)();