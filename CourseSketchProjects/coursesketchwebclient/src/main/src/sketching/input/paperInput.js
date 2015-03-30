/**
 * Contains input listeners for canvas interaction and functions for creating points using drawing events
 * @class InputListener
 */
function InputListener() {
    var currentPoint;
    var pastPoint;
    var currentStroke;
    /**
     * @typedef {Object} PSTool.
     * @member {PSTool}
     * @memberof InputListener
     */
    var tool = undefined;
    var totalZoom = 0;

    /**
     * Creates mouse listeners that enable strokes, panning and zooming.
     * @function initializeCanvas
     * @instance
     * @memberof InputListener
     */
    this.initializeCanvas = function(sketchCanvas, strokeCreationCallback, graphics) {
        var ps = graphics.getPaper();
        tool = new ps.Tool();
        /**
         * The pixel distance between points when recording points.
         * @member {Number}
         * @memberof PSTool
         */
        tool.fixedDistance = 5;

        //used for panning and zooming
        var startingCenter;
        var startingPoint;
        var lastPoint;

        /**
         * Allows you to zoom in or out based on a delta.
         * Attempts to limit zoom out to an exponential decay function.
         * This also makes sure that the final zoom function is in fact linear.
         * @memberof InputListener#initializeCanvas
         */
        function zoom(delta) {
            var oldZoom = totalZoom;
            totalZoom += delta;
            if (totalZoom < 0 && totalZoom > -1) {
                ps.view.zoom = -1 / (totalZoom - 1);
            } else if (totalZoom <= -1) {
                ps.view.zoom = -1 / (totalZoom - 1);
            } else {
                //console.log(totalZoom);
                ps.view.zoom = totalZoom + 1;
            }
        }

        /**
         * If shift is held, pans else if shift is not held, it starts a new path from the mouse point.
         *
         * @function onMouseDown
         * @memberof PSTool
         */
        tool.onMouseDown = function(event) {
            if (Key.isDown('shift') || event.event.button === 1) {
                // do panning
                startingPoint = ps.project.activeLayer.localToGlobal(event.point);
                startingCenter = ps.project.activeLayer.localToGlobal(ps.view.center);

            } else {
                currentPoint = createPointFromEvent(event);
                currentStroke = new SRL_Stroke(currentPoint);
                currentStroke.setId(generateUUID());
                graphics.createNewPath(event.point);
                pastPoint = currentPoint;
            }
        };

        /**
         * If shift is held, pans the view to follow the mouse else if shift is not held, it adds more points to the path created on MouseDown.
         * @function onMouseDown
         * @memberof PSTool
         */
        tool.onMouseDrag = function(event) {
            if (Key.isDown('shift') || event.event.button === 1) {
                // do panning
                currentStroke = undefined;
                ps.view.center =
                 startingCenter.subtract(ps.project.activeLayer.localToGlobal(event.point).subtract(startingPoint));
            } else {
                currentPoint = createPointFromEvent(event);
                //currentPoint.setSpeed(pastPoint);
                currentStroke.addPoint(currentPoint);
                graphics.updatePath(event.point);
                pastPoint = currentPoint;
            }
        };

        /**
         * finishes up the path that has been created by the mouse pointer, unless shift has been held, then it throws up.
         *
         * @function onMouseDown
         * @memberof PSTool
         */
        tool.onMouseUp = function(event) {
            currentPoint = createPointFromEvent(event);
            //currentPoint.setSpeed(pastPoint);
            currentStroke.addPoint(currentPoint);
            currentStroke.setTime(currentPoint.getTime());
            currentStroke.finish();
            graphics.endPath(event.point, currentStroke);
            try {
                if (strokeCreationCallback) {
                    strokeCreationCallback(currentStroke); // Sends back the current stroke.
                }
            } catch (err) {
                currentStroke = false;
                currentPoint = false;
                console.log(err);
            }
            currentStroke = false;
            currentPoint = false;
        };

        //zooms the view with the mousewheel
        sketchCanvas.addEventListener('mousewheel', function(event) {
            //event.stopPropagation();
            //event.preventDefault();
            // cross-browser wheel delta
            var e = window.event || e; // old IE support
            var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
            zoom(delta / 3);
        });

        $(sketchCanvas).bind('touchy-pinch', function(event, $target, data) {
            currentStroke = undefined;

            //event.stopPropagation();
            //event.preventDefault();
            console.log(data);

            // cross-browser wheel delta
            var e = window.event || e; // old IE support
            //var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
            zoom(data.scale - data.previousScale);
        });

        // makes zoom public.
        this.zoom = zoom;
    };

    /**
     * Creates an {@link SRL_Point} from a drawing event. Returns the SRL_Point.
     * @memberof InputListener
     * @private
     */
    function createPointFromEvent(drawingEvent) {
        var currentPoint = new SRL_Point(drawingEvent.point.x, drawingEvent.point.y);
        currentPoint.setId(generateUUID());
        currentPoint.setTime(drawingEvent.event.timeStamp);
        if (!isUndefined(drawingEvent.pressure)) {
            currentPoint.setPressure(drawingEvent.pressure);
        } else {
            currentPoint.setPressure(0.5);
        }
        currentPoint.setSize(0.5/*drawingEvent.size*/);
        currentPoint.setUserCreated(true);
        return currentPoint;
    }

    /**
     * Creates and returns a time stamp every time this function is called.
     * @returns {Number} - The time stamp.
     */
    function createTimeStamp() {
        return new Date().getTime();
    }
}
