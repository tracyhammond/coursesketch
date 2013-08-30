function placeAssignments(id) {
	document.getElementById('due_assignments' + id).innerHTML = createAssignments();
}

function createAssignments() {
	var html = '<h1>Uncompleted Assignments</h1>\n';
	html+='<ul>';
	var currentDate = new Date();
	for(var i = 0; i< upcomming_assignments.length; i++) {
		var list = upcomming_assignments[i];
		var dueDate = list[2];
		var dateType = getDateType(dueDate, currentDate);
		html+='<li>';
		html+='<div class="assignment_item school_item">';
		html+='	<div class="text">';
		html+='		<h3 class="name"><a href="'+list[0][1]+'">'+list[0][0]+'</a></h3>';
		html+='		<h1 class="'+dateType+'">'+dueDate+'</h1>';
		html+='		<h1 class="class"><a href="'+list[1][1]+'">'+list[1][0]+'</a></h1>';
		html+='		<p>'+list[3]+'</p>';
		html+='	</div>';
		html+='	<a href="'+list[0][1]+'"><img src="images/'+ list[4]+'" width="128" height="128"></a>';
		html+='</div>';
		html+='</li>';
	}
	html+='</ul>';
	return html;
}

function getDateType() {
	return 'late';
}
