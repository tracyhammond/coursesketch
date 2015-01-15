/**
 * Installs PaperScope globally, and attaches it to the DomObject canvasElement
 */
function Graphics(canvasElement, sketchManager) {
    paper.install(window);

    /**
     * The paper object that is a scope for this specific canvas.
     * It is an instance of PaperScope.
     * Contains view, project, and handles input.
     */
    var ps = undefined;
    var livePath = undefined;
    var canvasElement = $(canvasElement)[0];
    var drawUpdate = true;

    /**
     * the last stroke the user drew.  Used to prevent the creation of two paths.
     */
    var lastStroke = undefined;

    ps = new paper.PaperScope(canvasElement);
    ps.setup(canvasElement);

    /**
     * Resizes the canvasElement to the size of the canvasElement's container
     */
    this.correctSize = function correctSize() {
        var oldHeight = canvasElement.height;
        var oldWidth = canvasElement.width;
        canvasElement.height = $(canvasElement).parent().height();
        canvasElement.width = $(canvasElement).parent().width();
        if (oldHeight != canvasElement.height || oldWidth != canvasElement.width) {
            ps.view.viewSize.setHeight(canvasElement.height);
            ps.view.viewSize.setWidth(canvasElement.width);
        }
    };

    /**
     * Expands or shrinks the sketch so that it fills the canvas while keeping the same aspect ratio.
     * This does modify the data of the sketch so this can only be used on read only sketches.
     *
     * uses the canvas size.
     */
    this.fillCanvas = function() {
        ps.project.activeLayer.fitBounds(new ps.Rectangle(0, 0, canvasElement.width, canvasElement.height));
        ps.view.update();
    }

    /**
     * Updates the view at 60fps
     */
    ps.view.onFrame = function(event) {
        if (event.count <= 1) {
            this.correctSize();
        }
        ps.view.update();
    }.bind(this);

    /**
     * Starts a new path in the view at the given point
     */
    this.createNewPath = function(point) {
        livePath = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        livePath.add(point);
    };

    /**
     * Adds a given point to the active path
     */
    this.updatePath = function(point) {
        livePath.add(point);
    };

    /**
     * Adds a final given point to the path, then simplifies it (makes it pretty and smooth)
     */
    this.endPath = function(point, stroke) {
        livePath.add(point);
        livePath.simplify();
        lastStroke = stroke;
        livePath.data.id = stroke.getId();
    };

    /**
     * @return the PaperScope (will return scope of a specific element via a parameter)
     */
    this.getPaper = function() {
        return ps;
    };

    /**
     * Sequentially loads all of the saved strokes from the beginning, and does so instantaneously.
     */
    this.loadSketch = function() {
        lastStroke = undefined;
        ps.project.activeLayer.removeChildren();
        ps.view.update();
        var sketch = sketchManager.getCurrentSketch();
        var objectList = sketch.getList();
        for (var i = 0; i < objectList.length; i++) {
            var object = objectList[i];
            if (object instanceof SRL_Stroke) {
                loadStroke(object);
            }
        }
        ps.view.update();
    };

    /**
     * Removes an item from the view.
     */
    this.removeItem = function(itemId) {
        var object = ps.project.getItem({data: {id : itemId} });
        console.log("Removing item!");
        console.log(object);
        object.remove();
        //var result = ps.project.activeLayer.removeChild(object);
        ps.view.update();
    };

    /**
     * Draws a single stroke onto the screen.
     * @param stroke {Srl_Stroke} the stroke to be drawn.
     */
    function loadStroke(stroke) {
        if (lastStroke == stroke) {
            return; // we do not need to double path.
        }
        var path = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        path.data.id = stroke.getId();
        var pointList = stroke.getPoints();
        for (var i = 0; i < pointList.length; i++) {
            path.add(new ps.Point(pointList[i].getX(), pointList[i].getY()));
        }
        path.simplify();
    }

    /**
     * Adds ability to draw the command as it is added to the update list.
     */
    this.addUpdate = function addUpdate(update, redraw, updateIndex) {
        if (!drawUpdate) {
            return;
        }
        var commandList = update.commands;
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            if (command.commandType == CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE) {
                var stroke = command.decodedData;
                loadStroke(stroke);
            }
        }
        if (redraw) {
            ps.view.update();
        }
    }

    /**
     * @param drawInstant {boolean} If false this will tell the graphics to not draw anytime it receives an update.
     */
    this.setDrawUpdate = function(drawInstant) {
        drawUpdate = drawInstant;
    };

    /**
     * some manual unlinking to help out the garbage collector.
     */
    this.finalize = function() {
        sketchManager = undefined;
        canvasElement = undefined;
        ps = undefined;
    };
}
