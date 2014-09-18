/**
 * @class
 */
function SketchSurface() {
    var sketch = new SRL_Sketch();
    var updateList = undefined;
    var localInputListener = undefined;
    var sketchEventConverter = undefined;
    var shadowRoot = undefined;

    this.setRoot = function(root) {
        shadowRoot = root;
    };

    this.getSrlSketch = function() {
        return sketch;
    };

    this.bindToUpdateList = function(updateListToSet) {
        if (isUndefined(updateList)) {
            updateList = updateListToSet;
        } else {
            throw new Error("Update list is already defined");
        }
    };

    /**
     * Draws the stroke then creates an update that is added to the update
     * manager, given a stroke.
     * 
     * @param stroke
     *            {SRL_Stroke} a stroke that is added to the sketch.
     */
    function addStrokeCallback(stroke) {
        stroke.draw(sketch.canvasContext);

        var command = PROTOBUF_UTIL.createBaseCommand(PROTOBUF_UTIL.CommandType.ADD_STROKE, true);
        // then finish him!
        command.commandData = stroke.sendToProtobuf(parent).toArrayBuffer();
        command.decodedData = stroke;
        var update = PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
        if (updateList) {
            updateList.addUpdate(update, false);
        }
    }

    /**
     * @param InputListener
     *            {InputListenerClass} a class that represents an input listener
     * @param SketchEventConverter
     *            {SketchEventConverterClass} a class that represents
     */
    this.initialize = function(InputListener, SketchEventConverter) {
        localInputListener = new InputListener();
        var canvas = shadowRoot.querySelector("#drawingCanvas");
        localInputListener.initializeCanvas(canvas);
        var canvasContext = localInputListener.canvasContext;
        sketch.canvasContext = canvasContext;
        sketchEventConverter = new SketchEventConverter(localInputListener, addStrokeCallback, canvasContext);
    };
}

SketchSurface.prototype = Object.create(HTMLElement.prototype);
