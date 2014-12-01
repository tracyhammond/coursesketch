(function() {
	function getSketchs( callback ){
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
	 */
	function getUList(array, index) {
		return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(
			array[index].getSubmission().getUpdateList(),
			CourseSketch.PROTOBUF_UTIL.getSrlUpdateListClass());
	}
})();