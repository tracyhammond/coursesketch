(function() {
	CourseSketch.fakeLectures = [];

	var lecture1 = CourseSketch.prutil.SrlAssignment();
	var lecture2 = CourseSketch.prutil.SrlAssignment();
	var lecture3 = CourseSketch.prutil.SrlAssignment();
	var lecture4 = CourseSketch.prutil.SrlAssignment();
	var lecture5 = CourseSketch.prutil.SrlAssignment();
	var lecture6 = CourseSketch.prutil.SrlAssignment();
	var lecture7 = CourseSketch.prutil.SrlAssignment();
	var lecture8 = CourseSketch.prutil.SrlAssignment();
	var lecture9 = CourseSketch.prutil.SrlAssignment();
	var lecture10 = CourseSketch.prutil.SrlAssignment();

	var slides = CourseSketch.fakeSlides;

	var idLists = [];
	var idList1 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList1);

	var idList2 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList2);

	var idList3 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList3);

	var idList4 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList4);

	var idList5 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList5);

	var idList6 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList6);

	var idList7 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList7);

	var idList8 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList8);

	var idList9 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList9);

	var idList10 = CourseSketch.prutil.SrlProblem();
	idLists.push(idList10);

	for (var i = 0; i < idLists.length; i++) {
		// problem id fixing
		idLists[i].id = '' + (i * 10 + 1);
		idLists[i].assignmentId = '' + (i * 10 + 1);
		idLists[i].courseId = '' + Math.ceil((i + 1) / 2);
		console.log('idlist course id', idLists[i].courseId);

		// slide id fixing
		slides[i * 2].assignmentId = idLists[i].assignmentId;
		slides[i * 2 + 1].assignmentId = idLists[i].assignmentId;

		slides[i * 2].courseProblemId = idLists[i].id;
		slides[i * 2 + 1].courseProblemId = idLists[i].id;

		var problemHolder1 = CourseSketch.prutil.ProblemSlideHolder();
		var problemHolder2 = CourseSketch.prutil.ProblemSlideHolder();

		problemHolder1.id = slides[i * 2].id;
		problemHolder2.id = slides[i * 2 + 1].id;

		problemHolder1.itemType = CourseSketch.prutil.ItemType.SLIDE;
		problemHolder2.itemType = CourseSketch.prutil.ItemType.SLIDE;

		problemHolder1.unlocked = true;
		problemHolder1.unlocked = true;

		// add them as valid problems as seen by the database
		CourseSketch.fakeProblems.push(idLists[i]);
	}

	lecture1.id = '11';
	lecture1.courseId = '1';
	lecture1.name = 'Test lecture title!';
	lecture1.description = 'I am a test lecture description!';
	lecture1.idList = idList1;
	CourseSketch.fakeLectures.push(lecture1);

	lecture2.id = '21';
	lecture2.courseId = '1';
	lecture2.name = 'Another test lecture!';
	lecture2.description = 'Awesome sauce!';
	lecture2.idList = idList2;
	CourseSketch.fakeLectures.push(lecture2);

	lecture3.id = '31';
	lecture3.courseId = '2';
	lecture3.name = 'How to Use CourseSketch Lectures';
	lecture3.description = 'Learn how to make a super awesome CourseSketch lecture!';
	lecture3.idList = idList3;
	CourseSketch.fakeLectures.push(lecture3);

	lecture4.id = '41';
	lecture4.courseId = '2';
	lecture4.name = 'CourseSketch Tutorials';
	lecture4.description = 'Learn how to make a super awesome CourseSketch tutorials!';
	lecture4.idList = idList4;
	CourseSketch.fakeLectures.push(lecture4);

	lecture5.id = '51';
	lecture5.courseId = '3';
	lecture5.name = 'Quantum Entanglement Communicators';
	lecture5.description = 'FTL communication is a neccesity. This class will teach you the basic workings of a quantum entanglement communicator.';
	lecture5.idList = idList5;
	CourseSketch.fakeLectures.push(lecture5);

	lecture6.id = '61';
	lecture6.courseId = '3';
	lecture6.name = 'Rocket Physics';
	lecture6.description = 'The hardest physics class you will ever take. Your brain will not be able to handle this.';
	lecture6.idList = idList6;
	CourseSketch.fakeLectures.push(lecture6);

	lecture7.id = '71';
	lecture7.courseId = '4';
	lecture7.name = 'Introduction to Basket Weaving';
	lecture7.description = 'Before you can make a basket underwater, you must first make a basket above water.';
	lecture7.idList = idList7;
	CourseSketch.fakeLectures.push(lecture7);

	lecture8.id = '81';
	lecture8.courseId = '4';
	lecture8.name = 'Hydronics of Basket Weaving';
	lecture8.description = 'This class will teach you one of the basic challenges behind weaving baskets underwater: hydronics.';
	lecture8.idList = idList8;
	CourseSketch.fakeLectures.push(lecture8);

	lecture9.id = '91';
	lecture9.courseId = '5';
	lecture9.name = 'Using the Fake Page';
	lecture9.description = 'Learn how to use fake pages!';
	lecture9.idList = idList9;
	CourseSketch.fakeLectures.push(lecture9);

	lecture10.id = '101';
	lecture10.courseId = '5';
	lecture10.name = 'The Data Manager';
	lecture10.description = 'Learn how to use the CourseSketch Data Manager!';
	lecture10.idList = idList10;
	CourseSketch.fakeLectures.push(lecture10);

	for (var i = 0; i < CourseSketch.fakeLectures.length; i++) {
		CourseSketch.fakeLectures[i].setAssignmentType(CourseSketch.prutil.AssignmentType.LECTURE);
	}
})();
