function createCommandViewer(list, idToPutViewIn) {
	var output = parseUpdateList(list, 0);
	document.getElementById(idToPutViewIn).innerHTML = output;
}
var myWidth = 150; // single threaded = okay

function createUpdate(object) {
	html = '<div class = "updateInfo tiny-col-group2" style = "position:relative;">'
	html += '<div id="column1" class="sketchObject" style = "position:relative;">';
	html += '<p> ID:' + object.getUpdateId() + '</p>';
	html += '<p> Time:' + object.getTime() + '</p>';
	html += '</div>';
	html += '<div id="column2" class ="commands" style = "right:50%;">';
	html += '<div>';
	var commandList = object.getCommands();
	for (var i = 0; i < commandList.length ; i++) {
		html += createCommand(commandList[i]);
	}
	html += '</div>';
	html += '</div>';
	html += '</div>';
	return html;
}

function createCommand(command) {
	html = '<div class = "sketchObject">';
	html += '<p> Type : ' + command.getCommandTypeName() + '</p>';
	html += '<p> UserCreated : ' + command.getIsUserCreated() + '</p>';
	html += '<p> Id : ' + command.getCommandId() + '</p>';
	html += '</div>';
	return html;
}

/**
 * Creates a vertical list.
 * 
 * Update  -> Command
 */
function parseUpdateList(list, level) {
	var html = '';
	var size = list.length;
	var cumlativeWidth = 0;
	for (var i = 0; i< size; i++) {
		var object = list[i]; // this is a single update
		html += '<div class="update">';
		html += createUpdate(object); // create a view of an update
		html +='</div>';
	}
	return html;
}
