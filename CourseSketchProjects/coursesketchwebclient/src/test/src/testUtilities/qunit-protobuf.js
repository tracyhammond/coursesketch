/**
 * Returns a cleaned version of the update that is ready for comparison.
 */
function cleanUpdateForComparison(update) {
    return CourseSketch.prutil.decodeProtobuf(update.toArrayBuffer(), CourseSketch.prutil.getSrlUpdateClass());
}

/**
 * Returns a copy of the updateList for the purpose of not being edited
 * while in use.
 *
 * This is a synchronous method.  Can freeze the browser for long list.  Should not use often.
 *
 */
function cleanUpdateList(updateList) {
    var index = 0;
    var maxIndex = updateList.length;
    var newList = new Array();
    // for local scoping
    var oldList = updateList;
    for (var index = 0; index < oldList.length; index++) {
        newList.push(cleanUpdateForComparison(oldList[index]));
    }
    return newList;
}

function updateEqual(actual, expected, message) {
    var actualUpdate = cleanUpdateForComparison(actual);
    var expectedUpdate = cleanUpdateForComparison(expected);
    this.propEqual(actualUpdate, expectedUpdate, message);
}

function updateListEqual(actual, expected, message) {
    var actualList = cleanUpdateList(actual);
    var expectedList = cleanUpdateList(expected);
    this.propEqual(actualList, expectedList, message);
}

function compareProtobuf(actual, expected, type, message) {
    var cleanActual = CourseSketch.prutil.cleanProtobuf(actual, type);
    var cleanExpected = CourseSketch.prutil.cleanProtobuf(expected, type);
    this.propEqual(cleanActual, cleanExpected, message);
}

QUnit.extend(QUnit.assert, {
    updateListEqual: updateListEqual,
    updateEqual: updateEqual,
    protoEqual: compareProtobuf
});
