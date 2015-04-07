validateFirstRun(document.currentScript);
(function() {

    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    var courseManagement = CourseSketch.courseManagement;
    courseManagement.gradebookMode = true;
})();
 /**
     * Called when a user clicks on a course school item.
     * This loads the assignments from the database then calls 'showCourses' to display them.
     */
    courseManagement.courseClicked = function(course) {
      if (problemSelectionManager.isItemSelected(clickedElement)) {
            var itemNumber = clickedElement.dataset.item_number;
            CourseSketch.dataManager.addState('currentProblemIndex', itemNumber);
            CourseSketch.dataManager.addState('currentAssignment', problem.assignmentId);
            CourseSketch.dataManager.addState('CURRENT_QUESTION', problem.id);
            // change source to the problem page! and load problem
            if (CourseSketch.connection.isInstructor) {
                // solution editor page!
                CourseSketch.redirectContent('/src/instructor/review/multiviewGrading.html', 'Grading problems!');
            } else {
                CourseSketch.redirectContent('/src/student/experiment/experiment.html', 'Starting Problem');
            }
        } else {
            // TODO: find a more lightweight popup library
            /*
            var element = document.getElementById(id);
            var myOpenTip = new Opentip(element, {
                target : element,
                tipJoint : 'bottom'
            });
            myOpenTip.prepareToShow(); // Shows the tooltip after the given
            // delays. This could get interrupted

            if (CourseSketch.dataManager.getState('isInstructor')) {
                myOpenTip.setContent('Click again to edit the solution'); // Updates
                // Opentips
                // content
            } else {
                myOpenTip.setContent('Click again to open up a problem'); // Updates
                // Opentips
                // content
            }

            var pastToolTip = problemSelectionManager['currentToolTip'];
            if (pastToolTip) {
                pastToolTip.deactivate();
            }
            problemSelectionManager['currentToolTip'] = myOpenTip;
            */
            // note that queryselector is not allowed on these types of ids
            changeSelection(clickedElement, problemSelectionManager);
        }
    };  
    };
    