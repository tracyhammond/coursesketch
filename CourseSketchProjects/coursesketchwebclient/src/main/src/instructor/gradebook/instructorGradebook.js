/**
 * This creates a table with # of assignments = # of columns and # of students = # of rows.
 *
 * @param {Integer} numberStudents The number of students.
 * @param {Integer} numberAssignments The number of assignments.
 * @param {Element} table The table that is displaying students and assignments.
 */
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
