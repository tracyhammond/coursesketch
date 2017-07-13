/* eslint-disable valid-jsdoc, require-jsdoc */
function createSketchViewer(list, idToPutViewIn) {
    var parsedList = parseList(list, 0);
    document.getElementById(idToPutViewIn).innerHTML = parsedList[0];
}
var myWidth = 150; // single threaded = okay

function createView(sketchObject, level) {
    var tempWidth = myWidth;
    var html = '<div class = "sketchObject level' + level + '">';
    var objectType = sketchObject.check_type();
    html += '<p style="max-width:' + myWidth + 'px;">' + 'type: ' + objectType;
    html += ' <br>Id:<br>' + (sketchObject.getId().substring(0,12));
    html += '</p>';

    if (objectType === 'SRL_Shape') {
        var interpretations = sketchObject.getInterpretations();
        if (interpretations.length > 0) {
            console.log('open interpretations');
            html += '<div class = "interpretations style="max-width:' + myWidth + 'px;"">';
            html += '<p style="max-width:' + myWidth + 'px;">';
            for (var i = 0; i < interpretations.length; i++) {
                var inter = interpretations[i];
                html += 'label: ' + inter.label + ' confidence: ' + inter.confidence + '<br>';
            }
            html += '</p></div>';
        }
        var subObjects = sketchObject.getSubObjects();
        if (subObjects.length > 0) {
            html += '# of subshapes: ' + subObjects.length;
            var values =  parseList(subObjects, level);
            //html += '' + (tempWidth + 5)  +':' + level+'<br>';
            html += values[0];
            //size
            tempWidth = values[1];
        }
    } else if (objectType === 'SRL_Stroke') {
        html += '# of points: ' + sketchObject.getNumPoints();
    }

    html += '</div>';
    var array = [];
    array.push(html);
    array.push(tempWidth);
    return array;
}

function parseList(list, level) {
    var html = '';
    var size = list.length;
    var cumlativeWidth = 0;
    for (var i = 0; i < size; i++) {
        var object = list[i];
        var tempValues = createView(object, level + 1);
        var tempHtml = tempValues[0];
        var tempWidth = tempValues[1];
        html += '<div class="parseList" style="position:absolute;left:' + ((cumlativeWidth + 5)) + 'px;">';
        html += tempHtml;
        cumlativeWidth += tempWidth + 5;
        html += '</div>';
    }
    html += '</div>';
    objectsWidth = cumlativeWidth;
    html = '<div class = "subObjects' + level + '" style="position:relative; width:' + objectsWidth + 'px;">' + html;
    var array = [];
    array[0] = html;
    array[1] = cumlativeWidth;
    return array;
}
