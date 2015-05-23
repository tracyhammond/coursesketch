(function() {

    var defaultCategory = 'All Assignments';

    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var courseId = CourseSketch.dataManager.getState('gradebookCourseid');
            CourseSketch.gradeBook.loadGrades(courseId);
        });
    });

    CourseSketch.gradeBook.loadGrades = function(courseId) {
        CourseSketch.dataManager.getCourse(courseId, function(course) {
            CourseSketch.gradeBook.course = course;
            var categoryList = [ defaultCategory ];
            var assignmentList = course.assignmentList;
            CourseSketch.dataManager.getAssignments(assignmentList, undefined, function(assignments) {
                // loads all of the grades
                CourseSketch.dataManager.getAllAssignmentGrades(courseId, function(gradeList) {

                    var table = document.querySelector('#ALL');
                    CourseSketch.gradeBook.initializeTable(categoryList, assignments, gradeList, table);
                });
            });
        });
    };

    /**
     * Initializes a table from the given values.
     *
     * @param asignmentList
     * @param gradeList
     * @param studentList
     * @param table
     */
    CourseSketch.gradeBook.initializeTable = function(categoryList, assignmentList, gradeList, table) {
        table.innerHTML = '';

        var assignmentMap = new Map();
        var categorytMap = new Map();

        createCategoryHeader(categoryList, categorytMap, table);
        createAssignmentHeader(assignmentList, assignmentMap, categorytMap, table);
        CourseSketch.gradeBook.populateGrades(gradeList, assignmentMap, table);

        $('.collapsible').collapsible({
            accordion: false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
        });
    };

    /**
     * This populates the map of grades for each student and, calls student adding function if student has not been added yet.
     *
     * @param {List<String>} assignmentList The list of assignment IDs.
     * @param {List<ProtoGrade>} listGrades The list of grades from the server.
     * @return {List<ProtoGrade>} grades that were not displayed.   This is because the users do not exist anymore in the course roster.
     */
    CourseSketch.gradeBook.populateGrades = function(listGrades, assignmentMap, table) {
        var gradesNotShown = [];
        for (var i = 0; i < listGrades.length; i++) {
            var protoGrade = listGrades[i];
            var assignmentId = protoGrade.assignmentId;

            var assignment = assignmentMap.get(assignmentId);
            var stringGrade = '' + protoGrade.getCurrentGrade();
            var gradeElement = assignment.querySelector('.gradeValue');
            gradeElement.textContent = stringGrade.substring(0, 6);
        }
        return gradesNotShown;
    };

    /**
     * Creates the assignment header while populating the assignment map.
     *
     * @param {List<String>} assignmentList
     * @param {Element} table
     * @param {Map<String, Element>} assignmentMap
     */
    function createCategoryHeader(categoryList, categoryMap, table) {
        var template = document.querySelector('#expandableTemplate');
        var container = document.importNode(template.content, true);
        for (var i = 0; i < categoryList.length; i++) {
            var categoryElement = container.cloneNode(true);
            categoryMap.set(categoryList[i], categoryElement.firstElementChild);
            categoryElement.querySelector('.collapsible-header').textContent = categoryList[i];
            table.appendChild(categoryElement);
        }
    }

    /**
     * Creates the assignment header while populating the assignment map.
     *
     * @param {List<SrlAssignment>} assignmentList
     * @param {Element} table
     * @param {Map<String, Element>} assignmentMap
     */
    function createAssignmentHeader(assignmentList, assignmentMap, categoryMap, table) {
        var template = document.querySelector('#expandableTemplate');
        var container = document.importNode(template.content, true);

        var gradeTemplate = document.querySelector('#gradeHeader');
        var gradeContainer = document.importNode(gradeTemplate.content, true);
        for (var i = 0; i < assignmentList.length; i++) {
            var assignmentElement = container.cloneNode(true);
            assignmentMap.set(assignmentList[i].id, assignmentElement.firstElementChild);

            var assignmentHeader = gradeContainer.firstElementChild.cloneNode(true);

            console.log(assignmentHeader);
            assignmentElement.querySelector('.collapsible-header').appendChild(assignmentHeader);

            assignmentHeader.querySelector('.name').textContent = assignmentList[i].name;

            var collection = categoryMap.get(defaultCategory).querySelector('ul.collection');
            collection.appendChild(assignmentElement);
        }
    }
})();
