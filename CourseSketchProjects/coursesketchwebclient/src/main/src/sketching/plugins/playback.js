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
    var isPlaying = true;
    var pauseDuringStroke = false;
    var lastPausedIndex = Number.MAX_VALUE;
    var lastCreatedStroke = undefined;
    var lastPointAdded = undefined;
    var startTime;
    var pointList;

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
            if (command.commandType === CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE && isPlaying) {
                (function() {
                    var stroke = command.decodedData;
                    pointList = stroke.getPoints();

                    // set up the barrier...
                    var strokeBarrier = new CallbackBarrier();
                    var pointAdded = strokeBarrier.getCallbackAmount(pointList.length);

                    if (pauseDuringStroke) {
                        pointAdded = lastPointAdded;
                    }

                    var strokePath = new ps.Path({ strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black' });
                    if (pauseDuringStroke) {
                        strokePath = lastCreatedStroke;
                    }
                    strokeBarrier.finalize(function() {
                        strokePath.simplify();
                        commandFinished();
                    });
                    console.log(ps);

                    var startingTime = pointList[0].getTime();
                    var t;
                    var tList = [];
                    var startingIndex = 0;
                    if (pauseDuringStroke) {
                        startingIndex = lastPausedIndex;
                        pauseDuringStroke = false;
                        lastPausedIndex = Number.MAX_VALUE;
                    }
                    for (var i = startingIndex; i < pointList.length; i++) {

                        (function(index) {
                            t = setTimeout(function() {
                                if (isPlaying) {
                                    strokePath.add(new ps.Point(pointList[index].getX(), pointList[index].getY()));
                                    graphics.getPaper().view.update();
                                    pointAdded();
                                } else if (!isPlaying) { //pause during the stroke
                                    for (var j = 0; j < tList.length; j++) {
                                        clearTimeout(tList[j]);
                                    }
                                    if (lastPausedIndex > index) {
                                        lastPausedIndex = index;
                                    }
                                    lastCreatedStroke = strokePath;
                                    lastPointAdded = pointAdded;
                                    pauseDuringStroke = true;
                                    console.log('PAUSE!!!');

                                }
                            }, pointList[index].getTime() - startingTime);
                            tList.push(t);
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

    this.playNext = function(sTime) {
        if (!isUndefined(sTime)) {
            startingTime = sTime;
        }
        graphics.setDrawUpdate(false);
        currentIndex++;
        if (currentIndex === 0) {
            graphics.getPaper().project.activeLayer.removeChildren();
            graphics.getPaper().view.update();
        }
        if (currentIndex >= length) {
            graphics.setDrawUpdate(true);
            console.log('Finished');
            return;
        }
        var currentTime = (new Date().getTime());
        isPlaying = true;
        if (!pauseDuringStroke) {
            var playTime = currentTime - startingTime; //time play button pressed
            var updateTime = ((updateList[currentIndex].getTime()).subtract(updateList[0].getTime())).toNumber();
            setTimeout(function() {
                updateManager.addUpdate(updateList[currentIndex]);
            }, updateTime - playTime);
        } else {
            this.addUpdate(updateList[currentIndex], true, currentIndex);
        }
    };

    this.pauseNext = function() {
        currentIndex--;
        isPlaying = false;
    };
}
