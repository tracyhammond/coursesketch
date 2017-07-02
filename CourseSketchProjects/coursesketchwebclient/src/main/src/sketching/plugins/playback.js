/**
 * Plays back the user's commands from the beginning.
 * Strokes are drawn in real time, in sequence.
 * The other commands, such as undo/redo/clear are also called in sequence.
 *
 * @param {Array} updateList - The list of updates to be inserted in real time.
 * @param {UpdateManager} updateManager - The manager of the updates.
 * @param {Graphics} graphics - Used to draw objects to the screen.
 */
function Playback(updateList, updateManager, graphics) {
    var ps = graphics.getPaper();
    var currentIndex = -1;
    var length = updateList.length;

    /**
     * Whether or not the sketching is currently playing back.
     *
     * @type {Boolean}
     */
    var isPlaying = true;

    /**
     * Whether or not the sketch was paused in the middle of a stroke.
     *
     * @type {Boolean}
     */
    var pauseDuringStroke = false;

    /**
     * Keeps track of the last index the playback was on when it paused.
     *
     * @type {Integer}
     */
    var lastPausedIndex = Number.MAX_VALUE;

    /**
     * Last stroke that was played back.
     *
     * @type {PaperStroke}
     */
    var lastCreatedStroke = undefined;

    /**
     * Last point that was added to the canvas.
     *
     * @type {SRL_Point}
     */
    var lastPointAdded = undefined;
    var pointList;

    /**
     * Adds an update that is played back slowly.
     *
     * @param {SrlUpdate} update - The update that the sketch list is currently replaying
     * @param {Boolean} redraw - True if the sketch surface needs to be redrawn.
     * @param {Integer} updateIndex - The index that this update is in the update list.
     */
    this.addUpdate = function addUpdate(update, redraw, updateIndex, updateType, updatePluginId) {
        var commandList = update.commands;

        /**
         * Sets up a barrier for the commands.
         * Calls playNext after all strokes are finished.
         */
        var commandBarrier = new CallbackBarrier();
        var commandFinished = commandBarrier.getCallbackAmount(commandList.length);
        commandBarrier.finalize(this.playNext);

        /*
         * Runs through all of the commands in the update.
         */
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            if (command.commandType === CourseSketch.prutil.CommandType.ADD_STROKE && isPlaying) {
                (function() {
                    var stroke = command.decodedData;
                    pointList = stroke.getPoints();

                    // set up the barrier...
                    var strokeBarrier = new CallbackBarrier();
                    var pointAdded = strokeBarrier.getCallbackAmount(pointList.length);
                    var strokePath = new ps.Path({ strokeWidth: 2, strokeCap: 'round', selected: false, strokeColor: 'black' });
                    if (pauseDuringStroke) {
                        pointAdded = lastPointAdded;
                        strokePath = lastCreatedStroke;
                    }
                    strokeBarrier.finalize(function() {
                        strokePath.simplify();
                        commandFinished();
                    });

                    var startingTime = pointList[0].getTime();
                    var timeOut;
                    var timeOutList = [];
                    var startingIndex = 0;
                    if (pauseDuringStroke) {
                        startingIndex = lastPausedIndex;
                        pauseDuringStroke = false;
                        lastPausedIndex = Number.MAX_VALUE;
                    }
                    for (var pointIndex = startingIndex; pointIndex < pointList.length; pointIndex++) {
                        (function(index) {
                            timeOut = setTimeout(function() {
                                if (isPlaying) {
                                    strokePath.add(new ps.Point(pointList[index].getX(), pointList[index].getY()));
                                    graphics.getPaper().view.update();
                                    pointAdded();
                                } else if (!isPlaying) { //pause during the stroke
                                    for (var j = 0; j < timeOutList.length; j++) {
                                        clearTimeout(timeOutList[j]);
                                    }
                                    if (lastPausedIndex > index) {
                                        lastPausedIndex = index;
                                    }
                                    lastCreatedStroke = strokePath;
                                    lastPointAdded = pointAdded;
                                    pauseDuringStroke = true;

                                }
                            }, pointList[index].getTime() - startingTime);
                            timeOutList.push(timeOut);
                        })(pointIndex);
                    } // end of for loop
                })();
            } else {
                if (redraw) {
                    graphics.getPaper().view.update();
                }
                commandFinished();
            }
        }
    };

    /**
     * Calculates time between strokes and plays them back with a delay corresponding to this time.
     *
     * Also playback the sketch back from saved stroke index if it is paused.
     *
     * @param {Long} startTime - the time for when the sketch started.
     * @param {SketchSurface} surface - the surface.
     */
    this.playNext = function(startTime, surface) {
        if (!isUndefined(startTime)) {
            startingTime = startTime;
        }
        graphics.setDrawUpdate(false);
        currentIndex++;
        if (currentIndex === 0) {
            graphics.getPaper().project.activeLayer.removeChildren();
            graphics.getPaper().view.update();
        }
        if (currentIndex >= length) {
            graphics.setDrawUpdate(true);
            return;
        }
        var currentTime = (new Date().getTime());

        isPlaying = true;
        if (!pauseDuringStroke) {
            /*
             * Time passed from start to current stroke.
             */
            var playTime = currentTime - startingTime;

            /*
             * Time of the next stroke.
             */
            var updateTime = ((updateList[currentIndex].getTime()).subtract(updateList[0].getTime())).toNumber();

            /*
             * Time between the last played stroke and the next one.
             */
            var delayTime = updateTime - playTime;
            if (currentIndex === 1 || currentIndex === 0) {
                delayTime = 0;
            }
            setTimeout(function() {
                updateManager.addUpdate(updateList[currentIndex]);
            }, delayTime);
        } else {
            this.addUpdate(updateList[currentIndex], true, currentIndex);
        }
    };

    /**
     * Set isPlaying to false and pause the drawing.
     */
    this.pauseNext = function() {
        currentIndex--;
        isPlaying = false;
    };
}
