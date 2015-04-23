<<<<<<< HEAD
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
=======
CourseSketch.gradeBook.addCells = function(numberStudents, numberAssignments, table) {
    for (var i = 0; i < numberStudents; i++){
        var row = document.createElement('tr');
        if (i === 0) {
            for (var t = 0; t < numberAssignments; t++) {
                var title = document.createElement('th');
                title.textContent = i;
                table.appendChild(title);
            }
        }
        table.appendChild(row);
        for (var j = 0; j < numberAssignments; j++) {
            var column = document.createElement('td');
            column.textContent = [];
            row.appendChild(column);
        }
    }
};
>>>>>>> 54288283009b7a0c73d41454281dfea2f68aa354
