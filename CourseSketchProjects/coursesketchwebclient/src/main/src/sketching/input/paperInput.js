// TODO: change this to use hammer.js

function InputListener() {
    var currentPoint;
    var pastPoint;
    var currentStroke;
    var tool = undefined;

    this.initializeCanvas = function(sketchCanvas, strokeCreationCallback, graphics) {
        var ps = graphics.getPaper();
        tool = new ps.Tool();
        tool.minDistance = 5;
        tool.maxDistance = 5;
        ps.view.draw();
        sketchCanvas.addEventListener('mousemove', function(event) {
           console.log(event);
        }, false);
         sketchCanvas.addEventListener('touchmove', function(event) {
                    console.log(event);
                }, false);
        tool.on("mousedrag", function(event) {
            console.log(event);
        });
        tool.onMouseDown = function(event) {
            console.log("on Down");
            /*
            currentPoint = createPointFromEvent(event);
            currentStroke = new SRL_Stroke(currentPoint);
            currentStroke.setId(generateUUID());
            graphics.createNewPath(event.point);
            pastPoint = currentPoint;
            */
        };
        tool.onMouseDrag = function(event) {
            //console.log("on Drag");
            console.log(event);
            /*
            currentPoint = createPointFromEvent(event);
            //currentPoint.setSpeed(pastPoint);
            currentStroke.addPoint(currentPoint);
            graphics.updatePath(event.point);
            pastPoint = currentPoint;
            */
        };

        tool.onMouseUp = function(event) {
            console.log("on Up");
            /*
            currentPoint = createPointFromEvent(event);
            //currentPoint.setSpeed(pastPoint);
            currentStroke.addPoint(currentPoint);
            currentStroke.setTime(currentPoint.getTime());
            currentStroke.finish();
            graphics.endPath(event.point);
            try {
                if (strokeCreationCallback) {
                    strokeCreationCallback(currentStroke); // Sends back the current stroke.
                }
            } catch(err) {
                currentStroke = false;
                currentPoint = false;
            }
            currentStroke = false;
            currentPoint = false;
            */
        };
    }

    /**
     * Creates an {@link SRL_Point} from a drawing event.
     */
    function createPointFromEvent(drawingEvent) {
        var currentPoint = new SRL_Point(drawingEvent.x, drawingEvent.y);
        //currentPoint.setId(generateUUID());
        currentPoint.setTime(createTimeStamp());
        currentPoint.setPressure(drawingEvent.pressure);
        currentPoint.setSize(0.5/*drawingEvent.size*/);
        currentPoint.setUserCreated(true);
        return currentPoint;
    }

    // Creates a time stamp for every point.
    function createTimeStamp() {
        return new Date().getTime();
    };
}
