/* jshint camelcase: false */
/**
 * Installs PaperScope globally, and attaches it to the DomObject canvasElement.
 * @class Graphics
 * @param {Element} canvas the canvas element that is being drawn to.
 * @param {SketchManager} sketchManager - the manager that handles which sketch is currently active.
 */
function Graphics(canvas, sketchManager) {
    paper.install(window);

    /**
     * The paper object that is a scope for this specific canvas.
     * It is an instance of PaperScope.
     * Contains view, project, and handles input.
     */
    var ps = undefined;
    var livePath = undefined;
    var canvasElement = $(canvas)[0];
    var drawUpdate = true;

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
        if (oldHeight !== canvasElement.height || oldWidth !== canvasElement.width) {
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
        ps.activate();
        var boundary = new ps.Rectangle(ps.view.bounds);
        ps.project.activeLayer.fitBounds(boundary);
        ps.view.update();
    };

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
        livePath = new ps.Path({ strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black' });
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
        livePath.data.id = stroke.getId();
    };

    /**
     * @return {PaperScope} the PaperScope (will return scope of a specific element via a parameter)
     */
    this.getPaper = function() {
        return ps;
    };

    /**
     * Sequentially loads all of the saved strokes from the beginning, and does so instantaneously.
     */
    this.loadSketch = function() {
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
    var loadSketch = this.loadSketch;

    /**
     * Removes an item from the view.
     */
    this.removeItem = function(itemId) {
        var object = ps.project.getItem({ data: { id: itemId } });
        object.remove();
        ps.view.update();
    };
    var removeItem = this.removeItem;

    /**
     * Draws a single stroke onto the screen.
     * @param {Srl_Stroke} stroke the stroke to be drawn.
     */
    function loadStroke(stroke) {
        ps.activate();
        var object = ps.project.getItem({ data: { id: stroke.getId() } });
        if (!isUndefined(object) && object !== null) {
            return; // already added to the sketch.
        }
        var path = new ps.Path({ strokeWidth: 2, strokeCap: 'round', selected: false, strokeColor: 'black' });
        path.data.id = stroke.getId();
        var pointList = stroke.getPoints();
        for (var i = 0; i < pointList.length; i++) {
            path.add(new ps.Point(pointList[i].getX(), pointList[i].getY()));
        }
        path.simplify();
    }

    /**
     * Adds ability to draw the command as it is added to the update list.
     * @function addUpdate
     *
     * @memberof Graphics
     * @param {SrlUpdate} update the given update that is about to be executed.
     * @param {Boolean} redraw true if the given update should force a redraw of the canvas.
     * @param {Number} updateIndex the number index that this update occurs in the list of updates.
     * @param {Number} lastUpdateType the type of the last update which can either be 1, 0, -1. with 1 = redo, 0 = normal and -1 = undo.
     * @instance
     */
    this.addUpdate = function addUpdate(update, redraw, updateIndex, lastUpdateType) {
        ps.activate();
        if (!drawUpdate) {
            return;
        }
        var commandList = update.commands;
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            runCommand(command, lastUpdateType);
        }
        if (redraw) {
            ps.view.update();
        }
    };

    /**
     * @function runCommand
     * Runs a specific command given its lastUpdateType.
     * @param {SrlCommand} command the given command that is about to be executed.
     * @param {Number} lastUpdateType the type of the last update which can either be 1, 0, -1. with 1 = redo, 0 = normal and -1 = undo.
     * @instance
     */
    function runCommand(command, lastUpdateType) {
        if (command.commandType === CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE) {
            var stroke = command.decodedData;
            if (lastUpdateType === 0 || lastUpdateType === 1) {
                loadStroke(stroke);
            } else if (lastUpdateType === -1) {
                removeItem(stroke.getId());
            }
        } else if (command.commandType === CourseSketch.PROTOBUF_UTIL.CommandType.CLEAR) {
            if (lastUpdateType === 0 || lastUpdateType === 1) {
                ps.project.activeLayer.removeChildren();
            } else {
                loadSketch();
            }
        }
    }

    /**
     * @param {Boolean} drawInstant If false this will tell the graphics to not draw anytime it receives an update.
     */
    this.setDrawUpdate = function(drawInstant) {
        drawUpdate = drawInstant;
    };

    /**
     * Some manual unlinking to help out the garbage collector.
     */
    this.finalize = function() {
        sketchManager = undefined;
        canvasElement = undefined;
        ps = undefined;
    };
}
