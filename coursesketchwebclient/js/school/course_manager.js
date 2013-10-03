function classClickerFunction(list) {
	clearLists(2);
	changeSelection(list, courseSelectionManager);
	assignmentSelectionManager.clearAllSelectedItems();
	problemSelectionManager.clearAllSelectedItems();
	//we get the list from the id.
	var assignmentList = classAssignments.getList(list[1][2]);
	var builder = new schoolItemBuilder();
	builder.setList(assignmentList).setWidth('medium').centerItem(true);
	builder.showImage = false;
	builder.setEmptyListMessage('There are no assignments for this class!');
	builder.setOnBoxClick('assignmentClickerFunction');
	builder.build('assignment_list_column');
	replaceIframe('html/course_managment_frames/edit_course.html');
	showButton('assignment_button');
}

function assignmentClickerFunction(list) {
	// clears the problems
	changeSelection(list, assignmentSelectionManager);
	problemSelectionManager.clearAllSelectedItems();
	clearLists(1);
	new schoolItemBuilder().build('problem_list_column');
	var problemList = assignmentProblems.getList(list[1][2]);
	var builder = new schoolItemBuilder();
	builder.setList(problemList).setWidth('medium').centerItem(true);
	builder.showImage = false;
	builder.setEmptyListMessage('There are no problems for this assignment!');
	builder.setOnBoxClick('problemClickerFunction');
	builder.build('problem_list_column');
	replaceIframe('html/course_managment_frames/edit_assignment.html');
	showButton('problem_button');
}

function problemClickerFunction(list) {
	changeSelection(list, problemSelectionManager);
	replaceIframe('html/course_managment_frames/edit_problem.html');
}

function showButton(id) {
	document.getElementById(id).style.display = "block";
}

function hideButton(id) {
	document.getElementById(id).style.display = "none";
}

function clearLists(number) {
	var builder = new schoolItemBuilder();
	
	if(number>0) {
		hideButton('problem_button');
		builder.setEmptyListMessage('Please select an assignment to see the list of problems.');
		builder.build('problem_list_column');
	}
	if(number>1) {
		hideButton('assignment_button');
		builder.setEmptyListMessage('Please select a class to see the list of assignments.');
		builder.build('assignment_list_column');
	}
}

function changeSelection(list, selectionManager) {
	selectionManager.clearAllSelectedItems();
	selectionManager.addSelectedItem(list[1][2]);
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
	if (src) {
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