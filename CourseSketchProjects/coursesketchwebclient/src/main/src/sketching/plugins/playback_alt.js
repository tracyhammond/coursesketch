/**
 * Plays back the user's commands from the beginning.
 * Strokes are drawn in real time, in sequence.
 * The other commands, such as undo/redo/clear are also called in sequence.
 * @param {Array} updateList the list of updates to be inserted in real time.
 * @param {UpdateManager} updateManager  The manager of the updates.
 * @param {Graphics} graphics used to draw objects to the screen.
 */
function Playback(updateList, updateManager, graphics) {
    var ps = graphics.getPaper();
    var currentIndex = -1;
    var length = updateList.length;
    var target = length;
    this.addUpdate = function addUpdate(update, redraw, updateIndex) {
        var commandList = update.commands;
        // sets up a barrier for the commands.
        // calls playNext after all strokes are finished.
        var commandBarrier = new CallbackBarrier();
        var commandFinished = commandBarrier.getCallbackAmount(commandList.length);
        commandBarrier.finalize(this.playNext(target));

        // runs through all of the commands in the update.
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            if (command.commandType === CourseSketch.prutil.CommandType.ADD_STROKE) {
                (function() {
                    var stroke = command.decodedData;
                    var pointList = stroke.getPoints();

                    // set up the barrier...
                    var strokeBarrier = new CallbackBarrier();
                    var pointAdded = strokeBarrier.getCallbackAmount(pointList.length);

                    var strokePath = new ps.Path({ strokeWidth: 2, strokeCap: 'round', selected: false, strokeColor: 'black' });
                    strokeBarrier.finalize(function() {
                        strokePath.simplify();
                        commandFinished();
                    });
                    console.log(ps);

                    var startingTime = pointList[0].getTime();
                    for (var innerIndex = 0; innerIndex < pointList.length; innerIndex++) {

                        (function(index) {
                            setTimeout(function() {
                                strokePath.add(new ps.Point(pointList[index].getX(), pointList[index].getY()));
                                graphics.getPaper().view.update();
                                pointAdded();
                            }, pointList[index].getTime() - startingTime);
                        })(innerIndex);
                    }
                })();
            } else {
                if (redraw) {
                    graphics.getPaper().view.update();
                }
                commandFinished();
            }
        }
    };

    this.playNext = function(spot) {
        target = spot;
        graphics.setDrawUpdate(false);
        if (currentIndex > target){currentIndex--;}
        if (currentIndex < target){currentIndex++;}
        if (currentIndex === 0) {
            graphics.getPaper().project.activeLayer.removeChildren();
            graphics.getPaper().view.update();
        }
        if (currentIndex === target || currentIndex >= length) {
            graphics.setDrawUpdate(true);
            console.log('Finished');
            currentIndex = -1;
            return;
        }
        updateManager.addUpdate(updateList[currentIndex]);
    };
}
