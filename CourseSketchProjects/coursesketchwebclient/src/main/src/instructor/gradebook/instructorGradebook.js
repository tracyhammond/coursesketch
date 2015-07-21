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

    /**
     * Loads grades for the given course.
     * @param {String} courseId The id of the course.
     */
    CourseSketch.gradeBook.loadGrades = function(courseId) {
        CourseSketch.dataManager.getCourse(courseId, function(course) {
            CourseSketch.gradeBook.course = course;
            CourseSketch.dataManager.getCourseRoster(courseId, function(studentList) {
                // loads all of the grades
                CourseSketch.dataManager.courseRoster = studentList;
                CourseSketch.dataManager.getAllAssignmentGrades(courseId, function(gradeList) {
                    var assignmentList = course.assignmentList;
                    var table = document.querySelector('.tabletalk');
                    CourseSketch.gradeBook.initializeTable(assignmentList, gradeList, studentList, table);
                });
            });
        });
    };

    /**
     * Initializes a table from the given values.
     *
     * @param {List<String>} assignmentList List of assignment IDs.
     * @param {List<ProtoGrade>} gradeList List of grades from the server.
     * @param {List<String>} studentList List of student IDs.
     * @param {HTMLTable} table The grade table on the webpage.
     */
    CourseSketch.gradeBook.initializeTable = function(assignmentList, gradeList, studentList, table) {
        table.innerHTML = '';

        var assignmentMap = new Map();
        var studentMap = new Map();
        var body = document.createElement('tbody');
        table.appendChild(body);

        for (var i = 0; i < studentList.length; i++) {
            addNewStudent(studentMap, studentList[i], assignmentList, body);
        }
        CourseSketch.gradeBook.studentMap = studentMap;

        createAssignmentHeader(assignmentList, assignmentMap, table);
        CourseSketch.gradeBook.populateGrades(gradeList, studentMap, assignmentMap, body);
        populateStudentNames(studentMap);
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
     * @param {List<ProtoGrade>} listGrades The list of grades from the server.
     * @param {Map<String, Integer>} studentMap This is a map of studentIds to table rows.
     * @param {Map<String, Integer>} assignmentMap This is a map of assignmentIds to table columns.
     * @param {HTMLTable} table The grade table on the webpage.
     * @return {List<ProtoGrade>} grades that were not displayed.   This is because the users do not exist anymore in the course roster.
     */
    CourseSketch.gradeBook.populateGrades = function(listGrades, studentMap, assignmentMap, table) {
        var gradesNotShown = [];
        for (var i = 0; i < listGrades.length; i++) {
            var protoGrade = listGrades[i];
            var studentId = protoGrade.userId;
            var assignmentId = protoGrade.assignmentId;
            if (!studentMap.has(studentId)) {
                gradesNotShown.push(protoGrade);
                continue;
            }
            var studentRow = studentMap.get(studentId);
            var columnList = studentRow.children;
            var cell = columnList[assignmentMap.get(assignmentId)];
            var stringGrade = '' + protoGrade.getCurrentGrade();
            cell.textContent = stringGrade.substring(0, 6);
        }
        return gradesNotShown;
    };

    /**
     * Creates the assignment header while populating the assignment map.
     *
     * @param {List<String>} assignmentList List of assignments for the course.
     * @param {Map<String, Integer>} assignmentMap This is a map of assignmentIds to table columns.
     * @param {Element} table The grade table on the webpage.
     */
    function createAssignmentHeader(assignmentList, assignmentMap, table) {
        var header = document.createElement('thead');
        header.className = 'scrollHeader';
        var row = document.createElement('tr');
        var nameLabel = document.createElement('th');
        var nameHolder = document.createElement('div');
        nameHolder.textContent = 'Student Name';
        nameLabel.appendChild(nameHolder);
        row.appendChild(nameLabel);
        var buttonList = [];
        for (var i = 0; i < assignmentList.length; i++) {
            assignmentMap.set(assignmentList[i], i);
            var th = document.createElement('th');
            th.style.minWidth = '10ch';
            th.style.maxWidth = '11ch';
            th.dataset.assignemnt = assignmentList[i];
            var button = document.createElement('a');
            button.textContent = assignmentList[i];
            buttonList.push(button);
            // TODO: use this code once the single request method has been resolved
            // Don't push until that refactor has been complete.
            /*
            CourseSketch.dataManager.getAssignment(assignmentList[i], function(assignment) {
                button.textContent = assignment.name;
            });
            */
            button.className = 'waves-effect waves-teal btn-flat truncate';
            th.appendChild(button);
            row.appendChild(th);
        }

        // Remove this code once the single request method has been resolved.
        CourseSketch.dataManager.getAssignments(assignmentList, undefined, function(assignments) {
            for (var i = 0; i < assignments.length; i++) {
                // The order in the assigmment list may not match the order in the list of assigments.
                // this is used to match the order together so the right name goes with the correct button.
                var index = assignmentList.indexOf(assignments[i].id);
                buttonList[index].textContent = assignments[i].name;
            }
        });
        header.appendChild(row);
        table.appendChild(header);
    }

    /**
     * Adds the student names to the first column of every row.
     *
     * @param {Map<String, Element>} studentMap This is a map of studentIds to table rows.
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

    /**
     * Reverts cells to their unselected state.
     */
    function clearOpenedCells() {
        var selectedCell = document.querySelector('.gradeselected');
        if (selectedCell === null) {
            return;
        }
        unselectCell(selectedCell);
    }

    /**
     * Changes a grade input cell to the selected state.
     *
     * In this state, instructors can input their grades. This state has the input field.
     *
     * @param {Event} event The event that triggered the function.
     */
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

            /**
             * Sets validity message for checking that the user only entered numbers.
             */
            input.oninvalid = function() {
                this.setCustomValidity('Please enter numbers only.');
            };

            /**
             * Clears validity message for checking that the user only entered numbers.
             *
             * setCustomValidity must be cleared oninput otherwise the error message will continually appear and form will never appear valid.
             */
            input.oninput = function() {
                this.setCustomValidity('');
            };
            $(this).addClass('gradeSelected');
        }
    }

    /**
     * Turns a cell into the focused cell for grade input.
     */
    function createFocusedCell() {
        var template = document.querySelector('#inputTemplate');
        var container = document.importNode(template.content, true);
        return container;
    }

    /**
     * TODO: Make comment save to the grade for the specified assignment and student.
     * Adds a comment to a grade.
     *
     * @param {HTMLTableCell} cell The cell that the comment is being added for.
     */
    function addComment(cell) {
        console.log(cell);
        var addCommentButton = cell.querySelector('.addComment');
        var addCommentInput = cell.querySelector('.commentInput');
        addCommentButton.style.display = 'none';
        addCommentInput.style.display = 'block';
    }

    /**
     * Removes the selection of a cell by removing the html code inside and replacing it with a grade.
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
                console.log('SAVING GRADE: [', newGrade, ', ', comment, ']');
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

        // This is used to throttle events.
        var previousTimeStamp = 0;

        /**
         * Handles presses of Tab key and Enter key.
         *
         * When Tab is pressed, moves the selection to the right 1 cell.
         * When Enter is pressed, moves the selection down 1 cell.
         * Has a minimum time of 50ms between key events.
         * @param {Event} event The event that triggers the function.
         */
        keyEventHandler = function(event) {
            if (event.timeStamp - previousTimeStamp < 50) {
                previousTimeStamp = event.timeStamp;
                // less than 50 millis we return and don't process the event
                return;
            }
            previousTimeStamp = event.timeStamp;

            var cell = document.querySelector('.gradeselected');
            if (isUndefined(cell) || cell === null) {
                return; // Just in case there is no entry cell
            }
            var isTab = event.which === 9; // Keycode for tab key
            var isEnter = event.which === 13; // Keycode for enter key
            if (isEnter) {
                moveDown(cell);
            }
            if (isTab) {
                moveRight(cell);
            }
        };
    })();

    /**
     * TODO: Make this work if we need it.
     * Moves selection one cell to the left.
     *
     * @param {HTMLTableCell} cell The starting cell that we will move left from.
     */
    function moveLeft(cell) {
        unselectCell(cell);
    }

    /**
     * Moves selection one cell to the right.
     *
     * @param {HTMLTableCell} cell The starting cell that we will move right from.
     */
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

    /**
     * Moves selection one cell down.
     *
     * @param {HTMLTableCell} cell The starting cell that we will move down from.
     */
    function moveDown(cell) {
        unselectCell(cell);
        var currentRow = cell.parentNode;
        var rowIndex = getChildIndex(currentRow);
        var nextRow = currentRow.parentNode.children[getChildIndex(currentRow) + 1];
        var columnIndex = getChildIndex(cell);

        // If the end of the column is reached, there is no nextRow
        if (isUndefined(nextRow) || nextRow === null) {
            nextRow = currentRow.parentNode.children[0]; // Wrap back to the top row
            columnIndex += 1; // Wrap over one column
        }
        var nextCell = nextRow.children[columnIndex];

        gradeCellSelected.bind(nextCell)();
    }

    /**
     * TODO: Make this work if we need it.
     * Moves selection one cell up.
     *
     * @param {HTMLTableCell} cell The starting cell that we will move up from.
     */
    function moveUp(cell) {
        unselectCell(cell);
    }

    /**
     * Builds a ProtoGrade for a selected cell to send to the server.
     *
     * @param {HTMLTableCell} cell The cell the grade is coming from.
     * @param {String} grade The value of the grade.
     * @param {String} comment The comment for the grade.
     * @returns {ProtoGrade} The ProtoGrade from the selected cell.
     */
    function buildProtoGrade(cell, grade, comment) {
        grade = parseFloat(grade); // Sent in is a string. Proto requires a float.
        var protoGrade = CourseSketch.PROTOBUF_UTIL.ProtoGrade();
        protoGrade.setCourseId(CourseSketch.gradeBook.course.id);
        protoGrade.setUserId(cell.dataset.student);
        if (!isUndefined(cell.dataset.assignment)) {
            protoGrade.setAssignmentId(cell.dataset.assignment);
        }
        if (!isUndefined(cell.dataset.problem)) {
            protoGrade.setProblemId(cell.dataset.problem);
        }
        if (!isNaN(grade)) {
            protoGrade.setCurrentGrade(grade);
        }
        var gradeHistory = CourseSketch.PROTOBUF_UTIL.GradeHistory();
        if (!isNaN(grade)) {
            gradeHistory.setGradeValue(grade);
        }
        if (!isUndefined(comment)) {
            gradeHistory.setComment(comment);
        }
        protoGrade.setGradeHistory(gradeHistory); // Don't need to add to list since there is only one gradeHistory value
        return protoGrade;
    }

    /**
     * Returns the index of an element in reference to its parent element.
     */
    function getChildIndex(element) {
        var k = -1;
        var e = element;
        while (e) {
            if ('previousSibling' in e) {
                e = e.previousSibling;
                k = k + 1;
            } else {
                k = -1;
                break;
            }
        }
        return k;
    }

    /*
    function expandColumns(assignmentId, problemList, index) {

        // create an assignmentMap that has index is set so that it will insert grades in the problem slot (this means the indexes are offset)
    }
    */

    /*
    function viewAssignmentBreakdown(cell) {
        var index = getChildIndex(cell);
        var assignmentId = cell.dataset.assignment;
        CourseSketch.dataManager.getAssignment(assignemntId, function(assignment) {
            CourseSketch.dataManager.getCourseProblems(assignment.problemList, undefined, function(courseProblems) {

            });
        });
    }
    */
})();
