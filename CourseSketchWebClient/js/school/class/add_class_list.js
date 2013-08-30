function addClassList(id, isSimple, showImage) {
	document.getElementById('due_assignments' + id).innerHTML = addClassList(isSimple);
}

function createClassList(isComplex, showImage) {
	var html = '<ul>';
	var currentDate = new Date();
	for(var i = 0; i< user_classes.length; i++) {
		var list = user_classes[i];
		var dueDate = list[2];
		var dateType = getDateType(dueDate, currentDate);
		html+='<li>';
		html+='	<div class="' + (isComplex ? 'school_item':'class_item' ) + '">';
		html+='		<div class="text">';
		if(isComplex) {
			html+='			<h3 class="name"><a href="'+list[0][1]+'">'+list[0][0]+'</a></h3>';
			html+='			<p>'+list[2]+'</p>';
		}else {
			html+='			<a href="'+list[0][1]+'">'+list[0][0]+'</a>';
		}
		html+='		</div>';
		if(showImage) {
			html+='		<a href="'+list[0][1]+'"><img src="images/'+ list[4]+'" width="128" height="128"></a>';
		}
		html+='	</div>';
		html+='</li>';
	}
	html+='</ul>';
	return html;
}