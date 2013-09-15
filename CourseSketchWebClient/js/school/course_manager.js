function classClickerFunction(list) {
	clearLists(2);
	changeSelection(list);
	//we get the list from the id.
	var assignmentList = classAssignments.getList(list[0][2]);
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
	changeSelection(list);
	clearLists(1);
	new schoolItemBuilder().build('problem_list_column');
	var problemList = assignmentProblems.getList(list[0][2]);
	var builder = new schoolItemBuilder();
	builder.setList(problemList).setWidth('medium').centerItem(true);
	builder.showImage = false;
	builder.setEmptyListMessage('There are no problems for this assignment!');
	builder.setOnBoxClick('problemClickerFunction');
	builder.build('problem_list_column');
	showButton('problem_button');
}

function problemClickerFunction(list) {
	changeSelection(list);
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

function changeSelection(list) {
	selectionManager.clearAllSelectedItems();
	selectionManager.addSelectedItem(list[0][2]);
}


function manageHeight() {
	var iframe = document.getElementById('edit_frame_id');
	var innerDoc = iframe.contentDocument || iframe.contentWindow.document;
	// Gets the visual height.
	var height = innerDoc.getElementsByTagName('body')[0].clientHeight;
	iframe.height = height;
}

/**
	Given the source this will create an iframe that will manage its own height.
*/
function replaceIframe(src) {
	var toReplace = document.getElementById('editable_unit');
	if (src) {
		toReplace.innerHTML =  '<Iframe id="edit_frame_id" src="'+ src+'" height="100%" width = 100% seamless = "seamless" onload="manageHeight()">';
	} else {
		toReplace.innerHTML = '<h2 style = "text-align:center">Nothing is selected yet</h2>' +
		'<h2 style = "text-align:center">Click an item to edit</h2>';
	}
}

var selectionManager = new clickSelectionManager();