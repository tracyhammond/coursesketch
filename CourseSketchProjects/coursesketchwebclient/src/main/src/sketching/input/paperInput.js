/**
 * Contains input listeners for canvas interaction and functions for creating points using drawing events.
 *
 * @constructor InputListener
 */
function InputListener() {
    /**
     * @type {SRL_Point}
     */
    var currentPoint;

    /**
     * @type {SRL_Point}
     */
    var pastPoint;

    /**
     * @type {SRL_Stroke}
     */
    var currentStroke;

    /**
     * @typedef {Object} PSTool.
     * @member {PSTool}
     * @memberof InputListener
     */
    var tool = undefined;
    var totalZoom = 0;

    var initialWidth;
    var initialHeight;

    var scaleX = 1;
    var scaleY = 1;
    var mutationObserver;

    /**
     * Creates mouse listeners that enable strokes, panning, and zooming.
     *
     * @function initializeCanvas
     * @instance
     * @memberof InputListener
     * @param {Element} sketchCanvas - The element that takes in the user input.
     * @param {Function} strokeCreationCallback - The function that is called when a stroke is created.
     * @param {Graphics} graphics - The graphics object used to display the sketch.
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
         *
         * @memberof InputListener#initializeCanvas
         * @param {Number} delta - The amount with witch the zoom was changed.
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
         * A listener that attempts to listen for 2 finger scroll events so that tablets can scroll the sketch surface.
         *
         * @param {Event} event - The event that contains normal event data.
         * @param {String} phase - The phases of the event.  ("start", "move", "end")
         * @param {Element} $target - The element the event is based off of.
         * @param {Object} data - Contains:<ul>
         *                  <li>movePoint</li>
         *                  <li>lastMovePoint</li>
         *                  <li>startPoint</li>
         *                  <li>velocity</li>
         *                  </ul>
         */
        $(sketchCanvas).bind('touchy-drag', function(event, phase, $target, data) {
            if (phase === 'start') {
                startingPoint = data.startPoint;
                startingCenter = ps.project.activeLayer.localToGlobal(ps.view.center);
            } else {
                ps.view.center = startingCenter.subtract(ps.project.activeLayer.
                        localToGlobal(data.movePoint).subtract(startingPoint));
            }
        });
        $(sketchCanvas).data('touchy-drag').settings.requiredTouches = 2;

        /**
         * If shift is held, pans else if shift is not held, it starts a new path from the mouse point.
         *
         * @function onMouseDown
         * @memberof PSTool
         * @param {Event} event - The event from pressing the mouseDown.
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
                graphics.createNewPath(scalePoint(event.point));
                pastPoint = currentPoint;
            }
        };

        /**
         * If shift is held, pans the view to follow the mouse else if shift is not held, it adds more points to the path created on MouseDown.
         *
         * @function onMouseDown
         * @memberof PSTool
         * @param {Event} event - The event from dragging the mouse.
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
                if (!currentStroke) {
                    tool.onMouseUp(event);
                }
                currentStroke.addPoint(currentPoint);
                graphics.updatePath(scalePoint(event.point));
                pastPoint = currentPoint;
            }
        };

        /**
         * Finishes up the path that has been created by the mouse pointer, unless shift has been held, then it throws up.
         *
         * @function onMouseDown
         * @memberof PSTool
         * @param {Event} event - The event from releasing the mouse.
         */
        tool.onMouseUp = function(event) {
            if (!currentStroke) {
                currentStroke = false;
                currentPoint = false;
                return;
            }
            currentPoint = createPointFromEvent(event);
            //currentPoint.setSpeed(pastPoint);
            currentStroke.addPoint(currentPoint);
            currentStroke.setTime(currentPoint.getTime());
            currentStroke.finish();
            graphics.endPath(scalePoint(event.point), currentStroke);
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

        /**
         * Called for resizing the graphics.
         *
         * @param width
         * @param height
         */
        this.resize = function(width, height) {
            if (!graphics.isActivated()) {
                setCanvasSize(width, height);
            } else {
                rescaleCanvas(width, height);
            }
        };

        /**
         * Some manual unlinking to help out the garbage collector.
         */
        this.finalize = function() {
            strokeCreationCallback = undefined;
            graphics = undefined;
            sketchCanvas = undefined;
        };
    };

    function rescaleCanvas(width, height) {
        console.log('initial', initialWidth);
        console.log('new', width);
        scaleX = 1 // convertValue(initialWidth, width);
        scaleY = 1 // convertValue(initialHeight, height);
    }

    function convertValue(initialValue, newValue) {
        if (initialValue === newValue) {
            return 1;
        }
        if (initialValue < newValue) {
            return 1.0 - (initialValue / newValue);
        }
        if (initialValue > newValue) {
            return 1.0 + (newValue / initialValue);
        }
    }

    function setCanvasSize(width, height) {
        initialWidth = width ;
        initialHeight = height;
    }
    this.setCanvasSize = setCanvasSize;

    function scalePoint(eventPoint) {
        eventPoint.x = eventPoint.x * scaleX;
        eventPoint.y = eventPoint.y * scaleY;
        return eventPoint;
    }

    /**
     * Creates an {@link SRL_Point} from a drawing event. Returns the SRL_Point.
     *
     * @memberof InputListener
     * @private
     * @param {Event} drawingEvent - The event from paper drawing.
     * @returns {SRL_Point} The point created from this event.
     */
    function createPointFromEvent(drawingEvent) {
        console.log(scaleX);
        console.log(scaleY);
        var newPoint = new SRL_Point(drawingEvent.point.x * scaleX, drawingEvent.point.y * scaleY);
        newPoint.setId(generateUUID());
        newPoint.setTime(drawingEvent.event.timeStamp);
        if (!isUndefined(drawingEvent.pressure)) {
            newPoint.setPressure(drawingEvent.pressure);
        } else {
            newPoint.setPressure(0.5);
        }
        newPoint.setSize(0.5/*drawingEvent.size*/);
        newPoint.setUserCreated(true);
        return newPoint;
    }

    /**
     * Creates and returns a time stamp every time this function is called.
     *
     * @returns {Number} - The time stamp.
     */
    function createTimeStamp() {
        return new Date().getTime();
    }
}
