/**
 * Plays back the user's commands from the beginning.
 * Strokes are drawn in real time, in sequence.
 * The other commands, such as undo/redo/clear are also called in sequence.
 */

function Playback(updateList, updateManager, graphics) {
    var currentIndex = -1;
    this.addUpdate = function addUpdate(update, redraw, updateIndex) {
        var commandList = update.commands;

        // sets up a barrier for the commands.
        // calls playNext after all strokes are finished.
        var commandBarrier = new CallbackBarrier();
        var commandFinished = commandBarrier.getCallbackAmount(commandList.length);
        commandBarrier.finalize(this.playNext);

        // runs through all of the commands in the update.
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            if (command.commandType == CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE) {
                (function() {
                    var stroke = command.decodedData;
                    var pointList = stroke.getPoints();

                    // set up the barrier...
                    var strokeBarrier = new CallbackBarrier();
                    var pointAdded = strokeBarrier.getCallbackAmount(pointList.length);
                    strokeBarrier.finalize(function() {
                        commandFinished();
                    });
                    var strokePath = new graphics.getPaper().Path();

                    var startingTime = pointList[0].getTime();
                    for (var i = 0; i < pointList.length; i++) {
                        (function(index) {
                            setTimeout(function() {
                                strokePath.add(new graphics.getPaper().Point(pointList[index].getX(), pointList[index].getY()));
                                graphics.getPaper().view.update();
                                pointAdded();
                            }, pointList[index].getTime() - startingTime);
                        })(i);
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

    this.playNext = function() {
        currentIndex++;
        if (currentIndex == 0) {
            graphics.getPaper().project.activeLayer.removeChildren();
            graphics.getPaper().view.update();
        }
        if (currentIndex >= updateList.length) {
            console.log("Finished");
            return;
        }
        updateManager.addUpdate(updateList[currentIndex]);

    }
}
