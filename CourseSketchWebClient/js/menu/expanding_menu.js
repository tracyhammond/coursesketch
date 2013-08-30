function list_menu_classes() {
	var html = '';

	if(class_list_showing) {
		class_list_showing = false;
	} else {
		class_list_showing = true;
		// ADD CLASSES HERE
	}
	document.getElementById('list_of_classes').innerHTML = html;

}

var class_list_showing = false;