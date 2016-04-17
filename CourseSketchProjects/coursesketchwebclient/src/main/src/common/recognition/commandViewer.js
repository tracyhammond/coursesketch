function createCommandViewer(list, idToPutViewIn, currentIndex) {
    var output = parseUpdateList(list, 0, currentIndex);
    document.getElementById(idToPutViewIn).innerHTML = output;
}

var myWidth = 150; // single threaded = okay

function createUpdate(object, highlight) {
    var html = '<div class = "updateInfo tiny-col-group2" style = "position:relative;">'
    html += '<div class="sketchObject" style = "position:relative;">';
    html += '<p> ID:' + object.getUpdateId() + '</p>';
    html += '<p> Time:' + object.getTime() + '</p>';
    html += '</div>';
    html += '<div class ="commands" style = "right:50%;">';
    html += '<div>';
    var commandList = object.getCommands();
    for (var i = 0; i < commandList.length ; i++) {
        html += createCommand(commandList[i], highlight);
    }
    html += '</div>';
    html += '</div>';
    html += '</div>';
    return html;
}

function createCommand(command, highlight) {
    var html = "";
    if (highlight) {
        html = '<div class = "sketchObject highlight">';
    } else {
        html = '<div class = "sketchObject">';
    }

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
function parseUpdateList(list, level, currentIndex) {
    var html = '';
    var size = list.length;
    var cumlativeWidth = 0;
    for (var i = 0; i < size; i++) {
        var object = list[i]; // this is a single update
        html += '<div class="update">';
        html += createUpdate(object, i == (currentIndex - 1)); // create a view of an update
        html +='</div>';
    }
    return html;
}
