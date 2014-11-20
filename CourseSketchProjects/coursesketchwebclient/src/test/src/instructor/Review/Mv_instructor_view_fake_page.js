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


	function createMvSketch(array) {
		for(var i = 0; i < array.length(); i++){
			var newelem = document.createElement('mv-sketch');
			document.quereySeletor("sketch-area").appendChild(newelem);
			newelem.setUpdateList(getUList(array, i));
		}
	}

	function getUList(array, index) {
		return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(
			array[index].getSubmission().getUpdateList(),
			CourseSketch.PROTOBUF_UTIL.getSrlUpdateListClass());
	}
})();