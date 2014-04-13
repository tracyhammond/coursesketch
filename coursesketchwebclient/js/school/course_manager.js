var waitingIcon = (function() {
	var manage = new WaitScreenManager();
	manage.waitIconText = "loading data";
	return manage.setWaitType(manage.TYPE_WAITING_ICON).build();
})();

function inializeCourseManagment() {
	document.getElementById('class_list_column').appendChild(waitingIcon);
	waitingIcon.startWaiting();

	var loadCourses = function(courseList) {
		if (waitingIcon.isRunning()) {
			waitingIcon.finishWaiting();
		}
		localScope.showCourses(courseList);
	};
	if (parent.dataManager.isDatabaseReady()) {
		parent.dataManager.pollUpdates(function () {
			parent.dataManager.getAllCourses(loadCourses);
		});
	} else {
		var intervalVar = setInterval(function() {
			if (parent.dataManager.isDatabaseReady()) {
				clearInterval(intervalVar);
				parent.dataManager.pollUpdates(function () {
					parent.dataManager.getAllCourses(loadCourses);
				});
			}
		}, 100);
	}
}

this.showCourses = function showCourses(courseList) {
	var builder = new SchoolItemBuilder();
	builder.setList(courseList).setWidth('medium').centerItem(true);
	if (parent.dataManager.getState("isInstructor")) {
		builder.setInstructorCard(true);
	}
	builder.showImage = false;
	builder.setBoxClickFunction(courseClickerFunction);
	builder.build('class_list_column');
	clearLists(2);
};

function courseClickerFunction(course) {
	clearLists(2);

	changeSelection(course.id, courseSelectionManager);
	assignmentSelectionManager.clearAllSelectedItems();
	problemSelectionManager.clearAllSelectedItems();

	// waiting icon
	document.getElementById('assignment_list_column').appendChild(waitingIcon);
	waitingIcon.startWaiting();

	//we can make this faster because we have the list of assignments
	parent.dataManager.getAssignments(course.assignmentList, function(assignmentList) {
		var builder = new SchoolItemBuilder();
		builder.setEmptyListMessage('There are no assignments for this course!');
		if (assignmentList == "NONEXISTANT_VALUE") {
			if (!course.getState().accessible) {
				builder.setEmptyListMessage('This course is currently not avialable. Please contact the instructor to let you view the assignments');
			}
			assignmentList = [];
		}

		builder.setList(assignmentList).setWidth('medium').centerItem(true);
		builder.showImage = false;
		
		builder.setBoxClickFunction(assignmentClickerFunction);
		if (waitingIcon.isRunning()) {
			waitingIcon.finishWaiting(); // stops the waiting icon
		}
		builder.build('assignment_list_column');
		if (parent.dataManager.getState("isInstructor")) {
			try {
				replaceIframe('html/instructor/course_managment_frames/edit_course.html');
			} catch(exception) {
				
			}
			showButton('assignment_button');
		}
	});
}
	parent.dataManager.insertCourse

function assignmentClickerFunction(assignment) {
	// clears the problems
	changeSelection(assignment.id, assignmentSelectionManager);
	problemSelectionManager.clearAllSelectedItems();
	clearLists(1);

	// waiting icon
	document.getElementById('problem_list_column').appendChild(waitingIcon);
	waitingIcon.startWaiting();
	console.log(assignment.problemList);
	parent.dataManager.getCourseProblems(assignment.problemList, function(problemList) {
		var builder = new SchoolItemBuilder();
		builder.setEmptyListMessage('There are no problems for this assignment!');
		if (problemList == "NONEXISTANT_VALUE") {
			problemList = [];
			if (!assignment.getState().accessible) {
				builder.setEmptyListMessage('This assignment is currently not avialable. Please contact the instructor to let you view the problems');
			}
		}
		for (var i = 0; i < problemList.length; i++) {
			var q = problemList[i].description;
			if (isUndefined(q) || q == "") {
				var prob = problemList[i];
				var problem = prob.problemInfo;
				if (!isUndefined(prob.problemInfo)) {
					var text = prob.getProblemInfo().getQuestionText();
					problemList[i].setDescription(text);
				} else {
					problemList[i].setDescription("No Description or question text");
				}
			}
		}
		builder.setList(problemList).setWidth('medium').centerItem(true);
		builder.showImage = false;
		builder.setBoxClickFunction(problemClickerFunction);
		if (waitingIcon.isRunning()) {
			waitingIcon.finishWaiting(); // stops the waiting icon
		}
		builder.build('problem_list_column');
		if (parent.dataManager.getState("isInstructor")) {
			try {
				replaceIframe('html/instructor/course_managment_frames/edit_assignment.html');
			} catch(exception) {
				
			}
			showButton('problem_button');
		}
	});
}

