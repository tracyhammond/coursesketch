validateFirstRun(document.currentScript);
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
	var idList1= CourseSketch.prutil.SrlProblem();
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
		var assignmentIdNumber = i * 100 + 1000;
		// problem id fixing
		idLists[i].id = '' + (i * 10 + assignmentIdNumber);
		idLists[i].assignmentId = '' + assignmentIdNumber;
		idLists[i].courseId = '' + Math.ceil((i + 1) / 2);

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

        idLists[i].subgroups = [problemHolder1, problemHolder2];

		// add them as valid problems as seen by the database
		CourseSketch.fakeProblems.push(idLists[i]);
	}

	lecture1.id = '1000';
	lecture1.courseId = '1';
	lecture1.name = 'Test lecture title!';
	lecture1.description = 'I am a test lecture description!';
	lecture1.problemGroups = [ idList1.id ];
	CourseSketch.fakeLectures.push(lecture1);

	lecture2.id = '1100';
	lecture2.courseId = '1';
	lecture2.name = 'Another test lecture!';
	lecture2.description = 'Awesome sauce!';
	lecture2.problemGroups = [ idList2.id ];
	CourseSketch.fakeLectures.push(lecture2);

	lecture3.id = '1200';
	lecture3.courseId = '2';
	lecture3.name = 'How to Use CourseSketch Lectures';
	lecture3.description = 'Learn how to make a super awesome CourseSketch lecture!';
	lecture3.problemGroups = [ idList3.id ];
	CourseSketch.fakeLectures.push(lecture3);

	lecture4.id = '1300';
	lecture4.courseId = '2';
	lecture4.name = 'CourseSketch Tutorials';
	lecture4.description = 'Learn how to make a super awesome CourseSketch tutorials!';
	lecture4.problemGroups = [ idList4.id ];
	CourseSketch.fakeLectures.push(lecture4);

	lecture5.id = '1400';
	lecture5.courseId = '3';
	lecture5.name = 'Quantum Entanglement Communicators';
	lecture5.description = 'FTL communication is a neccesity. This class will teach you the basic workings of a quantum entanglement communicator.';
	lecture5.problemGroups = [ idList5.id ];
	CourseSketch.fakeLectures.push(lecture5);

	lecture6.id = '1500';
	lecture6.courseId = '3';
	lecture6.name = 'Rocket Physics';
	lecture6.description = 'The hardest physics class you will ever take. Your brain will not be able to handle this.';
	lecture6.problemGroups = [ idList6.id ];
	CourseSketch.fakeLectures.push(lecture6);

	lecture7.id = '1600';
	lecture7.courseId = '4';
	lecture7.name = 'Introduction to Basket Weaving';
	lecture7.description = 'Before you can make a basket underwater, you must first make a basket above water.';
	lecture7.problemGroups = [ idList7.id ];
	CourseSketch.fakeLectures.push(lecture7);

	lecture8.id = '1700';
	lecture8.courseId = '4';
	lecture8.name = 'Hydronics of Basket Weaving';
	lecture8.description = 'This class will teach you one of the basic challenges behind weaving baskets underwater: hydronics.';
	lecture8.problemGroups = [ idList8.id ];
	CourseSketch.fakeLectures.push(lecture8);

	lecture9.id = '1800';
	lecture9.courseId = '5';
	lecture9.name = 'Using the Fake Page';
	lecture9.description = 'Learn how to use fake pages!';
	lecture9.problemGroups = [ idList9.id ];
	CourseSketch.fakeLectures.push(lecture9);

	lecture10.id = '1900';
	lecture10.courseId = '5';
	lecture10.name = 'The Data Manager';
	lecture10.description = 'Learn how to use the CourseSketch Data Manager!';
	lecture10.problemGroups = [ idList10.id ];
	CourseSketch.fakeLectures.push(lecture10);

	for (var i = 0; i < CourseSketch.fakeLectures.length; i++) {
		CourseSketch.fakeLectures[i].setAssignmentType(CourseSketch.prutil.AssignmentType.LECTURE);
	}

	// FORMAT FOR THIS LECTURE
	// GROUP 1
	// SLIDE
	// Lecture 1
	// SLIDE
	// GROUP 2
	// SLIDE
	// Lecture 2
	// Lecture 3
	// SLIDE
	// GROUP 3
	// Lecture 1
	// SLIDE
	// Lecture 2
	// GROUP 4
	// SLIDE
	var lecture11 = CourseSketch.prutil.SrlAssignment();
	lecture11.id = '2000';
	lecture11.courseId = '1';
	lecture11.name = 'The complicated lecture';
	lecture11.description = 'This complicated lecture holds other lectures';

	// GROUP 1
	var group1 = CourseSketch.prutil.SrlProblem();
	group1.id = '210';


	// GROUP 2
	var group2 = CourseSketch.prutil.SrlProblem();
	group2.id = '220';

	// GROUP 3
	var group3 = CourseSketch.prutil.SrlProblem();
	group3.id = '230';


	// GROUP 4
	var group4 = CourseSketch.prutil.SrlProblem();
	group4.id = '240';

})();
