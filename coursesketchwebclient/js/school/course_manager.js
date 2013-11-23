function courseClickerFunction(id) {
	console.log(id);
	clearLists(2);
	changeSelection(id, courseSelectionManager);
	assignmentSelectionManager.clearAllSelectedItems();
	problemSelectionManager.clearAllSelectedItems();
	//we get the list from the id.
	var assignmentList = parent.courseAssignments.getList(id);
	console.log(assignmentList);
	var builder = new SchoolItemBuilder();
	builder.setList(assignmentList).setWidth('medium').centerItem(true);
	builder.showImage = false;
	builder.setEmptyListMessage('There are no assignments for this course!');
	builder.setOnBoxClick('assignmentClickerFunction');
	builder.build('assignment_list_column');
	replaceIframe('html/instructor/course_managment_frames/edit_course.html');
	showButton('assignment_button');
}

function assignmentClickerFunction(id) {
	// clears the problems
	changeSelection(id, assignmentSelectionManager);
	problemSelectionManager.clearAllSelectedItems();
	clearLists(1);
	new SchoolItemBuilder().build('problem_list_column');
	var problemList = parent.assignmentProblems.getList(id);
	var builder = new SchoolItemBuilder();
	builder.setList(problemList).setWidth('medium').centerItem(true);
	builder.showImage = false;
	builder.setEmptyListMessage('There are no problems for this assignment!');
	builder.setOnBoxClick('problemClickerFunction');
	builder.build('problem_list_column');
	replaceIframe('html/instructor/course_managment_frames/edit_assignment.html');
	showButton('problem_button');
}

function problemClickerFunction(id) {
	changeSelection(id, problemSelectionManager);
	replaceIframe('html/instructor/course_managment_frames/edit_problem.html');
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
	if (src && toReplace != null) {
		toReplace.innerHTML =  '<Iframe id="edit_frame_id" src="'+ src+'" width = 100% ' +
		'sanbox = "allow-same-origin allow-scripts"' +
		'seamless = "seamless" onload="manageHeight()">';
	} else {
		toReplace.innerHTML = '<h2 style = "text-align:center">Nothing is selected yet</h2>' +
		'<h2 style = "text-align:center">Click an item to edit</h2>';
	}
}

var courseSelectionManager = new clickSelectionManager();
var assignmentSelectionManager = new clickSelectionManager();
var problemSelectionManager = new clickSelectionManager();