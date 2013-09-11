/**
 * Width is either 'small', 'medium', 'large'
 */
function placeAssignments(id, assignments, showClass, showTitle, showImage, center, width) {
	document.getElementById('due_assignments' + id).innerHTML = createAssignments(assignments, showClass, showTitle, showImage, center, width);
}

function createAssignments(assignments, showClass, showTitle, showImage, center, width) {

	if(width) {
	} else {
		width = 'large';
	}

	var html = "";
	if(showTitle) {
		html += '<h1>Uncompleted Assignments</h1>\n';
	}
	html+='<ul class = "school_list">';
	var currentDate = new Date();
	for(var i = 0; i< assignments.length; i++) {
		var list = assignments[i];
		var dueDate = list[2];
		var dateType = getDateType(dueDate, currentDate);
		html+='<li '+ (center?'class = "child_center"':'') +'>';
		html+='<div class="assignment_item school_item">';
		html+='	<div class="text">';
		html+='		<h3 class="name"><a href="' + list[0][1] + '">' + list[0][0] + '</a></h3>';
		html+='		<h1 class="' + dateType + '">' + get_formatted_date(currentDate, dueDate) + '</h1>';

		if(showClass) {
			html+='		<h1 class="class"><a href="' + list[1][1] + '">' + list[1][0] + '</a></h1>';
		}
		
		html+='		<p class="'+width+'">'+list[3]+'</p>';
		html+='	</div>';

		if(showImage) {
			html+='	<a href="' + list[0][1] + '"><img src="images/' + list[4] + '" width="128" height="128"></a>';
		}

		html+='</div>';
		html+='</li>';
	}
	html+='</ul>';
	return html;
}

function get_formatted_date(currentDate, dueDate) {
	var curr_date = dueDate.getDate();
	var curr_month = dueDate.getMonth() + 1; //Months are zero based
	var curr_year = dueDate.getFullYear();
	return (curr_date + "-" + curr_month + "-" + curr_year);
}


function getDateType() {
	return 'late';
}
