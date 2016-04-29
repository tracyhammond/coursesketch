(function() {

    var defaultCategory = 'All Assignments';

    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var courseId = CourseSketch.dataManager.getState('gradebookCourseid');
            CourseSketch.gradeBook.loadGrades(courseId);
        });
    });

    /**
     * Loads grades for a given course.
     *
     * @param {String} courseId The ID of the course to load grades for.
     */
    CourseSketch.gradeBook.loadGrades = function(courseId) {
        CourseSketch.dataManager.getCourse(courseId, function(course) {
            CourseSketch.gradeBook.course = course;
            var categoryList = [ defaultCategory ];
            var assignmentList = course.assignmentList;
            CourseSketch.dataManager.getAssignments(assignmentList, undefined, function(assignments) {
                // loads all of the grades
                CourseSketch.dataManager.getAllAssignmentGrades(courseId, function(gradeList) {
                    var table = document.querySelector('.gradeList');
                    CourseSketch.gradeBook.initializeTable(categoryList, assignments, gradeList, table);
                });
            });
        });
    };

    /**
     * Initializes a table from the given values.
     *
     * @param {List<String>} categoryList List of category names.
     * @param {List<String>} assignmentList List of assignment IDs.
     * @param {List<ProtoGrade>} gradeList List of grades from the server.
     * @param {HTMLTable} table The grade table on the webpage.
     */
    CourseSketch.gradeBook.initializeTable = function(categoryList, assignmentList, gradeList, table) {
        table.innerHTML = '';

        var assignmentMap = new Map();
        var categoryMap = new Map();

        createCategoryHeader(categoryList, categoryMap, table);
        createAssignmentHeader(assignmentList, assignmentMap, categoryMap, table);
        CourseSketch.gradeBook.populateGrades(gradeList, assignmentMap, table);

        $('.collapsible').collapsible({
            accordion: false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
        });
    };

    /**
     * This populates the map of grades for each student and, calls student adding function if student has not been added yet.
     *
     * @param {List<ProtoGrade>} listGrades The list of grades from the server.
     * @param {Map<String, Element>} assignmentMap Map of assignmentIDs to table row.
     * @param {HTMLTable} table The grade table on the webpage.
     * @return {List<ProtoGrade>} grades that were not displayed. This is because the users do not exist anymore in the course roster.
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
     * @param {List<String>} categoryList List of category names.
     * @param {Map<String, Element>} categoryMap Map of category names to corresponding table element.
     * @param {HTMLTable} table The grade table on the webpage.
     */
    function createCategoryHeader(categoryList, categoryMap, table) {
        var template = document.querySelector('#expandableTemplate');
        var container = document.importNode(template.content, true);
        for (var i = 0; i < categoryList.length; i++) {
            var categoryElement = container.cloneNode(true);
            categoryMap.set(categoryList[i], categoryElement.firstElementChild);

            var header = categoryElement.querySelector('.collapsible-header');
            header.textContent = categoryList[i];
            $(header).addClass('mdi-av-play-arrow');

            table.appendChild(categoryElement);
        }
    }

    /**
     * Creates the assignment header while populating the assignment map.
     *
     * @param {List<SrlAssignment>} assignmentList List of assignments.
     * @param {Map<String, Element>} assignmentMap Map of assignmentIDs to corresponding table element.
     * @param {Map<String, Element>} categoryMap Map of category names to corresponding table element.
     * @param {Element} table The grade table on the webpage.
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
