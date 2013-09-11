function addClassList(id, isComplex, showTitle, showImage, classes) {
	document.getElementById('class_list' + id).innerHTML = createClassList(isComplex, showTitle , showImage, classes);
}

function createClassList(isComplex, showTitle, showImage, classes) {
	var html = "";
	if(showTitle) {
		html = '<h1>Classes</h1>'
	}
	html += '<ul class = "school_list">';
	var currentDate = new Date();
	for(var i = 0; i< classes.length; i++) {
		var list = classes[i];
		var dueDate = list[2];
		var dateType = getDateType(dueDate, currentDate);
		html+='<li>';
		html+='	<div class="' + (isComplex ? 'school_item':'class_item' ) + '">';
		html+='		<div class="text">';
		if(isComplex) {
			html+='			<h3 class="name"><a href="'+list[0][1]+'">'+list[0][0]+'</a></h3>';
			html+='			<p>'+list[1]+'</p>';
		}else {
			html+='			<a href="'+list[0][1]+'">'+list[0][0]+'</a>';
		}
		html+='		</div>';
		if(showImage) {
			html+='		<a href="'+list[0][1]+'"><img src="images/'+ list[2]+'" width="128" height="128"></a>';
		}
		html+='	</div>';
		html+='</li>';
	}
	html+='</ul>';
	return html;
}