function checkEnter(element) {
	if(event.which == 13 || event.keyCode == 13) {
		element.blur();
	}
}
    	
function makeEditable(element) {
	element.readOnly='true';
	element.className = 'notEditing';
}

function makeUnEditable(element) {
	element.readOnly='';
	element.className = '';
}