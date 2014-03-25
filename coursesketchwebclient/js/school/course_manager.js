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

function courseClickerFunction(id) {
	clearLists(2);
	changeSelection(id, courseSelectionManager);
	assignmentSelectionManager.clearAllSelectedItems();
	problemSelectionManager.clearAllSelectedItems();

	console.log("LOADING THE ASSIGNMENTS TO DISPLAY!");

	//we get the list from the id.
	parent.dataManager.getAllAssignmentsFromCourse(id, function(assignmentList) {
		var builder = new SchoolItemBuilder();
		builder.setList(assignmentList).setWidth('medium').centerItem(true);
		builder.showImage = false;
		builder.setEmptyListMessage('There are no assignments for this course!');
		builder.setBoxClickFunction(assignmentClickerFunction);
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

function assignmentClickerFunction(id) {
	// clears the problems
	changeSelection(id, assignmentSelectionManager);
	problemSelectionManager.clearAllSelectedItems();
	clearLists(1);
	parent.dataManager.getAllProblemsFromAssignment(id, function(problemList) {
		for (var i = 0; i < problemList.length; i++) {
			var q = problemList[i].description;
			if (isUndefined(q) || q == "") {
				var prob = problemList[i];
				var text = prob.getProblemInfo().getQuestionText();
				problemList[i].setDescription(text);
			}
		}
		var builder = new SchoolItemBuilder();
		builder.setList(problemList).setWidth('medium').centerItem(true);
		builder.showImage = false;
		builder.setEmptyListMessage('There are no problems for this assignment!');
		builder.setBoxClickFunction(problemClickerFunction);
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

function problemClickerFunction(id) {
	if (problemSelectionManager.isItemSelected(id)) {
		var element = document.getElementById(id);
		var itemNumber = element.dataset.item_number;
		parent.dataManager.addState("CURRENT_QUESTION_INDEX", itemNumber);
		var assignment = parent.dataManager.getCourseProblem(id, function(problem) {
			parent.dataManager.addState("CURRENT_ASSIGNMENT", problem.assignmentId);
			parent.dataManager.addState("CURRENT_QUESTION", id);
			// change source to the problem page! and load problem
			if (parent.dataManager.getState("isInstructor")) {
				// solution editor page!
				parent.redirectContent("html/instructor/instructorproblemlayout.html", "");
			} else {
				parent.redirectContent("html/student/problemlayout.html", "Starting Problem");
			}
		});
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
	
	/**var courseEdit = document.getElementById('addNewCourse');
	courseEdit.innerHTML = '<textarea rows="4" cols="12">Please enter your courses description right here! </textarea>';**/
	var courseEdit1 = document.getElementById('addNewCourse_title');
	courseEdit1.innerHTML = '<textarea rows="1" cols="36">Enter your course&rsquo;s title here! </textarea>';

	var courseEdit2 = document.getElementById('addNewCourse_desc');
	courseEdit2.innerHTML = '<textarea rows="6" cols="36">Enter your course&rsquo;s description here! </textarea>';
	
	var courseEdit3 = document.getElementById('newCourseForm');
	courseEdit3.innerHTML = '<input id="newCourseTitle" type="text" name="newCourseTitle" maxlength="100"><br/><input id="newCourseDesc" type="text" name="newCourseDesc" maxlength="1000" style="width: 300px;"><br/><input type="submit" value="Submit">';
	
}

function testCourseAdder(){
								
	var course_title = document.getElementById("newCourseTitle").value;
	var course_desc = document.getElementById("newCourseDesc").value;
	
	alert(course_title + '<br>' + course_desc);
	/**
	return course_title;
	return course_desc;
	**/
													
	}


var courseSelectionManager = new clickSelectionManager();
var assignmentSelectionManager = new clickSelectionManager();
var problemSelectionManager = new clickSelectionManager();