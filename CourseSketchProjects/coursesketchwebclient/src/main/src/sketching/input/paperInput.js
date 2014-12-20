// TODO: change this to use hammer.js

function InputListener() {
    var currentPoint;
    var pastPoint;
    var currentStroke;
    var tool = undefined;
    var totalZoom = 0;

    this.initializeCanvas = function(sketchCanvas, strokeCreationCallback, graphics) {
        var ps = graphics.getPaper();
        tool = new ps.Tool();
        tool.fixedDistance = 5;

        var startingCenter;
        var startingPoint;
        var lastPoint;
        tool.onMouseDown = function(event) {
            if (Key.isDown('shift') || event.event.button == 1) {
                // do panning
                startingPoint = ps.project.activeLayer.localToGlobal(event.point);
                startingCenter= ps.project.activeLayer.localToGlobal(ps.view.center);

            } else {
                currentPoint = createPointFromEvent(event);
                currentStroke = new SRL_Stroke(currentPoint);
                currentStroke.setId(generateUUID());
                graphics.createNewPath(event.point);
                pastPoint = currentPoint;
            }
        };
        tool.onMouseDrag = function(event) {
            if (Key.isDown('shift') || event.event.button == 1) {
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
            } catch(err) {
                currentStroke = false;
                currentPoint = false;
                console.log(err);
            }
            currentStroke = false;
            currentPoint = false;
        };

        sketchCanvas.addEventListener("mousewheel", function(event) {
            event.stopPropagation();
            event.preventDefault();
            // cross-browser wheel delta
            var e = window.event || e; // old IE support
            var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
            totalZoom += delta;
            if (totalZoom < 0) {
                 ps.view.zoom = -1/totalZoom;
            } else {
                ps.view.zoom = totalZoom + 1;
            }
        });
    }

    /**
     * Creates an {@link SRL_Point} from a drawing event.
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

    // Creates a time stamp for every point.
    function createTimeStamp() {
        return new Date().getTime();
    };
}
