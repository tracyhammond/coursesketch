(function() {
    /**
     *
     * @param commandElementId
     * @param sketchElementId
     * @param {SketchSurface} sketchSurface
     * @returns {{addUpdate: Function}}
     */
    CourseSketch.createDebugRecognitionPlugin = function(commandElementId, sketchElementId, sketchSurface) {
        return {
            addUpdate: function(pluginUpdate, redraw, updateIndex, updateType) {
                var sketchList = sketchSurface.getCurrentSketch().getList();
                var updateList = sketchSurface.getUpdateList();
                createSketchViewer(sketchList, sketchElementId);
                createCommandViewer(updateList, commandElementId, updateIndex);
            }
        }
    }
})();
