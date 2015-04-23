CourseSketch.gradeBook.addCells = function (numberStudents,numberAssignments,table) {
    //This code generates columns and rows in a test instructor gradebook.
    for (var i = 0; i < numberStudents; i++) {
        var  row = document.createElement("tr");
        table.appendChild(row);
        for (var j = 0; j < numberAssignments; j++) {
            var column = undefined;
            if (i === 0) {
                var column = document.createElement("th");
                var innerElement = document.createElement('div');
                var text = "Assignment" + " " + j;
                innerElement.textContent = text;
                column.appendChild(innerElement);
            } else {
                var column = document.createElement("td");
                var innerElement = document.createElement('div');
                column.textContent = [];
                column.appendChild(innerElement);
            }
            row.appendChild(column);
        }
    }
}