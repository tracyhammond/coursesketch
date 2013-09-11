/**
 * Width is either 'small', 'medium', 'large'
 */
function addClassList(id, classes, isComplex, showTitle, showImage, center, width) {
	document.getElementById('class_list' + id).innerHTML = createClassList(classes, isComplex, showTitle, showImage, center, width);
}

function createClassList(classes, isComplex, showTitle, showImage, center, width) {
	
	if(width) {
	} else {
		width = 'large';
	}
	
	var html = "";
	if(showTitle) {
		html = '<h1>Classes</h1>'
	}
	html += '<ul class = "school_list">';
	for(var i = 0; i< classes.length; i++) {
		var list = classes[i];
		html+='<li '+ (center?'class = "child_center"':'') +'>';
		html+='	<div class="' + (isComplex ? 'school_item':'class_item' ) + '">';
		html+='		<div class="text">';
		if(isComplex) {
			html+='			<h3 class="name"><a href="'+list[0][1]+'">'+list[0][0]+'</a></h3>';
			html+='			<p class="'+width+'">'+list[1]+'</p>';
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