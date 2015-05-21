(function() {

    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var courseId = CourseSketch.dataManager.getState('gradebookCourseid');
            CourseSketch.gradeBook.loadGrades(courseId);
            CourseSketch.gradeBook.createTabs([ 'Quiz', 'Homework', 'Test' ], document.querySelector('.tabholder'));
        });
        var table = $('.tabletalk');
        CourseSketch.gradeBook.initializeTableScrolling(document.querySelector('.tabletalk'), table.offset());
        table.keyup(keyEventHandler);
    });

    CourseSketch.gradeBook.loadGrades = function(courseId) {

        CourseSketch.dataManager.getCourse(courseId, function(course) {
            CourseSketch.gradeBook.course = course;
            CourseSketch.dataManager.getCourseRoster(courseId, function(studentList) {
                // loads all of the grades
                CourseSketch.dataManager.getAllAssignmentGrades(courseId, function(gradeList) {
                    var assignmentList = course.assignmentList;
                    CourseSketch.gradeBook.populateGrades(assignmentList, gradeList, studentList, document.querySelector('.tabletalk'));
                });
            });
        });
    };

    /**
     * Creates a new tr with the number of assignments and adds it to the student map.
     *
     * @param {Map<String,Element>} studentMap This is a map of studentId's to table rows.
     * @param {String} studentId The student's ID number.
     * @param {Integer} assignmentList The list of assignments for the course.
     */
    function addNewStudent(studentMap, studentId, assignmentList, table) {
        var row = document.createElement('tr');
        for (var i = 0; i < assignmentList.length; i++) {
            var column = document.createElement('td');
            column.style.width = '10ch';
            column.style.maxWidth = '10ch';
            column.style.position = 'relative';
            column.className = 'gradecell';
            column.onclick = gradeCellSelected;
            column.dataset.student = studentId;
            // note that this is i minus the offset
            // column.dataset.column = i;
            column.dataset.assignment = assignmentList[i];
            row.appendChild(column);
        }
        table.appendChild(row);
        studentMap.set(studentId, row);
    }

    /**
     * This populates the map of grades for each student and, calls student adding function if student has not been added yet.
     *
     * @param {List<String>} assignmentList The list of assignment IDs.
     * @param {List<ProtoGrade>} listGrades The list of grades from the server.
     */
    CourseSketch.gradeBook.populateGrades = function(assignmentList, listGrades, studentList, table) {
        table.innerHTML = '';
        var assignmentMap = new Map();
        var studentMap = new Map();

        createAssignmentHeader(assignmentList, table, assignmentMap);
        var body = document.createElement('tbody');
        table.appendChild(body);
        for (var i = 0; i < studentList.length; i++) {
            addNewStudent(studentMap, studentList[i], assignmentList, body);
        }

        for (var i = 0; i < listGrades.length; i++) {
            var protoGrade = listGrades[i];
            var studentId = protoGrade.userId;
            var assignmentId = protoGrade.assignmentId;
            if (!studentMap.has(studentId)) {
                addNewStudent(studentMap, studentId, assignmentList, body);
            }
            var studentRow = studentMap.get(studentId);
            var columnList = studentRow.children;
            var cell = columnList[assignmentMap.get(assignmentId)];
            var stringGrade = '' + protoGrade.getCurrentGrade();
            cell.textContent = stringGrade.substring(0, 6);
        }

        populateStudentNames(studentMap);
    };

    /**
     * Creates the assignment header while populating the assignment map.
     * @param {List<String>} assignmentList
     * @param {Element} table
     * @param {Map<String, Integer>} assignmentMap
     */
    function createAssignmentHeader(assignmentList, table, assignmentMap) {
        var header = document.createElement('thead');
        header.className = 'scrollHeader';
        var row = document.createElement('tr');
        var nameLabel = document.createElement('th');
		var nameHolder = document.createElement('div');
		nameHolder.textContent = 'Student Name';
        nameLabel.appendChild(nameHolder);
        row.appendChild(nameLabel);
        for (var i = 0; i < assignmentList.length; i++) {
            assignmentMap.set(assignmentList[i], i);
            var th = document.createElement('th');
            th.style.minWidth = '10ch';
            th.style.maxWidth = '11ch';
            var button = document.createElement('a');
            button.textContent = assignmentList[i];
            button.className = 'waves-effect waves-teal btn-flat truncate';
            //Waves.attach(button, 'waves-effect waves-teal');
            th.appendChild(button);
            row.appendChild(th);
        }
        header.appendChild(row);
        table.appendChild(header);
    }

    /**
     * Adds the student names to the first column of every row.
     * @param {Map<String, Element>} studentMap
     */
    function populateStudentNames(studentMap) {
        studentMap.forEach(function(value, key, map) {
            row = map.get(key);
            var cell = document.createElement('td');
            cell.dataset.student = key;
            var cellText = key;
            if (key.length > 13) {
                cellText = key.substring(0, 10) + '...';
            }
            cell.textContent = cellText;
            cell.className = 'namecell';
            row.insertBefore(cell, row.firstChild);
        });
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
        gradeCategories.unshift('All');
        gradeCategories.push('New');
        for (var i = 0; i < gradeCategories.length; i++) {
            var item = document.createElement('li');
            item.className = 'tab col';
            var link = document.createElement('a');
            link.textContent = gradeCategories[i];

            // The last catagory will be adding a new catagory so we denote it with a plus
            if (i === gradeCategories.length - 1) {
                link.textContent = '+';
            }

            link.href = '#' + gradeCategories[i];
            item.appendChild(link);
            tabs.appendChild(item);
        }
        tabholder.appendChild(tabs);
        $('ul.tabs').tabs();
    };

    function clearOpenedCells() {
        var selectedCell = document.querySelector('.gradeselected');
        if (selectedCell === null) {
            return;
        }
        unselectCell(selectedCell);
    }

    function gradeCellSelected(event) {
        if (this.querySelector('input') === null) {
            clearOpenedCells();
            if (!isUndefined(event)) {
                event.stopPropagation();
            }

            var grade = this.textContent;
            // datasets have to be underscore.
            this.dataset.old_grade = grade;
            this.textContent = '';
            var container = createFocusedCell();
            var input = container.querySelector('.gradeInput');
            var addCommentButton = container.querySelector('.addComment');

            this.appendChild(container);
            input.value = grade;
            input.focus();
            input.select();

            addCommentButton.onclick = function() {
                addComment(this);
            }.bind(this);

            // setCustomValidity must be cleared oninput otherwise the error message will continually appear and form will never appear valid.
            input.oninvalid = function() { this.setCustomValidity('Please enter numbers only.') };
            input.oninput = function() { this.setCustomValidity('') };
            $(this).addClass('gradeSelected');
        }
    }

    function createFocusedCell() {
        var template = document.querySelector('#inputTemplate');
        var container = document.importNode(template.content, true);
        return container;
    }

    function addComment(cell) {
        console.log(cell);
        var addCommentButton = cell.querySelector('.addComment');
        var addCommentInput = cell.querySelector('.commentInput');
        addCommentButton.style.display = 'none';
        addCommentInput.style.display = 'block';
    }

    /**
     * removes the selection of a cell by removing the html code inside and replacing it with a grade.
     *
     * This may also trigger a grade save.
     *
     * @param {Element} cell the selected cell whose code is being removed.
     */
    function unselectCell(cell) {
        var oldGrade = cell.dataset.old_grade;
        var form = cell.querySelector('.gradeForm');
        if ($(form)[0].checkValidity()) {
            var newGrade = form.querySelector('.gradeInput').value;
            var comment = form.querySelector('.commentInput').value;

            // TODO: extra grade validation here (is it out of max points for the assignment?)

            if (oldGrade !== newGrade) {
                console.log('SAVING GRADE: [' , newGrade, ', ', comment, ']');
                var protoGrade = buildProtoGrade(cell, newGrade, comment);
                CourseSketch.dataManager.setGrade(protoGrade);
            }

            // replaces input with new grade value.
            cell.textContent = '' + newGrade;
            $(cell).removeClass('gradeSelected');
        } else {
            $(form).find(':submit').click();
            throw new BaseException('Grade input is not valid');
        }
    }

    var keyEventHandler = undefined;
    (function() {
        /**
         * @type {Number} this is used to throttle events.
         */
        var previousTimeStamp = 0;
        keyEventHandler = function(event) {
            if (event.timeStamp - previousTimeStamp < 50) {
                previousTimeStamp = event.timeStamp;
                // less than 50 millis we return and don't process the event
                return;
            }
            previousTimeStamp = event.timeStamp;

            var cell = document.querySelector('.gradeselected');
            if (isUndefined(cell) || cell === null) { return; } // Just in case there is no entry cell
            var isTab = event.which === 9; // Keycode for tab key
            var isEnter = event.which === 13; // Keycode for enter key
            if (isEnter) { moveDown(cell); }
            if (isTab) { moveRight(cell); }
        };
    })();

    function moveLeft(cell) {

        unselectCell(cell);
    }

    function moveRight(cell) {
        unselectCell(cell);
        // If there is a cell to move to.
        if (cell.nextSibling !== null) {
            gradeCellSelected.bind(cell.nextSibling)();
        } else {
            var currentRow = cell.parentElement;
            var nextRow = currentRow.parentElement.rows[currentRow.rowIndex]; // rows[] indexes from 0. rowIndex starts at 1. Idk why.
            var nextCell = nextRow.querySelector('.gradecell');
            gradeCellSelected.bind(nextCell)();
        }

    }

    function moveDown(cell) {
        unselectCell(cell);
        var currentRow = cell.parentNode;
        var rowIndex = getChildIndex(currentRow);
        var nextRow = currentRow.parentNode.children[getChildIndex(currentRow) + 1];
        var columnIndex = getChildIndex(cell);

        // If the end of the column is reached, there is no nextRow
        if (isUndefined(nextRow) || nextRow === null) {
            nextRow = currentRow.parentNode.children[0] // Wrap back to the top row
            columnIndex += 1; // Wrap over one column
        }
        var nextCell = nextRow.children[columnIndex];

        gradeCellSelected.bind(nextCell)();
    }

    function moveUp(cell) {
        unselectCell(cell);
    }

    function buildProtoGrade(cell, grade, comment) {
        grade = parseFloat(grade); // Sent in is a string. Proto requires a float.
        var protoGrade = CourseSketch.PROTOBUF_UTIL.ProtoGrade();
        protoGrade.setCourseId(CourseSketch.gradeBook.course.id);
        protoGrade.setUserId(cell.dataset.student);
        if (!isUndefined(cell.dataset.assignment)) { protoGrade.setAssignmentId(cell.dataset.assignment); }
        if (!isUndefined(cell.dataset.problem)) { protoGrade.setProblemId(cell.dataset.problem); }
        if (!isNaN(grade)) { protoGrade.setCurrentGrade(grade); }
        var gradeHistory = CourseSketch.PROTOBUF_UTIL.GradeHistory();
        if (!isNaN(grade)) { gradeHistory.setGradeValue(grade); }
        if (!isUndefined(comment)) { gradeHistory.setComment(comment); }
        protoGrade.setGradeHistory(gradeHistory); // Don't need to add to list since there is only one gradeHistory value
        return protoGrade;
    }

    /**
     * Returns the index of an element in reference to its parent element.
     */
    function getChildIndex(element) {
        var k=-1, e=element;
        while (e) {
            if ( "previousSibling" in e ) {
                e = e.previousSibling;
                k = k + 1;
            } else {
                k= -1;
                break;
            }
        }
        return k;
    }
})();