function problemClickerFunction(problem) {
	var id = problem.id;
	if (problemSelectionManager.isItemSelected(id)) {
		var element = document.getElementById(id);
		var itemNumber = element.dataset.item_number;
		parent.dataManager.addState("CURRENT_QUESTION_INDEX", itemNumber);
		parent.dataManager.addState("CURRENT_ASSIGNMENT", problem.assignmentId);
		parent.dataManager.addState("CURRENT_QUESTION", id);
		// change source to the problem page! and load problem
		if (parent.dataManager.getState("isInstructor")) {
			// solution editor page!
			parent.redirectContent("html/instructor/instructorproblemlayout.html", "");
		} else {
			parent.redirectContent("html/student/problemlayout.html", "Starting Problem");
		}
	}
	else {
		var element = document.getElementById(id);
		var myOpenTip = new Opentip(element, { target: element, tipJoint: "bottom" });
		myOpenTip.prepareToShow(); // Shows the tooltip after the given delays. This could get interrupted

		if (parent.dataManager.getState("isInstructor")) {
			myOpenTip.setContent("Click again to edit the solution"); // Updates Opentips content
		} else {
			myOpenTip.setContent("Click again to open up a problem"); // Updates Opentips content
		}

		var pastToolTip = problemSelectionManager['currentToolTip'];
		if (pastToolTip) {
			pastToolTip.deactivate();
		}
		problemSelectionManager['currentToolTip'] = myOpenTip;
		changeSelection(id, problemSelectionManager);
	}

	if (parent.dataManager.getState("isInstructor")) {
		try {
			replaceIframe('html/instructor/course_managment_frames/edit_problem.html');
		} catch(exception) {
			
		}
		showButton('problem_button');
	}
}

function showButton(id) {
	var element = document.getElementById(id);
	if (element) {
		element.style.display = "block";
	}
}

function hideButton(id) {
	var element = document.getElementById(id);
	if (element) {
		element.style.display = "none";
	}
}

function clearLists(number) {
	var builder = new SchoolItemBuilder();
	
	if(number>0) {
		hideButton('problem_button');
		builder.setEmptyListMessage('Please select an assignment to see the list of problems.');
		builder.build('problem_list_column');
	}
	if(number>1) {
		hideButton('assignment_button');
		builder.setEmptyListMessage('Please select a course to see the list of assignments.');
		builder.build('assignment_list_column');
	}
}

function changeSelection(id, selectionManager) {
	selectionManager.clearAllSelectedItems();
	selectionManager.addSelectedItem(id);
}

function manageHeight() {
	var iframe = document.getElementById('edit_frame_id');
	var innerDoc = iframe.contentDocument || iframe.contentWindow.document;
	// Gets the visual height.
	if(innerDoc) {
		var iFrameElement = innerDoc.getElementById('iframeBody') || innerDoc.getElementsByTagName('body')[0];
		if(!iFrameElement) {
			return;
		}
		var height = iFrameElement.scrollHeight;
		iframe.height = height;
	}
}

/**
	Given the source this will create an iframe that will manage its own height.
	TODO: make this more general.
*/
function replaceIframe(src) {
	var toReplace = document.getElementById('editable_unit');
	if (src && toReplace && toReplace != null) {
		toReplace.innerHTML =  '<Iframe id="edit_frame_id" src="'+ src +'" width = 100% ' +
		'sanbox = "allow-same-origin allow-scripts"' +
		'seamless = "seamless" onload="manageHeight()">';
	} else {
		toReplace.innerHTML = '<h2 style = "text-align:center">Nothing is selected yet</h2>' +
		'<h2 style = "text-align:center">Click an item to edit</h2>';
	}
}

function addNewCourse() { // Functionality to allow for adding of courses by instructors
	var course = new SrlCourse();
	//course.id = "Course_01";
	course.name = "Physics";
	course.description = "Physics is Phun";
	//course.semester = "Should be in format: '_F13' (_F = Fall, Sp = Spring, Su = Summer) ";
	//course.accessDate = "mm/dd/yyyy";
	//course.closeDate = "mm/dd/yyyy";
	var newCourse = insertCourse(course);
	
	/**course.id = 
	course.name = **/
	showCourses([course]);
	insertCourse([course]);
	//var newCourse = insertCourse(course);

	/**alert("Hello! I am an alert box!!");
	document.getElementById("demo");**/
}

var courseSelectionManager = new clickSelectionManager();
var assignmentSelectionManager = new clickSelectionManager();
var problemSelectionManager = new clickSelectionManager();