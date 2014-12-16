/*
 * this Multiview page goes off a single problem at a time and laods all student experiments of that
 * problem id.
 */

(function() {
	/*
	 * a list of experiments to laod into the sketching pannels
	 * gets all experiments that hold the current problem id and places them is
	 * sketchList
	 */

	function getSketches( callback ){
	    CourseSketch.dataManager.getAllExperiments(CourseSketch.problemNavigator.getCurrentProblemId(), function(sketchList) {
	         if (isUndefined(sketchList)) {
				if (element.isRunning()) {
					element.finishWaiting();
				}
				return;
			} 
			if (!isUndefined(callback)) {
				callback(sketchList);
			}
	    });
	}

	/*
	 * used to get list of eperiments and then calls createMvSketch to create
	 * all sketches on to the grade screen.
	 */

	function createMvList(){
        getSketches(createMvSketch);
    }
	/*
	 * creates a multiview sketch panel and attaches it to the grading area 
	 * this can be done dynamically
	 */
        
	function createMvSketch(array) {
		for(var i = 0; i < array.length(); i++){
			var newelem = document.createElement('mv-sketch');
			document.quereySeletor("sketch-area").appendChild(newelem);
			newelem.setUpdateList(getUList(array, i));
		}
	}
	/*
	 * gets a specific set of sketch data to be used in the multiview sketch panel
	 *
	 *@param array
	 *       {array<experiments>}
	 *@param index
	 *		 {int}
	 */
	function getUList(array, index) {
		return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(
			array[index].getSubmission().getUpdateList(),
			CourseSketch.PROTOBUF_UTIL.getSrlUpdateListClass());
	}

	/*
	 * wipes the previous sketches, and laods the next problem into sketchList
	 * and then places them into the Multiview screen.
	 */

	function nextProblem() {
		multiviewSketchdelete(sketchList);
		CourseSketch.problemNavigator.gotoNext();
		createMvList();
	}

	/*
	 * wipes the previous sketches, and laods the previous problem into sketchList
	 * and then places them into the Multiview screen.
	 */

	function previousProblem() {
		multiviewSketchdelete(sketchList);
		CourseSketch.problemNavigator.gotoPrevious();
		createMvList();
	}

	/*
	 * used to delete the array of experiments inorder to load a new set of sketches based on the problem
	 *
	 *@param array
	 *				{array<experiments>}
	 */

	function multiviewSketchdelete(array) {
		for(var i = 0; i < array.length; i++){
			CourseSketch.SKETCHING_SURFACE_HANDLER.deleteSketch(array[i]);
		}
	}
})();