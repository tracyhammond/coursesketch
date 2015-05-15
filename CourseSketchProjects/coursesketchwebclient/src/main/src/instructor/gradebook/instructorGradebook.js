(function() {

    $(document).ready(function() {
        CourseSketch.gradeBook.loadGrades('courseId');
        CourseSketch.gradeBook.createTabs(['Quiz', 'Homework', 'Test'], document.querySelector('.tabholder'));
    });

    CourseSketch.gradeBook.loadGrades = function(courseId) {

        CourseSketch.dataManager.getCourse(courseId, function(course) {
            // loads all of the grades
            CourseSketch.dataManager.getAllAssignmentGrades(courseId, function(gradeList) {
                var assignmentList = course.assignmentList;
                CourseSketch.gradeBook.populateGrades(assignmentList, gradeList, document.querySelector('.tabletalk'));
            });
        });
    };

    /**
     * This adds to the map a student row with columns for assignment grades.
     * @param {Map<studentI,tr>} studentMap This is the Map where the student ID are stored.
     * @param {String} studentId The student's ID number.
     * @param {Integer} numberOfAssignments The number of assignments and length of the listAssignments list.
     */
    function addNewStudent(studentMap, studentId, numberOfAssignments, table) {
        var row = document.createElement('tr');
        for (var i = 0; i < numberOfAssignments; i++) {
            var column = document.createElement('td');
            row.appendChild(column);
        }
        table.appendChild(row);
        studentMap.set(studentId, row);
    }

    /**
     * This populates the map of grades for each student and, calls student adding function if student has not been added yet.
     *
     * @param {List<String>} listAssignments The list of assignment IDs.
     * @param {List<ProtoGrade>} listGrades The list of grades from the server.
     */
    CourseSketch.gradeBook.populateGrades = function(listAssignments, listGrades, table) {
        table.innerHTML = '';
        var assignmentMap = new Map();
        var studentMap = new Map();
        var header = document.createElement('thead');
        var row = document.createElement('tr');
        var nameLabel = document.createElement('th');
        nameLabel.textContent = 'Student Name';
        row.appendChild(nameLabel);
        for (var i = 0; i < listAssignments.length; i++) {
            assignmentMap.set(listAssignments[i], i);
            var th = document.createElement('th');
            th.textContent = listAssignments[i];
            row.appendChild(th);
        }
        header.appendChild(row);
        table.appendChild(header);

        var body = document.createElement('tbody');
        table.appendChild(body);

        for (var i = 0; i < listGrades.length; i++) {
            var protoGrade = listGrades[i];
            var studentId = protoGrade.userId;
            var assignmentId = protoGrade.assignmentId;
            if (!studentMap.has(studentId)) {
                addNewStudent(studentMap, studentId, listAssignments.length, body);
            }
            var studentRow = studentMap.get(studentId);
            var columnList = studentRow.children;
            var cell = columnList[assignmentMap.get(assignmentId)];
            cell.textContent = protoGrade.getCurrentGrade();
            cell.onclick = gradeCellOnClick;
        }
        studentMap.forEach(function(value, key, map) {
            row = map.get(key);
            var cell = document.createElement('td');
            cell.textContent = key;
            row.insertBefore(cell, row.firstChild);
        })
    };

    function gradeCellOnClick() {
        if (this.querySelector('input') === null) {
            var grade = this.textContent;
            var inputElement = document.createElement('input');
            inputElement.type = "text";
            this.innerHTML = '';
            this.classname = "input-field";
            inputElement.value = grade;
            inputElement.pattern = "\\d*"
            inputElement.style.padding = "0px";
            this.appendChild(inputElement);
            inputElement.focus();
            inputElement.select();
        }
    }

    /**
     * This creates tabs, one for each grading category.
     *
     * @param {List<String>} gradeCategories The list of categories for grades in the gradebook.
     * @param {element} tabholder The element that will hold the tabs.
     */
    CourseSketch.gradeBook.createTabs = function(gradeCategories, tabholder) {
        tabholder.innerHTML = '';
        var tabs = document.createElement('ul');
        tabs.className = 'tabs';
        for (var i = 0; i < gradeCategories.length; i++) {
                var item = document.createElement('li');
                item.className = 'tab col';
                var link = document.createElement('a');
                link.textContent = gradeCategories[i];
                link.href = "#" + gradeCategories[i];
                item.appendChild(link);
                tabs.appendChild(item);
            }
        tabholder.appendChild(tabs);
        $('ul.tabs').tabs();
    };
})();

/*
    CourseSketch.gradeBook.populateGrades = function(listAssignments, listGrades, table) {
        table.innerHTML = '';
        var assignmentMap = new Map();
        var studentMap = new Map();
        var header = document.createElement('thead');
        var row = document.createElement('tr');
        var nameLabel = document.createElement('th');
        nameLabel.textContent = 'Sutdent Name';
        row.appendChild(nameLabel);
        for (var i = 0; i < listAssignments.length; i++) {
            assignmentMap.set(listAssignments[i], i);
            var th = document.createElement('th');
            th.textContent = listAssignments[i];
            row.appendChild(th);
        }
        header.appendChild(row);
        table.appendChild(header);

        var body = document.createElement('tbody');
        table.appendChild(body);
*/
