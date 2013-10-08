function checkEnter(element) {
	if(event.which == 13 || event.keyCode == 13) {
		element.blur();
	}
}

function makeEditable(element) {
	element.readOnly='';
	var name = element.className;
	element.className = name.replace('notEditing','editing');
}

function makeUnEditable(element) {
	element.readOnly='true';
	var name = element.className;
	element.className = name.replace('editing','notEditing');
}